package co.tpcreative.supersafe.ui.albumcover;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.util.List;
import butterknife.BindView;
import butterknife.OnClick;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.activity.BaseActivity;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.common.views.GridSpacingItemDecoration;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.User;
import co.tpcreative.supersafe.model.room.InstanceGenerator;

public class AlbumCoverActivity extends BaseActivity implements BaseView,CompoundButton.OnCheckedChangeListener ,AlbumCoverAdapter.ItemSelectedListener,AlbumCoverDefaultAdapter.ItemSelectedListener{

    private AlbumCoverPresenter presenter;
    @BindView(R.id.btnSwitch)
    SwitchCompat btnSwitch;
    @BindView(R.id.recyclerViewDefault)
    RecyclerView recyclerViewDefault;

    @BindView(R.id.recyclerViewCustom)
    RecyclerView recyclerViewCustom;

    @BindView(R.id.llRecyclerView)
    LinearLayout llRecyclerView;
    @BindView(R.id.tvPremiumDescription)
    TextView tvPremiumDescription;

    private AlbumCoverDefaultAdapter adapterDefault;
    private AlbumCoverAdapter adapterCustom;
    private boolean isReload;
    private static final String TAG = AlbumCoverActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_cover);
        presenter = new AlbumCoverPresenter();
        presenter.bindView(this);
        presenter.getData(this);
        initRecycleViewDefault(getLayoutInflater());
        initRecycleViewCustom(getLayoutInflater());
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        onDrawOverLay(this);
        btnSwitch.setOnCheckedChangeListener(this);
        presenter.getData();
        tvPremiumDescription.setText(getString(R.string.premium_cover_description));
    }

    public void initRecycleViewDefault(LayoutInflater layoutInflater) {
        adapterDefault = new AlbumCoverDefaultAdapter(layoutInflater, getApplicationContext(),this);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getApplicationContext(), 3);
        recyclerViewDefault.setLayoutManager(mLayoutManager);
        recyclerViewDefault.addItemDecoration(new GridSpacingItemDecoration(3, 4, true));
        recyclerViewDefault.setItemAnimator(new DefaultItemAnimator());
        recyclerViewDefault.setAdapter(adapterDefault);
        recyclerViewDefault.setNestedScrollingEnabled(false);

    }

    public void initRecycleViewCustom(LayoutInflater layoutInflater) {
        adapterCustom = new AlbumCoverAdapter(layoutInflater, getApplicationContext(), presenter.mMainCategories,this);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getApplicationContext(), 3);
        recyclerViewCustom.setLayoutManager(mLayoutManager);
        recyclerViewCustom.addItemDecoration(new GridSpacingItemDecoration(3, 4, true));
        recyclerViewCustom.setItemAnimator(new DefaultItemAnimator());
        recyclerViewCustom.setAdapter(adapterCustom);
        recyclerViewCustom.setNestedScrollingEnabled(false);
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
    public void onClickItem(int position) {
        Utils.Log(TAG,"position..."+position);
        try {
            presenter.mMainCategories.items_id = presenter.mList.get(position).items_id;
            presenter.mMainCategories.mainCategories_Local_Id = "";
            InstanceGenerator.getInstance(this).onUpdate(presenter.mMainCategories);
            presenter.getData();
            isReload = true;
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onClickedDefaultItem(int position) {
        Utils.Log(TAG,"position..."+position);
        try {
            presenter.mMainCategories.items_id = "";
            presenter.mMainCategories.mainCategories_Local_Id = presenter.mListMainCategories.get(position).mainCategories_Local_Id;
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
        switch (compoundButton.getId()) {
            case R.id.btnSwitch: {
                if (User.getInstance().isPremiumExpired()) {
                    btnSwitch.setChecked(false);
                    onShowPremium();
                    break;
                }
                else {
                    presenter.mMainCategories.isCustom_Cover = b;
                    InstanceGenerator.getInstance(this).onUpdate(presenter.mMainCategories);
                    llRecyclerView.setVisibility(b?View.VISIBLE : View.INVISIBLE);
                    Utils.Log(TAG,"action here");
                }
                break;
            }
        }
    }

    @OnClick(R.id.rlSwitch)
    public void onActionSwitch(View view){
        btnSwitch.setChecked(!btnSwitch.isChecked());
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
                    if (User.getInstance().isPremiumExpired()) {
                        setTitle(presenter.mMainCategories.categories_name);
                        presenter.mMainCategories.isCustom_Cover = false;
                        btnSwitch.setChecked(presenter.mMainCategories.isCustom_Cover);
                        llRecyclerView.setVisibility(presenter.mMainCategories.isCustom_Cover?View.VISIBLE : View.INVISIBLE);
                        InstanceGenerator.getInstance(this).onUpdate(presenter.mMainCategories);
                    }else{
                        setTitle(presenter.mMainCategories.categories_name);
                        btnSwitch.setChecked(presenter.mMainCategories.isCustom_Cover);
                        llRecyclerView.setVisibility(presenter.mMainCategories.isCustom_Cover?View.VISIBLE : View.INVISIBLE);
                    }
                }
                break;
            }
            case GET_LIST_FILE:{
                Utils.Log(TAG,"load data");
                adapterDefault.setDataSource(presenter.mListMainCategories);
                adapterCustom.setDataSource(presenter.mList);
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
    public void onBackPressed() {
        if (isReload){
            Intent intent = getIntent();
            setResult(RESULT_OK,intent);
            Utils.Log(TAG,"onBackPressed");
        }
        super.onBackPressed();
    }

    public void onShowPremium(){
        try {
            de.mrapp.android.dialog.MaterialDialog.Builder builder = new de.mrapp.android.dialog.MaterialDialog.Builder(getContext());
            co.tpcreative.supersafe.model.Theme theme = co.tpcreative.supersafe.model.Theme.getInstance().getThemeInfo();
            builder.setHeaderBackground(theme.getAccentColor());
            builder.setTitle(getString(R.string.this_is_premium_feature));
            builder.setMessage(getString(R.string.upgrade_now));
            builder.setCustomHeader(R.layout.custom_header);
            builder.setPadding(40,40,40,0);
            builder.setMargin(60,0,60,0);
            builder.showHeader(true);
            builder.setPositiveButton(getString(R.string.get_premium), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Navigator.onMoveToPremium(getContext());
                }
            });

            builder.setNegativeButton(getText(R.string.later), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });

            de.mrapp.android.dialog.MaterialDialog dialog = builder.show();
            builder.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {
                    Button positive = dialog.findViewById(android.R.id.button1);
                    Button negative = dialog.findViewById(android.R.id.button2);
                    TextView textView = (TextView) dialog.findViewById(android.R.id.message);

                    if (positive!=null && negative!=null && textView!=null){
                        positive.setTextColor(getContext().getResources().getColor(theme.getAccentColor()));
                        negative.setTextColor(getContext().getResources().getColor(theme.getAccentColor()));
                        textView.setTextSize(16);
                    }
                }
            });

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

}
