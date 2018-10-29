package co.tpcreative.supersafe.ui.cloudmanager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import com.google.gson.Gson;
import java.util.List;
import butterknife.BindView;
import butterknife.OnClick;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.activity.BaseGoogleApi;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.controller.ServiceManager;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
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

    @BindView(R.id.tvValueSupersafeSpace)
    TextView tvValueSupersafeSpace;
    @BindView(R.id.tvValueOtherSpace)
    TextView tvValueOtherSpace;
    @BindView(R.id.tvValueFreeSpace)
    TextView tvValueFreeSpace;

    @BindView(R.id.btnSwitchPauseSync)
    SwitchCompat btnSwitchPauseSync;
    @BindView(R.id.tvDriveAccount)
    TextView tvDriveAccount;
    private CloudManagerPresenter presenter;
    private boolean isPauseCloudSync = true;

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
        btnSwitchPauseSync.setOnCheckedChangeListener(this);

        String lefFiles = String.format(getString(R.string.left),"100");
        tvLeft.setText(lefFiles);

        String updated = String.format(getString(R.string.left),"0");
        tvUploaded.setText(updated);
        onShowUI();
    }

    public void onShowUI(){
        tvSupersafeSpace.setVisibility(View.VISIBLE);
        tvOtherSpace.setVisibility(View.VISIBLE);
        tvFreeSpace.setVisibility(View.VISIBLE);
        final User mUser = User.getInstance().getUserInfo();
        Utils.Log(TAG,"user :"+ new Gson().toJson(mUser));
        boolean isThrow = false;
        if (mUser!=null){
            DriveAbout driveAbout = mUser.driveAbout;
            tvDriveAccount.setText(mUser.cloud_id);
            try {
                String superSafeSpace = ConvertUtils.byte2FitMemorySize(driveAbout.inAppUsed);
                tvValueSupersafeSpace.setText(superSafeSpace);
            }
            catch (Exception e){
                tvValueOtherSpace.setText(getString(R.string.calculating));
                isThrow = true;
            }
            try {
                String superSafeSpace = ConvertUtils.byte2FitMemorySize(driveAbout.quotaBytesUsedAggregate);
                tvValueOtherSpace.setText(superSafeSpace);
            }
            catch (Exception e){
                tvValueOtherSpace.setText(getString(R.string.calculating));
                isThrow = true;
            }
            try {
                final  long result = driveAbout.quotaBytesTotal - driveAbout.quotaBytesUsedAggregate;
                String superSafeSpace = ConvertUtils.byte2FitMemorySize(result);
                tvValueFreeSpace.setText(superSafeSpace);
            }
            catch (Exception e){
                tvValueFreeSpace.setText(getString(R.string.calculating));
                isThrow = true;
            }

            try {
                if (mUser.syncData!=null){
                    String lefFiles = String.format(getString(R.string.left),""+mUser.syncData.left);
                    tvLeft.setText(lefFiles);
                }
            }
            catch (Exception e){
                String lefFiles = String.format(getString(R.string.left),"100");
                tvLeft.setText(lefFiles);
                isThrow = true;
            }

            try {
                if (mUser.syncData!=null){
                    String uploadedFiles = String.format(getString(R.string.uploaded),""+(100 - mUser.syncData.left));
                    tvUploaded.setText(uploadedFiles);
                }
            }
            catch (Exception e){
                String uploadedFiles = String.format(getString(R.string.uploaded),"0");
                tvUploaded.setText(uploadedFiles);
                isThrow = true;
            }

            if (isThrow){
                tvSupersafeSpace.setVisibility(View.INVISIBLE);
                tvOtherSpace.setVisibility(View.INVISIBLE);
                tvFreeSpace.setVisibility(View.INVISIBLE);
            }
        }
    }

    public void onShowSwitch(){
        final boolean pause_cloud_sync = PrefsController.getBoolean(getString(R.string.key_pause_cloud_sync),false);
        btnSwitchPauseSync.setChecked(pause_cloud_sync);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_cloud_manager, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_item_refresh:{
                presenter.onGetDriveAbout();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
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
        switch (status){
            case GET_LIST_FILES_IN_APP:{
                onShowUI();
                break;
            }
        }
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
                isPauseCloudSync = b;
                PrefsController.putBoolean(getString(R.string.key_pause_cloud_sync),b);
                break;
            }
        }
    }

    @Override
    public void onNotifier(EnumStatus status) {
        switch (status){
            case FINISH:{
                finish();
                break;
            }
        }
    }

    @Override
    public void onOrientationChange(boolean isFaceDown) {
        onFaceDown(isFaceDown);
    }

    @OnClick(R.id.btnRemoveLimit)
    public void onRemoveLimit(View view){
        Navigator.onMoveToPremium(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        onRegisterHomeWatcher();
        SuperSafeApplication.getInstance().writeKeyHomePressed(CloudManagerActivity.class.getSimpleName());
        presenter.onGetDriveAbout();
        onShowSwitch();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!isPauseCloudSync){
            ServiceManager.getInstance().onSyncDataOwnServer("0");
        }
    }
}
