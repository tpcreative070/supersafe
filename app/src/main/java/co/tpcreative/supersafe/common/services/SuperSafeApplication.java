package co.tpcreative.supersafe.common.services;

import android.Manifest;
import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.support.v4.content.PermissionChecker;
import android.util.Log;

import com.bumptech.glide.request.target.ViewTarget;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.gson.Gson;
import com.snatik.storage.EncryptConfiguration;
import com.snatik.storage.Storage;

import org.solovyev.android.checkout.Billing;

import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.hiddencamera.config.CameraImageFormat;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumPinAction;
import io.fabric.sdk.android.Fabric;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import co.tpcreative.supersafe.BuildConfig;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.api.RootAPI;
import co.tpcreative.supersafe.common.controller.ServiceManager;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.network.Dependencies;
import co.tpcreative.supersafe.model.User;

public class SuperSafeApplication extends MultiDexApplication implements Dependencies.DependenciesListener, MultiDexApplication.ActivityLifecycleCallbacks {

    private static final String TAG = SuperSafeApplication.class.getSimpleName();
    private static SuperSafeApplication mInstance;
    private String supersafe;
    private String supersafePrivate;
    private String supersafeBackup;
    private String supersafeBreakInAlerts;
    private String supersafeLog;
    private String supersafeShare;
    private String supersafePicture;
    private String supersafeDataBaseFolder;
    private String key;
    private String fake_key;
    private String userSecret;
    private Storage storage;
    private EncryptConfiguration configuration;
    private EncryptConfiguration configurationFile;
    private static int resumed;
    private static int paused;
    private static int started;
    private static int stopped;
    private static String url;
    private int Orientation = 0;

    protected static Dependencies dependencies;
    public static RootAPI serverAPI;
    public static RootAPI serverDriveApi;
    private String authorization = null;

    private GoogleSignInOptions.Builder options;
    private Set<Scope> requiredScopes;
    private List<String> requiredScopesString;
    private boolean isLive = false;


    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        isLive = true;




        Fabric.with(this, new Crashlytics());

        ViewTarget.setTagId(R.id.fab_glide_tag);


        /*Init own service api*/

        dependencies = Dependencies.getsInstance(getApplicationContext(), getUrl());
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
        PrefsController.putInt(getString(R.string.key_screen_status), EnumPinAction.NONE.ordinal());

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
        if (BuildConfig.DEBUG){
            supersafe = storage.getExternalStorageDirectory() + "/SuperSafe_DoNot_Delete/";
        }
        else{
            supersafe = storage.getExternalStorageDirectory() + "/.SuperSafe_DoNot_Delete/";
        }

        key = ".encrypt_key";
        fake_key = ".encrypt_fake_key";
        userSecret = ".userSecret";
        supersafePrivate = supersafe + "private/";
        supersafeBackup = supersafe + "backup/";
        supersafeLog = supersafe + "log/";
        supersafeBreakInAlerts = supersafe + "break_in_alerts/";
        supersafeShare = supersafe + "share/";
        supersafeDataBaseFolder = "/data/data/"+SuperSafeApplication.getInstance().getPackageName()+"/databases/";


        supersafePicture = storage.getExternalStorageDirectory(Environment.DIRECTORY_PICTURES) + "/SuperSafeExport/";
        registerActivityLifecycleCallbacks(this);
        Log.d(TAG, supersafe);


        options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestScopes(Drive.SCOPE_FILE)
                .requestScopes(Drive.SCOPE_APPFOLDER);

        requiredScopes = new HashSet<>(2);
        requiredScopes.add(Drive.SCOPE_FILE);
        requiredScopes.add(Drive.SCOPE_APPFOLDER);

