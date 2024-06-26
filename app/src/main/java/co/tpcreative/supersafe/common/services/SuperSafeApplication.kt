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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import co.tpcreative.supersafe.BuildConfig
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.api.ApiService
import co.tpcreative.supersafe.common.api.RetrofitBuilder
import co.tpcreative.supersafe.common.controller.PrefsController
import co.tpcreative.supersafe.common.encypt.SecurityUtil
import co.tpcreative.supersafe.common.extension.*
import co.tpcreative.supersafe.common.helper.EncryptDecryptFilesHelper
import co.tpcreative.supersafe.common.helper.EncryptDecryptPinHelper
import co.tpcreative.supersafe.common.helper.ThemeHelper
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EnumPinAction
import co.tpcreative.supersafe.model.EnumThemeModel
import co.tpcreative.supersafe.model.EnumTypeServices
import co.tpcreative.supersafe.model.MigrationModel
import co.tpcreative.supersafe.ui.enterpin.EnterPinAct
import com.bumptech.glide.request.target.ImageViewTarget
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.*
import java.util.*

class SuperSafeApplication : MultiDexApplication(), Application.ActivityLifecycleCallbacks, LifecycleObserver {
    private lateinit var superSafe: String
    private lateinit var superSafeOldPath: String
    private lateinit var superSafePrivate: String
    private lateinit var superSafeBreakInAlerts: String
    private lateinit var superSafeLog: String
    private lateinit var superSafeShare: String
    private lateinit var superSafePicture: String
    private var key: String? = null
    private var fakeKey: String? = null
    private lateinit var options: GoogleSignInOptions.Builder
    private lateinit var requiredScopesString: MutableList<String>
    private var isLive = false
    private var activity: Activity? = null
    private var mMapMigrationItem: MutableMap<String, MigrationModel> = HashMap<String, MigrationModel>()
    private val classes = Stack<Activity>()

