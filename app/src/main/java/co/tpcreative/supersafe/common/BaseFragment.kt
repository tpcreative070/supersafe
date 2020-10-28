package co.tpcreative.supersafe.common
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import co.tpcreative.supersafe.common.util.Utils
import com.google.gson.Gson
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

abstract class BaseFragment : Fragment() {
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
            //work()
            viewResponse
        } else {
            val view: View = inflater.inflate(getLayoutId(), container, false)
            //work()
            view
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        lock.withLock {
            isLoaded = true
            condition.signalAll()
        }
        work()
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        isDead = true
        super.onDestroyView()
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

    var TAG : String = this::class.java.simpleName

    fun <T>log(clazz: Class<T> , content : Any?){
        if (content is String){
            Utils.Log(clazz,content)
        }else{
            Utils.Log(clazz, Gson().toJson(content))
        }
    }

    fun log(fragment : Fragment, content : Any?){
        if (content is String){
            Utils.Log(fragment.javaClass,content)
        }else{
            Utils.Log(fragment.javaClass, Gson().toJson(content))
        }
    }
}