package co.tpcreative.supersafe.ui.main_tab;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
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
import com.snatik.storage.Storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.SensorOrientationChangeNotifier;
import co.tpcreative.supersafe.common.activity.BaseGoogleApi;
import co.tpcreative.supersafe.common.controller.GoogleDriveConnectionManager;
import co.tpcreative.supersafe.common.controller.ServiceManager;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.controller.SingletonManagerTab;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.MainCategories;

public class MainTabActivity extends BaseGoogleApi implements SingletonManagerTab.SingleTonResponseListener,SensorOrientationChangeNotifier.Listener,MainTabView, GoogleDriveConnectionManager.GoogleDriveConnectionManagerListener{

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
    private Storage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tab);
        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.baseline_account_circle_white_36);
        ab.setDisplayHomeAsUpEnabled(false);

        SingletonManagerTab.getInstance().setListener(this);

        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        PrefsController.putBoolean(getString(R.string.key_running),true);
        initSpeedDial(true);

        ServiceManager.getInstance().onStartService();

        presenter = new MainTabPresenter();
        presenter.bindView(this);
        presenter.onGetUserInfo();

        storage = new Storage(this);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:{
                Log.d(TAG,"Home action");
                return true;
            }
            case R.id.switch_flash :{
                Log.d(TAG,"Call here");
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
        if (addActionItems) {
            Drawable drawable = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.baseline_photo_camera_white_24);
            FabWithLabelView fabWithLabelView = mSpeedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id
                    .fab_camera, drawable)
                    .setFabImageTintColor(ResourcesCompat.getColor(getResources(), R.color.inbox_primary,getTheme()))
                    .setLabelColor(Color.WHITE)
                    .setLabel(getString(R.string.camera))
                    .setLabelBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.inbox_primary,
                           getTheme()))
                    .create());
            if (fabWithLabelView != null) {
                fabWithLabelView.setSpeedDialActionItem(fabWithLabelView.getSpeedDialActionItemBuilder()
                        .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.material_white_1000,
                               getTheme()))
                        .create());
            }

            drawable = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.baseline_photo_white_24);
            mSpeedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id.fab_photo, drawable)
                    .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.material_green_500,
                           getTheme()))
                    .setLabel(R.string.photo)
                    .setLabelColor(getResources().getColor(R.color.white))
                    .setLabelBackgroundColor(getResources().getColor(R.color.colorBlue))
                    .create());

            mSpeedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id.fab_album, R.drawable
                    .baseline_add_to_photos_white_36)
                    .setLabel(getString(R.string.album))
                    .setTheme(R.style.AppTheme_Purple)
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
                         boolean response = MainCategories.getInstance().onAddCategories(base64Code,value);
                         if (response){
                             Toast.makeText(MainTabActivity.this,"Created album successful",Toast.LENGTH_SHORT).show();
                         }
                         else{
                             Toast.makeText(MainTabActivity.this,"Album name already existing",Toast.LENGTH_SHORT).show();
                         }

                         adapter.getCurrentFragment().onResume();
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
                            Navigator.onMoveCamera(MainTabActivity.this);
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
        Log.d(TAG,"Selected album :");
        switch (requestCode){
            case Navigator.CAMERA_ACTION :{
                if (resultCode == Activity.RESULT_OK) {
                }
                else{
                    Utils.Log(TAG,"Nothing to do on Camera");
                }
                break;
            }
            case Navigator.PHOTO_SLIDE_SHOW:{
                break;
            }
            case Constants.REQUEST_CODE :{
                if (resultCode == Activity.RESULT_OK && data != null) {
                    ArrayList<Image> images = data.getParcelableArrayListExtra(Constants.INTENT_EXTRA_IMAGES);
                    for (int i = 0, l = images.size(); i < l; i++) {
                        String path = images.get(i).path;
                        String id = "" + images.get(i).id;
                        String mimeType = Utils.getMimeType(path);
                        Log.d(TAG, "mimeType " + mimeType);
                        Log.d(TAG, "path " + path);
                        if (mimeType.equals(getString(R.string.key_mime_type_sdcard_jpg)) || mimeType.equals(getString(R.string.key_mime_type_sdcard_png))) {
                            //onUploadFileRXJava(EnumTypeFile.IMAGE, path, mimeType, id);
                        } else {
                           // onUploadFileRXJava(EnumTypeFile.VIDEO, path, mimeType, id);
                        }
                    }
                }
                else {
                    Utils.Log(TAG,"Nothing to do on Gallery");
                }
                break;
            }
            default:{
                Utils.Log(TAG,"Nothing to do");
                break;
            }
        }


        if (requestCode == Constants.REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            ArrayList<Image> images = data.getParcelableArrayListExtra(Constants.INTENT_EXTRA_IMAGES);
            StringBuffer stringBuffer = new StringBuffer();
            for (int i = 0, l = images.size(); i < l; i++) {
                stringBuffer.append(images.get(i).path + "\n");
            }
            Log.d(TAG,"Selected album :" + stringBuffer.toString());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_tab, menu);
        return true;
    }

    @Override
    public void onOrientationChange(int orientation) {
        Log.d(TAG,"displayOrientation " + orientation);
        SuperSafeApplication.getInstance().setOrientation(orientation);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SensorOrientationChangeNotifier.getInstance().addListener(this);
        ServiceManager.getInstance().onSyncData();
       // ServiceManager.getInstance().testLoop();
        GoogleDriveConnectionManager.getInstance().setListener(this);
        Utils.Log(TAG,"path database :" + SuperSafeApplication.getInstance().getPathDatabase());

       // File output = new File(SuperSafeApplication.getInstance().getSupperSafe()+getString(R.string.key_database));
       // File input = new File(SuperSafeApplication.getInstance().getPathDatabase());
       // storage.createFile(output,input);
       onBackUp();
    }

    public void onBackUp(){
        try {
            String inFileName = getDatabasePath(getString(R.string.key_database)).getAbsolutePath();
            File dbFile = new File(inFileName);
            FileInputStream fis = new FileInputStream(dbFile);
            String outFileName = SuperSafeApplication.getInstance().getSuperSafe()+"/database_copy.db";

            if (!storage.isFileExist(inFileName)){
                Utils.Log(TAG,"Path is not existing :" + SuperSafeApplication.getInstance().getPathDatabase());
                return;
            }

            // Open the empty db as the output stream
            OutputStream output = new FileOutputStream(outFileName);
            // Transfer bytes from the inputfile to the outputfile
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer))>0){
                output.write(buffer, 0, length);
            }
            // Close the streams
            output.flush();
            output.close();
            fis.close();
        }
        catch (IOException e){
            e.printStackTrace();
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
    protected void onPause() {
        super.onPause();
        SensorOrientationChangeNotifier.getInstance().remove(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SuperSafeApplication.getInstance().getRequestQueue().getCache().clear();
        ServiceManager.getInstance().onDismissServices();
    }

    public void onAnimationIcon(Menu menu){
        MenuItem item = menu.getItem(0);
        item.setIcon(R.drawable.upload_animation);
        // Get the background, which has been compiled to an AnimationDrawable object.
        AnimationDrawable frameAnimation = (AnimationDrawable) item.getIcon();
        // Start the animation (looped playback by default).
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG,"running");
                frameAnimation.start();
            }
        });
    }


    /*MainTab View*/

    @Override
    public void startLoading() {

    }

    @Override
    public void stopLoading() {

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
}
