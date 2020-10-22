package co.tpcreative.supersafe.common
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import java.lang.ref.WeakReference
import java.util.*

class SensorOrientationChangeNotifier private constructor() {
    val TAG = javaClass.simpleName
    private val mListeners: ArrayList<WeakReference<Listener>>? = ArrayList(3)
    private var mOrientation = 0
    private val mSensorEventListener: SensorEventListener?
    private val mSensorManager: SensorManager?

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

    fun getOrientation(): Int {
        return mOrientation
    }

    fun addListener(listener: co.tpcreative.supersafe.common.SensorOrientationChangeNotifier.Listener?) {
        if (get(listener) == null) // prevent duplications
            mListeners?.add(WeakReference(listener))
        if (mListeners?.size == 1) {
            onResume() // this is the first client
        }
    }

    fun remove(listener: Listener?) {
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
                if (wr.get() == null) deadLiksArr.add(wr) else wr.get()?.onOrientationChange(mOrientation)
            }
        }

        // remove dead references
        for (wr in deadLiksArr) {
            mListeners?.remove(wr)
        }
    }

    fun isPortrait(): Boolean {
        return mOrientation == 0 || mOrientation == 180
    }

    fun isLandscape(): Boolean {
        return !isPortrait()
    }

    interface Listener {
        open fun onOrientationChange(orientation: Int)
    }

    private inner class NotifierSensorEventListener : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            val x: Float = event!!.values!!.get(0)
            val y: Float = event!!.values!!.get(1)
            var newOrientation = mOrientation
            if (x < 5 && x > -5 && y > 5) newOrientation = 0 else if (x < -5 && y < 5 && y > -5) newOrientation = 90 else if (x < 5 && x > -5 && y < -5) newOrientation = 180 else if (x > 5 && y < 5 && y > -5) newOrientation = 270

            //Log.e(TAG,"mOrientation="+mOrientation+"   ["+event.values[0]+","+event.values[1]+","+event.values[2]+"]");
            if (mOrientation != newOrientation) {
                mOrientation = newOrientation
                notifyListeners()
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    companion object {
        private var mInstance: SensorOrientationChangeNotifier? = null
        fun getInstance(): SensorOrientationChangeNotifier? {
            if (mInstance == null) mInstance = SensorOrientationChangeNotifier()
            return mInstance
        }
    }

    init {
        mSensorEventListener = NotifierSensorEventListener()
        val applicationContext: Context = SuperSafeApplication.getInstance().getApplicationContext()
        mSensorManager = applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
}