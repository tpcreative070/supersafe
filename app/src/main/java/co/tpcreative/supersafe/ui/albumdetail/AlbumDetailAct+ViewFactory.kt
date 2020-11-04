package co.tpcreative.supersafe.ui.albumdetail
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.controller.PrefsController
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.controller.SingletonManagerProcessing
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.ConvertUtils
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.common.views.GridSpacingItemDecoration
import co.tpcreative.supersafe.common.views.NpaGridLayoutManager
import co.tpcreative.supersafe.model.*
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.Glide
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.leinardi.android.speeddial.SpeedDialView
import com.snatik.storage.Storage
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_album_detail.*
import kotlinx.android.synthetic.main.activity_album_detail.collapsing_toolbar
import kotlinx.android.synthetic.main.activity_album_detail.recyclerView
import kotlinx.android.synthetic.main.activity_album_detail.speedDial
import kotlinx.android.synthetic.main.activity_album_detail.toolbar
import kotlinx.android.synthetic.main.footer_items_detail_album.*
import java.io.File
import java.util.ArrayList

fun AlbumDetailAct.initUI(){
    TAG = this::class.java.simpleName
    storage = Storage(this)
    storage?.setEncryptConfiguration(SuperSafeApplication.getInstance().getConfigurationFile())
    initSpeedDial(true)
    presenter = AlbumDetailPresenter()
    presenter?.bindView(this)
    onInit()
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
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
                    Glide.with(this)
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

fun AlbumDetailAct.initRecycleView(layoutInflater: LayoutInflater) {
    try {
        val isVertical: Boolean = PrefsController.getBoolean(getString(R.string.key_vertical_adapter), false)
        if (isVertical) {
            recyclerView?.recycledViewPool?.clear()
            verticalAdapter = AlbumDetailVerticalAdapter(getLayoutInflater(), this, this)
            val mLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(applicationContext)
            recyclerView?.layoutManager = mLayoutManager
            while (recyclerView?.itemDecorationCount!! > 0) {
                recyclerView?.removeItemDecorationAt(0)
            }
            recyclerView?.addItemDecoration(DividerItemDecoration(this, 0))
            recyclerView?.adapter = verticalAdapter
            verticalAdapter?.setDataSource(presenter?.mList)
        } else {
            recyclerView?.recycledViewPool?.clear()
            adapter = AlbumDetailAdapter(layoutInflater, applicationContext, this)
            val mLayoutManager: RecyclerView.LayoutManager = NpaGridLayoutManager(applicationContext, 3)
            recyclerView?.layoutManager = mLayoutManager
            while (recyclerView?.itemDecorationCount!! > 0) {
                recyclerView?.removeItemDecorationAt(0)
            }
            recyclerView?.addItemDecoration(GridSpacingItemDecoration(3, 4, true))
            recyclerView?.adapter = adapter
            adapter?.setDataSource(presenter?.mList)
        }
    } catch (e: Exception) {
        e.message
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
            .message (text =content!!)
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
                        ServiceManager.getInstance()?.setmListExport(mListExporting)
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
                        ServiceManager.getInstance()?.setmListExport(mListExporting)
                        ServiceManager.getInstance()?.onExportingFiles()
                    }
                    EnumStatus.DELETE -> {
                        presenter?.onDelete()
                    }
                    else -> Utils.Log(TAG,"Nothing")
                }
            }
    builder.show()
}

fun AlbumDetailAct.onDialogDownloadFile() {
    SingletonManagerProcessing.getInstance()?.onStartProgressing(this,R.string.downloading)
}

/*Download file*/
fun AlbumDetailAct.onEnableSyncData() {
    val mUser: User? = Utils.getUserInfo()
    if (mUser != null) {
        if (mUser.verified) {
            if (!mUser.driveConnected) {
                Navigator.onCheckSystem(this, null)
            } else {
                onDialogDownloadFile()
                ServiceManager.getInstance()?.onPreparingEnableDownloadData(presenter?.mList?.let { Utils.getCheckedList(it) })
                //ServiceManager.getInstance().getObservableDownload();
            }
        } else {
            Navigator.onVerifyAccount(this)
        }
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
                .setFabImageTintColor(ContextCompat.getColor(this,R.color.white))
                .setLabelColor(Color.WHITE)
                .setLabelBackgroundColor(ResourcesCompat.getColor(resources, R.color.inbox_primary,
                        theme))
                .create())
        drawable = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.baseline_photo_white_24)
        speedDial?.addActionItem(SpeedDialActionItem.Builder(R.id.fab_photo, drawable)
                .setFabBackgroundColor(ResourcesCompat.getColor(resources, mThemeApp?.getPrimaryColor()!!,
                        theme))
                .setLabel(R.string.photo)
                .setFabImageTintColor(ContextCompat.getColor(this,R.color.white))
                .setLabelColor(ContextCompat.getColor(applicationContext,R.color.white))
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
       SingletonManagerProcessing.getInstance()?.onStartProgressing(this,R.string.exporting)
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
        Utils.Log(TAG, e.message+"")
    }
}

