package co.tpcreative.suppersafe.ui.checksystem;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import com.gc.materialdesign.views.ProgressBarCircularIndeterminate;
import com.google.gson.Gson;

import butterknife.BindView;
import co.tpcreative.suppersafe.R;
import co.tpcreative.suppersafe.common.Navigator;
import co.tpcreative.suppersafe.common.activity.BaseActivity;
import co.tpcreative.suppersafe.common.controller.PrefsController;
import co.tpcreative.suppersafe.common.request.UserCloudRequest;
import co.tpcreative.suppersafe.ui.enablecloud.EnableCloudActivity;

public class CheckSystemActivity extends BaseActivity implements CheckSystemView{

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
        startLoading();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                presenter.onUserCloudChecking();
            }
        },5000);


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
}
