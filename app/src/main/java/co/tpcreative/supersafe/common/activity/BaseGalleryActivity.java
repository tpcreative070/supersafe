package co.tpcreative.supersafe.common.activity;
import android.app.FragmentManager;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.LayoutRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import com.snatik.storage.Storage;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.HomeWatcher;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.SensorFaceUpDownChangeNotifier;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.controller.SingletonManager;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.ThemeUtil;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumPinAction;
import co.tpcreative.supersafe.model.ThemeApp;
import co.tpcreative.supersafe.ui.lockscreen.EnterPinActivity;
import co.tpcreative.supersafe.ui.move_gallery.MoveGalleryFragment;
import spencerstudios.com.bungeelib.Bungee;

public abstract class BaseGalleryActivity extends AppCompatActivity implements  MoveGalleryFragment.OnGalleryAttachedListener, SensorFaceUpDownChangeNotifier.Listener{
    Unbinder unbinder;
    protected ActionBar actionBar ;
    int onStartCount = 0;
    private HomeWatcher mHomeWatcher;
    public static final String TAG = BaseGalleryActivity.class.getSimpleName();
    protected Storage storage;
    private MoveGalleryFragment fragment;

    protected void attachFragment(int layoutId) {
        fragment = (MoveGalleryFragment) MoveGalleryFragment.newInstance();
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(layoutId, fragment).commit();
    }

    protected void openAlbum() {
        fragment.openAlbum();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        actionBar = getSupportActionBar();
        onStartCount = 1;
        if (savedInstanceState == null) {
            this.overridePendingTransition(R.animator.anim_slide_in_left,
                    R.animator.anim_slide_out_left);
        } else {
            onStartCount = 2;
        }
        storage = new Storage(this);
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    public Resources.Theme getTheme() {
        Resources.Theme theme = super.getTheme();
        final ThemeApp result = ThemeApp.getInstance().getThemeInfo();
        if (result!=null){
            theme.applyStyle(ThemeUtil.getSlideThemeId(result.getId()), true);
        }
        return theme;
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        Log.d(TAG,"action here");
        unbinder = ButterKnife.bind(this);
    }

    protected void onFaceDown(final boolean isFaceDown){
        if (isFaceDown){
            final boolean result = PrefsController.getBoolean(getString(R.string.key_face_down_lock),false);
            if (result){
                Navigator.onMoveToFaceDown(SuperSafeApplication.getInstance());
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Utils.Log(TAG,"onStop....");
    }

    @Override
    protected void onDestroy() {
        if (unbinder != null){
            unbinder.unbind();
        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        SensorFaceUpDownChangeNotifier.getInstance().remove(this);
        Utils.Log(TAG,"onPause");
        if (mHomeWatcher!=null){
            Utils.Log(TAG,"Stop home watcher....");
            mHomeWatcher.stopWatch();
        }
    }

    @Override
    protected void onResume() {
        Utils.Log(TAG,"Action here........onResume");
        SensorFaceUpDownChangeNotifier.getInstance().addListener(this);
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
                        Utils.onHomePressed();
                        onStopListenerAWhile();
                        break;
                    }
                    default:{
                        Utils.Log(TAG,"Nothing to do on home " +action.name());
                        break;
                    }
                }
                mHomeWatcher.stopWatch();
            }
            @Override
            public void onHomeLongPressed() {
                Utils.Log(TAG,"Pressed long home button");
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

    protected void setDisplayHomeAsUpEnabled(boolean check){
        actionBar.setDisplayHomeAsUpEnabled(check);
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
        int  value = PrefsController.getInt(getString(R.string.key_screen_status), EnumPinAction.NONE.ordinal());
        EnumPinAction action = EnumPinAction.values()[value];
        switch (action){
            case SCREEN_LOCK:{
                if (!SingletonManager.getInstance().isVisitLockScreen()){
                    Navigator.onMoveToVerifyPin(SuperSafeApplication.getInstance().getActivity(),EnumPinAction.NONE);                        Utils.Log(TAG,"Pressed home button");
                    SingletonManager.getInstance().setVisitLockScreen(true);
                    Utils.Log(TAG,"Verify pin");
                }else{
                    Utils.Log(TAG,"Verify pin already");
                }
                break;
            }
            default:{
                Utils.Log(TAG,"Nothing to do on start " +action.name());
                break;
            }
        }
        if (SingletonManager.getInstance().isAnimation()){
            if (onStartCount > 1) {
                this.overridePendingTransition(R.animator.anim_slide_in_right,
                        R.animator.anim_slide_out_right);
            } else if (onStartCount == 1) {
                onStartCount++;
            }
        }else{
            Bungee.zoom(this);
            SingletonManager.getInstance().setAnimation(true);
            Utils.Log(TAG,"onStartBaseGalleryActivity");
        }
    }

    protected abstract void onStopListenerAWhile();
}
