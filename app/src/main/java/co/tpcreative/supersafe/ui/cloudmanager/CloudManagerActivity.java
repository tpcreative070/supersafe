package co.tpcreative.supersafe.ui.cloudmanager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import com.google.gson.Gson;
import java.util.List;
import butterknife.BindView;
import butterknife.OnClick;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.activity.BaseGoogleApi;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.util.ConvertUtils;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.DriveAbout;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.User;

public class CloudManagerActivity extends BaseGoogleApi implements CompoundButton.OnCheckedChangeListener,BaseView<Long>{

    private static String TAG = CloudManagerActivity.class.getSimpleName();
    @BindView(R.id.tvUploaded)
    TextView tvUploaded;
    @BindView(R.id.tvLeft)
    TextView tvLeft;
    @BindView(R.id.btnRemoveLimit)
    Button btnRemoveLimit;
    @BindView(R.id.tvSupersafeSpace)
    TextView tvSupersafeSpace;
    @BindView(R.id.tvOtherSpace)
    TextView tvOtherSpace;
    @BindView(R.id.tvFreeSpace)
    TextView tvFreeSpace;
    @BindView(R.id.btnSwitchPauseSync)
    SwitchCompat btnSwitchPauseSync;
    @BindView(R.id.btnSwitchSyncWithWifi)
    Button btnSwitchSyncWithWifi;

    private CloudManagerPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_manager);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        onDrawOverLay(this);

        presenter = new CloudManagerPresenter();
        presenter.bindView(this);
        presenter.onGetList();

        final User mUser = User.getInstance().getUserInfo();
        if (mUser!=null){
            DriveAbout driveAbout = mUser.driveAbout;
            //Utils.Log(TAG,"Response :" + new Gson().toJson(driveAbout));
            try {
                String superSafeSpace = String.format(getString(R.string.supersafe_used_space), ConvertUtils.byte2FitMemorySize(driveAbout.quotaBytesUsed));
                tvSupersafeSpace.setText(superSafeSpace);
            }
            catch (Exception e){
                tvSupersafeSpace.setText(getString(R.string.calculating));
            }
            try {
                String superSafeSpace = String.format(getString(R.string.other_used_space), ConvertUtils.byte2FitMemorySize(driveAbout.quotaBytesUsedAggregate));
                tvOtherSpace.setText(superSafeSpace);
            }
            catch (Exception e){
                tvOtherSpace.setText(getString(R.string.calculating));
            }

            try {
                final  long result = driveAbout.quotaBytesTotal - driveAbout.quotaBytesUsedAggregate;
                String superSafeSpace = String.format(getString(R.string.free_space), ConvertUtils.byte2FitMemorySize(result));
                tvFreeSpace.setText(superSafeSpace);
            }
            catch (Exception e){
                tvFreeSpace.setText(getString(R.string.calculating));
            }

        }
    }

    @Override
    public void onStartLoading(EnumStatus status) {

    }

    @Override
    public void onStopLoading(EnumStatus status) {

    }

    @Override
    public void onError(String message) {

    }

    @Override
    public void onError(String message, EnumStatus status) {
       switch (status){
           case REQUEST_ACCESS_TOKEN:{
               Utils.Log(TAG,"Error response "+ message);
               getAccessToken();
               break;
           }
           default:{
               Utils.Log(TAG,"Error response "+ message);
               break;
           }
       }
    }

    @Override
    public void onSuccessful(String message) {

    }

    @Override
    public void onSuccessful(String message, EnumStatus status) {
        Utils.Log(TAG,"Successful response "+ message);
    }

    @Override
    public void onSuccessful(String message, EnumStatus status, Long object) {

    }

    @Override
    public void onSuccessful(String message, EnumStatus status, List list) {
        Utils.Log(TAG,"Successful response "+ message);
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()){
            case R.id.btnSwitchPauseSync :{
                break;
            }
            case R.id.btnSwitchSyncWithWifi :{
                break;
            }
        }
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

    @OnClick(R.id.btnRemoveLimit)
    public void onRemoveLimit(View view){

    }

    @Override
    protected void onResume() {
        super.onResume();
        onRegisterHomeWatcher();
    }

    @Override
    protected void onDriveError() {
        Log.d(TAG, "onDriveError");
    }

    @Override
    protected void onDriveSignOut() {
        Log.d(TAG, "onDriveSignOut");
    }

    @Override
    protected void onDriveRevokeAccess() {
        Log.d(TAG, "onDriveRevokeAccess");
    }

    @Override
    protected void onDriveClientReady() {

    }

    @Override
    protected void onDriveSuccessful() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

}
