package co.tpcreative.supersafe.common.activity;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;
import com.snatik.storage.Storage;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.HomeWatcher;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.SensorFaceUpDownChangeNotifier;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.controller.ServiceManager;
import co.tpcreative.supersafe.common.controller.SingletonBaseActivity;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumPinAction;
import co.tpcreative.supersafe.ui.lockscreen.EnterPinActivity;
import spencerstudios.com.bungeelib.Bungee;

public abstract class BasePlayerActivity extends AppCompatActivity implements  SensorFaceUpDownChangeNotifier.Listener{
    Unbinder unbinder;
    protected ActionBar actionBar ;
    int onStartCount = 0;
    private Toast mToast;
    private HomeWatcher mHomeWatcher;
    public static final String TAG = BasePlayerActivity.class.getSimpleName();
    protected Storage storage;

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
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        Log.d(TAG,"action here");
        unbinder = ButterKnife.bind(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Utils.Log(TAG,"onStop....");
        if (mHomeWatcher!=null){
            mHomeWatcher.stopWatch();
        }
    }

    @Override
    protected void onDestroy() {
        Utils.Log(TAG,"onDestroy....");
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
        Utils.Log(TAG,"onResume....");
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
                if (!EnterPinActivity.isVisible){
                    Navigator.onMoveToVerifyPin(SuperSafeApplication.getInstance().getActivity(),EnumPinAction.NONE);                        Utils.Log(TAG,"Pressed home button");
                    EnterPinActivity.isVisible = true;
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
        if (SingletonBaseActivity.getInstance().isAnimation()){
            if (onStartCount > 1) {
                this.overridePendingTransition(R.animator.anim_slide_in_right,
                        R.animator.anim_slide_out_right);
            } else if (onStartCount == 1) {
                onStartCount++;
            }
        }else{
            Bungee.zoom(this);
            SingletonBaseActivity.getInstance().setAnimation(true);
        }
    }

    protected abstract void onStopListenerAWhile();
}
