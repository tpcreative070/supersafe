package co.tpcreative.suppersafe.common.services;
import android.Manifest;
import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.support.v4.content.PermissionChecker;
import android.text.TextUtils;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.gson.Gson;
import com.snatik.storage.EncryptConfiguration;
import com.snatik.storage.Storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import co.tpcreative.suppersafe.BuildConfig;
import co.tpcreative.suppersafe.R;
import co.tpcreative.suppersafe.common.api.RootAPI;
import co.tpcreative.suppersafe.common.controller.ServiceManager;
import co.tpcreative.suppersafe.common.controller.PrefsController;
import co.tpcreative.suppersafe.common.network.Dependencies;
import co.tpcreative.suppersafe.model.User;
import co.tpcreative.suppersafe.ui.splashscreen.SplashScreenActivity;

public class SupperSafeApplication extends MultiDexApplication implements Dependencies.DependenciesListener, MultiDexApplication.ActivityLifecycleCallbacks {

    private static final String TAG = SupperSafeApplication.class.getSimpleName();
    private static SupperSafeApplication mInstance;
    private String suppersafe;
    private String key;
    private Storage storage;
    private EncryptConfiguration configuration;
    private EncryptConfiguration configurationFile;
    private static int resumed;
    private static int paused;
    private static int started;
    private static int stopped;
    private static String url ;
    /*Volley*/
    private RequestQueue mRequestQueue;
    private int Orientation = 0;

    protected static Dependencies dependencies;
    public static RootAPI serverAPI ;
    public static RootAPI serverDriveApi;
    private String authorization = null ;
    private Activity activity;

    private GoogleSignInOptions.Builder options;
    private Set<Scope> requiredScopes;
    private List<String> requiredScopesString;
    private String localCategories_Id;
    private String globalCategories_Id;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        /*Init own service api*/

        dependencies = Dependencies.getsInstance(getApplicationContext(),getUrl());
        dependencies.dependenciesListener(this);
        dependencies.init();
        serverAPI = (RootAPI) Dependencies.serverAPI;

        /*Init Drive api*/

        serverDriveApi = new RetrofitHelper().getCityService();


        ServiceManager.getInstance().setContext(this);

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

        /*Config file*/
        String IVX_ = "abcdefghijklmnop"; // 16 lenght - not secret
        String SECRET_KEY_ = "secret@123"; // 16 lenght - secret
        byte[] SALT_ = "0000111100001111".getBytes(); // random 16 bytes array
        configurationFile = new EncryptConfiguration.Builder()
                .setEncryptContent(IVX_, SECRET_KEY_, SALT_)
                .build();

        storage = new Storage(getApplicationContext());
        suppersafe = storage.getExternalStorageDirectory() + "/SupperSafe_DoNot_Delete/";
        key = ".encrypt_key";
        registerActivityLifecycleCallbacks(this);
        Log.d(TAG,suppersafe);

         options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.server_client_id))
                        .requestScopes(Drive.SCOPE_FILE)
                        .requestProfile()
                        .requestScopes(Drive.SCOPE_APPFOLDER);


         requiredScopes = new HashSet<>(2);
         requiredScopes.add(Drive.SCOPE_FILE);
         requiredScopes.add(Drive.SCOPE_APPFOLDER);

         requiredScopesString = new ArrayList<>();
         requiredScopesString.add(DriveScopes.DRIVE_APPDATA);
         requiredScopesString.add(DriveScopes.DRIVE_FILE);

    }

    public GoogleSignInOptions getGoogleSignInOptions(final Account account){
        if (options!=null){
            if (account!=null){
                options.setAccount(account);
            }
            return options.build();
        }
        return options.build();
    }

    public EncryptConfiguration getConfigurationFile() {
        return configurationFile;
    }


    public List<String> getRequiredScopesString(){
        return requiredScopesString;
    }

    public Set<Scope> getRequiredScopes(){
        return requiredScopes;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static synchronized SupperSafeApplication getInstance() {
        return mInstance;
    }

    public void setConnectivityListener(SupperSafeReceiver.ConnectivityReceiverListener listener) {
        SupperSafeReceiver.connectivityReceiverListener = listener;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
        if (activity instanceof SplashScreenActivity){
            this.activity = activity;
        }
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

    public String getSupperSafe() {
        return suppersafe;
    }

    public void initFolder() {
        if (storage.isDirectoryExists(suppersafe)) {
            Log.d(TAG, "KeepSafety is existing");
        } else {
            storage.createDirectory(suppersafe);
            Log.d(TAG, "KeepSafety was created");
        }
    }

    public void writeKey(String value) {
        if (!isPermissionWrite()) {
            Log.d(TAG, "Please grant access permission");
            return;
        }
        storage.setEncryptConfiguration(configuration);
        storage.createFile(getSupperSafe() + key, value);
        Log.d(TAG, "Created key :" + value);
    }

    public String readKey() {
        if (!isPermissionRead()) {
            Log.d(TAG, "Please grant access permission");
            return "";
        }
        storage.setEncryptConfiguration(configuration);
        boolean isFile = storage.isFileExist(getSupperSafe() + key);
        if (isFile) {
            String value = storage.readTextFile(getSupperSafe() + key);
            Log.d(TAG, "Key value is : " + value);
            return value;
        }
        return "";
    }

    public void onDeleteKey(){
        if (!isPermissionRead()) {
            Log.d(TAG, "Please grant access permission");
            return ;
        }
        boolean isFile = storage.isFileExist(getSupperSafe() + key);
        if (isFile){
            storage.deleteFile(getSupperSafe()+key);
        }
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

    public int getOrientation() {
        return Orientation;
    }

    public void setOrientation(int mOrientation) {
        this.Orientation = mOrientation;
    }

    public Storage getStorage() {
        return storage;
    }

    public String getUrl(){
        if (BuildConfig.DEBUG){
            url = getString(R.string.url_developer);
        }
        else{
            url = getString(R.string.url_live);
        }
        return url;
    }


    /*Retrofit and RXJava*/

    @Override
    public Class onObject() {
        return RootAPI.class;
    }

    @Override
    public String onAuthorToken() {
        try{
            String value = PrefsController.getString(getString(R.string.key_user),"");
            final User user = new Gson().fromJson(value,User.class);
            if (user!=null){
                authorization = user.author.session_token;
            }
            return authorization;
        }
        catch (Exception e){
        }
        return null;
    }

    @Override
    public HashMap<String, String> onCustomHeader() {
        HashMap<String,String>hashMap = new HashMap<>();
        hashMap.put("Content-Type","application/json");
        if (authorization!=null){
            hashMap.put("Authorization",authorization);
        }
        return hashMap;
    }

    @Override
    public boolean isXML() {
        return false;
    }

    public String getDeviceId(){
        return Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public String getManufacturer(){
        String manufacturer = Build.MANUFACTURER;
        return manufacturer;
    }

    public String getModel(){
        String model = Build.MODEL;
        return model;
    }

    public int getVersion(){
        int version = Build.VERSION.SDK_INT;
        return version;
    }

    public String getVersionRelease(){
        String versionRelease = Build.VERSION.RELEASE;
        return versionRelease;
    }

    public String getLocalCategoriesId() {
        return localCategories_Id;
    }

    public void setLocalCategoriesId(String localCategories_Id) {
        this.localCategories_Id = localCategories_Id;
    }

    public String getGlobalCategoriesId() {
        return globalCategories_Id;
    }

    public void setGlobalCategoriesId(String globalCategories_Id) {
        this.globalCategories_Id = globalCategories_Id;
    }





}
