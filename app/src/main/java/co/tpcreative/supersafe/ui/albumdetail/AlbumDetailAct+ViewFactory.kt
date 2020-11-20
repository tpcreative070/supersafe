package co.tpcreative.supersafe.ui.albumdetail
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.controller.SingletonManagerProcessing
import co.tpcreative.supersafe.common.extension.readFile
import co.tpcreative.supersafe.common.helper.EncryptDecryptFilesHelper
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.network.Status
import co.tpcreative.supersafe.common.network.base.ViewModelFactory
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.common.views.*
import co.tpcreative.supersafe.model.*
import co.tpcreative.supersafe.viewmodel.AlbumDetailViewModel
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.Glide
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.leinardi.android.speeddial.SpeedDialView
import com.snatik.storage.Storage
import kotlinx.android.synthetic.main.activity_album_detail.*
import kotlinx.android.synthetic.main.activity_album_detail.backdrop
import kotlinx.android.synthetic.main.activity_album_detail.collapsing_toolbar
import kotlinx.android.synthetic.main.activity_album_detail.progress_bar
import kotlinx.android.synthetic.main.activity_album_detail.recyclerView
import kotlinx.android.synthetic.main.activity_album_detail.toolbar
import kotlinx.android.synthetic.main.activity_album_detail.tv_Audios
import kotlinx.android.synthetic.main.activity_album_detail.tv_Others
import kotlinx.android.synthetic.main.activity_album_detail.tv_Photos
import kotlinx.android.synthetic.main.activity_album_detail.tv_Videos
import kotlinx.android.synthetic.main.footer_items_detail_album.*
import kotlinx.coroutines.*
import java.util.*

fun AlbumDetailAct.initUI(){
    TAG = this::class.java.simpleName
    window.statusBarColor = Color.TRANSPARENT
    setupViewModel()
    storage = Storage(this)
    storage?.setEncryptConfiguration(SuperSafeApplication.getInstance().getConfigurationFile())
    initSpeedDial(true)
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    llBottom?.visibility = View.GONE
    /*Root Fragment*/
    imgShare.setOnClickListener {
        EncryptDecryptFilesHelper.getInstance()?.createDirectory(SuperSafeApplication.getInstance().getSuperSafeShare())
        viewModel.getCheckedItems().observe(this, Observer {
            onShowDialog(it,EnumStatus.SHARE,true)
        })
    }

    imgExport.setOnClickListener {
        EncryptDecryptFilesHelper.getInstance()?.createDirectory(SuperSafeApplication.getInstance().getSuperSafePicture())
        viewModel.getCheckedItems().observe(this, Observer {
            onShowDialog(it,EnumStatus.EXPORT,false)
        })
    }

    imgDelete.setOnClickListener {
       viewModel.getCheckedItems().observe(this, Observer {
           onShowDialog(it,EnumStatus.DELETE,false)
       })
    }

    imgMove.setOnClickListener {
        openAlbum()
        Utils.Log(TAG, "Moving...")
    }

    recyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            Utils.Log(TAG, "Scrolling change listener")
            if (actionMode != null) {
                speedDial?.visibility = View.INVISIBLE
            }
        }
    })

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
            if (it) {
                llToolbarInfo.visibility = View.INVISIBLE
                progress_bar.visibility = View.VISIBLE
            } else {
                llToolbarInfo.visibility = View.VISIBLE
                progress_bar.visibility = View.INVISIBLE
            }
        }
    })

    viewModel.photos.observe(this, Observer {
        tv_Photos.text = kotlin.String.format(getString(R.string.photos_default), "$it")
    })
    viewModel.videos.observe(this, Observer {
        tv_Videos.text = kotlin.String.format(getString(R.string.videos_default), "$it")
    })
    viewModel.audios.observe(this, Observer {
        tv_Audios.text = kotlin.String.format(getString(R.string.audios_default), "$it")
    })
    viewModel.others.observe(this, Observer {
        tv_Others.text = kotlin.String.format(getString(R.string.others_default), "$it")
    })

    viewModel.isSelectAll.observe(this, Observer {
        if (it) {
            selectItems()
        } else {
            deselectItems()
        }
    })

    viewModel.count.observe(this, Observer {
        Utils.Log(TAG,"Count $it")
        if (it == 0) {
            llBottom?.visibility = View.GONE
        } else {
            llBottom?.visibility = View.VISIBLE
        }
    })

    initRecycleView(layoutInflater)
    getData()
}

