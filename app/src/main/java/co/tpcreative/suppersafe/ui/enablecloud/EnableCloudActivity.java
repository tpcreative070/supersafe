package co.tpcreative.suppersafe.ui.enablecloud;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.google.gson.Gson;

import butterknife.BindView;
import butterknife.OnClick;
import co.tpcreative.suppersafe.R;
import co.tpcreative.suppersafe.common.activity.BaseGoogleApi;
import co.tpcreative.suppersafe.common.controller.PrefsController;
import co.tpcreative.suppersafe.common.controller.ServiceManager;
import co.tpcreative.suppersafe.common.request.UserCloudRequest;
import co.tpcreative.suppersafe.ui.verifyaccount.VerifyAccountActivity;


public class EnableCloudActivity extends BaseGoogleApi implements EnableCloudView {

    private static final String TAG = EnableCloudActivity.class.getSimpleName();

    public static final int ENABLE_CLOUD = 100;

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
        Log.d(TAG,"user another account");
        btnUserAnotherAccount.setEnabled(false);
        btnLinkGoogleDrive.setEnabled(false);
        onShowWarningAnotherAccount();
    }

    @Override
    protected void onDriveClientReady() {
        Log.d(TAG,"Google drive ready");
        ServiceManager.getInstance().onGetLastSignIn();
        UserCloudRequest request = new UserCloudRequest();
        request.user_id = presenter.mUser.email;
        request.cloud_id = presenter.mUser.cloud_id;
        presenter.onAddUserCloud(request);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        btnUserAnotherAccount.setEnabled(true);
        btnLinkGoogleDrive.setEnabled(true);
        switch (requestCode){
            case EnableCloudActivity.ENABLE_CLOUD :
                if (resultCode == Activity.RESULT_OK) {
                    finish();
                }
                break;
            case VerifyAccountActivity.REQUEST_CODE_EMAIL :
                if (resultCode == Activity.RESULT_OK) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    Log.d(TAG,"accountName : " + accountName);
                    final String cloud_id = presenter.mUser.cloud_id;
                    if (cloud_id==null){
                        presenter.mUser.cloud_id = accountName;
                        ServiceManager.getInstance().onSignOut(new ServiceManager.ServiceManagerListener() {
                            @Override
                            public void onCompletedDisconnect() {

                            }
                            @Override
                            public void onCompletedSignOut() {
                                signIn(accountName);
                            }

                            @Override
                            public void onError() {
                                signIn(accountName);
                            }
                        });

                    }
                    else{
                        if (accountName.equals(cloud_id)){
                            presenter.mUser.cloud_id = accountName;
                            ServiceManager.getInstance().onSignOut(new ServiceManager.ServiceManagerListener() {
                                @Override
                                public void onCompletedDisconnect() {

                                }

                                @Override
                                public void onCompletedSignOut() {
                                    onShowProgressDialog();
                                    signIn(accountName);
                                }

                                @Override
                                public void onError() {
                                    onShowProgressDialog();
                                    signIn(accountName);
                                }
                            });
                        }
                        else{
                           onShowWarning(cloud_id);
                        }
                    }
                }
                break;
              case VerifyAccountActivity.REQUEST_CODE_EMAIL_ANOTHER_ACCOUNT:
                  if (resultCode == Activity.RESULT_OK) {
                      String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                      Log.d(TAG,"accountName : " + accountName);
                      presenter.mUser.cloud_id = accountName;
                      ServiceManager.getInstance().onSignOut(new ServiceManager.ServiceManagerListener() {
                          @Override
                          public void onCompletedDisconnect() {

                          }

                          @Override
                          public void onCompletedSignOut() {
                              onShowProgressDialog();
                              signIn(accountName);
                          }

                          @Override
                          public void onError() {
                              onShowProgressDialog();
                              signIn(accountName);
                          }
                      });
                  }
                  break;
            default:
                Log.d(TAG,"Nothing action");
                break;
        }
    }

    @Override
    public void startLoading() {

    }

    @Override
    public void stopLoading() {

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
                            Log.d(TAG,"positive");
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

        new MaterialStyledDialog.Builder(this)
                .setTitle(R.string.user_another_google_drive_title)
                .setDescription(builder.toString())
                .setHeaderDrawable(R.drawable.ic_drive_cloud)
                .setHeaderScaleType(ImageView.ScaleType.CENTER_INSIDE)
                .setHeaderColor(R.color.colorPrimary)
                .setCancelable(true)
                .setPositiveText(R.string.user_another)
                .setNegativeText(R.string.cancel)
                .setCheckBox(false,R.string.enable_cloud)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Log.d(TAG,"positive");
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

    @Override
    public void showError(String message) {
        Log.d(TAG,""+message);
        onSopProgressDialog();
    }

    @Override
    public void showSuccessful(String message) {
        onSopProgressDialog();
        presenter.mUser.cloud_id= message;
        PrefsController.putString(getString(R.string.key_user),new Gson().toJson(presenter.mUser));
        onBackPressed();
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

    public void onSopProgressDialog(){
        if (progressDialog!=null){
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.unbindView();
    }
}
