package co.tpcreative.supersafe.common.activity;
import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.r0adkll.slidr.model.SlidrConfig;
import com.snatik.storage.Storage;
import java.io.File;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.HomeWatcher;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.SensorFaceUpDownChangeNotifier;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.controller.SingletonMultipleListener;
import co.tpcreative.supersafe.common.hiddencamera.CameraCallbacks;
import co.tpcreative.supersafe.common.hiddencamera.CameraConfig;
import co.tpcreative.supersafe.common.hiddencamera.CameraError;
import co.tpcreative.supersafe.common.hiddencamera.CameraPreview;
import co.tpcreative.supersafe.common.hiddencamera.HiddenCameraUtils;
import co.tpcreative.supersafe.common.hiddencamera.config.CameraFacing;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.ThemeUtil;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumPinAction;
import co.tpcreative.supersafe.model.Theme;

public abstract class BaseVerifyPinActivity extends AppCompatActivity implements CameraCallbacks,SensorFaceUpDownChangeNotifier.Listener{
    Unbinder unbinder;
    protected ActionBar actionBar ;
    int onStartCount = 0;
    private Toast mToast;
    private HomeWatcher mHomeWatcher;
    public static final String TAG = BaseVerifyPinActivity.class.getSimpleName();
    private SlidrConfig mConfig;
    protected Storage storage;

    /*Hidden camera*/
    private CameraPreview mCameraPreview;
    private CameraConfig mCachedCameraConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        actionBar = getSupportActionBar();
        onStartCount = 1;
        if (savedInstanceState == null) {
            this.overridePendingTransition(android.R.anim.fade_in,
                    android.R.anim.fade_in);
        } else {
            onStartCount = 2;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        storage = new Storage(this);
        //Add the camera preview surface to the root of the activity view.
        mCameraPreview = addPreView();
    }

