package co.tpcreative.supersafe.ui.trash;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.bumptech.glide.Glide;
import com.ftinc.kit.util.SizeUtils;
import com.litao.android.lib.Utils.GridSpacingItemDecoration;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;
import butterknife.BindView;
import butterknife.OnClick;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.activity.BaseGoogleApi;
import co.tpcreative.supersafe.common.controller.ServiceManager;
import co.tpcreative.supersafe.common.controller.SingletonPrivateFragment;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;

public class TrashActivity extends BaseGoogleApi implements TrashView ,TrashAdapter.ItemSelectedListener{

    private static final String TAG = TrashActivity.class.getSimpleName();
    private SlidrConfig mConfig;
    @BindView(R.id.tv_Audios)
    TextView tv_Audios;
    @BindView(R.id.tv_Videos)
    TextView tvVideos;
    @BindView(R.id.tv_Photos)
    TextView tv_Photos;
    @BindView(R.id.btnUpgradeVersion)
    Button btnUpgradeVersion;
    @BindView(R.id.btnTrash)
    Button btnTrash;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    private TrashAdapter adapter;
    private TrashPresenter presenter;
    private ActionMode actionMode;
    private int countSelected;
    private boolean isSelectAll = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trash);

        //android O fix bug orientation
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

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
        initRecycleView(getLayoutInflater());
        presenter = new TrashPresenter();
        presenter.bindView(this);
        presenter.getData(this);

    }

    public void initRecycleView(LayoutInflater layoutInflater){
        adapter = new TrashAdapter(layoutInflater,getApplicationContext(),this);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getApplicationContext(), 3);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(3,4, true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onClickItem(int position) {
        if (actionMode == null) {
            actionMode = toolbar.startActionMode(callback);
        }
        toggleSelection(position);
        actionMode.setTitle(countSelected + " " + getString(com.darsh.multipleimageselect.R.string.selected));
        if (countSelected == 0) {
            actionMode.finish();
        }
    }

    @OnClick(R.id.btnTrash)
    public void onTrash(View view){
        if (presenter.mList.size()>0){
            if (countSelected==0){
                onShowDialog(getString(R.string.empty_all_trash),true);
            }
            else{
                onShowDialog(getString(R.string.restore),false);
            }
        }
    }

    public void onShowDialog(String message,boolean isEmpty){
        MaterialDialog.Builder builder =  new MaterialDialog.Builder(this)
                .title(getString(R.string.confirm))
                .theme(Theme.LIGHT)
                .content(message)
                .titleColor(getResources().getColor(R.color.black))
                .negativeText(getString(R.string.cancel))
                .positiveText(getString(R.string.ok))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        presenter.onDeleteAll(isEmpty);
                    }
                });


        builder.show();
    }

    @Override
    public void onDone() {
        if (actionMode!=null){
            actionMode.finish();
        }
        presenter.getData(this);
        btnTrash.setText(getString(R.string.key_empty_trash));

        SingletonPrivateFragment.getInstance().onUpdateView();
        ServiceManager.getInstance().onGetListCategoriesSync(true);

    }

    @Override
    public void onReloadData() {
        adapter.setDataSource(presenter.mList);
    }

    @Override
    protected void onDriveClientReady() {

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
    public void startLoading() {

    }

    @Override
    public void stopLoading() {

    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }


    /*Action mode*/


    private ActionMode.Callback callback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater menuInflater = mode.getMenuInflater();
            menuInflater.inflate(R.menu.menu_select, menu);
            actionMode = mode;
            countSelected = 0;
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {int i = item.getItemId();
            if (i == R.id.menu_item_select_all) {
                isSelectAll = !isSelectAll;
                selectAll();
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            if (countSelected > 0) {
                deselectAll();
            }
            actionMode = null;
        }
    };

    private void toggleSelection(int position) {
        presenter.mList.get(position).isChecked = !presenter.mList.get(position).isChecked;
        if (presenter.mList.get(position).isChecked) {
            countSelected++;
        } else {
            countSelected--;
        }
        onShowUI();
        adapter.notifyDataSetChanged();
    }

    private void deselectAll() {
        for (int i = 0, l = presenter.mList.size(); i < l; i++) {
            presenter.mList.get(i).isChecked = false;
        }
        countSelected = 0;
        onShowUI();
        adapter.notifyDataSetChanged();
    }

    public void selectAll(){
        int countSelect = 0 ;
        for (int i =0;i<presenter.mList.size();i++){
            presenter.mList.get(i).isChecked = isSelectAll;
            if (presenter.mList.get(i).isChecked) {
                countSelect++;
            }
        }
        countSelected = countSelect;
        onShowUI();
        adapter.notifyDataSetChanged();
        actionMode.setTitle(countSelected + " " + getString(com.darsh.multipleimageselect.R.string.selected));
    }

    public void onShowUI(){
        if (countSelected==0){
            btnTrash.setText(getString(R.string.key_empty_trash));
        }
        else{
            btnTrash.setText(getString(R.string.key_restore));
        }
    }

}
