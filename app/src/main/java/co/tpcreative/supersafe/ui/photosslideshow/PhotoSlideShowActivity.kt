package co.tpcreative.supersafe.ui.photosslideshow
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.widget.*
import androidx.appcompat.widget.AppCompatImageView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import butterknife.BindView
import cn.pedant.SweetAlert.SweetAlertDialog
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseGalleryActivity
import co.tpcreative.supersafe.common.controller.PrefsController
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.controller.SingletonFakePinComponent
import co.tpcreative.supersafe.common.controller.SingletonPrivateFragment
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Configuration
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.*
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.github.chrisbanes.photoview.OnPhotoTapListener
import com.github.chrisbanes.photoview.PhotoView
import com.snatik.storage.Storage
import dmax.dialog.SpotsDialog
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.util.*

class PhotoSlideShowActivity : BaseGalleryActivity(), View.OnClickListener, BaseView<EmptyModel> {
    private val options: RequestOptions = RequestOptions()
            .centerInside()
            .placeholder(R.color.black38)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .error(R.drawable.baseline_music_note_white_48)
            .priority(Priority.HIGH)

    @BindView(R.id.rlTop)
    var rlTop: RelativeLayout? = null

    @BindView(R.id.llBottom)
    var llBottom: LinearLayout? = null

    @BindView(R.id.imgArrowBack)
    var imgArrowBack: AppCompatImageView? = null

    @BindView(R.id.imgOverflow)
    var imgOverflow: AppCompatImageView? = null

    @BindView(R.id.imgShare)
    var imgShare: AppCompatImageView? = null

    @BindView(R.id.imgExport)
    var imgExport: AppCompatImageView? = null

    @BindView(R.id.imgMove)
    var imgMove: AppCompatImageView? = null

    @BindView(R.id.imgRotate)
    var imgRotate: AppCompatImageView? = null

    @BindView(R.id.imgDelete)
    var imgDelete: AppCompatImageView? = null
    private var isHide = false
    private var presenter: PhotoSlideShowPresenter? = null
    private var viewPager: ViewPager? = null
    private var adapter: SamplePagerAdapter? = null
    private var isReload = false
    private var dialog: AlertDialog? = null
    private var subscriptions: Disposable? = null
    private var isProgressing = false
    private var position = 0
    private var photoView: PhotoView? = null
    var mDialogProgress: SweetAlertDialog? = null
    private var handler: Handler? = null
    private val delay = 2000 //milliseconds
    private var page = 0
    var runnable: Runnable = object : Runnable {
        override fun run() {
            if (adapter?.getCount() == page) {
                page = 0
            } else {
                page++
            }
            viewPager?.setCurrentItem(page, true)
            handler?.postDelayed(this, delay.toLong())
        }
    }