fun AlbumDetailAct.multipleDelete(){
    var i = 0
    while (i < dataSource.size) {
        if (dataSource[i].isChecked) adapter?.removeAt(i) else i++
    }
    viewModel.onCalculate()
    actionMode?.finish()
}

fun AlbumDetailAct.deselectItems(){
    var i = 0
    while (i < dataSource.size) {
        if (dataSource[i].isChecked){
            dataSource[i].isChecked = false
            adapter?.notifyItemChanged(i)
        }
        i++
    }
    countSelected = 0
    viewModel.count.postValue(countSelected)
    actionMode?.title = countSelected.toString() + " " + getString(R.string.selected)
}

fun AlbumDetailAct.selectItems(){
    var i = 0
    while (i < dataSource.size) {
        if (!dataSource[i].isChecked){
            dataSource[i].isChecked = true
            adapter?.notifyItemChanged(i)
        }
        i++
    }
    countSelected = i
    viewModel.count.postValue(countSelected)
    actionMode?.title = countSelected.toString() + " " + getString(R.string.selected)
}

fun AlbumDetailAct.initRecycleView(layoutInflater: LayoutInflater){
    try {
        if (Utils.isVertical()) {
            gridLayoutManager = NpaGridLayoutManager(this, AlbumDetailAdapter.SPAN_COUNT_ONE)
            adapter = AlbumDetailAdapter(gridLayoutManager, layoutInflater, applicationContext, this@initRecycleView)
            recyclerView?.layoutManager = gridLayoutManager
            recyclerView?.addListOfDecoration(this)
            recyclerView?.adapter = adapter
        } else {
            gridLayoutManager = NpaGridLayoutManager(this, AlbumDetailAdapter.SPAN_COUNT_THREE)
            adapter = AlbumDetailAdapter(gridLayoutManager, layoutInflater, applicationContext, this@initRecycleView)
            recyclerView?.layoutManager = gridLayoutManager
            recyclerView?.addGridOfDecoration(3,4)
            recyclerView?.adapter = adapter
        }
    } catch (e: Exception) {
       e.printStackTrace()
    }
}


