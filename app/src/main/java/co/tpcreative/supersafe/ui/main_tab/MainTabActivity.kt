package co.tpcreative.supersafe.ui.main_tab
import android.Manifest
import android.R
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import androidx.viewpager.widget.ViewPager
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.listener.Listener
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.Categories
import co.tpcreative.supersafe.model.Image
import co.tpcreative.supersafe.model.MimeTypeFile
import co.tpcreative.supersafe.model.User
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.karumi.dexter.listener.PermissionRequest
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.*

class MainTabActivity : BaseGoogleApi(), BaseView<Any?> {
    @BindView(R.id.speedDial)
    var mSpeedDialView: SpeedDialView? = null

    @BindView(R.id.viewpager)
    var viewPager: ViewPager? = null

    @BindView(R.id.toolbar)
    var toolbar: Toolbar? = null

    @BindView(R.id.tabs)
    var tabLayout: TabLayout? = null

    @BindView(R.id.rlOverLay)
    var rlOverLay: RelativeLayout? = null

    @BindView(R.id.viewFloatingButton)
    var viewFloatingButton: View? = null
    private var adapter: MainViewPagerAdapter? = null
    private var presenter: MainTabPresenter? = null
    var animation: FramesSequenceAnimation? = null
    private var menuItem: MenuItem? = null
    private var previousStatus: EnumStatus? = null
    private val mInterstitialAd: InterstitialAd? = null
    private var mCountToRate = 0
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_tab)
        initSpeedDial(true)
        setSupportActionBar(toolbar)
        toolbar.inflateMenu(R.menu.main_tab)
        val ab: ActionBar = getSupportActionBar()
        ab.setHomeAsUpIndicator(R.drawable.baseline_account_circle_white_24)
        ab.setDisplayHomeAsUpEnabled(true)
        setupViewPager(viewPager)
        tabLayout.setupWithViewPager(viewPager)
        PrefsController.putBoolean(getString(R.string.key_running), true)
        presenter = MainTabPresenter()
        presenter.bindView(this)
        presenter.onGetUserInfo()
        onShowSuggestion()
        if (Utils.Companion.isCheckSyncSuggestion()) {
            onSuggestionSyncData()
        }
        if (presenter.mUser != null) {
            if (presenter.mUser.driveConnected) {
                if (NetworkUtil.pingIpAddress(this)) {
                    return
                }
                Utils.Companion.onObserveData(2000, Listener { onAnimationIcon(EnumStatus.DONE) })
            }
        }
        Utils.Companion.Log(TAG, "system access token : " + Utils.Companion.getAccessToken())
    }

    private fun showInterstitial() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show()
        }
    }

    fun onShowSuggestion() {
        val isFirstFile: Boolean = PrefsController.getBoolean(getString(R.string.key_is_first_files), false)
        if (!isFirstFile) {
            val mList: MutableList<ItemModel?> = SQLHelper.getListAllItems(false)
            if (mList != null && mList.size > 0) {
                PrefsController.putBoolean(getString(R.string.key_is_first_files), true)
                return
            }
            viewFloatingButton.setVisibility(View.VISIBLE)
            onSuggestionAddFiles()
        } else {
            val isFirstEnableSyncData: Boolean = PrefsController.getBoolean(getString(R.string.key_is_first_enable_sync_data), false)
            if (!isFirstEnableSyncData) {
                if (presenter.mUser.driveConnected) {
                    PrefsController.putBoolean(getString(R.string.key_is_first_enable_sync_data), true)
                }
                onSuggestionSyncData()
            }
        }
    }

    override fun onOrientationChange(isFaceDown: Boolean) {
        Utils.Companion.Log(TAG, "onOrientationChange")
        onFaceDown(isFaceDown)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: EnumStatus?) {
        when (event) {
            EnumStatus.REGISTER_OR_LOGIN -> {
                rlOverLay.setVisibility(View.INVISIBLE)
            }
            EnumStatus.UNLOCK -> {
                rlOverLay.setVisibility(View.INVISIBLE)
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }
            EnumStatus.FINISH -> {
                Navigator.onMoveToFaceDown(this)
            }
            EnumStatus.PRIVATE_DONE -> {
                if (mSpeedDialView != null) {
                    mSpeedDialView.show()
                }
            }
            EnumStatus.DOWNLOAD -> {
                runOnUiThread(Runnable {
                    Utils.Companion.Log(TAG, "sync value " + event.name)
                    onAnimationIcon(event)
                })
            }
            EnumStatus.UPLOAD -> {
                runOnUiThread(Runnable {
                    Utils.Companion.Log(TAG, "sync value " + event.name)
                    onAnimationIcon(event)
                })
            }
            EnumStatus.DONE -> {
                runOnUiThread(Runnable {
                    Utils.Companion.Log(TAG, "sync value " + event.name)
                    onAnimationIcon(event)
                })
            }
            EnumStatus.SYNC_ERROR -> {
                runOnUiThread(Runnable {
                    Utils.Companion.Log(TAG, "sync value " + event.name)
                    onAnimationIcon(event)
                })
            }
            EnumStatus.REQUEST_ACCESS_TOKEN -> {
                runOnUiThread(Runnable {
                    Utils.Companion.Log(TAG, "Request token")
                    getAccessToken()
                })
            }
            EnumStatus.SHOW_FLOATING_BUTTON -> {
                runOnUiThread(Runnable { mSpeedDialView.show() })
            }
            EnumStatus.HIDE_FLOATING_BUTTON -> {
                runOnUiThread(Runnable { mSpeedDialView.hide() })
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

    fun onAlert(content: String?) {
        MaterialDialog.Builder(this).title("Alert")
                .content(content)
                .positiveText("Ok")
                .onPositive(object : SingleButtonCallback {
                    override fun onClick(dialog: MaterialDialog, which: DialogAction) {}
                })
                .show()
    }

    protected override fun onResume() {
        super.onResume()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        onCallLockScreen()
        onRegisterHomeWatcher()
        presenter.onGetUserInfo()
        ServiceManager.Companion.getInstance().setRequestShareIntent(false)
        Utils.Companion.Log(TAG, "onResume")
    }

    protected override fun onDestroy() {
        super.onDestroy()
        Utils.Companion.Log(TAG, "OnDestroy")
        Utils.Companion.onUpdatedCountRate()
        EventBus.getDefault().unregister(this)
        PrefsController.putBoolean(getString(R.string.second_loads), true)
        if (SingletonManager.Companion.getInstance().isReloadMainTab()) {
            SingletonManager.Companion.getInstance().setReloadMainTab(false)
        } else {
            ServiceManager.Companion.getInstance().onDismissServices()
        }
    }

    protected override fun onStopListenerAWhile() {
        EventBus.getDefault().unregister(this)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item.getItemId()) {
            R.id.home -> {
                Utils.Companion.Log(TAG, "Home action")
                if (presenter != null) {
                    if (presenter.mUser.verified) {
                        Navigator.onManagerAccount(getActivity())
                    } else {
                        Navigator.onVerifyAccount(getActivity())
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

    private fun setupViewPager(viewPager: ViewPager?) {
        viewPager.setOffscreenPageLimit(3)
        adapter = MainViewPagerAdapter(getSupportFragmentManager())
        viewPager.setAdapter(adapter)
    }

    private fun initSpeedDial(addActionItems: Boolean) {
        Utils.Companion.Log(TAG, "Init floating button")
        val mThemeApp: ThemeApp = ThemeApp.Companion.getInstance().getThemeInfo()
        if (addActionItems) {
            var drawable: Drawable = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.baseline_photo_camera_white_24)
            mSpeedDialView.addActionItem(SpeedDialActionItem.Builder(R.id.fab_camera, drawable)
                    .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), mThemeApp.getPrimaryColor(),
                            getTheme()))
                    .setLabel(getString(R.string.camera))
                    .setLabelColor(Color.WHITE)
                    .setLabelBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.inbox_primary,
                            getTheme()))
                    .create())
            drawable = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.baseline_photo_white_24)
            mSpeedDialView.addActionItem(SpeedDialActionItem.Builder(R.id.fab_photo, drawable)
                    .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), mThemeApp.getPrimaryColor(),
                            getTheme()))
                    .setLabel(R.string.photo)
                    .setLabelColor(getResources().getColor(R.color.white))
                    .setLabelBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.inbox_primary,
                            getTheme()))
                    .create())
            mSpeedDialView.addActionItem(SpeedDialActionItem.Builder(R.id.fab_album, R.drawable.baseline_add_to_photos_white_36)
                    .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), mThemeApp.getPrimaryColor(),
                            getTheme()))
                    .setLabel(getString(R.string.album))
                    .setLabelColor(getResources().getColor(R.color.white))
                    .setLabelBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.inbox_primary,
                            getTheme()))
                    .create())
            mSpeedDialView.show()
        }

        //Set main action clicklistener.
        mSpeedDialView.setOnChangeListener(object : SpeedDialView.OnChangeListener {
            override fun onMainActionSelected(): Boolean {
                return false // True to keep the Speed Dial open
            }

            override fun onToggleChanged(isOpen: Boolean) {
                //mSpeedDialView.setMainFabOpenedDrawable(AppCompatResources.getDrawable(getContext(), R.drawable.baseline_add_white_24));
                //mSpeedDialView.setMainFabClosedDrawable(AppCompatResources.getDrawable(getContext(), R.drawable.baseline_add_white_24));
                Utils.Companion.Log(TAG, "Speed dial toggle state changed. Open = $isOpen")
            }
        })

        //Set option fabs clicklisteners.
        mSpeedDialView.setOnActionSelectedListener(object : OnActionSelectedListener {
            override fun onActionSelected(actionItem: SpeedDialActionItem?): Boolean {
                when (actionItem.getId()) {
                    R.id.fab_album -> {
                        onShowDialog()
                        return false // false will close it without animation
                    }
                    R.id.fab_photo -> {
                        Navigator.onMoveToAlbum(this@MainTabActivity)
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

    fun onShowDialog() {
        val builder: MaterialDialog.Builder = MaterialDialog.Builder(this)
                .title(getString(R.string.create_album))
                .theme(Theme.LIGHT)
                .titleColor(ContextCompat.getColor(this, R.color.black))
                .inputType(InputType.TYPE_CLASS_TEXT)
                .negativeText(getString(R.string.cancel))
                .positiveText(getString(R.string.ok))
                .input(null, null, object : InputCallback {
                    override fun onInput(dialog: MaterialDialog, input: CharSequence?) {
                        Utils.Companion.Log(TAG, "Value")
                        val value = input.toString()
                        val base64Code: String = Utils.Companion.getHexCode(value)
                        val item: MainCategoryModel = SQLHelper.getTrashItem()
                        val result: String = item.categories_hex_name
                        if (base64Code == result) {
                            Toast.makeText(this@MainTabActivity, "This name already existing", Toast.LENGTH_SHORT).show()
                        } else {
                            val response: Boolean = SQLHelper.onAddCategories(base64Code, value, false)
                            if (response) {
                                Toast.makeText(this@MainTabActivity, "Created album successful", Toast.LENGTH_SHORT).show()
                                ServiceManager.Companion.getInstance().onPreparingSyncCategoryData()
                            } else {
                                Toast.makeText(this@MainTabActivity, "Album name already existing", Toast.LENGTH_SHORT).show()
                            }
                            SingletonPrivateFragment.Companion.getInstance().onUpdateView()
                        }
                    }
                })
        builder.show()
    }

    fun onAddPermissionCamera() {
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.CAMERA)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report.areAllPermissionsGranted()) {
                            val list: MutableList<MainCategoryModel?> = SQLHelper.getList()
                            if (list != null) {
                                Navigator.onMoveCamera(this@MainTabActivity, list[0])
                            }
                        } else {
                            Utils.Companion.Log(TAG, "Permission is denied")
                        }
                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            /*Miss add permission in manifest*/
                            Utils.Companion.Log(TAG, "request permission is failed")
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest?>?, token: PermissionToken?) {
                        /* ... */
                        token.continuePermissionRequest()
                    }
                })
                .withErrorListener(object : PermissionRequestErrorListener {
                    override fun onError(error: DexterError?) {
                        Utils.Companion.Log(TAG, "error ask permission")
                    }
                }).onSameThread().check()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Utils.Companion.Log(TAG, "Selected album :")
        when (requestCode) {
            Navigator.COMPLETED_RECREATE -> {
                if (resultCode == Activity.RESULT_OK) {
                    SingletonManager.Companion.getInstance().setReloadMainTab(true)
                    Navigator.onMoveToMainTab(this)
                    Utils.Companion.Log(TAG, "New Activity")
                } else {
                    Utils.Companion.Log(TAG, "Nothing Updated theme")
                }
            }
            Navigator.CAMERA_ACTION -> {
                if (resultCode == Activity.RESULT_OK) {
                    Utils.Companion.Log(TAG, "reload data")
                    SingletonPrivateFragment.Companion.getInstance().onUpdateView()
                } else {
                    Utils.Companion.Log(TAG, "Nothing to do on Camera")
                }
            }
            Navigator.PHOTO_SLIDE_SHOW -> {
                if (resultCode == Activity.RESULT_OK) {
                    Utils.Companion.Log(TAG, "reload data")
                    SingletonPrivateFragment.Companion.getInstance().onUpdateView()
                } else {
                    Utils.Companion.Log(TAG, "Nothing to do on Camera")
                }
            }
            Navigator.REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val images: ArrayList<Image?> = data.getParcelableArrayListExtra(Navigator.INTENT_EXTRA_IMAGES)
                    val mListImportFiles: MutableList<ImportFilesModel?> = ArrayList<ImportFilesModel?>()
                    var i = 0
                    val l = images.size
                    while (i < l) {
                        val path = images[i].path
                        val name = images[i].name
                        val id = "" + images[i].id
                        val mimeType: String = Utils.Companion.getMimeType(path)
                        Utils.Companion.Log(TAG, "mimeType $mimeType")
                        Utils.Companion.Log(TAG, "name $name")
                        Utils.Companion.Log(TAG, "path $path")
                        val fileExtension: String = Utils.Companion.getFileExtension(path)
                        Utils.Companion.Log(TAG, "file extension " + Utils.Companion.getFileExtension(path))
                        try {
                            val mimeTypeFile: MimeTypeFile = Utils.Companion.mediaTypeSupport().get(fileExtension)
                                    ?: return
                            mimeTypeFile.name = name
                            val list: MutableList<MainCategoryModel?> = SQLHelper.getList()
                            if (list == null) {
                                Utils.Companion.onWriteLog("Main categories is null", EnumStatus.WRITE_FILE)
                                return
                            }
                            val mCategory: MainCategoryModel? = list[0]
                            Utils.Companion.Log(TAG, "Show category " + Gson().toJson(mCategory))
                            val importFiles = ImportFilesModel(list[0], mimeTypeFile, path, i, false)
                            mListImportFiles.add(importFiles)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        i++
                    }
                    ServiceManager.Companion.getInstance().setListImport(mListImportFiles)
                    ServiceManager.Companion.getInstance().onPreparingImportData()
                } else {
                    Utils.Companion.Log(TAG, "Nothing to do on Gallery")
                }
            }
            else -> {
                Utils.Companion.Log(TAG, "Nothing to do")
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (toolbar == null) {
            return false
        }
        toolbar.inflateMenu(R.menu.main_tab)
        menuItem = toolbar.getMenu().getItem(0)
        return true
    }

    fun getMenuItem(): MenuItem? {
        return menuItem
    }

    protected override fun onPause() {
        super.onPause()
        val user: User = Utils.Companion.getUserInfo()
        if (user != null) {
            if (!user.driveConnected) {
                onAnimationIcon(EnumStatus.SYNC_ERROR)
            }
        }
    }

    override fun onBackPressed() {
        if (mSpeedDialView.isOpen()) {
            mSpeedDialView.close()
        } else {
            PremiumManager.Companion.getInstance().onStop()
            Utils.Companion.onDeleteTemporaryFile()
            Utils.Companion.onExportAndImportFile(SuperSafeApplication.Companion.getInstance().getSupersafeDataBaseFolder(), SuperSafeApplication.Companion.getInstance().getSupersafeBackup(), object : ServiceManagerSyncDataListener {
                override fun onCompleted() {
                    Utils.Companion.Log(TAG, "Exporting successful")
                }

                override fun onError() {
                    Utils.Companion.Log(TAG, "Exporting error")
                }

                override fun onCancel() {}
            })
            SuperSafeApplication.Companion.getInstance().writeUserSecret(presenter.mUser)
            val isPressed: Boolean = PrefsController.getBoolean(getString(R.string.we_are_a_team), false)
            if (isPressed) {
                super.onBackPressed()
            } else {
                val isSecondLoad: Boolean = PrefsController.getBoolean(getString(R.string.second_loads), false)
                if (isSecondLoad) {
                    val isPositive: Boolean = PrefsController.getBoolean(getString(R.string.we_are_a_team_positive), false)
                    mCountToRate = PrefsController.getInt(getString(R.string.key_count_to_rate), 0)
                    if (!isPositive && mCountToRate > Utils.Companion.COUNT_RATE) {
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
        Utils.Companion.Log(TAG, "value : " + status.name)
        if (getMenuItem() == null) {
            Utils.Companion.Log(TAG, "Menu is nulll")
            return
        }
        if (previousStatus == status && status == EnumStatus.DOWNLOAD) {
            Utils.Companion.Log(TAG, "Action here 1")
            return
        }
        if (previousStatus == status && status == EnumStatus.UPLOAD) {
            Utils.Companion.Log(TAG, "Action here 2")
            return
        }
        val item = getMenuItem()
        if (animation != null) {
            animation.stop()
        }
        Utils.Companion.Log(TAG, "Calling AnimationsContainer........................")
        Utils.Companion.onWriteLog("Calling AnimationsContainer", EnumStatus.CREATE)
        previousStatus = status
        animation = AnimationsContainer.Companion.getInstance().createSplashAnim(item, status)
        animation.start()
    }

    /*MainTab View*/
    override fun onStartLoading(status: EnumStatus?) {}
    override fun onStopLoading(status: EnumStatus?) {}
    override fun getContext(): Context? {
        return getApplicationContext()
    }

    protected override fun onDriveClientReady() {}
    protected override fun isSignIn(): Boolean {
        return false
    }

    protected override fun onDriveSuccessful() {
        onCheckRequestSignOut()
    }

    protected override fun onDriveError() {}
    protected override fun onDriveSignOut() {}
    protected override fun onDriveRevokeAccess() {}
    override fun onError(message: String?, status: EnumStatus?) {}
    override fun onError(message: String?) {}
    override fun onSuccessful(message: String?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?) {}
    override fun getActivity(): Activity? {
        return this
    }

    override fun onSuccessful(message: String?, status: EnumStatus?, `object`: Any?) {}
    protected override fun startServiceNow() {}
    override fun onSuccessful(message: String?, status: EnumStatus?, list: MutableList<*>?) {}
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
                        mSpeedDialView.open()
                        view.dismiss(true)
                        viewFloatingButton.setVisibility(View.GONE)
                        PrefsController.putBoolean(getString(R.string.key_is_first_files), true)
                    }

                    override fun onOuterCircleClick(view: TapTargetView?) {
                        super.onOuterCircleClick(view)
                        PrefsController.putBoolean(getString(R.string.key_is_first_files), true)
                        view.dismiss(true)
                        viewFloatingButton.setVisibility(View.GONE)
                        Utils.Companion.Log(TAG, "onOuterCircleClick")
                    }

                    override fun onTargetDismissed(view: TapTargetView?, userInitiated: Boolean) {
                        super.onTargetDismissed(view, userInitiated)
                        PrefsController.putBoolean(getString(R.string.key_is_first_files), true)
                        view.dismiss(true)
                        viewFloatingButton.setVisibility(View.GONE)
                        Utils.Companion.Log(TAG, "onTargetDismissed")
                    }

                    override fun onTargetCancel(view: TapTargetView?) {
                        super.onTargetCancel(view)
                        PrefsController.putBoolean(getString(R.string.key_is_first_files), true)
                        view.dismiss(true)
                        viewFloatingButton.setVisibility(View.GONE)
                        Utils.Companion.Log(TAG, "onTargetCancel")
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
                        view.dismiss(true)
                        PrefsController.putBoolean(getString(R.string.key_is_first_enable_sync_data), true)
                        Utils.Companion.Log(TAG, "onTargetClick")
                    }

                    override fun onOuterCircleClick(view: TapTargetView?) {
                        super.onOuterCircleClick(view)
                        PrefsController.putBoolean(getString(R.string.key_is_first_enable_sync_data), true)
                        view.dismiss(true)
                        Utils.Companion.Log(TAG, "onOuterCircleClick")
                    }

                    override fun onTargetDismissed(view: TapTargetView?, userInitiated: Boolean) {
                        super.onTargetDismissed(view, userInitiated)
                        PrefsController.putBoolean(getString(R.string.key_is_first_enable_sync_data), true)
                        view.dismiss(true)
                        Utils.Companion.Log(TAG, "onTargetDismissed")
                    }

                    override fun onTargetCancel(view: TapTargetView?) {
                        super.onTargetCancel(view)
                        PrefsController.putBoolean(getString(R.string.key_is_first_enable_sync_data), true)
                        view.dismiss(true)
                        Utils.Companion.Log(TAG, "onTargetCancel")
                    }
                })
    }

    fun onEnableSyncData() {
        val mUser: User = Utils.Companion.getUserInfo()
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
        val inflater: LayoutInflater = getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.custom_view_rate_app_dialog, null)
        val happy: TextView? = view.findViewById<TextView?>(R.id.tvHappy)
        val unhappy: TextView? = view.findViewById<TextView?>(R.id.tvUnhappy)
        val builder: MaterialDialog.Builder = MaterialDialog.Builder(this)
                .title(getString(R.string.how_are_we_doing))
                .customView(view, true)
                .theme(Theme.LIGHT)
                .cancelable(true)
                .titleColor(getResources().getColor(R.color.black))
                .positiveText(getString(R.string.i_love_it))
                .negativeText(getString(R.string.report_problem))
                .neutralText(getString(R.string.no_thanks))
                .onNeutral(object : SingleButtonCallback {
                    override fun onClick(dialog: MaterialDialog, which: DialogAction) {
                        PrefsController.putBoolean(getString(R.string.we_are_a_team), true)
                        finish()
                    }
                })
                .onNegative(object : SingleButtonCallback {
                    override fun onClick(dialog: MaterialDialog, which: DialogAction) {
                        val categories = Categories(1, getString(R.string.contact_support))
                        val support = HelpAndSupport(categories, getString(R.string.contact_support), getString(R.string.contact_support_content), null)
                        Navigator.onMoveReportProblem(getContext(), support)
                        PrefsController.putBoolean(getString(R.string.we_are_a_team), true)
                    }
                })
                .onPositive(object : SingleButtonCallback {
                    override fun onClick(dialog: MaterialDialog, which: DialogAction) {
                        Utils.Companion.Log(TAG, "Positive")
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
        private val TAG = MainTabActivity::class.java.simpleName
    }
}