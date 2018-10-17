package co.tpcreative.supersafe.ui.albumdetail;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
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
import com.snatik.storage.Storage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;
import butterknife.OnClick;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.SensorOrientationChangeNotifier;
import co.tpcreative.supersafe.common.activity.BaseGalleryActivity;
import co.tpcreative.supersafe.common.controller.GalleryCameraMediaManager;
import co.tpcreative.supersafe.common.controller.ServiceManager;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.common.util.Configuration;
import co.tpcreative.supersafe.common.views.GridSpacingItemDecoration;
import co.tpcreative.supersafe.model.EnumFormatType;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.Items;
import co.tpcreative.supersafe.model.MainCategories;
import co.tpcreative.supersafe.model.MimeTypeFile;
import co.tpcreative.supersafe.model.room.InstanceGenerator;
import dmax.dialog.SpotsDialog;


public class AlbumDetailActivity extends BaseGalleryActivity implements BaseView, AlbumDetailAdapter.ItemSelectedListener, GalleryCameraMediaManager.AlbumDetailManagerListener{
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
    TextView tv_Audios;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.llBottom)
    LinearLayout llBottom;
    private AlbumDetailPresenter presenter;
    private AlbumDetailAdapter adapter;
    private boolean isReload;
    private Storage storage;
    private ActionMode actionMode;
    private int countSelected;
    private boolean isSelectAll = false;
    private AlertDialog dialog;


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

        presenter = new AlbumDetailPresenter();
        presenter.bindView(this);
        presenter.getData(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(presenter.mainCategories.categories_name);

        final Items items = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getLatestId(presenter.mainCategories.categories_local_id, false,presenter.mainCategories.isFakePin);
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
                backdrop.setRotation(items.degrees);
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
        onDrawOverLay(this);
        llBottom.setVisibility(View.INVISIBLE);


        /*Root Fragment*/
        attachFragment(R.id.gallery_root);
    }

    @Override
    public void onStillScreenLock(EnumStatus status) {
        super.onStillScreenLock(status);
        switch (status){
            case FINISH:{
                finish();
                break;
            }
        }
    }

    @Override
    public void onOrientationChange(boolean isFaceDown) {
        onFaceDown(isFaceDown);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_album_detail, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_album_settings:{
                Navigator.onAlbumSettings(this,presenter.mainCategories);
                return true;
            }
            case R.id.action_select_items :{
                if (actionMode == null) {
                    actionMode = toolbar.startActionMode(callback);
                }
                countSelected = 0;
                actionMode.setTitle(countSelected + " " + getString(com.darsh.multipleimageselect.R.string.selected));
                Utils.Log(TAG,"Action here");
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClickItem(int position) {
        if (actionMode!=null){
            toggleSelection(position);
            actionMode.setTitle(countSelected + " " + getString(com.darsh.multipleimageselect.R.string.selected));
        }
        else{
            try {
                Navigator.onPhotoSlider(this, presenter.mList.get(position), presenter.mList,presenter.mainCategories);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onLongClickItem(int position) {
        if (actionMode == null) {
            actionMode = toolbar.startActionMode(callback);
        }
        toggleSelection(position);
        actionMode.setTitle(countSelected + " " + getString(com.darsh.multipleimageselect.R.string.selected));
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

    @OnClick(R.id.imgShare)
    public void onClickedShare(View view){
        if (countSelected>0){
            storage.createDirectory(SuperSafeApplication.getInstance().getSupersafePicture());
            presenter.status = EnumStatus.SHARE;
            onShowDialog(presenter.status);
        }
    }

    @OnClick(R.id.imgExport)
    public void onClickedExport(){
        storage.createDirectory(SuperSafeApplication.getInstance().getSupersafePicture());
        presenter.status = EnumStatus.EXPORT;
        onShowDialog(presenter.status);
    }

    @OnClick(R.id.imgDelete)
    public void onClickedDelete(View view){
       if (countSelected>0){
           presenter.status = EnumStatus.DELETE;
           onShowDialog(presenter.status);
       }
    }

    @OnClick(R.id.imgMove)
    public void onClickedMove(View view){
        openAlbum();
    }

    /*Exporting....*/
    @Override
    public void onStartProgress() {
        onStartProgressing();
    }

    /*Exporting*/
    @Override
    public void onStopProgress() {
        try {
            if (presenter.mListExportShare.size()==0){
                Utils.Log(TAG,"onStopProgress");
                onStopProgressing();
                switch (presenter.status){
                    case SHARE:{
                        if (presenter.mListShare!=null){
                            if (presenter.mListShare.size()>0){
                                Utils.shareMultiple(presenter.mListShare,this);
                            }
                        }
                        break;
                    }
                    case EXPORT:{
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(AlbumDetailActivity.this,"Exported at "+SuperSafeApplication.getInstance().getSupersafePicture(),Toast.LENGTH_LONG).show();
                            }
                        });
                        break;
                    }
                }
            }
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
    }

    private void onStartProgressing(){
        try{
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (dialog==null){
                        dialog = new SpotsDialog.Builder()
                                .setContext(AlbumDetailActivity.this)
                                .setMessage(getString(R.string.progressing))
                                .setCancelable(true)
                                .build();
                    }
                    if (!dialog.isShowing()){
                        dialog.show();
                        Utils.Log(TAG,"Showing dialog...");
                    }
                }
            });
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void onStopProgressing(){
        try{
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (dialog!=null){
                        dialog.dismiss();
                        if (actionMode!=null){
                            actionMode.finish();
                        }
                    }
                }
            });
        }
        catch (Exception e){
           Utils.Log(TAG,e.getMessage());
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
        onRegisterHomeWatcher();
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
        }else if (actionMode!=null){
            actionMode.finish();
        } else {
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


    public void onShowDialog(EnumStatus status){
        String content = "";
        switch (status){
            case EXPORT:{
                content = String.format(getString(R.string.export_items),""+countSelected);
                break;
            }
            case SHARE:{
                content = String.format(getString(R.string.share_items),""+countSelected);
                break;
            }
            case DELETE:{
                content = String.format(getString(R.string.move_items_to_trash),""+countSelected);
                break;
            }
            case MOVE:{
                break;
            }
        }


        MaterialDialog.Builder builder =  new MaterialDialog.Builder(this)
                .title(getString(R.string.confirm))
                .theme(Theme.LIGHT)
                .content(content)
                .titleColor(getResources().getColor(R.color.black))
                .negativeText(getString(R.string.cancel))
                .positiveText(getString(R.string.ok))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                        switch (status){
                            case SHARE:{
                                GalleryCameraMediaManager.getInstance().onStartProgress();
                                presenter.mListExportShare.clear();
                                presenter.mListShare.clear();
                                for (Items index : presenter.mList){
                                    if (index.isChecked){
                                        presenter.mListExportShare.add(index.id);
                                        EnumFormatType formatType = EnumFormatType.values()[index.formatType];
                                        switch (formatType){
                                            case AUDIO:{
                                                File input = new File(index.originalPath);
                                                File output = new File(SuperSafeApplication.getInstance().getSupersafeShare() +index.originalName +index.fileExtension);
                                                if (storage.isFileExist(input.getAbsolutePath())){
                                                    presenter.mListShare.add(output);
                                                    ServiceManager.getInstance().onExportFiles(input,output,presenter.mListExportShare);
                                                }
                                                break;
                                            }
                                            case VIDEO:{
                                                File input = new File(index.originalPath);
                                                File output = new File(SuperSafeApplication.getInstance().getSupersafeShare()+index.originalName +index.fileExtension);
                                                if (storage.isFileExist(input.getAbsolutePath())){
                                                    presenter.mListShare.add(output);
                                                    ServiceManager.getInstance().onExportFiles(input,output,presenter.mListExportShare);
                                                }
                                                break;
                                            }
                                            default:{
                                                File input = new File(index.thumbnailPath);
                                                File output = new File(SuperSafeApplication.getInstance().getSupersafeShare()+index.originalName +index.fileExtension);
                                                if (storage.isFileExist(input.getAbsolutePath())){
                                                    presenter.mListShare.add(output);
                                                    ServiceManager.getInstance().onExportFiles(input,output,presenter.mListExportShare);
                                                }
                                                break;
                                            }
                                        }
                                    }
                                }
                                break;
                            }
                            case EXPORT:{
                                GalleryCameraMediaManager.getInstance().onStartProgress();
                                presenter.mListExportShare.clear();
                                presenter.mListShare.clear();
                                for (Items index : presenter.mList){
                                    if (index.isChecked){
                                        presenter.mListExportShare.add(index.id);
                                        EnumFormatType formatType = EnumFormatType.values()[index.formatType];
                                        switch (formatType){
                                            case AUDIO:{
                                                File input = new File(index.originalPath);
                                                Utils.Log(TAG,"Name :"+index.originalName);
                                                File output = new File(SuperSafeApplication.getInstance().getSupersafePicture() +index.title);
                                                if (storage.isFileExist(input.getAbsolutePath())){
                                                    presenter.mListShare.add(output);
                                                    ServiceManager.getInstance().onExportFiles(input,output,presenter.mListExportShare);
                                                }
                                                break;
                                            }
                                            case VIDEO:{
                                                File input = new File(index.originalPath);
                                                File output = new File(SuperSafeApplication.getInstance().getSupersafePicture()+index.title);
                                                if (storage.isFileExist(input.getAbsolutePath())){
                                                    presenter.mListShare.add(output);
                                                    ServiceManager.getInstance().onExportFiles(input,output,presenter.mListExportShare);
                                                }
                                                break;
                                            }
                                            default:{
                                                File input = new File(index.originalPath);
                                                File output = new File(SuperSafeApplication.getInstance().getSupersafePicture()+index.title);
                                                if (storage.isFileExist(input.getAbsolutePath())){
                                                    presenter.mListShare.add(output);
                                                    ServiceManager.getInstance().onExportFiles(input,output,presenter.mListExportShare);
                                                }
                                                break;
                                            }
                                        }
                                    }
                                }
                                break;
                            }
                            case DELETE:{
                                presenter.onDelete();
                                if (actionMode!=null){
                                    actionMode.finish();
                                }
                                isReload = true;
                                break;
                            }
                        }
                    }
                });
        builder.show();
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
                    List<Integer> mListFiles = new ArrayList<>();
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
                            mListFiles.add(i);
                            ServiceManager.getInstance().onSaveDataOnGallery(mimeTypeFile,mListFiles, path, presenter.mainCategories);
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
            case Navigator.SHARE :{
                if (actionMode!=null){
                    actionMode.finish();
                }
                Utils.Log(TAG,"share action");
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
        storage.deleteDirectory(SuperSafeApplication.getInstance().getSupersafeShare());
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
                adapter.setDataSource(presenter.mList);

                if (actionMode!=null){
                    countSelected = 0;
                    actionMode.finish();
                    llBottom.setVisibility(View.INVISIBLE);
                    isReload = true;
                }
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
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int i = item.getItemId();
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
        try{
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (countSelected==0){
                        llBottom.setVisibility(View.INVISIBLE);
                        mSpeedDialView.setVisibility(View.VISIBLE);
                    }
                    else{
                        llBottom.setVisibility(View.VISIBLE);
                        mSpeedDialView.setVisibility(View.INVISIBLE);
                    }
                }
            });
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


    /*Gallery action*/


    @Override
    public Configuration getConfiguration() {
        //default configuration
        Configuration cfg=new Configuration.Builder()
                .hasCamera(true)
                .hasShade(true)
                .hasPreview(true)
                .setSpaceSize(4)
                .setPhotoMaxWidth(120)
                .setLocalCategoriesId(presenter.mainCategories.categories_local_id)
                .setCheckBoxColor(0xFF3F51B5)
                .setDialogHeight(Configuration.DIALOG_HALF)
                .setDialogMode(Configuration.DIALOG_LIST)
                .setMaximum(9)
                .setTip(null)
                .setAblumsTitle(null)
                .build();
        return cfg;
    }


    @Override
    public void onMoveAlbumSuccessful() {

    }

    @Override
    public List<Items> getListItems() {
        return presenter.mList;
    }



}
