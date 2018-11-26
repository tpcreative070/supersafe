package co.tpcreative.supersafe.ui.theme;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import butterknife.BindView;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.activity.BaseActivity;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.controller.SingletonManagerTab;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.views.GridSpacingItemDecoration;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.ui.settings.SettingsActivity;


public class ThemeSettingsActivity extends BaseActivity implements BaseView, ThemeSettingsAdapter.ItemSelectedListener{

    private ThemeSettingsAdapter adapter;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tvPremiumDescription)
    TextView tvPremiumDescription;
    @BindView(R.id.imgIcon)
    ImageView imgIcon;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    private boolean isUpdated;
    private ThemeSettingsPresenter presenter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_settings);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        onDrawOverLay(this);
        initRecycleView(getLayoutInflater());
        presenter = new ThemeSettingsPresenter();
        presenter.bindView(this);
        presenter.getData();
        tvPremiumDescription.setText(getString(R.string.customize_your_theme));
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

    public void initRecycleView(LayoutInflater layoutInflater) {
        adapter = new ThemeSettingsAdapter(layoutInflater, getApplicationContext(), this);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getApplicationContext(), 4);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(4, 4, true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onClickItem(int position) {
        isUpdated = true;
        presenter.mTheme = presenter.mList.get(position);
        setStatusBarColored(this,presenter.mTheme.getPrimaryColor(),presenter.mTheme.getPrimaryDarkColor());
        tvTitle.setTextColor(getContext().getResources().getColor(presenter.mTheme.getAccentColor()));
        imgIcon.setColorFilter(getContext().getResources().getColor(presenter.mTheme.getAccentColor()), PorterDuff.Mode.SRC_ATOP);
        PrefsController.putString(getString(R.string.key_theme_object),new Gson().toJson(presenter.mTheme));
        adapter.notifyItemChanged(position);
    }

    @Override
    protected void onResume() {
        super.onResume();
        onRegisterHomeWatcher();
        SuperSafeApplication.getInstance().writeKeyHomePressed(ThemeSettingsActivity.class.getSimpleName());
    }

    @Override
    public void onBackPressed() {
        Intent intent = getIntent();
        if (isUpdated){
            setResult(RESULT_OK,intent);
        }
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                 onBackPressed();
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(isUpdated){
            EventBus.getDefault().post(EnumStatus.RECREATE);
        }
    }

    @Override
    public void onStartLoading(EnumStatus status) {

    }

    @Override
    public void onStopLoading(EnumStatus status) {

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
            case SHOW_DATA:{
                adapter.setDataSource(presenter.mList);
                break;
            }
            case RELOAD:{
                break;
            }
        }
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }


    @Override
    public void onSuccessful(String message, EnumStatus status, Object object) {

    }

    @Override
    public void onSuccessful(String message, EnumStatus status, List list) {

    }
}
