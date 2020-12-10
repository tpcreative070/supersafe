package co.tpcreative.supersafe.common.util
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Point
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Patterns
import android.view.View
import android.view.animation.TranslateAnimation
import android.view.inputmethod.InputMethodManager
import android.webkit.MimeTypeMap
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.PermissionChecker
import co.tpcreative.supersafe.BuildConfig
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.controller.PrefsController
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.controller.SingletonManager
import co.tpcreative.supersafe.common.encypt.SecurityUtil
import co.tpcreative.supersafe.common.entities.InstanceGenerator
import co.tpcreative.supersafe.common.extension.*
import co.tpcreative.supersafe.common.helper.EncryptDecryptFilesHelper
import co.tpcreative.supersafe.common.helper.EncryptDecryptPinHelper
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.helper.ThemeHelper
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.model.*
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.util.Base64
import com.google.common.base.Charsets
import com.google.gson.Gson
import com.tapadoo.alerter.Alerter
import org.apache.commons.io.FilenameUtils
import org.greenrobot.eventbus.EventBus
import java.io.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by pc on 07/16/2017.
 */
object Utils {
    val GOOGLE_CONSOLE_KEY: String = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAk+6HXAFTNx3LbODafbpgsLqkdyMqMEvIYt55lqTjLIh0PkoAX7oSAD0fY7BXW0Czuys13hNNdyzmDjQe76xmUWTNfXM1vp0JQtStl7tRqNaFuaRje59HKRLpRTW1MGmgKw/19/18EalWTjbGOW7C2qZ5eGIOvGfQvvlraAso9lCTeEwze3bmGTc7B8MOfDqZHETdavSVgVjGJx/K10pzAauZFGvZ+ryZtU0u+9ZSyGx1CgHysmtfcZFKqZLbtOxUQHpBMeJf2M1LReqbR1kvJiAeLYqdOMWzmmNcsEoG6g/e+F9ZgjZjoQzqhWsrTE2IQZAaiwU4EezdqqruNXx6uwIDAQAB"
    // utility function
    const val FORMAT_TIME: String = "yyyy-MM-dd HH:mm:ss"
    const val FORMAT_TIME_FILE_NAME: String = "yyyyMMdd_HHmmss"
    const val FORMAT_SERVER_DATE_TIME = "MM/dd/yyyy hh:mm:ss a"
    /*Note
    * hh for 12h
    * HH for 24h
    * */

