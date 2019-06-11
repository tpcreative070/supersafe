package co.tpcreative.supersafe.ui.breakinalerts;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.util.List;
import butterknife.BindView;
import butterknife.OnClick;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.SensorOrientationChangeNotifier;
import co.tpcreative.supersafe.common.activity.BaseActivity;
import co.tpcreative.supersafe.common.adapter.DividerItemDecoration;
import co.tpcreative.supersafe.common.controller.GalleryCameraMediaManager;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.controller.ServiceManager;
import co.tpcreative.supersafe.common.hiddencamera.HiddenCameraUtils;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.ui.camera.CameraActivity;
import co.tpcreative.supersafe.ui.resetpin.ResetPinActivity;

public class BreakInAlertsActivity extends BaseActivity implements BaseView, CompoundButton.OnCheckedChangeListener,BreakInAlertsAdapter.ItemSelectedListener{

    private static final String TAG = BreakInAlertsActivity.class.getSimpleName();
    @BindView(R.id.btnSwitch)
    SwitchCompat btnSwitch;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.tvPremiumDescription)
    TextView tvPremiumDescription;
    private BreakInAlertsAdapter adapter;
    private BreakInAlertsPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_break_in_alerts);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        btnSwitch.setOnCheckedChangeListener(this);
        final boolean value = PrefsController.getBoolean(getString(R.string.key_break_in_alert),false);
        btnSwitch.setChecked(value);
        initRecycleView();
        presenter = new BreakInAlertsPresenter();
        presenter.bindView(this);
        presenter.onGetData();
        tvPremiumDescription.setText(getString(R.string.break_in_alerts));
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_break_in_alerts, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_item_select_all:{
                if (presenter.mList==null || presenter.mList.size()==0){
                    break;
                }
                Utils.showDialog(this, getString(R.string.are_you_sure_delete_all_items), new ServiceManager.ServiceManagerSyncDataListener() {
                    @Override
                    public void onCompleted() {
                        presenter.onDeleteAll();
                    }
                    @Override
                    public void onError() {

                    }
                    @Override
                    public void onCancel() {

                    }
                });
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.rlBreakInAlerts)
    public void onActionBreak(View view){
        btnSwitch.setChecked(!btnSwitch.isChecked());
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (b){
            recyclerView.setVisibility(View.VISIBLE);
        }
        else{
            recyclerView.setVisibility(View.INVISIBLE);
        }

        if (HiddenCameraUtils.isFrontCameraAvailable(this)){
            onAddPermissionCamera(b);
        }
        else{
            showMessage(getString(R.string.error_not_having_camera));
        }
    }

    public void initRecycleView(){
        adapter = new BreakInAlertsAdapter(getLayoutInflater(),this,this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onClickItem(int position) {
        Navigator.onMoveBreakInAlertsDetail(this,presenter.mList.get(position));
    }

    public void onAddPermissionCamera(boolean value) {
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.CAMERA)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            PrefsController.putBoolean(getString(R.string.key_break_in_alert),value);
                        }
                        else{
                            PrefsController.putBoolean(getString(R.string.key_break_in_alert),false);
                            btnSwitch.setChecked(false);
                            Utils.Log(TAG,"Permission is denied");
                        }
                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            PrefsController.putBoolean(getString(R.string.key_break_in_alert),false);
                            btnSwitch.setChecked(false);
                            /*Miss add permission in manifest*/
                            Utils.Log(TAG, "request permission is failed");
                        }
                    }
                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        /* ... */
                        token.continuePermissionRequest();
                    }
                })
                .withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        Utils.Log(TAG, "error ask permission");
                    }
                }).onSameThread().check();
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
                adapter.setDataSource(presenter.mList);
                break;
            }
            case DELETE:{
                presenter.onGetData();
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
        return getApplicationContext();
    }

    @Override
    public Activity getActivity() {
        return null;
    }
}
