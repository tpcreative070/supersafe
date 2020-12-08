package co.tpcreative.supersafe.ui.albumdetail
import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import androidx.core.content.ContextCompat
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseGalleryActivity
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.controller.SingletonPrivateFragment
import co.tpcreative.supersafe.common.extension.deleteDirectory
import co.tpcreative.supersafe.common.helper.EncryptDecryptFilesHelper
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
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class AlbumDetailAct : BaseGalleryActivity() ,AlbumDetailAdapter.ItemSelectedListener {
    var adapter: AlbumDetailAdapter? = null
    var actionMode: ActionMode? = null
    var countSelected = 0
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: EnumStatus?) {
        when (event) {
            EnumStatus.REFRESH -> {
                runOnUiThread {
                    /*Checking after update view*/
                    if (countSelected==0){
                        getData()
                    }
                }
            }
            EnumStatus.FINISH -> {
                Navigator.onMoveToFaceDown(this)
            }
            EnumStatus.UPDATED_VIEW_DETAIL_ALBUM -> {
                runOnUiThread {
                    /*Checking after update view*/
                    if (countSelected==0){
                        getData()
                    }
                }
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
        /*Always check to delete de share folder*/
        SuperSafeApplication.getInstance().getSuperSafeShare().deleteDirectory()
        if (viewModel.isRequestSyncData){
            SingletonPrivateFragment.getInstance()?.onUpdateView()
            ServiceManager.getInstance()?.onPreparingSyncData()
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
                Navigator.onAlbumSettings(this, mainCategory)
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
                        Utils.onBasicAlertNotify(this,"Alert","Can not support to open type of this file")
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
        appbar.setExpanded(false)
        if (actionMode == null) {
            actionMode = toolbar?.startActionMode(callback)
        }
        toggleSelections(position)
        actionMode?.title = (countSelected.toString() + " " + getString(R.string.selected))
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
                            Navigator.onMoveCamera(this@AlbumDetailAct, mainCategory)
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
                    /*Checking after update view*/
                    if (countSelected==0){
                        getData()
                    }
                } else {
                    Utils.Log(TAG, "Nothing to do on Camera")
                }
            }
            Navigator.PHOTO_SLIDE_SHOW -> {
                if (resultCode == Activity.RESULT_OK) {
                    Utils.Log(TAG, "reload data")
                    /*Checking after update view*/
                    if (countSelected==0){
                        getData()
                    }
                } else {
                    Utils.Log(TAG, "Nothing to do on Camera")
                }
            }
            Navigator.REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    Utils.Log(TAG, "Response data here")
                    val mData: MutableList<ImageModel>? = data.getParcelableArrayListExtra(Navigator.INTENT_EXTRA_IMAGES)
                    mData?.let {
                        val mResult = Utils.getDataItemsFromImport(mainCategory,it)
                        importingData(mResult)
                    }
                } else {
                    Utils.Log(TAG, "Nothing to do on Gallery")
                }
            }
            Navigator.SHARE -> {
                if (actionMode != null) {
                    actionMode?.finish()
                }
                /*After shared done*/
                Utils.putScreenStatus(EnumPinAction.SCREEN_UNLOCK.ordinal)
                Utils.Log(TAG, "share action")
            }
            else -> {
                Utils.Log(TAG, "Nothing to do")
            }
        }
    }

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
                viewModel.isSelectAll.postValue(!(viewModel.isSelectAll.value ?: false))
                return true
            }
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            viewModel.isSelectAll.postValue(false)
            actionMode = null
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            speedDial.show()
        }
    }

    /*Gallery action*/
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
                    ?.setCheckBoxColor(-0xc0ae4b)
                    ?.setFakePIN(mainCategory.isFakePin)
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

    override fun onMoveAlbumSuccessful() {
        actionMode?.finish()
        getData()
    }
    override fun getListItems(): MutableList<ItemModel> {
        return dataSource
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