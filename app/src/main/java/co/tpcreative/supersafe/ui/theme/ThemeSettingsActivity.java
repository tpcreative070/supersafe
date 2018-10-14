package co.tpcreative.supersafe.ui.theme;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import com.google.gson.Gson;
import java.util.List;
import butterknife.BindView;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.activity.BaseActivity;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.controller.SingletonBaseApiActivity;
import co.tpcreative.supersafe.common.controller.SingletonManagerTab;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.views.GridSpacingItemDecoration;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.User;

public class ThemeSettingsActivity extends BaseActivity implements BaseView, ThemeSettingsAdapter.ItemSelectedListener{

    private ThemeSettingsAdapter adapter;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
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
        PrefsController.putString(getString(R.string.key_theme_object),new Gson().toJson(presenter.mTheme));
        presenter.getData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        onRegisterHomeWatcher();
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
        if (isUpdated){
            SingletonBaseApiActivity.getInstance().onStillScreenLock(EnumStatus.RECREATE);
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