    protected void setStatusBarColored(AppCompatActivity context, int colorPrimary,int colorPrimaryDark) {
        context.getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources()
                .getColor(colorPrimary)));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = context.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(context,colorPrimaryDark));
        }
    }

    @Override
    public Resources.Theme getTheme() {
        Resources.Theme theme = super.getTheme();
        final Theme result = Theme.getInstance().getThemeInfo();
        if (result!=null){
            theme.applyStyle(ThemeUtil.getSlideThemeId(result.getId()), true);
        }
        return theme;
    }

    public void onCallLockScreen(){
        int  value = PrefsController.getInt(getString(R.string.key_screen_status),EnumPinAction.NONE.ordinal());
        EnumPinAction action = EnumPinAction.values()[value];
        switch (action){
            case SPLASH_SCREEN:{
                Navigator.onMoveToVerifyPin(this,EnumPinAction.NONE);
                PrefsController.putInt(getString(R.string.key_screen_status),EnumPinAction.SCREEN_LOCK.ordinal());
                break;
            }
            default:{
                Utils.Log(TAG,"Nothing to do");
            }
        }
    }

    protected void onFaceDown(final boolean isFaceDown){
        if (isFaceDown){
            final boolean result = PrefsController.getBoolean(getString(R.string.key_face_down_lock),false);
            if (result){
                Navigator.onMoveToFaceDown(SuperSafeApplication.getInstance());
            }
        }
    }

    protected float getRandom(float range, float startsfrom) {
        return (float) (Math.random() * range) + startsfrom;
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        Log.d(TAG,"action here");
        unbinder = ButterKnife.bind(this);
    }

    @Override
    protected void onDestroy() {
        if (mHomeWatcher!=null){
            mHomeWatcher.stopWatch();
        }
        if (unbinder != null)
            unbinder.unbind();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        SensorFaceUpDownChangeNotifier.getInstance().remove(this);
        if (mCameraPreview != null) mCameraPreview.stopPreviewAndFreeCamera();
    }

    @Override
    protected void onResume() {
        SensorFaceUpDownChangeNotifier.getInstance().addListener(this);
        Utils.Log(TAG,"Action here........onResume");
        if (mHomeWatcher!=null){
            if (!mHomeWatcher.isRegistered){
                onRegisterHomeWatcher();
            }
        }
        if (mCachedCameraConfig != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                return;
            }
            startCamera(mCachedCameraConfig);
        }
        super.onResume();
    }

    protected void onRegisterHomeWatcher(){
        /*Home action*/
        if (mHomeWatcher!=null){
            if (mHomeWatcher.isRegistered){
                return;
            }
        }

        mHomeWatcher = new HomeWatcher(this);
        mHomeWatcher.setOnHomePressedListener(new HomeWatcher.OnHomePressedListener() {
            @Override
            public void onHomePressed() {
                int  value = PrefsController.getInt(getString(R.string.key_screen_status), EnumPinAction.NONE.ordinal());
                EnumPinAction action = EnumPinAction.values()[value];
                switch (action){
                    case NONE:{
                        PrefsController.putInt(getString(R.string.key_screen_status),EnumPinAction.SCREEN_LOCK.ordinal());
                        Navigator.onMoveToVerifyPin(SuperSafeApplication.getInstance().getActivity(),EnumPinAction.NONE);                        break;
                    }
                    default:{
                        Utils.Log(TAG,"Nothing to do");
                    }
                }
                mHomeWatcher.stopWatch();
            }
            @Override
            public void onHomeLongPressed() {
            }
        });
        mHomeWatcher.startWatch();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        System.gc();
    }

    protected void showToast(String text) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
        mToast.show();
    }


    protected void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    protected void setDisplayHomeAsUpEnabled(boolean check){
        actionBar.setDisplayHomeAsUpEnabled(check);
    }

    protected void setNoTitle(){
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    protected void setFullScreen(){
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    protected void setAdjustScreen(){
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        /*android:windowSoftInputMode="adjustPan|adjustResize"*/
    }

    protected String getResourceString(int code) {
        return getResources().getString(code);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home :{
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (onStartCount > 1) {
            this.overridePendingTransition(android.R.anim.fade_in,
                    android.R.anim.fade_out);
        } else if (onStartCount == 1) {
            onStartCount++;
        }
    }

    /*Hidden camera*/

    /**
     * Add camera preview to the root of the activity layout.
     *
     * @return {@link CameraPreview} that was added to the view.
     */
    private CameraPreview addPreView() {
        //create fake camera view
        CameraPreview cameraSourceCameraPreview = new CameraPreview(this, this);
        cameraSourceCameraPreview.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        View view = ((ViewGroup) getWindow().getDecorView().getRootView()).getChildAt(0);

        if (view instanceof LinearLayout) {
            LinearLayout linearLayout = (LinearLayout) view;

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(1, 1);
            linearLayout.addView(cameraSourceCameraPreview, params);
        } else if (view instanceof RelativeLayout) {
            RelativeLayout relativeLayout = (RelativeLayout) view;

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(1, 1);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            relativeLayout.addView(cameraSourceCameraPreview, params);
        } else if (view instanceof FrameLayout) {
            FrameLayout frameLayout = (FrameLayout) view;

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(1, 1);
            frameLayout.addView(cameraSourceCameraPreview, params);
        } else {
            throw new RuntimeException("Root view of the activity/fragment cannot be other than Linear/Relative/Frame layout");
        }
        return cameraSourceCameraPreview;
    }



    /**
     * Start the hidden camera. Make sure that you check for the runtime permissions before you start
     * the camera.
     *
     * @param cameraConfig camera configuration {@link CameraConfig}
     */
    @RequiresPermission(Manifest.permission.CAMERA)
    protected void startCamera(CameraConfig cameraConfig) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) { //check if the camera permission is available
            onCameraError(CameraError.ERROR_CAMERA_PERMISSION_NOT_AVAILABLE);
        } else if (cameraConfig.getFacing() == CameraFacing.FRONT_FACING_CAMERA
                && !HiddenCameraUtils.isFrontCameraAvailable(this)) {   //Check if for the front camera
            onCameraError(CameraError.ERROR_DOES_NOT_HAVE_FRONT_CAMERA);
        } else {
            mCachedCameraConfig = cameraConfig;
            mCameraPreview.startCameraInternal(cameraConfig);
        }
    }

    /**
     * Call this method to capture the image using the camera you initialized. Don't forget to
     * initialize the camera using {@link #startCamera(CameraConfig)} before using this function.
     */

    protected void takePicture() {
        if (mCameraPreview != null) {
            if (mCameraPreview.isSafeToTakePictureInternal()) {
                mCameraPreview.takePictureInternal();
            }
        } else {
            throw new RuntimeException("Background camera not initialized. Call startCamera() to initialize the camera.");
        }
    }

    /**
     * Stop and release the camera forcefully.
     */
    protected void stopCamera() {
        mCachedCameraConfig = null;    //Remove config.
        if (mCameraPreview != null) mCameraPreview.stopPreviewAndFreeCamera();
    }

    @Override
    public void onImageCapture(@NonNull File imageFile, @NonNull String pin) {

    }

    @Override
    public void onCameraError(int errorCode) {

    }


}