fun AlbumDetailAct.getData(){
    viewModel.isLoading.postValue(true)
    viewModel.getData(this@getData).observe(this@getData, Observer {
        when (it.status) {
            Status.SUCCESS -> {
                CoroutineScope(Dispatchers.Main).launch {
                    val mBanner = async {
                        onBannerLoading()
                    }
                    val mResult = async {
                        Utils.Log(TAG, "Loading...")
                        adapter?.setDataSource(it.data)
                    }
                    mBanner.await()
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

private fun AlbumDetailAct.deleteItems(isExport : Boolean? = false){
    viewModel.deleteItems(isExport).observe(this, Observer {
        multipleDelete()
    })
}

fun AlbumDetailAct.switchLayout() {
    recyclerView.clearDecorations()
    if (gridLayoutManager?.spanCount == AlbumDetailAdapter.SPAN_COUNT_ONE) {
        gridLayoutManager?.spanCount = AlbumDetailAdapter.SPAN_COUNT_THREE
        recyclerView.addGridOfDecoration(3,4)
        Utils.setIsVertical(false)
    } else {
        gridLayoutManager?.spanCount = AlbumDetailAdapter.SPAN_COUNT_ONE
        recyclerView.addListOfDecoration(this)
        Utils.setIsVertical(true)
    }
    adapter?.notifyItemRangeChanged(0, adapter?.itemCount!!)
}

fun AlbumDetailAct.switchIcon() {
    if (gridLayoutManager?.spanCount == AlbumDetailAdapter.SPAN_COUNT_THREE) {
        mMenuItem?.icon = ContextCompat.getDrawable(this, R.drawable.baseline_view_comfy_white_48)
    } else {
        mMenuItem?.icon = ContextCompat.getDrawable(this, R.drawable.baseline_format_list_bulleted_white_48)
    }
}

fun AlbumDetailAct.toggleSelections(position: Int) {
    dataSource[position].isChecked = !(dataSource[position].isChecked)
    if (dataSource[position].isChecked) {
        countSelected++
    } else {
        countSelected--
    }
    viewModel.count.postValue(countSelected)
    adapter?.notifyItemChanged(position)
    speedDial.hide()
}

fun AlbumDetailAct.onShowDialog(mData : MutableList<ItemModel>,status: EnumStatus?,isSharingFiles: Boolean) {
    var content: String? = ""
    when (status) {
        EnumStatus.EXPORT -> {
            content = kotlin.String.format(getString(R.string.export_items), "" + countSelected)
        }
        EnumStatus.SHARE -> {
            content = kotlin.String.format(getString(R.string.share_items), "" + countSelected)
        }
        EnumStatus.DELETE -> {
            content = kotlin.String.format(getString(R.string.move_items_to_trash), "" + countSelected)
        }
        EnumStatus.MOVE -> {
        }
        else -> Utils.Log(TAG,"Nothing")
    }
    val builder: MaterialDialog = MaterialDialog(this)
            .title(text = getString(R.string.confirm))
            .message(text = content!!)
            .negativeButton(text = getString(R.string.cancel))
            .positiveButton(text = getString(R.string.ok))
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

fun AlbumDetailAct.exportingFiles(mData : MutableList<ItemModel>,isSharingFiles : Boolean) = CoroutineScope(Dispatchers.Main).launch{
    if(!Utils.isConnectedToGoogleDrive()){
        Utils.onBasicAlertNotify(this@exportingFiles,"Alert","Please connect Google drive first")
        return@launch
    }
    if (Utils.getSaverSpace() && isSharingFiles){
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

/*Init Floating View*/
fun AlbumDetailAct.initSpeedDial(addActionItems: Boolean) {
    val mThemeApp: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
    if (addActionItems) {
        var drawable: Drawable? = AppCompatResources.getDrawable(applicationContext, R.drawable.baseline_photo_camera_white_24)
        speedDial?.addActionItem(SpeedDialActionItem.Builder(R.id.fab_camera, drawable)
                .setFabBackgroundColor(ResourcesCompat.getColor(resources, mThemeApp?.getPrimaryColor()!!,
                        theme))
                .setLabel(getString(R.string.camera))
                .setFabImageTintColor(ContextCompat.getColor(this, R.color.white))
                .setLabelColor(Color.WHITE)
                .setLabelBackgroundColor(ResourcesCompat.getColor(resources, R.color.inbox_primary,
                        theme))
                .create())
        drawable = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.baseline_photo_white_24)
        speedDial?.addActionItem(SpeedDialActionItem.Builder(R.id.fab_photo, drawable)
                .setFabBackgroundColor(ResourcesCompat.getColor(resources, mThemeApp?.getPrimaryColor()!!,
                        theme))
                .setLabel(R.string.photo)
                .setFabImageTintColor(ContextCompat.getColor(this, R.color.white))
                .setLabelColor(ContextCompat.getColor(applicationContext, R.color.white))
                .setLabelBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.inbox_primary,
                        theme))
                .create())
        speedDial?.mainFabAnimationRotateAngle = 180f
    }

    //Set main action clicklistener.
    speedDial?.setOnChangeListener(object : SpeedDialView.OnChangeListener {
        override fun onMainActionSelected(): Boolean {
            return false // True to keep the Speed Dial open
        }

        override fun onToggleChanged(isOpen: Boolean) {
            Utils.Log(TAG, "Speed dial toggle state changed. Open = $isOpen")
        }
    })

    //Set option fabs clicklisteners.
    speedDial?.setOnActionSelectedListener(object : SpeedDialView.OnActionSelectedListener {
        override fun onActionSelected(actionItem: SpeedDialActionItem?): Boolean {
            when (actionItem?.id) {
                R.id.fab_album -> return false // false will close it without animation
                R.id.fab_photo -> {
                    Navigator.onMoveToAlbum(this@initSpeedDial)
                    return false // closes without animation (same as mSpeedDialView.close(false); return false;)
                }
                R.id.fab_camera -> {
                    onAddPermissionCamera()
                    return false
                }
            }
            return true // To keep the Speed Dial open
        }
    })
    speedDial.mainFab.setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_IN)
}


fun AlbumDetailAct.onStartProgressing() {
    try {
       SingletonManagerProcessing.getInstance()?.onStartProgressing(this, R.string.exporting)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun AlbumDetailAct.onStopProgressing() {
    try {
        SingletonManagerProcessing.getInstance()?.onStopProgressing(this)
        if (actionMode != null) {
            actionMode?.finish()
        }
    } catch (e: Exception) {
        Utils.Log(TAG, e.message + "")
    }
}

suspend fun AlbumDetailAct.onBannerLoading() = withContext(Dispatchers.Main) {
    try {
        collapsing_toolbar.title = mainCategory.categories_name
        val mList: MutableList<ItemModel>? = SQLHelper.getListItems(mainCategory.categories_local_id, mainCategory.isFakePin)
        val items: ItemModel? = SQLHelper.getItemId(mainCategory.items_id)
        if (items != null && mList != null && mList.size > 0) {
            when (EnumFormatType.values()[items.formatType]) {
                EnumFormatType.AUDIO -> {
                    try {
                        val myColor = Color.parseColor(mainCategory.image)
                        backdrop?.setBackgroundColor(myColor)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                EnumFormatType.FILES -> {
                    try {
                        val myColor = Color.parseColor(mainCategory.image)
                        backdrop?.setBackgroundColor(myColor)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                else -> {
                    if (storage?.isFileExist(items.getThumbnail())!!) {
                        backdrop?.rotation = items.degrees.toFloat()
                        Glide.with(this@onBannerLoading.applicationContext)
                                .load(items.getThumbnail().readFile())
                                .apply(options!!)
                                .into(backdrop!!)
                    } else {
                        backdrop?.setImageResource(0)
                        val myColor = Color.parseColor(mainCategory.image)
                        backdrop?.setBackgroundColor(myColor)
                    }
                }
            }
        } else {
            backdrop?.setImageResource(0)
            val mainCategories: MainCategoryModel? = SQLHelper.getCategoriesPosition(mainCategory.mainCategories_Local_Id)
            if (mainCategories != null) {
                try {
                    val myColor = Color.parseColor(mainCategories.image)
                    backdrop?.setBackgroundColor(myColor)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                try {
                    val myColor = Color.parseColor(mainCategory.image)
                    backdrop?.setBackgroundColor(myColor)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    catch (e : Exception){
        e.printStackTrace()
    }
}

fun AlbumDetailAct.importingData(mData : MutableList<ImportFilesModel>) = CoroutineScope(Dispatchers.Main).launch{
    val mResult = ServiceManager.getInstance()?.onImportData(mData)
    when(mResult?.status){
        Status.SUCCESS -> {
            getData()
        }
        else -> Utils.Log(TAG,mResult?.message)
    }
}

private fun AlbumDetailAct.setupViewModel() {
    viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory()
    ).get(AlbumDetailViewModel::class.java)
}

