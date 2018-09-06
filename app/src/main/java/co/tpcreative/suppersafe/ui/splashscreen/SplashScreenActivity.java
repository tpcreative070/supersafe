package co.tpcreative.suppersafe.ui.splashscreen;
import android.os.Build;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import co.tpcreative.suppersafe.R;
import co.tpcreative.suppersafe.common.Navigator;
import co.tpcreative.suppersafe.common.SensorOrientationChangeNotifier;
import co.tpcreative.suppersafe.common.activity.BaseActivity;
import co.tpcreative.suppersafe.common.controller.PrefsController;
import co.tpcreative.suppersafe.common.controller.ServiceManager;
import co.tpcreative.suppersafe.common.services.SupperSafeApplication;

public class SplashScreenActivity extends BaseActivity implements SensorOrientationChangeNotifier.Listener{

    private Handler handler = new Handler();
    private final int TIMER = 1000;
    private String value = "" ;
    private boolean grant_access;
    private boolean isRunning;
    private static final String TAG = SplashScreenActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        value  = SupperSafeApplication.getInstance().readKey();
        grant_access = PrefsController.getBoolean(getString(R.string.key_grant_access),false);
        isRunning = PrefsController.getBoolean(getString(R.string.key_running),false);
        if (SupperSafeApplication.getInstance().isGrantAccess()){
            grant_access = true;
        }
        else{
            grant_access = false;
        }
        SupperSafeApplication.getInstance().initFolder();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (grant_access){
                    if (isRunning){
                        if(!"".equals(value)){
                            Navigator.onMoveToVerifyPin(SplashScreenActivity.this,false);
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
            }
        },TIMER);

        Log.d(TAG,"" + SupperSafeApplication.getInstance().getDeviceId());

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
        ServiceManager.getInstance().setCountSyncData(0);
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
        SupperSafeApplication.getInstance().setOrientation(orientation);
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
