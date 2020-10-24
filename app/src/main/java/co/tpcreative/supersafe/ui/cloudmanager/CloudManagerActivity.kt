package co.tpcreative.supersafe.ui.cloudmanager
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import butterknife.BindView
import butterknife.OnClick
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
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.Theme
import com.snatik.storage.Storage
import de.mrapp.android.dialog.MaterialDialog
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class CloudManagerActivity : BaseGoogleApi(), CompoundButton.OnCheckedChangeListener, BaseView<Long> {
    @BindView(R.id.tvUploaded)
    var tvUploaded: AppCompatTextView? = null

    @BindView(R.id.tvLeft)
    var tvLeft: AppCompatTextView? = null

    @BindView(R.id.btnRemoveLimit)
    var btnRemoveLimit: AppCompatButton? = null

    @BindView(R.id.tvSupersafeSpace)
    var tvSupersafeSpace: AppCompatTextView? = null

    @BindView(R.id.tvOtherSpace)
    var tvOtherSpace: AppCompatTextView? = null

    @BindView(R.id.tvFreeSpace)
    var tvFreeSpace: AppCompatTextView? = null

    @BindView(R.id.llPremium)
    var llPremium: LinearLayout? = null

    @BindView(R.id.llTitle)
    var llTitle: LinearLayout? = null

    @BindView(R.id.tvValueSupersafeSpace)
    var tvValueSupersafeSpace: AppCompatTextView? = null

    @BindView(R.id.tvValueOtherSpace)
    var tvValueOtherSpace: AppCompatTextView? = null

    @BindView(R.id.tvValueFreeSpace)
    var tvValueFreeSpace: AppCompatTextView? = null

    @BindView(R.id.btnSwitchPauseSync)
    var btnSwitchPauseSync: SwitchCompat? = null

    @BindView(R.id.tvDriveAccount)
    var tvDriveAccount: AppCompatTextView? = null

    @BindView(R.id.tvDeviceSaving)
    var tvDeviceSaving: AppCompatTextView? = null

    @BindView(R.id.switch_SaveSpace)
    var btnSwitchSaveSpace: SwitchCompat? = null
    private var presenter: CloudManagerPresenter? = null
    private var isPauseCloudSync = true
    private var isDownload = false
    private var isSpaceSaver = false
    private var storage: Storage? = null
    private var isRefresh = false
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cloud_manager)
        val toolbar: Toolbar = findViewById<Toolbar?>(R.id.toolbar)
        setSupportActionBar(toolbar)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        storage = Storage(this)
        presenter = CloudManagerPresenter()
        presenter?.bindView(this)
        btnSwitchPauseSync?.setOnCheckedChangeListener(this)
        btnSwitchSaveSpace?.setOnCheckedChangeListener(this)
        val lefFiles: String = kotlin.String.format(getString(R.string.left), "" + Navigator.LIMIT_UPLOAD)
        tvLeft?.setText(lefFiles)
        val updated: String = kotlin.String.format(getString(R.string.left), "0")
        tvUploaded?.setText(updated)
        onShowUI()
        onUpdatedView()
        presenter?.onGetDriveAbout()
    }

    fun onUpdatedView() {
        if (Utils.isPremium()) {
            llPremium?.setVisibility(View.GONE)
            llTitle?.setVisibility(View.VISIBLE)
        } else {
            llPremium?.setVisibility(View.VISIBLE)
            llTitle?.setVisibility(View.GONE)
        }
    }

    @OnClick(R.id.llPause)
    fun onActionPause(view: View?) {
        btnSwitchPauseSync?.setChecked(!btnSwitchPauseSync!!.isChecked())
    }

    @OnClick(R.id.rlSaveSpace)
    fun onActionSaveSpace(view: View?) {
        btnSwitchSaveSpace?.setChecked(!btnSwitchSaveSpace!!.isChecked())
    }

    fun onShowUI() {
        tvSupersafeSpace?.setVisibility(View.VISIBLE)
        tvOtherSpace?.setVisibility(View.VISIBLE)
        tvFreeSpace?.setVisibility(View.VISIBLE)
        val mUser: User? = Utils.getUserInfo()
        var isThrow = false
        if (mUser != null) {
            val driveAbout: DriveAbout? = mUser.driveAbout
            tvDriveAccount?.setText(mUser.cloud_id)
            try {
                val superSafeSpace: String? = driveAbout?.inAppUsed?.let { ConvertUtils.byte2FitMemorySize(it) }
                tvValueSupersafeSpace?.setText(superSafeSpace)
            } catch (e: Exception) {
                tvValueOtherSpace?.setText(getString(R.string.calculating))
                isThrow = true
            }
            try {
                val storageQuota: StorageQuota? = driveAbout?.storageQuota
                if (storageQuota != null) {
                    val superSafeSpace: String? = ConvertUtils.byte2FitMemorySize(storageQuota.usage)
                    tvValueOtherSpace?.setText(superSafeSpace)
                }
            } catch (e: Exception) {
                tvValueOtherSpace?.setText(getString(R.string.calculating))
                isThrow = true
            }
            try {
                val storageQuota: StorageQuota? = driveAbout?.storageQuota
                if (storageQuota != null) {
                    val result = storageQuota.limit - storageQuota.usage
                    val superSafeSpace: String? = ConvertUtils.byte2FitMemorySize(result)
                    tvValueFreeSpace?.setText(superSafeSpace)
                }
            } catch (e: Exception) {
                tvValueFreeSpace?.setText(getString(R.string.calculating))
                isThrow = true
            }
            try {
                if (mUser.syncData != null) {
                    val lefFiles: String? = kotlin.String.format(getString(R.string.left), "" + mUser.syncData?.left)
                    tvLeft?.setText(lefFiles)
                }
            } catch (e: Exception) {
                val lefFiles: String = kotlin.String.format(getString(R.string.left), "" + Navigator.LIMIT_UPLOAD)
                tvLeft?.setText(lefFiles)
                isThrow = true
            }
            try {
                if (mUser.syncData != null) {
                    val uploadedFiles: String? = kotlin.String.format(getString(R.string.uploaded), "" + (Navigator.LIMIT_UPLOAD - mUser?.syncData?.left!!))
                    tvUploaded?.setText(uploadedFiles)
                }
            } catch (e: Exception) {
                val uploadedFiles: String = kotlin.String.format(getString(R.string.uploaded), "0")
                tvUploaded?.setText(uploadedFiles)
                isThrow = true
            }
            if (isThrow) {
                tvSupersafeSpace?.setVisibility(View.INVISIBLE)
                tvOtherSpace?.setVisibility(View.INVISIBLE)
                tvFreeSpace?.setVisibility(View.INVISIBLE)
            }
        }
    }

    fun onShowSwitch() {
        val pause_cloud_sync: Boolean = PrefsController.getBoolean(getString(R.string.key_pause_cloud_sync), false)
        btnSwitchPauseSync?.setChecked(pause_cloud_sync)
        val saving_space: Boolean = PrefsController.getBoolean(getString(R.string.key_saving_space), false)
        btnSwitchSaveSpace?.setChecked(saving_space)
        if (saving_space) {
            presenter?.onGetSaveData()
        } else {
            tvDeviceSaving?.setText(ConvertUtils.Companion.byte2FitMemorySize(0))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_cloud_manager, menu)
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
                tvDeviceSaving?.setText(presenter?.sizeSaverFiles?.let { ConvertUtils.byte2FitMemorySize(it) })
            }
            EnumStatus.GET_LIST_FILE -> {
                onShowDialog()
            }
            EnumStatus.DOWNLOAD -> {
                tvDeviceSaving?.setText(ConvertUtils.Companion.byte2FitMemorySize(0))
                isDownload = true
            }
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
                    btnSwitchSaveSpace?.setChecked(false)
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
        }
    }

    fun onShowPremium() {
        try {
            val builder = getContext()?.let { MaterialDialog.Builder(it) }
            val themeApp: ThemeApp? = ThemeApp.getInstance()?.getThemeInfo()
            builder?.setHeaderBackground(themeApp?.getAccentColor()!!)
            builder?.setTitle(getString(R.string.this_is_premium_feature))
            builder?.setMessage(getString(R.string.upgrade_now))
            builder?.setCustomHeader(R.layout.custom_header)
            builder?.setPadding(40, 40, 40, 0)
            builder?.setMargin(60, 0, 60, 0)
            builder?.showHeader(true)
            builder?.setPositiveButton(getString(R.string.get_premium), object : DialogInterface.OnClickListener {
                override fun onClick(dialogInterface: DialogInterface?, i: Int) {
                    getContext()?.let { Navigator.onMoveToPremium(it) }
                }
            })
            builder?.setNegativeButton(getText(R.string.later), object : DialogInterface.OnClickListener {
                override fun onClick(dialogInterface: DialogInterface?, i: Int) {
                    PrefsController.putBoolean(getString(R.string.key_saving_space), false)
                }
            })
            val dialog = builder?.show()
            builder?.setOnShowListener(object : DialogInterface.OnShowListener {
                override fun onShow(dialogInterface: DialogInterface?) {
                    val positive = dialog?.findViewById<AppCompatButton?>(android.R.id.button1)
                    val negative = dialog?.findViewById<AppCompatButton?>(android.R.id.button2)
                    val textView: AppCompatTextView? = dialog?.findViewById<View?>(R.id.message) as AppCompatTextView?
                    if (positive != null && negative != null && textView != null) {
                        positive.setTextColor(ContextCompat.getColor(getContext()!!,themeApp?.getAccentColor()!!))
                        negative.setTextColor(ContextCompat.getColor(getContext()!!,themeApp?.getAccentColor()!!))
                        textView.setTextSize(16f)
                    }
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    protected override fun onResume() {
        super.onResume()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        onRegisterHomeWatcher()
        //SuperSafeApplication.getInstance().writeKeyHomePressed(CloudManagerActivity.class.getSimpleName());
        onShowSwitch()
    }

    protected override fun onDestroy() {
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
                    }
                }
            }
            ServiceManager.Companion.getInstance()?.onPreparingSyncData()
            Utils.Log(TAG, "Re-Download file")
        }
        if (isSpaceSaver) {
            val mList: MutableList<ItemModel>? = SQLHelper.getListSyncData(true, true, false)
            if (mList != null) {
                for (index in mList) {
                    val formatType = EnumFormatType.values()[index.formatType]
                    when (formatType) {
                        EnumFormatType.IMAGE -> {
                            storage?.deleteFile(index.originalPath)
                        }
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

    @OnClick(R.id.btnRemoveLimit)
    fun onRemoveLimit(view: View?) {
        Navigator.onMoveToPremium(this)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun onShowDialog() {
        val inflater: LayoutInflater = getContext()?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.custom_view_dialog, null)
        val space_required: TextView? = view.findViewById<TextView?>(R.id.tvSpaceRequired)
        space_required?.setText(kotlin.String.format(getString(R.string.space_required), presenter?.sizeFile?.let { ConvertUtils.byte2FitMemorySize(it) }))
        val builder: com.afollestad.materialdialogs.MaterialDialog.Builder = com.afollestad.materialdialogs.MaterialDialog.Builder(this)
                .title(getString(R.string.download_private_cloud_files))
                .customView(view, false)
                .theme(Theme.LIGHT)
                .cancelable(false)
                .titleColor(ContextCompat.getColor(getContext()!!,R.color.black))
                .inputType(InputType.TYPE_CLASS_TEXT)
                .negativeText(getString(R.string.cancel))
                .onNegative { dialog, which ->
                    Utils.Log(TAG, "negative")
                    btnSwitchSaveSpace?.setChecked(true)
                }
                .positiveText(getString(R.string.download))
                .onPositive { dialog, which ->
                    Utils.Log(TAG, "positive")
                    PrefsController.putBoolean(getString(R.string.key_saving_space), false)
                    presenter?.onDisableSaverSpace(EnumStatus.DOWNLOAD)
                }
        builder.show()
    }

    companion object {
        private val TAG = CloudManagerActivity::class.java.simpleName
    }
}