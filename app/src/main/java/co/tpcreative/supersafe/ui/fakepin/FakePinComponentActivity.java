package co.tpcreative.supersafe.ui.fakepin;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.afollestad.materialdialogs.MaterialDialog;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.activity.BaseActivityNoneSlideFakePin;
import co.tpcreative.supersafe.common.controller.ServiceManager;
import co.tpcreative.supersafe.common.controller.SingletonFakePinComponent;
import co.tpcreative.supersafe.common.controller.SingletonManager;
import co.tpcreative.supersafe.common.entities.MainCategoryEntity;
import co.tpcreative.supersafe.common.helper.SQLHelper;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.common.views.GridSpacingItemDecoration;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.Image;
import co.tpcreative.supersafe.model.ImportFilesModel;
import co.tpcreative.supersafe.model.MainCategoryModel;
import co.tpcreative.supersafe.model.MimeTypeFile;
import co.tpcreative.supersafe.model.ThemeApp;

public class FakePinComponentActivity extends BaseActivityNoneSlideFakePin implements BaseView ,FakePinComponentAdapter.ItemSelectedListener,SingletonFakePinComponent.SingletonPrivateFragmentListener{

    private static final String TAG = FakePinComponentActivity.class.getSimpleName();
    @BindView(R.id.speedDial)
    SpeedDialView mSpeedDialView;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    private FakePinComponentAdapter adapter;
    private FakePinComponentPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fake_pin_component);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        initSpeedDial();
        initRecycleView(getLayoutInflater());
        presenter = new FakePinComponentPresenter();
        presenter.bindView(this);
    }

    public void initRecycleView(LayoutInflater layoutInflater){
        adapter = new FakePinComponentAdapter(layoutInflater,this,this);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, 10, true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
    }

    private void initSpeedDial() {
        final ThemeApp mThemeApp = ThemeApp.getInstance().getThemeInfo();
        Drawable drawable = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.baseline_photo_camera_white_24);
        mSpeedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id
                .fab_camera, drawable)
                .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), mThemeApp.getPrimaryColor(),
                        getTheme()))

                .setLabel(getString(R.string.camera))
                .setLabelColor(Color.WHITE)
                .setLabelBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.inbox_primary,
                        getTheme()))
                .create());

        drawable = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.baseline_photo_white_24);
        mSpeedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id.fab_photo, drawable)
                .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), mThemeApp.getPrimaryColor(),
                        getTheme()))
                .setLabel(R.string.photo)
                .setLabelColor(getResources().getColor(R.color.white))
                .setLabelBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.inbox_primary,
                        getTheme()))
                .create());

        mSpeedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id.fab_album, R.drawable
                .baseline_add_to_photos_white_36)
                .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), mThemeApp.getPrimaryColor(),
                        getTheme()))
                .setLabel(getString(R.string.album))
                .setLabelColor(getResources().getColor(R.color.white))
                .setLabelBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.inbox_primary,
                        getTheme()))
                .create());
        mSpeedDialView.setMainFabAnimationRotateAngle(180);

        //Set main action clicklistener.
        mSpeedDialView.setOnChangeListener(new SpeedDialView.OnChangeListener() {
            @Override
            public boolean onMainActionSelected() {
                return false; // True to keep the Speed Dial open
            }

            @Override
            public void onToggleChanged(boolean isOpen) {
                Utils.Log(TAG, "Speed dial toggle state changed. Open = " + isOpen);
            }
        });

        //Set option fabs clicklisteners.
        mSpeedDialView.setOnActionSelectedListener(new SpeedDialView.OnActionSelectedListener() {
            @Override
            public boolean onActionSelected(SpeedDialActionItem actionItem) {
                switch (actionItem.getId()) {
                    case R.id.fab_album:
                        onShowDialog();
                        return false; // false will close it without animation
                    case R.id.fab_photo:
                        Navigator.onMoveToAlbum(FakePinComponentActivity.this);
                        return false; // closes without animation (same as mSpeedDialView.close(false); return false;)
                    case R.id.fab_camera:
                        onAddPermissionCamera();
                        return false;
                }
                return true; // To keep the Speed Dial open
            }
        });
    }

    public void onShowDialog() {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(this)
                .title(getString(R.string.create_album))
                .theme(com.afollestad.materialdialogs.Theme.LIGHT)
                .titleColor(getResources().getColor(R.color.black))
                .inputType(InputType.TYPE_CLASS_TEXT)
                .negativeText(getString(R.string.cancel))
                .positiveText(getString(R.string.ok))
                .input(null, null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        Utils.Log(TAG, "Value");
                        String value = input.toString();
                        String base64Code = Utils.getHexCode(value);
                        MainCategoryModel item = SQLHelper.getTrashItem();
                        String result = item.categories_hex_name;
                        if (base64Code.equals(result)) {
                            Toast.makeText(FakePinComponentActivity.this, "This name already existing", Toast.LENGTH_SHORT).show();
                        } else {
                            boolean response = SQLHelper.onAddFakePinCategories(base64Code, value,true);
                            if (response) {
                                Toast.makeText(FakePinComponentActivity.this, "Created album successful", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(FakePinComponentActivity.this, "Album name already existing", Toast.LENGTH_SHORT).show();
                            }
                            presenter.getData();
                        }
                    }
                });
        builder.show();
    }

    public void onAddPermissionCamera() {
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.CAMERA)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            final List<MainCategoryModel> list = SQLHelper.getListFakePin();
                            if (list != null) {
                                Navigator.onMoveCamera(FakePinComponentActivity.this, list.get(0));
                            }
                        } else {
                            Utils.Log(TAG, "Permission is denied");
                        }
                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Utils.Log(TAG, "Selected album :");
        switch (requestCode) {
            case Navigator.CAMERA_ACTION: {
                if (resultCode == Activity.RESULT_OK) {
                    Utils.Log(TAG, "reload data");
                    SingletonFakePinComponent.getInstance().onUpdateView();
                } else {
                    Utils.Log(TAG, "Nothing to do on Camera");
                }
                break;
            }
            case Navigator.REQUEST_CODE: {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    ArrayList<Image> images = data.getParcelableArrayListExtra(Navigator.INTENT_EXTRA_IMAGES);
                    List<ImportFilesModel> mListImport = new ArrayList<>();
                    for (int i = 0, l = images.size(); i < l; i++) {
                        String path = images.get(i).path;
                        String name = images.get(i).name;
                        String id = "" + images.get(i).id;
                        String mimeType = Utils.getMimeType(path);
                        Utils.Log(TAG, "mimeType " + mimeType);
                        Utils.Log(TAG, "name " + name);
                        Utils.Log(TAG, "path " + path);
                        String fileExtension = Utils.getFileExtension(path);
                        Utils.Log(TAG, "file extension " + Utils.getFileExtension(path));
                        try {
                            final MimeTypeFile mimeTypeFile = Utils.mediaTypeSupport().get(fileExtension);
                            if (mimeTypeFile==null){
                                return;
                            }
                            mimeTypeFile.name = name;
                            final List<MainCategoryModel> list = SQLHelper.getListFakePin();
                            if (list == null) {
                                Utils.onWriteLog("Main categories is null", EnumStatus.WRITE_FILE);
                                return;
                            }
                            ImportFilesModel importFiles = new ImportFilesModel(list.get(0),mimeTypeFile,path,0,false);
                            mListImport.add(importFiles);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    ServiceManager.getInstance().setListImport(mListImport);
                    ServiceManager.getInstance().onPreparingImportData();
                } else {
                    Utils.Log(TAG, "Nothing to do on Gallery");
                }
                break;
            }
            default: {
                Utils.Log(TAG, "Nothing to do");
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

    @Override
    protected void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }
        presenter.getData();
        SingletonFakePinComponent.getInstance().setListener(this);
        onRegisterHomeWatcher();
        SingletonManager.getInstance().setVisitFakePin(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ServiceManager.getInstance().onDismissServices();
        Utils.Log(TAG,"OnDestroy");
        EventBus.getDefault().unregister(this);
        presenter.unbindView();
        SingletonManager.getInstance().setVisitFakePin(false);
    }

    @Override
    protected void onStopListenerAWhile() {
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onOrientationChange(boolean isFaceDown) {

    }

    @Override
    public void onUpdateView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                presenter.getData();
            }
        });
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
        return this;
    }

    @Override
    public void onClickItem(int position) {
        Utils.Log(TAG,"Position :"+ position);
        try {
            String value  = Utils.getHexCode(getString(R.string.key_trash));
            if (value.equals(presenter.mList.get(position).categories_hex_name)){
                Navigator.onMoveTrash(getActivity());
            }
            else{
                final MainCategoryModel mainCategories = presenter.mList.get(position);
                Navigator.onMoveAlbumDetail(getActivity(),mainCategories);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onSetting(int position) {
        Navigator.onAlbumSettings(getActivity(),presenter.mList.get(position));
    }

    @Override
    public void onDeleteAlbum(int position) {
        presenter.onDeleteAlbum(position);
    }

    @Override
    public void onEmptyTrash(int position) {

    }

    @Override
    public void onBackPressed() {
        if (mSpeedDialView.isOpen()){
            mSpeedDialView.close();
        }
        else{
            super.onBackPressed();
        }
    }
}
