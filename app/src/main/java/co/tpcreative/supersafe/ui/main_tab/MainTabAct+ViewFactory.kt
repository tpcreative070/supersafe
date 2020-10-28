package co.tpcreative.supersafe.ui.main_tab
import android.Manifest
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.text.InputType
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.viewpager.widget.ViewPager
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.controller.PrefsController
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.controller.SingletonPrivateFragment
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.listener.Listener
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.common.utilimport.NetworkUtil
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.ItemModel
import co.tpcreative.supersafe.model.MainCategoryModel
import co.tpcreative.supersafe.model.ThemeApp
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.leinardi.android.speeddial.SpeedDialView
import kotlinx.android.synthetic.main.activity_main_tab.*

fun MainTabAct.initUI(){
    initSpeedDial()
    setSupportActionBar(toolbar)
    toolbar?.inflateMenu(R.menu.main_tab)
    val ab: ActionBar? = supportActionBar
    ab?.setHomeAsUpIndicator(R.drawable.baseline_account_circle_white_24)
    ab?.setDisplayHomeAsUpEnabled(true)
    setupViewPager(viewpager)
    tabs.setupWithViewPager(viewpager)
    PrefsController.putBoolean(getString(R.string.key_running), true)
    presenter = MainTabPresenter()
    presenter?.bindView(this)
    presenter?.onGetUserInfo()
    onShowSuggestion()
    if (Utils.isCheckSyncSuggestion()) {
        onSuggestionSyncData()
    }
    if (presenter!!.mUser != null) {
        if (presenter!!.mUser?.driveConnected!!) {
            if (NetworkUtil.pingIpAddress(this)) {
                return
            }
            Utils.onObserveData(2000, object : Listener {
                override fun onStart() {
                    onAnimationIcon(EnumStatus.DONE)
                }
            })
        }
    }
}


fun MainTabAct.setupViewPager(viewPager: ViewPager?) {
    viewPager?.offscreenPageLimit = 3
    adapter = MainViewPagerAdapter(supportFragmentManager)
    viewPager?.adapter = adapter
}

fun MainTabAct.initSpeedDial() {
    Utils.Log(TAG, "Init floating button")
    val mThemeApp: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
    var drawable: Drawable? = AppCompatResources.getDrawable(applicationContext, R.drawable.baseline_photo_camera_white_24)
    speedDial?.addActionItem(SpeedDialActionItem.Builder(R.id.fab_camera, drawable)
            .setFabBackgroundColor(ResourcesCompat.getColor(resources, mThemeApp?.getPrimaryColor()!!,
                    theme))
            .setLabel(getString(R.string.camera))
            .setLabelColor(Color.WHITE)
            .setLabelBackgroundColor(ResourcesCompat.getColor(resources, R.color.inbox_primary,
                    theme))
            .create())
    drawable = AppCompatResources.getDrawable(applicationContext, R.drawable.baseline_photo_white_24)
    speedDial?.addActionItem(SpeedDialActionItem.Builder(R.id.fab_photo, drawable)
            .setFabBackgroundColor(ResourcesCompat.getColor(resources, mThemeApp?.getPrimaryColor()!!,
                    theme))
            .setLabel(R.string.photo)
            .setLabelColor(ContextCompat.getColor(getActivity()!!,R.color.white))
            .setLabelBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.inbox_primary,
                    theme))
            .create())
    speedDial?.addActionItem(SpeedDialActionItem.Builder(R.id.fab_album, R.drawable.baseline_add_to_photos_white_36)
            .setFabBackgroundColor(ResourcesCompat.getColor(resources, mThemeApp?.getPrimaryColor()!!,
                    theme))
            .setLabel(getString(R.string.album))
            .setLabelColor(ContextCompat.getColor(applicationContext,R.color.white))
            .setLabelBackgroundColor(ResourcesCompat.getColor(resources, R.color.inbox_primary,
                    theme))
            .create())
    speedDial?.show()

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
                R.id.fab_album -> {
                    onShowDialog()
                    return false // false will close it without animation
                }
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
}

fun MainTabAct.onShowDialog() {
    val builder: MaterialDialog = MaterialDialog(this)
            .title(text = getString(R.string.create_album))
            .negativeButton(text = getString(R.string.cancel))
            .positiveButton(text = getString(R.string.ok))
            .input(hint = null, hintRes = null,inputType = InputType.TYPE_CLASS_TEXT) { dialog, input ->
                Utils.Log(TAG, "Value")
                val value = input.toString()
                val base64Code: String = Utils.getHexCode(value)
                val item: MainCategoryModel? = SQLHelper.getTrashItem()
                val result: String? = item?.categories_hex_name
                if (base64Code == result) {
                    Toast.makeText(this@onShowDialog, "This name already existing", Toast.LENGTH_SHORT).show()
                } else {
                    val response: Boolean = SQLHelper.onAddCategories(base64Code, value, false)
                    if (response) {
                        Toast.makeText(this@onShowDialog, "Created album successful", Toast.LENGTH_SHORT).show()
                        ServiceManager.getInstance()?.onPreparingSyncCategoryData()
                    } else {
                        Toast.makeText(this@onShowDialog, "Album name already existing", Toast.LENGTH_SHORT).show()
                    }
                    SingletonPrivateFragment.getInstance()?.onUpdateView()
                }
            }
    builder.show()
}

fun MainTabAct.onAddPermissionCamera() {
    Dexter.withContext(this)
            .withPermissions(
                    Manifest.permission.CAMERA)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report?.areAllPermissionsGranted()!!) {
                        val list: MutableList<MainCategoryModel>? = SQLHelper.getList()
                        if (list != null) {
                            Navigator.onMoveCamera(this@onAddPermissionCamera, list[0])
                        }
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
            .withErrorListener {
                Utils.Log(TAG, "error ask permission")
            }.onSameThread().check()
}

fun MainTabAct.onAlert(content: String) {
    MaterialDialog(this).title(text = "Alert")
            .message(text = content)
            .positiveButton(text = "Ok")
            .positiveButton {
            }
            .show()
}

fun MainTabAct.onShowSuggestion() {
    val isFirstFile: Boolean = PrefsController.getBoolean(getString(R.string.key_is_first_files), false)
    if (!isFirstFile) {
        val mList: MutableList<ItemModel>? = SQLHelper.getListAllItems(false)
        if (mList != null && mList.size > 0) {
            PrefsController.putBoolean(getString(R.string.key_is_first_files), true)
            return
        }
        viewFloatingButton?.setVisibility(View.VISIBLE)
        onSuggestionAddFiles()
    } else {
        val isFirstEnableSyncData: Boolean = PrefsController.getBoolean(getString(R.string.key_is_first_enable_sync_data), false)
        if (!isFirstEnableSyncData) {
            if (presenter?.mUser?.driveConnected!!) {
                PrefsController.putBoolean(getString(R.string.key_is_first_enable_sync_data), true)
            }
            onSuggestionSyncData()
        }
    }
}