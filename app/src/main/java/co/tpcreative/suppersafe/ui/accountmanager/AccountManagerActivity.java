package co.tpcreative.suppersafe.ui.accountmanager;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.ftinc.kit.util.SizeUtils;
import com.google.gson.Gson;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import butterknife.BindView;
import butterknife.OnClick;
import co.tpcreative.suppersafe.R;
import co.tpcreative.suppersafe.common.activity.BaseActivity;
import co.tpcreative.suppersafe.common.controller.PrefsController;
import co.tpcreative.suppersafe.common.controller.ServiceManager;
import co.tpcreative.suppersafe.model.User;

public class AccountManagerActivity extends BaseActivity {

    private static final String TAG = AccountManagerActivity.class.getSimpleName();

    private SlidrConfig mConfig;
    @BindView(R.id.tvEmail)
    TextView tvEmail;
    @BindView(R.id.tvStatus)
    TextView tvStatus;
    @BindView(R.id.tvLicenseStatus)
    TextView tvLicenseStatus;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_manager);
        //android O fix bug orientation
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        int primary = getResources().getColor(R.color.colorPrimary);
        int secondary = getResources().getColor(R.color.colorPrimaryDark);

        mConfig = new SlidrConfig.Builder()
                .primaryColor(primary)
                .secondaryColor(secondary)
                .position(SlidrPosition.LEFT)
                .velocityThreshold(2400)
                .touchSize(SizeUtils.dpToPx(this, 32))
                .build();
        Slidr.attach(this, mConfig);

        final User mUser = User.getInstance().getUserInfo();
        if (mUser!=null){
            if (mUser.verified){
                tvStatus.setText("Verified");
            }
            tvEmail.setText(mUser.email);
        }
    }

    @OnClick(R.id.btnSignOut)
    public void onSignOut(View view){
        Log.d(TAG,"sign out");
        final User mUser = User.getInstance().getUserInfo();
        if (mUser!=null){
            mUser.verified = false;
            PrefsController.putString(getString(R.string.key_user),new Gson().toJson(mUser));
            ServiceManager.getInstance().onSignOut(new ServiceManager.ServiceManagerListener() {
                @Override
                public void onCompletedDisconnect() {
                    onBackPressed();
                }

                @Override
                public void onCompletedSignOut() {
                    onBackPressed();
                }
                @Override
                public void onError() {
                    onBackPressed();
                }
            });
        }
    }

}
