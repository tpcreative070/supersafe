package co.tpcreative.supersafe.common
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import butterknife.ButterKnife
import butterknife.Unbinder
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

abstract class BaseFragment : Fragment() {
    protected var unbinder: Unbinder? = null
    var isInLeft = false
    var isOutLeft = false
    var isCurrentScreen = false
    var isLoaded = false
    var isDead = false
    private val lock = ReentrantLock()
    private val condition = lock.newCondition()
    protected abstract fun getLayoutId(): Int
    protected abstract fun getLayoutId(inflater: LayoutInflater?, viewGroup: ViewGroup?): View?
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        isDead = false
        val viewResponse = getLayoutId(inflater, container)
        return if (viewResponse != null) {
            unbinder = ButterKnife.bind(this, viewResponse)
            work()
            viewResponse
        } else {
            val view: View = inflater.inflate(getLayoutId(), container, false)
            unbinder = ButterKnife.bind(this, view)
            work()
            view
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        lock.withLock {
            isLoaded = true
            condition.signalAll()
        }
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        isDead = true
        super.onDestroyView()
        unbinder?.unbind()
        hide()
    }

    override fun onDestroy() {
        super.onDestroy()
        remove()
        isLoaded = false
    }

    protected fun remove() {}
    protected fun hide() {}
    protected open fun work() {}
}