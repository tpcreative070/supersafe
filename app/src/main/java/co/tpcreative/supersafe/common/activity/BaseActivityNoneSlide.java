package co.tpcreative.supersafe.common.activity;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import com.snatik.storage.Storage;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.HomeWatcher;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.SensorFaceUpDownChangeNotifier;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.ThemeUtil;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumPinAction;
import co.tpcreative.supersafe.model.Theme;
import spencerstudios.com.bungeelib.Bungee;


public abstract class BaseActivityNoneSlide extends AppCompatActivity implements  SensorFaceUpDownChangeNotifier.Listener{

    Unbinder unbinder;
    int onStartCount = 0;
    private Toast mToast;
    private HomeWatcher mHomeWatcher;
    public static final String TAG = BaseActivityNoneSlide.class.getSimpleName();
    protected Storage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onStartCount = 1;
        if (savedInstanceState == null) {
            Bungee.fade(this);
        } else {
            onStartCount = 2;
        }
        storage = new Storage(this);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
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
        unbinder = ButterKnife.bind(this);
        Log.d(TAG,"action here");
    }

    @Override
    protected void onDestroy() {
        Utils.Log(TAG,"onDestroy....");
        if (mHomeWatcher!=null){
            mHomeWatcher.stopWatch();
        }
        if (unbinder != null){
            unbinder.unbind();
        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        SensorFaceUpDownChangeNotifier.getInstance().remove(this);
    }

    @Override
    protected void onResume() {
        Utils.Log(TAG,"onResume....");
        SensorFaceUpDownChangeNotifier.getInstance().addListener(this);
        if (mHomeWatcher!=null){
            if (!mHomeWatcher.isRegistered){
                onRegisterHomeWatcher();
            }
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
                        PrefsController.putInt(getString(R.string.key_screen_status),EnumPinAction.SCREEN_PRESS_HOME.ordinal());
                        break;
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
            Bungee.fade(this);
        } else if (onStartCount == 1) {
            onStartCount++;
        }
    }

}
