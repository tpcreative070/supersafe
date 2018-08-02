package co.tpcreative.suppersafe.common.activity;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import co.tpcreative.suppersafe.R;


public class BaseActivity extends AppCompatActivity {
    Unbinder unbinder;
    protected ActionBar actionBar ;
    int onStartCount = 0;
    public static final String TAG = BaseActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        actionBar = getSupportActionBar();
        onStartCount = 1;
        if (savedInstanceState == null) // 1st time
        {
            this.overridePendingTransition(R.animator.anim_slide_in_left,
                    R.animator.anim_slide_out_left);
        } else // already created so reverse animation
        {
            onStartCount = 2;
        }
    }

    protected float getRandom(float range, float startsfrom) {
        return (float) (Math.random() * range) + startsfrom;
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        Log.d(TAG,"action here");
        unbinder = ButterKnife.bind(this);
    }

    @Override
    protected void onDestroy() {
        if (unbinder != null)
            unbinder.unbind();
        super.onDestroy();
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
            this.overridePendingTransition(R.animator.anim_slide_in_right,
                    R.animator.anim_slide_out_right);
        } else if (onStartCount == 1) {
            onStartCount++;
        }
    }

}
