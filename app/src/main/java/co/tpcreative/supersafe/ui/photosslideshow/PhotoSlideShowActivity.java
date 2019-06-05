package co.tpcreative.supersafe.ui.photosslideshow;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.gson.Gson;
import com.snatik.storage.Storage;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.Cipher;
import butterknife.BindView;
import cn.pedant.SweetAlert.SweetAlertDialog;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.activity.BaseGalleryActivity;
import co.tpcreative.supersafe.common.controller.GalleryCameraMediaManager;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.controller.ServiceManager;
import co.tpcreative.supersafe.common.controller.SingletonFakePinComponent;
import co.tpcreative.supersafe.common.controller.SingletonPrivateFragment;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Configuration;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumDelete;
import co.tpcreative.supersafe.model.EnumFormatType;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.ExportFiles;
import co.tpcreative.supersafe.model.Items;
import co.tpcreative.supersafe.model.User;
import co.tpcreative.supersafe.model.room.InstanceGenerator;
import dmax.dialog.SpotsDialog;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class PhotoSlideShowActivity extends BaseGalleryActivity implements View.OnClickListener ,BaseView,GalleryCameraMediaManager.AlbumDetailManagerListener{

    private static final String TAG = PhotoSlideShowActivity.class.getSimpleName();
    private RequestOptions options = new RequestOptions()
            .centerInside()
            .placeholder(R.color.black38)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .error(R.drawable.baseline_music_note_white_48)
            .priority(Priority.HIGH);

    @BindView(R.id.rlTop)
    RelativeLayout rlTop;
    @BindView(R.id.llBottom)
    LinearLayout llBottom;
    @BindView(R.id.imgArrowBack)
    ImageView imgArrowBack;
    @BindView(R.id.imgOverflow)
    ImageView imgOverflow;
    @BindView(R.id.imgShare)
    ImageView imgShare;
    @BindView(R.id.imgExport)
    ImageView imgExport;
    @BindView(R.id.imgMove)
    ImageView imgMove;
    @BindView(R.id.imgRotate)
    ImageView imgRotate;
    @BindView(R.id.imgDelete)
    ImageView imgDelete;
    private boolean isHide;
    private PhotoSlideShowPresenter presenter;
    private Storage storage;
    private ViewPager viewPager;
    private SamplePagerAdapter adapter;
    private boolean isReload;
    private AlertDialog dialog;
    private Disposable subscriptions;
    private boolean isProgressing;
    private  int position = 0;
    private  PhotoView photoView;
    SweetAlertDialog mDialogProgress;
    private Handler handler;
    private int delay = 2000; //milliseconds
    private int page = 0;
    Runnable runnable = new Runnable() {
        public void run() {
            if (adapter.getCount() == page) {
                page = 0;
            } else {
                page++;
            }
            viewPager.setCurrentItem(page, true);
            handler.postDelayed(this, delay);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photos_slideshow);
        storage = new Storage(this);
        storage.setEncryptConfiguration(SuperSafeApplication.getInstance().getConfigurationFile());
        presenter = new PhotoSlideShowPresenter();
        presenter.bindView(this);
        presenter.getIntent(this);
        viewPager = findViewById(R.id.view_pager);
        adapter = new SamplePagerAdapter(this);
        viewPager.setAdapter(adapter);
        imgArrowBack.setOnClickListener(this);
        imgOverflow.setOnClickListener(this);
        imgDelete.setOnClickListener(this);
        imgExport.setOnClickListener(this);
        imgRotate.setOnClickListener(this);
        imgShare.setOnClickListener(this);
        imgMove.setOnClickListener(this);
        GalleryCameraMediaManager.getInstance().setListener(this);
        attachFragment(R.id.gallery_root);
        /*Auto slide*/
        handler = new Handler();
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
            @Override
            public void onPageSelected(int position) {
                page = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    public void onStartSlider(){
        try {
            handler.postDelayed(runnable, delay);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void onStopSlider(){
        try{
            handler.removeCallbacks(runnable);
        }
        catch (Exception e){
            e.printStackTrace();
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utils.Log(TAG,"OnDestroy");
        EventBus.getDefault().unregister(this);
        presenter.unbindView();
        onStopSlider();
        Utils.Log(TAG,"Destroy");
        if (subscriptions!=null){
            subscriptions.dispose();
        }
        try {
            storage.deleteFile(Utils.getPackagePath(getApplicationContext()).getAbsolutePath());
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onOrientationChange(boolean isFaceDown) {
        onFaceDown(isFaceDown);
    }

    /*BaseGallery*/

    @Override
    public Configuration getConfiguration() {
        //default configuration
        try {
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
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Items> getListItems() {
        try {
            final List<Items> list = new ArrayList<>();
            final Items item = presenter.mList.get(position);
            if (item!=null){
                item.isChecked = true;
                list.add(item);
                return list;
            }
            onBackPressed();
            return null;
        }
        catch (Exception e){
            onBackPressed();
        }
        return null;
    }

    class SamplePagerAdapter extends PagerAdapter {
        private Context context;
        SamplePagerAdapter(Context context){
            this.context = context;
        }
        @Override
        public int getCount() {
            return presenter.mList.size();
        }

        @Override
        public View instantiateItem(ViewGroup container, int position) {
            //PhotoView photoView = new PhotoView(container.getContext());
            LayoutInflater inflater = getLayoutInflater();
            View myView = inflater.inflate(R.layout.content_view, null);
            photoView = myView.findViewById(R.id.imgPhoto);
            ImageView imgPlayer = myView.findViewById(R.id.imgPlayer);
            final Items mItems = presenter.mList.get(position);
            EnumFormatType enumTypeFile = EnumFormatType.values()[mItems.formatType];
            photoView.setOnPhotoTapListener(new OnPhotoTapListener() {
                @Override
                public void onPhotoTap(ImageView view, float x, float y) {
                    Utils.Log(TAG,"on Clicked");
                    onStopSlider();
                    isHide = !isHide;
                    onHideView();
                }
            });
            imgPlayer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Items items = presenter.mList.get(viewPager.getCurrentItem());
                    Navigator.onPlayer(PhotoSlideShowActivity.this,items,presenter.mainCategories);
                }
            });
            try{
                String path = mItems.thumbnailPath;
                File file = new File(""+path);
                if (file.exists() || file.isFile()){
                    photoView.setRotation(mItems.degrees);
                    Glide.with(context)
                            .load(storage.readFile(path))
                            .apply(options)
                            .into(photoView);
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
            switch (enumTypeFile){
                case VIDEO:{
                    imgPlayer.setVisibility(View.VISIBLE);
                    break;
                }
                case AUDIO:{
                    imgPlayer.setVisibility(View.VISIBLE);
                    break;
                }
                case FILES:{
                    imgPlayer.setVisibility(View.INVISIBLE);
                    break;
                }
                default:{
                    imgPlayer.setVisibility(View.INVISIBLE);
                    break;
                }
            }
            container.addView(myView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            photoView.setTag("myview" + position);
            return myView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return PagerAdapter.POSITION_NONE;
        }
    }

    public void onHideView(){
        if (isHide){
            Utils.slideToTopHeader(rlTop);
            Utils.slideToBottomFooter(llBottom);
        }
        else {
            Utils.slideToBottomHeader(rlTop);
            Utils.slideToTopFooter(llBottom);
        }
    }

    @Override
    public void onClick(View view) {
        position = viewPager.getCurrentItem();
        switch (view.getId()){
            case R.id.imgArrowBack :{
                if (isHide){
                    break;
                }
                onBackPressed();
                break;
            }
            case R.id.imgOverflow :{
                openOptionMenu(view);
                break;
            }
            case R.id.imgShare :{
                if (isHide){
                    break;
                }
                try {
                    if (presenter.mList!=null){
                        if (presenter.mList.size()>0){
                            storage.createDirectory(SuperSafeApplication.getInstance().getSupersafeShare());
                            presenter.status = EnumStatus.SHARE;
                            onShowDialog(EnumStatus.SHARE,position);
                        }
                        else{
                            onBackPressed();
                        }
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                break;
            }
            case R.id.imgDelete :{
                if (isHide){
                    break;
                }
                try {
                    if (presenter.mList!=null){
                        if (presenter.mList.size()>0){
                            presenter.status = EnumStatus.DELETE;
                            onShowDialog(EnumStatus.DELETE,position);
                        }
                        else{
                            onBackPressed();
                        }
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                break;
            }
            case R.id.imgExport :{
                if (isHide){
                    break;
                }
                Utils.Log(TAG,"Action here");
                try {
                    if (presenter.mList!=null){
                        if (presenter.mList.size()>0){
                            storage.createDirectory(SuperSafeApplication.getInstance().getSupersafePicture());
                            presenter.status = EnumStatus.EXPORT;
                            if (presenter.mList.get(position).isSaver){
                                onEnableSyncData(position);
                            }
                            else{
                                onShowDialog(EnumStatus.EXPORT,position);
                            }
                        }
                        else{
                            onBackPressed();
                        }
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                break;
            }
            case R.id.imgRotate : {
                if (isHide){
                    break;
                }
                try {
                    if (isProgressing){
                        return;
                    }
                    final Items items = InstanceGenerator.getInstance(this).getItemId(presenter.mList.get(viewPager.getCurrentItem()).items_id,presenter.mList.get(viewPager.getCurrentItem()).isFakePin);
                    EnumFormatType formatTypeFile = EnumFormatType.values()[items.formatType];
                    if (formatTypeFile!=EnumFormatType.AUDIO && formatTypeFile !=EnumFormatType.FILES ){
                        if (items!=null) {
                            onRotateBitmap(items);
                            isReload = true;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
            case R.id.imgMove :{
                presenter.status = EnumStatus.MOVE;
                openAlbum();
                break;
            }

        }
    }

    /*Download file*/
    public void onEnableSyncData(int position){
        final User mUser = User.getInstance().getUserInfo();
        if (mUser!=null){
            if (mUser.verified){
                if (!mUser.driveConnected){
                    Navigator.onCheckSystem(this,null);
                }
                else{
                    onDialogDownloadFile();
                    final List<Items> list = new ArrayList<>();
                    final Items items = presenter.mList.get(position);
                    items.isChecked = true;
                    list.add(items);
                    ServiceManager.getInstance().setListDownloadFile(list);
                    ServiceManager.getInstance().getObservableDownload(true);
                }
            }
            else{
                Navigator.onVerifyAccount(this);
            }
        }
    }

    public void onDialogDownloadFile(){
        mDialogProgress = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
                .setTitleText(getString(R.string.downloading));
        mDialogProgress.show();
        mDialogProgress.setCancelable(false);
    }
    
    @Override
    public void onMoveAlbumSuccessful() {
        try {
            isReload  = true;
            presenter.mList.remove(position);
            adapter.notifyDataSetChanged();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void onShowDialog(EnumStatus status, int position){
        String content = "";
        switch (status){
            case EXPORT:{
                content = String.format(getString(R.string.export_items),"1");
                break;
            }
            case SHARE:{
                content = String.format(getString(R.string.share_items),"1");
                break;
            }
            case DELETE:{
                content = String.format(getString(R.string.move_items_to_trash),"1");
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
                        final Items items = presenter.mList.get(position);
                        final boolean isSaver = PrefsController.getBoolean(getString(R.string.key_saving_space),false);
                        EnumFormatType formatType = EnumFormatType.values()[items.formatType];
                        switch (formatType) {
                            case IMAGE: {
                                items.isSaver = isSaver;
                                InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onUpdate(items);
                                if (isSaver){
                                    storage.deleteFile(items.originalPath);
                                }
                                break;
                            }
                        }
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        List<ExportFiles> mListExporting = new ArrayList<>();
                        switch (status){
                            case SHARE:{
                                GalleryCameraMediaManager.getInstance().onStartProgress();
                                presenter.mListShare.clear();
                                final Items index = presenter.mList.get(position);
                                    if (index!=null){
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
                                                    ExportFiles exportFiles = new ExportFiles(input,output,0,false,index.formatType);
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
                                                    ExportFiles exportFiles = new ExportFiles(input,output,0,false,index.formatType);
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
                                                    ExportFiles exportFiles = new ExportFiles(input,output,0,false,index.formatType);
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
                                                    ExportFiles exportFiles = new ExportFiles(input,output,0,false,index.formatType);
                                                    mListExporting.add(exportFiles);
                                                }
                                                break;
                                            }
                                        }

                                }
                                onStartProgressing();
                                ServiceManager.getInstance().setmListExport(mListExporting);
                                ServiceManager.getInstance().onExportingFiles();
                                break;
                            }
                            case EXPORT:{
                                GalleryCameraMediaManager.getInstance().onStartProgress();
                                presenter.mListShare.clear();
                                final Items index = presenter.mList.get(position);
                                if (index!=null){
                                        EnumFormatType formatType = EnumFormatType.values()[index.formatType];
                                        switch (formatType){
                                            case AUDIO:{
                                                File input = new File(index.originalPath);
                                                File output = new File(SuperSafeApplication.getInstance().getSupersafePicture() +index.title);
                                                if (storage.isFileExist(output.getAbsolutePath())){
                                                    output = new File(SuperSafeApplication.getInstance().getSupersafePicture()+index.originalName+"(1)" +index.fileExtension);
                                                }
                                                if (storage.isFileExist(input.getAbsolutePath())){
                                                    presenter.mListShare.add(output);
                                                    ExportFiles exportFiles = new ExportFiles(input,output,0,false,index.formatType);
                                                    mListExporting.add(exportFiles);
                                                }
                                                break;
                                            }
                                            case FILES:{
                                                File input = new File(index.originalPath);
                                                File output = new File(SuperSafeApplication.getInstance().getSupersafePicture() +index.title);
                                                if (storage.isFileExist(output.getAbsolutePath())){
                                                    output = new File(SuperSafeApplication.getInstance().getSupersafePicture()+index.originalName+"(1)" +index.fileExtension);
                                                }
                                                if (storage.isFileExist(input.getAbsolutePath())){
                                                    presenter.mListShare.add(output);
                                                    ExportFiles exportFiles = new ExportFiles(input,output,0,false,index.formatType);
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
                                                    ExportFiles exportFiles = new ExportFiles(input,output,0,false,index.formatType);
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
                                                    ExportFiles exportFiles = new ExportFiles(input,output,0,false,index.formatType);
                                                    mListExporting.add(exportFiles);
                                                }
                                                break;
                                            }
                                        }
                                    }
                                    onStartProgressing();
                                    ServiceManager.getInstance().setmListExport(mListExporting);
                                    ServiceManager.getInstance().onExportingFiles();
                                break;
                            }
                            case DELETE:{
                                presenter.onDelete(position);
                                isReload = true;
                                break;
                            }
                        }
                    }
                });
        builder.show();
    }

    /*Gallery interface*/

    @Override
    public void onUpdatedView() {

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
                                Toast.makeText(PhotoSlideShowActivity.this,"Exported at "+SuperSafeApplication.getInstance().getSupersafePicture(),Toast.LENGTH_LONG).show();
                            }
                        });
                        break;
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
                                .setContext(PhotoSlideShowActivity.this)
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
                        deselectAll();
                        Utils.Log(TAG,"Action 1");
                    }
                }
            });
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
    }

    private void deselectAll() {
        switch (presenter.status){
            case EXPORT:{
                Utils.Log(TAG,"Action 2");
                presenter.mList.get(position).isExport = true;
                presenter.mList.get(position).isDeleteLocal = true;
                InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onUpdate(presenter.mList.get(position));
                onCheckDelete();
                break;
            }
        }
    }

    public void onCheckDelete(){
        final List<Items> mList = presenter.mList;
        Utils.Log(TAG,"Action 3");
        EnumFormatType formatTypeFile = EnumFormatType.values()[mList.get(position).formatType];
        if (formatTypeFile == EnumFormatType.AUDIO && mList.get(position).global_original_id == null) {
            InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onDelete(mList.get(position));
        } else if (formatTypeFile == EnumFormatType.FILES && mList.get(position).global_original_id == null) {
            InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onDelete(mList.get(position));
        } else if (mList.get(position).global_original_id == null & mList.get(position).global_thumbnail_id == null) {
            InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onDelete(mList.get(position));
        } else {
            mList.get(position).deleteAction = EnumDelete.DELETE_WAITING.ordinal();
            InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onUpdate(mList.get(position));
            Utils.Log(TAG, "ServiceManager waiting for delete");
        }
        storage.deleteDirectory(SuperSafeApplication.getInstance().getSupersafePrivate() + mList.get(position).items_id);
        presenter.onDelete(position);
        isReload = true;
        Utils.Log(TAG,"Action 4");
    }

    @SuppressLint("RestrictedApi")
    public void openOptionMenu(View v){
        onStopSlider();
        PopupMenu popup = new PopupMenu(this, v);
        popup.getMenuInflater().inflate(R.menu.menu_slideshow, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.action_slideshow:{
                        Utils.Log(TAG,"Slide show images");
                        onStartSlider();
                        isHide = true;
                        onHideView();
                        return true;
                    }
                }
                return true;
            }
        });
        popup.show();
    }

    @Override
    public void onCompletedDownload(EnumStatus status) {
        switch (status){
            case DONE:{
                mDialogProgress.setTitleText("Success!")
                        .setConfirmText("OK")
                        .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                mDialogProgress.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismiss();
                        onShowDialog(EnumStatus.EXPORT,position);
                    }
                });
                Utils.Log(TAG, " already sync");
                break;
            }
            case ERROR:{
                mDialogProgress.setTitleText("No connection, Try again")
                        .setConfirmText("OK")
                        .changeAlertType(SweetAlertDialog.ERROR_TYPE);
                break;
            }
        }
    }


    /*ViewPresenter*/

    @Override
    public void onStartLoading(EnumStatus status) {

    }

    @Override
    public void onStopLoading(EnumStatus status) {

    }

    @Override
    public void onBackPressed() {
        if (isReload){
            SingletonPrivateFragment.getInstance().onUpdateView();
            SingletonFakePinComponent.getInstance().onUpdateView();
            Intent intent = new Intent();
            setResult(RESULT_OK,intent);
        }
        super.onBackPressed();
    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }

    @Override
    protected void onPause() {
        super.onPause();
        onStopSlider();
    }


    public void onRotateBitmap(final Items items){
        subscriptions = Observable.create(subscriber -> {
            isProgressing = true;
            Utils.Log(TAG,"Start Progressing encrypt thumbnail data");
            final  Items mItem = items;
            int mDegrees = mItem.degrees;

            if (mDegrees>=360){
                mDegrees = 90;
            }
            else{
                if (mDegrees>90){
                    mDegrees = mDegrees+90;
                }
                else{
                    mDegrees = 180;
                }
            }
            final  int valueDegrees = mDegrees;
            mItem.degrees = valueDegrees;
            presenter.mList.get(position).degrees = valueDegrees;
                    subscriber.onNext(mItem);
            subscriber.onComplete();
        })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .subscribe(response -> {
                    final Items mItem = (Items) response;
                    if (mItem!=null){
                        InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onUpdate(items);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                isProgressing = false;
                                viewPager.getAdapter().notifyDataSetChanged();
                            }
                        });
                        Utils.Log(TAG,"Thumbnail saved successful");
                    }
                    else{
                        Utils.Log(TAG,"Thumbnail saved failed");
                    }
                });
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
            case DELETE:{
                isReload = true;
                adapter.notifyDataSetChanged();
                if (presenter.mList.size()==0){
                    onBackPressed();
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

}
