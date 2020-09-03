package co.tpcreative.supersafe.ui.camera;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraOptions;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.PictureResult;
import com.otaliastudios.cameraview.controls.Facing;
import com.otaliastudios.cameraview.controls.Flash;
import com.otaliastudios.cameraview.controls.Grid;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import butterknife.BindView;
import butterknife.OnClick;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.activity.BaseActivity;
import co.tpcreative.supersafe.common.controller.GalleryCameraMediaManager;
import co.tpcreative.supersafe.common.controller.ServiceManager;
import co.tpcreative.supersafe.common.entities.MainCategoryEntity;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.MainCategoryModel;
import co.tpcreative.supersafe.model.ThemeApp;

public class CameraActivity extends BaseActivity implements
        ActivityCompat.OnRequestPermissionsResultCallback{

    private static final String TAG = CameraActivity.class.getSimpleName();

    private static final Flash[] FLASH_OPTIONS = {
            Flash.AUTO,
            Flash.OFF,
            Flash.ON,
    };

    private static final int[] FLASH_ICONS = {
            R.drawable.ic_flash_auto,
            R.drawable.ic_flash_off,
            R.drawable.ic_flash_on,
    };

    private int mCurrentFlash;
    private boolean isReload;
    @BindView(R.id.camera)
    CameraView mCameraView;
    @BindView(R.id.btnSwitch)
    ImageButton btnSwitch;
    @BindView(R.id.btnDone)
    Button btnDone;
    @BindView(R.id.btnFlash)
    ImageButton btnFlash;
    @BindView(R.id.btnAutoFocus)
    ImageButton btnAutoFocus;
    @BindView(R.id.take_picture)
    FloatingActionButton take_picture;
    private ThemeApp themeApp = ThemeApp.getInstance().getThemeInfo();

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.take_picture:
                    if (mCameraView != null) {
                        if (GalleryCameraMediaManager.getInstance().isProgressing()){
                            Utils.Log(TAG, "Progressing");
                            break;
                        }
                        mCameraView.takePicture();
                        GalleryCameraMediaManager.getInstance().setProgressing(true);
                    }
                    break;
                case R.id.btnFlash:
                    if (mCameraView != null) {
                        mCurrentFlash = (mCurrentFlash + 1) % FLASH_OPTIONS.length;
                        btnFlash.setImageResource(FLASH_ICONS[mCurrentFlash]);
                        mCameraView.setFlash(FLASH_OPTIONS[mCurrentFlash]);
                    }
                    break;
                case R.id.btnSwitch:
                    if (mCameraView != null) {
                        Facing facing = mCameraView.getFacing();
                        if (facing == Facing.FRONT){
                            mCameraView.setFacing(Facing.BACK);
                        }else{
                            mCameraView.setFacing(Facing.FRONT);
                        }
                    }
                    break;
                case R.id.btnDone :
                    ServiceManager.getInstance().onPreparingSyncData();
                    onBackPressed();
                    break;
            }
        }
    };

    private MainCategoryModel mainCategories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        if (mCameraView != null) {
            mCameraView.addCameraListener(mCallback);
            mCameraView.setLifecycleOwner(this);
        }
        take_picture.setOnClickListener(mOnClickListener);
        btnDone.setOnClickListener(mOnClickListener);
        btnFlash.setOnClickListener(mOnClickListener);
        btnSwitch.setOnClickListener(mOnClickListener);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }
        try {
            Bundle bundle = getIntent().getExtras();
            mainCategories = (MainCategoryModel) bundle.get(getString(R.string.key_main_categories));
        }
        catch (Exception e){
            Utils.onWriteLog(""+e.getMessage(), EnumStatus.WRITE_FILE);
        }
        btnAutoFocus.setVisibility(View.INVISIBLE);
    }

    @OnClick(R.id.btnAutoFocus)
    public void onClickedFocus(View view){
        if (mCameraView!=null){
            if (mCameraView.getGrid() == Grid.OFF){
                btnAutoFocus.setColorFilter(SuperSafeApplication.getInstance().getResources().getColor(R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);
                mCameraView.setGrid(Grid.DRAW_3X3);
            }
            else {
                btnAutoFocus.setColorFilter(SuperSafeApplication.getInstance().getResources().getColor(themeApp.getAccentColor()), android.graphics.PorterDuff.Mode.SRC_IN);
                mCameraView.setGrid(Grid.OFF);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EnumStatus event) {
        switch (event){
            case FINISH:{
                Navigator.onMoveToFaceDown(this);
                break;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }
        if (mCameraView!=null){
            mCameraView.open();
        }
//        if (mCameraView.getAutoFocus()){
//            btnAutoFocus.setColorFilter(SuperSafeApplication.getInstance().getResources().getColor(themeApp.getAccentColor()), android.graphics.PorterDuff.Mode.SRC_IN);
//        }
//        else{
//            btnAutoFocus.setColorFilter(SuperSafeApplication.getInstance().getResources().getColor(R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);
//        }
        GalleryCameraMediaManager.getInstance().setProgressing(false);
        onRegisterHomeWatcher();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utils.Log(TAG,"OnDestroy");
        if (mCameraView!=null){
            mCameraView.destroy();
        }
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onStopListenerAWhile() {
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onOrientationChange(boolean isFaceDown) {

    }

    @Override
    protected void onPause() {
        Utils.Log(TAG,"onPause");
        super.onPause();
        if (mCameraView!=null){
            mCameraView.close();
        }
    }

    @Override
    public void onBackPressed() {
        if (isReload){
            Intent intent = new Intent();
            setResult(RESULT_OK,intent);
            Utils.Log(TAG,"onBackPressed");
        }
        super.onBackPressed();
    }

    private CameraListener mCallback = new CameraListener() {
        @Override
        public void onCameraOpened(@NonNull CameraOptions options) {
            super.onCameraOpened(options);
        }

        @Override
        public void onPictureTaken(@NonNull PictureResult result) {
            super.onPictureTaken(result);
            byte[] data = result.getData();
            Toast.makeText(CameraActivity.this, R.string.picture_taken, Toast.LENGTH_SHORT).show();
            if (mainCategories==null){
                Utils.Log(TAG, "Local id is null");
                Utils.onWriteLog("Main categories is null",EnumStatus.WRITE_FILE);
                return;
            }
            isReload = true;
            ServiceManager.getInstance().onSaveDataOnCamera(data,mainCategories);
            Utils.Log(TAG,"take picture");
        }
    };

//    private CameraView.Callback mCallback
//            = new CameraView.Callback() {
//        @Override
//        public void onCameraOpened(CameraView cameraView) {
//            Utils.Log(TAG, "onCameraOpened");
//        }
//        @Override
//        public void onCameraClosed(CameraView cameraView) {
//            Utils.Log(TAG, "onCameraClosed");
//        }
//
//        @Override
//        public void onPictureTaken(CameraView cameraView, final byte[] data,int orientation) {
//            Utils.Log(TAG, "onPictureTaken " + data.length);
//            Toast.makeText(cameraView.getContext(), R.string.picture_taken, Toast.LENGTH_SHORT).show();
//            if (mainCategories==null){
//                Utils.Log(TAG, "Local id is null");
//                Utils.onWriteLog("Main categories is null",EnumStatus.WRITE_FILE);
//                return;
//            }
//            isReload = true;
//            ServiceManager.getInstance().onSaveDataOnCamera(data,mainCategories);
//        }
//    };
}
