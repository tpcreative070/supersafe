package co.tpcreative.supersafe.ui.checksystem;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.gc.materialdesign.views.ProgressBarCircularIndeterminate;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import butterknife.BindView;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.activity.BaseGoogleApi;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.controller.ServiceManager;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.request.SignInRequest;
import co.tpcreative.supersafe.common.request.UserCloudRequest;
import co.tpcreative.supersafe.common.request.VerifyCodeRequest;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.ThemeApp;

public class CheckSystemActivity extends BaseGoogleApi implements BaseView {

    private static final String TAG = CheckSystemActivity.class.getSimpleName();
    @BindView(R.id.progressBarCircularIndeterminate)
    ProgressBarCircularIndeterminate progressBarCircularIndeterminate;
    private CheckSystemPresenter presenter;
    Handler handler = new Handler();
    String email ;

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
        onStartLoading(EnumStatus.OTHER);
        if (presenter.googleOauth != null) {
            String email = presenter.googleOauth.email;
            if (email.equals(presenter.mUser.email)){
                presenter.onCheckUser(presenter.googleOauth.email,presenter.googleOauth.email);
            }
            else{
                this.email = email;
                VerifyCodeRequest request = new VerifyCodeRequest();
                request.email = email;
                request.other_email = email;
                request.user_id = presenter.mUser.email;
                request._id = presenter.mUser._id;
                presenter.onChangeEmail(request);
            }
        } else {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    presenter.onUserCloudChecking();
                }
            }, 5000);
        }
        onStartOverridePendingTransition();
        ThemeApp themeApp = ThemeApp.getInstance().getThemeInfo();
        progressBarCircularIndeterminate.setBackgroundColor(getResources().getColor(themeApp.getAccentColor()));
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
        //SuperSafeApplication.getInstance().writeKeyHomePressed(CheckSystemActivity.class.getSimpleName());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utils.Log(TAG,"OnDestroy");
        EventBus.getDefault().unregister(this);
        presenter.unbindView();
    }


    @Override
    public void onOrientationChange(boolean isFaceDown) {
        onFaceDown(isFaceDown);
    }


    @Override
    public Context getContext() {
        return getApplicationContext();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Navigator.ENABLE_CLOUD:
                if (resultCode == Activity.RESULT_OK) {
                    Log.d(TAG, "onBackPressed onActivity Result");
                    onBackPressed();
                }
                break;
            default:
                Log.d(TAG, "Nothing action");
                break;
        }
    }

    @Override
    protected void onDriveClientReady() {
        Log.d(TAG, "onDriveClient");
        presenter.mUser.driveConnected = true;
        PrefsController.putString(getString(R.string.key_user), new Gson().toJson(presenter.mUser));
        UserCloudRequest request = new UserCloudRequest();
        request.cloud_id = presenter.mUser.email;
        request.user_id = presenter.mUser.email;
        presenter.onAddUserCloud(request);
    }

    @Override
    public Activity getActivity() {
        return this;
    }


    public void onVerifyInputCode(String email) {

        Utils.Log(TAG, " User..." + new Gson().toJson(presenter.mUser));
        try {

            MaterialDialog.Builder dialog = new MaterialDialog.Builder(this);
            String next = "<font color='#0091EA'>(" + email + ")</font>";
            String value = getString(R.string.description_pin_code, next);
            dialog.title(getString(R.string.verify_email));
            dialog.content(Html.fromHtml(value));
            dialog.theme(Theme.LIGHT);
            dialog.inputType(InputType.TYPE_CLASS_NUMBER);
            dialog.input(getString(R.string.pin_code), null, false, new MaterialDialog.InputCallback() {
                @Override
                public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                    Log.d(TAG, "call input code");
                    VerifyCodeRequest request = new VerifyCodeRequest();
                    request.email = presenter.mUser.email;
                    request.code = input.toString().trim();
                    request._id = presenter.mUser._id;
                    presenter.onVerifyCode(request);
                }
            });
            dialog.positiveText(getString(R.string.ok));
            dialog.negativeText(getString(R.string.cancel));
            dialog.onNegative(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    onStopLoading(EnumStatus.OTHER);
                    onBackPressed();
                }
            });
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDriveSuccessful() {

    }

    @Override
    protected void onDriveError() {

    }

    @Override
    protected void onDriveSignOut() {

    }

    @Override
    protected void onDriveRevokeAccess() {

    }

    @Override
    public void onStartLoading(EnumStatus status) {
        progressBarCircularIndeterminate.setVisibility(View.VISIBLE);
    }

    @Override
    public void onStopLoading(EnumStatus status) {
        progressBarCircularIndeterminate.setVisibility(View.GONE);
    }

    @Override
    public void onError(String message) {

    }

    @Override
    public void onSuccessful(String message) {

    }

    @Override
    public void onSuccessful(String message, EnumStatus status) {
        switch (status) {
            case RESEND_CODE: {
                Navigator.onEnableCloud(this);
                break;
            }
            case CLOUD_ID_EXISTING: {
                if (presenter.mUser != null) {
                    presenter.mUser.cloud_id = message;
                    Log.d(TAG, "CLOUD_ID_EXISTING : " + message);
                    PrefsController.putString(getString(R.string.key_user), new Gson().toJson(presenter.mUser));
                }
                Navigator.onEnableCloud(this);
                break;
            }
            case CREATE: {
                Utils.Log(TAG,"CREATE.............action");
                onBackPressed();
                break;
            }
            case SEND_EMAIL: {
                if (presenter.googleOauth != null) {
                    onVerifyInputCode(presenter.googleOauth.email);
                }
                break;
            }
            case USER_ID_EXISTING: {
                Log.d(TAG, "USER_ID_EXISTING : " + message);
                break;
            }
            case CHANGE_EMAIL:{
                presenter.onCheckUser(presenter.mUser.email,presenter.mUser.other_email);
                break;
            }
            case VERIFY_CODE: {
                Utils.Log(TAG, "VERIFY_CODE...........action here");
                if (presenter.googleOauth != null) {
                    if (presenter.googleOauth.isEnableSync) {
                        Log.d(TAG, "Syn google drive");
                        signOut(new ServiceManager.ServiceManagerSyncDataListener() {
                            @Override
                            public void onCompleted() {
                                onStartLoading(status);
                                signIn(presenter.mUser.email);
                            }

                            @Override
                            public void onError() {
                                onStartLoading(status);
                                signIn(presenter.mUser.email);
                            }

                            @Override
                            public void onCancel() {

                            }
                        });
                    } else {
                        Log.d(TAG, "Google drive Sync is disable");
                        onStopLoading(status);
                        onBackPressed();
                    }
                } else {
                    Toast.makeText(this, "Oauth is null", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    @Override
    public void onError(String message, EnumStatus status) {
        switch (status) {
            case RESEND_CODE: {
                break;
            }
            case CREATE: {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                onStopLoading(status);
                break;
            }
            case SIGN_UP: {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                onStopLoading(status);
                break;
            }
            case SIGN_IN: {
                onStopLoading(status);
                Toast.makeText(this, "" + message, Toast.LENGTH_SHORT).show();
                onBackPressed();
                break;
            }
            case VERIFY_CODE: {
                Toast.makeText(this, "Failed verify code", Toast.LENGTH_SHORT).show();
                if (presenter.mUser != null) {
                    onVerifyInputCode(presenter.mUser.email);
                }
                break;
            }
            case CHANGE_EMAIL:{
                SignInRequest request = new SignInRequest();
                request.email = email;
                presenter.onSignIn(request);
                break;
            }
            case CLOUD_ID_EXISTING: {
                Navigator.onEnableCloud(this);
                break;
            }
        }
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