    const val COUNT_RATE = 9
    const val CODE_EXCEPTION = 1111
    const val MAX_LENGTH = 100
    val TAG = Utils::class.java.simpleName
    fun isValidEmail(target: CharSequence?): Boolean {
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target ?: "").matches()
    }

    fun isValid(target: CharSequence?): Boolean {
        return !TextUtils.isEmpty(target)
    }

    fun showDialog(activity: Activity, message: String) {
        val builder = MaterialDialog(activity)
        builder.title(R.string.confirm)
        builder.message(text = message)
        builder.positiveButton(R.string.ok)
        builder.show()
    }

    fun showDialog(activity: Activity, @StringRes message: Int, ls: ServiceManager.ServiceManagerSyncDataListener) {
        val builder = MaterialDialog(activity)
        builder.title(R.string.confirm)
        builder.message(message)
        builder.positiveButton(R.string.ok)
        builder.negativeButton(R.string.cancel)
        builder.negativeButton { ls.onCancel() }
        builder.positiveButton { ls.onCompleted() }
        builder.show()
    }

    private fun mCreateAndSaveFileOverride(fileName: String?, path_folder_name: String?, responseJson: String?, append: Boolean): Boolean {
        Log(TAG, "path $path_folder_name")
        val newLine = System.getProperty("line.separator")
        return try {
            val root = File("$path_folder_name/$fileName")
            val saved = root.getSize(SizeUnit.MB)
            if (saved >= 1) {
                root.absolutePath.deleteFile()
            }
            if (!root.exists()) {
                val parentFolder = File(path_folder_name ?: "")
                if (!parentFolder.exists()) {
                    parentFolder.mkdirs()
                }
                root.createNewFile()
            }
            val file = FileWriter(root, append)
            file.write("\r\n")
            file.write(responseJson)
            file.write("\r\n")
            file.flush()
            file.close()
            true
        } catch (e: IOException) {
            e.message?.let { Log(TAG, it) }
            false
        }
    }

    fun hideSoftKeyboard(context: Activity) {
        val view: View? = context.currentFocus
        if (view != null) {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    fun hideKeyboard(view: View?) {
        // Check if no view has focus:
        if (view != null) {
            val inputManager = SuperSafeApplication.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }

    fun getPackagePath(context: Context): File {
        return File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                ".temporary.jpg")
    }

    fun getMimeType(url: String?): String? {
        var type: String? = null
        val extension: String? = MimeTypeMap.getFileExtensionFromUrl(url)
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
        return type
    }

    fun getFileExtension(url: String?): String{
        return FilenameUtils.getExtension(url).toLowerCase(Locale.ROOT)
    }

    fun Log(TAG: String, message: String?) {
        if (BuildConfig.DEBUG) {
            if (message != null) {
                android.util.Log.d(TAG, message)
            }
        }
    }

    fun <T> Log(clazz: Class<T>, content: Any?) {
        if (content is String) {
            Log(clazz.simpleName, content)
        } else {
            Log(clazz.simpleName, Gson().toJson(content))
        }
    }

    fun getUUId(): String? {
        return try {
            UUID.randomUUID().toString()
        } catch (e: Exception) {
            "" + System.currentTimeMillis()
        }
    }

    fun getCurrentDateTime(): String? {
        val date = Date()
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        return dateFormat.format(date)
    }

    fun getCurrentDateTime(formatName: String?): String? {
        val date = Date()
        val dateFormat = SimpleDateFormat(formatName, Locale.getDefault())
        return dateFormat.format(date)
    }

    fun getCurrentDate(value: String?): String? {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        try {
            val mDate = sdf.parse(value ?: "")
            val dateFormat = SimpleDateFormat("EE dd MMM, yyyy", Locale.getDefault())
            return dateFormat.format(mDate ?: "")
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return ""
    }

    private fun getCurrentDateTimeFormat(): String? {
        val date = Date()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(date)
    }

    fun getHexCode(value: String): String {
        return String(Base64.encodeBase64(value.toUpperCase(Locale.ROOT).toByteArray(Charsets.UTF_8)));
    }

    // To animate view slide out from top to bottom
    fun slideToBottomHeader(view: View) {
        val animate = TranslateAnimation(0F, 0F, (-view.height).toFloat(), 0F)
        animate.duration = 500
        animate.fillAfter = true
        view.startAnimation(animate)
    }

    // To animate view slide out from bottom to top
    fun slideToTopHeader(view: View) {
        Log(TAG, " " + view.height)
        val animate = TranslateAnimation(0F, 0F, 0F, (-view.height).toFloat())
        animate.duration = 500
        animate.fillAfter = true
        view.startAnimation(animate)
    }

    // To animate view slide out from top to bottom
    fun slideToBottomFooter(view: View) {
        val animate = TranslateAnimation(0F, 0F, 0F, view.height.toFloat())
        animate.duration = 500
        animate.fillAfter = true
        view.startAnimation(animate)
    }

    // To animate view slide out from bottom to top
    fun slideToTopFooter(view: View) {
        Log(TAG, " " + view.height)
        val animate = TranslateAnimation(0F, 0F, view.width.toFloat(), 0F)
        animate.duration = 500
        animate.fillAfter = true
        view.startAnimation(animate)
    }

    fun stringToHex(content: String): String {
        return String(Base64.encodeBase64(content.toByteArray(Charsets.UTF_8)))
    }

    fun hexToString(hex: String): String? {
        try {
            val data = Base64.decodeBase64(hex.toByteArray())
            return String(data, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun mediaTypeSupport(): HashMap<String, MimeTypeFile> {
        val hashMap = HashMap<String, MimeTypeFile>()
        hashMap["mp4"] = MimeTypeFile(".mp4", EnumFormatType.VIDEO, "video/mp4")
        hashMap["3gp"] = MimeTypeFile(".3gp", EnumFormatType.VIDEO, "video/3gp")
        hashMap["wmv"] = MimeTypeFile(".wmv", EnumFormatType.VIDEO, "video/wmv")
        hashMap["mkv"] = MimeTypeFile(".mkv", EnumFormatType.VIDEO, "video/mkv")
        hashMap["m4a"] = MimeTypeFile(".m4a", EnumFormatType.AUDIO, "audio/m4a")
        hashMap["aac"] = MimeTypeFile(".aac", EnumFormatType.AUDIO, "audio/aac")
        hashMap["mp3"] = MimeTypeFile(".mp3", EnumFormatType.AUDIO, "audio/mp3")
        hashMap["wav"] = MimeTypeFile(".wav", EnumFormatType.AUDIO, "audio/wav")
        hashMap["jpg"] = MimeTypeFile(".jpg", EnumFormatType.IMAGE, "image/jpeg")
        hashMap["jpeg"] = MimeTypeFile(".jpeg", EnumFormatType.IMAGE, "image/jpeg")
        hashMap["png"] = MimeTypeFile(".png", EnumFormatType.IMAGE, "image/png")
        hashMap["gif"] = MimeTypeFile(".gif", EnumFormatType.IMAGE, "image/gif")
        return hashMap
    }

    fun mimeTypeSupport(): HashMap<String, MimeTypeFile> {
        val hashMap = HashMap<String, MimeTypeFile>()
        hashMap["video/mp4"] = MimeTypeFile(".mp4", EnumFormatType.VIDEO, "video/mp4")
        hashMap["video/3gp"] = MimeTypeFile(".3gp", EnumFormatType.VIDEO, "video/3gp")
        hashMap["video/wmv"] = MimeTypeFile(".wmv", EnumFormatType.VIDEO, "video/wmv")
        hashMap["video/mkv"] = MimeTypeFile(".mkv", EnumFormatType.VIDEO, "video/mkv")
        hashMap["audio/m4a"] = MimeTypeFile(".m4a", EnumFormatType.AUDIO, "audio/m4a")
        hashMap["audio/aac"] = MimeTypeFile(".aac", EnumFormatType.AUDIO, "audio/aac")
        hashMap["audio/mp3"] = MimeTypeFile(".mp3", EnumFormatType.AUDIO, "audio/mp3")
        hashMap["audio/mpeg"] = MimeTypeFile(".mp3", EnumFormatType.AUDIO, "audio/mpeg")
        hashMap["audio/wav"] = MimeTypeFile(".wav", EnumFormatType.AUDIO, "audio/wav")
        hashMap["image/jpeg"] = MimeTypeFile(".jpg", EnumFormatType.IMAGE, "image/jpeg")
        hashMap["image/png"] = MimeTypeFile(".png", EnumFormatType.IMAGE, "image/png")
        hashMap["image/gif"] = MimeTypeFile(".gif", EnumFormatType.IMAGE, "image/gif")
        hashMap["application/msword"] = MimeTypeFile(".doc", EnumFormatType.FILES, "application/msword")
        hashMap["application/vnd.openxmlformats-officedocument.wordprocessingml.document"] = MimeTypeFile(".docx", EnumFormatType.FILES, "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
        hashMap["application/vnd.openxmlformats-officedocument.wordprocessingml.template"] = MimeTypeFile(".dotx", EnumFormatType.FILES, "application/vnd.openxmlformats-officedocument.wordprocessingml.template")
        hashMap["application/vnd.ms-word.document.macroEnabled.12"] = MimeTypeFile(".dotm", EnumFormatType.FILES, "application/vnd.ms-word.document.macroEnabled.12")
        hashMap["application/vnd.ms-excel"] = MimeTypeFile(".xls", EnumFormatType.FILES, "application/vnd.ms-excel")
        hashMap["application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"] = MimeTypeFile(".xlsx", EnumFormatType.FILES, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        hashMap["application/vnd.openxmlformats-officedocument.spreadsheetml.template"] = MimeTypeFile(".xltx", EnumFormatType.FILES, "application/vnd.openxmlformats-officedocument.spreadsheetml.template")
        hashMap["application/vnd.ms-excel.sheet.macroEnabled.12"] = MimeTypeFile(".xlsm", EnumFormatType.FILES, "application/vnd.ms-excel.sheet.macroEnabled.12")
        hashMap["application/vnd.ms-excel.template.macroEnabled.12"] = MimeTypeFile(".xltm", EnumFormatType.FILES, "application/vnd.ms-excel.template.macroEnabled.12")
        hashMap["application/vnd.ms-excel.addin.macroEnabled.12"] = MimeTypeFile(".xlam", EnumFormatType.FILES, "application/vnd.ms-excel.addin.macroEnabled.12")
        hashMap["application/vnd.ms-excel.sheet.binary.macroEnabled.12"] = MimeTypeFile(".xlsb", EnumFormatType.FILES, "application/vnd.ms-excel.sheet.binary.macroEnabled.12")
        hashMap["application/vnd.ms-powerpoint"] = MimeTypeFile(".ppt", EnumFormatType.FILES, "application/vnd.ms-powerpoint")
        hashMap["application/vnd.openxmlformats-officedocument.presentationml.presentation"] = MimeTypeFile(".pptx", EnumFormatType.FILES, "application/vnd.openxmlformats-officedocument.presentationml.presentation")
        hashMap["application/vnd.openxmlformats-officedocument.presentationml.template"] = MimeTypeFile(".potx", EnumFormatType.FILES, "application/vnd.openxmlformats-officedocument.presentationml.template")
        hashMap["application/vnd.ms-powerpoint.addin.macroEnabled.12"] = MimeTypeFile(".ppsx", EnumFormatType.FILES, "application/vnd.ms-powerpoint.addin.macroEnabled.12")
        hashMap["application/vnd.ms-powerpoint.presentation.macroEnabled.12t"] = MimeTypeFile(".pptm", EnumFormatType.FILES, "application/vnd.ms-powerpoint.presentation.macroEnabled.12")
        hashMap["application/vnd.ms-powerpoint.template.macroEnabled.12"] = MimeTypeFile(".potm", EnumFormatType.FILES, "application/vnd.ms-powerpoint.template.macroEnabled.12")
        hashMap["application/vnd.ms-powerpoint.slideshow.macroEnabled.12"] = MimeTypeFile(".ppsm", EnumFormatType.FILES, "application/vnd.ms-powerpoint.slideshow.macroEnabled.12")
        hashMap["application/vnd.ms-access"] = MimeTypeFile(".mdb", EnumFormatType.FILES, "application/vnd.ms-access")
        return hashMap
    }

    fun onWriteLog(message: String?, status: EnumStatus) {
        if (!BuildConfig.DEBUG) {
            return
        }
        if (status == null) {
            mCreateAndSaveFileOverride("log.txt", SuperSafeApplication.getInstance().getSuperSafeLog(), "----Time----" + getCurrentDateTimeFormat() + " ----Content--- :" + message, true)
        } else {
            mCreateAndSaveFileOverride("log.txt", SuperSafeApplication.getInstance().getSuperSafeLog(), "----Time----" + getCurrentDateTimeFormat() + " ----Status---- :" + status.name + " ----Content--- :" + message, true)
        }
    }

    private fun appendLog(text: String?) {
        val logFile = File(SuperSafeApplication.getInstance().getFileLogs())
        if (!logFile.exists()) {
            try {
                logFile.createNewFile()
            } catch (e: IOException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            }
        }
        try {
            //BufferedWriter for performance, true to set append to file flag
            val buf = BufferedWriter(FileWriter(logFile, true))
            buf.append("""
    $text
    
    """.trimIndent())
            buf.newLine()
            buf.close()
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
    }

    fun onWriteLog(action: EnumStatus, status: EnumStatus, value: String?) {
        if (!BuildConfig.DEBUG) {
            return
        }
        onCheck()
        appendLog("Version " + BuildConfig.VERSION_NAME + " ; created date time :" + getCurrentDateTime(FORMAT_TIME).toString() + " ; Action :" + action.name.toString() + " ; Status: " + status.name.toString() + " ; message log: " + value)
    }

    private fun onCheck() {
        val file = File(SuperSafeApplication.getInstance().getFileLogs())
        if (file.exists()) {
            val mSize = file.getSize(SizeUnit.MB).toLong()
            if (mSize > 2) {
                file.absolutePath.deleteFile()
            }
        }
    }

    fun getMilliseconds(value: String?,format : String): Long {
        if (value.isNullOrEmpty()) {
            return System.currentTimeMillis()
        }
        try {
            val dateFormat = SimpleDateFormat(format, Locale.getDefault())
            val date = dateFormat.parse(value)
            return date?.time ?: System.currentTimeMillis()
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return System.currentTimeMillis()
    }

    fun shareMultiple(files: MutableList<File>, context: Activity) {
        val uris = ArrayList<Uri>()
        for (file in files) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val uri: Uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file)
                uris.add(uri)
            } else {
                uris.add(Uri.fromFile(file))
            }
        }
        val intent = Intent(Intent.ACTION_SEND_MULTIPLE)
        intent.type = "*/*"
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        intent.flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
        context.startActivityForResult(Intent.createChooser(intent, context.getString(R.string.share)), Navigator.SHARE)
    }

    private fun getScreenSize(activity: Activity): Point {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val size = Point()
            activity.display?.getRealSize(size)
            size
        }else{
            val size = Point()
            activity.windowManager?.defaultDisplay?.getSize(size)
            size
        }
    }

    fun getScreenWidth(activity: Activity): Int {
        return getScreenSize(activity).x
    }

    fun getScreenHeight(activity: Activity): Int {
        return getScreenSize(activity).y
    }

    fun getFontString(content: Int, value: String): String {
        val themeApp: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
        val mAccessColor = String.format("#%06x", themeApp?.getAccentColor()?.let { ContextCompat.getColor(SuperSafeApplication.getInstance(), it) }?.and(0xffffff))
        return SuperSafeApplication.getInstance().getString(content, "<font color='$mAccessColor'><b>$value</b></font>")
    }

    fun getFontString(content: Int, value: String?, fontSize: Int): String {
        val themeApp: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
        val mAccessColor = String.format("#%06x", themeApp?.getAccentColor()?.let { ContextCompat.getColor(SuperSafeApplication.getInstance(), it) }?.and(0xffffff))
        return SuperSafeApplication.getInstance().getString(content, "<font size='$fontSize' color='$mAccessColor'><b>$value</b></font>")
    }

    fun appInstalledOrNot(uri: String): Boolean {
        val pm: PackageManager = SuperSafeApplication.getInstance().packageManager
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES)
            return true
        } catch (e: PackageManager.NameNotFoundException) {
        }
        return false
    }

    fun onDeleteTemporaryFile() {
        try {
            val rootDataDir: File? = SuperSafeApplication.getInstance().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val list = rootDataDir?.listFiles()
            list.let {
                if (it != null) {
                    for (i in it.indices) {
                        it[i].absolutePath.deleteFile()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isLandscape(activity: AppCompatActivity): Boolean {
        val landscape: Boolean
        val displayMetrics = DisplayMetrics()
        /*Support for android 11*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            activity.display?.getRealMetrics(displayMetrics)
        }else{
            activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        }
        val width: Int = displayMetrics.widthPixels
        val height: Int = displayMetrics.heightPixels
        landscape = width >= height
        return landscape
    }

    /**
     * @return Number of bytes available on External storage
     */
    fun getAvailableSpaceInBytes(): Long {
        var availableSpace = -1L
        val stat = StatFs(Environment.getExternalStorageDirectory().path)
        availableSpace = stat.availableBlocksLong * stat.blockSizeLong
        return availableSpace
    }

    fun writePinToSharedPreferences(pin: String?) {
        //PrefsController.putString(getString(R.string.key_pin),Utils.sha256(pin));
        SuperSafeApplication.getInstance().writeKey(pin)
    }

    fun getPinFromSharedPreferences(): String? {
        //PrefsController.getString(getString(R.string.key_pin), "");
        return SuperSafeApplication.getInstance().readKey()
    }

    fun writeFakePinToSharedPreferences(pin: String?) {
        //PrefsController.putString(getString(R.string.key_pin),Utils.sha256(pin));
        SuperSafeApplication.getInstance().writeFakeKey(pin)
    }

    fun getFakePinFromSharedPreferences(): String? {
        //PrefsController.getString(getString(R.string.key_pin), "");
        return SuperSafeApplication.getInstance().readFakeKey()
    }

    fun isExistingFakePin(pin: String?, currentPin: String?): Boolean {
        return pin == currentPin
    }

    fun isExistingRealPin(pin: String?, currentPin: String?): Boolean {
        return pin == currentPin
    }

    fun onHomePressed(){
        putScreenStatus(EnumPinAction.SCREEN_LOCK.ordinal)
        Log(TAG, "Pressed home button")
        if (!SingletonManager.getInstance().isVisitLockScreen()){
            SuperSafeApplication.getInstance().getActivity()?.let {
                Navigator.onMoveToVerifyPin(it, EnumPinAction.NONE)
                Log(TAG, "Navigation to Verify PIN")
            }
            SingletonManager.getInstance().setVisitLockScreen(true)
            Log(TAG, "---------------------------------------------------------------------------------Verify pin---------------------------------------------------------------------------------")
        } else {
            Log(TAG, "Verify pin already")
        }
    }

    fun getUserId(): String? {
        try {
            val mUser = getUserInfo()
            if (mUser != null) {
                return mUser.email
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun getUserCloudId(): String? {
        try {
            val mUser = getUserInfo()
            if (mUser != null) {
                return mUser.cloud_id
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun getDeviceId() : String {
        return SuperSafeApplication.getInstance().getDeviceId()
    }

    fun getAccessToken(): String? {
        try {
            val user = getUserInfo()
            if (user != null) {
                return user.author?.session_token
            }
        } catch (e: Exception) {
        }
        return SecurityUtil.DEFAULT_TOKEN
    }

    fun getDriveAccessToken() : String? {
        val mUser = getUserInfo()
        return mUser?.access_token
    }

    fun getMicAccessToken() : String? {
        val mUser = getUserInfo()
        return mUser?.email_token?.access_token
    }

    fun onPushEventBus(status: EnumStatus?) {
        EventBus.getDefault().post(status)
    }


    /*Remove duplicated item for download id*/
    fun clearListFromDuplicate(globalList: MutableList<ItemModel>, localList: MutableList<ItemModel>): MutableList<ItemModel>? {
        val modelMap: MutableMap<String, ItemModel> = HashMap<String, ItemModel>()
        val mList: MutableList<ItemModel> = ArrayList<ItemModel>()
        if (globalList.size == 0) {
            return mList
        }

        /*Merged local data*/
        val mLocalList: MutableList<ItemModel> = getMergedOriginalThumbnailList(false, localList)
        for (index in mLocalList) {
            index.global_id?.let {
                modelMap[it] = index
            }
        }

        /*Merged global data*/
        val mGlobalList: MutableList<ItemModel> = getMergedOriginalThumbnailList(true, globalList)
        for (index in mGlobalList) {
            val item: ItemModel? = modelMap[index.global_id]
            if (item != null) {
                if (index.global_id != item.global_id) {
                    mList.add(index)
                }
            } else {
                mList.add(index)
            }
        }
        return mList
    }

    /*Merge list original and thumbnail as list*/
    fun getMergedOriginalThumbnailList(isNotSync: Boolean, mDataList: MutableList<ItemModel>): MutableList<ItemModel> {
        val mList: MutableList<ItemModel> = ArrayList<ItemModel>()
        for (index in mDataList) {
            if (isNotSync) {
                if (!index.originalSync) {
                    mList.add(ItemModel(index, true))
                }
                val mType = EnumFormatType.values()[index.formatType]
                if (EnumFormatType.IMAGE == mType || EnumFormatType.VIDEO == mType) {
                    if (!index.thumbnailSync) {
                        mList.add(ItemModel(index, false))
                    }
                }
            } else {
                if (index.originalSync) {
                    mList.add(ItemModel(index, true))
                }
                val mType = EnumFormatType.values()[index.formatType]
                if (EnumFormatType.IMAGE == mType || EnumFormatType.VIDEO == mType) {
                    if (index.thumbnailSync) {
                        mList.add(ItemModel(index, false))
                    }
                }
            }
        }
        return mList
    }
    /*Check request delete item from global*/
    fun checkItemDeleteSyncedLocal(mSyncedList: List<ItemModel>): List<ItemModel> {
        val mListResult: MutableList<ItemModel> = ArrayList<ItemModel>()
        val mListLocal: List<ItemModel>? = SQLHelper.getListItemId(true, false)
        /*Convert list to hash-map*/
        val mMap: Map<String?, ItemModel> = mSyncedList.associateBy({ it.items_id }, { it })
        mListLocal?.let {
            for (index in it) {
                val mValue: ItemModel? = mMap[index.items_id]
                if (mValue == null) {
                    mListResult.add(index)
                }
            }
        }
        return mListResult
    }

    /*Check request delete category from global*/
    fun checkCategoryDeleteSyncedLocal(mSyncedList: List<MainCategoryModel>): List<MainCategoryModel> {
        val mListResult: MutableList<MainCategoryModel> = ArrayList<MainCategoryModel>()
        val mListLocal: List<MainCategoryModel>? = SQLHelper.requestSyncCategories(isSyncOwnServer = true, isFakePin = false)
        /*Convert list to hash-map*/
        val mMap: Map<String?, MainCategoryModel> = mSyncedList.associateBy({ it.categories_id }, { it })
        mListLocal?.let {
            for (index in it) {
                val mValue: MainCategoryModel? = mMap[index.categories_id]
                val mObject = SQLHelper.getItemsList(index.categories_id)
                if (mValue == null && mObject?.size == 0) {
                    mListResult.add(index)
                }
            }
        }
        Log(TAG, "checking category ${mListResult.size}")
        return mListResult
    }

    /*Delete folder*/
    fun onDeleteItemFolder(item_id: String?) {
        val path: String = SuperSafeApplication.getInstance().getSuperSafePrivate() + item_id
        Log(TAG, "Delete folder $path")
        (SuperSafeApplication.getInstance().getSuperSafePrivate() + item_id).deleteDirectory()
    }

    fun onDeleteFile(file_path: String?) {
        file_path?.deleteFile()
    }

    /*Create folder*/
    fun createDestinationDownloadItem(items_id: String?): String {
        val path: String = SuperSafeApplication.getInstance().getSuperSafePrivate()
        return "$path$items_id/"
    }

    fun getOriginalPath(currentTime: String?, items_id: String?): String {
        val rootPath: String = SuperSafeApplication.getInstance().getSuperSafePrivate()
        val pathContent = "$rootPath$items_id/"
        createDirectory(pathContent)
        return pathContent + currentTime
    }

    /*Create folder*/
    private fun createDirectory(path: String?): Boolean {
        val directory = File(path ?: "")
        if (directory.exists()) {
            Log(TAG, "Directory '$path' already exists")
            return false
        }
        return directory.mkdirs()
    }

    fun isNotEmptyOrNull(value: String?): Boolean {
        return !(value == null || value == "" || value == "null")
    }

    fun checkSaverToDelete(originalPath: String?, isOriginalGlobalId: Boolean) {
        if (isSaverSpace()) {
            if (originalPath?.isFileExist() == true) {
                if (isOriginalGlobalId) {
                    onDeleteFile(originalPath)
                }
            }
        }
    }

    fun clearAppDataAndReCreateData(){
        SuperSafeApplication.getInstance().initData()
        SuperSafeApplication.getInstance().deleteFolder()
        SuperSafeApplication.getInstance().initFolder()
        InstanceGenerator.getInstance(SuperSafeApplication.getInstance())?.cleanUp()
        InstanceGenerator.getInstance(SuperSafeApplication.getInstance())
        SQLHelper.onCleanDatabase()
        PrefsController.clear()
        putUserPreShare(User())
        SQLHelper.getList()
        EncryptDecryptFilesHelper.getInstance()?.cleanUp()
        EncryptDecryptPinHelper.getInstance()?.cleanUp()
        val account: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(SuperSafeApplication.getInstance())
        if (account!=null){
            putRequestGoogleDriveSignOut(true)
        }else{
            putRequestGoogleDriveSignOut(false)
        }
        ServiceManager.getInstance()?.onStopService()
        ServiceManager.getInstance()?.cleanUp()
        setRequestSyncData(true)
        Log(TAG, "clearAppDataAndReCreateData")
    }

    fun setDriveConnect(isConnected: Boolean? = null, accessToken: String? = null, cloudId: String? = null){
        val mUser = getUserInfo()
        isConnected?.let {
            Log(TAG, "show drive connected here $it")
            mUser?.driveConnected = it
        }
        if (!accessToken.isNullOrEmpty()){
            mUser?.access_token = accessToken
        }
        if (!cloudId.isNullOrEmpty()){
            mUser?.cloud_id = cloudId
        }
        putUserPreShare(mUser)
    }

    fun setEmailToken(data: EmailToken?){
        val mUser = getUserInfo()
        val token: EmailToken? = mUser?.email_token
        token?.access_token = data?.token_type + " " + data?.access_token
        token?.refresh_token = data?.refresh_token
        token?.token_type = data?.token_type
        mUser?.email_token = token
        putUserPreShare(mUser)
    }

    fun onScanFile(activity: Context, nameLogs: String?) {
        if (PermissionChecker.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED) {
            Log(TAG, "Granted permission....")
            val path = SuperSafeApplication.getInstance().getSuperSafe()
            /*Scan internal card*/
            MediaScannerConnection.scanFile(activity, arrayOf("file".getExternalStorageDirectory()), null, null)
            /*Scan internal app*/
            MediaScannerConnection.scanFile(activity, arrayOf(path), null, null)
            EncryptDecryptPinHelper.getInstance()?.createFile("$path/$nameLogs", "")
        } else {
            Log(TAG, "No permission")
        }
    }

    fun isRealCheckedOut(orderId: String): Boolean {
        return orderId.contains("GPA")
    }

    fun isPremium(): Boolean {
        if (SuperSafeApplication.getInstance().isDebugPremium()) {
            return true
        }
        val mCheckout: CheckoutItems? = getCheckoutItems()
        mCheckout?.let { mCheckoutResult ->
            if (mCheckoutResult.isPurchasedLifeTime || mCheckoutResult.isPurchasedOneYears || mCheckoutResult.isPurchasedSixMonths) {
                return true
            }
        }
        return false
    }

    fun isVerifiedAccount() : Boolean{
        val mUser = getUserInfo()
        return mUser?.verified ?: false
    }

    fun isCheckingAllowUpload(): Boolean {
        val mUser = getUserInfo() ?: return false
        val syncData: SyncData? = mUser.syncData
        if (!isPremium()) {
            syncData?.let { mSyncDataResult ->
                if (mSyncDataResult.left == 0) {
                    return false
                }
            }
        }
        return true
    }

    fun isCheckingAllowDriveConnect(): Boolean {
        val mUser = getUserInfo()
        mUser?.let { mUserResult ->
            if (mUserResult.driveConnected) {
                if (mUserResult.access_token != null && mUserResult.access_token != "") {
                    if (mUserResult.cloud_id != null && mUserResult.cloud_id != "" && !isPauseSync()) {
                        return true
                    } else {
                        mUserResult.driveConnected = false
                        mUserResult.access_token = null
                        putUserPreShare(mUser)
                    }
                }
            }
        }
        return false
    }

    fun isConnectedToGoogleDrive(): Boolean {
        val mAuthor: User? = getUserInfo()
        return mAuthor?.driveConnected ?: false
    }

    fun isAvailableBiometric() : Boolean{
       return PrefsController.getBoolean(SuperSafeApplication.getInstance().getString(R.string.key_fingerprint_unlock), false)
    }
    fun getSyncData() : SyncData? {
        return getUserInfo()?.syncData
    }

    fun onBasicAlertNotify(context: Activity, title: String? = "Warning", message: String) {
        val theme = ThemeApp.getInstance()?.getThemeInfo()
        Alerter.create(context)
                .setTitle(title!!)
                .setBackgroundColorInt(ContextCompat.getColor(context, theme?.getAccentColor()
                        ?: R.color.colorAccent))
                .setText(message)
                .show()
    }

    fun onAlertNotify(context: Activity, title: String, message: String, listener: UtilsListener? = null) {
        val theme = ThemeApp.getInstance()?.getThemeInfo()
        Alerter.create(context)
                .setBackgroundColorInt(ContextCompat.getColor(context, theme?.getAccentColor()
                        ?: R.color.colorAccent))
                .setTitle(title)
                .setText(message)
                .setDuration(10000)
                .addButton("Yes", R.style.AlertButton, View.OnClickListener {
                    listener?.onPositive()
                })
                .show()
    }

    fun isSensorAvailable(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val bm = BiometricManager.from(SuperSafeApplication.getInstance())
            val canAuthenticate = bm.canAuthenticate()
            (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS)
        } else {
            false
        }
    }

    fun getPositionThemeMode(): Int {
        return PrefsController.getInt(SuperSafeApplication.getInstance().getString(R.string.key_position_theme), 0)
    }

    fun setPositionThemeMode(positionTheme: Int) {
        PrefsController.putInt(SuperSafeApplication.getInstance().getString(R.string.key_position_theme), positionTheme)
    }

    fun getCurrentThemeMode(): Int {
        return if (getPositionThemeMode() == 0) {
            R.style.LightDialogTheme
        } else R.style.DarkDialogTheme
    }

    fun getThemeColor() : Int{
        return PrefsController.getInt(SuperSafeApplication.getInstance().getString(R.string.key_theme_object), 0)
    }

    fun putThemeColor(position: Int){
        PrefsController.putInt(SuperSafeApplication.getInstance().getString(R.string.key_theme_object), position)
    }

    fun deleteFolderOfItemId(items_id: String) {
        (SuperSafeApplication.getInstance().getSuperSafePrivate() + items_id).deleteDirectory()
    }

    fun isCameraAvailable(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
    }

    /*Stopping saver space*/
    fun stopSaverSpace(){
        val mList: MutableList<ItemModel>? = SQLHelper.getListSyncData(isSyncCloud = true, isFakePin = false)
        mList?.let { mResultList ->
            for (i in mResultList.indices) {
                when (EnumFormatType.values()[mResultList[i].formatType]) {
                    EnumFormatType.IMAGE -> {
                        if (mResultList[i].originalSync) {
                            mResultList[i].isSyncCloud = false
                            mResultList[i].originalSync = false
                            mResultList[i].isSaver = false
                            mResultList[i].statusProgress = EnumStatusProgress.NONE.ordinal
                            SQLHelper.updatedItem(mResultList[i])
                        }
                    }
                    else -> Log(TAG, "Nothing")
                }
            }
        }
    }

    /*Stopping premium features*/
    fun stoppingPremiumFeatures(){
        putFacePin(false)
        putBreakAlert(false)
        putSecretDoor(false)
        if(isSaverSpace()){
            stopSaverSpace()
            putSaverSpace(false)
        }
        if (getPositionThemeMode()>0){
            setPositionThemeMode(0)
            ThemeHelper.applyTheme(EnumThemeModel.byPosition(getPositionThemeMode()))
        }
        if (getThemeColor()>0){
            putThemeColor(0)
        }
    }

    fun checkingServicesToStopPremiumFeatures() : Boolean{
        if (isSaverSpace() || getThemeColor()>0 || getPositionThemeMode() > 0 || isFacePin() || isBreakAlert()){
            return true
        }
        return false
    }

    fun checkingExistingSaver(){
        if (!isSaverSpace()){
            val mList: MutableList<ItemModel>? = SQLHelper.getListSyncData(isSyncCloud = true, isSaver = true, isFakePin = false)
            mList?.let {
                if (it.size>0){
                    stopSaverSpace()
                }
            }
        }
    }

    fun isVertical() : Boolean {
        return PrefsController.getBoolean(SuperSafeApplication.getInstance().getString(R.string.key_vertical_adapter), false)
    }

    fun setIsVertical(isVertical: Boolean){
        return PrefsController.putBoolean(SuperSafeApplication.getInstance().getString(R.string.key_vertical_adapter), isVertical)
    }

    fun geOutputExportFiles(item: ItemModel, isSharingFiles: Boolean) : File?{
        var mFolderName = SuperSafeApplication.getInstance().getSuperSafePicture()
        if (isSharingFiles){
            mFolderName = SuperSafeApplication.getInstance().getSuperSafeShare()
        }
        var output: File? = File(mFolderName + item.title)
        if (output?.isFileExist(output.absolutePath) == true) {
            output = File(mFolderName + output.nameWithoutExtension + "(${System.currentTimeMillis()})" + item.fileExtension)
        }
        return output
    }

    fun geInputExportFiles(item: ItemModel) : File{
        val path = item.getOriginal()
        return File(path)
    }

    fun isRequestDeletedLocal(index: ItemModel) :Boolean{
        val formatTypeFile = EnumFormatType.values()[index.formatType]
        return if (formatTypeFile == EnumFormatType.AUDIO && index.global_original_id == null) {
            true
        } else if (formatTypeFile == EnumFormatType.FILES && index.global_original_id == null) {
            true
        } else (index.global_original_id == null) and (index.global_thumbnail_id == null)
    }

    fun availableToSave(mData: MutableList<ItemModel>) : String?{
        var spaceAvailable: Long = 0
        for (index in mData) {
            if (index.isSaver) {
                spaceAvailable += index.size?.toLong()!!
            }
        }
        val availableSpaceOS: Long = getAvailableSpaceInBytes()
        if (availableSpaceOS < spaceAvailable) {
            val mResutSpace = spaceAvailable - availableSpaceOS
            val result: String? = ConvertUtils.byte2FitMemorySize(mResutSpace)
            return result
        }
        return null
    }

    fun getDataItemsFromImport(mainCategory: MainCategoryModel, mData: MutableList<ImageModel>) : MutableList<ImportFilesModel> {
        val mList = mutableListOf<ImportFilesModel>()
        for (index in mData){
            val path = index.path
            val name = index.name
            val id = "" + index.id
            val mimeType: String? = Utils.getMimeType(path)
            Log(TAG, "mimeType $mimeType")
            Log(TAG, "name $name")
            Log(TAG, "path $path")
            val fileExtension: String = getFileExtension(path)
            Log(TAG, "file extension " + getFileExtension(path))
            try {
                val mimeTypeFile: MimeTypeFile? = mediaTypeSupport()[fileExtension]
                 mimeTypeFile?.let { mResult ->
                     mResult.name = name
                     val importFiles = ImportFilesModel(mainCategory, mResult, path, 0, false)
                     mList.add(importFiles)
                 }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return mList
    }

    fun getId() : String?{
        val mData = getUserInfo()
        return mData?._id
    }

    fun isRequestUpload() : Boolean{
        if (isCheckingAllowDriveConnect() && isCheckingAllowUpload()){
            val mResult: MutableList<ItemModel>? = SQLHelper.getItemListUpload()
            mResult?.let {
                if (it.size>0){
                    return true
                }
            }
        }
        return false
    }

    fun deletedItemsOnAnotherCloudId(){
        val mList: MutableList<ItemModel>? = SQLHelper.getListSyncData(isSyncCloud = true, isFakePin = false)
        mList?.let {
            for (index in it){
                SQLHelper.deleteItem(index)
                deleteFolderOfItemId(SuperSafeApplication.getInstance().getSuperSafePrivate() + index.items_id)
            }
        }
    }

   fun getString(res : Int) : String {
       return SuperSafeApplication.getInstance().getString(res)
   }
}

interface UtilsListener {
    fun onPositive()
    fun onNegative()
}