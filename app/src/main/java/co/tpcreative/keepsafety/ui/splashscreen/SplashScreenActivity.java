package co.tpcreative.keepsafety.ui.splashscreen;
import android.os.Handler;
import android.os.Bundle;
import co.tpcreative.keepsafety.R;
import co.tpcreative.keepsafety.common.Navigator;
import co.tpcreative.keepsafety.common.activity.BaseActivity;
import co.tpcreative.keepsafety.common.controller.PrefsController;

public class SplashScreenActivity extends BaseActivity {

    private Handler handler = new Handler();
    private final int TIMER = 3000;
    private String value = "" ;
    private boolean grant_access;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        value  = PrefsController.getString(getString(R.string.key_pin),"");
        grant_access = PrefsController.getBoolean(getString(R.string.key_grant_access),false);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (grant_access){
                    if(!"".equals(value)){
                        Navigator.onMoveToVerifyPin(SplashScreenActivity.this);
                    }
                    else{
                        Navigator.onMoveSetPin(SplashScreenActivity.this);
                    }
                }
                else{
                    Navigator.onMoveGrantAccess(SplashScreenActivity.this);
                }
            }
        },TIMER);
    }

}
