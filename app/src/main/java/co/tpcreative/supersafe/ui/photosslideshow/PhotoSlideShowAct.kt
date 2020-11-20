package co.tpcreative.supersafe.ui.photosslideshow
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseGalleryActivity
import co.tpcreative.supersafe.common.controller.SingletonFakePinComponent
import co.tpcreative.supersafe.common.controller.SingletonPrivateFragment
import co.tpcreative.supersafe.common.extension.deleteFile
import co.tpcreative.supersafe.common.extension.readFile
import co.tpcreative.supersafe.common.helper.EncryptDecryptFilesHelper
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Configuration
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.*
import co.tpcreative.supersafe.viewmodel.PhotoSlideShowViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.github.chrisbanes.photoview.PhotoView
import kotlinx.android.synthetic.main.activity_photos_slideshow.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.util.*

class PhotoSlideShowAct : BaseGalleryActivity(), View.OnClickListener {
    private val options: RequestOptions = RequestOptions()
            .centerInside()
            .placeholder(R.color.black38)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .error(R.drawable.baseline_music_note_white_48)
            .priority(Priority.HIGH)
    var isHide = false
    var adapter: SamplePagerAdapter? = null
    lateinit var dataSource : MutableList<ItemModel>
    var dialog: AlertDialog? = null
    var isProgressing = false
    var position = 0
    private var photoView: PhotoView? = null
    var handler: Handler? = null
    val delay = 2000 //milliseconds
    private var page = 0
    var runnable: Runnable = object : Runnable {
        override fun run() {
            if (adapter?.count == page) {
                page = 0
            } else {
                page++
            }
            view_pager?.setCurrentItem(page, true)
            handler?.postDelayed(this, delay.toLong())
        }
    }

    lateinit var viewModel : PhotoSlideShowViewModel
    var progressing : EnumStepProgressing = EnumStepProgressing.NONE
    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
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
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: EnumStatus?) {
        when (event) {
            EnumStatus.FINISH -> {
                Navigator.onMoveToFaceDown(this)
            }
            else -> Utils.Log(TAG, "Nothing")
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
        onStopSlider()
        Utils.Log(TAG, "Destroy")
        Utils.getPackagePath(applicationContext).absolutePath.deleteFile()
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
                    ?.setLocalCategoriesId(mainCategory.categories_local_id)
                    ?.setFakePIN(mainCategory.isFakePin)
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
            val item: ItemModel? = dataSource[position]
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

    override fun onBackPressed() {
        if (viewModel.isRequestSyncData) {
            SingletonPrivateFragment.getInstance()?.onUpdateView()
            SingletonFakePinComponent.getInstance().onUpdateView()
            val intent = Intent()
            setResult(RESULT_OK, intent)
        }
        super.onBackPressed()
    }

    inner class SamplePagerAdapter(private val context: Context?) : PagerAdapter() {
        override fun getCount(): Int {
            return dataSource.size
        }
        override fun instantiateItem(container: ViewGroup, position: Int): View {
            //PhotoView photoView = new PhotoView(container.getContext());
            val inflater: LayoutInflater = layoutInflater
            val myView: View = inflater.inflate(R.layout.content_view, null)
            photoView = myView.findViewById(R.id.imgPhoto)
            val imgPlayer = myView.findViewById<ImageView?>(R.id.imgPlayer)
            val mItems: ItemModel? = dataSource[position]
            val enumTypeFile = EnumFormatType.values()[mItems!!.formatType]
            photoView?.setOnPhotoTapListener { view, x, y ->
                Utils.Log(TAG, "on Clicked")
                onStopSlider()
                isHide = !isHide
                onHideView()
            }
            imgPlayer.setOnClickListener(View.OnClickListener {
                val items: ItemModel? = view_pager?.currentItem?.let { it1 -> dataSource[it1] }
                if (items != null) {
                    Navigator.onPlayer(this@PhotoSlideShowAct, items, mainCategory)
                }
            })
            try {
                val path: String? = mItems.getThumbnail()
                val file = File("" + path)
                if (file.exists() || file.isFile) {
                    photoView?.rotation = mItems.degrees.toFloat()
                    if (mItems.mimeType == getString(R.string.key_gif)) {
                        val mOriginal: String? = mItems.getOriginal()
                        val mFileOriginal = File("" + mOriginal)
                        if (mFileOriginal.exists() || mFileOriginal.isFile) {
                            Glide.with(context!!)
                                    .asGif()
                                    .load(mOriginal?.readFile())
                                    .apply(options)
                                    .into(photoView!!)
                        }
                    } else {
                        Glide.with(context!!)
                                .load(path?.readFile())
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
            container.addView(myView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
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
        position = view_pager?.currentItem!!
        when (view?.id) {
            R.id.imgArrowBack -> {
                if (!isHide) {
                    onBackPressed()
                }
            }
            R.id.imgOverflow -> {
                openOptionMenu(view)
            }
            R.id.imgShare -> {
                if (!isHide) {
                    if (!isHide) {
                        dataSource[position].isChecked = true
                        EncryptDecryptFilesHelper.getInstance()?.createDirectory(SuperSafeApplication.getInstance().getSuperSafePicture())
                        viewModel.getCheckedItems().observe(this, androidx.lifecycle.Observer {
                            onShowDialog(it, EnumStatus.SHARE, true)
                        })
                    }
                }
            }
            R.id.imgDelete -> {
                if (!isHide) {
                    dataSource[position].isChecked = true
                    EncryptDecryptFilesHelper.getInstance()?.createDirectory(SuperSafeApplication.getInstance().getSuperSafePicture())
                    viewModel.getCheckedItems().observe(this, androidx.lifecycle.Observer {
                        onShowDialog(it, EnumStatus.DELETE, false)
                    })
                }
            }
            R.id.imgExport -> {
                if (!isHide) {
                    dataSource[position].isChecked = true
                    EncryptDecryptFilesHelper.getInstance()?.createDirectory(SuperSafeApplication.getInstance().getSuperSafePicture())
                    viewModel.getCheckedItems().observe(this, androidx.lifecycle.Observer {
                        onShowDialog(it, EnumStatus.EXPORT, false)
                    })
                }
            }
            R.id.imgRotate -> {
                if (!isHide) {
                    val items: ItemModel? = SQLHelper.getItemId(dataSource[view_pager?.currentItem!!].items_id, dataSource[view_pager?.currentItem!!].isFakePin)
                    val formatTypeFile = EnumFormatType.values()[items?.formatType!!]
                    if (formatTypeFile != EnumFormatType.AUDIO && formatTypeFile != EnumFormatType.FILES) {
                       if (!isProgressing){
                           onRotateBitmap(items)
                           viewModel.isRequestSyncData = true
                       }
                    }
                }
            }
            R.id.imgMove -> {
                openAlbum()
            }
        }
    }

    override fun onMoveAlbumSuccessful() {
        try {
            dataSource.removeAt(position)
            adapter?.notifyDataSetChanged()
            if (dataSource.size==0){
                onBackPressed()
            }
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

    override fun onPause() {
        super.onPause()
        onStopSlider()
    }

    val mainCategory : MainCategoryModel
        get(){
            return viewModel.mainCategoryModel
        }
}