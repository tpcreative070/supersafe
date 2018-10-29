package co.tpcreative.supersafe.common.controller;
import android.content.Context;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.model.EnumStatus;

public class SingletonMultipleListener {

    private static SingletonMultipleListener mInstance;
    public final String TAG = getClass().getSimpleName();
    private ArrayList<WeakReference<Listener>> mListeners = new ArrayList<>(3);

    private SingletonMultipleListener() {
        Context applicationContext = SuperSafeApplication.getInstance().getApplicationContext();
    }

    public static SingletonMultipleListener getInstance() {
        if (mInstance == null)
            mInstance = new SingletonMultipleListener();

        return mInstance;
    }


    public void addListener(SingletonMultipleListener.Listener listener) {
        if (get(listener) == null) // prevent duplications
            mListeners.add(new WeakReference<SingletonMultipleListener.Listener>(listener));
    }

    public void remove(SingletonMultipleListener.Listener listener) {
        WeakReference<SingletonMultipleListener.Listener> listenerWR = get(listener);
        remove(listenerWR);
    }

    private void remove(WeakReference<SingletonMultipleListener.Listener> listenerWR) {
        if (listenerWR != null)
            mListeners.remove(listenerWR);
    }

    private WeakReference<SingletonMultipleListener.Listener> get(SingletonMultipleListener.Listener listener) {
        for (WeakReference<SingletonMultipleListener.Listener> existingListener : mListeners)
            if (existingListener.get() == listener)
                return existingListener;
        return null;
    }

    public void notifyListeners(EnumStatus status) {
        ArrayList<WeakReference<SingletonMultipleListener.Listener>> deadLiksArr = new ArrayList<WeakReference<SingletonMultipleListener.Listener>>();
        for (WeakReference<SingletonMultipleListener.Listener> wr : mListeners) {
            if (wr.get() == null)
                deadLiksArr.add(wr);
            else
                wr.get().onNotifier(status);
        }
        // remove dead references
        for (WeakReference<SingletonMultipleListener.Listener> wr : deadLiksArr) {
            mListeners.remove(wr);
        }
    }

    public interface Listener {
        void onNotifier(EnumStatus status);
    }
}