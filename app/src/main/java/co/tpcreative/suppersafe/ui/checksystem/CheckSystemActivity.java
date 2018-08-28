package co.tpcreative.suppersafe.ui.checksystem;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.gc.materialdesign.views.ProgressBarCircularIndeterminate;
import com.google.gson.Gson;
import butterknife.BindView;
import co.tpcreative.suppersafe.R;
import co.tpcreative.suppersafe.common.Navigator;
import co.tpcreative.suppersafe.common.activity.BaseGoogleApi;
import co.tpcreative.suppersafe.common.controller.PrefsController;
import co.tpcreative.suppersafe.common.controller.ServiceManager;
import co.tpcreative.suppersafe.common.request.UserCloudRequest;
import co.tpcreative.suppersafe.common.request.VerifyCodeRequest;
import co.tpcreative.suppersafe.ui.enablecloud.EnableCloudActivity;

public class CheckSystemActivity extends BaseGoogleApi implements CheckSystemView{

    private static final String TAG = CheckSystemActivity.class.getSimpleName();

    @BindView(R.id.progressBarCircularIndeterminate)
    ProgressBarCircularIndeterminate progressBarCircularIndeterminate;
    private CheckSystemPresenter presenter;
    Handler handler =new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_system);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().hide();
        presenter = new CheckSystemPresenter();
        presenter.bindView(this);
        presenter.getIntent(this);
        startLoading();

        if (presenter.googleOauth!=null){
            presenter.onCheckUser(presenter.googleOauth.email);
        }
        else{
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    presenter.onUserCloudChecking();
                }
            },5000);
        }

    }

    @Override
    public void showError(String message) {
        Log.d(TAG,message);
        Navigator.onEnableCloud(this);
    }

    @Override
    public void showSuccessful(String cloud_id) {
        Log.d(TAG,cloud_id);
        if (presenter.mUser!=null){
            presenter.mUser.cloud_id = cloud_id;
            PrefsController.putString(getString(R.string.key_user),new Gson().toJson(presenter.mUser));
        }
        Navigator.onEnableCloud(this);
    }

    @Override
    public void startLoading() {
        progressBarCircularIndeterminate.setVisibility(View.VISIBLE);
    }

    @Override
    public void stopLoading() {
        progressBarCircularIndeterminate.setVisibility(View.GONE);
    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(RESULT_OK,intent);
        super.onBackPressed();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case EnableCloudActivity.ENABLE_CLOUD :
                if (resultCode == Activity.RESULT_OK) {
                   onBackPressed();
                }
                break;
            default:
                Log.d(TAG,"Nothing action");
                break;
        }
    }

    @Override
    protected void onDriveClientReady() {
        Log.d(TAG,"onDriveClient");
        UserCloudRequest request = new UserCloudRequest();
        request.cloud_id = presenter.mUser.email;
        request.user_id  = presenter.mUser.email;
        presenter.onAddUserCloud(request);
    }

    @Override
    public void onShowUserCloud(boolean error, String message) {
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
        if (!error){

           onBackPressed();
        }
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public void sendEmailSuccessful() {
        if (presenter.mUser!=null){
            onVerifyInputCode(presenter.mUser.email);
        }
    }

    @Override
    public void onSignUpFailed(String message) {
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }


    @Override
    public void showUserExisting(String userId,boolean isExisting) {
        Log.d(TAG,""+isExisting);
        //signIn(userId);
        Toast.makeText(this,userId +" is existing  :" + isExisting,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showSuccessfulVerificationCode() {
       if (presenter.googleOauth!=null){
           if (presenter.googleOauth.isEnableSync){
               Log.d(TAG,"Syn google drive");
               ServiceManager.getInstance().onSignOut(new ServiceManager.ServiceManagerListener() {
                   @Override
                   public void onCompletedDisconnect() {

                   }
                   @Override
                   public void onCompletedSignOut() {
                       signIn(presenter.mUser.email);
                   }

                   @Override
                   public void onError() {
                       signIn(presenter.mUser.email);
                   }
               });
           }
           else{
               onBackPressed();
           }
       }
       else{
           Toast.makeText(this,"Oauth is null",Toast.LENGTH_SHORT).show();
       }
    }

    @Override
    public void showFailedVerificationCode() {
        Toast.makeText(this,"Failed verify code",Toast.LENGTH_SHORT).show();
        if (presenter.mUser!=null){
            onVerifyInputCode(presenter.mUser.email);
        }
    }

    public void onVerifyInputCode(String email){
        MaterialDialog.Builder dialog =  new MaterialDialog.Builder(this);
        String next = "<font color='#0091EA'>("+email+")</font>";
        String value  = getString(R.string.description_pin_code, next);
        dialog.title(getString(R.string.verify_email));
        dialog.content(Html.fromHtml(value));
        dialog.theme(Theme.LIGHT);
        dialog.input(getString(R.string.pin_code), null, false, new MaterialDialog.InputCallback() {
            @Override
            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                Log.d(TAG,"call input code");
                VerifyCodeRequest request = new VerifyCodeRequest();
                request.email = presenter.mUser.email;
                request.code = input.toString().trim();
                presenter.onVerifyCode(request);
            }
        });
        dialog.positiveText(getString(R.string.ok));
        dialog.negativeText(getString(R.string.cancel));
        EditText editText = dialog.show().getInputEditText();
        editText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                view.setFocusable(true);
                view.setFocusableInTouchMode(true);
                return false;
            }
        });
        editText.setFocusable(false);
    }

}
