package co.tpcreative.supersafe.ui.camera;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import com.google.android.cameraview.CameraView;

import butterknife.BindView;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.activity.BaseActivity;
import co.tpcreative.supersafe.common.controller.GalleryCameraMediaManager;
import co.tpcreative.supersafe.common.controller.ServiceManager;
import co.tpcreative.supersafe.common.util.Utils;

import co.tpcreative.supersafe.model.MainCategories;

public class CameraActivity extends BaseActivity implements
        ActivityCompat.OnRequestPermissionsResultCallback{

    private static final String TAG = CameraActivity.class.getSimpleName();

    private static final int[] FLASH_OPTIONS = {
            CameraView.FLASH_AUTO,
            CameraView.FLASH_OFF,
            CameraView.FLASH_ON,
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
    @BindView(R.id.take_picture)
    FloatingActionButton take_picture;

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
                        int facing = mCameraView.getFacing();
                        mCameraView.setFacing(facing == CameraView.FACING_FRONT ?
                                CameraView.FACING_BACK : CameraView.FACING_FRONT);
                    }
                    break;
                case R.id.btnDone :
                    onBackPressed();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        if (mCameraView != null) {
            mCameraView.addCallback(mCallback);
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
    }


    @Override
    protected void onResume() {
        super.onResume();
        mCameraView.start();
        Utils.Log(TAG,"onResume");
    }

    @Override
    protected void onPause() {
        mCameraView.stop();
        Utils.Log(TAG,"onPause");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utils.Log(TAG,"onDestroy");
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


    private CameraView.Callback mCallback
            = new CameraView.Callback() {
        @Override
        public void onCameraOpened(CameraView cameraView) {
            Utils.Log(TAG, "onCameraOpened");
        }
        @Override
        public void onCameraClosed(CameraView cameraView) {
            Utils.Log(TAG, "onCameraClosed");
        }

        @Override
        public void onPictureTaken(CameraView cameraView, final byte[] data,int orientation) {
            Utils.Log(TAG, "onPictureTaken " + data.length);
            Toast.makeText(cameraView.getContext(), R.string.picture_taken, Toast.LENGTH_SHORT).show();
            if (MainCategories.getInstance().intent_localCategoriesId==null){
                Utils.Log(TAG, "Local id is null");
                return;
            }
            isReload = true;
            ServiceManager.getInstance().onSaveDataOnCamera(data);
        }
    };


}
