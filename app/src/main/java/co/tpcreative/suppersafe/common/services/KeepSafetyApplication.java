package co.tpcreative.suppersafe.common.services;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.support.v4.content.PermissionChecker;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.snatik.storage.EncryptConfiguration;
import com.snatik.storage.Storage;

import java.util.List;

import co.tpcreative.suppersafe.R;
import co.tpcreative.suppersafe.common.Navigator;
import co.tpcreative.suppersafe.common.controller.PrefsController;
import co.tpcreative.suppersafe.ui.askpermission.AskPermissionActivity;

public class KeepSafetyApplication extends MultiDexApplication implements MultiDexApplication.ActivityLifecycleCallbacks {

    private static final String TAG = KeepSafetyApplication.class.getSimpleName();

    private static KeepSafetyApplication mInstance;
    private String keepSafety;
    private String key;
    private Storage storage;
    private EncryptConfiguration configuration;
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

        /*Storage Config*/
        String IVX = "abcdefghijklmnop"; // 16 lenght - not secret
        String SECRET_KEY = "secret@123"; // 16 lenght - secret
        byte[] SALT = "0000111100001111".getBytes(); // random 16 bytes array
        configuration = new EncryptConfiguration.Builder()
                .setEncryptContent(IVX, SECRET_KEY, SALT)
                .build();
        storage = new Storage(getApplicationContext());
        keepSafety = storage.getExternalStorageDirectory() + "/.KeepSafety_DoNot_Delete/";
        key = ".encrypt_key";
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


    public String getKeepSafety() {
        return keepSafety;
    }


    public void initFolder() {
        if (storage.isDirectoryExists(keepSafety)) {
            Log.d(TAG, "KeepSafety is existing");
        } else {
            storage.createDirectory(keepSafety);
            Log.d(TAG, "KeepSafety was created");
        }
    }

    public void writeKey(String value) {
        if (!isPermissionWrite()) {
            Log.d(TAG, "Please grant access permission");
            return;
        }
        storage.setEncryptConfiguration(configuration);
        storage.createFile(getKeepSafety() + key, value);
        Log.d(TAG, "Created key :" + value);
    }

    public String readKey() {
        if (!isPermissionRead()) {
            Log.d(TAG, "Please grant access permission");
            return "";
        }
        storage.setEncryptConfiguration(configuration);
        boolean isFile = storage.isFileExist(getKeepSafety() + key);
        if (isFile) {
            String value = storage.readTextFile(getKeepSafety() + key);
            Log.d(TAG, "Key value is : " + value);
            return value;
        }
        return "";
    }

    public boolean isPermissionRead() {
        int permission = PermissionChecker.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permission == PermissionChecker.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    public boolean isPermissionWrite() {
        int permission = PermissionChecker.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission == PermissionChecker.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    public boolean isGrantAccess(){
       if (isPermissionWrite() && isPermissionRead()){
           return true;
       }
       return false;
    }

}

