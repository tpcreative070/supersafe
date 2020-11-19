package co.tpcreative.supersafe.ui.albumdetail
import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseGalleryActivity
import co.tpcreative.supersafe.common.controller.PrefsController
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.controller.SingletonManagerProcessing
import co.tpcreative.supersafe.common.controller.SingletonPrivateFragment
import co.tpcreative.supersafe.common.helper.EncryptDecryptFilesHelper
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Configuration
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.common.views.NpaGridLayoutManager
import co.tpcreative.supersafe.model.*
import co.tpcreative.supersafe.viewmodel.AlbumDetailViewModel
import com.bumptech.glide.Priority
import com.bumptech.glide.request.RequestOptions
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_album_detail.*
import kotlinx.android.synthetic.main.activity_album_detail.appbar
import kotlinx.android.synthetic.main.activity_album_detail.toolbar
import kotlinx.android.synthetic.main.activity_album_detail.tv_Audios
import kotlinx.android.synthetic.main.activity_album_detail.tv_Others
import kotlinx.android.synthetic.main.activity_album_detail.tv_Photos
import kotlinx.android.synthetic.main.activity_album_detail.tv_Videos
import kotlinx.android.synthetic.main.activity_trash.*
import kotlinx.android.synthetic.main.footer_items_detail_album.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class AlbumDetailAct : BaseGalleryActivity(), BaseView<Int>, AlbumDetailAdapter.ItemSelectedListener, AlbumDetailVerticalAdapter.ItemSelectedListener {
    var presenter: AlbumDetailPresenter? = null
    var adapter: AlbumDetailAdapter? = null
    var verticalAdapter: AlbumDetailVerticalAdapter? = null
    var isReload = false
    var actionMode: ActionMode? = null
    var countSelected = 0
    var isSelectAll = false
    var dialog: AlertDialog? = null
    var mMenuItem: MenuItem? = null
    lateinit var viewModel : AlbumDetailViewModel
    var gridLayoutManager: NpaGridLayoutManager? = null
    var progressing : EnumStepProgressing = EnumStepProgressing.NONE
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
    }

    fun waitingToInit(){
        attachFragment(R.id.gallery_root)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: EnumStatus?) {
        when (event) {
            EnumStatus.REFRESH -> {
                onCallData()
            }
            EnumStatus.FINISH -> {
                Navigator.onMoveToFaceDown(this)
            }
            EnumStatus.UPDATED_VIEW_DETAIL_ALBUM -> {
                CoroutineScope(Dispatchers.Main).launch {
                    presenter?.getData(EnumStatus.RELOAD)
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
                            Utils.onBasicAlertNotify(this, getString(R.string.key_alert), "Exported at " + SuperSafeApplication.getInstance().getSuperSafePicture())
                        }
                        else -> Utils.Log(TAG, "Nothing")
                    }
                } catch (e: Exception) {
                    Utils.Log(TAG, e.message + "")
                }
            }
            EnumStatus.DOWNLOAD_COMPLETED -> {
                SingletonManagerProcessing.getInstance()?.onStopProgressing(this)
                var i = 0
                while (i < presenter?.mList?.size!!) {
                    val items: ItemModel? = presenter?.mList?.get(i)
                    if (items?.isChecked!!) {
                        items.isSaver = false
                    }
                    i++
                }
                onExport()
                Utils.Log(TAG, "already sync ")
            }
            EnumStatus.DOWNLOAD_FAILED -> {
                Utils.onBasicAlertNotify(this, getString(R.string.key_alert), getString(R.string.no_internet_connection))
            }
            else -> Utils.Log(TAG, "Nothing ==> Event bus")
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
        storage?.deleteDirectory(SuperSafeApplication.getInstance().getSuperSafeShare())


        if (viewModel.isRequestSyncData){
            SingletonPrivateFragment.getInstance()?.onUpdateView()
            ServiceManager.getInstance()?.onPreparingSyncData()
            EncryptDecryptFilesHelper.getInstance()?.deleteDirectory(SuperSafeApplication.getInstance().getSuperSafeShare())
        }
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
        mMenuItem = toolbar?.menu?.getItem(0)
        if (Utils.isVertical()) {
            mMenuItem?.icon = ContextCompat.getDrawable(this, R.drawable.baseline_view_comfy_white_48)
        } else {
            mMenuItem?.icon = ContextCompat.getDrawable(this, R.drawable.baseline_format_list_bulleted_white_48)
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
                appbar.setExpanded(false)
                if (actionMode == null) {
                    actionMode = toolbar?.startActionMode(callback)
                }
                countSelected = 0
                actionMode?.title = countSelected.toString() + " " + getString(R.string.selected)
                Utils.Log(TAG, "Action here")
                return true
            }
            R.id.action_view -> {
                if (mMenuItem != null) {
                    switchLayout()
                    switchIcon()
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
//        if (position >= presenter?.mList?.size!!) {
//            return
//        }
//        if (actionMode != null) {
//            toggleSelection(position)
//            actionMode?.title = (countSelected.toString() + " " + getString(R.string.selected))
//        } else {
//            try {
//                when (EnumFormatType.values()[presenter?.mList?.get(position)?.formatType!!]) {
//                    EnumFormatType.FILES -> {
//                        Toast.makeText(getContext(), "Can not support to open type of this file", Toast.LENGTH_SHORT).show()
//                    }
//                    else -> {
//                        presenter?.mainCategories?.let { Navigator.onPhotoSlider(this, presenter?.mList?.get(position)!!, presenter?.mList!!, it) }
//                    }
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }

        if (actionMode != null) {
            toggleSelections(position)
            actionMode?.title = (countSelected.toString() + " " + getString(R.string.selected))
            if (countSelected==0){
                actionMode?.finish()
            }
        }else{
            try {
                when (EnumFormatType.values()[dataSource[position].formatType]) {
                    EnumFormatType.FILES -> {
                        Toast.makeText(getContext(), "Can not support to open type of this file", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        Navigator.onPhotoSlider(this, dataSource[position], dataSource, mainCategory)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onLongClickItem(position: Int) {
        Utils.Log(TAG, "On long clicked item")
//        appbar.setExpanded(false)
//        if (position >= presenter?.mList?.size!!) {
//            return
//        }
//        if (actionMode == null) {
//            actionMode = toolbar?.startActionMode(callback)
//        }
//        toggleSelection(position)
//        actionMode?.title = countSelected.toString() + " " + getString(R.string.selected)

        appbar.setExpanded(false)
        if (actionMode == null) {
            actionMode = toolbar?.startActionMode(callback)
        }
        toggleSelections(position)
        actionMode?.title = (countSelected.toString() + " " + getString(R.string.selected))
    }

    override fun onStartLoading(status: EnumStatus) {}
    override fun onStopLoading(status: EnumStatus) {}
    override fun getContext(): Context? {
        return applicationContext
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Utils.Log(TAG, "Selected album :")
        when (requestCode) {
            Navigator.CAMERA_ACTION -> {
                if (resultCode == Activity.RESULT_OK) {
                    Utils.Log(TAG, "reload data")
                    CoroutineScope(Dispatchers.Main).launch {
                        presenter?.getData(EnumStatus.RELOAD)
                    }
                } else {
                    Utils.Log(TAG, "Nothing to do on Camera")
                }
            }
            Navigator.PHOTO_SLIDE_SHOW -> {
                if (resultCode == Activity.RESULT_OK) {
                    Utils.Log(TAG, "reload data")
                    CoroutineScope(Dispatchers.Main).launch {
                        presenter?.getData(EnumStatus.RELOAD)
                    }
                } else {
                    Utils.Log(TAG, "Nothing to do on Camera")
                }
            }
            Navigator.REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    Utils.Log(TAG, "Response data here")
                    val images: ArrayList<ImageModel>? = data.getParcelableArrayListExtra(Navigator.INTENT_EXTRA_IMAGES)
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
                onPushDataToList()
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
                onPushDataToList()
            }
            EnumStatus.DELETE -> {
                SingletonPrivateFragment.getInstance()?.onUpdateView()
                if (actionMode != null) {
                    actionMode?.finish()
                }
                isReload = true
            }
            else -> Utils.Log(TAG, "Nothing")
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
            else -> Utils.Log(TAG, "Nothing")
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
            menuInflater.inflate(R.menu.menu_select, menu)
            actionMode = mode
            countSelected = 0
            window.statusBarColor = ContextCompat.getColor(applicationContext, R.color.material_orange_900)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            val i = item?.itemId
            if (i == R.id.menu_item_select_all) {
                //isSelectAll = !isSelectAll
                //selectAll()
                viewModel.isSelectAll.postValue(!(viewModel.isSelectAll.value ?: false))
                return true
            }
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
//            if (countSelected > 0) {
//                deselectAll()
//            }
            viewModel.isSelectAll.postValue(false)
            actionMode = null
            window.statusBarColor = android.graphics.Color.TRANSPARENT
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
                                    storage?.deleteFile(items.getOriginal())
                                }
                            }
                            else -> Utils.Log(TAG, "Nothing")
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

    private fun onCheckDelete() {
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
                storage?.deleteDirectory(SuperSafeApplication.Companion.getInstance().getSuperSafePrivate() + mList[i].items_id)
                onUpdateAdapter(EnumStatus.REMOVE_AT_ADAPTER, i)
            }
            i++
        }
        CoroutineScope(Dispatchers.Main).launch {
            presenter?.getData(EnumStatus.REFRESH)
        }
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

    private fun onUpdateAdapter(status: EnumStatus?, position: Int)  = CoroutineScope(Dispatchers.Main).launch{
        val isVertical = Utils.isVertical()
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
            else -> Utils.Log(TAG, "Nothing")
        }
    }

    val dataSource : MutableList<ItemModel>
        get() {
            return adapter?.getDataSource() ?: mutableListOf()
        }
    val mainCategory : MainCategoryModel
        get(){
            return viewModel.mainCategoryModel
        }

}