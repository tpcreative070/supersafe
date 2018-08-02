package co.tpcreative.suppersafe.ui.splashscreen;
import android.os.Handler;
import android.os.Bundle;
import co.tpcreative.suppersafe.R;
import co.tpcreative.suppersafe.common.Navigator;
import co.tpcreative.suppersafe.common.activity.BaseActivity;
import co.tpcreative.suppersafe.common.controller.PrefsController;
import co.tpcreative.suppersafe.common.services.KeepSafetyApplication;

public class SplashScreenActivity extends BaseActivity {

    private Handler handler = new Handler();
    private final int TIMER = 1000;
    private String value = "" ;
    private boolean grant_access;
    private boolean isRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        value  = KeepSafetyApplication.getInstance().readKey();
        grant_access = PrefsController.getBoolean(getString(R.string.key_grant_access),false);
        isRunning = PrefsController.getBoolean(getString(R.string.key_running),false);
        if (KeepSafetyApplication.getInstance().isGrantAccess()){
            grant_access = true;
        }
        else{
            grant_access = false;
        }
        KeepSafetyApplication.getInstance().initFolder();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (grant_access){
                    if (isRunning){
                        if(!"".equals(value)){
                            Navigator.onMoveToVerifyPin(SplashScreenActivity.this);
                        }
                        else{
                            Navigator.onMoveSetPin(SplashScreenActivity.this);
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
    }

}
