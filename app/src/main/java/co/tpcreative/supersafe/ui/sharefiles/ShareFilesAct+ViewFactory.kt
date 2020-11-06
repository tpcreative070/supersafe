package co.tpcreative.supersafe.ui.sharefiles
import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.View
import androidx.core.content.ContextCompat
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.controller.PrefsController
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.controller.SingletonManagerProcessing
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.PathUtil
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.*
import co.tpcreative.supersafe.ui.restore.onStartProgressing
import com.google.gson.Gson
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_share_files.*
import java.io.File
import java.util.ArrayList

fun ShareFilesAct.initUI(){
    TAG = this::class.java.simpleName
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
    onShowUI(View.GONE)
    onAddPermission()
    try {
        val themeApp: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
        if (themeApp != null) {
            tvTitle?.setTextColor(ContextCompat.getColor(this,themeApp.getAccentColor()))
        }
    } catch (e: Exception) {
        PrefsController.putInt(SuperSafeApplication.getInstance().getString(R.string.key_theme_object), 0)
    }

    btnGotIt.setOnClickListener {
        finish()
    }
}

fun ShareFilesAct.onAddPermission() {
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
            .withErrorListener { Utils.Log(TAG, "error ask permission") }.onSameThread().check()
}

fun ShareFilesAct.onHandlerIntent() {
    try {
        val intent: Intent = getIntent()
        val action: String? = intent.getAction()
        val type: String? = intent.getType()
        Utils.Log(TAG, "original type :$type")
        if (Intent.ACTION_SEND == action && type != null) {
            handleSendSingleItem(intent)
        } else if (Intent.ACTION_SEND_MULTIPLE == action && type != null) {
            handleSendMultipleFiles(intent)
        } else {
            Utils.Log(TAG, "Sending items is not existing")
        }
    } catch (e: Exception) {
        finish()
        e.printStackTrace()
    }
}

fun ShareFilesAct.handleSendSingleItem(intent: Intent) {
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
                Utils.Log(TAG, "Path existing " + mStore?.isFileExist(imageUri.path))
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
                finish()
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

fun ShareFilesAct.handleSendMultipleFiles(intent: Intent?) {
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
                    finish()
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

fun ShareFilesAct.onStartProgressing() {
    try {
        SingletonManagerProcessing.getInstance()?.onStartProgressing(this,R.string.importing)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun ShareFilesAct.onStopProgressing() {
    Utils.Log(TAG, "onStopProgressing")
    try {
        SingletonManagerProcessing.getInstance()?.onStopProgressing(this)
    } catch (e: Exception) {
        Utils.Log(TAG, e.message+"")
    }
}

fun ShareFilesAct.onShowUI(res: Int) {
    runOnUiThread(Runnable {
        try {
            tvTitle?.visibility = res
            imgChecked?.visibility = res
            btnGotIt?.visibility = res
            rlProgress?.visibility = res
            tvTitle?.text = kotlin.String.format(getString(R.string.imported_file_successful), "" + count)
        } catch (e: Exception) {
            finish()
        }
    })
}