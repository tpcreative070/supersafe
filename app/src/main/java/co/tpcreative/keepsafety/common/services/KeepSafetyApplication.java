package co.tpcreative.keepsafety.common.services;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.snatik.storage.Storage;
import co.tpcreative.keepsafety.common.controller.PrefsController;

public class KeepSafetyApplication extends MultiDexApplication implements  MultiDexApplication.ActivityLifecycleCallbacks {

    private static final String TAG = KeepSafetyApplication.class.getSimpleName();

    private static KeepSafetyApplication mInstance;
    private String pathFolder;
    private Storage storage;
    private static int resumed;
    private static int paused;
    private static int started;
    private static int stopped;
    /*Volley*/
    private RequestQueue mRequestQueue;



    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        new PrefsController.Builder()
                .setContext(getApplicationContext())
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();

        storage = new Storage(getApplicationContext());
        storage.createDirectory(storage.getExternalStorageDirectory()+"/.KeepSafety_DoNot_Delete");
        registerActivityLifecycleCallbacks(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static synchronized KeepSafetyApplication getInstance() {
        return mInstance;
    }

    public void setConnectivityListener(KeepSafetyReceiver.ConnectivityReceiverListener listener) {
        KeepSafetyReceiver.connectivityReceiverListener = listener;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        ++resumed;
    }

    @Override
    public void onActivityPaused(Activity activity) {
        ++paused;
        Log.d(TAG, "application is in foreground: " + (resumed > paused));
    }


    @Override
    public void onActivityStarted(Activity activity) {
        ++started;
    }

    @Override
    public void onActivityStopped(Activity activity) {
        ++stopped;
        Log.d(TAG, "application is visible: " + (started > stopped));
    }

    public boolean isApplicationVisible() {
        return started > stopped;
    }

    public boolean isApplicationInForeground() {
        return resumed > paused;
    }


    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }



    public String getPathFolder(){
        return pathFolder;
    }



}

