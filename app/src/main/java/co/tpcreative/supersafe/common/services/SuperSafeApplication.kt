package co.tpcreative.supersafe.common.services
import android.Manifest
import android.accounts.Account
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.core.content.PermissionChecker
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import co.tpcreative.supersafe.BuildConfig
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.api.RootAPI
import co.tpcreative.supersafe.common.controllerimport.PrefsController
import co.tpcreative.supersafe.common.controllerimport.ServiceManager
import co.tpcreative.supersafe.common.hiddencamera.config.CameraImageFormat
import co.tpcreative.supersafe.common.network.Dependencies
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EnumPinAction
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.User
import com.bumptech.glide.request.target.ImageViewTarget
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.snatik.storage.EncryptConfiguration
import com.snatik.storage.Storage
import com.snatik.storage.security.SecurityUtil
import java.io.File
import java.util.*

class SuperSafeApplication : MultiDexApplication(), Dependencies.DependenciesListener<RootAPI>, Application.ActivityLifecycleCallbacks {
    private lateinit var supersafe: String
    private lateinit var supersafePrivate: String
    private lateinit var supersafeBackup: String
    private lateinit var supersafeBreakInAlerts: String
    private lateinit var supersafeLog: String
    private lateinit var supersafeShare: String
    private lateinit var supersafePicture: String
    private lateinit var supersafeDataBaseFolder: String
    private var key: String? = null
    private var fake_key: String? = null
    private var userSecret: String? = null
    private lateinit var storage : Storage
    private var configurationFile: EncryptConfiguration? = null
    private var configurationPin: EncryptConfiguration? = null
    private var Orientation = 0
    private var authorization: String? = null
    private lateinit var options: GoogleSignInOptions.Builder
    private lateinit var requiredScopes: MutableSet<Scope>
    private lateinit var requiredScopesString: MutableList<String>
    private var isLive = false
    private var secretKey: String? = null
    private var activity: Activity? = null
    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this, object : OnInitializationCompleteListener {
            override fun onInitializationComplete(initializationStatus: InitializationStatus?) {}
        })
        mInstance = this
        isLive = true
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        ImageViewTarget.setTagId(R.id.fab_glide_tag)
        /*Init own service api*/dependencies = Dependencies.getInstance(this, getUrl()!!)
        dependencies?.dependenciesListener(this)
        dependencies?.init()
        serverAPI = Dependencies.Companion.serverAPI as RootAPI
        /*Init Drive api*/serverDriveApi = RetrofitHelper().getCityService(getString(R.string.url_google))
        /*Init GraphMicrosoft*/serviceGraphMicrosoft = RetrofitHelper().getCityService(getString(R.string.url_graph_microsoft))
        ServiceManager.getInstance()?.setContext(this)
        PrefsController.Builder()
                .setContext(getApplicationContext())
                ?.setMode(ContextWrapper.MODE_PRIVATE)
                ?.setPrefsName(getPackageName())
                ?.setUseDefaultSharedPreference(true)
                ?.build()
        PrefsController.putInt(getString(R.string.key_screen_status), EnumPinAction.NONE.ordinal)
        PrefsController.putLong(getString(R.string.key_seek_to), 0)
        PrefsController.putInt(getString(R.string.key_lastWindowIndex), 0)
        /*Config file*/configurationFile = EncryptConfiguration.Builder()
                .setChuckSize(1024 * 2)
                .setEncryptContent(SecurityUtil.IVX, getSecretKey(), SecurityUtil.SALT)
                .build()
        configurationPin = EncryptConfiguration.Builder()
                .setChuckSize(1024 * 2)
                .setEncryptContent(SecurityUtil.IVX, SecurityUtil.SECRET_KEY, SecurityUtil.SALT)
                .build()
        storage = Storage(getApplicationContext())
        supersafe = storage?.getExternalStorageDirectory() + "/.SuperSafe_DoNot_Delete/"
        key = ".encrypt_key"
        fake_key = ".encrypt_fake_key"
        userSecret = ".userSecret"
        supersafePrivate = supersafe + "private/"
        supersafeBackup = supersafe + "backup/"
        supersafeLog = supersafe + "log/"
        supersafeBreakInAlerts = supersafe + "break_in_alerts/"
        supersafeShare = supersafe + "share/"
        supersafeDataBaseFolder = "/data/data/" + getInstance().getPackageName() + "/databases/"
        supersafePicture = storage.getExternalStorageDirectory(Environment.DIRECTORY_PICTURES) + "/SuperSafeExport/"
        registerActivityLifecycleCallbacks(this)
        Utils.Log(TAG, supersafe)
        options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestScopes(Scope(DriveScopes.DRIVE_FILE))
                .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
        requiredScopes = HashSet(2)
        requiredScopes.add(Scope(DriveScopes.DRIVE_FILE))
        requiredScopes.add(Scope(DriveScopes.DRIVE_APPDATA))
        requiredScopesString = ArrayList()
        requiredScopesString.add(DriveScopes.DRIVE_APPDATA)
        requiredScopesString.add(DriveScopes.DRIVE_FILE)
        Utils.onCheckNewVersion()
        /*SP-100*/
    }

    fun getSecretKey(): String? {
        val user: User? = Utils.getUserInfo()
        if (user != null) {
            if (user._id != null) {
                Utils.Log(TAG, "Get secret key " + user._id)
                secretKey = user._id
                return secretKey
            }
            Utils.Log(TAG, "secret id is null")
        } else {
            Utils.Log(TAG, "Get secret key null")
        }
        return SecurityUtil.SECRET_KEY
    }

    fun getGoogleSignInOptions(account: Account?): GoogleSignInOptions? {
        if (options != null) {
            if (account != null) {
                options.setAccountName(account.name)
            }
            return options.build()
        }
        return options.build()
    }

    fun getConfigurationFile(): EncryptConfiguration? {
        configurationFile = EncryptConfiguration.Builder()
                .setChuckSize(1024 * 2)
                .setEncryptContent(SecurityUtil.IVX, getSecretKey(), SecurityUtil.SALT)
                .build()
        return configurationFile
    }

    fun getRequiredScopesString(): MutableList<String>? {
        return requiredScopesString
    }

    fun getRequiredScopes(): MutableSet<Scope>? {
        return requiredScopes
    }

    protected override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    fun setConnectivityListener(listener: SuperSafeReceiver.ConnectivityReceiverListener?) {
        SuperSafeReceiver.connectivityReceiverListener = listener
    }

    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
        if (this.activity == null) {
            this.activity = activity
        }
    }

    fun getActivity(): Activity? {
        return activity
    }

    override fun onActivityResumed(activity: Activity) {
        ++resumed
    }

    override fun onActivityPaused(activity: Activity) {
        ++paused
        Utils.Log(TAG, "application is in foreground: " + (resumed > paused))
    }

    override fun onActivityStarted(activity: Activity) {
        ++started
        Utils.Log(TAG, "onActivityStarted")
    }

    override fun onActivityStopped(activity: Activity) {
        ++stopped
        Utils.Log(TAG, "application is visible: " + (started > stopped))
    }

    fun isApplicationVisible(): Boolean {
        return started > stopped
    }

    fun isApplicationInForeground(): Boolean {
        return resumed > paused
    }

    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
    fun getSuperSafe(): String? {
        return supersafe
    }

    fun getSupersafePrivate(): String? {
        return supersafePrivate
    }

    fun getSupersafeBackup(): String? {
        return supersafeBackup
    }

    fun getSupersafeLog(): String? {
        return supersafeLog
    }

    fun getFileLogs(): String? {
        return "$supersafe/log.txt"
    }

    fun getSupersafeBreakInAlerts(): String? {
        return supersafeBreakInAlerts
    }

    fun getSupersafeShare(): String? {
        return supersafeShare
    }

    fun getSupersafePicture(): String? {
        return supersafePicture
    }

    fun getSupersafeDataBaseFolder(): String? {
        return supersafeDataBaseFolder
    }

    fun initFolder() {
        if (storage?.isDirectoryExists(supersafe) and storage?.isDirectoryExists(supersafePrivate) and storage?.isDirectoryExists(supersafeBackup) and storage?.isDirectoryExists(supersafeLog) and storage?.isDirectoryExists(supersafeBreakInAlerts)) {
            Utils.Log(TAG, "SuperSafe is existing")
        } else {
            storage?.createDirectory(supersafe)
            storage?.createDirectory(supersafePrivate)
            storage?.createDirectory(supersafeBackup)
            storage?.createDirectory(supersafeLog)
            storage?.createDirectory(supersafeBreakInAlerts)
            Utils.Log(TAG, "SuperSafe was created")
        }
    }

    fun deleteFolder() {
        storage.deleteDirectory(supersafe)
    }

    fun writeKey(value: String?) {
        if (!isPermissionWrite()) {
            Utils.Log(TAG, "Please grant access permission")
            return
        }
        storage?.setEncryptConfiguration(configurationPin)
        storage?.createFile(getSuperSafe() + key, value)
        Utils.Log(TAG, "Created key :$value")
    }

    fun readKey(): String? {
        try {
            if (!isPermissionRead()) {
                Utils.Log(TAG, "Please grant access permission")
                return ""
            }
            storage.setEncryptConfiguration(configurationPin)
            val isFile = storage.isFileExist(getSuperSafe() + key)
            if (isFile) {
                val value = storage.readTextFile(getSuperSafe() + key)
                Utils.Log(TAG, "Key value is : $value")
                return value
            }
        } catch (e: Exception) {
            e.message
            deleteFolder()
        }
        return ""
    }

    fun writeUserSecret(user: User?) {
        if (!isPermissionWrite()) {
            Utils.Log(TAG, "Please grant access permission")
            return
        }
        storage.setEncryptConfiguration(configurationPin)
        storage.createFile(getSuperSafe() + userSecret, Gson().toJson(user))
    }

    fun readUseSecret(): User? {
        try {
            if (!isPermissionRead()) {
                Utils.Log(TAG, "Please grant access permission")
                return null
            }
            storage.setEncryptConfiguration(configurationPin)
            val isFile = storage.isFileExist(getSuperSafe() + userSecret)
            if (isFile) {
                val value = storage.readTextFile(getSuperSafe() + userSecret)
                if (value != null) {
                    Utils.Log(TAG, value)
                    val mUser: User? = Gson().fromJson(value, User::class.java)
                    if (mUser != null) {
                        return mUser
                    }
                }
                return null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun writeFakeKey(value: String?) {
        if (!isPermissionWrite()) {
            Utils.Log(TAG, "Please grant access permission")
            return
        }
        storage.setEncryptConfiguration(configurationPin)
        storage.createFile(getSuperSafe() + fake_key, value)
        Utils.Log(TAG, "Created key :$value")
    }

    fun readFakeKey(): String? {
        if (!isPermissionRead()) {
            Utils.Log(TAG, "Please grant access permission")
            return ""
        }
        storage.setEncryptConfiguration(configurationPin)
        val isFile = storage.isFileExist(getSuperSafe() + fake_key)
        if (isFile) {
            val value = storage.readTextFile(getSuperSafe() + fake_key)
            Utils.Log(TAG, "Key value is : $value")
            return value
        }
        return ""
    }

    fun onDeleteKey() {
        if (!isPermissionRead()) {
            Utils.Log(TAG, "Please grant access permission")
            return
        }
        val isFile = storage.isFileExist(getSuperSafe() + key)
        if (isFile) {
            storage.deleteFile(getSuperSafe() + key)
        }
    }

    fun isPermissionRead(): Boolean {
        val permission: Int = PermissionChecker.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
        return if (permission == PermissionChecker.PERMISSION_GRANTED) {
            true
        } else false
    }

    fun isPermissionWrite(): Boolean {
        val permission: Int = PermissionChecker.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return if (permission == PermissionChecker.PERMISSION_GRANTED) {
            true
        } else false
    }

    fun isGrantAccess(): Boolean {
        return if (isPermissionWrite() && isPermissionRead()) {
            true
        } else false
    }

    fun getOrientation(): Int {
        return Orientation
    }

    fun setOrientation(mOrientation: Int) {
        Orientation = mOrientation
    }

    fun getStorage(): Storage? {
        return storage
    }

    fun getUrl(): String? {
        url = if (!BuildConfig.DEBUG || isLive) {
            SecurityUtil.url_live
        } else {
            SecurityUtil.url_developer
        }
        return url
    }

    fun getPathDatabase(): String? {
        return getDatabasePath(getString(R.string.key_database)).getAbsolutePath()
    }

    /*Retrofit and RXJava*/

    override fun onObject(): Class<RootAPI> {
        return RootAPI::class.java
    }

    override fun onAuthorToken(): String {
        try {
            var user: User? = Utils.getUserInfo()
            if (user != null) {
                authorization = ""
                user.author?.session_token?.let {
                    authorization = it
                }
                Utils.onWriteLog(authorization, EnumStatus.REQUEST_ACCESS_TOKEN)
            } else {
                user = readUseSecret()
                authorization = ""
                user?.author?.session_token?.let {
                    authorization = it
                }
            }
            return authorization!!
        } catch (e: Exception) {
        }
        return SecurityUtil.DEFAULT_TOKEN
    }

    override fun onCustomHeader(): HashMap<String, String> {
        val hashMap = HashMap<String, String>()
        hashMap["Content-Type"] = "application/json"
        if (authorization != null) {
            hashMap.set("Authorization",onAuthorToken())
        }
        return hashMap
    }

    fun getDeviceId(): String? {
        return Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID)
    }

    fun getManufacturer(): String? {
        return Build.MANUFACTURER
    }

    fun getModel(): String? {
        return Build.MODEL
    }

    fun getVersion(): Int {
        return Build.VERSION.SDK_INT
    }

    fun getVersionRelease(): String? {
        return Build.VERSION.RELEASE
    }

    fun getPackageId(): String? {
        return BuildConfig.APPLICATION_ID
    }

    fun getAppVersionRelease(): String? {
        return BuildConfig.VERSION_NAME
    }

    fun getPackagePath(context: Context?): File? {
        return File(context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                ".temporary.jpg")
    }

    fun getPackageFolderPath(context: Context?): File? {
        return File(context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.getAbsolutePath())
    }

    fun getDefaultStorageFile(mImageFormat: Int): File? {
        return File(getInstance().getSupersafeBreakInAlerts()
                + File.separator
                + "IMG_" + System.currentTimeMillis() //IMG_214515184113123.png
                + if (mImageFormat == CameraImageFormat.Companion.FORMAT_JPEG) ".jpeg" else ".png")
    }

    fun getKeyHomePressed(): MutableMap<String?, String?>? {
        try {
            val value: String? = PrefsController.getString(getString(R.string.key_home_pressed), null)
            if (value != null) {
                var map: MutableMap<String?, String?> = HashMap()
                map = Gson().fromJson(value, map.javaClass)
                return map
            }
            return HashMap()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return HashMap()
    }

    fun writeKeyHomePressed(value: String?) {
        try {
            val map = getKeyHomePressed()
            map?.set(value, value)
            PrefsController.putString(getString(R.string.key_home_pressed), Gson().toJson(map))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object{
        private val TAG = SuperSafeApplication::class.java.simpleName
        private var mInstance: SuperSafeApplication? = null
        private var resumed = 0
        private var paused = 0
        private var started = 0
        private var stopped = 0
        private var url: String? = null
        protected var dependencies: Dependencies<*>? = null
        var serverAPI: RootAPI? = null
        var serverDriveApi: RootAPI? = null
        var serviceGraphMicrosoft: RootAPI? = null
        @Volatile private var INSTANCE: SuperSafeApplication? = null
        fun  getInstance(): SuperSafeApplication {
            return INSTANCE?: synchronized(this){
                SuperSafeApplication().also {
                    INSTANCE = it
                }
            }
        }
    }

    val MIGRATION_4_5: Migration = object : Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE 'items' ADD COLUMN  'isUpdate' INTEGER NOT NULL DEFAULT 0")
        }
    }
}