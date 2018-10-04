package co.tpcreative.supersafe.ui.theme;
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
import com.litao.android.lib.Utils.GridSpacingItemDecoration;
import butterknife.BindView;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.activity.BaseActivity;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.controller.SingletonManagerTab;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.User;

public class ThemeSettingsActivity extends BaseActivity implements ThemeSettingsView, ThemeSettingsAdapter.ItemSelectedListener{

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

    public void initRecycleView(LayoutInflater layoutInflater) {
        adapter = new ThemeSettingsAdapter(layoutInflater, getApplicationContext(), this);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getApplicationContext(), 4);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(4, 4, true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onShowUI() {
        adapter.setDataSource(presenter.mList);
    }

    @Override
    public void startLoading() {

    }

    @Override
    public void stopLoading() {

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
    public Context getContext() {
        return getApplicationContext();
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
    }

    @Override
    public void onBackPressed() {
        Intent intent = getIntent();
        if (isUpdated){
            final User mUser = User.getInstance().getUserInfo();
            if (mUser!=null){
                mUser.isUpdateView = true;
                PrefsController.putString(getString(R.string.key_user),new Gson().toJson(mUser));
            }
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
        recreate();
    }
}
