package co.tpcreative.supersafe.ui.sharefiles
import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import butterknife.BindView
import butterknife.OnClick
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseActivityNone
import co.tpcreative.supersafe.common.controller.PrefsController
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.PathUtil
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.*
import com.google.gson.Gson
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.DexterError
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.PermissionRequestErrorListener
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.snatik.storage.Storage
import dmax.dialog.SpotsDialog
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.util.*

class ShareFilesActivity : BaseActivityNone() {
    val mListFile: MutableList<Int> = ArrayList()
    private var dialog: AlertDialog? = null

    @BindView(R.id.imgChecked)
    var imgChecked: AppCompatImageView? = null

    @BindView(R.id.btnGotIt)
    var btnGotIt: AppCompatButton? = null

    @BindView(R.id.tvTitle)
    var tvTitle: AppCompatTextView? = null

    @BindView(R.id.rlProgress)
    var rlProgress: RelativeLayout? = null
    private val mListImport: MutableList<ImportFilesModel> = ArrayList<ImportFilesModel>()
    private var count = 0
    protected override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share_files)
        val mUser: User? = Utils.getUserInfo()
        if (mUser != null) {
            if (mUser._id != null) {
                ServiceManager.getInstance()?.onInitConfigurationFile()
            } else {
                finish()
                return
            }
        } else {
            finish()
            return
        }
        storage = Storage(this)
        onShowUI(View.GONE)
        onAddPermission()
        try {
            val themeApp: ThemeApp? = ThemeApp.Companion.getInstance()?.getThemeInfo()
            if (themeApp != null) {
                tvTitle?.setTextColor(ContextCompat.getColor(this,themeApp.getAccentColor()))
            }
        } catch (e: Exception) {
            val themeApp = ThemeApp(0, R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorButton, "#0091EA")
            PrefsController.putString(SuperSafeApplication.getInstance().getString(R.string.key_theme_object), Gson().toJson(themeApp))
        }
    }

    fun onAddPermission() {
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report?.areAllPermissionsGranted()!!) {
                            onHandlerIntent()
                            SuperSafeApplication.getInstance().initFolder()
                        } else {
                            finish()
                            Utils.Log(TAG, "Permission is denied")
                        }
                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied) {
                            /*Miss add permission in manifest*/
                            Utils.Log(TAG, "request permission is failed")
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest?>?, token: PermissionToken?) {
                        /* ... */
                        token?.continuePermissionRequest()
                    }
                })
                .withErrorListener(object : PermissionRequestErrorListener {
                    override fun onError(error: DexterError?) {
                        Utils.Log(TAG, "error ask permission")
                    }
                }).onSameThread().check()
    }

    fun onHandlerIntent() {
        try {
            val intent: Intent = getIntent()
            val action: String? = intent.getAction()
            val type: String? = intent.getType()
            Utils.Log(TAG, "original type :$type")
            if (Intent.ACTION_SEND == action && type != null) {
                handleSendSingleItem(intent)
            } else if (Intent.ACTION_SEND_MULTIPLE == action && type != null) {
                handleSendMultipleFiles(intent, EnumFormatType.FILES)
            } else {
                Utils.Log(TAG, "Sending items is not existing")
            }
        } catch (e: Exception) {
            finish()
            e.printStackTrace()
        }
    }

    fun handleSendSingleItem(intent: Intent) {
        try {
            val imageUri : Uri? = intent.getParcelableExtra(Intent.EXTRA_STREAM)
            val type: String? = intent.getType()
            val list: MutableList<MainCategoryModel>? = SQLHelper.getList()
            val mainCategories: MainCategoryModel? = list?.get(0)
            if (imageUri != null && mainCategories != null) {
                mListFile.clear()
                onStartProgressing()
                var response : String? = ""
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Utils.Log(TAG, "value path :" + imageUri.path)
                    Utils.Log(TAG, "Path existing " + storage?.isFileExist(imageUri.path))
                    response = PathUtil.getRealPathFromUri(this, imageUri)
                    if (response == null) {
                        response = PathUtil.getFilePathFromURI(this, imageUri)
                    }
                } else {
                    response = PathUtil.getPath(this, imageUri)
                    if (response == null) {
                        response = PathUtil.getFilePathFromURI(this, imageUri)
                    }
                }
                if (response == null) {
                    onStopProgressing()
                    Utils.showGotItSnackbar(imgChecked!!, R.string.error_occurred, object : ServiceManager.ServiceManagerSyncDataListener {
                        override fun onCompleted() {
                            finish()
                        }

                        override fun onError() {}
                        override fun onCancel() {}
                    })
                } else {
                    val mFile = File(response)
                    if (mFile.exists()) {
                        val path = mFile.absolutePath
                        val name = mFile.name
                        val fileExtension: String? = Utils.getFileExtension(path)
                        val mimeType: String? = intent.getType()
                        Utils.Log(TAG, "file extension $fileExtension")
                        Utils.Log(TAG, "Path file :$path")
                        var mimeTypeFile: MimeTypeFile? = Utils.mediaTypeSupport().get(fileExtension)
                        if (mimeTypeFile == null) {
                            val mMimeTypeSupport: MimeTypeFile? = Utils.mimeTypeSupport().get(mimeType)
                            if (mMimeTypeSupport != null) {
                                when (mMimeTypeSupport.formatType) {
                                    EnumFormatType.IMAGE -> {
                                        mimeTypeFile = MimeTypeFile(mMimeTypeSupport.extension, EnumFormatType.IMAGE, mimeType)
                                        mimeTypeFile.name = Utils.getCurrentDateTime(Utils.FORMAT_TIME_FILE_NAME) + mMimeTypeSupport.extension
                                    }
                                    EnumFormatType.VIDEO -> {
                                        mimeTypeFile = MimeTypeFile(mMimeTypeSupport.extension, EnumFormatType.VIDEO, mimeType)
                                        mimeTypeFile.name = Utils.getCurrentDateTime(Utils.FORMAT_TIME_FILE_NAME) + mMimeTypeSupport.extension
                                    }
                                    EnumFormatType.AUDIO -> {
                                        mimeTypeFile = MimeTypeFile(mMimeTypeSupport.extension, EnumFormatType.AUDIO, mimeType)
                                        mimeTypeFile.name = Utils.getCurrentDateTime(Utils.FORMAT_TIME_FILE_NAME) + mMimeTypeSupport.extension
                                    }
                                    else -> {
                                        mimeTypeFile = MimeTypeFile(mMimeTypeSupport.extension, EnumFormatType.FILES, mimeType)
                                        mimeTypeFile.name = Utils.getCurrentDateTime(Utils.FORMAT_TIME_FILE_NAME) + mMimeTypeSupport.extension
                                    }
                                }
                            } else {
                                mimeTypeFile = MimeTypeFile(".$fileExtension", EnumFormatType.FILES, mimeType)
                                mimeTypeFile.name = name
                                Utils.Log(TAG, "type file $mimeType")
                            }
                        }
                        if (mimeTypeFile.name == null || mimeTypeFile.name == "") {
                            mimeTypeFile.name = name
                        }
                        count += 1
                        val importFiles = ImportFilesModel(mainCategories, mimeTypeFile, path, 0, false)
                        mListImport.add(importFiles)
                        ServiceManager.getInstance()?.setListImport(mListImport)
                        ServiceManager.getInstance()?.onPreparingImportData()
                    } else {
                        onStopProgressing()
                        finish()
                    }
                }
            } else {
                Utils.Log(TAG, "Nothing to do at single item")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun handleSendMultipleFiles(intent: Intent?, enumFormatType: EnumFormatType?) {
        try {
            val imageUris: ArrayList<Uri>? = intent?.getParcelableArrayListExtra(Intent.EXTRA_STREAM)
            val list: MutableList<MainCategoryModel>? = SQLHelper.getList()
            val mainCategories: MainCategoryModel? = list?.get(0)
            if (imageUris != null) {
                mListFile.clear()
                onStartProgressing()
                for (i in imageUris.indices) {
                    var response : String? = ""
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        response = PathUtil.getRealPathFromUri(this, imageUris[i])
                        if (response == null) {
                            response = PathUtil.getFilePathFromURI(this, imageUris[i])
                        }
                    } else {
                        response = PathUtil.getPath(this, imageUris[i])
                        if (response == null) {
                            response = PathUtil.getFilePathFromURI(this, imageUris[i])
                        }
                    }
                    if (response != null) {
                        val mFile = File(response)
                        if (mFile.exists()) {
                            val path = mFile.absolutePath
                            val name = mFile.name
                            val mimeType: String? = intent.getType()
                            val fileExtension: String? = Utils.getFileExtension(path)
                            Utils.Log(TAG, "file extension $fileExtension")
                            Utils.Log(TAG, "Path file :$path")
                            var mimeTypeFile: MimeTypeFile? = Utils.mediaTypeSupport().get(fileExtension)
                            if (mimeTypeFile == null) {
                                val mMimeTypeSupport: MimeTypeFile? = Utils.mimeTypeSupport().get(mimeType)
                                if (mMimeTypeSupport != null) {
                                    when (mMimeTypeSupport.formatType) {
                                        EnumFormatType.IMAGE -> {
                                            mimeTypeFile = MimeTypeFile(mMimeTypeSupport.extension, EnumFormatType.IMAGE, mimeType)
                                            mimeTypeFile.name = Utils.getCurrentDateTime(Utils.FORMAT_TIME_FILE_NAME) + mMimeTypeSupport.extension
                                        }
                                        EnumFormatType.VIDEO -> {
                                            mimeTypeFile = MimeTypeFile(mMimeTypeSupport.extension, EnumFormatType.VIDEO, mimeType)
                                            mimeTypeFile.name = Utils.getCurrentDateTime(Utils.FORMAT_TIME_FILE_NAME) + mMimeTypeSupport.extension
                                        }
                                        EnumFormatType.AUDIO -> {
                                            mimeTypeFile = MimeTypeFile(mMimeTypeSupport.extension, EnumFormatType.AUDIO, mimeType)
                                            mimeTypeFile.name = Utils.getCurrentDateTime(Utils.FORMAT_TIME_FILE_NAME) + mMimeTypeSupport.extension
                                        }
                                        else -> {
                                            mimeTypeFile = MimeTypeFile(mMimeTypeSupport.extension, EnumFormatType.FILES, mimeType)
                                            mimeTypeFile.name = Utils.getCurrentDateTime(Utils.FORMAT_TIME_FILE_NAME) + mMimeTypeSupport.extension
                                        }
                                    }
                                } else {
                                    mimeTypeFile = MimeTypeFile(".$fileExtension", EnumFormatType.FILES, mimeType)
                                    mimeTypeFile.name = name
                                    Utils.Log(TAG, "type file $mimeType")
                                }
                            }
                            if (mimeTypeFile.name == null || mimeTypeFile.name == "") {
                                mimeTypeFile.name = name
                            }
                            count += 1
                            val importFiles = ImportFilesModel(mainCategories, mimeTypeFile, path, i, false)
                            mListImport.add(importFiles)
                            Utils.Log(TAG, Gson().toJson(mimeTypeFile))
                        } else {
                            onStopProgressing()
                            finish()
                        }
                    } else {
                        onStopProgressing()
                        Utils.showGotItSnackbar(imgChecked!!, R.string.error_occurred, object : ServiceManager.ServiceManagerSyncDataListener {
                            override fun onCompleted() {
                                finish()
                            }
                            override fun onError() {}
                            override fun onCancel() {}
                        })
                    }
                }
                ServiceManager.getInstance()?.setListImport(mListImport)
                ServiceManager.getInstance()?.onPreparingImportData()
            } else {
                finish()
                Utils.Log(TAG, "Nothing to do at multiple items")
            }
        } catch (e: Exception) {
            finish()
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        ServiceManager.getInstance()?.setRequestShareIntent(true)
        Utils.onScanFile(this, "scan.log")
    }

    protected override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        Utils.onDeleteTemporaryFile()
    }

    protected override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: EnumStatus?) {
        when (event) {
            EnumStatus.FINISH -> {
                Navigator.onMoveToFaceDown(this)
            }
            EnumStatus.IMPORTED_COMPLETELY -> {
                try {
                    onStopProgressing()
                    onShowUI(View.VISIBLE)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun onStartProgressing() {
        try {
            runOnUiThread(Runnable {
                if (dialog == null) {
                    val themeApp: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
                    dialog = SpotsDialog.Builder()
                            .setContext(this@ShareFilesActivity)
                            .setDotColor(themeApp?.getAccentColor()!!)
                            .setMessage(getString(R.string.importing))
                            .setCancelable(true)
                            .build()
                }
                if (!dialog?.isShowing()!!) {
                    dialog?.show()
                    Utils.Log(TAG, "Showing dialog...")
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun onStopProgressing() {
        Utils.Log(TAG, "onStopProgressing")
        try {
            runOnUiThread(Runnable {
                if (dialog != null) {
                    dialog?.dismiss()
                }
            })
        } catch (e: Exception) {
            Utils.Log(TAG, e.message+"")
        }
    }

    fun onShowUI(res: Int) {
        runOnUiThread(Runnable {
            try {
                tvTitle?.setVisibility(res)
                imgChecked?.setVisibility(res)
                btnGotIt?.setVisibility(res)
                rlProgress?.setVisibility(res)
                tvTitle?.setText(kotlin.String.format(getString(R.string.imported_file_successful), "" + count))
            } catch (e: Exception) {
                finish()
            }
        })
    }

    @OnClick(R.id.btnGotIt)
    fun onClickedGotIt(view: View?) {
        finish()
    }

    companion object {
        private val TAG = ShareFilesActivity::class.java.simpleName
    }
}