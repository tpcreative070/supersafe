package co.tpcreative.supersafe.ui.test;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.activity.BaseActivity;
import co.tpcreative.supersafe.common.activity.BaseVerifyPinActivity;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.hiddencamera.CameraConfig;
import co.tpcreative.supersafe.common.hiddencamera.config.CameraFacing;
import co.tpcreative.supersafe.common.hiddencamera.config.CameraFocus;
import co.tpcreative.supersafe.common.hiddencamera.config.CameraImageFormat;
import co.tpcreative.supersafe.common.hiddencamera.config.CameraResolution;
import co.tpcreative.supersafe.common.hiddencamera.config.CameraRotation;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.model.EnumStatus;

public class TestActivity extends BaseVerifyPinActivity {

    private CameraConfig mCameraConfig;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        onInitHiddenCamera();
    }

    @Override
    public void onOrientationChange(boolean isFaceDown) {

    }

    @Override
    public void onNotifier(EnumStatus status) {

    }

    public void onInitHiddenCamera() {
        final boolean value = PrefsController.getBoolean(getString(R.string.key_break_in_alert), false);
        if (!value) {
            return;
        }
        mCameraConfig = new CameraConfig()
                .getBuilder(this)
                .setCameraFacing(CameraFacing.FRONT_FACING_CAMERA)
                .setCameraResolution(CameraResolution.HIGH_RESOLUTION)
                .setImageFormat(CameraImageFormat.FORMAT_JPEG)
                .setImageRotation(CameraRotation.ROTATION_270)
                .setCameraFocus(CameraFocus.AUTO)
                .build();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            //Start camera preview
            startCamera(mCameraConfig);
        }
    }

    public void onTakePicture(String pin) {
        final boolean value = PrefsController.getBoolean(getString(R.string.key_break_in_alert), false);
        if (!value) {
            return;
        }
        mCameraConfig.getBuilder(SuperSafeApplication.getInstance())
                .setPin(pin);
        mCameraConfig.getBuilder(SuperSafeApplication.getInstance()).
                setImageFile(SuperSafeApplication.getInstance().getDefaultStorageFile(CameraImageFormat.FORMAT_JPEG));
        takePicture();
    }



}
