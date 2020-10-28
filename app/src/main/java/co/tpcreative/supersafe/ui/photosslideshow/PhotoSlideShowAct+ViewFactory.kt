package co.tpcreative.supersafe.ui.photosslideshow
import android.os.Handler
import android.os.Looper
import cn.pedant.SweetAlert.SweetAlertDialog
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.controller.PrefsController
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.*
import com.afollestad.materialdialogs.MaterialDialog
import com.snatik.storage.Storage
import dmax.dialog.SpotsDialog
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_photos_slideshow.*
import kotlinx.android.synthetic.main.footer_items.*
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.util.ArrayList

fun PhotoSlideShowAct.initUI(){
    storage = Storage(this)
    storage?.setEncryptConfiguration(SuperSafeApplication.getInstance().getConfigurationFile())
    presenter = PhotoSlideShowPresenter()
    presenter?.bindView(this)
    presenter?.getIntent(this)
    imgArrowBack?.setOnClickListener(this)
    imgOverflow?.setOnClickListener(this)
    imgDelete?.setOnClickListener(this)
    imgExport?.setOnClickListener(this)
    imgRotate?.setOnClickListener(this)
    imgShare?.setOnClickListener(this)
    imgMove?.setOnClickListener(this)
    /*Auto slide*/
    handler = Handler(Looper.getMainLooper())
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

fun PhotoSlideShowAct.onShowDialog(status: EnumStatus?, position: Int) {
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
    }
    val builder: MaterialDialog = MaterialDialog(this)
            .title(text = getString(R.string.confirm))
            .message(text = content)
            .negativeButton(text = getString(R.string.cancel))
            .positiveButton(text = getString(R.string.ok))
            .negativeButton {
                val items: ItemModel? = presenter?.mList?.get(position)
                val isSaver: Boolean = PrefsController.getBoolean(getString(R.string.key_saving_space), false)
                when (EnumFormatType.values()[items?.formatType!!]) {
                    EnumFormatType.IMAGE -> {
                        items?.isSaver = isSaver
                        SQLHelper.updatedItem(items)
                        if (isSaver) {
                            storage?.deleteFile(items.originalPath)
                        }
                    }
                }
            }
            .positiveButton {
                val mListExporting: MutableList<ExportFiles> = ArrayList<ExportFiles>()
                when (status) {
                    EnumStatus.SHARE -> {
                        Utils.onPushEventBus(EnumStatus.START_PROGRESS)
                        presenter?.mListShare?.clear()
                        val index: ItemModel? = presenter?.mList?.get(position)
                        if (index != null) {
                            val formatType = EnumFormatType.values()[index.formatType]
                            when (formatType) {
                                EnumFormatType.AUDIO -> {
                                    val input = File(index.originalPath)
                                    var output: File? = File(SuperSafeApplication.getInstance().getSupersafeShare() + index.originalName + index.fileExtension)
                                    if (storage?.isFileExist(output?.getAbsolutePath())!!) {
                                        output = File(SuperSafeApplication.getInstance().getSupersafeShare() + index.originalName + "(1)" + index.fileExtension)
                                    }
                                    if (storage?.isFileExist(input.absolutePath)!!) {
                                        if (output != null) {
                                            presenter?.mListShare?.add(output)
                                        }
                                        val exportFiles = ExportFiles(input, output, 0, false, index.formatType)
                                        mListExporting.add(exportFiles)
                                    }
                                }
                                EnumFormatType.FILES -> {
                                    val input = File(index.originalPath)
                                    var output: File? = File(SuperSafeApplication.getInstance().getSupersafeShare() + index.originalName + index.fileExtension)
                                    if (storage?.isFileExist(output?.getAbsolutePath())!!) {
                                        output = File(SuperSafeApplication.getInstance().getSupersafeShare() + index.originalName + "(1)" + index.fileExtension)
                                    }
                                    if (storage?.isFileExist(input.absolutePath)!!) {
                                        presenter?.mListShare?.add(output!!)
                                        val exportFiles = ExportFiles(input, output, 0, false, index.formatType)
                                        mListExporting.add(exportFiles)
                                    }
                                }
                                EnumFormatType.VIDEO -> {
                                    val input = File(index.originalPath)
                                    var output: File? = File(SuperSafeApplication.getInstance().getSupersafeShare() + index.originalName + index.fileExtension)
                                    if (storage?.isFileExist(output?.getAbsolutePath())!!) {
                                        output = File(SuperSafeApplication.getInstance().getSupersafeShare() + index.originalName + "(1)" + index.fileExtension)
                                    }
                                    if (storage?.isFileExist(input.absolutePath)!!) {
                                        presenter?.mListShare?.add(output!!)
                                        val exportFiles = ExportFiles(input, output, 0, false, index.formatType)
                                        mListExporting.add(exportFiles)
                                    }
                                }
                                else -> {
                                    var path = ""
                                    path = if (index.mimeType == getString(R.string.key_gif)) ({
                                        index.originalPath
                                    }).toString() else ({
                                        index.thumbnailPath
                                    }).toString()
                                    val input = File(path)
                                    var output: File? = File(SuperSafeApplication.getInstance().getSupersafeShare() + index.originalName + index.fileExtension)
                                    if (storage?.isFileExist(output?.getAbsolutePath())!!) {
                                        output = File(SuperSafeApplication.getInstance().getSupersafeShare() + index.originalName + "(1)" + index.fileExtension)
                                    }
                                    if (storage?.isFileExist(input.absolutePath)!!) {
                                        presenter?.mListShare?.add(output!!)
                                        val exportFiles = ExportFiles(input, output, 0, false, index.formatType)
                                        mListExporting.add(exportFiles)
                                    }
                                }
                            }
                        }
                        onStartProgressing()
                        ServiceManager.getInstance()?.setmListExport(mListExporting)
                        ServiceManager.getInstance()?.onExportingFiles()
                    }
                    EnumStatus.EXPORT -> {
                        Utils.onPushEventBus(EnumStatus.START_PROGRESS)
                        presenter?.mListShare?.clear()
                        val index: ItemModel? = presenter?.mList?.get(position)
                        if (index != null) {
                            val formatType = EnumFormatType.values()[index.formatType]
                            when (formatType) {
                                EnumFormatType.AUDIO -> {
                                    val input = File(index.originalPath)
                                    var output: File? = File(SuperSafeApplication.getInstance().getSupersafePicture() + index.title)
                                    if (storage?.isFileExist(output?.getAbsolutePath())!!) {
                                        output = File(SuperSafeApplication.getInstance().getSupersafePicture() + index.originalName + "(1)" + index.fileExtension)
                                    }
                                    if (storage?.isFileExist(input.absolutePath)!!) {
                                        presenter?.mListShare?.add(output!!)
                                        val exportFiles = ExportFiles(input, output, 0, false, index.formatType)
                                        mListExporting.add(exportFiles)
                                    }
                                }
                                EnumFormatType.FILES -> {
                                    val input = File(index.originalPath)
                                    var output: File? = File(SuperSafeApplication.getInstance().getSupersafePicture() + index.title)
                                    if (storage?.isFileExist(output?.getAbsolutePath())!!) {
                                        output = File(SuperSafeApplication.getInstance().getSupersafePicture() + index.originalName + "(1)" + index.fileExtension)
                                    }
                                    if (storage?.isFileExist(input.absolutePath)!!) {
                                        presenter?.mListShare?.add(output!!)
                                        val exportFiles = ExportFiles(input, output, 0, false, index.formatType)
                                        mListExporting.add(exportFiles)
                                    }
                                }
                                EnumFormatType.VIDEO -> {
                                    val input = File(index.originalPath)
                                    var output: File? = File(SuperSafeApplication.getInstance().getSupersafePicture() + index.title)
                                    if (storage?.isFileExist(output?.getAbsolutePath())!!) {
                                        output = File(SuperSafeApplication.getInstance().getSupersafePicture() + index.originalName + "(1)" + index.fileExtension)
                                    }
                                    if (storage?.isFileExist(input.absolutePath)!!) {
                                        presenter?.mListShare?.add(output!!)
                                        val exportFiles = ExportFiles(input, output, 0, false, index.formatType)
                                        mListExporting.add(exportFiles)
                                    }
                                }
                                else -> {
                                    val input = File(index.originalPath)
                                    var output: File? = File(SuperSafeApplication.getInstance().getSupersafePicture() + index.title)
                                    if (storage?.isFileExist(output?.getAbsolutePath())!!) {
                                        output = File(SuperSafeApplication.getInstance().getSupersafePicture() + index.originalName + "(1)" + index.fileExtension)
                                    }
                                    if (storage?.isFileExist(input.absolutePath)!!) {
                                        presenter?.mListShare?.add(output!!)
                                        val exportFiles = ExportFiles(input, output, 0, false, index.formatType)
                                        mListExporting.add(exportFiles)
                                    }
                                }
                            }
                        }
                        onStartProgressing()
                        ServiceManager.getInstance()?.setmListExport(mListExporting)
                        ServiceManager.getInstance()?.onExportingFiles()
                    }
                    EnumStatus.DELETE -> {
                        presenter?.onDelete(position)
                        isReload = true
                    }
                }
            }
    builder.show()
}

