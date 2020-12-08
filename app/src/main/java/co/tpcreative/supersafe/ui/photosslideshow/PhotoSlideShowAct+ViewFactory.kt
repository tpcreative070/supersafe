package co.tpcreative.supersafe.ui.photosslideshow
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.controller.SingletonManagerProcessing
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.network.Status
import co.tpcreative.supersafe.common.network.base.ViewModelFactory
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.NetworkUtil
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.*
import co.tpcreative.supersafe.viewmodel.PhotoSlideShowViewModel
import com.afollestad.materialdialogs.MaterialDialog
import kotlinx.android.synthetic.main.activity_photos_slideshow.*
import kotlinx.android.synthetic.main.footer_items.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

fun PhotoSlideShowAct.initUI(){
    TAG = this::class.java.simpleName
    setupViewModel()
    imgArrowBack?.setOnClickListener(this)
    imgOverflow?.setOnClickListener(this)
    imgDelete?.setOnClickListener(this)
    imgExport?.setOnClickListener(this)
    imgRotate?.setOnClickListener(this)
    imgShare?.setOnClickListener(this)
    imgMove?.setOnClickListener(this)
    /*Auto slide*/
    handler = Handler(Looper.getMainLooper())

    viewModel.isLoading.observe(this, Observer {
        if (progressing == EnumStepProgressing.DOWNLOADING){
            SingletonManagerProcessing.getInstance()?.onStopProgressing(this)
            SingletonManagerProcessing.getInstance()?.onStartProgressing(this,R.string.downloading)
        }
        else if (progressing == EnumStepProgressing.EXPORTING){
            SingletonManagerProcessing.getInstance()?.onStopProgressing(this)
            SingletonManagerProcessing.getInstance()?.onStartProgressing(this,R.string.exporting)
        }
        else{
            SingletonManagerProcessing.getInstance()?.onStopProgressing(this)
        }
    })
    getData()
}

