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
import co.tpcreative.supersafe.common.api.ApiService
import co.tpcreative.supersafe.common.api.RetrofitBuilder
import co.tpcreative.supersafe.common.controller.PrefsController
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.helper.ThemeHelper
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.*
import com.bumptech.glide.request.target.ImageViewTarget
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.snatik.storage.EncryptConfiguration
import com.snatik.storage.Storage
import com.snatik.storage.security.SecurityUtil
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.util.*

class SuperSafeApplication : MultiDexApplication(), Application.ActivityLifecycleCallbacks {
    private lateinit var superSafe: String
    private lateinit var superSafeOldPath: String
    private lateinit var superSafePrivate: String
    private lateinit var superSafeBackup: String
    private lateinit var superSafeBreakInAlerts: String
    private lateinit var superSafeLog: String
    private lateinit var superSafeShare: String
    private lateinit var superSafePicture: String
    private lateinit var superSafeDataBaseFolder: String
    private var key: String? = null
    private var fake_key: String? = null
    private var userSecret: String? = null
    private lateinit var storage: Storage
    private var configurationFile: EncryptConfiguration? = null
    private var configurationPin: EncryptConfiguration? = null
    private lateinit var options: GoogleSignInOptions.Builder
    private lateinit var requiredScopesString: MutableList<String>
    private var isLive = false
    private var secretKey: String? = null
    private var activity: Activity? = null
    private var mMapMigrationItem: MutableMap<String, MigrationModel> = HashMap<String, MigrationModel>()
    private var compositeDisposable: CompositeDisposable? = null
    override fun onCreate() {
        super.onCreate()
        mInstance = this
        isLive = true
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        ImageViewTarget.setTagId(R.id.fab_glide_tag)
        serverDriveApi = RetrofitHelper().getService(getString(R.string.url_google))
        serviceGraphMicrosoft = RetrofitHelper().getService(getString(R.string.url_graph_microsoft))
        serverAPI = RetrofitHelper().getTPCreativeService(getUrl())
        serverApiCor = RetrofitBuilder.getService()
        serverDriveApiCor = RetrofitBuilder.getService(getString(R.string.url_google))
        serverMicCor = RetrofitBuilder.getService(getString(R.string.url_graph_microsoft))
        ServiceManager.getInstance()?.setContext(this)
        PrefsController.Builder()
                .setContext(applicationContext)
                ?.setMode(ContextWrapper.MODE_PRIVATE)
                ?.setPrefsName(packageName)
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
        storage = Storage(this)

        /*Migration*/
        if (isLiveMigration()) {
            superSafe = getExternalFilesDir(null)?.absolutePath + "/.SuperSafe_DoNot_Delete/"
            superSafeOldPath = storage.externalStorageDirectory + "/.SuperSafe_DoNot_Delete/"
        } else {
            superSafe = storage.externalStorageDirectory + "/.SuperSafe_DoNot_Delete/"
            superSafeOldPath = storage.externalStorageDirectory + "/.SuperSafe_DoNot_Delete/"
        }

        key = ".encrypt_key"
        fake_key = ".encrypt_fake_key"
        userSecret = ".userSecret"
        superSafePrivate = superSafe + "private/"
        superSafeBackup = superSafe + "backup/"
        superSafeLog = superSafe + "log/"
        superSafeBreakInAlerts = superSafe + "break_in_alerts/"
        superSafeShare = superSafe + "share/"
        superSafeDataBaseFolder = "/data/data/" + getInstance().packageName + "/databases/"
        superSafePicture = storage.getExternalStorageDirectory(Environment.DIRECTORY_PICTURES) + "/SuperSafeExport/"
        registerActivityLifecycleCallbacks(this)
        Utils.Log(TAG, superSafe)
        options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestScopes(Scope(DriveScopes.DRIVE_FILE))
                .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
        requiredScopesString = ArrayList()
        requiredScopesString.add(DriveScopes.DRIVE_APPDATA)
        requiredScopesString.add(DriveScopes.DRIVE_FILE)
        Utils.onCheckNewVersion()
        ThemeHelper.applyTheme(EnumThemeModel.byPosition(Utils.getPositionTheme()))
    }

