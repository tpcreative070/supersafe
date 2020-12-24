package co.tpcreative.supersafe.ui.main_tab
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseGoogleApi
import co.tpcreative.supersafe.common.controller.PremiumManager
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.controller.SingletonManager
import co.tpcreative.supersafe.common.controller.SingletonPrivateFragment
import co.tpcreative.supersafe.common.dialog.DialogListener
import co.tpcreative.supersafe.common.dialog.DialogManager
import co.tpcreative.supersafe.common.extension.*
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.common.views.AnimationsContainer
import co.tpcreative.supersafe.model.*
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.tasks.Task
import kotlinx.android.synthetic.main.activity_main_tab.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class MainTabAct : BaseGoogleApi(){
    var adapter: MainViewPagerAdapter? = null
    var animation: AnimationsContainer.FramesSequenceAnimation? = null
    var mMenuItem: MenuItem? = null
    var previousStatus: EnumStatus? = null
    var isRequestRating = true
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
            EnumStatus.UNLOCK -> {
                PremiumManager.getInstance().onStartInAppPurchase()
                CoroutineScope(Dispatchers.Main).launch {
                    if (Utils.getCountToRate() > Utils.COUNT_RATE) {
                        if (isRequestRating){
                            reviewInApp()
                            isRequestRating = false
                            Utils.Log(TAG,"Call review...")
                        }
                    }
                }
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
            EnumStatus.EXPIRED_SUBSCRIPTIONS -> {
                DialogManager.getInstance()?.onStartDialog(this, R.string.key_alert, R.string.warning_expired_subscription, object : DialogListener {
                    override fun onClickButton() {
                        Navigator.onMoveToPremium(this@MainTabAct)
                    }

                    override fun dismiss() {
                        Utils.stoppingPremiumFeatures()
                    }
                })
            }
            else -> Utils.Log(TAG, "Nothing")
        }
    }

    private fun reviewInApp(){
        val manager = ReviewManagerFactory.create(this)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { request ->
            if (request.isSuccessful) {
                // We got the ReviewInfo object
                val reviewInfo = request.result
                val flow: Task<Void> = manager.launchReviewFlow(this, reviewInfo)
                flow.addOnCompleteListener { tasks ->
                    Utils.Log(TAG,"Review completed")
                }
            } else {
                // There was some problem, continue regardless of the result.
                Utils.Log(TAG,"Nothing")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Utils.Log(TAG, "onResume")
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        onRegisterHomeWatcher()
        ServiceManager.getInstance()?.setRequestShareIntent(false)
        if (Utils.isRequestSyncData() || Utils.isRequestUpload()){
            ServiceManager.getInstance()?.onPreparingSyncData()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Utils.Log(TAG, "OnDestroy")
        Utils.onUpdatedCountRate()
        EventBus.getDefault().unregister(this)
        if (SingletonManager.getInstance().isReloadMainTab()) {
            SingletonManager.getInstance().setReloadMainTab(false)
        } else {
            ServiceManager.getInstance()?.onDismissServices()
        }
    }

    override fun onStart() {
        super.onStart()
        Utils.Log(TAG, "onStart !!!!")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                Utils.Log(TAG, "Home action")
                if (Utils.isVerifiedAccount()) {
                    Navigator.onManagerAccount(this)
                } else {
                    Navigator.onVerifyAccount(this)
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
                    SingletonManager.getInstance().setReloadMainTab(true)
                    Navigator.onMoveToMainTab(this, true)
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
                    val mData: ArrayList<ImageModel>? = data.getParcelableArrayListExtra(Navigator.INTENT_EXTRA_IMAGES)
                    mData?.let {
                        val list: MutableList<MainCategoryModel> = SQLHelper.getList()
                        val mCategory: MainCategoryModel = list[0]
                        val mResult = mCategory.let { it1 -> Utils.getDataItemsFromImport(it1, it) }
                        importingData(mResult)
                    }
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
        Utils.Log(TAG, "Created menu")
        return true
    }

    override fun onPause() {
        Utils.Log(TAG, "onResume ->putHomePressed")
        super.onPause()
        if (!Utils.isConnectedToGoogleDrive()){
            onAnimationIcon(EnumStatus.SYNC_ERROR)
            Utils.Log(TAG, " onAnimationIcon(EnumStatus.SYNC_ERROR)")
        }
    }

    override fun onStop() {
        Utils.Log(TAG, "onResume ->putHomePressed")
        super.onStop()
    }

    override fun onBackPressed() {
        if (speedDial?.isOpen!!) {
            speedDial?.close()
        } else {
            PremiumManager.getInstance().onStop()
            Utils.onDeleteTemporaryFile()
            Navigator.onMoveSeeYou(this)
            super.onBackPressed()
        }
    }

    /*MainTab View*/
    override fun onDriveClientReady() {}
    override fun isSignIn(): Boolean {
        return false
    }

    override fun onDriveSuccessful() {
        onCheckRequestSignOut()
    }

    override fun onDriveError() {}
    override fun onDriveSignOut() {
        Navigator.onCheckSystem(this, null)
    }
    override fun onDriveRevokeAccess() {}
    override fun startServiceNow() {}
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
                        Utils.putFirstFiles(true)
                    }

                    override fun onOuterCircleClick(view: TapTargetView?) {
                        super.onOuterCircleClick(view)
                        Utils.putFirstFiles(true)
                        view?.dismiss(true)
                        viewFloatingButton?.visibility = View.GONE
                        Utils.Log(TAG, "onOuterCircleClick")
                    }

                    override fun onTargetDismissed(view: TapTargetView?, userInitiated: Boolean) {
                        super.onTargetDismissed(view, userInitiated)
                        Utils.putFirstFiles(true)
                        view?.dismiss(true)
                        viewFloatingButton?.visibility = View.GONE
                        Utils.Log(TAG, "onTargetDismissed")
                    }

                    override fun onTargetCancel(view: TapTargetView?) {
                        super.onTargetCancel(view)
                        Utils.putFirstFiles(true)
                        view?.dismiss(true)
                        viewFloatingButton?.visibility = View.GONE
                        Utils.Log(TAG, "onTargetCancel")
                    }
                })
    }
}