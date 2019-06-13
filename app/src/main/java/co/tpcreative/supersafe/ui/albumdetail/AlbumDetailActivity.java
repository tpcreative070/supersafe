package co.tpcreative.supersafe.ui.albumdetail;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
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
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.activity.BaseGalleryActivity;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.controller.ServiceManager;
import co.tpcreative.supersafe.common.controller.SingletonPrivateFragment;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.ConvertUtils;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.common.util.Configuration;
import co.tpcreative.supersafe.common.views.GridSpacingItemDecoration;
import co.tpcreative.supersafe.common.views.NpaGridLayoutManager;
import co.tpcreative.supersafe.model.EnumDelete;
import co.tpcreative.supersafe.model.EnumFormatType;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.ExportFiles;
import co.tpcreative.supersafe.model.Image;
import co.tpcreative.supersafe.model.ImportFiles;
import co.tpcreative.supersafe.model.Items;
import co.tpcreative.supersafe.model.MainCategories;
import co.tpcreative.supersafe.model.MimeTypeFile;
import co.tpcreative.supersafe.model.ThemeApp;
import co.tpcreative.supersafe.model.User;
import co.tpcreative.supersafe.model.room.InstanceGenerator;
import dmax.dialog.SpotsDialog;

public class AlbumDetailActivity extends BaseGalleryActivity implements BaseView<Integer>, AlbumDetailAdapter.ItemSelectedListener,AlbumDetailVerticalAdapter.ItemSelectedListener{
    private static final String TAG = AlbumDetailActivity.class.getSimpleName();
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.speedDial)
    SpeedDialView mSpeedDialView;
    @BindView(R.id.backdrop)
    ImageView backdrop;
    @BindView(R.id.tv_Photos)
    TextView tv_Photos;
    @BindView(R.id.tv_Videos)
    TextView tv_Videos;
    @BindView(R.id.tv_Audios)
    TextView tv_Audios;
    @BindView(R.id.tv_Others)
    TextView tv_Others;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.llBottom)
    LinearLayout llBottom;
    private AlbumDetailPresenter presenter;
    private AlbumDetailAdapter adapter;
    private AlbumDetailVerticalAdapter verticalAdapter;
    private boolean isReload;
    private Storage storage;
    private ActionMode actionMode;
    private int countSelected;
    private boolean isSelectAll = false;
    private AlertDialog dialog;
    SweetAlertDialog mDialogProgress;
    private MenuItem menuItem;
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
        initSpeedDial(true);
        presenter = new AlbumDetailPresenter();
        presenter.bindView(this);
        onInit();
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(presenter.mainCategories.categories_name);
        final Items items = InstanceGenerator.getInstance(this).getItemId(presenter.mainCategories.items_id);
        if (items != null) {
            EnumFormatType formatTypeFile = EnumFormatType.values()[items.formatType];
            switch (formatTypeFile){
                case AUDIO:{
                    try {
                        int myColor = Color.parseColor(presenter.mainCategories.image);
                        backdrop.setBackgroundColor(myColor);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
                }
                case FILES:{
                    try {
                        int myColor = Color.parseColor(presenter.mainCategories.image);
                        backdrop.setBackgroundColor(myColor);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
                }
                default:{
                    if (storage.isFileExist(items.thumbnailPath)){
                        backdrop.setRotation(items.degrees);
                        Glide.with(this)
                                .load(storage.readFile(items.thumbnailPath))
                                .apply(options)
                                .into(backdrop);
                    }
                    else{
                        backdrop.setImageResource(0);
                        int myColor = Color.parseColor(presenter.mainCategories.image);
                        backdrop.setBackgroundColor(myColor);
                    }
                    break;
                }
            }
        } else {
            backdrop.setImageResource(0);
            final MainCategories mainCategories = MainCategories.getInstance().getCategoriesPosition(presenter.mainCategories.mainCategories_Local_Id);
            if (mainCategories!=null){
                try {
                    int myColor = Color.parseColor(mainCategories.image);
                    backdrop.setBackgroundColor(myColor);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else{
                try {
                    int myColor = Color.parseColor(presenter.mainCategories.image);
                    backdrop.setBackgroundColor(myColor);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        llBottom.setVisibility(View.GONE);
        /*Root Fragment*/
        attachFragment(R.id.gallery_root);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                Utils.Log(TAG,"Scrolling change listener");
                if (actionMode!=null){
                    mSpeedDialView.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    public void onInit(){
        presenter.getData(this);
        initRecycleView(getLayoutInflater());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EnumStatus event) {
        switch (event){
            case FINISH:{
                Navigator.onMoveToFaceDown(this);
                break;
            }
            case UPDATED_VIEW_DETAIL_ALBUM:{
                try {
                    this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            presenter.getData(EnumStatus.RELOAD);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
            case START_PROGRESS:{
                onStartProgressing();
                break;
            }
            case STOP_PROGRESS:{
                try {
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
                catch (Exception e){
                    Utils.Log(TAG,e.getMessage());
                }
                break;
            }
            case DOWNLOAD_COMPLETED:{
                mDialogProgress.setTitleText("Success!")
                        .setConfirmText("OK")
                        .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                mDialogProgress.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismiss();
                        onClickedExport();
                    }
                });
                Utils.Log(TAG, "already sync ");
                break;
            }
            case DOWNLOAD_FAILED:{
                mDialogProgress.setTitleText("No connection, Try again")
                        .setConfirmText("OK")
                        .changeAlertType(SweetAlertDialog.ERROR_TYPE);
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
        if (isReload) {
            ServiceManager.getInstance().onSyncDataOwnServer("0");
        }
        storage.deleteDirectory(SuperSafeApplication.getInstance().getSupersafeShare());
    }

    @Override
    protected void onStop() {
        super.onStop();
        Utils.Log(TAG,"onStop Album");
    }

    @Override
    protected void onStopListenerAWhile() {
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onOrientationChange(boolean isFaceDown) {
        onFaceDown(isFaceDown);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (toolbar==null){
            return false;
        }
        toolbar.inflateMenu(R.menu.menu_album_detail);
        this.menuItem = toolbar.getMenu().getItem(0);
        boolean isVertical = PrefsController.getBoolean(getString(R.string.key_vertical_adapter),false);
        if (isVertical){
            menuItem.setIcon(getResources().getDrawable(R.drawable.baseline_view_comfy_white_48));
        }
        else{
            menuItem.setIcon(getResources().getDrawable(R.drawable.baseline_format_list_bulleted_white_48));
        }
        return true;
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
                actionMode.setTitle(countSelected + " " + getString(R.string.selected));
                Utils.Log(TAG,"Action here");
                return true;
            }
            case R.id.action_view :{
               if (menuItem!=null){
                   boolean isVertical = PrefsController.getBoolean(getString(R.string.key_vertical_adapter),false);
                   if (isVertical){
                       menuItem.setIcon(getResources().getDrawable(R.drawable.baseline_format_list_bulleted_white_48));
                       PrefsController.putBoolean(getString(R.string.key_vertical_adapter),false);
                       onInit();
                   }
                   else{
                       menuItem.setIcon(getResources().getDrawable(R.drawable.baseline_view_comfy_white_48));
                       PrefsController.putBoolean(getString(R.string.key_vertical_adapter),true);
                       onInit();
                   }
               }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClickItem(int position) {
        Utils.Log(TAG,"On clicked item");
        if (position>=presenter.mList.size()){
            return;
        }
        if (actionMode!=null){
            toggleSelection(position);
            actionMode.setTitle(countSelected + " " + getString(R.string.selected));
        }
        else{
            try {
                EnumFormatType formatType = EnumFormatType.values()[presenter.mList.get(position).formatType];
                switch (formatType){
                    case FILES:{
                        Toast.makeText(getContext(),"Can not support to open type of this file",Toast.LENGTH_SHORT).show();
                        break;
                    }
                    default:{
                        Navigator.onPhotoSlider(this, presenter.mList.get(position), presenter.mList,presenter.mainCategories);
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onLongClickItem(int position) {
        Utils.Log(TAG,"On long clicked item");
        if (position>=presenter.mList.size()){
            return;
        }
        if (actionMode == null) {
            actionMode = toolbar.startActionMode(callback);
        }
        toggleSelection(position);
        actionMode.setTitle(countSelected + " " + getString(R.string.selected));
    }

    @OnClick(R.id.imgShare)
    public void onClickedShare(View view){
       if (countSelected>0){
            storage.createDirectory(SuperSafeApplication.getInstance().getSupersafeShare());
            presenter.status = EnumStatus.SHARE;
            onShowDialog(presenter.status);
        }
    }

    @OnClick(R.id.imgExport)
    public void onClickedExport(){
        if (countSelected>0){
            storage.createDirectory(SuperSafeApplication.getInstance().getSupersafePicture());
            presenter.status = EnumStatus.EXPORT;
            boolean isSaver = false;
            long spaceAvailable = 0;
            for (int i = 0;i<presenter.mList.size();i++){
                final Items items = presenter.mList.get(i);
                if (items.isSaver && items.isChecked){
                    isSaver = true;
                    spaceAvailable +=Long.parseLong(items.size);;
                }
            }
            long availableSpaceOS =  Utils.getAvailableSpaceInBytes();
            if (availableSpaceOS < spaceAvailable){
                long request_spaces = spaceAvailable - availableSpaceOS;
                String result =  ConvertUtils.byte2FitMemorySize(request_spaces);
                String message = String.format(getString(R.string.your_space_is_not_enough_to),"export. ","Request spaces: "+result);
                Utils.showDialog(this,message);
            }
            else{
                if (isSaver){
                    onEnableSyncData();
                }
                else{
                    onShowDialog(presenter.status);
                }
            }
        }
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

    private void onStartProgressing(){
        try{
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (dialog==null){
                        ThemeApp themeApp = ThemeApp.getInstance().getThemeInfo();
                        dialog = new SpotsDialog.Builder()
                                .setContext(AlbumDetailActivity.this)
                                .setDotColor(themeApp.getAccentColor())
                                .setMessage(getString(R.string.exporting))
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

    /*Init Floating View*/
    private void initSpeedDial(boolean addActionItems) {
        final ThemeApp mThemeApp = ThemeApp.getInstance().getThemeInfo();
        if (addActionItems) {
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
                Utils.Log(TAG, "Speed dial toggle state changed. Open = " + isOpen);
            }
        });

        //Set option fabs clicklisteners.
        mSpeedDialView.setOnActionSelectedListener(new SpeedDialView.OnActionSelectedListener() {
            @Override
            public boolean onActionSelected(SpeedDialActionItem actionItem) {
                switch (actionItem.getId()) {
                    case R.id.fab_album:
                        return false; // false will close it without animation
                    case R.id.fab_photo:
                        Navigator.onMoveToAlbum(AlbumDetailActivity.this);
                        return false; // closes without animation (same as mSpeedDialView.close(false); return false;)
                    case R.id.fab_camera:
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

    public void initRecycleView(LayoutInflater layoutInflater) {
        try {
            boolean isVertical = PrefsController.getBoolean(getString(R.string.key_vertical_adapter),false);
            if (isVertical){
                verticalAdapter = new AlbumDetailVerticalAdapter(getLayoutInflater(),this,this);
                RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                recyclerView.setLayoutManager(mLayoutManager);
                while (recyclerView.getItemDecorationCount() > 0) {
                    recyclerView.removeItemDecorationAt(0);
                }
                recyclerView.addItemDecoration(new DividerItemDecoration(this, 0));
                recyclerView.setAdapter(verticalAdapter);
                verticalAdapter.setDataSource(presenter.mList);
            }
            else{
                adapter = new AlbumDetailAdapter(layoutInflater, getApplicationContext(), this);
                RecyclerView.LayoutManager mLayoutManager = new NpaGridLayoutManager(getApplicationContext(), 3);
                recyclerView.setLayoutManager(mLayoutManager);
                while (recyclerView.getItemDecorationCount() > 0) {
                    recyclerView.removeItemDecorationAt(0);
                }
                recyclerView.addItemDecoration(new GridSpacingItemDecoration(3, 4, true));
                recyclerView.setAdapter(adapter);
                adapter.setDataSource(presenter.mList);
            }
        }
        catch (Exception e){
            e.getMessage();
        }
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
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        presenter.status = EnumStatus.CANCEL;
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        List<ExportFiles> mListExporting = new ArrayList<>();
                        switch (status){
                            case SHARE:{
                                EventBus.getDefault().post(EnumStatus.START_PROGRESS);
                                presenter.mListShare.clear();
                                for (int i = 0; i< presenter.mList.size();i++){
                                    Items index = presenter.mList.get(i);
                                    if (index.isChecked){
                                        EnumFormatType formatType = EnumFormatType.values()[index.formatType];
                                        switch (formatType){
                                            case AUDIO:{
                                                File input = new File(index.originalPath);
                                                File output = new File(SuperSafeApplication.getInstance().getSupersafeShare() +index.originalName +index.fileExtension);
                                                if (storage.isFileExist(output.getAbsolutePath())){
                                                    output = new File(SuperSafeApplication.getInstance().getSupersafeShare()+index.originalName+"(1)" +index.fileExtension);
                                                }
                                                if (storage.isFileExist(input.getAbsolutePath())){
                                                    presenter.mListShare.add(output);
                                                    ExportFiles exportFiles = new ExportFiles(input,output,i,false,index.formatType);
                                                    mListExporting.add(exportFiles);
                                                }
                                                break;
                                            }
                                            case FILES:{
                                                File input = new File(index.originalPath);
                                                File output = new File(SuperSafeApplication.getInstance().getSupersafeShare() +index.originalName +index.fileExtension);
                                                if (storage.isFileExist(output.getAbsolutePath())){
                                                    output = new File(SuperSafeApplication.getInstance().getSupersafeShare()+index.originalName+"(1)" +index.fileExtension);
                                                }
                                                if (storage.isFileExist(input.getAbsolutePath())){
                                                    presenter.mListShare.add(output);
                                                    ExportFiles exportFiles = new ExportFiles(input,output,i,false,index.formatType);
                                                    mListExporting.add(exportFiles);
                                                }
                                                break;
                                            }
                                            case VIDEO:{
                                                File input = new File(index.originalPath);
                                                File output = new File(SuperSafeApplication.getInstance().getSupersafeShare()+index.originalName +index.fileExtension);

                                                if (storage.isFileExist(output.getAbsolutePath())){
                                                    output = new File(SuperSafeApplication.getInstance().getSupersafeShare()+index.originalName+"(1)" +index.fileExtension);
                                                }
                                                if (storage.isFileExist(input.getAbsolutePath())){
                                                    presenter.mListShare.add(output);
                                                    ExportFiles exportFiles = new ExportFiles(input,output,i,false,index.formatType);
                                                    mListExporting.add(exportFiles);
                                                }
                                                break;
                                            }
                                            default:{
                                                File input = new File(index.thumbnailPath);
                                                File output = new File(SuperSafeApplication.getInstance().getSupersafeShare()+index.originalName +index.fileExtension);
                                                if (storage.isFileExist(output.getAbsolutePath())){
                                                    output = new File(SuperSafeApplication.getInstance().getSupersafeShare()+index.originalName+"(1)" +index.fileExtension);
                                                }
                                                if (storage.isFileExist(input.getAbsolutePath())){
                                                    presenter.mListShare.add(output);
                                                    ExportFiles exportFiles = new ExportFiles(input,output,i,false,index.formatType);
                                                    mListExporting.add(exportFiles);
                                                }
                                                break;
                                            }
                                        }
                                    }
                                }
                                onStartProgressing();
                                ServiceManager.getInstance().setmListExport(mListExporting);
                                ServiceManager.getInstance().onExportingFiles();
                                break;
                            }
                            case EXPORT:{
                                EventBus.getDefault().post(EnumStatus.START_PROGRESS);
                                presenter.mListShare.clear();

                                for (int i = 0;i< presenter.mList.size();i++){
                                    Items index = presenter.mList.get(i);
                                    if (index.isChecked){
                                        EnumFormatType formatType = EnumFormatType.values()[index.formatType];
                                        switch (formatType){
                                            case AUDIO:{
                                                File input = new File(index.originalPath);
                                                Utils.Log(TAG,"Name :"+index.originalName);
                                                File output = new File(SuperSafeApplication.getInstance().getSupersafePicture() +index.title);
                                                if (storage.isFileExist(output.getAbsolutePath())){
                                                    output = new File(SuperSafeApplication.getInstance().getSupersafePicture()+index.originalName+"(1)" +index.fileExtension);
                                                }
                                                if (storage.isFileExist(input.getAbsolutePath())){
                                                    presenter.mListShare.add(output);
                                                    ExportFiles exportFiles = new ExportFiles(input,output,i,false,index.formatType);
                                                    mListExporting.add(exportFiles);
                                                }
                                                break;
                                            }
                                            case FILES:{
                                                File input = new File(index.originalPath);
                                                Utils.Log(TAG,"Name :"+index.originalName);
                                                File output = new File(SuperSafeApplication.getInstance().getSupersafePicture() +index.title);
                                                if (storage.isFileExist(output.getAbsolutePath())){
                                                    output = new File(SuperSafeApplication.getInstance().getSupersafePicture()+index.originalName+"(1)" +index.fileExtension);
                                                }
                                                if (storage.isFileExist(input.getAbsolutePath())){
                                                    presenter.mListShare.add(output);
                                                    ExportFiles exportFiles = new ExportFiles(input,output,i,false,index.formatType);
                                                    mListExporting.add(exportFiles);
                                                }
                                                break;
                                            }
                                            case VIDEO:{
                                                File input = new File(index.originalPath);
                                                File output = new File(SuperSafeApplication.getInstance().getSupersafePicture()+index.title);
                                                if (storage.isFileExist(output.getAbsolutePath())){
                                                    output = new File(SuperSafeApplication.getInstance().getSupersafePicture()+index.originalName+"(1)" +index.fileExtension);
                                                }
                                                if (storage.isFileExist(input.getAbsolutePath())){
                                                    presenter.mListShare.add(output);
                                                    ExportFiles exportFiles = new ExportFiles(input,output,i,false,index.formatType);
                                                    mListExporting.add(exportFiles);
                                                }
                                                break;
                                            }
                                            default:{
                                                File input = new File(index.originalPath);
                                                File output = new File(SuperSafeApplication.getInstance().getSupersafePicture()+index.title);
                                                if (storage.isFileExist(output.getAbsolutePath())){
                                                    output = new File(SuperSafeApplication.getInstance().getSupersafePicture()+index.originalName+"(1)" +index.fileExtension);
                                                }
                                                if (storage.isFileExist(input.getAbsolutePath())){
                                                    presenter.mListShare.add(output);
                                                    ExportFiles exportFiles = new ExportFiles(input,output,i,false,index.formatType);
                                                    mListExporting.add(exportFiles);
                                                }
                                                Utils.Log(TAG,"Exporting file "+ input.getAbsolutePath());
                                                break;
                                            }
                                        }
                                    }
                                }
                                onStartProgressing();
                                ServiceManager.getInstance().setmListExport(mListExporting);
                                ServiceManager.getInstance().onExportingFiles();
                                break;
                            }
                            case DELETE:{
                                presenter.onDelete();
                                break;
                            }
                        }
                    }
                });
        builder.show();
    }

    public void onDialogDownloadFile(){
        mDialogProgress = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
                .setTitleText(getString(R.string.downloading));
        mDialogProgress.show();
        mDialogProgress.setCancelable(false);
    }

    /*Download file*/
    public void onEnableSyncData(){
        final User mUser = User.getInstance().getUserInfo();
        if (mUser!=null){
            if (mUser.verified){
                if (!mUser.driveConnected){
                    Navigator.onCheckSystem(this,null);
                }
                else{
                    onDialogDownloadFile();
                    ServiceManager.getInstance().setListDownloadFile(presenter.mList);
                    ServiceManager.getInstance().getObservableDownload();
                }
            }
            else{
                Navigator.onVerifyAccount(this);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Utils.Log(TAG, "Selected album :");
        switch (requestCode) {
            case Navigator.CAMERA_ACTION: {
                if (resultCode == Activity.RESULT_OK) {
                    Utils.Log(TAG, "reload data");
                    presenter.getData(EnumStatus.RELOAD);
                } else {
                    Utils.Log(TAG, "Nothing to do on Camera");
                }
                break;
            }
            case Navigator.PHOTO_SLIDE_SHOW: {
                if (resultCode == Activity.RESULT_OK) {
                    Utils.Log(TAG, "reload data");
                    presenter.getData(EnumStatus.RELOAD);
                } else {
                    Utils.Log(TAG, "Nothing to do on Camera");
                }
                break;
            }
            case Navigator.REQUEST_CODE: {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    ArrayList<Image> images = data.getParcelableArrayListExtra(Navigator.INTENT_EXTRA_IMAGES);
                    List<ImportFiles> mListImportFiles = new ArrayList<>();
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
                            if (presenter.mainCategories == null) {
                                Utils.onWriteLog("Main categories is null", EnumStatus.WRITE_FILE);
                                return;
                            }
                            ImportFiles importFiles = new ImportFiles(presenter.mainCategories,mimeTypeFile,path,i,false);
                            mListImportFiles.add(importFiles);
                            isReload = true;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    ServiceManager.getInstance().setmListImport(mListImportFiles);
                    ServiceManager.getInstance().onImportingFiles();
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
                if (actionMode!=null){
                    countSelected = 0;
                    actionMode.finish();
                    llBottom.setVisibility(View.GONE);
                    isReload = true;
                }
                boolean isVertical = PrefsController.getBoolean(getString(R.string.key_vertical_adapter),false);
                if (isVertical){
                    verticalAdapter.setDataSource(presenter.mList);
                }
                else{
                    adapter.setDataSource(presenter.mList);
                }
                break;
            }
            case REFRESH:{
                String photos = String.format(getString(R.string.photos_default),""+presenter.photos);
                tv_Photos.setText(photos);
                String videos = String.format(getString(R.string.videos_default),""+presenter.videos);
                tv_Videos.setText(videos);
                String audios = String.format(getString(R.string.audios_default),""+presenter.audios);
                tv_Audios.setText(audios);
                String others = String.format(getString(R.string.others_default),""+presenter.others);
                tv_Others.setText(others);
                boolean isVertical = PrefsController.getBoolean(getString(R.string.key_vertical_adapter),false);
                if (isVertical){
                    verticalAdapter.getDataSource().clear();
                    verticalAdapter.getDataSource().addAll(presenter.mList);
                }
                else{
                    adapter.getDataSource().clear();
                    adapter.getDataSource().addAll(presenter.mList);
                }
                break;
            }
            case DELETE:{
                SingletonPrivateFragment.getInstance().onUpdateView();
                if (actionMode!=null){
                    actionMode.finish();
                }
                isReload = true;
                break;
            }
        }
    }

    @Override
    public void onSuccessful(String message, EnumStatus status, Integer object) {
        switch (status){
            case DELETE:{
                Utils.Log(TAG,"Position "+ object);
                onUpdateAdapter(EnumStatus.REMOVE_AT_ADAPTER,object);
                break;
            }
        }
    }

    @Override
    public Activity getActivity() {
        return this;
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ThemeApp themeApp = ThemeApp.getInstance().getThemeInfo();
                Window window = getWindow();
                window.setStatusBarColor(ContextCompat.getColor(getContext(), themeApp.getPrimaryDarkColor()));
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
        onUpdateAdapter(EnumStatus.UPDATE_AT_ADAPTER,position);
    }

    private void deselectAll() {
        boolean isExport = false;
        final boolean isSaver = PrefsController.getBoolean(getString(R.string.key_saving_space),false);
        for (int i = 0, l = presenter.mList.size(); i < l; i++) {
            switch (presenter.status){
                case EXPORT:{
                    if (presenter.mList.get(i).isChecked){
                        presenter.mList.get(i).isExport = true;
                        presenter.mList.get(i).isDeleteLocal = true;
                        InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onUpdate(presenter.mList.get(i));
                    }
                    isExport = true;
                    break;
                }
                case CANCEL:{
                    final Items items = presenter.mList.get(i);
                    if (items.isChecked){
                        items.isChecked = false;
                        EnumFormatType formatType = EnumFormatType.values()[items.formatType];
                        switch (formatType) {
                            case IMAGE: {
                                items.isSaver = isSaver;
                                InstanceGenerator.getInstance(this).onUpdate(items);
                                if (isSaver){
                                    storage.deleteFile(items.originalPath);
                                }
                                break;
                            }
                        }
                        onUpdateAdapter(EnumStatus.UPDATE_AT_ADAPTER,i);
                    }
                    break;
                }
                default:{
                    if (presenter.mList.get(i).isChecked){
                        presenter.mList.get(i).isChecked = false;
                        onUpdateAdapter(EnumStatus.UPDATE_AT_ADAPTER,i);
                    }
                    break;
                }
            }
        }
        countSelected = 0;
        onShowUI();
        if (isExport){
           onCheckDelete();
        }
    }

    public void onCheckDelete(){
        final List<Items> mList = presenter.mList;
        for (int i = 0, l = mList.size(); i < l; i++) {
            if (presenter.mList.get(i).isChecked){
                EnumFormatType formatTypeFile = EnumFormatType.values()[mList.get(i).formatType];
                if (formatTypeFile == EnumFormatType.AUDIO && mList.get(i).global_original_id == null) {
                    InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onDelete(mList.get(i));
                } else if (formatTypeFile == EnumFormatType.FILES && mList.get(i).global_original_id == null) {
                    InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onDelete(mList.get(i));
                } else if (mList.get(i).global_original_id == null & mList.get(i).global_thumbnail_id == null) {
                    InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onDelete(mList.get(i));
                } else {
                    mList.get(i).deleteAction = EnumDelete.DELETE_WAITING.ordinal();
                    InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onUpdate(mList.get(i));
                    Utils.Log(TAG, "ServiceManager waiting for delete");
                }
                storage.deleteDirectory(SuperSafeApplication.getInstance().getSupersafePrivate() + mList.get(i).items_id);
                onUpdateAdapter(EnumStatus.REMOVE_AT_ADAPTER,i);
            }
        }
        presenter.getData(EnumStatus.REFRESH);
    }

    public void selectAll(){
        try {
            int countSelect = 0 ;
            for (int i =0;i<presenter.mList.size();i++){
                presenter.mList.get(i).isChecked = isSelectAll;
                if (presenter.mList.get(i).isChecked) {
                    countSelect++;
                }
            }
            countSelected = countSelect;
            onShowUI();
            onUpdateAdapter(EnumStatus.UPDATE_ENTIRE_ADAPTER,0);
            actionMode.setTitle(countSelected + " " + getString(R.string.selected));
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void onShowUI(){
        try{
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (countSelected==0){
                        llBottom.setVisibility(View.GONE);
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
        try {
            Configuration cfg =new Configuration.Builder()
                    .hasCamera(true)
                    .hasShade(true)
                    .hasPreview(true)
                    .setSpaceSize(4)
                    .setPhotoMaxWidth(120)
                    .setLocalCategoriesId(presenter.mainCategories.categories_local_id)
                    .setCheckBoxColor(0xFF3F51B5)
                    .setFakePIN(presenter.mainCategories.isFakePin)
                    .setDialogHeight(Configuration.DIALOG_HALF)
                    .setDialogMode(Configuration.DIALOG_LIST)
                    .setMaximum(9)
                    .setTip(null)
                    .setAblumsTitle(null)
                    .build();
            return cfg;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onMoveAlbumSuccessful() {
    }

    @Override
    public List<Items> getListItems() {
        return presenter.mList;
    }

    public void onUpdateAdapter(EnumStatus status,int position){
        boolean isVertical = PrefsController.getBoolean(getString(R.string.key_vertical_adapter),false);
        switch (status){
            case UPDATE_ENTIRE_ADAPTER:{
                if (isVertical){
                    if (verticalAdapter!=null){
                        if (verticalAdapter.getDataSource()!=null){
                            verticalAdapter.notifyDataSetChanged();
                        }
                    }
                }
                else{
                    if (adapter!=null){
                        if (adapter.getDataSource()!=null){
                            adapter.notifyDataSetChanged();
                        }
                    }
                }
                break;
            }
            case REMOVE_AT_ADAPTER:{
                if (isVertical){
                    if (verticalAdapter!=null){
                        if (verticalAdapter.getDataSource()!=null){
                            if (verticalAdapter.getDataSource().size()>position){
                                verticalAdapter.removeAt(position);
                            }
                        }
                    }
                }
                else {
                    if (adapter!=null){
                        if (adapter.getDataSource()!=null){
                            if (adapter.getDataSource().size()>position){
                                adapter.removeAt(position);
                            }
                        }
                    }
                }
                break;
            }
            case UPDATE_AT_ADAPTER:{
                if (isVertical){
                    if (verticalAdapter!=null){
                        if (verticalAdapter.getDataSource()!=null){
                            if (verticalAdapter.getDataSource().size()>position){
                                verticalAdapter.notifyItemChanged(position);
                            }
                        }
                    }
                }
                else{
                    if (adapter!=null){
                        if (adapter.getDataSource()!=null){
                            if (adapter.getDataSource().size()>position){
                                adapter.notifyItemChanged(position);
                            }
                        }
                    }
                }
                break;
            }
        }
    }
}
