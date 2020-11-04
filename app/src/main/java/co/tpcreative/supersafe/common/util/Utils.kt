package co.tpcreative.supersafe.common.util
import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Patterns
import android.util.TypedValue
import android.view.Display
import android.view.View
import android.view.animation.TranslateAnimation
import android.view.inputmethod.InputMethodManager
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
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
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.listener.Listener
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.model.*
import com.afollestad.materialdialogs.MaterialDialog
import com.anjlab.android.iab.v3.PurchaseData
import com.google.android.material.snackbar.Snackbar
import com.google.api.client.util.Base64
import com.google.common.base.Charsets
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.snatik.storage.Storage
import com.snatik.storage.helpers.OnStorageListener
import com.snatik.storage.helpers.SizeUnit
import com.snatik.storage.security.SecurityUtil
import com.tapadoo.alerter.Alerter
import io.reactivex.Completable
import io.reactivex.CompletableObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import org.apache.commons.io.FilenameUtils
import org.greenrobot.eventbus.EventBus
import java.io.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by pc on 07/16/2017.
 */
object Utils  {
    fun onChangeCategories(mainCategories: MainCategoryModel): Boolean {
        try {
            val hex_name = mainCategories.categories_name?.let { getHexCode(it) }
            val mIsFakePin: Boolean = mainCategories.isFakePin
            val response: MainCategoryModel? = SQLHelper.getCategoriesItemId(hex_name, mIsFakePin)
            if (response == null) {
                mainCategories.categories_hex_name = hex_name
                mainCategories.isChange = true
                mainCategories.isSyncOwnServer = false
                SQLHelper.updateCategory(mainCategories)
                return true
            }
            Log(TAG, "value changed :" + Gson().toJson(response))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    val GOOGLE_CONSOLE_KEY: String = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAk+6HXAFTNx3LbODafbpgsLqkdyMqMEvIYt55lqTjLIh0PkoAX7oSAD0fY7BXW0Czuys13hNNdyzmDjQe76xmUWTNfXM1vp0JQtStl7tRqNaFuaRje59HKRLpRTW1MGmgKw/19/18EalWTjbGOW7C2qZ5eGIOvGfQvvlraAso9lCTeEwze3bmGTc7B8MOfDqZHETdavSVgVjGJx/K10pzAauZFGvZ+ryZtU0u+9ZSyGx1CgHysmtfcZFKqZLbtOxUQHpBMeJf2M1LReqbR1kvJiAeLYqdOMWzmmNcsEoG6g/e+F9ZgjZjoQzqhWsrTE2IQZAaiwU4EezdqqruNXx6uwIDAQAB"

        // utility function
        var FORMAT_TIME: String? = "yyyy-MM-dd HH:mm:ss"
        var FORMAT_TIME_FILE_NAME: String? = "yyyyMMdd_HHmmss"
        const val COUNT_RATE = 9
        const val START_TIMER: Long = 5000
        private val storage: Storage = Storage(SuperSafeApplication.getInstance())
        private val TAG = Utils::class.java.simpleName
        private fun bytesToHexString(bytes: ByteArray): String {
            return bytes.joinToString("") { "%02x".format(it) }
        }

        fun isValidEmail(target: CharSequence?): Boolean {
            return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches()
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
            builder.negativeButton {ls.onCancel() }
            builder.positiveButton {ls.onCompleted() }
            builder.show()
        }

        fun mCreateAndSaveFileOverride(fileName: String?, path_folder_name: String?, responseJson: String?, append: Boolean): Boolean {
            Log(TAG, "path $path_folder_name")
            val newLine = System.getProperty("line.separator")
            return try {
                val root = File("$path_folder_name/$fileName")
                val saved = storage.getSize(root, SizeUnit.MB)
                if (saved >= 1) {
                    storage.deleteFile(root.absolutePath)
                }
                if (!root.exists()) {
                    val parentFolder = File(path_folder_name)
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
            val view: View? = context.getCurrentFocus()
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

        fun dpToPx(dp: Int): Int {
            val r: Resources = SuperSafeApplication.getInstance().getResources()
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    dp.toFloat(), r.displayMetrics).toInt()
        }

        fun showKeyboard(view: View?) {
            // Check if no view has focus:
            if (view != null) {
                val inputManager = SuperSafeApplication.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
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

        fun getFileExtension(url: String?): String? {
            return FilenameUtils.getExtension(url).toLowerCase()
        }

        fun Log(TAG: String, message: String?) {
            if (BuildConfig.DEBUG) {
                if (message != null) {
                    android.util.Log.d(TAG, message)
                }
            }
        }

    fun <T>Log(clazz: Class<T>, content: Any?){
        if (content is String){
            Log(clazz.simpleName, content)
        }else{
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
            val sdf = SimpleDateFormat("yyyyMMdd_HHmmss")
            try {
                val mDate = sdf.parse(value)
                val dateFormat = SimpleDateFormat("EE dd MMM, yyyy", Locale.getDefault())
                return dateFormat.format(mDate)
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            return ""
        }

        fun getCurrentDateTimeFormat(): String? {
            val date = Date()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            return dateFormat.format(date)
        }

        fun getHexCode(value: String): String {
            return Base64.encodeBase64String(value.toUpperCase(Locale.ROOT).toByteArray(Charsets.UTF_8))
        }

        fun onExportAndImportFile(input: String, output: String, ls: ServiceManager.ServiceManagerSyncDataListener) {
            val storage = Storage(SuperSafeApplication.getInstance())
            val mFile = storage.getFiles(input)
            try {
                for (index in mFile) {
                    if (storage.isFileExist(index.absolutePath)) {
                        storage.createFile(File(output + index.name), File(index.absolutePath), object : OnStorageListener {
                            override fun onSuccessful() {}
                            override fun onFailed() {
                                ls.onError()
                            }

                            override fun onSuccessful(path: String?) {}
                            override fun onSuccessful(position: Int) {}
                        })
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                ls.onCompleted()
            }
        }

        fun showToast(context: Context, @StringRes text: Int, isLong: Boolean) {
            showToast(context, context.getString(text), isLong)
        }

        fun showToast(context: Context, text: String?, isLong: Boolean) {
            Toast.makeText(context, text, if (isLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()
        }

        fun showInfoSnackbar(view: View, @StringRes text: Int, isLong: Boolean) {
            Handler(Looper.getMainLooper()).postDelayed({
                run {
                    multilineSnackbar(
                            Snackbar.make(
                                    view, text,
                                    if (isLong) Snackbar.LENGTH_LONG else Snackbar.LENGTH_SHORT))
                }
            }, 100)
        }

        fun showGotItSnackbar(view: View, @StringRes text: Int) {
            Handler(Looper.getMainLooper()).postDelayed({
                kotlin.run {
                    multilineSnackbar(
                            Snackbar.make(
                                    view, text, Snackbar.LENGTH_INDEFINITE)
                                    .setAction(R.string.got_it, View.OnClickListener { })
                    )
                }
            }, 200)
        }

        fun showGotItSnackbar(view: View, text: String) {
            onObserveData(START_TIMER, object : Listener {
                override fun onStart() {
                    multilineSnackbar(
                            Snackbar.make(
                                    view, text, Snackbar.LENGTH_INDEFINITE)
                                    .setAction(R.string.got_it, View.OnClickListener { })
                    ).show()
                }
            })
        }

        fun showGotItSnackbar(view: View, @StringRes text: Int, ls: ServiceManager.ServiceManagerSyncDataListener) {
            onObserveData(START_TIMER, object : Listener {
                override fun onStart() {
                    multilineSnackbar(
                            Snackbar.make(
                                    view, text, Snackbar.LENGTH_INDEFINITE)
                                    .setAction(R.string.got_it, View.OnClickListener { ls.onCompleted() })
                    ).show()
                }
            })
        }

        private fun multilineSnackbar(snackbar: Snackbar): Snackbar {
            val textView: AppCompatTextView = snackbar.view.findViewById(R.id.snackbar_text)
            textView.setMaxLines(5)
            return snackbar
        }

        fun slideToRight(view: View) {
            val animate = TranslateAnimation(0F, view.width.toFloat(), 0F, 0F)
            animate.setDuration(500)
            animate.setFillAfter(true)
            view.startAnimation(animate)
            view.setVisibility(View.GONE)
        }

        // To animate view slide out from right to left
        fun slideToLeft(view: View) {
            val animate = TranslateAnimation(0F, (-view.width).toFloat(), 0F, 0F)
            animate.setDuration(500)
            animate.setFillAfter(true)
            view.startAnimation(animate)
            view.setVisibility(View.GONE)
        }

        // To animate view slide out from top to bottom
        fun slideToBottomHeader(view: View) {
            val animate = TranslateAnimation(0F, 0F, (-view.height).toFloat(), 0F)
            animate.setDuration(500)
            animate.setFillAfter(true)
            view.startAnimation(animate)
        }

        // To animate view slide out from bottom to top
        fun slideToTopHeader(view: View) {
            Log(TAG, " " + view.height)
            val animate = TranslateAnimation(0F, 0F, 0F, (-view.height).toFloat())
            animate.setDuration(500)
            animate.setFillAfter(true)
            view.startAnimation(animate)
        }

        // To animate view slide out from top to bottom
        fun slideToBottomFooter(view: View) {
            val animate = TranslateAnimation(0F, 0F, 0F, view.height.toFloat())
            animate.setDuration(500)
            animate.setFillAfter(true)
            view.startAnimation(animate)
        }

        // To animate view slide out from bottom to top
        fun slideToTopFooter(view: View) {
            Log(TAG, " " + view.getHeight())
            val animate = TranslateAnimation(0F, 0F, view.width.toFloat(), 0F)
            animate.setDuration(500)
            animate.setFillAfter(true)
            view.startAnimation(animate)
        }

        fun stringToHex(content: String): String? {
            return Base64.encodeBase64String(content.toByteArray(Charsets.UTF_8))
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

        fun DeviceInfo(): String? {
            try {
                val manufacturer: String = Build.MANUFACTURER
                val model: String = Build.MODEL
                val version: Int = Build.VERSION.SDK_INT
                val versionRelease: String = Build.VERSION.RELEASE
                return """manufacturer $manufacturer 
 model $model 
 version $version 
 versionRelease $versionRelease 
 app version name ${BuildConfig.VERSION_NAME}"""
            } catch (e: Exception) {
                onWriteLog(e.message, EnumStatus.DEVICE_ABOUT)
            }
            return "Exception"
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

        fun onCheck() {
            val file = File(SuperSafeApplication.getInstance().getFileLogs())
            if (file.exists()) {
                val mSize = SuperSafeApplication.getInstance().getStorage()?.getSize(file, SizeUnit.MB)!!.toLong()
                if (mSize > 2) {
                    SuperSafeApplication.getInstance().getStorage()?.deleteFile(file.absolutePath)
                }
            }
        }

        fun shareMultiple(files: MutableList<File>, context: Activity) {
            val uris = ArrayList<Uri>()
            for (file in files) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val uri: Uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID.toString() + ".provider", file)
                    uris.add(uri)
                } else {
                    uris.add(Uri.fromFile(file))
                }
            }
            val intent = Intent(Intent.ACTION_SEND_MULTIPLE)
            intent.setType("*/*")
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
            context.startActivityForResult(Intent.createChooser(intent, context.getString(R.string.share)), Navigator.SHARE)
        }

        private fun getScreenSize(activity: Context): Point {
            val display: Display? = (activity as Activity?)?.getWindowManager()?.getDefaultDisplay()
            val size = Point()
            if (display != null) {
                display.getSize(size)
            }
            return size
        }

        fun getScreenWidth(activity: Context): Int {
            return getScreenSize(activity).x
        }

        fun getScreenHeight(activity: Context): Int {
            return getScreenSize(activity).y
        }

        fun getFontString(content: Int, value: String): String? {
            val themeApp: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
            return SuperSafeApplication.getInstance().getString(content, "<font color='" + themeApp?.getAccentColorHex() + "'>" + "<b>" + value + "</b>" + "</font>")
        }

        fun getFontString(content: Int, value: String?, fontSize: Int): String? {
            val themeApp: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
            return SuperSafeApplication.getInstance().getString(content, "<font size='" + fontSize + "' color='" + themeApp?.getAccentColorHex() + "'>" + "<b>" + value + "</b>" + "</font>")
        }

        fun appInstalledOrNot(uri: String): Boolean {
            val pm: PackageManager = SuperSafeApplication.getInstance().getPackageManager()
            try {
                pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES)
                return true
            } catch (e: PackageManager.NameNotFoundException) {
            }
            return false
        }

        fun objectToHashMap(items: PurchaseData?): MutableMap<String?, Any?>? {
            val type = object : TypeToken<MutableMap<String?, Any?>?>() {}.type
            return Gson().fromJson(Gson().toJson(items), type)
        }

        fun onDeleteTemporaryFile() {
            try {
                val rootDataDir: File? = SuperSafeApplication.getInstance().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                val list = rootDataDir?.listFiles()
                list.let {
                    if (it != null) {
                        for (i in it.indices) {
                            SuperSafeApplication.getInstance().getStorage()?.deleteFile(it[i].absolutePath)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun isLandscape(activity: AppCompatActivity): Boolean {
            val landscape: Boolean
            val displaymetrics = DisplayMetrics()
            activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics)
            val width: Int = displaymetrics.widthPixels
            val height: Int = displaymetrics.heightPixels
            landscape = if (width < height) {
                false
            } else {
                true
            }
            return landscape
        }

        /**
         * @return Number of bytes available on External storage
         */
        fun getAvailableSpaceInBytes(): Long {
            var availableSpace = -1L
            val stat = StatFs(Environment.getExternalStorageDirectory().path)
            availableSpace = stat.getAvailableBlocks().toLong()!! * stat.getBlockSize().toLong()!!
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

        fun isEnabledFakePin(): Boolean {
            return PrefsController.getBoolean(SuperSafeApplication.getInstance().getString(R.string.key_fake_pin), false)
        }

        fun isExistingFakePin(pin: String?, currentPin: String?): Boolean {
            return if (pin == currentPin) {
                true
            } else false
        }

        fun isExistingRealPin(pin: String?, currentPin: String?): Boolean {
            return if (pin == currentPin) {
                true
            } else false
        }

        fun onCheckNewVersion() {
            val current_code_version: Int = PrefsController.getInt(SuperSafeApplication.getInstance().getString(R.string.current_code_version), 0)
            if (current_code_version == BuildConfig.VERSION_CODE) {
                Log(TAG, "Already install this version")
                return
            } else {
                PrefsController.putInt(SuperSafeApplication.getInstance().getString(R.string.current_code_version), BuildConfig.VERSION_CODE)
                PrefsController.putBoolean(SuperSafeApplication.getInstance().getString(R.string.we_are_a_team), false)
                Log(TAG, "New install this version")
            }
        }

        fun onUpdatedCountRate() {
            val count: Int = PrefsController.getInt(SuperSafeApplication.getInstance().getString(R.string.key_count_to_rate), 0)
            if (count > 999) {
                PrefsController.putInt(SuperSafeApplication.getInstance().getString(R.string.key_count_to_rate), 0)
            } else {
                PrefsController.putInt(SuperSafeApplication.getInstance().getString(R.string.key_count_to_rate), count + 1)
            }
        }

        fun onObserveData(second: Long, ls: Listener) {
            Completable.timer(second, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                    .subscribe(object : CompletableObserver {
                        override fun onSubscribe(d: Disposable) {}
                        override fun onComplete() {
                            Log(TAG, "Completed")
                            ls.onStart()
                        }

                        override fun onError(e: Throwable) {}
                    })
        }

        fun onHomePressed() {
            PrefsController.putInt(SuperSafeApplication.getInstance().getString(R.string.key_screen_status), EnumPinAction.SCREEN_LOCK.ordinal)
            Log(TAG, "Pressed home button")
            if (!SingletonManager.getInstance().isVisitLockScreen()) {
                SuperSafeApplication.getInstance().getActivity()?.let { Navigator.onMoveToVerifyPin(it, EnumPinAction.NONE) }
                SingletonManager.getInstance().setVisitLockScreen(true)
                Log(TAG, "Verify pin")
            } else {
                Log(TAG, "Verify pin already")
            }
        }

        fun getUserInfo(): User? {
            try {
                val value: String? = PrefsController.getString(SuperSafeApplication.getInstance().getString(R.string.key_user), null)
                if (value != null) {
                    val mUser: User? = Gson().fromJson(value, User::class.java)
                    if (mUser != null) {
                        return mUser
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
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

        /*Checking allow sync data*/
        fun isAllowSyncData(): Boolean {
            return isAllowRequestDriveApis()
        }

        fun isPauseSync(): Boolean {
            return PrefsController.getBoolean(SuperSafeApplication.getInstance().getString(R.string.key_pause_cloud_sync), false)
        }

        fun isCheckSyncSuggestion(): Boolean {
            val name: String = SuperSafeApplication.getInstance().getString(R.string.key_count_sync)
            val mCount: Int = PrefsController.getInt(name, 0)
            val mSynced = getUserInfo()?.driveConnected
            mSynced?.let {
                if (!it) {
                    if (mCount == 5) {
                        PrefsController.putInt(name, 0)
                        return true
                    } else {
                        PrefsController.putInt(name, mCount + 1)
                    }
                }
            }
            return false
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

        fun onPushEventBus(status: EnumStatus?) {
            EventBus.getDefault().post(status)
        }

        /*Improved sync data*/ /*Filter only item already synced*/
        fun filterOnlyGlobalOriginalId(list1: MutableList<ItemModel>): MutableList<ItemModel>? {
            val mList: MutableList<ItemModel> = ArrayList<ItemModel>()
            for (index in list1) {
                if (index.isSyncCloud) {
                    mList.add(index)
                }
            }
            return mList
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
            Log(TAG, "onPreparingSyncData ==> Index download globalList " + Gson().toJson(globalList))
            Log(TAG, "onPreparingSyncData ==> Index download map " + Gson().toJson(modelMap))
            Log(TAG, "onPreparingSyncData ==> Index download list " + Gson().toJson(mGlobalList))
            for (index in mGlobalList) {
                val item: ItemModel? = modelMap[index.global_id]
                if (item != null) {
                    if (index.global_id != item.global_id) {
                        mList.add(index)
                        Log(TAG, "onPreparingSyncData ==> Index download" + Gson().toJson(index))
                    }
                } else {
                    mList.add(index)
                    Log(TAG, "onPreparingSyncData ==> Index download add " + Gson().toJson(index))
                }
            }
            return mList
        }

        /*Merge list to hash map for upload, download and delete*/
        fun mergeListToHashMap(mList: MutableList<ItemModel>): MutableMap<String, ItemModel> {
            val map: MutableMap<String, ItemModel> = HashMap<String, ItemModel>()
            for (index in mList) {
                index.unique_id?.let {
                    map[it] = index
                }
            }
            return map
        }

        /*Get the first of item data*/
        fun getArrayOfIndexHashMap(mMapDelete: MutableMap<String, ItemModel>?): ItemModel? {
            if (mMapDelete != null) {
                if (mMapDelete.size > 0) {
                    val model: ItemModel? = mMapDelete[mMapDelete.keys.toTypedArray()[0]]
                    Log(TAG, "Object need to be deleting " + Gson().toJson(model))
                    return model
                }
            }
            return null
        }

        /*Get the first of category data*/
        fun getArrayOfIndexCategoryHashMap(mMapDelete: MutableMap<String, MainCategoryModel>): MainCategoryModel? {
            if (mMapDelete != null) {
                if (mMapDelete.size > 0) {
                    val model: MainCategoryModel? = mMapDelete[mMapDelete.keys.toTypedArray()[0]]
                    Log(TAG, "Object need to be deleting " + Gson().toJson(model))
                    return model
                }
            }
            return null
        }

        /*Delete hash map after delete Google drive or Server system*/
        fun deletedIndexOfCategoryHashMap(itemModel: MainCategoryModel?, map: MutableMap<String, MainCategoryModel>?): Boolean {
            try {
                if (map != null) {
                    if (map.size > 0) {
                        itemModel?.unique_id?.let {
                            map.remove(it)
                        }
                        return true
                    }
                }
            } catch (e: Exception) {
                Log(TAG, "Could not delete hash map==============================>")
            }
            return false
        }

        /*Merge list to hash map for upload, download and delete*/
        fun mergeListToCategoryHashMap(mList: MutableList<MainCategoryModel>): MutableMap<String, MainCategoryModel> {
            val map: MutableMap<String, MainCategoryModel> = HashMap<String, MainCategoryModel>()
            for (index in mList) {
                index.unique_id?.let {
                    map[it] = index
                }
            }
            return map
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

        /*Delete hash map after delete Google drive and Server system*/
        fun deletedIndexOfHashMap(itemModel: ItemModel?, map: MutableMap<String, ItemModel>?): Boolean {
            try {
                if (map != null) {
                    if (map.size > 0) {
                        itemModel?.unique_id?.let {
                            map.remove(it)
                        }
                        return true
                    }
                }
            } catch (e: Exception) {
                Log(TAG, "Could not delete hash map==============================>")
            }
            return false
        }

        /*------------------------Import area-------------------*/ /*Add list to hash map for import*/
        fun mergeListToHashMapImport(mList: MutableList<ImportFilesModel>): MutableMap<String, ImportFilesModel>? {
            val map: MutableMap<String, ImportFilesModel> = HashMap<String, ImportFilesModel>()
            for (index in mList) {
                index.unique_id?.let {
                    map[it]= index
                }
            }
            return map
        }

        /*Get the first of data for import*/
        fun getArrayOfIndexHashMapImport(mMapDelete: MutableMap<String, ImportFilesModel>?): ImportFilesModel? {
            if (mMapDelete != null) {
                if (mMapDelete.size > 0) {
                    val model: ImportFilesModel? = mMapDelete[mMapDelete.keys.toTypedArray()[0]]
                    Log(TAG, "Object need to be deleting " + Gson().toJson(model))
                    return model
                }
            }
            return null
        }

        /*Delete hash map after delete Google drive and Server system for import*/
        fun deletedIndexOfHashMapImport(itemModel: ImportFilesModel?, map: MutableMap<String, ImportFilesModel>?): Boolean {
            try {
                if (map != null) {
                    if (map.size > 0) {
                        itemModel?.unique_id?.let {
                            map.remove(it)
                        }
                        return true
                    }
                }
            } catch (e: Exception) {
                Log(TAG, "Could not delete hash map==============================>")
            }
            return false
        }

    /*Convert list to hash-map*/
    fun convertItemListToMap(list: List<ItemModel>): Map<String, ItemModel>? {
        val mMap: MutableMap<String, ItemModel> = HashMap<String, ItemModel>()
        for (index in list) {
            mMap[index.items_id!!] = index
        }
        return mMap
    }

    /*Check request delete item from global*/
    fun checkItemDeleteSyncedLocal(mSyncedList: List<ItemModel>): List<ItemModel> {
        val mListResult: MutableList<ItemModel> = ArrayList<ItemModel>()
        val mListLocal: List<ItemModel>? = SQLHelper.getListItemId(true,false)
        /*Convert list to hash-map*/
        val mMap: Map<String?, ItemModel>? = mSyncedList.associateBy({it.items_id}, {it})
        Utils.Log(TAG,"checking item ${mListLocal?.size}")
        mListLocal?.let {
            for (index in it) {
                val mValue: ItemModel? = mMap?.get(index.items_id)
                Utils.Log(TAG,"checking item delete...")
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
        val mListLocal: List<MainCategoryModel>? = SQLHelper.getListCategories(false)
        /*Convert list to hash-map*/
        val mMap: Map<String?, MainCategoryModel>? = mSyncedList.associateBy({it.categories_id}, {it})
        mListLocal?.let {
            for (index in it) {
                val mValue: MainCategoryModel? = mMap?.get(index.categories_id)
                val mObject = SQLHelper.getItemsList(index.categories_id)
                Log(TAG,Gson().toJson(mValue))
                Log(TAG,Gson().toJson(mObject))
                if (mValue == null && mObject?.size == 0) {
                    mListResult.add(index)
                }
            }
        }
        Utils.Log(TAG,"checking category ${mListResult?.size}")
        return mListResult
    }

    /*Check saver space*/
        fun getSaverSpace(): Boolean {
            return PrefsController.getBoolean(SuperSafeApplication.getInstance().getString(R.string.key_saving_space), false)
        }

        /*Delete folder*/
        fun onDeleteItemFolder(item_id: String?) {
            val path: String = SuperSafeApplication.getInstance().getSuperSafePrivate() + item_id
            Log(TAG, "Delete folder $path")
            SuperSafeApplication.getInstance().getStorage()!!.deleteDirectory(SuperSafeApplication.getInstance().getSuperSafePrivate() + item_id)
        }

        fun onDeleteFile(file_path: String?) {
            SuperSafeApplication.getInstance().getStorage()!!.deleteFile(file_path)
        }

        /*Create folder*/
        fun createDestinationDownloadItem(items_id: String?): String? {
            val path: String? = SuperSafeApplication.getInstance().getSuperSafePrivate()
            return "$path$items_id/"
        }

        fun getOriginalPath(currentTime: String?, items_id: String?): String? {
            val rootPath: String? = SuperSafeApplication.getInstance().getSuperSafePrivate()
            val pathContent = "$rootPath$items_id/"
            createDirectory(pathContent)
            return pathContent + currentTime
        }

        /*Create folder*/
        fun createDirectory(path: String?): Boolean {
            val directory = File(path)
            if (directory.exists()) {
                Utils.Log(TAG, "Directory '$path' already exists")
                return false
            }
            return directory.mkdirs()
        }

        fun isNotEmptyOrNull(value: String?): Boolean {
            return if (value == null || value == "" || value == "null") {
                false
            } else true
        }

        fun getCheckedList(mList: MutableList<ItemModel>): MutableList<ItemModel> {
            val mResult: MutableList<ItemModel> = ArrayList<ItemModel>()
            for (index in mList) {
                if (index.isChecked) {
                    mResult.add(index)
                }
            }
            return mResult
        }

        fun checkSaverToDelete(originalPath: String?, isOriginalGlobalId: Boolean) {
            if (getSaverSpace()) {
                if (SuperSafeApplication.getInstance().getStorage()!!.isFileExist(originalPath)) {
                    if (isOriginalGlobalId) {
                        onDeleteFile(originalPath)
                    }
                }
            }
        }

        fun setUserPreShare(user: User?) {
            PrefsController.putString(SuperSafeApplication.Companion.getInstance().getString(R.string.key_user), Gson().toJson(user))
        }

        fun onScanFile(activity: Context, nameLogs: String?) {
            if (PermissionChecker.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED) {
                Log(TAG, "Granted permission....")
                val storage: Storage? = SuperSafeApplication.getInstance().getStorage()
                val path = SuperSafeApplication.getInstance().getSuperSafe()
                if (storage != null) {
                    val file = File(storage.externalStorageDirectory + "/" + nameLogs)
                    MediaScannerConnection.scanFile(activity, arrayOf(file.absolutePath), null, null)
                    MediaScannerConnection.scanFile(activity, arrayOf(storage.externalStorageDirectory), null, null)
                    storage.createFile(path + "/" + nameLogs, "")
//                    val bm: Bitmap = BitmapFactory.decodeResource(SuperSafeApplication.getInstance().resources, R.drawable.ic_drive_cloud)
//                    saveImage(bm)
//                    saveScanLog()
                }
            } else {
                Log(TAG, "No permission")
            }
        }

    /*Request from android greater than or equally*/
    fun saveScanLog(){
        val resolver = SuperSafeApplication.getInstance().contentResolver
        val contentValues = ContentValues()
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "image1")
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/supersafe")
        val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
        resolver.openOutputStream(uri!!).use {
            try {
               // it?.write("Hello".toByteArray())
                val finalBitmap = BitmapFactory.decodeResource(SuperSafeApplication.getInstance().resources, R.drawable.ic_drive_cloud)
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)
                it?.flush()
                it?.close()
                Utils.Log(TAG, "Created file successfully")
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                Utils.Log(TAG, "Could not create file")
            }
        }
    }

    fun saveImage() {
        val bm: Bitmap = BitmapFactory.decodeResource(SuperSafeApplication.getInstance().resources, R.drawable.ic_drive_cloud)
        val root: String = SuperSafeApplication.getInstance().getSuperSafe()!!
        val myDir = File(root)
        myDir.mkdirs()
        val file = File(myDir, "text.jpg")
        try {
            val out = FileOutputStream(file)
            bm.compress(Bitmap.CompressFormat.JPEG, 90, out)
            out.flush()
            out.close()
            Utils.Log(TAG, "Created file successfully")
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            Utils.Log(TAG, "Could not create file")
        }
    }

    fun checkRequestUploadItemData() {
            val mResult: MutableList<ItemModel>? = SQLHelper.getItemListUpload()
            if (mResult != null) {
                if (mResult.size > 0 && isCheckAllowUpload()) {
                    ServiceManager.getInstance()?.onPreparingSyncData()
                    return
                }
            }
            ServiceManager.getInstance()?.onDefaultValue()
            Log(TAG, "All items already synced...........")
        }

        fun isRealCheckedOut(orderId: String): Boolean {
            return if (orderId.contains("GPA")) {
                true
            } else false
        }

        fun setCheckoutItems(checkoutItems: CheckoutItems?) {
            PrefsController.putString(SuperSafeApplication.Companion.getInstance().getString(R.string.key_checkout_items), Gson().toJson(checkoutItems))
        }

        fun getCheckoutItems(): CheckoutItems? {
            val value: String? = PrefsController.getString(SuperSafeApplication.Companion.getInstance().getString(R.string.key_checkout_items), null)
            if (value != null) {
                val mResult: CheckoutItems? = Gson().fromJson(value, CheckoutItems::class.java)
                if (mResult != null) {
                    return mResult
                }
            }
            return null
        }

        fun isPremium(): Boolean {
        if (BuildConfig.DEBUG){
            return true
        }
            val mCheckout: CheckoutItems? = getCheckoutItems()
            if (mCheckout != null) {
                if (mCheckout.isPurchasedLifeTime || mCheckout.isPurchasedOneYears || mCheckout.isPurchasedSixMonths) {
                    return true
                }
            }
            return false
        }

        fun isCheckAllowUpload(): Boolean {
            val mUser = getUserInfo() ?: return false
            val syncData: SyncData? = mUser.syncData
            if (!isPremium()) {
                if (syncData != null) {
                    if (syncData.left == 0) {
                        return false
                    }
                }
            }
            return true
        }

        fun isAllowRequestDriveApis(): Boolean {
            val mUser = getUserInfo()
            if (mUser != null) {
                if (mUser.driveConnected) {
                    if (mUser.access_token != null && mUser.access_token != "") {
                        if (mUser.cloud_id != null && mUser.cloud_id != "" && !isPauseSync()) {
                            return true
                        }
                        else{
                            mUser.driveConnected = false
                            mUser.access_token = null
                            setUserPreShare(mUser)
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

    fun onBasicAlertNotify(context: Activity, title: String? = "Warning", message: String) {
        val theme = ThemeApp.getInstance()?.getThemeInfo()
        Alerter.create(context)
                .setTitle(title!!)
                .setBackgroundColorInt(ContextCompat.getColor(context, theme?.getAccentColor() ?: R.color.colorAccent))
                .setText(message)
                .show()
    }

    fun onAlertNotify(context: Activity, title: String, message: String, listener: UtilsListener? = null){
        val theme = ThemeApp.getInstance()?.getThemeInfo()
        Alerter.create(context)
                .setBackgroundColorInt(ContextCompat.getColor(context, theme?.getAccentColor() ?: R.color.colorAccent))
                .setTitle(title)
                .setText(message)
                .setDuration(10000)
                .addButton("Yes", R.style.AlertButton, View.OnClickListener {
                    listener?.onPositive()
                })
                .show()
    }

    fun isHardwareAvailable(): Boolean{
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            val bm = BiometricManager.from(SuperSafeApplication.getInstance())
            val canAuthenticate = bm.canAuthenticate()
            !(canAuthenticate == BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE || canAuthenticate == BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE)

        } else {
            false
        }
    }

    fun isSensorAvailable(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            val bm = BiometricManager.from(SuperSafeApplication.getInstance())
            val canAuthenticate = bm.canAuthenticate()
            (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS)
        } else {
            false
        }
    }

    fun  getPositionTheme() : Int{
        return PrefsController.getInt(SuperSafeApplication.getInstance().getString(R.string.key_position_theme), 0)
    }

    fun setPositionTheme(positionTheme: Int){
        PrefsController.putInt(SuperSafeApplication.getInstance().getString(R.string.key_position_theme), positionTheme)
    }

    fun deleteFolderOfItemId(items_id : String){
        SuperSafeApplication.getInstance().getStorage()?.deleteDirectory(SuperSafeApplication.getInstance().getSuperSafePrivate() + items_id)
    }

    /*Delete hash map after migrated item*/
    fun getArrayOfMigrationIndexHashMap(mMapDelete: MutableMap<String, MigrationModel>?): MigrationModel? {
        if (mMapDelete != null) {
            if (mMapDelete.size > 0) {
                val model: MigrationModel? = mMapDelete[mMapDelete.keys.toTypedArray()[0]]
                Log(TAG, "Object need to be deleting " + Gson().toJson(model))
                return model
            }
        }
        return null
    }

    /*Delete hash map after migrated item*/
    fun deletedIndexOfMigrationHashMap(uniId: String?, map: MutableMap<String, MigrationModel>?): Boolean {
        try {
            if (map != null) {
                if (map.isNotEmpty()) {
                    map.remove(uniId)
                    return true
                }
            }
        } catch (e: Exception) {
            Log(TAG, "Could not delete hash map==============================>")
        }
        return false
    }
}

interface UtilsListener {
    fun onPositive()
    fun onNegative()
}