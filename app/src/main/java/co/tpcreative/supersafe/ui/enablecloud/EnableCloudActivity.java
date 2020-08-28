package co.tpcreative.supersafe.ui.enablecloud;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.google.gson.Gson;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.util.List;
import butterknife.BindView;
import butterknife.OnClick;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.activity.BaseGoogleApi;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.controller.ServiceManager;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.request.UserCloudRequest;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.ThemeApp;
import co.tpcreative.supersafe.model.User;

public class EnableCloudActivity extends BaseGoogleApi implements BaseView {

    private static final String TAG = EnableCloudActivity.class.getSimpleName();
    private EnableCloudPresenter presenter;
    private ProgressDialog progressDialog;
    @BindView(R.id.btnLinkGoogleDrive)
    Button btnLinkGoogleDrive;
    @BindView(R.id.btnUserAnotherAccount)
    Button btnUserAnotherAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enable_cloud);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().hide();
        presenter = new EnableCloudPresenter();
        presenter.bindView(this);
        presenter.onUserInfo();
        Utils.Log(TAG,"Enable cloud...........");
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EnumStatus event) {
        switch (event){
            case FINISH:{
                Navigator.onMoveToFaceDown(this);
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
        onRegisterHomeWatcher();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utils.Log(TAG,"OnDestroy");
        EventBus.getDefault().unregister(this);
        presenter.unbindView();
    }

    @Override
    protected void onStopListenerAWhile() {
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onOrientationChange(boolean isFaceDown) {
        onFaceDown(isFaceDown);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(RESULT_OK,intent);
        super.onBackPressed();
    }


    @OnClick(R.id.btnLinkGoogleDrive)
    public void onGoogleDrive(View view){
        btnUserAnotherAccount.setEnabled(false);
        btnLinkGoogleDrive.setEnabled(false);
       final String cloud_id = presenter.mUser.cloud_id;
       if (cloud_id==null){
           ServiceManager.getInstance().onPickUpNewEmail(this);
       }
       else{
           ServiceManager.getInstance().onPickUpExistingEmail(this,cloud_id);
       }
    }

    @OnClick(R.id.btnUserAnotherAccount)
    public void onUserAnotherAccount(View view){
        Utils.Log(TAG,"user another account");
        btnUserAnotherAccount.setEnabled(false);
        btnLinkGoogleDrive.setEnabled(false);
        onShowWarningAnotherAccount();
    }

    @Override
    protected void onDriveClientReady() {
        Utils.Log(TAG,"Google drive ready");
        UserCloudRequest request = new UserCloudRequest(presenter.mUser.email,presenter.mUser.cloud_id, SuperSafeApplication.getInstance().getDeviceId());
        presenter.onAddUserCloud(request);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        btnUserAnotherAccount.setEnabled(true);
        btnLinkGoogleDrive.setEnabled(true);
        switch (requestCode){
            case Navigator.ENABLE_CLOUD :
                if (resultCode == Activity.RESULT_OK) {
                    finish();
                }
                break;
            case Navigator.REQUEST_CODE_EMAIL :
                if (resultCode == Activity.RESULT_OK) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    Utils.Log(TAG,"accountName : " + accountName);
                    final String cloud_id = presenter.mUser.cloud_id;
                    if (cloud_id==null){
                        presenter.mUser.cloud_id = accountName;
                        Utils.Log(TAG,"Call Sign out");
                        signOut(new ServiceManager.ServiceManagerSyncDataListener() {
                            @Override
                            public void onCompleted() {
                                signIn(accountName);
                            }

                            @Override
                            public void onError() {
                                signIn(accountName);
                            }

                            @Override
                            public void onCancel() {

                            }
                        });
                    }
                    else{
                        if (accountName.equals(cloud_id)){
                            presenter.mUser.cloud_id = accountName;
                            Utils.Log(TAG,"Call Sign out");
                            signOut(new ServiceManager.ServiceManagerSyncDataListener() {
                                @Override
                                public void onCompleted() {
                                    onShowProgressDialog();
                                    signIn(accountName);
                                }
                                @Override
                                public void onError() {
                                    onShowProgressDialog();
                                    signIn(accountName);
                                }

                                @Override
                                public void onCancel() {

                                }
                            });
                        }
                        else{
                           onShowWarning(cloud_id);
                        }
                    }
                }
                break;
              case Navigator.REQUEST_CODE_EMAIL_ANOTHER_ACCOUNT:
                  if (resultCode == Activity.RESULT_OK) {
                      String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                      Utils.Log(TAG,"accountName : " + accountName);
                      presenter.mUser.cloud_id = accountName;
                      Utils.Log(TAG,"Call Sign out");
                      signOut(new ServiceManager.ServiceManagerSyncDataListener() {
                          @Override
                          public void onCompleted() {
                              onShowProgressDialog();
                              signIn(accountName);
                          }
                          @Override
                          public void onError() {
                              onShowProgressDialog();
                              signIn(accountName);
                          }
                          @Override
                          public void onCancel() {

                          }
                      });

                  }
                  break;
            default:
                Utils.Log(TAG,"Nothing action");
                break;
        }
    }

    @Override
    public void onStartLoading(EnumStatus status) {

    }

    @Override
    public void onStopLoading(EnumStatus status) {

    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }

    public void onShowWarning(final String cloud_id){
        String value = String.format(getString(R.string.choose_the_same_account),cloud_id);
            new MaterialStyledDialog.Builder(this)
                    .setTitle(R.string.not_the_same_account)
                    .setDescription(value)
                    .setHeaderDrawable(R.drawable.ic_drive_cloud)
                    .setHeaderScaleType(ImageView.ScaleType.CENTER_INSIDE)
                    .setHeaderColor(R.color.colorPrimary)
                    .setCancelable(true)
                    .setPositiveText(R.string.select_again)
                    .setNegativeText(R.string.cancel)
                    .setCheckBox(false,R.string.enable_cloud)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            Utils.Log(TAG,"positive");
                        }
                    })
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                        }
                    })
                    .show();

    }

    public void onShowWarningAnotherAccount(){
        StringBuilder builder = new StringBuilder();
        builder.append("If you use another Google Drive");
        builder.append("\n");
        builder.append("1. Files sync with previous Google Drive will stop");
        builder.append("\n");
        builder.append("2. All of your local files will be synced to the new Google Drive");

        ThemeApp themeApp = ThemeApp.getInstance().getThemeInfo();
        new MaterialStyledDialog.Builder(this)
                .setTitle(R.string.user_another_google_drive_title)
                .setDescription(builder.toString())
                .setHeaderDrawable(R.drawable.ic_drive_cloud)
                .setHeaderScaleType(ImageView.ScaleType.CENTER_INSIDE)
                .setHeaderColor(themeApp.getAccentColor())
                .setCancelable(true)
                .setPositiveText(R.string.user_another)
                .setNegativeText(R.string.cancel)
                .setCheckBox(false,R.string.enable_cloud)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Utils.Log(TAG,"positive");
                        final String cloud_id = presenter.mUser.cloud_id;
                        if (cloud_id==null){
                            ServiceManager.getInstance().onPickUpNewEmailNoTitle(EnableCloudActivity.this,presenter.mUser.email);
                        }
                        else{
                            ServiceManager.getInstance().onPickUpNewEmailNoTitle(EnableCloudActivity.this,cloud_id);
                        }
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        btnUserAnotherAccount.setEnabled(true);
                        btnLinkGoogleDrive.setEnabled(true);
                    }
                })
                .show();
    }

    public void onShowProgressDialog(){
        if (progressDialog!=null){
            if (progressDialog.isShowing()){
                return;
            }
        }
        progressDialog = new ProgressDialog(EnableCloudActivity.this);
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public void onStopProgressDialog(){
        if (progressDialog!=null){
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onDriveSuccessful() {
        Utils.Log(TAG,"onDriveSuccessful");
    }

    @Override
    protected void onDriveError() {
        Utils.Log(TAG,"onDriveError");
        onStopProgressDialog();
    }

    @Override
    protected void onDriveSignOut() {
        Utils.Log(TAG,"onDriveSignOut");
    }

    @Override
    protected void onDriveRevokeAccess() {
        Utils.Log(TAG,"onDriveRevokeAccess");
    }

    @Override
    public void onError(String message) {

    }

    @Override
    public void onSuccessful(String message) {

    }

    @Override
    public void onError(String message, EnumStatus status) {
        Utils.Log(TAG,""+message);
        switch (status){
            case CREATE:{
                onStopProgressDialog();
                break;
            }
        }
    }

    @Override
    protected void startServiceNow() {
        ServiceManager.getInstance().onStartService();
        onStopProgressDialog();
    }

    @Override
    public void onSuccessful(String message, EnumStatus status) {
        switch (status){
            case CREATE:{
                onStopProgressDialog();
                final User mUser = User.getInstance().getUserInfo();
                mUser.cloud_id= message;
                mUser.driveConnected = true;
                PrefsController.putString(getString(R.string.key_user),new Gson().toJson(mUser));
                presenter.mUser = mUser;
                Utils.Log(TAG,"Finsh enable cloud.........................");
                //ServiceManager.getInstance().onSyncDataOwnServer("0");
                ServiceManager.getInstance().onGetUserInfo();
                ServiceManager.getInstance().onGetListCategoriesSync();
                onBackPressed();
                break;
            }
        }
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public void onSuccessful(String message, EnumStatus status, Object object) {

    }

    @Override
    public void onSuccessful(String message, EnumStatus status, List list) {

    }

    @Override
    protected boolean isSignIn() {
        return true;
    }
}
