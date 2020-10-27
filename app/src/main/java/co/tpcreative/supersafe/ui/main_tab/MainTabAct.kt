package co.tpcreative.supersafe.ui.main_tab
import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.viewpager.widget.ViewPager
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseGoogleApi
import co.tpcreative.supersafe.common.controller.PrefsController
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.controller.SingletonManager
import co.tpcreative.supersafe.common.controller.SingletonPrivateFragment
import co.tpcreative.supersafe.common.controllerimport.PremiumManager
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.listener.Listener
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.common.utilimport.NetworkUtil
import co.tpcreative.supersafe.common.views.AnimationsContainer
import co.tpcreative.supersafe.model.*
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import com.google.android.gms.ads.InterstitialAd
import com.google.gson.Gson
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.leinardi.android.speeddial.SpeedDialView
import kotlinx.android.synthetic.main.activity_main_tab.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class MainTabAct : BaseGoogleApi(), BaseView<EmptyModel> {
    var adapter: MainViewPagerAdapter? = null
    var presenter: MainTabPresenter? = null
    var animation: AnimationsContainer.FramesSequenceAnimation? = null
    private var menuItem: MenuItem? = null
    private var previousStatus: EnumStatus? = null
    private val mInterstitialAd: InterstitialAd? = null
    private var mCountToRate = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_tab)
        initUI()
        Utils.Log(TAG, "system access token : " + Utils.getAccessToken())
    }

    override fun onOrientationChange(isFaceDown: Boolean) {
        Utils.Log(TAG, "onOrientationChange")
        onFaceDown(isFaceDown)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: EnumStatus?) {
        when (event) {
            EnumStatus.REGISTER_OR_LOGIN -> {
                rlOverLay?.setVisibility(View.INVISIBLE)
            }
            EnumStatus.UNLOCK -> {
                rlOverLay?.setVisibility(View.INVISIBLE)
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }
            EnumStatus.FINISH -> {
                Navigator.onMoveToFaceDown(this)
            }
            EnumStatus.PRIVATE_DONE -> {
                if (speedDial != null) {
                    speedDial?.show()
                }
            }
            EnumStatus.DOWNLOAD -> {
                runOnUiThread(Runnable {
                    Utils.Log(TAG, "sync value " + event.name)
                    onAnimationIcon(event)
                })
            }
            EnumStatus.UPLOAD -> {
                runOnUiThread(Runnable {
                    Utils.Log(TAG, "sync value " + event.name)
                    onAnimationIcon(event)
                })
            }
            EnumStatus.DONE -> {
                runOnUiThread(Runnable {
                    Utils.Log(TAG, "sync value " + event.name)
                    onAnimationIcon(event)
                })
            }
            EnumStatus.SYNC_ERROR -> {
                runOnUiThread(Runnable {
                    Utils.Log(TAG, "sync value " + event.name)
                    onAnimationIcon(event)
                })
            }
            EnumStatus.REQUEST_ACCESS_TOKEN -> {
                runOnUiThread(Runnable {
                    Utils.Log(TAG, "Request token")
                    getAccessToken()
                })
            }
            EnumStatus.SHOW_FLOATING_BUTTON -> {
                runOnUiThread(Runnable { speedDial?.show() })
            }
            EnumStatus.HIDE_FLOATING_BUTTON -> {
                runOnUiThread(Runnable { speedDial?.hide() })
            }
            EnumStatus.CONNECTED -> {
                getAccessToken()
            }
            EnumStatus.NO_SPACE_LEFT -> {
                onAlert(getString(R.string.key_no_space_left_space))
            }
            EnumStatus.NO_SPACE_LEFT_CLOUD -> {
                onAlert(getString(R.string.key_no_space_left_space_cloud))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        onCallLockScreen()
        onRegisterHomeWatcher()
        presenter?.onGetUserInfo()
        ServiceManager.getInstance()?.setRequestShareIntent(false)
        Utils.Log(TAG, "onResume")
    }

    override fun onDestroy() {
        super.onDestroy()
        Utils.Log(TAG, "OnDestroy")
        Utils.onUpdatedCountRate()
        EventBus.getDefault().unregister(this)
        PrefsController.putBoolean(getString(R.string.second_loads), true)
        if (SingletonManager.getInstance().isReloadMainTab()) {
            SingletonManager.getInstance().setReloadMainTab(false)
        } else {
            ServiceManager.getInstance()?.onDismissServices()
        }
    }

    override fun onStopListenerAWhile() {
        EventBus.getDefault().unregister(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                Utils.Log(TAG, "Home action")
                if (presenter != null) {
                    if (presenter?.mUser?.verified!!) {
                        Navigator.onManagerAccount(getActivity()!!)
                    } else {
                        Navigator.onVerifyAccount(getActivity()!!)
                    }
                }
                return true
            }
            R.id.action_sync -> {
                onEnableSyncData()
                return true
            }
            R.id.settings -> {
                Navigator.onSettings(this)
                return true
            }
            R.id.help -> {
                Navigator.onMoveHelpSupport(this)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Utils.Log(TAG, "Selected album :")
        when (requestCode) {
            Navigator.COMPLETED_RECREATE -> {
                if (resultCode == Activity.RESULT_OK) {
                    SingletonManager.Companion.getInstance().setReloadMainTab(true)
                    Navigator.onMoveToMainTab(this)
                    Utils.Log(TAG, "New Activity")
                } else {
                    Utils.Log(TAG, "Nothing Updated theme")
                }
            }
            Navigator.CAMERA_ACTION -> {
                if (resultCode == Activity.RESULT_OK) {
                    Utils.Log(TAG, "reload data")
                    SingletonPrivateFragment.getInstance()?.onUpdateView()
                } else {
                    Utils.Log(TAG, "Nothing to do on Camera")
                }
            }
            Navigator.PHOTO_SLIDE_SHOW -> {
                if (resultCode == Activity.RESULT_OK) {
                    Utils.Log(TAG, "reload data")
                    SingletonPrivateFragment.getInstance()?.onUpdateView()
                } else {
                    Utils.Log(TAG, "Nothing to do on Camera")
                }
            }
            Navigator.REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val images: ArrayList<Image>? = data.getParcelableArrayListExtra(Navigator.INTENT_EXTRA_IMAGES)
                    val mListImportFiles: MutableList<ImportFilesModel> = ArrayList<ImportFilesModel>()
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
                            val list: MutableList<MainCategoryModel>? = SQLHelper.getList()
                            if (list == null) {
                                Utils.onWriteLog("Main categories is null", EnumStatus.WRITE_FILE)
                                return
                            }
                            val mCategory: MainCategoryModel? = list[0]
                            Utils.Log(TAG, "Show category " + Gson().toJson(mCategory))
                            val importFiles = ImportFilesModel(list[0], mimeTypeFile, path, i, false)
                            mListImportFiles.add(importFiles)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        i++
                    }
                    ServiceManager.getInstance()?.setListImport(mListImportFiles)
                    ServiceManager.getInstance()?.onPreparingImportData()
                } else {
                    Utils.Log(TAG, "Nothing to do on Gallery")
                }
            }
            else -> {
                Utils.Log(TAG, "Nothing to do")
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (toolbar == null) {
            return false
        }
        toolbar?.inflateMenu(R.menu.main_tab)
        menuItem = toolbar?.getMenu()?.getItem(0)
        return true
    }

    private fun getMenuItem(): MenuItem? {
        return menuItem
    }

    override fun onPause() {
        super.onPause()
        val user: User? = Utils.getUserInfo()
        if (user != null) {
            if (!user.driveConnected) {
                onAnimationIcon(EnumStatus.SYNC_ERROR)
            }
        }
    }

    override fun onBackPressed() {
        if (speedDial?.isOpen()!!) {
            speedDial?.close()
        } else {
            PremiumManager.getInstance().onStop()
            Utils.onDeleteTemporaryFile()
            Utils.onExportAndImportFile(SuperSafeApplication.getInstance().getSupersafeDataBaseFolder(), SuperSafeApplication.getInstance().getSupersafeBackup(), object : ServiceManager.ServiceManagerSyncDataListener {
                override fun onCompleted() {
                    Utils.Log(TAG, "Exporting successful")
                }
                override fun onError() {
                    Utils.Log(TAG, "Exporting error")
                }
                override fun onCancel() {}
            })
            SuperSafeApplication.getInstance().writeUserSecret(presenter?.mUser)
            val isPressed: Boolean = PrefsController.getBoolean(getString(R.string.we_are_a_team), false)
            if (isPressed) {
                super.onBackPressed()
            } else {
                val isSecondLoad: Boolean = PrefsController.getBoolean(getString(R.string.second_loads), false)
                if (isSecondLoad) {
                    val isPositive: Boolean = PrefsController.getBoolean(getString(R.string.we_are_a_team_positive), false)
                    mCountToRate = PrefsController.getInt(getString(R.string.key_count_to_rate), 0)
                    if (!isPositive && mCountToRate > Utils.COUNT_RATE) {
                        onAskingRateApp()
                    } else {
                        super.onBackPressed()
                    }
                } else {
                    super.onBackPressed()
                }
            }
        }
    }

    fun onAnimationIcon(status: EnumStatus?) {
        Utils.Log(TAG, "value : " + status?.name)
        if (getMenuItem() == null) {
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
        val item = getMenuItem()
        if (animation != null) {
            animation?.stop()
        }
        Utils.Log(TAG, "Calling AnimationsContainer........................")
        Utils.onWriteLog("Calling AnimationsContainer", EnumStatus.CREATE)
        previousStatus = status
        animation = AnimationsContainer.getInstance()?.createSplashAnim(item, status)
        animation?.start()
    }

    /*MainTab View*/
    override fun onStartLoading(status: EnumStatus) {}
    override fun onStopLoading(status: EnumStatus) {}
    override fun getContext(): Context? {
        return getApplicationContext()
    }

    override fun onDriveClientReady() {}
    override fun isSignIn(): Boolean {
        return false
    }

    override fun onDriveSuccessful() {
        onCheckRequestSignOut()
    }

    override fun onDriveError() {}
    override fun onDriveSignOut() {}
    override fun onDriveRevokeAccess() {}
    override fun onError(message: String?, status: EnumStatus?) {}
    override fun onError(message: String?) {}
    override fun onSuccessful(message: String?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?) {}
    override fun getActivity(): Activity? {
        return this
    }

    override fun onSuccessful(message: String?, status: EnumStatus?, `object`: EmptyModel?) {}
    override fun startServiceNow() {}
    override fun onSuccessful(message: String?, status: EnumStatus?, list: MutableList<EmptyModel>?) {}
    fun onSuggestionAddFiles() {
        TapTargetView.showFor(this,  // `this` is an Activity
                TapTarget.forView(viewFloatingButton, getString(R.string.tap_here_to_add_items), getString(R.string.tap_here_to_add_items_description))
                        .titleTextSize(25)
                        .titleTextColor(R.color.white)
                        .descriptionTextColor(R.color.md_light_blue_200)
                        .descriptionTextSize(17)
                        .outerCircleColor(R.color.colorPrimary)
                        .transparentTarget(true)
                        .targetCircleColor(R.color.white)
                        .cancelable(true)
                        .transparentTarget(true)
                        .dimColor(R.color.transparent),
                object : TapTargetView.Listener() {
                    // The listener can listen for regular clicks, long clicks or cancels
                    override fun onTargetClick(view: TapTargetView?) {
                        super.onTargetClick(view) // This call is optional
                        speedDial?.open()
                        view?.dismiss(true)
                        viewFloatingButton?.setVisibility(View.GONE)
                        PrefsController.putBoolean(getString(R.string.key_is_first_files), true)
                    }

                    override fun onOuterCircleClick(view: TapTargetView?) {
                        super.onOuterCircleClick(view)
                        PrefsController.putBoolean(getString(R.string.key_is_first_files), true)
                        view?.dismiss(true)
                        viewFloatingButton?.setVisibility(View.GONE)
                        Utils.Log(TAG, "onOuterCircleClick")
                    }

                    override fun onTargetDismissed(view: TapTargetView?, userInitiated: Boolean) {
                        super.onTargetDismissed(view, userInitiated)
                        PrefsController.putBoolean(getString(R.string.key_is_first_files), true)
                        view?.dismiss(true)
                        viewFloatingButton?.setVisibility(View.GONE)
                        Utils.Log(TAG, "onTargetDismissed")
                    }

                    override fun onTargetCancel(view: TapTargetView?) {
                        super.onTargetCancel(view)
                        PrefsController.putBoolean(getString(R.string.key_is_first_files), true)
                        view?.dismiss(true)
                        viewFloatingButton?.setVisibility(View.GONE)
                        Utils.Log(TAG, "onTargetCancel")
                    }
                })
    }

    fun onSuggestionSyncData() {
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

    fun onEnableSyncData() {
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

    fun onAskingRateApp() {
        val inflater: LayoutInflater = getContext()?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.custom_view_rate_app_dialog, null)
        val happy: AppCompatTextView? = view.findViewById<AppCompatTextView?>(R.id.tvHappy)
        val unhappy: AppCompatTextView? = view.findViewById<AppCompatTextView?>(R.id.tvUnhappy)
        val builder: MaterialDialog.Builder = MaterialDialog.Builder(this)
                .title(getString(R.string.how_are_we_doing))
                .customView(view, true)
                .theme(Theme.LIGHT)
                .cancelable(true)
                .titleColor(getResources().getColor(R.color.black))
                .positiveText(getString(R.string.i_love_it))
                .negativeText(getString(R.string.report_problem))
                .neutralText(getString(R.string.no_thanks))
                .onNeutral(object : MaterialDialog.SingleButtonCallback {
                    override fun onClick(dialog: MaterialDialog, which: DialogAction) {
                        PrefsController.putBoolean(getString(R.string.we_are_a_team), true)
                        finish()
                    }
                })
                .onNegative(object : MaterialDialog.SingleButtonCallback {
                    override fun onClick(dialog: MaterialDialog, which: DialogAction) {
                        val categories = Categories(1, getString(R.string.contact_support))
                        val support = HelpAndSupport(categories, getString(R.string.contact_support), getString(R.string.contact_support_content), null)
                        Navigator.onMoveReportProblem(getContext()!!, support)
                        PrefsController.putBoolean(getString(R.string.we_are_a_team), true)
                    }
                })
                .onPositive(object : MaterialDialog.SingleButtonCallback {
                    override fun onClick(dialog: MaterialDialog, which: DialogAction) {
                        Utils.Log(TAG, "Positive")
                        onRateApp()
                        PrefsController.putBoolean(getString(R.string.we_are_a_team), true)
                        PrefsController.putBoolean(getString(R.string.we_are_a_team_positive), true)
                    }
                })
        builder.build().show()
    }

    fun onRateApp() {
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

    companion object {
        private val TAG = MainTabAct::class.java.simpleName
    }
}