    private fun getSecretKey(): String? {
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
        if (account != null) {
            options.setAccountName(account.name)
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

    override fun attachBaseContext(base: Context?) {
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

    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
    fun getSuperSafe(): String? {
        return superSafe
    }

    fun getSuperSafeOldPath(): String? {
        return superSafeOldPath
    }

    fun getSuperSafePrivate(): String {
        return superSafePrivate
    }

    fun getSuperSafeBackup(): String {
        return superSafeBackup
    }

    fun getSuperSafeLog(): String {
        return superSafeLog
    }

    fun getFileLogs(): String {
        return "$superSafe/log.txt"
    }

    fun getSuperSafeBreakInAlerts(): String {
        return superSafeBreakInAlerts
    }

    fun getSuperSafeShare(): String {
        return superSafeShare
    }

    fun getSuperSafePicture(): String {
        return superSafePicture
    }

    fun getSuperSafeDataBaseFolder(): String {
        return superSafeDataBaseFolder
    }

    fun initFolder() {
        if (storage.isDirectoryExists(superSafe) and storage.isDirectoryExists(superSafePrivate) and storage.isDirectoryExists(superSafeBackup) and storage.isDirectoryExists(superSafeLog) and storage.isDirectoryExists(superSafeBreakInAlerts) and storage.isDirectoryExists(superSafeOldPath)) {
            Utils.Log(TAG, "SuperSafe is existing")
        } else {
            storage.createDirectory(superSafe)
            storage.createDirectory(superSafeOldPath)
            storage.createDirectory(superSafePrivate)
            storage.createDirectory(superSafeBackup)
            storage.createDirectory(superSafeLog)
            storage.createDirectory(superSafeBreakInAlerts)
            Utils.Log(TAG, "SuperSafe was created")
        }
    }

    fun deleteFolder() {
        Utils.Log(TAG, "Request delete folder")
        storage.deleteDirectory(superSafe)
    }

    fun writeKey(value: String?) {
        if (!isPermissionWrite()) {
            Utils.Log(TAG, "Please grant access permission")
            return
        }
        storage.setEncryptConfiguration(configurationPin)
        storage.createFile(getSuperSafe() + key, value)
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

    private fun isPermissionRead(): Boolean {
        val permission: Int = PermissionChecker.checkSelfPermission(applicationContext, Manifest.permission.READ_EXTERNAL_STORAGE)
        return permission == PermissionChecker.PERMISSION_GRANTED
    }

    private fun isPermissionWrite(): Boolean {
        val permission: Int = PermissionChecker.checkSelfPermission(applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return permission == PermissionChecker.PERMISSION_GRANTED
    }

    fun isGrantAccess(): Boolean {
        return isPermissionWrite() && isPermissionRead()
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

    fun getDeviceId(): String? {
        return Settings.Secure.getString(applicationContext.contentResolver, Settings.Secure.ANDROID_ID)
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

    companion object {
        private val TAG = SuperSafeApplication::class.java.simpleName
        private var resumed = 0
        private var paused = 0
        private var started = 0
        private var stopped = 0
        private var url: String? = null
        var serverAPI: ApiService? = null
        var serverApiCor : ApiService? = null
        var serverDriveApiCor : ApiService? = null
        var serverMicCor : ApiService? = null
        var serverDriveApi: ApiService? = null
        var serviceGraphMicrosoft: ApiService? = null
        @Volatile
        private var mInstance: SuperSafeApplication? = null
        fun getInstance(): SuperSafeApplication {
            return mInstance as SuperSafeApplication
        }
    }

    val MIGRATION_4_5: Migration = object : Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE 'items' ADD COLUMN  'isUpdate' INTEGER NOT NULL DEFAULT 0")
        }
    }

    val MIGRATION_5_6: Migration = object : Migration(5, 6) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE 'items' ADD COLUMN  'isRequestChecking' INTEGER NOT NULL DEFAULT 0")
        }
    }

    suspend fun onPreparingMigration() = withContext(Dispatchers.IO) {
        mMapMigrationItem.clear()
        File(getSuperSafeOldPath()!!).walkTopDown().forEach {
            val mResult = it.absolutePath.replace(superSafeOldPath, superSafe)
            if (it.isFile) {
                val mUniqueId = UUID.randomUUID().toString()
                mMapMigrationItem[mUniqueId] = MigrationModel(mUniqueId, it, File(mResult), false)
                Utils.Log(TAG, "path of new file : $mResult")
            } else {
                storage.createDirectory(mResult)
            }
        }
        if (mMapMigrationItem.isNotEmpty()) {
            for (index in mMapMigrationItem){
                val mValue = index.value
                moveTo(mValue.mInput,mValue.mOutput)
                Utils.Log(TAG,"loop of index ${mValue.Id}")
            }
        }
    }

    private suspend fun moveTo(source: File, dest: File) = withContext(Dispatchers.IO){
        try {
            if (source.isDirectory) {
                source.mkdirs()
                Utils.Log(TAG, "Call here")
            }
            val fis = FileInputStream(source)
            val bufferLength = 1024
            val buffer = ByteArray(bufferLength)
            val fos = FileOutputStream(dest)
            val bos = BufferedOutputStream(fos, bufferLength)
            var read = 0
            read = fis.read(buffer, 0, read)
            while (read != -1) {
                bos.write(buffer, 0, read)
                read = fis.read(buffer) // if read value is -1, it escapes loop.
            }
            fis.close()
            bos.flush()
            bos.close()
            Utils.Log(TAG, "Finish...")
            if (!source.delete()) {
                Utils.Log(TAG, "failed to delete ${source.name}")
            }
        } catch (exception: IOException) {
            exception.printStackTrace()
            Utils.Log(TAG, "Could not move file...")
        }
    }

    fun isRequestMigration(): Boolean {
        File(getSuperSafeOldPath()!!).walkTopDown().forEach {
            if (it.isFile) {
                return true
            }
        }
        return false
    }

    fun isLiveMigration(): Boolean {
        return true
    }

    fun isDebugPremium(): Boolean {
        return true
    }
}