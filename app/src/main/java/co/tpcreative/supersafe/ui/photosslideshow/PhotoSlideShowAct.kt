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
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import cn.pedant.SweetAlert.SweetAlertDialog
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseGalleryActivity
import co.tpcreative.supersafe.common.controller.SingletonFakePinComponent
import co.tpcreative.supersafe.common.controller.SingletonPrivateFragment
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Configuration
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.*
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.github.chrisbanes.photoview.OnPhotoTapListener
import com.github.chrisbanes.photoview.PhotoView
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_photos_slideshow.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.util.*

class PhotoSlideShowAct : BaseGalleryActivity(), View.OnClickListener, BaseView<EmptyModel> {
    private val options: RequestOptions = RequestOptions()
            .centerInside()
            .placeholder(R.color.black38)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .error(R.drawable.baseline_music_note_white_48)
            .priority(Priority.HIGH)
    var isHide = false
    var presenter: PhotoSlideShowPresenter? = null
    private var adapter: SamplePagerAdapter? = null
    var isReload = false
    var dialog: AlertDialog? = null
    var subscriptions: Disposable? = null
    var isProgressing = false
    var position = 0
    private var photoView: PhotoView? = null
    var mDialogProgress: SweetAlertDialog? = null
    var handler: Handler? = null
    val delay = 2000 //milliseconds
    private var page = 0
    var runnable: Runnable = object : Runnable {
        override fun run() {
            if (adapter?.getCount() == page) {
                page = 0
            } else {
                page++
            }
            view_pager?.setCurrentItem(page, true)
            handler?.postDelayed(this, delay.toLong())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photos_slideshow)
        initUI()
        view_pager?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                page = position
            }
            override fun onPageScrollStateChanged(state: Int) {}
        })
        attachFragment(R.id.gallery_root)
        adapter = SamplePagerAdapter(this)
        view_pager?.adapter = adapter
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
                            runOnUiThread(Runnable { Toast.makeText(this@PhotoSlideShowAct, "Exported at " + SuperSafeApplication.Companion.getInstance().getSuperSafePicture(), Toast.LENGTH_LONG).show() })
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

    override fun onStopListenerAWhile() {
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
                    ?.setDialogHeight(Configuration.DIALOG_HALF)
                    ?.setDialogMode(Configuration.DIALOG_LIST)
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
                val items: ItemModel? = view_pager?.getCurrentItem()?.let { it1 -> presenter?.mList?.get(it1) }
                if (items != null) {
                    Navigator.onPlayer(this@PhotoSlideShowAct, items, presenter?.mainCategories!!)
                }
            })
            try {
                val path: String? = mItems.getThumbnail()
                val file = File("" + path)
                if (file.exists() || file.isFile) {
                    photoView?.setRotation(mItems.degrees.toFloat())
                    if (mItems.mimeType == getString(R.string.key_gif)) {
                        val mOriginal: String? = mItems.getOriginal()
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
                    imgPlayer.visibility = View.VISIBLE
                }
                EnumFormatType.AUDIO -> {
                    imgPlayer.visibility = View.VISIBLE
                }
                EnumFormatType.FILES -> {
                    imgPlayer.visibility = View.INVISIBLE
                }
                else -> {
                    imgPlayer.visibility = View.INVISIBLE
                }
            }
            container?.addView(myView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            photoView?.tag = "myview$position"
            return myView
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View?)
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object`
        }

        override fun getItemPosition(`object`: Any): Int {
            return POSITION_NONE
        }
    }

    override fun onClick(view: View?) {
        position = view_pager?.getCurrentItem()!!
        when (view?.id) {
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
                                storage?.createDirectory(SuperSafeApplication.getInstance().getSuperSafeShare())
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
                                storage?.createDirectory(SuperSafeApplication.getInstance().getSuperSafePicture())
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
                        val items: ItemModel? = SQLHelper.getItemId(presenter?.mList?.get(view_pager?.currentItem!!)?.items_id, presenter?.mList?.get(view_pager?.currentItem!!)?.isFakePin!!)
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

    override fun onMoveAlbumSuccessful() {
        try {
            isReload = true
            presenter?.mList?.removeAt(position)
            adapter?.notifyDataSetChanged()
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
        return applicationContext
    }

    override fun onPause() {
        super.onPause()
        onStopSlider()
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
}