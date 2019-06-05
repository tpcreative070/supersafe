package co.tpcreative.supersafe.ui.splashscreen;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import com.google.gson.Gson;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import butterknife.BindView;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.activity.BaseActivityNoneSlide;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.listener.Listener;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumPinAction;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.MainCategories;
import co.tpcreative.supersafe.model.ThemeApp;
import co.tpcreative.supersafe.model.User;
import co.tpcreative.supersafe.model.room.InstanceGenerator;


public class SplashScreenActivity extends BaseActivityNoneSlide {

    private String value = "" ;
    private boolean grant_access;
    private boolean isRunning;
    private static final String TAG = SplashScreenActivity.class.getSimpleName();
    @BindView(R.id.rlScreen)
    RelativeLayout rlScreen;
    private int DELAY = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        /*Black list*/
        if (SuperSafeApplication.getInstance().getDeviceId().equals("66801ac00252fe84")){
            finish();
        }
        ThemeApp themeApp = ThemeApp.getInstance().getThemeInfo();
        rlScreen.setBackgroundColor(getResources().getColor(themeApp.getPrimaryColor()));
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
        Utils.Log(TAG,"Key "+ value);
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        int version = Build.VERSION.SDK_INT;
        String versionRelease = Build.VERSION.RELEASE;
        Utils.Log(TAG, "manufacturer " + manufacturer
                + " \n model " + model
                + " \n version " + version
                + " \n versionRelease " + versionRelease
        );
        MainCategories.getInstance().getList();
        Utils.onWriteLog("^^^--------------------------------Launch App----------------------------^^^", null);
        Utils.onWriteLog(Utils.DeviceInfo(), EnumStatus.DEVICE_ABOUT);
        final int count  = InstanceGenerator.getInstance(this).getLatestItem();
        Utils.Log(TAG,"Max "+count);
        Utils.onObserveData(DELAY, new Listener() {
            @Override
            public void onStart() {
                if (grant_access){
                    if (isRunning){
                        if(!"".equals(value)){
                            PrefsController.putInt(getString(R.string.key_screen_status),EnumPinAction.SPLASH_SCREEN.ordinal());
                            Navigator.onMoveToMainTab(SplashScreenActivity.this);
                        }
                        else{
                            SuperSafeApplication.getInstance().deleteFolder();
                            SuperSafeApplication.getInstance().initFolder();
                            InstanceGenerator.getInstance(SplashScreenActivity.this).onCleanDatabase();
                            PrefsController.putString(getString(R.string.key_user),new Gson().toJson(new User()));
                            MainCategories.getInstance().getList();
                            PrefsController.putBoolean(getString(R.string.key_request_sign_out_google_drive),true);
                            Navigator.onMoveToDashBoard(SplashScreenActivity.this);
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
        });
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EnumStatus event) {
        switch (event){
            case FINISH:{
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utils.Log(TAG,"OnDestroy");
        EventBus.getDefault().unregister(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_tab, menu);
        return true;
    }

    @Override
    public void onOrientationChange(boolean isFaceDown) {

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

}
