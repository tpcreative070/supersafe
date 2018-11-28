package co.tpcreative.supersafe.common;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import co.tpcreative.supersafe.common.services.SuperSafeApplication;

public class SensorFaceUpDownChangeNotifier {
    private static SensorFaceUpDownChangeNotifier mInstance;
    public final String TAG = getClass().getSimpleName();
    private ArrayList<WeakReference<Listener>> mListeners = new ArrayList<WeakReference<Listener>>(3);
    private SensorEventListener mSensorEventListener;
    private SensorManager mSensorManager;
    private boolean isFaceDown;
    private boolean isFaceDownTemporary;

    private SensorFaceUpDownChangeNotifier() {
        mSensorEventListener = new NotifierSensorEventListener();
        Context applicationContext = SuperSafeApplication.getInstance().getApplicationContext();
        mSensorManager = (SensorManager) applicationContext.getSystemService(Context.SENSOR_SERVICE);

    }

    public static SensorFaceUpDownChangeNotifier getInstance() {
        if (mInstance == null)
            mInstance = new SensorFaceUpDownChangeNotifier();

        return mInstance;
    }

    /**
     * Call on activity reset()
     */
    private void onResume() {
        mSensorManager.registerListener(mSensorEventListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }

    /**
     * Call on activity onPause()
     */
    private void onPause() {
        mSensorManager.unregisterListener(mSensorEventListener);
    }

    public boolean isFaceDown() {
        return isFaceDown;
    }

    public void addListener(SensorFaceUpDownChangeNotifier.Listener listener) {
        if (get(listener) == null) // prevent duplications
            mListeners.add(new WeakReference<Listener>(listener));

        if (mListeners.size() == 1) {
            onResume(); // this is the first client
        }
    }

    public void remove(SensorFaceUpDownChangeNotifier.Listener listener) {
        WeakReference<Listener> listenerWR = get(listener);
        remove(listenerWR);
    }

    private void remove(WeakReference<Listener> listenerWR) {
        if (listenerWR != null)
            mListeners.remove(listenerWR);

        if (mListeners.size() == 0) {
            onPause();
        }

    }

    private WeakReference<Listener> get(SensorFaceUpDownChangeNotifier.Listener listener) {
        for (WeakReference<Listener> existingListener : mListeners)
            if (existingListener.get() == listener)
                return existingListener;
        return null;
    }

    private void notifyListeners() {
        ArrayList<WeakReference<Listener>> deadLiksArr = new ArrayList<WeakReference<Listener>>();
        for (WeakReference<Listener> wr : mListeners) {
            if (wr.get() == null){
                deadLiksArr.add(wr);
            }
            else {
                wr.get().onOrientationChange(isFaceDown);
            }
        }
        // remove dead references
        for (WeakReference<Listener> wr : deadLiksArr) {
            mListeners.remove(wr);
        }
    }

    public interface Listener {
        void onOrientationChange(boolean isFaceDown);
    }

    private class NotifierSensorEventListener implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent event) {
            final float factor = 0.95F;
                boolean nowDown = event.values[2] < -SensorManager.GRAVITY_EARTH * factor;
                if (nowDown != isFaceDown) {
                    if (nowDown) {
                        Log.i(TAG, "DOWN");
                    } else {
                        Log.i(TAG, "UP");
                    }
                    isFaceDown = nowDown;
                }

            if (isFaceDown != isFaceDownTemporary) {
                notifyListeners();
                isFaceDownTemporary = isFaceDown;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }
}