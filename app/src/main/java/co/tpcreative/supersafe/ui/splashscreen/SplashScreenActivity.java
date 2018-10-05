package co.tpcreative.supersafe.ui.splashscreen;
import android.os.Build;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.snatik.storage.Storage;

import java.util.List;

import butterknife.BindView;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.SensorOrientationChangeNotifier;
import co.tpcreative.supersafe.common.activity.BaseActivity;
import co.tpcreative.supersafe.common.controller.GalleryCameraMediaManager;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.controller.ServiceManager;
import co.tpcreative.supersafe.common.controller.SingletonPrivateFragment;
import co.tpcreative.supersafe.common.response.DriveResponse;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumPinAction;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.Items;
import co.tpcreative.supersafe.model.room.InstanceGenerator;

public class SplashScreenActivity extends BaseActivity implements SensorOrientationChangeNotifier.Listener{

    private Handler handler = new Handler();
    private final int TIMER = 1000;
    private String value = "" ;
    private boolean grant_access;
    private boolean isRunning;
    private static final String TAG = SplashScreenActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        value  = SuperSafeApplication.getInstance().readKey();
        grant_access = PrefsController.getBoolean(getString(R.string.key_grant_access),false);
        isRunning = PrefsController.getBoolean(getString(R.string.key_running),false);
        if (SuperSafeApplication.getInstance().isGrantAccess()){
            grant_access = true;
        }
        else{
            grant_access = false;
        }
        SuperSafeApplication.getInstance().initFolder();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (grant_access){
                    if (isRunning){
                        if(!"".equals(value)){
                            PrefsController.putInt(getString(R.string.key_screen_status),EnumPinAction.SPLASH_SCREEN.ordinal());
                            Navigator.onMoveToMainTab(SplashScreenActivity.this);
                        }
                        else{
                            Navigator.onMoveSetPin(SplashScreenActivity.this,false);

                        }
                    }
                    else{
                        Navigator.onMoveToDashBoard(SplashScreenActivity.this);
                    }
                }
                else{
                    Navigator.onMoveGrantAccess(SplashScreenActivity.this);
                }
                finish();
            }
        },TIMER);

        Log.d(TAG,"" + SuperSafeApplication.getInstance().getDeviceId());

        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        int version = Build.VERSION.SDK_INT;
        String versionRelease = Build.VERSION.RELEASE;

        Log.e(TAG, "manufacturer " + manufacturer
                + " \n model " + model
                + " \n version " + version
                + " \n versionRelease " + versionRelease
        );
        ServiceManager.getInstance().onStartService();
        ServiceManager.getInstance().setUploadData(false);
        ServiceManager.getInstance().setDownloadData(false);
        ServiceManager.getInstance().setDeleteOwnCloud(false);
        ServiceManager.getInstance().setDeleteSyncCLoud(false);
        ServiceManager.getInstance().setGetListCategories(false);
        ServiceManager.getInstance().setCategoriesSync(false);
        ServiceManager.getInstance().setDeleteAlbum(false);

        Utils.onWriteLog("^^^--------------------------------Launch App----------------------------^^^", null);
        Utils.onWriteLog(Utils.DeviceInfo(), EnumStatus.DEVICE_ABOUT);

    }

    @Override
    public void onStillScreenLock(EnumStatus status) {
        super.onStillScreenLock(status);
        switch (status){
            case FINISH:{
                finish();
                break;
            }
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
    }

    @Override
    protected void onPause() {
        super.onPause();
        SensorOrientationChangeNotifier.getInstance().remove(this);
    }

}
