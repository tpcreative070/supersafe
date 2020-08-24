package co.tpcreative.supersafe.ui.main_tab;
import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.viewpager.widget.ViewPager;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.material.tabs.TabLayout;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.activity.BaseGoogleApi;
import co.tpcreative.supersafe.common.controller.PremiumManager;
import co.tpcreative.supersafe.common.controller.ServiceManager;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.controller.SingletonManager;
import co.tpcreative.supersafe.common.controller.SingletonPrivateFragment;
import co.tpcreative.supersafe.common.listener.Listener;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.NetworkUtil;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.common.views.AnimationsContainer;
import co.tpcreative.supersafe.model.Categories;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.HelpAndSupport;
import co.tpcreative.supersafe.model.Image;
import co.tpcreative.supersafe.model.ImportFiles;
import co.tpcreative.supersafe.model.Items;
import co.tpcreative.supersafe.model.MainCategories;
import co.tpcreative.supersafe.model.MimeTypeFile;
import co.tpcreative.supersafe.model.ThemeApp;
import co.tpcreative.supersafe.model.User;
import co.tpcreative.supersafe.model.room.InstanceGenerator;
import spencerstudios.com.bungeelib.Bungee;

public class MainTabActivity extends BaseGoogleApi implements BaseView{
    private static final String TAG = MainTabActivity.class.getSimpleName();
    @BindView(R.id.speedDial)
    SpeedDialView mSpeedDialView;
    @BindView(R.id.viewpager)
    ViewPager viewPager;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tabs)
    TabLayout tabLayout;
    @BindView(R.id.rlOverLay)
    RelativeLayout rlOverLay;
    @BindView(R.id.viewFloatingButton)
    View viewFloatingButton;
    private MainViewPagerAdapter adapter;
    private MainTabPresenter presenter;
    AnimationsContainer.FramesSequenceAnimation animation;
    private MenuItem menuItem;
    private EnumStatus previousStatus;
    private InterstitialAd mInterstitialAd;
    private int  mCountToRate = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tab);
        initSpeedDial(true);
        setSupportActionBar(toolbar);
        toolbar.inflateMenu(R.menu.main_tab);
        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.baseline_account_circle_white_24);
        ab.setDisplayHomeAsUpEnabled(true);
        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        PrefsController.putBoolean(getString(R.string.key_running),true);
        presenter = new MainTabPresenter();
        presenter.bindView(this);
        presenter.onGetUserInfo();
        onShowSuggestion();
        if (Utils.isCheckSyncSuggestion()){
            onSuggestionSyncData();
        }
        PremiumManager.getInstance().onStartInAppPurchase();
        if (presenter.mUser.driveConnected){
            if (NetworkUtil.pingIpAddress(this)) {
                return;
            }
            Utils.onObserveData(2000, new Listener() {
                @Override
                public void onStart() {
                    onAnimationIcon(EnumStatus.DONE);
                }
            });
        };
        Utils.Log(TAG,"Author access token "+ Utils.getAccessToken());
    }

    private void showInterstitial() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }

    public void onShowSuggestion(){
        final boolean isFirstFile = PrefsController.getBoolean(getString(R.string.key_is_first_files),false);
        if (!isFirstFile){
            List<Items> mList = InstanceGenerator.getInstance(this).getListAllItems(false);
            if (mList!=null && mList.size()>0){
                PrefsController.putBoolean(getString(R.string.key_is_first_files),true);
                return;
            }
            viewFloatingButton.setVisibility(View.VISIBLE);
            onSuggestionAddFiles();
        }
        else{
            final boolean isFirstEnableSyncData = PrefsController.getBoolean(getString(R.string.key_is_first_enable_sync_data),false);
            if (!isFirstEnableSyncData){
                if (presenter.mUser.driveConnected){
                    PrefsController.putBoolean(getString(R.string.key_is_first_enable_sync_data),true);
                }
                onSuggestionSyncData();
            }
        }
    }

    @Override
    public void onOrientationChange(boolean isFaceDown) {
        Utils.Log(TAG,"onOrientationChange");
        onFaceDown(isFaceDown);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EnumStatus event) {
        switch (event){
            case REGISTER_OR_LOGIN:{
                rlOverLay.setVisibility(View.INVISIBLE);
                break;
            }
            case UNLOCK:{
                rlOverLay.setVisibility(View.INVISIBLE);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                break;
            }
            case FINISH:{
                Navigator.onMoveToFaceDown(this);
                break;
            }
            case PRIVATE_DONE:{
                if (mSpeedDialView!=null){
                    mSpeedDialView.show();
                }
                break;
            }
            case DOWNLOAD:{
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.Log(TAG,"sync value "+ event.name());
                        onAnimationIcon(event);
                    }
                });
                break;
            }
            case UPLOAD:{
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.Log(TAG,"sync value "+ event.name());
                        onAnimationIcon(event);
                    }
                });
                break;
            }
            case DONE:{
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.Log(TAG,"sync value "+ event.name());
                        onAnimationIcon(event);
                    }
                });
                break;
            }
            case SYNC_ERROR:{
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.Log(TAG,"sync value "+ event.name());
                        onAnimationIcon(event);
                    }
                });
                break;
            }
            case REQUEST_ACCESS_TOKEN:{
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.Log(TAG,"Request token");
                        getAccessToken();
                    }
                });
                break;
            }
            case SHOW_FLOATING_BUTTON:{
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSpeedDialView.show();
                    }
                });
                break;
            }
            case HIDE_FLOATING_BUTTON:{
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSpeedDialView.hide();
                    }
                });
                break;
            }
            case CONNECTED:{
                getAccessToken();
                break;
            }
        }
    };

    @Override
    protected void onResume(){
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }
        onCallLockScreen();
        onRegisterHomeWatcher();
        presenter.onGetUserInfo();
        Utils.Log(TAG,"onResume");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utils.Log(TAG,"OnDestroy");
        Utils.onUpdatedCountRate();
        EventBus.getDefault().unregister(this);
        PrefsController.putBoolean(getString(R.string.second_loads),true);
        if (SingletonManager.getInstance().isReloadMainTab()){
            SingletonManager.getInstance().setReloadMainTab(false);
        }else{
            ServiceManager.getInstance().onDismissServices();
        }
    }

    @Override
    protected void onStopListenerAWhile() {
        EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:{
                Utils.Log(TAG,"Home action");
                if (presenter!=null){
                    if (presenter.mUser.verified){
                        Navigator.onManagerAccount(getActivity());
                    }
                    else{
                        Navigator.onVerifyAccount(getActivity());
                    }
                }
                return true;
            }
            case R.id.action_sync :{
                onEnableSyncData();
                return true;
            }
            case R.id.settings :{
                Navigator.onSettings(this);
                return true;
            }
            case R.id.help :{
                Navigator.onMoveHelpSupport(this);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupViewPager(ViewPager viewPager) {
        viewPager.setOffscreenPageLimit(3);
        adapter = new MainViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
    }

    private void initSpeedDial(boolean addActionItems) {
        Utils.Log(TAG,"Init floating button");
        final ThemeApp mThemeApp = ThemeApp.getInstance().getThemeInfo();
        if (addActionItems) {
            Drawable drawable = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.baseline_photo_camera_white_24);
            mSpeedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id
                    .fab_camera, drawable)
                   .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), mThemeApp.getPrimaryColor(),
                           getTheme()))

                    .setLabel(getString(R.string.camera))
                   .setLabelColor(Color.WHITE)
                    .setLabelBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.inbox_primary,
                           getTheme()))
                    .create());

            drawable = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.baseline_photo_white_24);
            mSpeedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id.fab_photo, drawable)
                    .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), mThemeApp.getPrimaryColor(),
                           getTheme()))
                    .setLabel(R.string.photo)
                    .setLabelColor(getResources().getColor(R.color.white))
                    .setLabelBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.inbox_primary,
                            getTheme()))
                    .create());

            mSpeedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id.fab_album, R.drawable
                    .baseline_add_to_photos_white_36)
                    .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), mThemeApp.getPrimaryColor(),
                            getTheme()))
                    .setLabel(getString(R.string.album))
                    .setLabelColor(getResources().getColor(R.color.white))
                    .setLabelBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.inbox_primary,
                            getTheme()))
                    .create());
            mSpeedDialView.show();
        }

        //Set main action clicklistener.
        mSpeedDialView.setOnChangeListener(new SpeedDialView.OnChangeListener() {
            @Override
            public boolean onMainActionSelected() {
                return false; // True to keep the Speed Dial open
            }
            @Override
            public void onToggleChanged(boolean isOpen) {
                //mSpeedDialView.setMainFabOpenedDrawable(AppCompatResources.getDrawable(getContext(), R.drawable.baseline_add_white_24));
                //mSpeedDialView.setMainFabClosedDrawable(AppCompatResources.getDrawable(getContext(), R.drawable.baseline_add_white_24));
                Utils.Log(TAG, "Speed dial toggle state changed. Open = " + isOpen);
            }
        });

        //Set option fabs clicklisteners.
        mSpeedDialView.setOnActionSelectedListener(new SpeedDialView.OnActionSelectedListener() {
            @Override
            public boolean onActionSelected(SpeedDialActionItem actionItem) {
                switch (actionItem.getId()) {
                    case R.id.fab_album:
                        onShowDialog();
                        return false; // false will close it without animation
                    case R.id.fab_photo:
                        Navigator.onMoveToAlbum(MainTabActivity.this);
                        return false; // closes without animation (same as mSpeedDialView.close(false); return false;)
                    case R.id.fab_camera:
                        onAddPermissionCamera();
                        return  false;
                }
                return true; // To keep the Speed Dial open
            }
        });
    }


    public void onShowDialog(){
        MaterialDialog.Builder builder =  new MaterialDialog.Builder(this)
                .title(getString(R.string.create_album))
                .theme(Theme.LIGHT)
                .titleColor(getResources().getColor(R.color.black))
                .inputType(InputType.TYPE_CLASS_TEXT)
                .negativeText(getString(R.string.cancel))
                .positiveText(getString(R.string.ok))
                .input(null, null, new MaterialDialog.InputCallback() {
                     @Override
                     public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                         Utils.Log(TAG,"Value");
                         String value = input.toString();
                         String base64Code = Utils.getHexCode(value);

                         MainCategories item = MainCategories.getInstance().getTrashItem();
                         String result = item.categories_hex_name;
                         if (base64Code.equals(result)){
                             Toast.makeText(MainTabActivity.this,"This name already existing",Toast.LENGTH_SHORT).show();
                         }
                         else{
                             boolean response = MainCategories.getInstance().onAddCategories(base64Code,value,false);
                             if (response){
                                 Toast.makeText(MainTabActivity.this,"Created album successful",Toast.LENGTH_SHORT).show();
                                 ServiceManager.getInstance().onGetListCategoriesSync();
                             }
                             else{
                                 Toast.makeText(MainTabActivity.this,"Album name already existing",Toast.LENGTH_SHORT).show();
                             }
                             SingletonPrivateFragment.getInstance().onUpdateView();
                         }
                     }
                 });
        builder.show();
    }

    public void onAddPermissionCamera() {
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.CAMERA)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            final List<MainCategories> list = MainCategories.getInstance().getList();
                            if (list!=null){
                                Navigator.onMoveCamera(MainTabActivity.this,list.get(0));
                            }
                        }
                        else{
                            Utils.Log(TAG,"Permission is denied");
                        }
                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            /*Miss add permission in manifest*/
                            Utils.Log(TAG, "request permission is failed");
                        }
                    }
                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        /* ... */
                        token.continuePermissionRequest();
                    }
                })
                .withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        Utils.Log(TAG, "error ask permission");
                    }
                }).onSameThread().check();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Utils.Log(TAG, "Selected album :");
        switch (requestCode) {
            case Navigator.COMPLETED_RECREATE :{
                if (resultCode == Activity.RESULT_OK) {
                    SingletonManager.getInstance().setReloadMainTab(true);
                    Navigator.onMoveToMainTab(this);
                    Utils.Log(TAG,"New Activity");
                } else {
                    Utils.Log(TAG, "Nothing Updated theme");
                }
                break;
            }
            case Navigator.CAMERA_ACTION: {
                if (resultCode == Activity.RESULT_OK) {
                    Utils.Log(TAG, "reload data");
                    SingletonPrivateFragment.getInstance().onUpdateView();
                } else {
                    Utils.Log(TAG, "Nothing to do on Camera");
                }
                break;
            }
            case Navigator.PHOTO_SLIDE_SHOW: {
                if (resultCode == Activity.RESULT_OK) {
                    Utils.Log(TAG, "reload data");
                    SingletonPrivateFragment.getInstance().onUpdateView();

                } else {
                    Utils.Log(TAG, "Nothing to do on Camera");
                }
                break;
            }
            case Navigator.REQUEST_CODE: {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    ArrayList<Image> images = data.getParcelableArrayListExtra(Navigator.INTENT_EXTRA_IMAGES);
                    List<ImportFiles> mListImportFiles = new ArrayList<>();
                    for (int i = 0, l = images.size(); i < l; i++) {
                        String path = images.get(i).path;
                        String name = images.get(i).name;
                        String id = "" + images.get(i).id;
                        String mimeType = Utils.getMimeType(path);
                        Utils.Log(TAG, "mimeType " + mimeType);
                        Utils.Log(TAG, "name " + name);
                        Utils.Log(TAG, "path " + path);
                        String fileExtension = Utils.getFileExtension(path);
                        Utils.Log(TAG, "file extension " + Utils.getFileExtension(path));
                        try {
                            MimeTypeFile mimeTypeFile = Utils.mediaTypeSupport().get(fileExtension);
                            if (mimeTypeFile==null){
                                return;
                            }
                            mimeTypeFile.name = name;
                            final List<MainCategories> list = MainCategories.getInstance().getList();
                            if (list==null){
                                Utils.onWriteLog("Main categories is null", EnumStatus.WRITE_FILE);
                                return;
                            }
                            ImportFiles importFiles = new ImportFiles(list.get(0),mimeTypeFile,path,i,false);
                            mListImportFiles.add(importFiles);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    ServiceManager.getInstance().setmListImport(mListImportFiles);
                    ServiceManager.getInstance().onImportingFiles();
                } else {
                    Utils.Log(TAG, "Nothing to do on Gallery");
                }
                break;
            }
            default: {
                Utils.Log(TAG, "Nothing to do");
                break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (toolbar==null){
            return false ;
        }
        toolbar.inflateMenu(R.menu.main_tab);
        this.menuItem = toolbar.getMenu().getItem(0);
        return true;
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }

    @Override
    protected void onPause() {
        super.onPause();
        final User user = User.getInstance().getUserInfo();
        if (user!=null){
            if (!user.driveConnected){
                onAnimationIcon(EnumStatus.SYNC_ERROR);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mSpeedDialView.isOpen()){
            mSpeedDialView.close();
        }else {
            PremiumManager.getInstance().onStop();
            Utils.onDeleteTemporaryFile();
            Utils.onExportAndImportFile(SuperSafeApplication.getInstance().getSupersafeDataBaseFolder(), SuperSafeApplication.getInstance().getSupersafeBackup(), new ServiceManager.ServiceManagerSyncDataListener() {
                @Override
                public void onCompleted() {
                    Utils.Log(TAG,"Exporting successful");
                }
                @Override
                public void onError() {
                    Utils.Log(TAG,"Exporting error");
                }
                @Override
                public void onCancel() {

                }
            });
            SuperSafeApplication.getInstance().writeUserSecret(presenter.mUser);
            final boolean isPressed =  PrefsController.getBoolean(getString(R.string.we_are_a_team),false);
            if (isPressed){
                super.onBackPressed();
            }
            else{
                final boolean  isSecondLoad = PrefsController.getBoolean(getString(R.string.second_loads),false);
                if (isSecondLoad){
                    final boolean isPositive = PrefsController.getBoolean(getString(R.string.we_are_a_team_positive),false);
                    mCountToRate = PrefsController.getInt(getString(R.string.key_count_to_rate),0);
                    if (!isPositive && mCountToRate>Utils.COUNT_RATE){
                       onAskingRateApp();
                    }
                    else {
                        super.onBackPressed();
                    }
                }
                else{
                    super.onBackPressed();
                }
            }
        }
    }

    public void onAnimationIcon(final EnumStatus status){
        Utils.Log(TAG,"value : "+ status.name());
        if (getMenuItem()==null){
            Utils.Log(TAG,"Menu is nulll");
            return;
        }

        if ((previousStatus == status) && (status == EnumStatus.DOWNLOAD)){
            Utils.Log(TAG,"Action here 1");
            return;
        }

        if ((previousStatus == status) && (status ==EnumStatus.UPLOAD)){
            Utils.Log(TAG,"Action here 2");
            return;
        }

        MenuItem item = getMenuItem();
        if (animation!=null){
            animation.stop();
        }
        Utils.Log(TAG,"Calling AnimationsContainer........................");
        Utils.onWriteLog("Calling AnimationsContainer",EnumStatus.CREATE);
        previousStatus = status;
        animation = AnimationsContainer.getInstance().createSplashAnim(item, status);
        animation.start();
    }

    /*MainTab View*/

    @Override
    public void onStartLoading(EnumStatus status) {
    }

    @Override
    public void onStopLoading(EnumStatus status) {
    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }

    @Override
    protected void onDriveClientReady() {
    }

    @Override
    protected boolean isSignIn() {
        return false;
    }

    @Override
    protected void onDriveSuccessful() {
        onCheckRequestSignOut();
    }

    @Override
    protected void onDriveError() {
    }

    @Override
    protected void onDriveSignOut() {

    }

    @Override
    protected void onDriveRevokeAccess() {
    }

    @Override
    public void onError(String message, EnumStatus status) {
    }

    @Override
    public void onError(String message) {

    }

    @Override
    public void onSuccessful(String message) {

    }

    @Override
    public void onSuccessful(String message, EnumStatus status) {

    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public void onSuccessful(String message, EnumStatus status, Object object) {

    }

    @Override
    protected void startServiceNow() {

    }

    @Override
    public void onSuccessful(String message, EnumStatus status, List list) {

    }

    public void onSuggestionAddFiles(){
        TapTargetView.showFor(this,                 // `this` is an Activity
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
                new TapTargetView.Listener() {          // The listener can listen for regular clicks, long clicks or cancels
                    @Override
                    public void onTargetClick(TapTargetView view) {
                        super.onTargetClick(view);      // This call is optional
                        mSpeedDialView.open();
                        view.dismiss(true);
                        viewFloatingButton.setVisibility(View.GONE);
                        PrefsController.putBoolean(getString(R.string.key_is_first_files),true);
                    }

                    @Override
                    public void onOuterCircleClick(TapTargetView view) {
                        super.onOuterCircleClick(view);
                        PrefsController.putBoolean(getString(R.string.key_is_first_files),true);
                        view.dismiss(true);
                        viewFloatingButton.setVisibility(View.GONE);
                        Utils.Log(TAG,"onOuterCircleClick");
                    }

                    @Override
                    public void onTargetDismissed(TapTargetView view, boolean userInitiated) {
                        super.onTargetDismissed(view, userInitiated);
                        PrefsController.putBoolean(getString(R.string.key_is_first_files),true);
                        view.dismiss(true);
                        viewFloatingButton.setVisibility(View.GONE);
                        Utils.Log(TAG,"onTargetDismissed");
                    }

                    @Override
                    public void onTargetCancel(TapTargetView view) {
                        super.onTargetCancel(view);
                        PrefsController.putBoolean(getString(R.string.key_is_first_files),true);
                        view.dismiss(true);
                        viewFloatingButton.setVisibility(View.GONE);
                        Utils.Log(TAG,"onTargetCancel");
                    }
                });
    }


    public void onSuggestionSyncData(){
        TapTargetView.showFor(this,                 // `this` is an Activity
                TapTarget.forToolbarMenuItem(toolbar,R.id.action_sync, getString(R.string.tap_here_to_enable_sync_data), getString(R.string.tap_here_to_enable_sync_data_description))
                        .titleTextSize(25)
                        .titleTextColor(R.color.white)
                        .descriptionTextColor(R.color.colorPrimary)
                        .descriptionTextSize(17)
                        .outerCircleColor(R.color.colorButton)
                        .transparentTarget(true)
                        .targetCircleColor(R.color.white)
                        .cancelable(true)
                        .dimColor(R.color.white),
                new TapTargetView.Listener() {          // The listener can listen for regular clicks, long clicks or cancels
                    @Override
                    public void onTargetClick(TapTargetView view) {
                        super.onTargetClick(view);      // This call is optional
                        onEnableSyncData();
                        view.dismiss(true);
                        PrefsController.putBoolean(getString(R.string.key_is_first_enable_sync_data),true);
                        Utils.Log(TAG,"onTargetClick");
                    }

                    @Override
                    public void onOuterCircleClick(TapTargetView view) {
                        super.onOuterCircleClick(view);
                        PrefsController.putBoolean(getString(R.string.key_is_first_enable_sync_data),true);
                        view.dismiss(true);
                        Utils.Log(TAG,"onOuterCircleClick");
                    }

                    @Override
                    public void onTargetDismissed(TapTargetView view, boolean userInitiated) {
                        super.onTargetDismissed(view, userInitiated);
                        PrefsController.putBoolean(getString(R.string.key_is_first_enable_sync_data),true);
                        view.dismiss(true);
                        Utils.Log(TAG,"onTargetDismissed");
                    }

                    @Override
                    public void onTargetCancel(TapTargetView view) {
                        super.onTargetCancel(view);
                        PrefsController.putBoolean(getString(R.string.key_is_first_enable_sync_data),true);
                        view.dismiss(true);
                        Utils.Log(TAG,"onTargetCancel");
                    }
                });
    }

    public void onEnableSyncData(){
        final User mUser = User.getInstance().getUserInfo();
        if (mUser!=null){
            if (mUser.verified){
                if (!mUser.driveConnected){
                    Navigator.onCheckSystem(this,null);
                }
                else{
                    Navigator.onManagerCloud(this);
                }
            }
            else{
                Navigator.onVerifyAccount(this);
            }
        }
    }

    public void onAskingRateApp() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.custom_view_rate_app_dialog, null);
        TextView happy = view.findViewById(R.id.tvHappy);
        TextView unhappy = view.findViewById(R.id.tvUnhappy);
        MaterialDialog.Builder builder = new MaterialDialog.Builder(this)
                .title(getString(R.string.how_are_we_doing))
                .customView(view, true)
                .theme(Theme.LIGHT)
                .cancelable(true)
                .titleColor(getResources().getColor(R.color.black))
                .positiveText(getString(R.string.i_love_it))
                .negativeText(getString(R.string.report_problem))
                .neutralText(getString(R.string.no_thanks))
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        PrefsController.putBoolean(getString(R.string.we_are_a_team),true);
                        finish();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Categories categories = new Categories(1,getString(R.string.contact_support));
                        HelpAndSupport support = new HelpAndSupport(categories,getString(R.string.contact_support),getString(R.string.contact_support_content),null);
                        Navigator.onMoveReportProblem(getContext(),support);
                        PrefsController.putBoolean(getString(R.string.we_are_a_team),true);
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                      Utils.Log(TAG,"Positive");
                      onRateApp();
                      PrefsController.putBoolean(getString(R.string.we_are_a_team),true);
                      PrefsController.putBoolean(getString(R.string.we_are_a_team_positive),true);
                    }
                });
        builder.build().show();
    }

    public void onRateApp() {
        Uri uri = Uri.parse("market://details?id=" + getString(R.string.supersafe_live));
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + getString(R.string.supersafe_live))));
        }
    }
}