/*Gallery interface*/
fun PhotoSlideShowAct.onStartProgressing() {
    try {
        runOnUiThread(Runnable {
            if (dialog == null) {
                dialog = SpotsDialog.Builder()
                        .setContext(this)
                        .setMessage(getString(R.string.exporting))
                        .setCancelable(true)
                        .build()
            }
            if (!dialog?.isShowing!!) {
                dialog?.show()
                Utils.Log(TAG, "Showing dialog...")
            }
        })
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun PhotoSlideShowAct.onStopProgressing() {
    try {
        runOnUiThread(Runnable {
            if (dialog != null) {
                dialog?.dismiss()
                deselectAll()
                Utils.Log(TAG, "Action 1")
            }
        })
    } catch (e: Exception) {
        Utils.Log(TAG, e.message+"")
    }
}


fun PhotoSlideShowAct.deselectAll() {
    when (presenter?.status) {
        EnumStatus.EXPORT -> {
            Utils.Log(TAG, "Action 2")
            presenter?.mList?.get(position)?.isExport = true
            presenter?.mList?.get(position)?.isDeleteLocal = true
            presenter?.mList?.get(position)?.let { SQLHelper.updatedItem(it) }
            onCheckDelete()
        }
    }
}

fun PhotoSlideShowAct.onCheckDelete() {
    val mList: MutableList<ItemModel> = presenter?.mList!!
    Utils.Log(TAG, "Action 3")
    val formatTypeFile = EnumFormatType.values()[mList[position].formatType]
    if (formatTypeFile == EnumFormatType.AUDIO && mList[position].global_original_id == null) {
        SQLHelper.deleteItem(mList[position])
    } else if (formatTypeFile == EnumFormatType.FILES && mList[position].global_original_id == null) {
        SQLHelper.deleteItem(mList[position])
    } else if ((mList[position].global_original_id == null) and (mList[position].global_thumbnail_id == null)) {
        SQLHelper.deleteItem(mList[position])
    } else {
        mList[position].deleteAction = EnumDelete.DELETE_WAITING.ordinal
        SQLHelper.updatedItem(mList[position])
        Utils.Log(TAG, "ServiceManager waiting for delete")
    }
    storage?.deleteDirectory(SuperSafeApplication.getInstance().getSupersafePrivate() + mList[position].items_id)
    presenter?.onDelete(position)
    isReload = true
    Utils.Log(TAG, "Action 4")
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

fun PhotoSlideShowAct.onDialogDownloadFile() {
    mDialogProgress = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
            .setTitleText(getString(R.string.downloading))
    mDialogProgress?.show()
    mDialogProgress?.setCancelable(false)
}

fun PhotoSlideShowAct.onRotateBitmap(items: ItemModel?) {
    subscriptions = Observable.create<Any?> { subscriber: ObservableEmitter<Any?>? ->
        isProgressing = true
        Utils.Log(TAG, "Start Progressing encrypt thumbnail data")
        val mItem: ItemModel? = items
        var mDegrees: Int = mItem?.degrees!!
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
        mItem.degrees = valueDegrees
        presenter?.mList?.get(position)?.degrees = valueDegrees
        subscriber?.onNext(mItem)
        subscriber?.onComplete()
    }
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .observeOn(Schedulers.io())
            .subscribe { response: Any? ->
                val mItem: ItemModel? = response as ItemModel?
                if (mItem != null) {
                    SQLHelper.updatedItem(mItem)
                    runOnUiThread(Runnable {
                        isProgressing = false
                        view_pager?.getAdapter()?.notifyDataSetChanged()
                    })
                    Utils.Log(TAG, "Thumbnail saved successful")
                } else {
                    Utils.Log(TAG, "Thumbnail saved failed")
                }
            }
}

/*Download file*/
fun PhotoSlideShowAct.onEnableSyncData(position: Int) {
    val mUser: User? = Utils.getUserInfo()
    if (mUser != null) {
        if (mUser.verified) {
            if (!mUser.driveConnected) {
                Navigator.onCheckSystem(this, null)
            } else {
                onDialogDownloadFile()
                val list: MutableList<ItemModel> = ArrayList<ItemModel>()
                val items: ItemModel? = presenter?.mList?.get(position)
                items?.isChecked = true
                list.add(items!!)
                ServiceManager.getInstance()?.onPreparingEnableDownloadData(list)
            }
        } else {
            Navigator.onVerifyAccount(this)
        }
    }
}