        requiredScopesString = new ArrayList<>();
        requiredScopesString.add(DriveScopes.DRIVE_APPDATA);
        requiredScopesString.add(DriveScopes.DRIVE_FILE);
    }



    public GoogleSignInOptions getGoogleSignInOptions(final Account account) {
        if (options != null) {
            if (account != null) {
                options.setAccount(account);
            }
            return options.build();
        }
        return options.build();
    }

    public EncryptConfiguration getConfigurationFile() {
        return configurationFile;
    }


    public List<String> getRequiredScopesString() {
        return requiredScopesString;
    }

    public Set<Scope> getRequiredScopes() {
        return requiredScopes;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static synchronized SuperSafeApplication getInstance() {
        return mInstance;
    }

    /*In app purchase*/

    private final Billing mBilling = new Billing(this, new Billing.DefaultConfiguration() {
        /*In app purchase*/
        String key_purchase = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAk+6HXAFTNx3LbODafbpgsLqkdyMqMEvIYt55lqTjLIh0PkoAX7oSAD0fY7BXW0Czuys13hNNdyzmDjQe76xmUWTNfXM1vp0JQtStl7tRqNaFuaRje59HKRLpRTW1MGmgKw/19/18EalWTjbGOW7C2qZ5eGIOvGfQvvlraAso9lCTeEwze3bmGTc7B8MOfDqZHETdavSVgVjGJx/K10pzAauZFGvZ+ryZtU0u+9ZSyGx1CgHysmtfcZFKqZLbtOxUQHpBMeJf2M1LReqbR1kvJiAeLYqdOMWzmmNcsEoG6g/e+F9ZgjZjoQzqhWsrTE2IQZAaiwU4EezdqqruNXx6uwIDAQAB";
        @Override
        public String getPublicKey() {
            return key_purchase;
        }
    });

    @Nonnull
    public Billing getBilling() {
        return mBilling;
    }


    public void setConnectivityListener(SuperSafeReceiver.ConnectivityReceiverListener listener) {
        SuperSafeReceiver.connectivityReceiverListener = listener;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        String currentActivity = activity.getClass().getSimpleName();
        String hashValue = getKeyHomePressed().get(currentActivity);

        if (hashValue != null && hashValue.equals(currentActivity)) {
            int value = PrefsController.getInt(getString(R.string.key_screen_status), EnumPinAction.NONE.ordinal());
            EnumPinAction action = EnumPinAction.values()[value];
            switch (action) {
                case SCREEN_PRESS_HOME: {
                    Utils.Log(TAG, "Start screen off.................:" + activity.getClass().getSimpleName());
                    Navigator.onMoveToVerifyPin(activity, EnumPinAction.NONE);
                    PrefsController.putInt(getString(R.string.key_screen_status), EnumPinAction.SCREEN_LOCK.ordinal());
                    break;
                }
                default: {
                    Utils.Log(TAG, "Nothing to do");
                }
            }
        } else {
            Utils.Log(TAG, "Exception activity " + currentActivity);
        }
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
        Utils.Log(TAG, "onActivityStarted");
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


    public String getSuperSafe() {
        return supersafe;
    }

    public String getSupersafePrivate() {
        return supersafePrivate;
    }

    public String getSupersafeBackup() {
        return supersafeBackup;
    }

    public String getSupersafeLog() {
        return supersafeLog;
    }

    public String getSupersafeBreakInAlerts() {
        return supersafeBreakInAlerts;
    }

    public String getSupersafeShare() {
        return supersafeShare;
    }

    public String getSupersafePicture() {
        return supersafePicture;
    }

    public String getSupersafeDataBaseFolder() {
        return supersafeDataBaseFolder;
    }

    public void initFolder() {
        if (storage.isDirectoryExists(supersafe) & storage.isDirectoryExists(supersafePrivate) & storage.isDirectoryExists(supersafeBackup) & storage.isDirectoryExists(supersafeLog) & storage.isDirectoryExists(supersafeBreakInAlerts)) {
            Log.d(TAG, "SuperSafe is existing");
        } else {
            storage.createDirectory(supersafe);
            storage.createDirectory(supersafePrivate);
            storage.createDirectory(supersafeBackup);
            storage.createDirectory(supersafeLog);
            storage.createDirectory(supersafeBreakInAlerts);
            Log.d(TAG, "SuperSafe was created");
        }
    }

    public void deleteFolder() {
        storage.deleteDirectory(supersafe);
    }

    public void writeKey(String value) {
        if (!isPermissionWrite()) {
            Log.d(TAG, "Please grant access permission");
            return;
        }
        storage.setEncryptConfiguration(configuration);
        storage.createFile(getSuperSafe() + key, value);
        Log.d(TAG, "Created key :" + value);
    }

    public String readKey() {
        if (!isPermissionRead()) {
            Log.d(TAG, "Please grant access permission");
            return "";
        }
        storage.setEncryptConfiguration(configuration);
        boolean isFile = storage.isFileExist(getSuperSafe() + key);
        if (isFile) {
            String value = storage.readTextFile(getSuperSafe() + key);
            Log.d(TAG, "Key value is : " + value);
            return value;
        }
        return "";
    }

    public void writeUserSecret(final User user) {
        if (!isPermissionWrite()) {
            Log.d(TAG, "Please grant access permission");
            return;
        }
        storage.setEncryptConfiguration(configuration);
        storage.createFile(getSuperSafe() + userSecret, new Gson().toJson(user));
    }

    public User readUseSecret() {
        if (!isPermissionRead()) {
            Log.d(TAG, "Please grant access permission");
            return null;
        }
        storage.setEncryptConfiguration(configuration);
        boolean isFile = storage.isFileExist(getSuperSafe() + userSecret);
        if (isFile) {
            String value = storage.readTextFile(getSuperSafe() + userSecret);
            if (value!=null){
                final User mUser = new Gson().fromJson(value,User.class);
                if (mUser!=null){
                    return mUser;
                }
            }
            return null;
        }
        return null;
    }


    public void writeFakeKey(String value) {
        if (!isPermissionWrite()) {
            Log.d(TAG, "Please grant access permission");
            return;
        }
        storage.setEncryptConfiguration(configuration);
        storage.createFile(getSuperSafe() + fake_key, value);
        Log.d(TAG, "Created key :" + value);
    }

    public String readFakeKey() {
        if (!isPermissionRead()) {
            Log.d(TAG, "Please grant access permission");
            return "";
        }
        storage.setEncryptConfiguration(configuration);
        boolean isFile = storage.isFileExist(getSuperSafe() + fake_key);
        if (isFile) {
            String value = storage.readTextFile(getSuperSafe() + fake_key);
            Log.d(TAG, "Key value is : " + value);
            return value;
        }
        return "";
    }

    public void onDeleteKey() {
        if (!isPermissionRead()) {
            Log.d(TAG, "Please grant access permission");
            return;
        }
        boolean isFile = storage.isFileExist(getSuperSafe() + key);
        if (isFile) {
            storage.deleteFile(getSuperSafe() + key);
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

    public boolean isGrantAccess() {
        if (isPermissionWrite() && isPermissionRead()) {
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

    public String getUrl() {
        if (!BuildConfig.DEBUG || isLive) {
            url = getString(R.string.url_live);
        } else {
            url = getString(R.string.url_developer);
        }
        return url;
    }


    public String getPathDatabase() {
        String currentDBPath = getDatabasePath(getString(R.string.key_database)).getAbsolutePath();
        return currentDBPath;
    }

    /*Retrofit and RXJava*/

    @Override
    public Class onObject() {
        return RootAPI.class;
    }

    @Override
    public String onAuthorToken() {
        try {
            String value = PrefsController.getString(getString(R.string.key_user), "");
            User user = new Gson().fromJson(value, User.class);
            if (user != null) {
                authorization = user.author.session_token;
            }
            else{
                user = readUseSecret();
                authorization = user.author.session_token;
            }
            return authorization;
        } catch (Exception e) {
        }
        return null;
    }

    @Override
    public HashMap<String, String> onCustomHeader() {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("Content-Type", "application/json");
        if (authorization != null) {
            hashMap.put("Authorization", authorization);
        }
        return hashMap;
    }

    @Override
    public boolean isXML() {
        return false;
    }

    public String getDeviceId() {
        return Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public String getManufacturer() {
        String manufacturer = Build.MANUFACTURER;
        return manufacturer;
    }

    public String getModel() {
        String model = Build.MODEL;
        return model;
    }

    public int getVersion() {
        int version = Build.VERSION.SDK_INT;
        return version;
    }

    public String getVersionRelease() {
        String versionRelease = Build.VERSION.RELEASE;
        return versionRelease;
    }

    public File getPackagePath(Context context) {
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                ".temporary.jpg");
        return file;
    }

    public File getPackageFolderPath(Context context) {
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath());
        return file;
    }

    public File getDefaultStorageFile(int mImageFormat) {
        return new File(SuperSafeApplication.getInstance().getSupersafeBreakInAlerts()
                + File.separator
                + "IMG_" + System.currentTimeMillis()   //IMG_214515184113123.png
                + (mImageFormat == CameraImageFormat.FORMAT_JPEG ? ".jpeg" : ".png"));
    }

    public Map<String, String> getKeyHomePressed() {
        try {
            String value = PrefsController.getString(getString(R.string.key_home_pressed), null);
            if (value != null) {
                Map<String, String> map = new HashMap<String, String>();
                map = new Gson().fromJson(value, map.getClass());
                return map;
            }
            return new HashMap<>();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }

    public void writeKeyHomePressed(String value) {
        try {
            Map<String, String> map = getKeyHomePressed();
            map.put(value, value);
            PrefsController.putString(getString(R.string.key_home_pressed), new Gson().toJson(map));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
