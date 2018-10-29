package co.tpcreative.supersafe.ui.albumcover;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import java.util.List;
import butterknife.BindView;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.activity.BaseActivity;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.common.views.GridSpacingItemDecoration;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.room.InstanceGenerator;
import co.tpcreative.supersafe.ui.albumdetail.AlbumDetailActivity;
import co.tpcreative.supersafe.ui.help.HelpAndSupportContentActivity;

public class AlbumCoverActivity extends BaseActivity implements BaseView,CompoundButton.OnCheckedChangeListener ,AlbumCoverAdapter.ItemSelectedListener{

    private AlbumCoverPresenter presenter;
    @BindView(R.id.btnSwitch)
    SwitchCompat btnSwitch;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.llRecyclerView)
    LinearLayout llRecyclerView;
    @BindView(R.id.tvPremiumDescription)
    TextView tvPremiumDescription;
    private AlbumCoverAdapter adapter;
    private boolean isReload;

    private static final String TAG = AlbumCoverActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_cover);

        presenter = new AlbumCoverPresenter();
        presenter.bindView(this);
        presenter.getData(this);
        initRecycleView(getLayoutInflater());
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        onDrawOverLay(this);
        btnSwitch.setOnCheckedChangeListener(this);
        presenter.getData();
        tvPremiumDescription.setText(getString(R.string.premium_cover_description));
    }

    public void initRecycleView(LayoutInflater layoutInflater) {
        adapter = new AlbumCoverAdapter(layoutInflater, getApplicationContext(), presenter.mMainCategories,this);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getApplicationContext(), 3);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(3, 4, true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
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
    public void onClickItem(int position) {
        Utils.Log(TAG,"position..."+position);
        try {
            presenter.mMainCategories.item = new Gson().toJson(presenter.mList.get(position));
            InstanceGenerator.getInstance(this).onUpdate(presenter.mMainCategories);
            presenter.getData();
            isReload = true;
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home :{
                if (isReload){
                    Intent intent = getIntent();
                    setResult(RESULT_OK,intent);
                    Utils.Log(TAG,"onBackPressed");
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        presenter.mMainCategories.isCustom_Cover = b;
        InstanceGenerator.getInstance(this).onUpdate(presenter.mMainCategories);
        llRecyclerView.setVisibility(b?View.VISIBLE : View.INVISIBLE);
        Utils.Log(TAG,"action here");
    }

    @Override
    public void onStartLoading(EnumStatus status) {

    }

    @Override
    public void onStopLoading(EnumStatus status) {

    }

    @Override
    public void onError(String message) {

    }

    @Override
    public void onError(String message, EnumStatus status) {

    }

    @Override
    public void onSuccessful(String message) {

    }

    @Override
    public void onSuccessful(String message, EnumStatus status) {
        switch (status){
            case RELOAD:{
                if (presenter.mMainCategories!=null){
                    setTitle(presenter.mMainCategories.categories_name);
                    btnSwitch.setChecked(presenter.mMainCategories.isCustom_Cover);
                    llRecyclerView.setVisibility(presenter.mMainCategories.isCustom_Cover?View.VISIBLE : View.INVISIBLE);
                }
                break;
            }
            case GET_LIST_FILE:{
                Utils.Log(TAG,"load data");
                adapter.setDataSource(presenter.mList);
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
    public Context getContext() {
        return this;
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public void onOrientationChange(boolean isFaceDown) {
        onFaceDown(isFaceDown);
    }

    @Override
    protected void onResume() {
        super.onResume();
        onRegisterHomeWatcher();
        SuperSafeApplication.getInstance().writeKeyHomePressed(AlbumCoverActivity.class.getSimpleName());
    }


    @Override
    public void onBackPressed() {
        if (isReload){
            Intent intent = getIntent();
            setResult(RESULT_OK,intent);
            Utils.Log(TAG,"onBackPressed");
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
