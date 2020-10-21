package co.tpcreative.supersafe.common.controller
import android.content.Context
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.model.EnumStatus
import java.lang.ref.WeakReference
import java.util.*

class SingletonMultipleListener private constructor() {
    val TAG = javaClass.simpleName
    private val mListeners: ArrayList<WeakReference<Listener>>? = ArrayList(3)
    fun addListener(listener: Listener?) {
        if (listener?.let { get(it) } == null) // prevent duplications
            mListeners?.add(WeakReference(listener))
    }

    fun remove(listener: Listener) {
        val listenerWR = get(listener)
        remove(listenerWR)
    }

    private fun remove(listenerWR: WeakReference<Listener>?) {
        if (listenerWR != null) mListeners?.remove(listenerWR)
    }

    private operator fun get(listener: Listener): WeakReference<Listener>? {
        if (mListeners != null) {
            for (existingListener in mListeners) if (existingListener.get() === listener) return existingListener
        }
        return null
    }

    fun notifyListeners(status: EnumStatus) {
        val deadLiksArr = ArrayList<WeakReference<Listener>?>()
        if (mListeners != null) {
            for (wr in mListeners) {
                if (wr.get() == null) deadLiksArr.add(wr) else wr.get()?.onNotifier(status)
            }
        }
        // remove dead references
        for (wr in deadLiksArr) {
            mListeners?.remove(wr)
        }
    }

    interface Listener {
        open fun onNotifier(status: EnumStatus?)
    }

    companion object {
        private var mInstance: SingletonMultipleListener? = null
        fun getInstance(): SingletonMultipleListener? {
            if (mInstance == null) mInstance = SingletonMultipleListener()
            return mInstance
        }
    }

    init {
        val applicationContext: Context = SuperSafeApplication.getInstance().getApplicationContext()
    }
}