fun PhotoSlideShowAct.onStartSlider() {
    try {
        handler?.postDelayed(runnable, delay.toLong())
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun PhotoSlideShowAct.onStopSlider() {
    try {
        handler?.removeCallbacks(runnable)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun PhotoSlideShowAct.onShowDialog(mData : MutableList<ItemModel>,status: EnumStatus?,isSharingFiles: Boolean) {
    var content: String = ""
    when (status) {
        EnumStatus.EXPORT -> {
            content = kotlin.String.format(getString(R.string.export_items), "1")
        }
        EnumStatus.SHARE -> {
            content = kotlin.String.format(getString(R.string.share_items), "1")
        }
        EnumStatus.DELETE -> {
            content = kotlin.String.format(getString(R.string.move_items_to_trash), "1")
        }
        EnumStatus.MOVE -> {
        }
        else -> Utils.Log(TAG,"Nothing")
    }
    val builder: MaterialDialog = MaterialDialog(this)
            .title(text = getString(R.string.confirm))
            .message(text = content)
            .negativeButton(text = getString(R.string.cancel))
            .positiveButton(text = getString(R.string.ok))
            .negativeButton {
            }
            .positiveButton {
                when (status) {
                    EnumStatus.SHARE -> {
                        exportingFiles(mData,isSharingFiles)
                    }
                    EnumStatus.EXPORT -> {
                        exportingFiles(mData,isSharingFiles)
                    }
                    EnumStatus.DELETE -> {
                        deleteItems()
                    }
                    else -> Utils.Log(TAG, "Nothing")
                }
            }
    builder.show()
}

fun PhotoSlideShowAct.onHideView() {
    if (isHide) {
        Utils.slideToTopHeader(rlTop!!)
        Utils.slideToBottomFooter(llBottom!!)
    } else {
        Utils.slideToBottomHeader(rlTop!!)
        Utils.slideToTopFooter(llBottom!!)
    }
}

fun PhotoSlideShowAct.onRotateBitmap(items: ItemModel) = CoroutineScope(Dispatchers.Main).launch{
    isProgressing = true
    var mDegrees: Int = items.degrees!!
    mDegrees = if (mDegrees >= 360) {
        90
    } else {
        if (mDegrees > 90) {
            mDegrees + 90
        } else {
            180
        }
    }
    val valueDegrees = mDegrees
    items.degrees = valueDegrees
    dataSource[position].degrees = valueDegrees
    SQLHelper.updatedItem(items)
    adapter?.notifyDataSetChanged()
    isProgressing = false
}

private fun PhotoSlideShowAct.setupViewModel() {
    viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory()
    ).get(PhotoSlideShowViewModel::class.java)
}

fun PhotoSlideShowAct.getData(){
    viewModel.isLoading.postValue(true)
    viewModel.getData(this@getData).observe(this@getData, Observer {
        when (it.status) {
            Status.SUCCESS -> {
                CoroutineScope(Dispatchers.Main).launch {
                    dataSource = it.data ?: mutableListOf()
                    Utils.Log(TAG,"data size ${it.data?.size}")
                    val mSetUp  = async {
                        adapter = SamplePagerAdapter(this@getData)
                        view_pager?.adapter = adapter
                    }
                    val mResult = async {
                        Utils.Log(TAG, "Loading...")
                        view_pager.setCurrentItem(viewModel.position,false)
                        adapter?.notifyDataSetChanged()
                    }
                    mSetUp.await()
                    mResult.await()
                    viewModel.isLoading.postValue(false)
                    attachFragment(R.id.gallery_root)
                    Utils.Log(TAG, "Loading done")
                }
            }
            else -> {
                Utils.Log(TAG, "Nothing")
                viewModel.isLoading.postValue(false)
            }
        }
    })
}

fun PhotoSlideShowAct.multipleDelete(){
    var i = 0
    while (i < dataSource.size) {
        if (dataSource[i].isChecked) dataSource.removeAt(i) else i++
    }
    if (dataSource.size==0){
        onBackPressed()
    }
    adapter?.notifyDataSetChanged()
}

fun PhotoSlideShowAct.deleteItems(isExport : Boolean? = false){
    viewModel.deleteItems(isExport).observe(this, Observer {
        multipleDelete()
    })
}

fun PhotoSlideShowAct.exportingFiles(mData : MutableList<ItemModel>,isSharingFiles : Boolean) = CoroutineScope(Dispatchers.Main).launch{
    if (Utils.getSaverSpace()){
        if (NetworkUtil.pingIpAddress(this@exportingFiles)){
            Utils.onBasicAlertNotify(this@exportingFiles,"Alert",getString(R.string.no_connection))
            return@launch
        }
        if(!Utils.isConnectedToGoogleDrive()){
            Utils.showDialog(this@exportingFiles,R.string.need_signed_in_to_google_drive_before_using_this_feature, object : ServiceManager.ServiceManagerSyncDataListener {
                override fun onCompleted() {
                    Navigator.onCheckSystem(this@exportingFiles, null)
                }
                override fun onError() {
                }
                override fun onCancel() {
                }
            })
            return@launch
        }
        progressing = EnumStepProgressing.DOWNLOADING
        viewModel.isLoading.postValue(true)
        val mDataRequestingDownload = mData.filter { value ->  EnumFormatType.values()[value.formatType] == EnumFormatType.IMAGE }.toMutableList()
        val mResultedDownloading = ServiceManager.getInstance()?.downloadFilesToExporting(mDataRequestingDownload)
        when(mResultedDownloading?.status){
            Status.SUCCESS -> {
                Utils.Log(TAG,"Downloaded completely files")
                progressing = EnumStepProgressing.EXPORTING
                viewModel.isLoading.postValue(true)
                val mResultedExporting = ServiceManager.getInstance()?.exportingItems(mData,isSharingFiles)
                when(mResultedExporting?.status){
                    Status.SUCCESS -> {
                        Utils.Log(TAG,"Completed exported files")
                        if (isSharingFiles){
                            Utils.shareMultiple(mResultedExporting.data!!,this@exportingFiles)
                        }else{
                            Utils.onBasicAlertNotify(this@exportingFiles, getString(R.string.key_alert), "Exported at " + SuperSafeApplication.getInstance().getSuperSafePicture())
                            deleteItems(true)
                        }
                    }else ->Utils.Log(TAG,"Exported files has issued")
                }
                progressing = EnumStepProgressing.NONE
                viewModel.isLoading.postValue(false)
            }else -> {
            Utils.Log(TAG,"Downloaded files has issued")
            progressing = EnumStepProgressing.NONE
            viewModel.isLoading.postValue(false)
        }
        }
    }else{
        progressing = EnumStepProgressing.EXPORTING
        viewModel.isLoading.postValue(true)
        val mResultedExporting = ServiceManager.getInstance()?.exportingItems(mData,isSharingFiles)
        when(mResultedExporting?.status){
            Status.SUCCESS -> {
                Utils.Log(TAG,"Completed exported files")
                if (isSharingFiles){
                    Utils.shareMultiple(mResultedExporting.data!!,this@exportingFiles)
                }else{
                    Utils.onBasicAlertNotify(this@exportingFiles, getString(R.string.key_alert), "Exported at " + SuperSafeApplication.getInstance().getSuperSafePicture())
                    deleteItems(true)
                }
            }else ->Utils.Log(TAG,"Exported files has issued")
        }
        progressing = EnumStepProgressing.NONE
        viewModel.isLoading.postValue(false)
    }
}