    override fun onCreate() {
        super.onCreate()
        initData()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this);
    }

    fun initData(){
        mInstance = this
        isLive = true
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        ImageViewTarget.setTagId(R.id.fab_glide_tag)
        serverApiCor = RetrofitBuilder.getService(typeService = EnumTypeServices.SYSTEM)
        serverDriveApiCor = RetrofitBuilder.getService(getString(R.string.url_google), typeService = EnumTypeServices.GOOGLE_DRIVE)
        serverMicCor = RetrofitBuilder.getService(getString(R.string.url_graph_microsoft), typeService = EnumTypeServices.EMAIL_OUTLOOK)
        PrefsController.Builder()
                .setContext(applicationContext)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(packageName)
                .setUseDefaultSharedPreference(true)
                .build()
        Utils.putScreenStatus(EnumPinAction.NONE.ordinal)
        Utils.putSeekTo(0)
        Utils.putLastWindowIndex(0)
        /*Migration*/
        if (isLiveMigration()) {
            superSafe = getExternalFilesDir(null)?.absolutePath + "/.SuperSafe_DoNot_Delete/"
            superSafeOldPath = "".getExternalStorageDirectory() + "/.SuperSafe_DoNot_Delete/"
        } else {
            superSafe = "".getExternalStorageDirectory() + "/.SuperSafe_DoNot_Delete/"
            superSafeOldPath = "".getExternalStorageDirectory() + "/.SuperSafe_DoNot_Delete/"
        }

        if (isLiveMigration()){
            key = superSafe + SecurityUtil.new_encrypt_key
            fakeKey = superSafe +SecurityUtil.new_encrypt_fake_Key
        }else{
            key = superSafe + SecurityUtil.old_encrypt_key
            fakeKey = superSafe +SecurityUtil.old_encrypt_fake_Key
        }

        superSafePrivate = superSafe + "private/"
        superSafeLog = superSafe + "log/"
        superSafeBreakInAlerts = superSafe + "break_in_alerts/"
        superSafeShare = superSafe + "share/"
        superSafePicture = "".getExternalStorageDirectory(Environment.DIRECTORY_DOWNLOADS) + "/SuperSafeExport/"
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
        ThemeHelper.applyTheme(EnumThemeModel.byPosition(Utils.getPositionThemeMode()))
        Utils.Log(TAG, getDeviceId())
    }

    fun getGoogleSignInOptions(account: Account?): GoogleSignInOptions? {
        if (account != null) {
            options.setAccountName(account.name)
        }
        return options.build()
    }

    fun getRequiredScopesString(): MutableList<String> {
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
            Utils.Log(TAG, "onActivityCreated")
        }
        if (activity is EnterPinAct){
            classes.add(activity)
        }
    }

    override fun onActivityStarted(p0: Activity) {
        Utils.Log(TAG, "onActivityStarted $isRunningBackground")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun onAppBackgrounded() {
        Utils.Log(TAG, "App in background")
        isRunningBackground = true
        Utils.putScreenStatus(EnumPinAction.SCREEN_LOCK.ordinal)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun onAppForegrounded() {
        isRunningBackground  = false
        Utils.Log(TAG, "App in foreground")
    }

    override fun onActivityResumed(p0: Activity) {
        Utils.Log(TAG, "onActivityResumed")
    }

    override fun onActivityPaused(p0: Activity) {
        Utils.Log(TAG, "onActivityPaused")
    }

    override fun onActivityStopped(p0: Activity) {
        Utils.Log(TAG, "onActivityStopped")
    }

    fun getActivity(): Activity? {
        return activity
    }

    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
    fun getSuperSafe(): String {
        return superSafe
    }

    fun getSuperSafeOldPath(): String {
        return superSafeOldPath
    }

    fun getSuperSafePrivate(): String {
        return superSafePrivate
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

    fun initFolder() {
        if (superSafe.isDirectoryExists() and superSafePrivate.isDirectoryExists() and  superSafeLog.isDirectoryExists() and superSafeBreakInAlerts.isDirectoryExists() and superSafeOldPath.isDirectoryExists() and superSafePicture.isDirectoryExists()) {
            Utils.Log(TAG, "SuperSafe is existing")
        } else {
            if (isLiveMigration()){
                superSafe.createDirectory()
            }else{
                superSafeOldPath.createDirectory()
            }
            superSafePrivate.createDirectory()
            superSafeLog.createDirectory()
            superSafeBreakInAlerts.createDirectory()
            superSafePicture.createDirectory()
            Utils.Log(TAG, "SuperSafe was created")
        }
    }

    fun deleteFolder() {
        Utils.Log(TAG, "Request delete folder")
        superSafe.deleteDirectory()
    }

    fun writeKey(value: String?) {
        if (!isPermissionWrite()) {
            Utils.Log(TAG, "Please grant access permission")
            return
        }
        if (isLiveMigration()){
            val oldPath = getSuperSafe() + SecurityUtil.old_encrypt_key
            if (oldPath.isFileExist()){
                if (EncryptDecryptFilesHelper.getInstance()?.createFile(key, value)==true){
                    oldPath.deleteFile()
                }
            }else{
                EncryptDecryptFilesHelper.getInstance()?.createFile(key, value)
            }
        }else{
            EncryptDecryptPinHelper.getInstance()?.createFile(key, value)
        }
        Utils.Log(TAG, "Created key :$value")
    }

    fun readKey(): String? {
        if (!isPermissionRead()) {
            Utils.Log(TAG, "Please grant access permission")
            return ""
        }
        if (isLiveMigration()){
            /*Checking old version*/
            val oldPath = getSuperSafe() + SecurityUtil.old_encrypt_key
            if (oldPath.isFileExist()){
                return EncryptDecryptPinHelper.getInstance()?.readTextFile(oldPath)
            }
            return EncryptDecryptFilesHelper.getInstance()?.readTextFile(key)
        }else{
            return EncryptDecryptPinHelper.getInstance()?.readTextFile(key)
        }
    }

    fun writeFakeKey(value: String?) {
        if (!isPermissionWrite()) {
            Utils.Log(TAG, "Please grant access permission")
            return
        }
        if (isLiveMigration()){
            val oldPath = getSuperSafe() + SecurityUtil.old_encrypt_fake_Key
            if (oldPath.isFileExist()){
                Utils.Log(TAG, "Deleted file")
                if (EncryptDecryptFilesHelper.getInstance()?.createFile(fakeKey, value) == true){
                    oldPath.deleteFile()
                }
            }else{
                EncryptDecryptFilesHelper.getInstance()?.createFile(fakeKey, value)
            }
        }else{
            EncryptDecryptPinHelper.getInstance()?.createFile(fakeKey, value)
        }
        Utils.Log(TAG, "Created key :$value")
    }

    fun readFakeKey() : String? {
        if (!isPermissionRead()) {
            Utils.Log(TAG, "Please grant access permission")
            return ""
        }
        if (isLiveMigration()){
            /*Checking old version*/
            val oldPath = getSuperSafe() + SecurityUtil.old_encrypt_fake_Key
            if (oldPath.isFileExist()){
                return EncryptDecryptPinHelper.getInstance()?.readTextFile(oldPath)
            }
            return EncryptDecryptFilesHelper.getInstance()?.readTextFile(fakeKey)
        }else{
            return EncryptDecryptPinHelper.getInstance()?.readTextFile(fakeKey)
        }
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

    fun getUrl(): String? {
        url = if (!BuildConfig.DEBUG || isLive) {
            SecurityUtil.url_live
        } else {
            SecurityUtil.url_developer
        }
        return url
    }

    fun getDeviceId(): String {
        val id: String? = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        return id ?: "${System.currentTimeMillis()}"
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

    fun getPackageId(): String {
        return BuildConfig.APPLICATION_ID
    }

    companion object {
        private val TAG = SuperSafeApplication::class.java.simpleName
        private var url: String? = null
        var serverApiCor : ApiService? = null
        var serverDriveApiCor : ApiService? = null
        var serverMicCor : ApiService? = null
        var isRunningBackground : Boolean? = false
        @Volatile
        private var mInstance: SuperSafeApplication? = null
        fun getInstance(): SuperSafeApplication {
            return mInstance as SuperSafeApplication
        }
    }

    val migrationFrom4To5: Migration = object : Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE 'items' ADD COLUMN  'isUpdate' INTEGER NOT NULL DEFAULT 0")
        }
    }

    val migrationFrom5To6: Migration = object : Migration(5, 6) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE 'items' ADD COLUMN  'isRequestChecking' INTEGER NOT NULL DEFAULT 0")
        }
    }

    val migrationFrom6To7: Migration = object : Migration(6, 7) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE 'maincategories' ADD COLUMN  'created_date' TEXT DEFAULT '0'")
            database.execSQL("ALTER TABLE 'maincategories' ADD COLUMN  'updated_date' TEXT DEFAULT '0'")
            database.execSQL("ALTER TABLE 'maincategories' ADD COLUMN  'date_time' TEXT  DEFAULT '0'")
        }
    }

    suspend fun onPreparingMigration() = withContext(Dispatchers.IO) {
        mMapMigrationItem.clear()
        File(getSuperSafeOldPath()).walkTopDown().forEach {
            val mResult = it.absolutePath.replace(superSafeOldPath, superSafe)
            if (it.isFile) {
                val mUniqueId = UUID.randomUUID().toString()
                mMapMigrationItem[mUniqueId] = MigrationModel(mUniqueId, it, File(mResult), false)
                Utils.Log(TAG, "path of new file : $mResult")
            } else {
                mResult.createDirectory()
            }
        }
        Utils.Log(TAG,"Preparing migration....")
        with(mMapMigrationItem){
            forEach {
                val mValue = it.value
                moveTo(mValue.mInput, mValue.mOutput)
                responseMigration.invoke(mMapMigrationItem.size)
                Utils.Log(TAG, "loop of index ${mValue.Id}")
            }
        }
    }

    lateinit var responseMigration : (value: Int?) -> Unit

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
        File(getSuperSafeOldPath()).walkTopDown().forEach {
            if (it.isFile) {
                return true
            }
        }
        return false
    }

    fun isLiveMigration(): Boolean {
        if (!BuildConfig.DEBUG){
            return true
        }
        return true
    }

    fun checkingMigrationAfterVerifiedPin(value: String?, isReal: Boolean, isWrite: Boolean){
        if (isReal){
            if (checkingToWrite(isReal, isWrite)){
                writeKey(value)
            }
        }else{
            if (checkingToWrite(isReal, isWrite)){
                writeFakeKey(value)
            }
        }
    }

    private fun checkingToWrite(isReal: Boolean, isWrite: Boolean) : Boolean{
        if (isReal){
            val oldPath = getSuperSafe() + SecurityUtil.old_encrypt_key
            if (oldPath.isFileExist()){
                return true
            }
            return isWrite
        }
        else{
            val oldPath = getSuperSafe() + SecurityUtil.old_encrypt_fake_Key
            if (oldPath.isFileExist()){
                return true
            }
            return isWrite
        }
    }

    fun isDebugPremium(): Boolean {
        if (BuildConfig.DEBUG){
            return true
        }
        return false
    }
}