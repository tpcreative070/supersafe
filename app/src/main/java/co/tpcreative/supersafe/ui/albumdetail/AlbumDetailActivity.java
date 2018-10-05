package co.tpcreative.supersafe.ui.albumdetail;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
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
import com.litao.android.lib.Utils.GridSpacingItemDecoration;
import com.snatik.storage.Storage;
import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.activity.BaseActivity;
import co.tpcreative.supersafe.common.controller.GalleryCameraMediaManager;
import co.tpcreative.supersafe.common.controller.ServiceManager;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumFormatType;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.Items;
import co.tpcreative.supersafe.model.MainCategories;
import co.tpcreative.supersafe.model.MimeTypeFile;
import co.tpcreative.supersafe.model.room.InstanceGenerator;


public class AlbumDetailActivity extends BaseActivity implements BaseView, AlbumDetailAdapter.ItemSelectedListener, GalleryCameraMediaManager.AlbumDetailManagerListener {
    private static final String TAG = AlbumDetailActivity.class.getSimpleName();
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.speedDial)
    SpeedDialView mSpeedDialView;
    @BindView(R.id.main_content)
    CoordinatorLayout main_content;
    @BindView(R.id.backdrop)
    ImageView backdrop;
    @BindView(R.id.imgIcon)
    ImageView imgIcon;
    @BindView(R.id.tv_Photos)
    TextView tv_Photos;
    @BindView(R.id.tv_Videos)
    TextView tv_Videos;
    @BindView(R.id.tv_Audios)
    TextView tvAudios;
    private AlbumDetailPresenter presenter;
    private AlbumDetailAdapter adapter;
    private boolean isReload;
    private Storage storage;

    RequestOptions options = new RequestOptions()
            .centerCrop()
            .override(400, 400)
            .placeholder(R.color.colorPrimary)
            .error(R.color.colorPrimary)
            .priority(Priority.HIGH);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_detail);

        storage = new Storage(this);
        storage.setEncryptConfiguration(SuperSafeApplication.getInstance().getConfigurationFile());

        initRecycleView(getLayoutInflater());
        initSpeedDial(true);

        onDrawOverLay(this);


        presenter = new AlbumDetailPresenter();
        presenter.bindView(this);
        presenter.getData(this);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(presenter.mainCategories.categories_name);

        final Items items = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getLatestId(presenter.mainCategories.categories_local_id, false);
        if (items != null) {
            EnumFormatType formatTypeFile = EnumFormatType.values()[items.formatType];
            if (formatTypeFile == EnumFormatType.AUDIO) {

                try {
                    int myColor = Color.parseColor(presenter.mainCategories.image);
                    backdrop.setBackgroundColor(myColor);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            } else {
                Glide.with(this)
                        .load(storage.readFile(items.thumbnailPath))
                        .apply(options)
                        .into(backdrop);
                imgIcon.setVisibility(View.INVISIBLE);

            }
        } else {
            backdrop.setImageResource(0);
            try {
                int myColor = Color.parseColor(presenter.mainCategories.image);
                backdrop.setBackgroundColor(myColor);
            }
            catch (Exception e){

            }
            imgIcon.setVisibility(View.VISIBLE);
            imgIcon.setImageDrawable(MainCategories.getInstance().getDrawable(this,presenter.mainCategories.icon));
        }
        GalleryCameraMediaManager.getInstance().setListener(this);

    }

    @Override
    public void onUpdatedView() {
        try {
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    presenter.getData();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

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
    protected void onResume() {
        super.onResume();
        GalleryCameraMediaManager.getInstance().setListener(this);
    }


    /*Init Floating View*/

    private void initSpeedDial(boolean addActionItems) {
        final co.tpcreative.supersafe.model.Theme mTheme = co.tpcreative.supersafe.model.Theme.getInstance().getThemeInfo();
        if (addActionItems) {
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
            mSpeedDialView.setMainFabAnimationRotateAngle(180);
        }

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
                        showToast(" Album");
                        return false; // false will close it without animation
                    case R.id.fab_photo:
                        showToast(actionItem.getLabel(getApplicationContext()) + " Photo");
                        Navigator.onMoveToAlbum(AlbumDetailActivity.this);
                        return false; // closes without animation (same as mSpeedDialView.close(false); return false;)
                    case R.id.fab_camera:
                        showToast(actionItem.getLabel(getApplicationContext()) + " Camera");
                        onAddPermissionCamera();
                        return false;
                }
                return true; // To keep the Speed Dial open
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mSpeedDialView.isOpen()){
            mSpeedDialView.close();
        }else {
            super.onBackPressed();
        }
    }


    /*Init grant permission*/

    public void onAddPermissionCamera() {
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.CAMERA)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            Navigator.onMoveCamera(AlbumDetailActivity.this, presenter.mainCategories);
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

    public void initRecycleView(LayoutInflater layoutInflater) {
        adapter = new AlbumDetailAdapter(layoutInflater, getApplicationContext(), this);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getApplicationContext(), 3);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(3, 4, true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onClickItem(int position) {
        Utils.Log(TAG, "Position : " + position);
        try {
            Navigator.onPhotoSlider(this, presenter.mList.get(position), presenter.mList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAddToFavoriteSelected(int position) {

    }

    @Override
    public void onPlayNextSelected(int position) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "Selected album :");
        switch (requestCode) {
            case Navigator.CAMERA_ACTION: {
                if (resultCode == Activity.RESULT_OK) {
                    Utils.Log(TAG, "reload data");
                    presenter.getData();
                } else {
                    Utils.Log(TAG, "Nothing to do on Camera");
                }
                break;
            }
            case Navigator.PHOTO_SLIDE_SHOW: {
                if (resultCode == Activity.RESULT_OK) {
                    Utils.Log(TAG, "reload data");
                    presenter.getData();
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
                            if (presenter.mainCategories == null) {
                                Utils.onWriteLog("Main categories is null", EnumStatus.WRITE_FILE);
                                return;
                            }
                            ServiceManager.getInstance().onSaveDataOnGallery(mimeTypeFile, path, id, presenter.mainCategories);
                            isReload = true;
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

    /**
     * Converting dp to pixel
     */

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isReload) {
            ServiceManager.getInstance().onSyncDataOwnServer("0");
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
    public void onSuccessful(String message, EnumStatus status, Object object) {

    }

    @Override
    public void onSuccessful(String message, EnumStatus status, List list) {

    }
}
