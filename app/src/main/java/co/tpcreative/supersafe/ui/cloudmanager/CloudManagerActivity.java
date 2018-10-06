package co.tpcreative.supersafe.ui.cloudmanager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.OnClick;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.activity.BaseGoogleApi;
import co.tpcreative.supersafe.model.EnumStatus;

public class CloudManagerActivity extends BaseGoogleApi{

    private static String TAG = CloudManagerActivity.class.getSimpleName();
    @BindView(R.id.btnSignIn)
    Button btnSignIn;
    @BindView(R.id.btnDisconnect)
    Button btnDisconnect;
    @BindView(R.id.btnDriveAbout)
    Button btnDriveAbout;
    @BindView(R.id.tvStatus)
    TextView tvStatus;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_manager);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        onDrawOverLay(this);
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

    @Override
    protected void onResume() {
        super.onResume();
        onRegisterHomeWatcher();
    }

    @OnClick(R.id.btnSignIn)
    public void onClickedSignIn(View view) {
        signIn("butlerichotel@gmail.com");
    }

    @OnClick(R.id.btnDisconnect)
    public void onClickedDisconnect(View view) {
        signOut();
    }

    @OnClick(R.id.btnDriveAbout)
    public void onDriveAbout(View view) {
        getAccessToken();
    }

    @OnClick(R.id.btnChooseActivity)
    public void onChooseActivity(View view) {
    }

    @OnClick(R.id.btnUploadFileExtension)
    public void onUploadFileExtension(View view){

    }

    @OnClick(R.id.btnCreateFolder)
    public void onCreateFolder(View view){

    }

    @OnClick(R.id.btnCheckInAppFolder)
    public void onCheckInAppFolder(View view){

    }

    @OnClick(R.id.btnCreateInAppFolder)
    public void onCreateInAppFolder(View view){

    }

    @OnClick(R.id.btnUploadFileInAppFolder)
    public void onUploadFileInAppFolder(View view){

    }

    @OnClick(R.id.btnGetListFolder)
    public void onClickedListInApp(){

    }

    @OnClick(R.id.btnMainCategories)
    public void onClickedInitMainCategories(View view){

    }

    @OnClick(R.id.btnPrintInAppFolder)
    public void onClickedPrintInAppFolder(View view){

    }

    @Override
    protected void onDriveClientReady() {
        btnSignIn.setVisibility(View.GONE);
        btnDriveAbout.setVisibility(View.VISIBLE);
        btnDisconnect.setVisibility(View.VISIBLE);
        //ServiceManager.getInstance().onRefreshData();
    }

    @Override
    protected void onDriveSuccessful() {
        Log.d(TAG, "onDriveSuccessful");
        btnSignIn.setVisibility(View.GONE);
        btnDriveAbout.setVisibility(View.VISIBLE);
        btnDisconnect.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDriveError() {
        Log.d(TAG, "onDriveError");
        btnSignIn.setVisibility(View.VISIBLE);
        btnDriveAbout.setVisibility(View.GONE);
        btnDisconnect.setVisibility(View.GONE);
    }

    @Override
    protected void onDriveSignOut() {
        Log.d(TAG, "onDriveSignOut");
        btnSignIn.setVisibility(View.VISIBLE);
        btnDriveAbout.setVisibility(View.GONE);
        btnDisconnect.setVisibility(View.GONE);
    }

    @Override
    protected void onDriveRevokeAccess() {
        Log.d(TAG, "onDriveRevokeAccess");
        btnSignIn.setVisibility(View.VISIBLE);
        btnDriveAbout.setVisibility(View.GONE);
        btnDisconnect.setVisibility(View.GONE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }



}
