package co.tpcreative.supersafe.ui.main_tab
import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.net.Uri
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatTextView
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
import co.tpcreative.supersafe.common.views.AnimationsContainer
import co.tpcreative.supersafe.model.*
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.leinardi.android.speeddial.SpeedDialView
import kotlinx.android.synthetic.main.activity_main_tab.*

fun MainTabAct.initUI(){
    TAG = this::class.java.simpleName
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
            .setFabImageTintColor(ContextCompat.getColor(this, R.color.white))
            .setLabelColor(Color.WHITE)
            .setLabelBackgroundColor(ResourcesCompat.getColor(resources, R.color.inbox_primary,
                    theme))
            .create())
    drawable = AppCompatResources.getDrawable(applicationContext, R.drawable.baseline_photo_white_24)
    speedDial?.addActionItem(SpeedDialActionItem.Builder(R.id.fab_photo, drawable)
            .setFabBackgroundColor(ResourcesCompat.getColor(resources, mThemeApp?.getPrimaryColor()!!,
                    theme))
            .setLabel(R.string.photo)
            .setFabImageTintColor(ContextCompat.getColor(this, R.color.white))
            .setLabelColor(ContextCompat.getColor(getActivity()!!, R.color.white))
            .setLabelBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.inbox_primary,
                    theme))
            .create())
    speedDial?.addActionItem(SpeedDialActionItem.Builder(R.id.fab_album, R.drawable.baseline_add_to_photos_white_36)
            .setFabBackgroundColor(ResourcesCompat.getColor(resources, mThemeApp?.getPrimaryColor()!!,
                    theme))
            .setLabel(getString(R.string.album))
            .setFabImageTintColor(ContextCompat.getColor(this, R.color.white))
            .setLabelColor(ContextCompat.getColor(applicationContext, R.color.white))
            .setLabelBackgroundColor(ResourcesCompat.getColor(resources, R.color.inbox_primary,
                    theme))
            .create())
    speedDial.mainFab.setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_IN)
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
            .input(hint = null, hintRes = R.string.enter_name, inputType = InputType.TYPE_CLASS_TEXT) { dialog, input ->
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
        viewFloatingButton?.visibility = View.VISIBLE
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


fun MainTabAct.onAskingRateApp() {
    val inflater: LayoutInflater = getContext()?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val view: View = inflater.inflate(R.layout.custom_view_rate_app_dialog, null)
    val happy: AppCompatTextView? = view.findViewById<AppCompatTextView?>(R.id.tvHappy)
    val unhappy: AppCompatTextView? = view.findViewById<AppCompatTextView?>(R.id.tvUnhappy)
    val builder: MaterialDialog = MaterialDialog(this)
            .title(text = getString(R.string.how_are_we_doing))
            .customView(view = view, scrollable = true)
            .cancelable(true)
            .positiveButton(text = getString(R.string.i_love_it))
            .negativeButton(text = getString(R.string.report_problem))
            .neutralButton(text = getString(R.string.no_thanks))
            .neutralButton {
                PrefsController.putBoolean(getString(R.string.we_are_a_team), true)
                finish()
            }
            .negativeButton {
                val categories = Categories(1, getString(R.string.contact_support))
                val support = HelpAndSupport(categories, getString(R.string.contact_support), getString(R.string.contact_support_content), null)
                Navigator.onMoveReportProblem(getContext()!!, support)
                PrefsController.putBoolean(getString(R.string.we_are_a_team), true)
            }
            .positiveButton {
                Utils.Log(TAG, "Positive")
                onRateApp()
                PrefsController.putBoolean(getString(R.string.we_are_a_team), true)
                PrefsController.putBoolean(getString(R.string.we_are_a_team_positive), true)
            }
    builder.show()
}

fun MainTabAct.onRateApp() {
    val uri = Uri.parse("market://details?id=" + getString(R.string.supersafe_live))
    val goToMarket = Intent(Intent.ACTION_VIEW, uri)
    // To count with Play market backstack, After pressing back button,
    // to taken back to our application, we need to add following flags to intent.
    goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
            Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
            Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
    try {
        startActivity(goToMarket)
    } catch (e: ActivityNotFoundException) {
        startActivity(Intent(Intent.ACTION_VIEW,
                Uri.parse("http://play.google.com/store/apps/details?id=" + getString(R.string.supersafe_live))))
    }
}

fun MainTabAct.onSuggestionSyncData() {
    TapTargetView.showFor(this,  // `this` is an Activity
            TapTarget.forToolbarMenuItem(toolbar, R.id.action_sync, getString(R.string.tap_here_to_enable_sync_data), getString(R.string.tap_here_to_enable_sync_data_description))
                    .titleTextSize(25)
                    .titleTextColor(R.color.white)
                    .descriptionTextColor(R.color.colorPrimary)
                    .descriptionTextSize(17)
                    .outerCircleColor(R.color.colorButton)
                    .transparentTarget(true)
                    .targetCircleColor(R.color.white)
                    .cancelable(true)
                    .dimColor(R.color.white),
            object : TapTargetView.Listener() {
                // The listener can listen for regular clicks, long clicks or cancels
                override fun onTargetClick(view: TapTargetView?) {
                    super.onTargetClick(view) // This call is optional
                    onEnableSyncData()
                    view?.dismiss(true)
                    PrefsController.putBoolean(getString(R.string.key_is_first_enable_sync_data), true)
                    Utils.Log(TAG, "onTargetClick")
                }

                override fun onOuterCircleClick(view: TapTargetView?) {
                    super.onOuterCircleClick(view)
                    PrefsController.putBoolean(getString(R.string.key_is_first_enable_sync_data), true)
                    view?.dismiss(true)
                    Utils.Log(TAG, "onOuterCircleClick")
                }

                override fun onTargetDismissed(view: TapTargetView?, userInitiated: Boolean) {
                    super.onTargetDismissed(view, userInitiated)
                    PrefsController.putBoolean(getString(R.string.key_is_first_enable_sync_data), true)
                    view?.dismiss(true)
                    Utils.Log(TAG, "onTargetDismissed")
                }

                override fun onTargetCancel(view: TapTargetView?) {
                    super.onTargetCancel(view)
                    PrefsController.putBoolean(getString(R.string.key_is_first_enable_sync_data), true)
                    view?.dismiss(true)
                    Utils.Log(TAG, "onTargetCancel")
                }
            })
}

fun MainTabAct.onEnableSyncData() {
    val mUser: User? = Utils.getUserInfo()
    if (mUser != null) {
        if (mUser.verified) {
            if (!mUser.driveConnected) {
                Navigator.onCheckSystem(this, null)
            } else {
                Navigator.onManagerCloud(this)
            }
        } else {
            Navigator.onVerifyAccount(this)
        }
    }
}

fun MainTabAct.onAnimationIcon(status: EnumStatus?) {
    Utils.Log(TAG, "value : " + status?.name)
    if (mMenuItem == null) {
        Utils.Log(TAG, "Menu is nulll")
        return
    }
    if (previousStatus == status && status == EnumStatus.DOWNLOAD) {
        Utils.Log(TAG, "Action here 1")
        return
    }
    if (previousStatus == status && status == EnumStatus.UPLOAD) {
        Utils.Log(TAG, "Action here 2")
        return
    }
    val item = mMenuItem
    if (animation != null) {
        animation?.stop()
    }
    Utils.Log(TAG, "Calling AnimationsContainer........................")
    Utils.onWriteLog("Calling AnimationsContainer", EnumStatus.CREATE)
    previousStatus = status
    animation = AnimationsContainer.getInstance()?.createSplashAnim(item, status)
    animation?.start()
}