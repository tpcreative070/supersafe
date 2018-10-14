package co.tpcreative.supersafe.ui.fakepin;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.os.Bundle;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Toast;
import com.afollestad.materialdialogs.MaterialDialog;
import com.darsh.multipleimageselect.helpers.Constants;
import com.darsh.multipleimageselect.models.Image;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;
import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.activity.BaseActivity;
import co.tpcreative.supersafe.common.controller.ServiceManager;
import co.tpcreative.supersafe.common.controller.SingletonFakePinComponent;
import co.tpcreative.supersafe.common.controller.SingletonPrivateFragment;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.common.views.GridSpacingItemDecoration;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.MainCategories;
import co.tpcreative.supersafe.model.MimeTypeFile;
import co.tpcreative.supersafe.model.Theme;


public class FakePinComponentActivity extends BaseActivity implements BaseView ,FakePinComponentAdapter.ItemSelectedListener,SingletonFakePinComponent.SingletonPrivateFragmentListener{

    @BindView(R.id.speedDial)
    SpeedDialView mSpeedDialView;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    private FakePinComponentAdapter adapter;
    private FakePinComponentPresenter presenter;
    private static final String TAG = FakePinComponentActivity.class.getSimpleName();

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
        presenter.getData();
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
        final Theme mTheme = Theme.getInstance().getThemeInfo();
        Drawable drawable = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.baseline_photo_camera_white_24);
        mSpeedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id
                .fab_camera, drawable)
                .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), mTheme.getPrimaryColor(),
                        getTheme()))

                .setLabel(getString(R.string.camera))
                .setLabelColor(Color.WHITE)
                .setLabelBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.inbox_primary,
                        getTheme()))
                .create());

        drawable = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.baseline_photo_white_24);
        mSpeedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id.fab_photo, drawable)
                .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), mTheme.getPrimaryColor(),
                        getTheme()))
                .setLabel(R.string.photo)
                .setLabelColor(getResources().getColor(R.color.white))
                .setLabelBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.inbox_primary,
                        getTheme()))
                .create());

        mSpeedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id.fab_album, R.drawable
                .baseline_add_to_photos_white_36)
                .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), mTheme.getPrimaryColor(),
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
                Log.d(TAG, "Speed dial toggle state changed. Open = " + isOpen);
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
                        showMessage(actionItem.getLabel(getApplicationContext()) + " Photo");
                        Navigator.onMoveToAlbum(FakePinComponentActivity.this);
                        return false; // closes without animation (same as mSpeedDialView.close(false); return false;)
                    case R.id.fab_camera:
                        showMessage(actionItem.getLabel(getApplicationContext()) + " Camera");
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
                        MainCategories item = MainCategories.getInstance().getTrashItem();
                        String result = item.categories_hex_name;
                        if (base64Code.equals(result)) {
                            Toast.makeText(FakePinComponentActivity.this, "This name already existing", Toast.LENGTH_SHORT).show();
                        } else {
                            boolean response = MainCategories.getInstance().onAddFakePinCategories(base64Code, value,true);
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
                            final List<MainCategories> list = MainCategories.getInstance().getListFakePin();
                            if (list != null) {
                                Navigator.onMoveCamera(FakePinComponentActivity.this, list.get(0));
                            }
                        } else {
                            Log.d(TAG, "Permission is denied");
                        }
                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            /*Miss add permission in manifest*/
                            Log.d(TAG, "request permission is failed");
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
                        Log.d(TAG, "error ask permission");
                    }
                }).onSameThread().check();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "Selected album :");
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
            case Constants.REQUEST_CODE: {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    ArrayList<Image> images = data.getParcelableArrayListExtra(Constants.INTENT_EXTRA_IMAGES);
                    for (int i = 0, l = images.size(); i < l; i++) {
                        String path = images.get(i).path;
                        String name = images.get(i).name;
                        String id = "" + images.get(i).id;
                        String mimeType = Utils.getMimeType(path);
                        Log.d(TAG, "mimeType " + mimeType);
                        Log.d(TAG, "name " + name);
                        Log.d(TAG, "path " + path);
                        String fileExtension = Utils.getFileExtension(path);
                        Log.d(TAG, "file extension " + Utils.getFileExtension(path));

                        try {
                            final MimeTypeFile mimeTypeFile = Utils.mediaTypeSupport().get(fileExtension);
                            mimeTypeFile.name = name;
                            final List<MainCategories> list = MainCategories.getInstance().getListFakePin();
                            if (list == null) {
                                Utils.onWriteLog("Main categories is null", EnumStatus.WRITE_FILE);
                                return;
                            }
                            ServiceManager.getInstance().onSaveDataOnGallery(mimeTypeFile, path, id, list.get(0));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
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

    @Override
    protected void onResume() {
        super.onResume();
        SingletonFakePinComponent.getInstance().setListener(this);
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
        Log.d(TAG,"Position :"+ position);
        try {
            String value  = Utils.getHexCode(getString(R.string.key_trash));
            if (value.equals(presenter.mList.get(position).categories_hex_name)){
                Navigator.onMoveTrash(getActivity());
            }
            else{
                final MainCategories mainCategories = presenter.mList.get(position);
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
