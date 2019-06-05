package co.tpcreative.supersafe.ui.accountmanager;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
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
import co.tpcreative.supersafe.common.adapter.DividerItemDecoration;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.controller.ServiceManager;
import co.tpcreative.supersafe.common.controller.SingletonPremiumTimer;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.AppLists;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.User;
import co.tpcreative.supersafe.ui.albumcover.AlbumCoverActivity;

public class AccountManagerActivity extends BaseGoogleApi implements BaseView ,SingletonPremiumTimer.SingletonPremiumTimerListener,AccountManagerAdapter.ItemSelectedListener{

    private static final String TAG = AccountManagerActivity.class.getSimpleName();
    @BindView(R.id.tvEmail)
    TextView tvEmail;
    @BindView(R.id.tvStatus)
    TextView tvStatus;
    @BindView(R.id.tvLicenseStatus)
    TextView tvLicenseStatus;
    @BindView(R.id.btnSignOut)
    Button btnSignOut;
    @BindView(R.id.tvStatusAccount)
    TextView tvStatusAccount;
    @BindView(R.id.tvPremiumLeft)
    TextView tvPremiumLeft;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.rlPremiumComplimentary)
    RelativeLayout rlPremiumComplimentary;
    @BindView(R.id.rlPremium)
    RelativeLayout rlPremium;
    @BindView(R.id.scrollView)
    ScrollView scrollView;
    private AccountManagerPresenter presenter;
    private AccountManagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_manager);
        initRecycleView();
        presenter = new AccountManagerPresenter();
        presenter.bindView(this);
        presenter.getData();
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        onDrawOverLay(this);
        onUpdatedView();
        onStartOverridePendingTransition();
        scrollView.smoothScrollTo(0,0);
    }

    public void onUpdatedView(){

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

        final boolean isPremium = User.getInstance().isPremium();
        if (isPremium){
            tvLicenseStatus.setTextColor(getResources().getColor(R.color.ColorBlueV1));
            tvLicenseStatus.setText(getString(R.string.premium));
            rlPremium.setVisibility(View.VISIBLE);
            rlPremiumComplimentary.setVisibility(View.GONE);
        }
        else{
            rlPremium.setVisibility(View.GONE);
            rlPremiumComplimentary.setVisibility(View.VISIBLE);
            if (User.getInstance().isPremiumComplimentary()){
                tvLicenseStatus.setTextColor(getResources().getColor(R.color.ColorBlueV1));
                tvLicenseStatus.setText(getString(R.string.premium));
                if (SingletonPremiumTimer.getInstance().getDaysLeft()!=null){
                    String dayLeft = SingletonPremiumTimer.getInstance().getDaysLeft();
                    String sourceString = Utils.getFontString(R.string.your_complimentary_premium_remaining,dayLeft);
                    tvPremiumLeft.setText(Html.fromHtml(sourceString));
                }
            }
            else{
                tvLicenseStatus.setText(getString(R.string.free));
                tvPremiumLeft.setText(getString(R.string.premium_expired));
                tvPremiumLeft.setTextColor(getResources().getColor(R.color.red_300));
            }
        }
    }

    public void initRecycleView(){
        adapter = new AccountManagerAdapter(getLayoutInflater(),this,this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onClickItem(int position) {
        final AppLists app = presenter.mList.get(position);
        boolean isInstalled = app.isInstalled;
        if (!isInstalled){
            Uri uri = Uri.parse("market://details?id=" + app.packageName);
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            // To count with Play market backstack, After pressing back button,
            // to taken back to our application, we need to add following flags to intent.
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            try {
                startActivity(goToMarket);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=" + app.packageName)));
            }
        }
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
        //SuperSafeApplication.getInstance().writeKeyHomePressed(AccountManagerActivity.class.getSimpleName());
        final boolean isPremium = User.getInstance().isPremium();
        if (!isPremium){
            SingletonPremiumTimer.getInstance().setListener(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utils.Log(TAG,"OnDestroy");
        EventBus.getDefault().unregister(this);
        presenter.unbindView();
        SingletonPremiumTimer.getInstance().setListener(null);
    }

    @Override
    public void onOrientationChange(boolean isFaceDown) {
        onFaceDown(isFaceDown);
    }

    @Override
    public void onPremiumTimer(String days, String hours, String minutes, String seconds) {
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String value =Utils.getFontString(R.string.your_complimentary_premium_remaining,days);
                    tvPremiumLeft.setText(Html.fromHtml(value));
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
    protected void startServiceNow() {

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
        switch (status){
            case RELOAD:{
                adapter.setDataSource(presenter.mList);
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
        return false;
    }

}