    protected override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photos_slideshow)
        storage = Storage(this)
        storage?.setEncryptConfiguration(SuperSafeApplication.getInstance().getConfigurationFile())
        presenter = PhotoSlideShowPresenter()
        presenter?.bindView(this)
        presenter?.getIntent(this)
        viewPager = findViewById<ViewPager?>(R.id.view_pager)
        adapter = SamplePagerAdapter(this)
        viewPager?.setAdapter(adapter)
        imgArrowBack?.setOnClickListener(this)
        imgOverflow?.setOnClickListener(this)
        imgDelete?.setOnClickListener(this)
        imgExport?.setOnClickListener(this)
        imgRotate?.setOnClickListener(this)
        imgShare?.setOnClickListener(this)
        imgMove?.setOnClickListener(this)
        attachFragment(R.id.gallery_root)
        /*Auto slide*/handler = Handler()
        viewPager?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                page = position
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    fun onStartSlider() {
        try {
            handler?.postDelayed(runnable, delay.toLong())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onStopSlider() {
        try {
            handler?.removeCallbacks(runnable)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: EnumStatus?) {
        when (event) {
            EnumStatus.FINISH -> {
                Navigator.onMoveToFaceDown(this)
            }
            EnumStatus.START_PROGRESS -> {
                onStartProgressing()
            }
            EnumStatus.STOP_PROGRESS -> {
                try {
                    Utils.Log(TAG, "onStopProgress")
                    onStopProgressing()
                    when (presenter?.status) {
                        EnumStatus.SHARE -> {
                            if (presenter?.mListShare != null) {
                                if (presenter?.mListShare?.size!! > 0) {
                                    Utils.shareMultiple(presenter?.mListShare!!, this)
                                }
                            }
                        }
                        EnumStatus.EXPORT -> {
                            runOnUiThread(Runnable { Toast.makeText(this@PhotoSlideShowActivity, "Exported at " + SuperSafeApplication.Companion.getInstance().getSupersafePicture(), Toast.LENGTH_LONG).show() })
                        }
                    }
                } catch (e: Exception) {
                    Utils.Log(TAG, e.message+"")
                }
            }
            EnumStatus.DOWNLOAD_COMPLETED -> {
                mDialogProgress?.setTitleText("Success!")
                        ?.setConfirmText("OK")
                        ?.changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
                mDialogProgress?.setConfirmClickListener { sweetAlertDialog ->
                    sweetAlertDialog?.dismiss()
                    onShowDialog(EnumStatus.EXPORT, position)
                }
                Utils.Log(TAG, " already sync")
            }
            EnumStatus.DOWNLOAD_FAILED -> {
                mDialogProgress?.setTitleText("No connection, Try again")
                        ?.setConfirmText("OK")
                        ?.changeAlertType(SweetAlertDialog.ERROR_TYPE)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        onRegisterHomeWatcher()
    }

    override fun onDestroy() {
        super.onDestroy()
        Utils.Log(TAG, "OnDestroy")
        EventBus.getDefault().unregister(this)
        presenter?.unbindView()
        onStopSlider()
        Utils.Log(TAG, "Destroy")
        if (subscriptions != null) {
            subscriptions?.dispose()
        }
        try {
            storage?.deleteFile(Utils.getPackagePath(getApplicationContext()).getAbsolutePath())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    protected override fun onStopListenerAWhile() {
        EventBus.getDefault().unregister(this)
    }

    override fun onOrientationChange(isFaceDown: Boolean) {
        onFaceDown(isFaceDown)
    }

    /*BaseGallery*/
    override fun getConfiguration(): Configuration? {
        //default configuration
        try {
            return Configuration.Builder()
                    .hasCamera(true)
                    ?.hasShade(true)
                    ?.hasPreview(true)
                    ?.setSpaceSize(4)
                    ?.setPhotoMaxWidth(120)
                    ?.setLocalCategoriesId(presenter?.mainCategories?.categories_local_id)
                    ?.setCheckBoxColor(-0xc0ae4b)
                    ?.setDialogHeight(Configuration.Companion.DIALOG_HALF)
                    ?.setDialogMode(Configuration.Companion.DIALOG_LIST)
                    ?.setMaximum(9)
                    ?.setTip(null)
                    ?.setAblumsTitle(null)
                    ?.build()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    override fun getListItems(): MutableList<ItemModel>? {
        try {
            val list: MutableList<ItemModel> = ArrayList<ItemModel>()
            val item: ItemModel? = presenter?.mList?.get(position)
            if (item != null) {
                item.isChecked = true
                list.add(item)
                return list
            }
            onBackPressed()
            return null
        } catch (e: Exception) {
            onBackPressed()
        }
        return null
    }

    internal inner class SamplePagerAdapter(private val context: Context?) : PagerAdapter() {
        override fun getCount(): Int {
            return presenter?.mList?.size!!
        }
        override fun instantiateItem(container: ViewGroup, position: Int): View {
            //PhotoView photoView = new PhotoView(container.getContext());
            val inflater: LayoutInflater = getLayoutInflater()
            val myView: View = inflater.inflate(R.layout.content_view, null)
            photoView = myView.findViewById(R.id.imgPhoto)
            val imgPlayer = myView.findViewById<ImageView?>(R.id.imgPlayer)
            val mItems: ItemModel? = presenter?.mList?.get(position)
            val enumTypeFile = EnumFormatType.values()[mItems!!.formatType]
            photoView?.setOnPhotoTapListener(object : OnPhotoTapListener {
                override fun onPhotoTap(view: ImageView?, x: Float, y: Float) {
                    Utils.Log(TAG, "on Clicked")
                    onStopSlider()
                    isHide = !isHide
                    onHideView()
                }
            })
            imgPlayer.setOnClickListener(View.OnClickListener {
                val items: ItemModel? = viewPager?.getCurrentItem()?.let { it1 -> presenter?.mList?.get(it1) }
                if (items != null) {
                    Navigator.onPlayer(this@PhotoSlideShowActivity, items, presenter?.mainCategories!!)
                }
            })
            try {
                val path: String? = mItems.thumbnailPath
                val file = File("" + path)
                if (file.exists() || file.isFile) {
                    photoView?.setRotation(mItems.degrees.toFloat())
                    if (mItems.mimeType == getString(R.string.key_gif)) {
                        val mOriginal: String? = mItems.originalPath
                        val mFileOriginal = File("" + mOriginal)
                        if (mFileOriginal.exists() || mFileOriginal.isFile) {
                            Glide.with(context!!)
                                    .asGif()
                                    .load(storage?.readFile(mOriginal))
                                    .apply(options)
                                    .into(photoView!!)
                        }
                    } else {
                        Glide.with(context!!)
                                .load(storage?.readFile(path))
                                .apply(options)
                                .into(photoView!!)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            when (enumTypeFile) {
                EnumFormatType.VIDEO -> {
                    imgPlayer.setVisibility(View.VISIBLE)
                }
                EnumFormatType.AUDIO -> {
                    imgPlayer.setVisibility(View.VISIBLE)
                }
                EnumFormatType.FILES -> {
                    imgPlayer.setVisibility(View.INVISIBLE)
                }
                else -> {
                    imgPlayer.setVisibility(View.INVISIBLE)
                }
            }
            container?.addView(myView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            photoView?.setTag("myview$position")
            return myView
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View?)
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object`
        }

        override fun getItemPosition(`object`: Any): Int {
            return PagerAdapter.POSITION_NONE
        }
    }

    fun onHideView() {
        if (isHide) {
            Utils.slideToTopHeader(rlTop!!)
            Utils.slideToBottomFooter(llBottom!!)
        } else {
            Utils.slideToBottomHeader(rlTop!!)
            Utils.slideToTopFooter(llBottom!!)
        }
    }

    override fun onClick(view: View?) {
        position = viewPager?.getCurrentItem()!!
        when (view?.getId()) {
            R.id.imgArrowBack -> {
                if (!isHide){
                    onBackPressed()
                }
            }
            R.id.imgOverflow -> {
                openOptionMenu(view)
            }
            R.id.imgShare -> {
                if (!isHide) {
                    try {
                        if (presenter?.mList != null) {
                            if (presenter?.mList?.size!! > 0) {
                                storage?.createDirectory(SuperSafeApplication.getInstance().getSupersafeShare())
                                presenter?.status = EnumStatus.SHARE
                                onShowDialog(EnumStatus.SHARE, position)
                            } else {
                                onBackPressed()
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            R.id.imgDelete -> {
                if (!isHide) {
                    try {
                        if (presenter?.mList != null) {
                            if (presenter?.mList?.size!! > 0) {
                                presenter?.status = EnumStatus.DELETE
                                onShowDialog(EnumStatus.DELETE, position)
                            } else {
                                onBackPressed()
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            R.id.imgExport -> {
                if (!isHide) {
                    Utils.Log(TAG, "Action here")
                    try {
                        if (presenter?.mList != null) {
                            if (presenter?.mList?.size!! > 0) {
                                storage?.createDirectory(SuperSafeApplication.getInstance().getSupersafePicture())
                                presenter?.status = EnumStatus.EXPORT
                                if (presenter?.mList?.get(position)?.isSaver!!) {
                                    onEnableSyncData(position)
                                } else {
                                    onShowDialog(EnumStatus.EXPORT, position)
                                }
                            } else {
                                onBackPressed()
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            R.id.imgRotate -> {
                if (!isHide) {
                    try {
                        if (isProgressing) {
                            return
                        }
                        val items: ItemModel? = SQLHelper.getItemId(presenter?.mList?.get(viewPager?.getCurrentItem()!!)?.items_id, presenter?.mList?.get(viewPager?.getCurrentItem()!!)?.isFakePin!!)
                        val formatTypeFile = EnumFormatType.values()[items?.formatType!!]
                        if (formatTypeFile != EnumFormatType.AUDIO && formatTypeFile != EnumFormatType.FILES) {
                            onRotateBitmap(items)
                            isReload = true
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            R.id.imgMove -> {
                presenter?.status = EnumStatus.MOVE
                openAlbum()
            }
        }
    }

    /*Download file*/
    fun onEnableSyncData(position: Int) {
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

    fun onDialogDownloadFile() {
        mDialogProgress = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
                .setTitleText(getString(R.string.downloading))
        mDialogProgress?.show()
        mDialogProgress?.setCancelable(false)
    }

    override fun onMoveAlbumSuccessful() {
        try {
            isReload = true
            presenter?.mList?.removeAt(position)
            adapter?.notifyDataSetChanged()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onShowDialog(status: EnumStatus?, position: Int) {
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
        val builder: MaterialDialog.Builder = MaterialDialog.Builder(this)
                .title(getString(R.string.confirm))
                .theme(Theme.LIGHT)
                .content(content)
                .titleColor(getResources().getColor(R.color.black))
                .negativeText(getString(R.string.cancel))
                .positiveText(getString(R.string.ok))
                .onNegative(object : MaterialDialog.SingleButtonCallback {
                    override fun onClick(dialog: MaterialDialog, which: DialogAction) {
                        val items: ItemModel? = presenter?.mList?.get(position)
                        val isSaver: Boolean = PrefsController.getBoolean(getString(R.string.key_saving_space), false)
                        val formatType = EnumFormatType.values()[items?.formatType!!]
                        when (formatType) {
                            EnumFormatType.IMAGE -> {
                                items?.isSaver = isSaver
                                SQLHelper.updatedItem(items)
                                if (isSaver) {
                                    storage?.deleteFile(items.originalPath)
                                }
                            }
                        }
                    }
                })
                .onPositive(object : MaterialDialog.SingleButtonCallback {
                    override fun onClick(dialog: MaterialDialog, which: DialogAction) {
                        val mListExporting: MutableList<ExportFiles> = ArrayList<ExportFiles>()
                        when (status) {
                            EnumStatus.SHARE -> {
                                EventBus.getDefault().post(EnumStatus.START_PROGRESS)
                                presenter?.mListShare?.clear()
                                val index: ItemModel? = presenter?.mList?.get(position)
                                if (index != null) {
                                    val formatType = EnumFormatType.values()[index.formatType]
                                    when (formatType) {
                                        EnumFormatType.AUDIO -> {
                                            val input = File(index.originalPath)
                                            var output: File? = File(SuperSafeApplication.Companion.getInstance().getSupersafeShare() + index.originalName + index.fileExtension)
                                            if (storage?.isFileExist(output?.getAbsolutePath())!!) {
                                                output = File(SuperSafeApplication.Companion.getInstance().getSupersafeShare() + index.originalName + "(1)" + index.fileExtension)
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
                                                output = File(SuperSafeApplication.Companion.getInstance().getSupersafeShare() + index.originalName + "(1)" + index.fileExtension)
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
                                EventBus.getDefault().post(EnumStatus.START_PROGRESS)
                                presenter?.mListShare?.clear()
                                val index: ItemModel? = presenter?.mList?.get(position)
                                if (index != null) {
                                    val formatType = EnumFormatType.values()[index.formatType]
                                    when (formatType) {
                                        EnumFormatType.AUDIO -> {
                                            val input = File(index.originalPath)
                                            var output: File? = File(SuperSafeApplication.Companion.getInstance().getSupersafePicture() + index.title)
                                            if (storage?.isFileExist(output?.getAbsolutePath())!!) {
                                                output = File(SuperSafeApplication.Companion.getInstance().getSupersafePicture() + index.originalName + "(1)" + index.fileExtension)
                                            }
                                            if (storage?.isFileExist(input.absolutePath)!!) {
                                                presenter?.mListShare?.add(output!!)
                                                val exportFiles = ExportFiles(input, output, 0, false, index.formatType)
                                                mListExporting.add(exportFiles)
                                            }
                                        }
                                        EnumFormatType.FILES -> {
                                            val input = File(index.originalPath)
                                            var output: File? = File(SuperSafeApplication.Companion.getInstance().getSupersafePicture() + index.title)
                                            if (storage?.isFileExist(output?.getAbsolutePath())!!) {
                                                output = File(SuperSafeApplication.Companion.getInstance().getSupersafePicture() + index.originalName + "(1)" + index.fileExtension)
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
                })
        builder.show()
    }

    /*Gallery interface*/
    private fun onStartProgressing() {
        try {
            runOnUiThread(Runnable {
                if (dialog == null) {
                    dialog = SpotsDialog.Builder()
                            .setContext(this@PhotoSlideShowActivity)
                            .setMessage(getString(R.string.exporting))
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

    private fun deselectAll() {
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

    fun onCheckDelete() {
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

    @SuppressLint("RestrictedApi")
    fun openOptionMenu(v: View?) {
        onStopSlider()
        val popup = PopupMenu(this, v)
        popup.menuInflater.inflate(R.menu.menu_slideshow, popup.menu)
        popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_slideshow -> {
                    Utils.Log(TAG, "Slide show images")
                    onStartSlider()
                    isHide = true
                    onHideView()
                    return@OnMenuItemClickListener true
                }
            }
            true
        })
        popup.show()
    }

    /*ViewPresenter*/
    override fun onStartLoading(status: EnumStatus) {}
    override fun onStopLoading(status: EnumStatus) {}
    override fun onBackPressed() {
        if (isReload) {
            SingletonPrivateFragment.getInstance()?.onUpdateView()
            SingletonFakePinComponent.getInstance().onUpdateView()
            val intent = Intent()
            setResult(Activity.RESULT_OK, intent)
        }
        super.onBackPressed()
    }

    override fun getContext(): Context? {
        return getApplicationContext()
    }

    protected override fun onPause() {
        super.onPause()
        onStopSlider()
    }

    fun onRotateBitmap(items: ItemModel?) {
        subscriptions = Observable.create<Any?>(ObservableOnSubscribe<Any?> { subscriber: ObservableEmitter<Any?>? ->
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
        })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .subscribe { response: Any? ->
                    val mItem: ItemModel? = response as ItemModel?
                    if (mItem != null) {
                        SQLHelper.updatedItem(mItem)
                        runOnUiThread(Runnable {
                            isProgressing = false
                            viewPager?.getAdapter()?.notifyDataSetChanged()
                        })
                        Utils.Log(TAG, "Thumbnail saved successful")
                    } else {
                        Utils.Log(TAG, "Thumbnail saved failed")
                    }
                }
    }

    override fun onError(message: String?, status: EnumStatus?) {}
    override fun onError(message: String?) {}
    override fun onSuccessful(message: String?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?) {
        when (status) {
            EnumStatus.DELETE -> {
                isReload = true
                adapter?.notifyDataSetChanged()
                if (presenter?.mList?.size == 0) {
                    onBackPressed()
                }
            }
        }
    }

    override fun getActivity(): Activity? {
        return this
    }

    override fun onSuccessful(message: String?, status: EnumStatus?, `object`: EmptyModel?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?, list: MutableList<EmptyModel>?) {}

    companion object {
        private val TAG = PhotoSlideShowActivity::class.java.simpleName
    }
}