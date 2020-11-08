package co.tpcreative.supersafe.ui.main_tab
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseGoogleApi
import co.tpcreative.supersafe.common.controller.PrefsController
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.controller.SingletonManager
import co.tpcreative.supersafe.common.controller.SingletonPrivateFragment
import co.tpcreative.supersafe.common.controller.PremiumManager
import co.tpcreative.supersafe.common.dialog.DialogListener
import co.tpcreative.supersafe.common.dialog.DialogManager
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.common.views.AnimationsContainer
import co.tpcreative.supersafe.model.*
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main_tab.*
import kotlinx.android.synthetic.main.activity_main_tab.toolbar
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class MainTabAct : BaseGoogleApi(), BaseView<EmptyModel> {
    var adapter: MainViewPagerAdapter? = null
    var presenter: MainTabPresenter? = null
    var animation: AnimationsContainer.FramesSequenceAnimation? = null
    var mMenuItem: MenuItem? = null
    var previousStatus: EnumStatus? = null
    var mCountToRate = 0
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
                rlOverLay?.visibility = View.INVISIBLE
            }
            EnumStatus.UNLOCK -> {
                rlOverLay?.visibility = View.INVISIBLE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                PremiumManager.getInstance().onStartInAppPurchase()
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
            EnumStatus.EXPIRED_SUBSCRIPTIONS ->{
                DialogManager.getInstance()?.onStartDialog(this,R.string.key_alert,R.string.warning_expired_subscription, object : DialogListener{
                    override fun onClickButton() {
                        Navigator.onMoveToPremium(this@MainTabAct)
                    }
                    override fun dismiss() {
                        Utils.stoppingSaverSpace()
                        Utils.stoppingPremiumFeatures()
                        Utils.putAlreadyAskedExpiration(true)
                    }
                })
            }
            else -> Utils.Log(TAG,"Nothing")
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
                //onEnableSyncData()
                ServiceManager.getInstance()?.waitingForResult()
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
                    SingletonManager.getInstance().setReloadMainTab(true)
                    Navigator.onMoveToMainTab(this,true)
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
                    val images: ArrayList<ImageModel>? = data.getParcelableArrayListExtra(Navigator.INTENT_EXTRA_IMAGES)
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
        mMenuItem = toolbar?.menu?.getItem(0)
        return true
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
        if (speedDial?.isOpen!!) {
            speedDial?.close()
        } else {
            PremiumManager.getInstance().onStop()
            Utils.onDeleteTemporaryFile()
            Utils.onExportAndImportFile(SuperSafeApplication.getInstance().getSuperSafeDataBaseFolder(), SuperSafeApplication.getInstance().getSuperSafeBackup(), object : ServiceManager.ServiceManagerSyncDataListener {
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

    /*MainTab View*/
    override fun onStartLoading(status: EnumStatus) {}
    override fun onStopLoading(status: EnumStatus) {}
    override fun getContext(): Context? {
        return applicationContext
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
                        viewFloatingButton?.visibility = View.GONE
                        PrefsController.putBoolean(getString(R.string.key_is_first_files), true)
                    }

                    override fun onOuterCircleClick(view: TapTargetView?) {
                        super.onOuterCircleClick(view)
                        PrefsController.putBoolean(getString(R.string.key_is_first_files), true)
                        view?.dismiss(true)
                        viewFloatingButton?.visibility = View.GONE
                        Utils.Log(TAG, "onOuterCircleClick")
                    }

                    override fun onTargetDismissed(view: TapTargetView?, userInitiated: Boolean) {
                        super.onTargetDismissed(view, userInitiated)
                        PrefsController.putBoolean(getString(R.string.key_is_first_files), true)
                        view?.dismiss(true)
                        viewFloatingButton?.visibility = View.GONE
                        Utils.Log(TAG, "onTargetDismissed")
                    }

                    override fun onTargetCancel(view: TapTargetView?) {
                        super.onTargetCancel(view)
                        PrefsController.putBoolean(getString(R.string.key_is_first_files), true)
                        view?.dismiss(true)
                        viewFloatingButton?.visibility = View.GONE
                        Utils.Log(TAG, "onTargetCancel")
                    }
                })
    }

    companion object {
        private val TAG = MainTabAct::class.java.simpleName
    }
}