package co.tpcreative.supersafe.common
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import java.lang.ref.WeakReference
import java.util.*

class SensorFaceUpDownChangeNotifier private constructor() {
    val TAG = javaClass.simpleName
    private val mListeners: ArrayList<WeakReference<Listener>>? = ArrayList(3)
    private val mSensorEventListener: SensorEventListener?
    private val mSensorManager: SensorManager?
    private var isFaceDown = false
    private var isFaceDownTemporary = false

    /**
     * Call on activity reset()
     */
    private fun onResume() {
        mSensorManager?.registerListener(mSensorEventListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL)
    }

    /**
     * Call on activity onPause()
     */
    private fun onPause() {
        mSensorManager?.unregisterListener(mSensorEventListener)
    }

    fun isFaceDown(): Boolean {
        return isFaceDown
    }

    fun addListener(listener: co.tpcreative.supersafe.common.SensorFaceUpDownChangeNotifier.Listener?) {
        if (get(listener) == null) // prevent duplications
            mListeners?.add(WeakReference(listener))
        if (mListeners?.size == 1) {
            onResume() // this is the first client
        }
    }

    fun remove(listener: Listener) {
        val listenerWR = get(listener)
        remove(listenerWR)
    }

    private fun remove(listenerWR: WeakReference<Listener>?) {
        if (listenerWR != null) mListeners?.remove(listenerWR)
        if (mListeners?.size == 0) {
            onPause()
        }
    }

    private operator fun get(listener: Listener?): WeakReference<Listener>? {
        if (mListeners != null) {
            for (existingListener in mListeners) if (existingListener.get() === listener) return existingListener
        }
        return null
    }

    private fun notifyListeners() {
        val deadLiksArr = ArrayList<WeakReference<Listener>>()
        if (mListeners != null) {
            for (wr in mListeners) {
                if (wr.get() == null) {
                    deadLiksArr.add(wr)
                } else {
                    wr.get()?.onOrientationChange(isFaceDown)
                }
            }
        }
        // remove dead references
        for (wr in deadLiksArr) {
            mListeners?.remove(wr)
        }
    }

    interface Listener {
        open fun onOrientationChange(isFaceDown: Boolean)
    }

    private inner class NotifierSensorEventListener : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            val factor = 0.95f
            val nowDown: Boolean = event?.values?.get(2)!! < -SensorManager.GRAVITY_EARTH * factor
            if (nowDown != isFaceDown) {
                if (nowDown) {
                    Log.i(TAG, "DOWN")
                } else {
                    Log.i(TAG, "UP")
                }
                isFaceDown = nowDown
            }
            if (isFaceDown != isFaceDownTemporary) {
                notifyListeners()
                isFaceDownTemporary = isFaceDown
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    companion object {
        private var mInstance: SensorFaceUpDownChangeNotifier? = null
        fun getInstance(): SensorFaceUpDownChangeNotifier? {
            if (mInstance == null) mInstance = SensorFaceUpDownChangeNotifier()
            return mInstance
        }
    }

    init {
        mSensorEventListener = NotifierSensorEventListener()
        val applicationContext: Context = SuperSafeApplication.getInstance().getApplicationContext()
        mSensorManager = applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
}