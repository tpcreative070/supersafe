package co.tpcreative.supersafe.ui.cloudmanager
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.CompoundButton
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.activity.BaseGoogleApi
import co.tpcreative.supersafe.common.controller.PrefsController
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.util.ConvertUtils
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.*
import com.snatik.storage.Storage
import kotlinx.android.synthetic.main.activity_cloud_manager.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class CloudManagerAct : BaseGoogleApi(), CompoundButton.OnCheckedChangeListener, BaseView<Long> {
    var presenter: CloudManagerPresenter? = null
    var isPauseCloudSync = true
    var isDownload = false
    var isSpaceSaver = false
    var storage: Storage? = null
    var isRefresh = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cloud_manager)
        initUI()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_cloud_manager, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            R.id.menu_item_refresh -> {
                presenter?.onGetDriveAbout()
                isRefresh = true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStartLoading(status: EnumStatus) {}
    override fun onStopLoading(status: EnumStatus) {}
    override fun onError(message: String?) {}
    override fun onError(message: String?, status: EnumStatus?) {
        when (status) {
            EnumStatus.REQUEST_ACCESS_TOKEN -> {
                Utils.Log(TAG, "Error response $message")
                getAccessToken()
            }
            else -> {
                Utils.Log(TAG, "Error response $message")
            }
        }
    }

    override fun onSuccessful(message: String?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?) {
        when (status) {
            EnumStatus.GET_LIST_FILES_IN_APP -> {
                onShowUI()
            }
            EnumStatus.SAVER -> {
                tvDeviceSaving?.text = presenter?.sizeSaverFiles?.let { ConvertUtils.byte2FitMemorySize(it) }
            }
            EnumStatus.GET_LIST_FILE -> {
                onShowDialog()
            }
            EnumStatus.DOWNLOAD -> {
                tvDeviceSaving?.text = ConvertUtils.byte2FitMemorySize(0)
                isDownload = true
            }
            else -> Utils.Log(TAG,"Nothing")
        }
    }

    override fun onSuccessful(message: String?, status: EnumStatus?, `object`: Long?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?, list: MutableList<Long>?) {
        Utils.Log(TAG, "Successful response $message")
    }

    override fun getContext(): Context? {
        return this
    }

    override fun getActivity(): Activity? {
        return this
    }

    override fun onCheckedChanged(compoundButton: CompoundButton?, b: Boolean) {
        Utils.Log(TAG, "onCheckedChanged...............!!!")
        when (compoundButton?.getId()) {
            R.id.btnSwitchPauseSync -> {
                isPauseCloudSync = b
                PrefsController.putBoolean(getString(R.string.key_pause_cloud_sync), b)
            }
            R.id.switch_SaveSpace -> {
                if (!Utils.isPremium()) {
                    onShowPremium()
                    PrefsController.putBoolean(getString(R.string.key_saving_space), false)
                    switch_SaveSpace?.isChecked = false
                }
                if (b) {
                    isDownload = false
                    isSpaceSaver = true
                    presenter?.onEnableSaverSpace()
                } else {
                    isSpaceSaver = false
                    presenter?.onDisableSaverSpace(EnumStatus.GET_LIST_FILE)
                }
                PrefsController.putBoolean(getString(R.string.key_saving_space), b)
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
        onShowSwitch()
    }

    override fun onDestroy() {
        super.onDestroy()
        Utils.Log(TAG, "OnDestroy")
        EventBus.getDefault().unregister(this)
        if (!isPauseCloudSync) {
            ServiceManager.getInstance()?.onPreparingSyncData()
        }
        if (isDownload) {
            val mList: MutableList<ItemModel>? = SQLHelper.getListSyncData(true, false, false)
            if (mList != null && mList.size > 0) {
                for (i in mList.indices) {
                    val formatType = EnumFormatType.values()[mList[i].formatType]
                    when (formatType) {
                        EnumFormatType.IMAGE -> {
                            mList[i].isSyncCloud = false
                            mList[i].originalSync = false
                            SQLHelper.updatedItem(mList[i])
                        }
                        else -> Utils.Log(TAG,"Nothing")
                    }
                }
            }
            ServiceManager.getInstance()?.onPreparingSyncData()
            Utils.Log(TAG, "Re-Download file")
        }
        if (isSpaceSaver) {
            val mList: MutableList<ItemModel>? = SQLHelper.getListSyncData(true, true, false)
            if (mList != null) {
                for (index in mList) {
                    val formatType = EnumFormatType.values()[index.formatType]
                    when (formatType) {
                        EnumFormatType.IMAGE -> {
                            storage?.deleteFile(index.getOriginal())
                        }
                        else -> Utils.Log(TAG,"Nothing")
                    }
                }
            }
        }
        if (isRefresh) {
            ServiceManager.getInstance()?.onPreparingSyncCategoryData()
        }
        presenter?.unbindView()
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