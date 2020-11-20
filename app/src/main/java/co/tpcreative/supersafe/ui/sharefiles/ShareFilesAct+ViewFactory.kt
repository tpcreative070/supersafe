package co.tpcreative.supersafe.ui.sharefiles
import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.controller.PrefsController
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.controller.SingletonManagerProcessing
import co.tpcreative.supersafe.common.extension.isFileExist
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.network.Status
import co.tpcreative.supersafe.common.network.base.ViewModelFactory
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.PathUtil
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.*
import co.tpcreative.supersafe.viewmodel.ShareFilesViewModel
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_share_files.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.ArrayList

fun ShareFilesAct.initUI(){
    setupViewModel()
    TAG = this::class.java.simpleName
    if (Utils.getId().isNullOrEmpty()){
        finish()
        return
    }
    onShowUI(View.GONE)
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
    viewModel.isLoading.observe(this, Observer {
        if (it){
            SingletonManagerProcessing.getInstance()?.onStartProgressing(this,R.string.importing)
        }else{
            SingletonManagerProcessing.getInstance()?.onStopProgressing(this)
        }
    })
    viewModel.isLoading.postValue(true)
    onAddPermission()
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
        val action: String? = intent.action
        val type: String? = intent.type
        Utils.Log(TAG, "original type :$type")
        if (Intent.ACTION_SEND == action && type != null) {
            handleSendSingleItem(intent)
        } else if (Intent.ACTION_SEND_MULTIPLE == action && type != null) {
            handleSendMultipleFiles(intent)
        } else {
            Utils.Log(TAG, "Sending items is not existing")
        }
    } catch (e: Exception) {
        viewModel.isLoading.postValue(false)
        finish()
        e.printStackTrace()
    }
}

fun ShareFilesAct.handleSendSingleItem(intent: Intent) {
    try {
        val imageUri : Uri? = intent.getParcelableExtra(Intent.EXTRA_STREAM)
        val type: String? = intent.type
        val list: MutableList<MainCategoryModel>? = SQLHelper.getList()
        val mainCategories: MainCategoryModel? = list?.get(0)
        if (imageUri != null && mainCategories != null) {
            var response : String? = ""
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Utils.Log(TAG, "value path :" + imageUri.path)
                Utils.Log(TAG, "Path existing " + imageUri.path?.isFileExist())
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
                viewModel.isLoading.postValue(false)
                finish()
            } else {
                val mDataList = mutableListOf<ImportFilesModel>()
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
                    val importFiles = ImportFilesModel(mainCategories, mimeTypeFile, path, 0, false)
                    mDataList.add(importFiles)
                    importingData(mDataList)
                } else {
                    viewModel.isLoading.postValue(false)
                    finish()
                }
            }
        } else {
            Utils.Log(TAG, "Nothing to do at single item")
            viewModel.isLoading.postValue(false)
            finish()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        viewModel.isLoading.postValue(false)
        finish()
    }
}

fun ShareFilesAct.handleSendMultipleFiles(intent: Intent?) {
    try {
        val imageUris: ArrayList<Uri>? = intent?.getParcelableArrayListExtra(Intent.EXTRA_STREAM)
        val list: MutableList<MainCategoryModel>? = SQLHelper.getList()
        val mainCategories: MainCategoryModel? = list?.get(0)
        if (imageUris != null) {
            val mDataList = mutableListOf<ImportFilesModel>()
            for (index in imageUris) {
                var response : String? = ""
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    response = PathUtil.getRealPathFromUri(this, index)
                    if (response == null) {
                        response = PathUtil.getFilePathFromURI(this, index)
                    }
                } else {
                    response = PathUtil.getPath(this, index)
                    if (response == null) {
                        response = PathUtil.getFilePathFromURI(this, index)
                    }
                }
                if (response != null) {
                    val mFile = File(response)
                    if (mFile.exists()) {
                        val path = mFile.absolutePath
                        val name = mFile.name
                        val mimeType: String? = intent.type
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
                        mDataList.add(ImportFilesModel(mainCategories, mimeTypeFile, path, 0, false))
                    }
                }
            }
            importingData(mDataList)
        } else {
            viewModel.isLoading.postValue(false)
            finish()
            Utils.Log(TAG, "Nothing to do at multiple items")

        }
    } catch (e: Exception) {
        viewModel.isLoading.postValue(false)
        finish()
        e.printStackTrace()
    }
}

fun ShareFilesAct.onShowUI(res: Int,count : Int? = null) {
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

fun ShareFilesAct.importingData(mData : MutableList<ImportFilesModel>) = CoroutineScope(Dispatchers.Main).launch{
    val mResult = ServiceManager.getInstance()?.onImportData(mData)
    when(mResult?.status){
        Status.SUCCESS -> {
            viewModel.isLoading.postValue(false)
            onShowUI(View.VISIBLE,mData.size)
        }
        else -> {
            Utils.Log(TAG,mResult?.message)
            viewModel.isLoading.postValue(false)
        }
    }
}

private fun ShareFilesAct.setupViewModel() {
    viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory()
    ).get(ShareFilesViewModel::class.java)
}