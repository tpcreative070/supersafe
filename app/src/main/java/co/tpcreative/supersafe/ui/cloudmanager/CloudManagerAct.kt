package co.tpcreative.supersafe.ui.cloudmanager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.CompoundButton
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseGoogleApi
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.*
import co.tpcreative.supersafe.viewmodel.CloudManagerViewModel
import kotlinx.android.synthetic.main.activity_cloud_manager.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class CloudManagerAct : BaseGoogleApi(), CompoundButton.OnCheckedChangeListener{
//    var presenter: CloudManagerPresenter? = null
    var isPauseCloudSync = true
    var isDownload = false
    var isSaverSpace = false
    var isRefresh = false
    private val isPreviousSaverSpace  = Utils.getSaverSpace()
    lateinit var viewModel : CloudManagerViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cloud_manager)
        initUI()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_cloud_manager, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_item_refresh -> {
                isRefresh = true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCheckedChanged(compoundButton: CompoundButton?, b: Boolean) {
        when (compoundButton?.id) {
            R.id.btnSwitchPauseSync -> {
                isPauseCloudSync = b
                Utils.pauseSync(b)
            }
            R.id.switch_SaveSpace -> {
                if (!Utils.isPremium()) {
                    onShowPremium()
                    Utils.putSaverSpace(false)
                    switch_SaveSpace?.isChecked = false
                    return
                }
                if (b) {
                    /*Checking Google Drive connecting*/
                    if(!Utils.isConnectedToGoogleDrive()){
                        Utils.showDialog(this,R.string.need_signed_in_to_google_drive_before_using_this_feature, object : ServiceManager.ServiceManagerSyncDataListener {
                            override fun onCompleted() {
                                Navigator.onCheckSystem(this@CloudManagerAct, null)
                            }
                            override fun onError() {
                            }
                            override fun onCancel() {
                            }
                        })
                        return
                    }
                    isDownload = false
                    isSaverSpace = true
                    enableSaverSpace()
                } else {
                    isSaverSpace = false
                    disableSaverSpace(EnumStatus.GET_LIST_FILE)
                }
                Utils.putSaverSpace(b)
            }
        }
        Utils.Log(TAG, "onCheckedChanged...............isDownload $isDownload isSaverSapce $isSaverSpace")
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: EnumStatus?) {
        when (event) {
            EnumStatus.FINISH -> {
                Navigator.onMoveToFaceDown(this)
            }
            else -> Utils.Log(TAG,"Nothing")
        }
    }

    override fun onResume() {
        super.onResume()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        onRegisterHomeWatcher()
    }

    override fun onDestroy() {
        super.onDestroy()
        Utils.Log(TAG, "OnDestroy")
        EventBus.getDefault().unregister(this)
        /*Check disable saver func*/
        if (isDownload && isPreviousSaverSpace != Utils.getSaverSpace()) {
            /*Stop saver saver*/
            Utils.stopSaverSpace()
            Utils.Log(TAG, "stop saver space")
        }
        if (isSaverSpace && isPreviousSaverSpace != Utils.getSaverSpace()) {
            /*Start saver space*/
            viewModel.startSaverSpace()
            Utils.Log(TAG, "start saver space")
        }
        if (isRefresh || isPreviousSaverSpace != Utils.getSaverSpace() || !isPauseCloudSync) {
            ServiceManager.getInstance()?.onPreparingSyncData()
        }
    }

    override fun onOrientationChange(isFaceDown: Boolean) {
        onFaceDown(isFaceDown)
    }

    override fun onDriveError() {
        Utils.Log(TAG, "onDriveError")
    }

    override fun onDriveSignOut() {
        Utils.Log(TAG, "onDriveSignOut")
    }

    override fun onDriveRevokeAccess() {
        Utils.Log(TAG, "onDriveRevokeAccess")
    }

    override fun onDriveClientReady() {}
    override fun isSignIn(): Boolean {
        return false
    }

    override fun onDriveSuccessful() {}
    override fun startServiceNow() {}

    companion object {
        private val TAG = CloudManagerAct::class.java.simpleName
    }
}