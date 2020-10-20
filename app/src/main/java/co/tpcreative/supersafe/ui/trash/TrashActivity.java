package co.tpcreative.supersafe.ui.trash;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.util.List;
import butterknife.BindView;
import butterknife.OnClick;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.activity.BaseActivity;
import co.tpcreative.supersafe.common.controller.ServiceManager;
import co.tpcreative.supersafe.common.controller.SingletonPrivateFragment;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.common.views.GridSpacingItemDecoration;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.ThemeApp;
import co.tpcreative.supersafe.model.User;

public class TrashActivity extends BaseActivity implements BaseView,TrashAdapter.ItemSelectedListener{
    private static final String TAG = TrashActivity.class.getSimpleName();
    @BindView(R.id.tv_Audios)
    AppCompatTextView tv_Audios;
    @BindView(R.id.tv_Videos)
    AppCompatTextView tv_Videos;
    @BindView(R.id.tv_Photos)
    AppCompatTextView tv_Photos;
    @BindView(R.id.tv_Others)
    AppCompatTextView tv_Others;
    @BindView(R.id.btnUpgradeVersion)
    AppCompatButton btnUpgradeVersion;
    @BindView(R.id.btnTrash)
    AppCompatButton btnTrash;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rlRecyclerView)
    RelativeLayout rlRecyclerView;
    @BindView(R.id.llUpgrade)
    LinearLayout llUpgrade;
    @BindView(R.id.rlEmptyTrash)
    RelativeLayout rlEmptyTrash;
    private TrashAdapter adapter;
    private TrashPresenter presenter;
    private ActionMode actionMode;
    private int countSelected;
    private boolean isSelectAll = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trash);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initRecycleView(getLayoutInflater());
        presenter = new TrashPresenter();
        presenter.bindView(this);
        presenter.getData(this);
    }

    public void onUpdatedView(){
        if (!Utils.isPremium()){
            llUpgrade.setVisibility(View.VISIBLE);
            rlRecyclerView.setVisibility(View.GONE);
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
        //SuperSafeApplication.getInstance().writeKeyHomePressed(TrashActivity.class.getSimpleName());
        onUpdatedView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utils.Log(TAG,"OnDestroy");
        EventBus.getDefault().unregister(this);
        presenter.unbindView();
    }

    @Override
    protected void onStopListenerAWhile() {
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onOrientationChange(boolean isFaceDown) {
        onFaceDown(isFaceDown);
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
        actionMode.setTitle(countSelected + " " + getString(R.string.selected));
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

    @OnClick(R.id.btnUpgradeVersion)
    public void onUpgradeToRecover(){
        Navigator.onMoveToPremium(getApplicationContext());
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
    public Activity getActivity() {
        return this;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (Utils.isPremium()){
            if (toolbar==null){
                return false;
            }
            toolbar.inflateMenu(R.menu.menu_trash);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_select_items :{
                if (actionMode == null) {
                    actionMode = toolbar.startActionMode(callback);
                }
                countSelected = 0;
                actionMode.setTitle(countSelected + " " + getString(R.string.selected));
                Utils.Log(TAG,"Action here");
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /*Action mode*/
    private ActionMode.Callback callback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater menuInflater = mode.getMenuInflater();
            menuInflater.inflate(R.menu.menu_select, menu);
            actionMode = mode;
            countSelected = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.setStatusBarColor(ContextCompat.getColor(getContext(), R.color.material_orange_900));
            }
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ThemeApp themeApp = ThemeApp.getInstance().getThemeInfo();
                if (themeApp!=null){
                    Window window = getWindow();
                    window.setStatusBarColor(ContextCompat.getColor(getContext(), themeApp.getPrimaryDarkColor()));
                }
            }
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
        adapter.notifyItemChanged(position);
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
        actionMode.setTitle(countSelected + " " + getString(R.string.selected));
    }

    public void onShowUI(){
        if (countSelected==0){
            btnTrash.setText(getString(R.string.key_empty_trash));
        }
        else{
            btnTrash.setText(getString(R.string.key_restore));
        }
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
                String photos = String.format(getString(R.string.photos_default),""+presenter.photos);
                tv_Photos.setText(photos);
                String videos = String.format(getString(R.string.videos_default),""+presenter.videos);
                tv_Videos.setText(videos);
                String audios = String.format(getString(R.string.audios_default),""+presenter.audios);
                tv_Audios.setText(audios);
                String others = String.format(getString(R.string.others_default),""+presenter.others);
                tv_Others.setText(others);
                adapter.setDataSource(presenter.mList);
                break;
            }
            case DONE:{
                if (actionMode!=null){
                    actionMode.finish();
                }
                presenter.getData(this);
                btnTrash.setText(getString(R.string.key_empty_trash));
                SingletonPrivateFragment.getInstance().onUpdateView();
                ServiceManager.getInstance().onPreparingSyncData();
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
}
