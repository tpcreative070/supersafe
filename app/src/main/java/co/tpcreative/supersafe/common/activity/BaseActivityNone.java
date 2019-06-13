package co.tpcreative.supersafe.common.activity;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import com.snatik.storage.Storage;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.ThemeApp;

public abstract class BaseActivityNone extends AppCompatActivity{
    Unbinder unbinder;
    protected ActionBar actionBar ;
    int onStartCount = 0;
    public static final String TAG = BaseActivity.class.getSimpleName();
    protected Storage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        actionBar = getSupportActionBar();
        onStartCount = 1;
        storage = new Storage(this);
    }

    @Override
    public Resources.Theme getTheme() {
        Resources.Theme theme = super.getTheme();
        final ThemeApp result = ThemeApp.getInstance().getThemeInfo();
        if (result!=null){
            theme.applyStyle(R.style.AppTheme_Share, true);
        }
        return theme;
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        Log.d(TAG,"action here");
        unbinder = ButterKnife.bind(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Utils.Log(TAG,"onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Utils.Log(TAG,"onStop....");
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
    protected void onResume() {
        Utils.Log(TAG,"onResume....");
        super.onResume();
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
    }
}
