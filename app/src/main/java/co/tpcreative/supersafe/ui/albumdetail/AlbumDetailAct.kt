package co.tpcreative.supersafe.ui.albumdetail
import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseGalleryActivity
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.util.Configuration
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.common.views.GridSpacingItemDecoration
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.bumptech.glide.Priority
import com.karumi.dexter.listener.PermissionRequest
import com.leinardi.android.speeddial.SpeedDialView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.io.File
import java.util.*
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.controller.PrefsController
import co.tpcreative.supersafe.common.controller.SingletonPrivateFragment
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.views.NpaGridLayoutManager
import co.tpcreative.supersafe.model.*
import com.bumptech.glide.request.RequestOptions
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.DexterError
import com.karumi.dexter.listener.PermissionRequestErrorListener
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.leinardi.android.speeddial.SpeedDialActionItem
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_album_detail.*
import kotlinx.android.synthetic.main.footer_items_detail_album.*
import org.greenrobot.eventbus.ThreadMode

class AlbumDetailAct : BaseGalleryActivity(), BaseView<Int>, AlbumDetailAdapter.ItemSelectedListener, AlbumDetailVerticalAdapter.ItemSelectedListener {
    var presenter: AlbumDetailPresenter? = null
    private var adapter: AlbumDetailAdapter? = null
    private var verticalAdapter: AlbumDetailVerticalAdapter? = null
    private var isReload = false
    var actionMode: ActionMode? = null
    var countSelected = 0
    private var isSelectAll = false
    private var dialog: AlertDialog? = null
    var mDialogProgress: SweetAlertDialog? = null
    private var menuItem: MenuItem? = null
    var options: RequestOptions? = RequestOptions()
            .centerCrop()
            .override(400, 400)
            .placeholder(R.color.colorPrimary)
            .error(R.color.colorPrimary)
            .priority(Priority.HIGH)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album_detail)
        initUI()
        attachFragment(R.id.gallery_root)
    }

    fun onInit() {
        presenter?.getData(this)
        initRecycleView(getLayoutInflater())
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: EnumStatus?) {
        when (event) {
            EnumStatus.REFRESH -> {
                presenter?.getData(this)
            }
            EnumStatus.FINISH -> {
                Navigator.onMoveToFaceDown(this)
            }
            EnumStatus.UPDATED_VIEW_DETAIL_ALBUM -> {
                try {
                    this.runOnUiThread(Runnable { presenter?.getData(EnumStatus.RELOAD) })
                } catch (e: Exception) {
                    e.printStackTrace()
                }
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
                                if (presenter?.mListShare!!.size > 0) {
                                    Utils.shareMultiple(presenter?.mListShare!!, this)
                                }
                            }
                        }
                        EnumStatus.EXPORT -> {
                            runOnUiThread(Runnable { Toast.makeText(this@AlbumDetailAct, "Exported at " + SuperSafeApplication.getInstance().getSupersafePicture(), Toast.LENGTH_LONG).show() })
                        }
                    }
                } catch (e: Exception) {
                    Utils.Log(TAG, e.message +"")
                }
            }
            EnumStatus.DOWNLOAD_COMPLETED -> {
                mDialogProgress?.setTitleText("Success!")
                        ?.setConfirmText("OK")
                        ?.changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
                mDialogProgress?.setConfirmClickListener { sweetAlertDialog ->
                    sweetAlertDialog?.dismiss()
                    var i = 0
                    while (i < presenter?.mList?.size!!) {
                        val items: ItemModel? = presenter?.mList?.get(i)
                        if (items?.isChecked!!) {
                            items?.isSaver = false
                        }
                        i++
                    }
                    onExport()
                }
                Utils.Log(TAG, "already sync ")
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
        ServiceManager.getInstance()?.setRequestShareIntent(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        Utils.Log(TAG, "OnDestroy")
        EventBus.getDefault().unregister(this)
        presenter?.unbindView()
        if (isReload) {
            ServiceManager.getInstance()?.onPreparingSyncData()
        }
        storage?.deleteDirectory(SuperSafeApplication.getInstance().getSupersafeShare())
    }

    override fun onStop() {
        super.onStop()
        Utils.Log(TAG, "onStop Album")
    }

    override fun onStopListenerAWhile() {
        EventBus.getDefault().unregister(this)
    }

    override fun onOrientationChange(isFaceDown: Boolean) {
        onFaceDown(isFaceDown)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (toolbar == null) {
            return false
        }
        toolbar?.inflateMenu(R.menu.menu_album_detail)
        menuItem = toolbar?.menu?.getItem(0)
        val isVertical: Boolean = PrefsController.getBoolean(getString(R.string.key_vertical_adapter), false)
        if (isVertical) {
            menuItem?.icon = ContextCompat.getDrawable(this,R.drawable.baseline_view_comfy_white_48)
        } else {
            menuItem?.icon = ContextCompat.getDrawable(this,R.drawable.baseline_format_list_bulleted_white_48)
        }
        return true
    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_album_settings -> {
                Navigator.onAlbumSettings(this, presenter?.mainCategories!!)
                return true
            }
            R.id.action_select_items -> {
                if (actionMode == null) {
                    actionMode = toolbar?.startActionMode(callback)
                }
                countSelected = 0
                actionMode?.title = countSelected.toString() + " " + getString(R.string.selected)
                Utils.Log(TAG, "Action here")
                return true
            }
            R.id.action_view -> {
                if (menuItem != null) {
                    val isVertical: Boolean = PrefsController.getBoolean(getString(R.string.key_vertical_adapter), false)
                    if (isVertical) {
                        menuItem?.icon = ContextCompat.getDrawable(this,R.drawable.baseline_format_list_bulleted_white_48)
                        PrefsController.putBoolean(getString(R.string.key_vertical_adapter), false)
                        onInit()
                    } else {
                        menuItem?.icon = ContextCompat.getDrawable(this,R.drawable.baseline_view_comfy_white_48)
                        PrefsController.putBoolean(getString(R.string.key_vertical_adapter), true)
                        onInit()
                    }
                }
            }
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClickItem(position: Int) {
        Utils.Log(TAG, "On clicked item")
        if (position >= presenter?.mList?.size!!) {
            return
        }
        if (actionMode != null) {
            toggleSelection(position)
            actionMode?.setTitle(countSelected.toString() + " " + getString(R.string.selected))
        } else {
            try {
                when (EnumFormatType.values()[presenter?.mList?.get(position)?.formatType!!]) {
                    EnumFormatType.FILES -> {
                        Toast.makeText(getContext(), "Can not support to open type of this file", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        presenter?.mainCategories?.let { Navigator.onPhotoSlider(this, presenter?.mList?.get(position)!!, presenter?.mList!!, it) }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onLongClickItem(position: Int) {
        Utils.Log(TAG, "On long clicked item")
        if (position >= presenter?.mList?.size!!) {
            return
        }
        if (actionMode == null) {
            actionMode = toolbar?.startActionMode(callback)
        }
        toggleSelection(position)
        actionMode?.title = countSelected.toString() + " " + getString(R.string.selected)
    }

    private fun onStartProgressing() {
        try {
            runOnUiThread(Runnable {
                if (dialog == null) {
                    val themeApp: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
                    dialog = SpotsDialog.Builder()
                            .setContext(this@AlbumDetailAct)
                            .setDotColor(themeApp?.getAccentColor()!!)
                            .setMessage(getString(R.string.exporting))
                            .setCancelable(true)
                            .build()
                }
                if (!dialog!!.isShowing()) {
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
                    if (actionMode != null) {
                        actionMode?.finish()
                    }
                }
            })
        } catch (e: Exception) {
            Utils.Log(TAG, e.message+"")
        }
    }

    override fun onStartLoading(status: EnumStatus) {}
    override fun onStopLoading(status: EnumStatus) {}
    override fun getContext(): Context? {
        return applicationContext
    }

    /*Init Floating View*/
    fun initSpeedDial(addActionItems: Boolean) {
        val mThemeApp: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
        if (addActionItems) {
            var drawable: Drawable? = AppCompatResources.getDrawable(applicationContext, R.drawable.baseline_photo_camera_white_24)
            speedDial?.addActionItem(SpeedDialActionItem.Builder(R.id.fab_camera, drawable)
                    .setFabBackgroundColor(ResourcesCompat.getColor(resources, mThemeApp?.getPrimaryColor()!!,
                            theme))
                    .setLabel(getString(R.string.camera))
                    .setLabelColor(Color.WHITE)
                    .setLabelBackgroundColor(ResourcesCompat.getColor(resources, R.color.inbox_primary,
                            theme))
                    .create())
            drawable = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.baseline_photo_white_24)
            speedDial?.addActionItem(SpeedDialActionItem.Builder(R.id.fab_photo, drawable)
                    .setFabBackgroundColor(ResourcesCompat.getColor(resources, mThemeApp?.getPrimaryColor()!!,
                            theme))
                    .setLabel(R.string.photo)
                    .setLabelColor(ContextCompat.getColor(applicationContext,R.color.white))
                    .setLabelBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.inbox_primary,
                            theme))
                    .create())
            speedDial?.setMainFabAnimationRotateAngle(180f)
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
                when (actionItem?.getId()) {
                    R.id.fab_album -> return false // false will close it without animation
                    R.id.fab_photo -> {
                        Navigator.onMoveToAlbum(this@AlbumDetailAct)
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
    }

    override fun onBackPressed() {
        if (speedDial?.isOpen!!) {
            speedDial?.close()
        } else if (actionMode != null) {
            actionMode?.finish()
        } else {
            super.onBackPressed()
        }
    }

    /*Init grant permission*/
    fun onAddPermissionCamera() {
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.CAMERA)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report?.areAllPermissionsGranted()!!) {
                            presenter?.mainCategories?.let { Navigator.onMoveCamera(this@AlbumDetailAct, it) }
                        } else {
                            Utils.Log(TAG, "Permission is denied")
                        }
                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            /*Miss add permission in manifest*/
                            Utils.Log(TAG, "request permission is failed")
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest?>?, token: PermissionToken?) {
                        /* ... */
                        token?.continuePermissionRequest()
                    }
                })
                .withErrorListener(object : PermissionRequestErrorListener {
                    override fun onError(error: DexterError) {
                        Utils.Log(TAG, "error ask permission")
                    }
                }).onSameThread().check()
    }

    fun initRecycleView(layoutInflater: LayoutInflater) {
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

    fun onShowDialog(status: EnumStatus?) {
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
        val builder: MaterialDialog.Builder = MaterialDialog.Builder(this)
                .title(getString(R.string.confirm))
                .theme(Theme.LIGHT)
                .content(content!!)
                .titleColor(ContextCompat.getColor(this,R.color.black))
                .negativeText(getString(R.string.cancel))
                .positiveText(getString(R.string.ok))
                .onNegative { dialog, which -> presenter?.status = EnumStatus.CANCEL }
                .onPositive { dialog, which ->
                    val mListExporting: MutableList<ExportFiles> = ArrayList<ExportFiles>()
                    when (status) {
                        EnumStatus.SHARE -> {
                            EventBus.getDefault().post(EnumStatus.START_PROGRESS)
                            presenter?.mListShare?.clear()
                            var i = 0
                            while (i < presenter?.mList?.size!!) {
                                val index: ItemModel? = presenter?.mList?.get(i)
                                if (index != null) {
                                    if (index.isChecked) {
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
                                                    val exportFiles = ExportFiles(input, output, i, false, index.formatType)
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
                                                    if (output != null) {
                                                        presenter?.mListShare?.add(output)
                                                    }
                                                    val exportFiles = ExportFiles(input, output, i, false, index.formatType)
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
                                                    index.originalPath!!
                                                } else {
                                                    index.thumbnailPath!!
                                                }
                                                val input = File(path)
                                                var output: File? = File(SuperSafeApplication.getInstance().getSupersafeShare() + index.originalName + index.fileExtension)
                                                if (storage?.isFileExist(output?.getAbsolutePath())!!) {
                                                    output = File(SuperSafeApplication.getInstance().getSupersafeShare() + index.originalName + "(1)" + index.fileExtension)
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
                            EventBus.getDefault().post(EnumStatus.START_PROGRESS)
                            presenter?.mListShare?.clear()
                            var i = 0
                            while (i < presenter?.mList?.size!!) {
                                val index: ItemModel? = presenter?.mList?.get(i)
                                if (index?.isChecked!!) {
                                    val formatType = EnumFormatType.values()[index.formatType]
                                    when (formatType) {
                                        EnumFormatType.AUDIO -> {
                                            val input = File(index.originalPath)
                                            Utils.Log(TAG, "Name :" + index.originalName)
                                            var output: File? = File(SuperSafeApplication.getInstance().getSupersafePicture() + index.title)
                                            if (storage?.isFileExist(output?.getAbsolutePath())!!) {
                                                output = File(SuperSafeApplication.getInstance().getSupersafePicture() + index.originalName + "(1)" + index.fileExtension)
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
                                            val input = File(index.originalPath)
                                            Utils.Log(TAG, "Name :" + index.originalName)
                                            var output: File? = File(SuperSafeApplication.getInstance().getSupersafePicture() + index.title)
                                            if (storage?.isFileExist(output?.getAbsolutePath())!!) {
                                                output = File(SuperSafeApplication.getInstance().getSupersafePicture() + index.originalName + "(1)" + index.fileExtension)
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
                                            val input = File(index.originalPath)
                                            var output: File? = File(SuperSafeApplication.getInstance().getSupersafePicture() + index.title)
                                            if (storage?.isFileExist(output?.getAbsolutePath())!!) {
                                                output = File(SuperSafeApplication.getInstance().getSupersafePicture() + index.originalName + "(1)" + index.fileExtension)
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
                                            val input = File(index.originalPath)
                                            var output: File? = File(SuperSafeApplication.getInstance().getSupersafePicture() + index.title)
                                            if (storage?.isFileExist(output?.absolutePath)!!) {
                                                output = File(SuperSafeApplication.getInstance().getSupersafePicture() + index.originalName + "(1)" + index.fileExtension)
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
                    }
                }
        builder.show()
    }

    fun onDialogDownloadFile() {
        mDialogProgress = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
                .setTitleText(getString(R.string.downloading))
        mDialogProgress?.show()
        mDialogProgress?.setCancelable(false)
    }

    /*Download file*/
    fun onEnableSyncData() {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Utils.Log(TAG, "Selected album :")
        when (requestCode) {
            Navigator.CAMERA_ACTION -> {
                if (resultCode == Activity.RESULT_OK) {
                    Utils.Log(TAG, "reload data")
                    presenter?.getData(EnumStatus.RELOAD)
                } else {
                    Utils.Log(TAG, "Nothing to do on Camera")
                }
            }
            Navigator.PHOTO_SLIDE_SHOW -> {
                if (resultCode == Activity.RESULT_OK) {
                    Utils.Log(TAG, "reload data")
                    presenter?.getData(EnumStatus.RELOAD)
                } else {
                    Utils.Log(TAG, "Nothing to do on Camera")
                }
            }
            Navigator.REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    Utils.Log(TAG,"Response data here")
                    val images: ArrayList<Image>? = data.getParcelableArrayListExtra(Navigator.INTENT_EXTRA_IMAGES)
                    val mListImportFiles: MutableList<ImportFilesModel>? = ArrayList<ImportFilesModel>()
                    var i = 0
                    val l = images?.size
                    while (i < l!!) {
                        val path = images[i].path
                        val name = images[i].name
                        val id = "" + images[i].id
                        val mimeType: String? = Utils.getMimeType(path)
                        Utils.Log(TAG, "mimeType $mimeType")
                        Utils.Log(TAG, "name $name")
                        Utils.Log(TAG, "path $path")
                        val fileExtension: String? = Utils.getFileExtension(path)
                        Utils.Log(TAG, "file extension " + Utils.getFileExtension(path))
                        try {
                            val mimeTypeFile: MimeTypeFile = Utils.mediaTypeSupport().get(fileExtension)
                                    ?: return
                            mimeTypeFile.name = name
                            if (presenter?.mainCategories == null) {
                                Utils.onWriteLog("Main categories is null", EnumStatus.WRITE_FILE)
                                return
                            }
                            val importFiles = ImportFilesModel(presenter?.mainCategories, mimeTypeFile, path, i, false)
                            mListImportFiles?.add(importFiles)
                            isReload = true
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        i++
                    }
                    if (mListImportFiles != null) {
                        ServiceManager.getInstance()?.setListImport(mListImportFiles)
                    }
                    ServiceManager.getInstance()?.onPreparingImportData()
                } else {
                    Utils.Log(TAG, "Nothing to do on Gallery")
                }
            }
            Navigator.SHARE -> {
                if (actionMode != null) {
                    actionMode?.finish()
                }
                Utils.Log(TAG, "share action")
            }
            else -> {
                Utils.Log(TAG, "Nothing to do")
            }
        }
    }

    /**
     * Converting dp to pixel
     */
    override fun onError(message: String?, status: EnumStatus?) {}
    override fun onError(message: String?) {}
    override fun onSuccessful(message: String?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?) {
        when (status) {
            EnumStatus.RELOAD -> {
                val photos: String = kotlin.String.format(getString(R.string.photos_default), "" + presenter?.photos)
                tv_Photos?.text = photos
                val videos: String = kotlin.String.format(getString(R.string.videos_default), "" + presenter?.videos)
                tv_Videos?.text = videos
                val audios: String = kotlin.String.format(getString(R.string.audios_default), "" + presenter?.audios)
                tv_Audios?.text = audios
                val others: String = kotlin.String.format(getString(R.string.others_default), "" + presenter?.others)
                tv_Others?.text = others
                if (actionMode != null) {
                    countSelected = 0
                    actionMode?.finish()
                    llBottom?.visibility = View.GONE
                    isReload = true
                }
                val isVertical: Boolean = PrefsController.getBoolean(getString(R.string.key_vertical_adapter), false)
                if (isVertical) {
                    verticalAdapter?.setDataSource(presenter?.mList)
                } else {
                    adapter?.setDataSource(presenter?.mList)
                }
            }
            EnumStatus.REFRESH -> {
                val photos: String = kotlin.String.format(getString(R.string.photos_default), "" + presenter?.photos)
                tv_Photos?.text = photos
                val videos: String = kotlin.String.format(getString(R.string.videos_default), "" + presenter?.videos)
                tv_Videos?.text = videos
                val audios: String = kotlin.String.format(getString(R.string.audios_default), "" + presenter?.audios)
                tv_Audios?.text = audios
                val others: String = kotlin.String.format(getString(R.string.others_default), "" + presenter?.others)
                tv_Others?.text = others
                val isVertical: Boolean = PrefsController.getBoolean(getString(R.string.key_vertical_adapter), false)
                if (isVertical) {
                    verticalAdapter?.getDataSource()?.clear()
                    presenter?.mList?.let { verticalAdapter?.getDataSource()?.addAll(it) }
                } else {
                    adapter?.getDataSource()?.clear()
                    presenter?.mList?.let { adapter?.getDataSource()?.addAll(it) }
                }
            }
            EnumStatus.DELETE -> {
                SingletonPrivateFragment.getInstance()?.onUpdateView()
                if (actionMode != null) {
                    actionMode?.finish()
                }
                isReload = true
            }
        }
    }

    override fun onSuccessful(message: String?, status: EnumStatus?, `object`: Int?) {
        when (status) {
            EnumStatus.DELETE -> {
                Utils.Log(TAG, "Position $`object`")
                if (`object` != null) {
                    onUpdateAdapter(EnumStatus.REMOVE_AT_ADAPTER, `object`)
                }
            }
        }
    }

    override fun getActivity(): Activity? {
        return this
    }

    override fun onSuccessful(message: String?, status: EnumStatus?, list: MutableList<Int>?) {}

    /*Action mode*/
    private val callback: ActionMode.Callback? = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            mode?.menuInflater
            menuInflater?.inflate(R.menu.menu_select, menu)
            actionMode = mode
            countSelected = 0
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                val window: Window = getWindow()
                window.statusBarColor = ContextCompat.getColor(getContext()!!, R.color.material_orange_900)
            }
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            val i = item?.itemId
            if (i == R.id.menu_item_select_all) {
                isSelectAll = !isSelectAll
                selectAll()
                return true
            }
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            if (countSelected > 0) {
                deselectAll()
            }
            actionMode = null
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                val themeApp: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
                if (themeApp != null) {
                    window.statusBarColor = ContextCompat.getColor(getContext()!!, themeApp.getPrimaryDarkColor())
                }
            }
        }
    }

    private fun toggleSelection(position: Int) {
        presenter?.mList?.get(position)?.isChecked = !(presenter?.mList?.get(position)?.isChecked!!)
        if (presenter?.mList?.get(position)?.isChecked!!) {
            countSelected++
        } else {
            countSelected--
        }
        onShowUI()
        onUpdateAdapter(EnumStatus.UPDATE_AT_ADAPTER, position)
    }

    private fun deselectAll() {
        var isExport = false
        val isSaver: Boolean = PrefsController.getBoolean(getString(R.string.key_saving_space), false)
        var i = 0
        val l: Int? = presenter?.mList?.size
        while (i < l!!) {
            when (presenter?.status) {
                EnumStatus.EXPORT -> {
                    if (presenter?.mList?.get(i)?.isChecked!!) {
                        presenter?.mList?.get(i)?.isExport = true
                        presenter?.mList?.get(i)?.isDeleteLocal = true
                        SQLHelper.updatedItem(presenter?.mList?.get(i)!!)
                    }
                    isExport = true
                }
                EnumStatus.CANCEL -> {
                    val items: ItemModel? = presenter?.mList?.get(i)
                    if (items?.isChecked!!) {
                        items.isChecked = false
                        when (EnumFormatType.values()[items.formatType]) {
                            EnumFormatType.IMAGE -> {
                                items.isSaver = isSaver
                                SQLHelper.updatedItem(items)
                                if (isSaver) {
                                    storage?.deleteFile(items.originalPath)
                                }
                            }
                        }
                        onUpdateAdapter(EnumStatus.UPDATE_AT_ADAPTER, i)
                    }
                }
                else -> {
                    if (presenter?.mList?.get(i)?.isChecked!!) {
                        presenter?.mList?.get(i)?.isChecked = false
                        onUpdateAdapter(EnumStatus.UPDATE_AT_ADAPTER, i)
                    }
                }
            }
            i++
        }
        countSelected = 0
        onShowUI()
        if (isExport) {
            onCheckDelete()
        }
    }

    fun onCheckDelete() {
        val mList: MutableList<ItemModel>? = presenter?.mList
        var i = 0
        val l = mList?.size
        while (i < l!!) {
            if (presenter?.mList?.get(i)?.isChecked!!) {
                val formatTypeFile = EnumFormatType.values()[mList[i].formatType]
                if (formatTypeFile == EnumFormatType.AUDIO && mList[i].global_original_id == null) {
                    SQLHelper.deleteItem(mList[i])
                } else if (formatTypeFile == EnumFormatType.FILES && mList[i].global_original_id == null) {
                    SQLHelper.deleteItem(mList[i])
                } else if ((mList[i].global_original_id == null) and (mList[i].global_thumbnail_id == null)) {
                    SQLHelper.deleteItem(mList[i])
                } else {
                    mList[i].deleteAction = EnumDelete.DELETE_WAITING.ordinal
                    SQLHelper.updatedItem(mList[i])
                    Utils.Log(TAG, "ServiceManager waiting for delete")
                }
                storage?.deleteDirectory(SuperSafeApplication.Companion.getInstance().getSupersafePrivate() + mList[i].items_id)
                onUpdateAdapter(EnumStatus.REMOVE_AT_ADAPTER, i)
            }
            i++
        }
        presenter?.getData(EnumStatus.REFRESH)
    }

    fun selectAll() {
        try {
            var countSelect = 0
            for (i in presenter?.mList?.indices!!) {
                presenter?.mList?.get(i)?.isChecked = isSelectAll
                if (presenter?.mList?.get(i)?.isChecked!!) {
                    countSelect++
                }
            }
            countSelected = countSelect
            onShowUI()
            onUpdateAdapter(EnumStatus.UPDATE_ENTIRE_ADAPTER, 0)
            actionMode?.title = countSelected.toString() + " " + getString(R.string.selected)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onShowUI() {
        try {
            runOnUiThread(Runnable {
                if (countSelected == 0) {
                    llBottom?.visibility = View.GONE
                    speedDial?.visibility = View.VISIBLE
                } else {
                    llBottom?.visibility = View.VISIBLE
                    speedDial?.visibility = View.INVISIBLE
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /*Gallery action*/
    override fun getConfiguration(): Configuration? {
        //default configuration
        try {
            return presenter?.mainCategories?.isFakePin?.let {
                Configuration.Builder()
                        .hasCamera(true)
                        ?.hasShade(true)
                        ?.hasPreview(true)
                        ?.setSpaceSize(4)
                        ?.setPhotoMaxWidth(120)
                        ?.setLocalCategoriesId(presenter?.mainCategories?.categories_local_id)
                        ?.setCheckBoxColor(-0xc0ae4b)
                        ?.setFakePIN(it)
                        ?.setDialogHeight(Configuration.Companion.DIALOG_HALF)
                        ?.setDialogMode(Configuration.Companion.DIALOG_LIST)
                        ?.setMaximum(9)
                        ?.setTip(null)
                        ?.setAblumsTitle(null)
                        ?.build()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    override fun onMoveAlbumSuccessful() {}
    override fun getListItems(): MutableList<ItemModel>? {
        return presenter?.mList
    }

    fun onUpdateAdapter(status: EnumStatus?, position: Int) {
        val isVertical: Boolean = PrefsController.getBoolean(getString(R.string.key_vertical_adapter), false)
        when (status) {
            EnumStatus.UPDATE_ENTIRE_ADAPTER -> {
                if (isVertical) {
                    if (verticalAdapter != null) {
                        if (verticalAdapter?.getDataSource() != null) {
                            verticalAdapter?.notifyDataSetChanged()
                        }
                    }
                } else {
                    if (adapter != null) {
                        if (adapter?.getDataSource() != null) {
                            adapter?.notifyDataSetChanged()
                        }
                    }
                }
            }
            EnumStatus.REMOVE_AT_ADAPTER -> {
                if (isVertical) {
                    if (verticalAdapter != null) {
                        if (verticalAdapter?.getDataSource() != null) {
                            if (verticalAdapter?.getDataSource()!!.size > position) {
                                verticalAdapter?.removeAt(position)
                            }
                        }
                    }
                } else {
                    if (adapter != null) {
                        if (adapter?.getDataSource() != null) {
                            if (adapter?.getDataSource()!!.size > position) {
                                adapter?.removeAt(position)
                            }
                        }
                    }
                }
            }
            EnumStatus.UPDATE_AT_ADAPTER -> {
                if (isVertical) {
                    if (verticalAdapter != null) {
                        if (verticalAdapter?.getDataSource() != null) {
                            if (verticalAdapter?.getDataSource()!!.size > position) {
                                verticalAdapter?.notifyItemChanged(position)
                            }
                        }
                    }
                } else {
                    if (adapter != null) {
                        if (adapter?.getDataSource() != null) {
                            if (adapter?.getDataSource()!!.size > position) {
                                adapter?.notifyItemChanged(position)
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
    }
}