package co.tpcreative.supersafe.ui.albumdetail
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.MenuItem
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
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.network.Status
import co.tpcreative.supersafe.common.network.base.ViewModelFactory
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.ConvertUtils
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
import java.io.File
import java.util.*

fun AlbumDetailAct.initUI(){
    TAG = this::class.java.simpleName
    setupViewModel()
    storage = Storage(this)
    storage?.setEncryptConfiguration(SuperSafeApplication.getInstance().getConfigurationFile())
    initSpeedDial(true)
    presenter = AlbumDetailPresenter()
    presenter?.bindView(this)
    onInit()
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    llBottom?.visibility = View.GONE
    /*Root Fragment*/
    imgShare.setOnClickListener {
        if (countSelected > 0) {
            storage?.createDirectory(SuperSafeApplication.getInstance().getSuperSafeShare())
            presenter?.status = EnumStatus.SHARE
            onShowDialog(presenter?.status)
        }
    }

    imgExport.setOnClickListener {
        onExport()
    }

    imgDelete.setOnClickListener {
        if (countSelected > 0) {
            presenter?.status = EnumStatus.DELETE
            onShowDialog(presenter?.status)
        }
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
        if (it) {
            llToolbarInfo.visibility = View.INVISIBLE
            progress_bar.visibility = View.VISIBLE
        } else {
            llToolbarInfo.visibility = View.VISIBLE
            progress_bar.visibility = View.INVISIBLE
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


private fun AlbumDetailAct.getData(){
    viewModel.isLoading.postValue(true)
    viewModel.getData(this).observe(this, Observer {
        when (it.status) {
            Status.SUCCESS -> {
                CoroutineScope(Dispatchers.Main).launch {
                    val mResult = async {
                        Utils.Log(TAG, "Loading...")
                        adapter?.setDataSource(it.data)
                    }
                    mResult.await()
                    viewModel.isLoading.postValue(false)
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

private fun AlbumDetailAct.deleteItems(){
    viewModel.deleteItems().observe(this, Observer {
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
}

fun AlbumDetailAct.onExport(){
    if (countSelected > 0) {
        storage?.createDirectory(SuperSafeApplication.getInstance().getSuperSafePicture())
        presenter?.status = EnumStatus.EXPORT
        var isSaver = false
        var spaceAvailable: Long = 0
        for (i in presenter?.mList?.indices!!) {
            val items: ItemModel? = presenter?.mList?.get(i)
            if (items?.isSaver!! && items.isChecked) {
                isSaver = true
                spaceAvailable += items.size?.toLong()!!
            }
        }
        val availableSpaceOS: Long = Utils.getAvailableSpaceInBytes()
        if (availableSpaceOS < spaceAvailable) {
            val request_spaces = spaceAvailable - availableSpaceOS
            val result: String? = ConvertUtils.byte2FitMemorySize(request_spaces)
            val message: String = kotlin.String.format(getString(R.string.your_space_is_not_enough_to), "export. ", "Request spaces: $result")
            Utils.showDialog(this, message = message)
        } else {
            if (isSaver) {
                onEnableSyncData()
            } else {
                onShowDialog(presenter?.status)
            }
        }
    }
}



fun AlbumDetailAct.onShowDialog(status: EnumStatus?) {
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
    }
    val builder: MaterialDialog = MaterialDialog(this)
            .title(text = getString(R.string.confirm))
            .message(text = content!!)
            .negativeButton(text = getString(R.string.cancel))
            .positiveButton(text = getString(R.string.ok))
            .negativeButton { presenter?.status = EnumStatus.CANCEL }
            .positiveButton {
                val mListExporting: MutableList<ExportFiles> = ArrayList<ExportFiles>()
                when (status) {
                    EnumStatus.SHARE -> {
                        Utils.onPushEventBus(EnumStatus.START_PROGRESS)
                        presenter?.mListShare?.clear()
                        var i = 0
                        while (i < presenter?.mList?.size!!) {
                            val index: ItemModel? = presenter?.mList?.get(i)
                            if (index != null) {
                                if (index.isChecked) {
                                    when (EnumFormatType.values()[index.formatType]) {
                                        EnumFormatType.AUDIO -> {
                                            val input = File(index.getOriginal())
                                            var output: File? = File(SuperSafeApplication.getInstance().getSuperSafeShare() + index.originalName + index.fileExtension)
                                            if (storage?.isFileExist(output?.absolutePath)!!) {
                                                output = File(SuperSafeApplication.getInstance().getSuperSafeShare() + index.originalName + "(1)" + index.fileExtension)
                                            }
                                            if (storage?.isFileExist(input.absolutePath)!!) {
                                                if (output != null) {
                                                    presenter?.mListShare?.add(output)
                                                }
                                                val exportFiles = ExportFiles(input, output, i, false, index.formatType)
                                                mListExporting.add(exportFiles)
                                            }
                                        }
                                        EnumFormatType.FILES -> {
                                            val input = File(index.getOriginal())
                                            var output: File? = File(SuperSafeApplication.getInstance().getSuperSafeShare() + index.originalName + index.fileExtension)
                                            if (storage?.isFileExist(output?.absolutePath)!!) {
                                                output = File(SuperSafeApplication.getInstance().getSuperSafeShare() + index.originalName + "(1)" + index.fileExtension)
                                            }
                                            if (storage?.isFileExist(input.absolutePath)!!) {
                                                if (output != null) {
                                                    presenter?.mListShare?.add(output)
                                                }
                                                val exportFiles = ExportFiles(input, output, i, false, index.formatType)
                                                mListExporting.add(exportFiles)
                                            }
                                        }
                                        EnumFormatType.VIDEO -> {
                                            val input = File(index.getOriginal())
                                            var output: File? = File(SuperSafeApplication.getInstance().getSuperSafeShare() + index.originalName + index.fileExtension)
                                            if (storage?.isFileExist(output?.absolutePath)!!) {
                                                output = File(SuperSafeApplication.getInstance().getSuperSafeShare() + index.originalName + "(1)" + index.fileExtension)
                                            }
                                            if (storage?.isFileExist(input.absolutePath)!!) {
                                                if (output != null) {
                                                    presenter?.mListShare?.add(output)
                                                }
                                                val exportFiles = ExportFiles(input, output, i, false, index.formatType)
                                                mListExporting.add(exportFiles)
                                            }
                                        }
                                        else -> {
                                            var path = ""
                                            path = if (index.mimeType == getString(R.string.key_gif)) {
                                                index.getOriginal()
                                            } else {
                                                index.getThumbnail()
                                            }
                                            val input = File(path)
                                            var output: File? = File(SuperSafeApplication.getInstance().getSuperSafeShare() + index.originalName + index.fileExtension)
                                            if (storage?.isFileExist(output?.absolutePath)!!) {
                                                output = File(SuperSafeApplication.getInstance().getSuperSafeShare() + index.originalName + "(1)" + index.fileExtension)
                                            }
                                            if (storage?.isFileExist(input.absolutePath)!!) {
                                                if (output != null) {
                                                    presenter?.mListShare?.add(output)
                                                }
                                                val exportFiles = ExportFiles(input, output, i, false, index.formatType)
                                                mListExporting.add(exportFiles)
                                            }
                                        }
                                    }
                                }
                            }
                            i++
                        }
                        onStartProgressing()
                        ServiceManager.getInstance()?.setListExport(mListExporting)
                        ServiceManager.getInstance()?.onExportingFiles()
                    }
                    EnumStatus.EXPORT -> {
                        Utils.onPushEventBus(EnumStatus.START_PROGRESS)
                        presenter?.mListShare?.clear()
                        var i = 0
                        while (i < presenter?.mList?.size!!) {
                            val index: ItemModel? = presenter?.mList?.get(i)
                            if (index?.isChecked!!) {
                                val formatType = EnumFormatType.values()[index.formatType]
                                when (formatType) {
                                    EnumFormatType.AUDIO -> {
                                        val input = File(index.getOriginal())
                                        Utils.Log(TAG, "Name :" + index.originalName)
                                        var output: File? = File(SuperSafeApplication.getInstance().getSuperSafePicture() + index.title)
                                        if (storage?.isFileExist(output?.getAbsolutePath())!!) {
                                            output = File(SuperSafeApplication.getInstance().getSuperSafePicture() + index.originalName + "(1)" + index.fileExtension)
                                        }
                                        if (storage?.isFileExist(input.absolutePath)!!) {
                                            if (output != null) {
                                                presenter?.mListShare?.add(output)
                                            }
                                            val exportFiles = ExportFiles(input, output, i, false, index.formatType)
                                            mListExporting.add(exportFiles)
                                        }
                                    }
                                    EnumFormatType.FILES -> {
                                        val input = File(index.getOriginal())
                                        Utils.Log(TAG, "Name :" + index.originalName)
                                        var output: File? = File(SuperSafeApplication.getInstance().getSuperSafePicture() + index.title)
                                        if (storage?.isFileExist(output?.absolutePath)!!) {
                                            output = File(SuperSafeApplication.getInstance().getSuperSafePicture() + index.originalName + "(1)" + index.fileExtension)
                                        }
                                        if (storage?.isFileExist(input.absolutePath)!!) {
                                            if (output != null) {
                                                presenter?.mListShare?.add(output)
                                            }
                                            val exportFiles = ExportFiles(input, output, i, false, index.formatType)
                                            mListExporting.add(exportFiles)
                                        }
                                    }
                                    EnumFormatType.VIDEO -> {
                                        val input = File(index.getOriginal())
                                        var output: File? = File(SuperSafeApplication.getInstance().getSuperSafePicture() + index.title)
                                        if (storage?.isFileExist(output?.absolutePath)!!) {
                                            output = File(SuperSafeApplication.getInstance().getSuperSafePicture() + index.originalName + "(1)" + index.fileExtension)
                                        }
                                        if (storage?.isFileExist(input.absolutePath)!!) {
                                            if (output != null) {
                                                presenter?.mListShare?.add(output)
                                            }
                                            val exportFiles = ExportFiles(input, output, i, false, index.formatType)
                                            mListExporting.add(exportFiles)
                                        }
                                    }
                                    else -> {
                                        val input = File(index.getOriginal())
                                        var output: File? = File(SuperSafeApplication.getInstance().getSuperSafePicture() + index.title)
                                        if (storage?.isFileExist(output?.absolutePath)!!) {
                                            output = File(SuperSafeApplication.getInstance().getSuperSafePicture() + index.originalName + "(1)" + index.fileExtension)
                                        }
                                        if (storage?.isFileExist(input.absolutePath)!!) {
                                            if (output != null) {
                                                presenter?.mListShare?.add(output)
                                            }
                                            val exportFiles = ExportFiles(input, output, i, false, index.formatType)
                                            mListExporting.add(exportFiles)
                                        }
                                        Utils.Log(TAG, "Exporting file " + input.absolutePath)
                                    }
                                }
                            }
                            i++
                        }
                        onStartProgressing()
                        ServiceManager.getInstance()?.setListExport(mListExporting)
                        ServiceManager.getInstance()?.onExportingFiles()
                    }
                    EnumStatus.DELETE -> {
                        presenter?.onDelete()
                    }
                    else -> Utils.Log(TAG, "Nothing")
                }
            }
    builder.show()
}

fun AlbumDetailAct.onDialogDownloadFile() {
    SingletonManagerProcessing.getInstance()?.onStartProgressing(this, R.string.downloading)
}

/*Download file*/
fun AlbumDetailAct.onEnableSyncData() {
    if (Utils.isVerifiedAccount()) {
        if (Utils.isConnectedToGoogleDrive()) {
            onDialogDownloadFile()
            ServiceManager.getInstance()?.onPreparingEnableDownloadData(presenter?.mList?.let { Utils.getCheckedList(it) })

        } else {
            Navigator.onCheckSystem(this, null)
        }
    } else {
        Navigator.onVerifyAccount(this)
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

fun AlbumDetailAct.onInit() {
//    llToolbarInfo.visibility = View.INVISIBLE
//    progress_bar.visibility = View.VISIBLE
//    onCallData()
}

fun AlbumDetailAct.onCallData(){
    mainScope.launch {
        val mInitRecyclerView = async {
            initRecycleView(layoutInflater)
        }
        val mResultData = async {
            presenter?.getData(this@onCallData)
        }
        val mRecyclerViewLoading = async {
            onLoading()
        }
        val mBannerLoading = async {
            onBannerLoading()
        }
        mInitRecyclerView.await()
        mResultData.await()
        mRecyclerViewLoading.await()
        mBannerLoading.await()
        progress_bar.visibility = View.INVISIBLE
        llToolbarInfo.visibility = View.VISIBLE
        Utils.Log(TAG, "Loading data")
        waitingToInit()
    }
}


suspend fun AlbumDetailAct.onBannerLoading() = withContext(Dispatchers.Main) {
    collapsing_toolbar.title = presenter?.mainCategories?.categories_name
    val mList: MutableList<ItemModel>? = presenter?.mainCategories?.isFakePin?.let { SQLHelper.getListItems(presenter?.mainCategories?.categories_local_id, it) }
    val items: ItemModel? = SQLHelper.getItemId(presenter?.mainCategories?.items_id)
    if (items != null && mList != null && mList.size > 0) {
        when (EnumFormatType.values()[items.formatType]) {
            EnumFormatType.AUDIO -> {
                try {
                    val myColor = Color.parseColor(presenter?.mainCategories?.image)
                    backdrop?.setBackgroundColor(myColor)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            EnumFormatType.FILES -> {
                try {
                    val myColor = Color.parseColor(presenter?.mainCategories?.image)
                    backdrop?.setBackgroundColor(myColor)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            else -> {
                if (storage?.isFileExist(items.getThumbnail())!!) {
                    backdrop?.rotation = items.degrees.toFloat()
                    Glide.with(this@onBannerLoading.applicationContext)
                            .load(storage?.readFile(items.getThumbnail()))
                            .apply(options!!)
                            .into(backdrop!!)
                } else {
                    backdrop?.setImageResource(0)
                    val myColor = Color.parseColor(presenter?.mainCategories?.image)
                    backdrop?.setBackgroundColor(myColor)
                }
            }
        }
    } else {
        backdrop?.setImageResource(0)
        val mainCategories: MainCategoryModel? = SQLHelper.getCategoriesPosition(presenter?.mainCategories?.mainCategories_Local_Id)
        if (mainCategories != null) {
            try {
                val myColor = Color.parseColor(mainCategories.image)
                backdrop?.setBackgroundColor(myColor)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            try {
                val myColor = Color.parseColor(presenter?.mainCategories?.image)
                backdrop?.setBackgroundColor(myColor)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

suspend fun AlbumDetailAct.onLoading() = withContext(Dispatchers.Main){
    if (Utils.isVertical()) {
        verticalAdapter?.getDataSource()?.clear()
        presenter?.mList?.let { verticalAdapter?.getDataSource()?.addAll(it) }
    } else {
        adapter?.getDataSource()?.clear()
        presenter?.mList?.let { adapter?.getDataSource()?.addAll(it) }
    }
}

fun AlbumDetailAct.onPushDataToList(){
    mainScope.launch {
        val mResult = async {
            onLoading()
        }
        mResult.await()
        Utils.Log(TAG, "Reload data completed====>")
    }
}

private fun AlbumDetailAct.setupViewModel() {
    viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory()
    ).get(AlbumDetailViewModel::class.java)
}

private fun loadData(){

}

