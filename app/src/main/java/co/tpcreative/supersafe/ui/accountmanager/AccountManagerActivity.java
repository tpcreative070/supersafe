package co.tpcreative.supersafe.ui.accountmanager;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import co.tpcreative.supersafe.common.controller.SingletonPremiumTimer;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.User;
import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import fr.castorflex.android.circularprogressbar.CircularProgressDrawable;

public class AccountManagerActivity extends BaseGoogleApi implements BaseView ,SingletonPremiumTimer.SingletonPremiumTimerListener{

    private static final String TAG = AccountManagerActivity.class.getSimpleName();
    @BindView(R.id.tvEmail)
    TextView tvEmail;
    @BindView(R.id.tvStatus)
    TextView tvStatus;
    @BindView(R.id.tvLicenseStatus)
    TextView tvLicenseStatus;
    @BindView(R.id.progressbar_circular)
    CircularProgressBar mCircularProgressBar;
    @BindView(R.id.btnSignOut)
    Button btnSignOut;
    @BindView(R.id.tvStatusAccount)
    TextView tvStatusAccount;
    @BindView(R.id.tvPremiumLeft)
    TextView tvPremiumLeft;
    private AccountManagerPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_manager);
        presenter = new AccountManagerPresenter();
        presenter.bindView(this);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        onDrawOverLay(this);

        final User mUser = User.getInstance().getUserInfo();
        if (mUser!=null){
            tvEmail.setText(mUser.email);
            if (mUser.verified){
               tvStatusAccount.setTextColor(getResources().getColor(R.color.ColorBlueV1));
               tvStatusAccount.setText(getString(R.string.verified));
            }
            else{
                tvStatusAccount.setTextColor(getResources().getColor(R.color.red));
                tvStatusAccount.setText(getString(R.string.unverified));
            }
        }
        setProgressValue();
        Utils.Log(TAG,"account: "+ new Gson().toJson(mUser));
        String value = String.format(getString(R.string.your_complimentary_premium),"30");
        if (mUser.premium.status){
            tvLicenseStatus.setTextColor(getResources().getColor(R.color.ColorBlueV1));
            tvLicenseStatus.setText(getString(R.string.premium));
        }
        else {
            tvLicenseStatus.setText(getString(R.string.free));
        }
        tvPremiumLeft.setText(value);
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
    public void onPremiumTimer(String days, String hours, String minutes, String seconds) {
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String value = String.format(getString(R.string.your_complimentary_premium_remaining),days);
                    tvPremiumLeft.setText(value);
                }
            });
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


    @OnClick(R.id.btnUpgrade)
    public void onClickedUpgrade(View view){
        Navigator.onMoveToPremium(this);
    }

    @Override
    public void onOrientationChange(boolean isFaceDown) {
        onFaceDown(isFaceDown);
    }

    @Override
    protected void onResume() {
        super.onResume();
        onRegisterHomeWatcher();
        SingletonPremiumTimer.getInstance().setListener(this);
        SuperSafeApplication.getInstance().writeKeyHomePressed(AccountManagerActivity.class.getSimpleName());
    }

    public void setProgressValue(){
        CircularProgressDrawable circularProgressDrawable;
        CircularProgressDrawable.Builder b = new CircularProgressDrawable.Builder(this)
                .colors(getResources().getIntArray(R.array.gplus_colors))
                .sweepSpeed(2)
                .rotationSpeed(2)
                .strokeWidth(Utils.dpToPx(3))
                .style(CircularProgressDrawable.STYLE_ROUNDED);
        mCircularProgressBar.setIndeterminateDrawable(circularProgressDrawable = b.build());
        // /!\ Terrible hack, do not do this at home!
        circularProgressDrawable.setBounds(0,
                0,
                mCircularProgressBar.getWidth(),
                mCircularProgressBar.getHeight());
        mCircularProgressBar.setVisibility(View.INVISIBLE);
        mCircularProgressBar.setVisibility(View.INVISIBLE);
    }

    @OnClick(R.id.btnSignOut)
    public void onSignOut(View view){
        Log.d(TAG,"sign out");
        final User mUser = User.getInstance().getUserInfo();
        if (mUser!=null){
            signOut(new ServiceManager.ServiceManagerSyncDataListener() {
                @Override
                public void onCompleted() {
                    mUser.verified = false;
                    mUser.driveConnected = false;
                    PrefsController.putString(getString(R.string.key_user),new Gson().toJson(mUser));
                    onBackPressed();
                }
                @Override
                public void onError() {
                }

                @Override
                public void onCancel() {

                }
            });
        }
    }

    @Override
    protected void onDriveClientReady() {

    }

    @Override
    protected void onDriveSuccessful() {
        Log.d(TAG,"onDriveSuccessful");
        btnSignOut.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDriveError() {
        Log.d(TAG,"onDriveError");
    }

    @Override
    protected void onDriveSignOut() {
        Log.d(TAG,"onDriveSignOut");
    }

    @Override
    protected void onDriveRevokeAccess() {
        Log.d(TAG,"onDriveRevokeAccess");
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

    @Override
    public void onError(String message, EnumStatus status) {

    }

    @Override
    public void onError(String message) {

    }

    @Override
    public void onSuccessful(String message) {

    }

    @Override
    public void onSuccessful(String message, EnumStatus status) {

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
}
