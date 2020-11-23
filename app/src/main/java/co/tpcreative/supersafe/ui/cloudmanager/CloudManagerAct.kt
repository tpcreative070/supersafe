package co.tpcreative.supersafe.ui.cloudmanager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.CompoundButton
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseGoogleApi
import co.tpcreative.supersafe.common.controller.PrefsController
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.*
import co.tpcreative.supersafe.viewmodel.CloudManagerViewModel
import com.snatik.storage.Storage
import kotlinx.android.synthetic.main.activity_cloud_manager.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class CloudManagerAct : BaseGoogleApi(), CompoundButton.OnCheckedChangeListener{
//    var presenter: CloudManagerPresenter? = null
    var isPauseCloudSync = true
    var isDownload = false
    var isSpaceSaver = false
    var storage: Storage? = null
    var isRefresh = false
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
        Utils.Log(TAG, "onCheckedChanged...............!!!")
        when (compoundButton?.getId()) {
            R.id.btnSwitchPauseSync -> {
                isPauseCloudSync = b
                Utils.pauseSync(b)
            }
            R.id.switch_SaveSpace -> {
                if (!Utils.isPremium()) {
                    onShowPremium()
                    Utils.putSaverSpace(false)
                    switch_SaveSpace?.isChecked = false
                }
                if (b) {
                    isDownload = false
                    isSpaceSaver = true
                    enableSaverSpace()
                } else {
                    isSpaceSaver = false
                    disableSaverSpace(EnumStatus.GET_LIST_FILE)
                }
                Utils.putSaverSpace(b)
            }
        }
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
        if (!isPauseCloudSync) {
            ServiceManager.getInstance()?.onPreparingSyncData()
        }
        if (isDownload) {
            /*Stopping saver saver*/
            Utils.stoppingSaverSpace()
            ServiceManager.getInstance()?.onPreparingSyncData()
            Utils.Log(TAG, "Re-Download file")
        }
        if (isSpaceSaver) {
            viewModel.destroySaver()
        }
        if (isRefresh) {
            ServiceManager.getInstance()?.onPreparingSyncCategoryData()
        }
    }

    override fun onStopListenerAWhile() {
        EventBus.getDefault().unregister(this)
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