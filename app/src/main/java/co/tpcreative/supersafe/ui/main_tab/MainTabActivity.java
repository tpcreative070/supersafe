package co.tpcreative.supersafe.ui.main_tab;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.darsh.multipleimageselect.helpers.Constants;
import com.darsh.multipleimageselect.models.Image;
import com.google.gson.Gson;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.leinardi.android.speeddial.FabWithLabelView;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;
import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.SensorOrientationChangeNotifier;
import co.tpcreative.supersafe.common.activity.BaseGoogleApi;
import co.tpcreative.supersafe.common.controller.GoogleDriveConnectionManager;
import co.tpcreative.supersafe.common.controller.ServiceManager;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.controller.SingletonManagerTab;
import co.tpcreative.supersafe.common.controller.SingletonPrivateFragment;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.common.views.AnimationsContainer;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.MainCategories;
import co.tpcreative.supersafe.model.MimeTypeFile;
import co.tpcreative.supersafe.model.User;


public class MainTabActivity extends BaseGoogleApi implements SingletonManagerTab.SingleTonResponseListener,MainTabView, GoogleDriveConnectionManager.GoogleDriveConnectionManagerListener{

    private static final String TAG = MainTabActivity.class.getSimpleName();
    @BindView(R.id.speedDial)
    SpeedDialView mSpeedDialView;
    @BindView(R.id.viewpager)
    ViewPager viewPager;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tabs)
    TabLayout tabLayout;
    private Toast mToast;
    private MainViewPagerAdapter adapter;
    private MainTabPresenter presenter;
    AnimationsContainer.FramesSequenceAnimation animation;
    private MenuItem menuItem;
    private EnumStatus previousStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tab);
        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.mipmap.ic_launcher);
        ab.setDisplayHomeAsUpEnabled(false);
        SingletonManagerTab.getInstance().setListener(this);
        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        PrefsController.putBoolean(getString(R.string.key_running),true);
        initSpeedDial(true);
        presenter = new MainTabPresenter();
        presenter.bindView(this);
        presenter.onGetUserInfo();
    }

    @Override
    public void onAction(EnumStatus enumStatus) {
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onAnimationIcon(enumStatus);
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onSyncDone() {
        SingletonPrivateFragment.getInstance().onUpdateView();
    }

    @Override
    public void onRequestAccessToken() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Utils.Log(TAG,"Request token");
                getAccessToken();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:{
                Log.d(TAG,"Home action");
                return true;
            }
            case R.id.action_sync :{
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
                return true;
            }
            case R.id.settings :{
                Navigator.onSettings(this);
                return true;
            }
            case R.id.help :{
                Navigator.onHelp(this);
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

    @Override
    public void visitFloatingButton(int isVisit) {
        mSpeedDialView.setVisibility(isVisit);
    }


    private void initSpeedDial(boolean addActionItems) {
        final co.tpcreative.supersafe.model.Theme mTheme = co.tpcreative.supersafe.model.Theme.getInstance().getThemeInfo();
        if (addActionItems) {
            Drawable drawable = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.baseline_photo_camera_white_24);
            mSpeedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id
                    .fab_camera, drawable)
                   .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), mTheme.getPrimaryColor(),
                           getTheme()))

                    .setLabel(getString(R.string.camera))
                   .setLabelColor(Color.WHITE)
                    .setLabelBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.inbox_primary,
                           getTheme()))
                    .create());

            drawable = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.baseline_photo_white_24);
            mSpeedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id.fab_photo, drawable)
                    .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), mTheme.getPrimaryColor(),
                           getTheme()))
                    .setLabel(R.string.photo)
                    .setLabelColor(getResources().getColor(R.color.white))
                    .setLabelBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.inbox_primary,
                            getTheme()))
                    .create());

            mSpeedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id.fab_album, R.drawable
                    .baseline_add_to_photos_white_36)
                    .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), mTheme.getPrimaryColor(),
                            getTheme()))
                    .setLabel(getString(R.string.album))
                    .setLabelColor(getResources().getColor(R.color.white))
                    .setLabelBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.inbox_primary,
                            getTheme()))
                    .create());
            mSpeedDialView.setMainFabAnimationRotateAngle(180);
        }

        //Set main action clicklistener.
        mSpeedDialView.setOnChangeListener(new SpeedDialView.OnChangeListener() {
            @Override
            public boolean onMainActionSelected() {
                return false; // True to keep the Speed Dial open
            }

            @Override
            public void onToggleChanged(boolean isOpen) {
                Log.d(TAG, "Speed dial toggle state changed. Open = " + isOpen);
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
                        showToast(actionItem.getLabel(getApplicationContext()) + " Photo");
                        Navigator.onMoveToAlbum(MainTabActivity.this);
                        return false; // closes without animation (same as mSpeedDialView.close(false); return false;)
                    case R.id.fab_camera:
                        showToast(actionItem.getLabel(getApplicationContext()) + " Camera");
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
                             boolean response = MainCategories.getInstance().onAddCategories(base64Code,value);
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
                            Log.d(TAG,"Permission is denied");
                        }
                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            /*Miss add permission in manifest*/
                            Log.d(TAG, "request permission is failed");
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
                        Log.d(TAG, "error ask permission");
                    }
                }).onSameThread().check();
    }

    protected void showToast(String text) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
        mToast.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "Selected album :");
        switch (requestCode) {
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
            case Constants.REQUEST_CODE: {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    ArrayList<Image> images = data.getParcelableArrayListExtra(Constants.INTENT_EXTRA_IMAGES);
                    for (int i = 0, l = images.size(); i < l; i++) {
                        String path = images.get(i).path;
                        String name = images.get(i).name;
                        String id = "" + images.get(i).id;
                        String mimeType = Utils.getMimeType(path);
                        Log.d(TAG, "mimeType " + mimeType);
                        Log.d(TAG, "name " + name);
                        Log.d(TAG, "path " + path);
                        String fileExtension = Utils.getFileExtension(path);
                        Log.d(TAG, "file extension " + Utils.getFileExtension(path));

                        try {
                            final MimeTypeFile mimeTypeFile = Utils.mediaTypeSupport().get(fileExtension);
                            mimeTypeFile.name = name;
                            final List<MainCategories> list = MainCategories.getInstance().getList();
                            if (list==null){
                                Utils.onWriteLog("Main categories is null", EnumStatus.WRITE_FILE);
                                return;
                            }
                            ServiceManager.getInstance().onSaveDataOnGallery(mimeTypeFile, path, id, list.get(0));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
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
        getMenuInflater().inflate(R.menu.main_tab, menu);
        this.menuItem = menu.getItem(0);
        return true;
    }


    public MenuItem getMenuItem() {
        return menuItem;
    }


    @Override
    protected void onResume() {
        super.onResume();
        GoogleDriveConnectionManager.getInstance().setListener(this);
        ServiceManager.getInstance().onGetDriveAbout();
        Utils.Log(TAG,"path database :" + SuperSafeApplication.getInstance().getPathDatabase());
        Utils.onBackUp();
        final User mUser = User.getInstance().getUserInfo();
        if (mUser!=null){
            if (mUser.isUpdateView){
                mUser.isUpdateView = false;
                PrefsController.putString(getString(R.string.key_user),new Gson().toJson(mUser));
                recreate();
            }
        }
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
            super.onBackPressed();
        }
    }

    /*GoogleDriveConnectionManagerListener*/

    @Override
    public void onNetworkConnectionChanged(boolean isConnect) {
        Utils.Log(TAG,"Is connected :" + isConnect);
        if (isConnect){
            getAccessToken();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utils.Log(TAG,"OnDestroy");
        ServiceManager.getInstance().onDismissServices();
    }

    public void onAnimationIcon(final EnumStatus status){
        Utils.Log(TAG,"value : "+ status.name());
        if (getMenuItem()==null){
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
        Utils.Log(TAG,"Create new ");
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
    protected void onDriveSuccessful() {

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
    public void onSuccessful(String message, EnumStatus status, List list) {

    }
}
