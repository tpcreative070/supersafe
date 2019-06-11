package co.tpcreative.supersafe.ui.cloudmanager;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.snatik.storage.Storage;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.util.List;
import butterknife.BindView;
import butterknife.OnClick;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.activity.BaseGoogleApi;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.controller.ServiceManager;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.ConvertUtils;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.DriveAbout;
import co.tpcreative.supersafe.model.EnumFormatType;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.Items;
import co.tpcreative.supersafe.model.StorageQuota;
import co.tpcreative.supersafe.model.ThemeApp;
import co.tpcreative.supersafe.model.User;
import co.tpcreative.supersafe.model.room.InstanceGenerator;

public class CloudManagerActivity extends BaseGoogleApi implements CompoundButton.OnCheckedChangeListener, BaseView<Long> {
    private static String TAG = CloudManagerActivity.class.getSimpleName();
    @BindView(R.id.tvUploaded)
    TextView tvUploaded;
    @BindView(R.id.tvLeft)
    TextView tvLeft;
    @BindView(R.id.btnRemoveLimit)
    Button btnRemoveLimit;
    @BindView(R.id.tvSupersafeSpace)
    TextView tvSupersafeSpace;
    @BindView(R.id.tvOtherSpace)
    TextView tvOtherSpace;
    @BindView(R.id.tvFreeSpace)
    TextView tvFreeSpace;
    @BindView(R.id.llPremium)
    LinearLayout llPremium;
    @BindView(R.id.llTitle)
    LinearLayout llTitle;
    @BindView(R.id.tvValueSupersafeSpace)
    TextView tvValueSupersafeSpace;
    @BindView(R.id.tvValueOtherSpace)
    TextView tvValueOtherSpace;
    @BindView(R.id.tvValueFreeSpace)
    TextView tvValueFreeSpace;
    @BindView(R.id.btnSwitchPauseSync)
    SwitchCompat btnSwitchPauseSync;
    @BindView(R.id.tvDriveAccount)
    TextView tvDriveAccount;
    @BindView(R.id.tvDeviceSaving)
    TextView tvDeviceSaving;
    @BindView(R.id.switch_SaveSpace)
    SwitchCompat btnSwitchSaveSpace;
    private CloudManagerPresenter presenter;
    private boolean isPauseCloudSync = true;
    private boolean isDownload;
    private boolean isSpaceSaver;
    private Storage storage;
    private boolean isRefresh ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_manager);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        storage = new Storage(this);
        presenter = new CloudManagerPresenter();
        presenter.bindView(this);
        btnSwitchPauseSync.setOnCheckedChangeListener(this);
        btnSwitchSaveSpace.setOnCheckedChangeListener(this);
        String lefFiles = String.format(getString(R.string.left), "" + Navigator.LIMIT_UPLOAD);
        tvLeft.setText(lefFiles);
        String updated = String.format(getString(R.string.left), "0");
        tvUploaded.setText(updated);
        onShowUI();
        onUpdatedView();
        presenter.onGetDriveAbout();
        onStartOverridePendingTransition();
    }

    public void onUpdatedView() {
        if (User.getInstance().isPremiumExpired()) {
            llPremium.setVisibility(View.VISIBLE);
            llTitle.setVisibility(View.GONE);
        } else {
            llPremium.setVisibility(View.GONE);
            llTitle.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.llPause)
    public void onActionPause(View view){
        btnSwitchPauseSync.setChecked(!btnSwitchPauseSync.isChecked());
    }

    @OnClick(R.id.rlSaveSpace)
    public void onActionSaveSpace(View view){
        btnSwitchSaveSpace.setChecked(!btnSwitchSaveSpace.isChecked());
    }

    public void onShowUI() {
        tvSupersafeSpace.setVisibility(View.VISIBLE);
        tvOtherSpace.setVisibility(View.VISIBLE);
        tvFreeSpace.setVisibility(View.VISIBLE);
        final User mUser = User.getInstance().getUserInfo();
        boolean isThrow = false;
        if (mUser != null) {
            DriveAbout driveAbout = mUser.driveAbout;
            tvDriveAccount.setText(mUser.cloud_id);
            try {
                String superSafeSpace = ConvertUtils.byte2FitMemorySize(driveAbout.inAppUsed);
                tvValueSupersafeSpace.setText(superSafeSpace);
            } catch (Exception e) {
                tvValueOtherSpace.setText(getString(R.string.calculating));
                isThrow = true;
            }
            try {
                final StorageQuota storageQuota = driveAbout.storageQuota;
                if (storageQuota!=null){
                    String superSafeSpace = ConvertUtils.byte2FitMemorySize(storageQuota.usage);
                    tvValueOtherSpace.setText(superSafeSpace);
                }
            } catch (Exception e) {
                tvValueOtherSpace.setText(getString(R.string.calculating));
                isThrow = true;
            }
            try {
                final StorageQuota storageQuota = driveAbout.storageQuota;
                if (storageQuota!=null){
                    final long result = storageQuota.limit - storageQuota.usage;
                    String superSafeSpace = ConvertUtils.byte2FitMemorySize(result);
                    tvValueFreeSpace.setText(superSafeSpace);
                }
            } catch (Exception e) {
                tvValueFreeSpace.setText(getString(R.string.calculating));
                isThrow = true;
            }
            try {
                if (mUser.syncData != null) {
                    String lefFiles = String.format(getString(R.string.left), "" + mUser.syncData.left);
                    tvLeft.setText(lefFiles);
                }
            } catch (Exception e) {
                String lefFiles = String.format(getString(R.string.left), "" + Navigator.LIMIT_UPLOAD);
                tvLeft.setText(lefFiles);
                isThrow = true;
            }
            try {
                if (mUser.syncData != null) {
                    String uploadedFiles = String.format(getString(R.string.uploaded), "" + (Navigator.LIMIT_UPLOAD - mUser.syncData.left));
                    tvUploaded.setText(uploadedFiles);
                }
            } catch (Exception e) {
                String uploadedFiles = String.format(getString(R.string.uploaded), "0");
                tvUploaded.setText(uploadedFiles);
                isThrow = true;
            }

            if (isThrow) {
                tvSupersafeSpace.setVisibility(View.INVISIBLE);
                tvOtherSpace.setVisibility(View.INVISIBLE);
                tvFreeSpace.setVisibility(View.INVISIBLE);
            }
        }
    }

    public void onShowSwitch() {
        final boolean pause_cloud_sync = PrefsController.getBoolean(getString(R.string.key_pause_cloud_sync), false);
        btnSwitchPauseSync.setChecked(pause_cloud_sync);
        final boolean saving_space = PrefsController.getBoolean(getString(R.string.key_saving_space), false);
        btnSwitchSaveSpace.setChecked(saving_space);
        if (saving_space) {
            presenter.onGetSaveData();
        }
        else {
            tvDeviceSaving.setText(ConvertUtils.byte2FitMemorySize(0));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_cloud_manager, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_refresh: {
                final boolean isExpired = User.getInstance().isPremiumExpired();
                if (isExpired) {
                    if (!User.getInstance().isCheckAllowUpload()){
                        break;
                    }
                }
                presenter.onGetDriveAbout();
                isRefresh = true;
                break;
            }
        }
        return super.onOptionsItemSelected(item);
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
        switch (status) {
            case REQUEST_ACCESS_TOKEN: {
                Utils.Log(TAG, "Error response " + message);
                getAccessToken();
                break;
            }
            default: {
                Utils.Log(TAG, "Error response " + message);
                break;
            }
        }
    }

    @Override
    public void onSuccessful(String message) {
    }

    @Override
    public void onSuccessful(String message, EnumStatus status) {
        switch (status) {
            case GET_LIST_FILES_IN_APP: {
                onShowUI();
                break;
            }
            case SAVER: {
                tvDeviceSaving.setText(ConvertUtils.byte2FitMemorySize(presenter.sizeSaverFiles));
                break;
            }
            case GET_LIST_FILE: {
                onShowDialog();
                break;
            }
            case DOWNLOAD: {
                tvDeviceSaving.setText(ConvertUtils.byte2FitMemorySize(0));
                isDownload = true;
                break;
            }
        }
    }

    @Override
    public void onSuccessful(String message, EnumStatus status, Long object) {

    }

    @Override
    public void onSuccessful(String message, EnumStatus status, List list) {
        Utils.Log(TAG, "Successful response " + message);
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
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        Utils.Log(TAG, "onCheckedChanged...............!!!");
        switch (compoundButton.getId()) {
            case R.id.btnSwitchPauseSync: {
                isPauseCloudSync = b;
                PrefsController.putBoolean(getString(R.string.key_pause_cloud_sync), b);
                break;
            }
            case R.id.switch_SaveSpace: {
                if (!User.getInstance().isPremium()){
                    onShowPremium();
                    PrefsController.putBoolean(getString(R.string.key_saving_space), false);
                    btnSwitchSaveSpace.setChecked(false);
                    break;
                }
                if (b) {
                    isDownload = false;
                    isSpaceSaver = true;
                    presenter.onEnableSaverSpace();
                } else {
                    isSpaceSaver = false;
                    presenter.onDisableSaverSpace(EnumStatus.GET_LIST_FILE);
                }
                PrefsController.putBoolean(getString(R.string.key_saving_space), b);
                break;
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

    public void onShowPremium(){
        try {
            de.mrapp.android.dialog.MaterialDialog.Builder builder = new de.mrapp.android.dialog.MaterialDialog.Builder(getContext());
            ThemeApp themeApp = ThemeApp.getInstance().getThemeInfo();
            builder.setHeaderBackground(themeApp.getAccentColor());
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
                    PrefsController.putBoolean(getString(R.string.key_saving_space), false);
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
                        positive.setTextColor(getContext().getResources().getColor(themeApp.getAccentColor()));
                        negative.setTextColor(getContext().getResources().getColor(themeApp.getAccentColor()));
                        textView.setTextSize(16);
                    }
                }
            });

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }
        onRegisterHomeWatcher();
        //SuperSafeApplication.getInstance().writeKeyHomePressed(CloudManagerActivity.class.getSimpleName());
        onShowSwitch();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utils.Log(TAG,"OnDestroy");
        EventBus.getDefault().unregister(this);
        if (!isPauseCloudSync) {
            ServiceManager.getInstance().onSyncDataOwnServer("0");
        }
        if (isDownload) {
            final List<Items> mList = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getListSyncData(true, false, false);
            if (mList != null && mList.size() > 0) {
                for (int i = 0; i < mList.size(); i++) {
                    EnumFormatType formatType = EnumFormatType.values()[mList.get(i).formatType];
                    switch (formatType) {
                        case IMAGE: {
                            mList.get(i).isSyncCloud = false;
                            mList.get(i).originalSync = false;
                            mList.get(i).statusAction = EnumStatus.DOWNLOAD.ordinal();
                            InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onUpdate(mList.get(i));
                            break;
                        }
                    }
                }
            }
            ServiceManager.getInstance().onSyncDataOwnServer("0");
            Utils.Log(TAG, "Re-Download file");
        }
        if (isSpaceSaver){
            final List<Items> mList = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getListSyncData(true, true, false);
            for (Items index : mList){
                EnumFormatType formatType = EnumFormatType.values()[index.formatType];
                switch (formatType) {
                    case IMAGE: {
                        storage.deleteFile(index.originalPath);
                        break;
                    }
                }
            }
        }
        if (isRefresh){
            ServiceManager.getInstance().onGetListCategoriesSync();
        }
        presenter.unbindView();
    }

    @Override
    public void onOrientationChange(boolean isFaceDown) {
        onFaceDown(isFaceDown);
    }
    @OnClick(R.id.btnRemoveLimit)
    public void onRemoveLimit(View view) {
        Navigator.onMoveToPremium(this);
    }
    @Override
    protected void onDriveError() {
        Utils.Log(TAG, "onDriveError");
    }
    @Override
    protected void onDriveSignOut() {
        Utils.Log(TAG, "onDriveSignOut");
    }
    @Override
    protected void onDriveRevokeAccess() {
        Utils.Log(TAG, "onDriveRevokeAccess");
    }
    @Override
    protected void onDriveClientReady() {
    }
    @Override
    protected boolean isSignIn() {
        return false;
    }
    @Override
    protected void onDriveSuccessful() {
    }

    @Override
    protected void startServiceNow() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void onShowDialog() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.custom_view_dialog, null);
        TextView space_required = view.findViewById(R.id.tvSpaceRequired);
        space_required.setText(String.format(getString(R.string.space_required), ConvertUtils.byte2FitMemorySize(presenter.sizeFile)));
        MaterialDialog.Builder builder = new MaterialDialog.Builder(this)
                .title(getString(R.string.download_private_cloud_files))
                .customView(view, false)
                .theme(Theme.LIGHT)
                .cancelable(false)
                .titleColor(getResources().getColor(R.color.black))
                .inputType(InputType.TYPE_CLASS_TEXT)
                .negativeText(getString(R.string.cancel))
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Utils.Log(TAG, "negative");
                        btnSwitchSaveSpace.setChecked(true);
                    }
                })
                .positiveText(getString(R.string.download))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Utils.Log(TAG, "positive");
                        PrefsController.putBoolean(getString(R.string.key_saving_space), false);
                        presenter.onDisableSaverSpace(EnumStatus.DOWNLOAD);
                    }
                });
        builder.show();
    }
}
