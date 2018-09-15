package co.tpcreative.supersafe.ui.albumdetail;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import com.darsh.multipleimageselect.helpers.Constants;
import com.darsh.multipleimageselect.models.Image;
import com.ftinc.kit.util.SizeUtils;
import com.google.gson.Gson;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.leinardi.android.speeddial.FabWithLabelView;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;
import com.snatik.storage.Storage;
import com.snatik.storage.security.SecurityUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import butterknife.BindView;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.Navigator;
import co.tpcreative.supersafe.common.activity.BaseActivity;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.DriveDescription;
import co.tpcreative.supersafe.model.EnumFileType;
import co.tpcreative.supersafe.model.EnumFormatType;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.Items;
import co.tpcreative.supersafe.model.MainCategories;
import co.tpcreative.supersafe.model.MimeTypeFile;
import co.tpcreative.supersafe.model.room.InstanceGenerator;
import id.zelory.compressor.Compressor;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class AlbumDetailActivity extends BaseActivity implements AlbumDetailView , AlbumDetailAdapter.ItemSelectedListener{
    public static final String EXTRA_NAME = "cheese_name";
    private static final String TAG = AlbumDetailActivity.class.getSimpleName();
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.speedDial)
    SpeedDialView mSpeedDialView;
    @BindView(R.id.main_content)
    CoordinatorLayout main_content;
    private AlbumDetailPresenter presenter;
    private AlbumDetailAdapter adapter;
    private SlidrConfig mConfig;
    private Disposable subscriptions;
    private Storage storage;
    private Storage mStorage;
    private Cipher mCipher;
    private Cipher mCiphers;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_detail);
        initRecycleView(getLayoutInflater());
        initSpeedDial(true);

        //android O fix bug orientation
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
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

        presenter = new AlbumDetailPresenter();
        presenter.bindView(this);
        presenter.getData();

        Intent intent = getIntent();
        final String cheeseName = intent.getStringExtra(EXTRA_NAME);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(cheeseName);
        Utils.Log(TAG,Utils.getCurrentDateTime());

        storage = new Storage(this);

        storage.setEncryptConfiguration(SuperSafeApplication.getInstance().getConfigurationFile());
        mCipher = storage.getCipher(Cipher.ENCRYPT_MODE);


        mStorage = new Storage(this);
        mStorage.setEncryptConfiguration(SuperSafeApplication.getInstance().getConfigurationFile());
        mCiphers = mStorage.getCipher(Cipher.ENCRYPT_MODE);
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

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onReloadData() {
        adapter.setDataSource(presenter.mList);
    }

    /*Init Floating View*/

    private void initSpeedDial(boolean addActionItems) {
        if (addActionItems) {
            Drawable drawable = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.baseline_photo_camera_white_24);
            FabWithLabelView fabWithLabelView = mSpeedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id
                    .fab_camera, drawable)
                    .setFabImageTintColor(ResourcesCompat.getColor(getResources(), R.color.inbox_primary,getTheme()))
                    .setLabelColor(Color.WHITE)
                    .setLabel(getString(R.string.camera))
                    .setLabelBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.inbox_primary,
                            getTheme()))
                    .create());
            if (fabWithLabelView != null) {
                fabWithLabelView.setSpeedDialActionItem(fabWithLabelView.getSpeedDialActionItemBuilder()
                        .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.material_white_1000,
                                getTheme()))
                        .create());
            }

            drawable = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.baseline_photo_white_24);
            mSpeedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id.fab_photo, drawable)
                    .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.material_green_500,
                            getTheme()))
                    .setLabel(R.string.photo)
                    .setLabelColor(getResources().getColor(R.color.white))
                    .setLabelBackgroundColor(getResources().getColor(R.color.colorBlue))
                    .create());

            mSpeedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id.fab_album, R.drawable
                    .baseline_add_to_photos_white_36)
                    .setLabel(getString(R.string.album))
                    .setTheme(R.style.AppTheme_Purple)
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
                        return  false;
                }
                return true; // To keep the Speed Dial open
            }
        });
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
                            Navigator.onMoveCamera(AlbumDetailActivity.this);
                        }
                        else{
                            Log.d(TAG,"Permission is denied");
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


    public void initRecycleView(LayoutInflater layoutInflater){
        adapter = new AlbumDetailAdapter(layoutInflater,getApplicationContext(),this);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getApplicationContext(), 3);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(3, dpToPx(0), false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onClickItem(int position) {
        Utils.Log(TAG,"Position : "+ position);
        try {
            Navigator.onPhotoSlider(this,presenter.mList.get(position),presenter.mList);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onAddToFavoriteSelected(int position) {

    }

    @Override
    public void onPlayNextSelected(int position) {

    }

    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG,"Selected album :");


        switch (requestCode){
            case Navigator.CAMERA_ACTION :{
                if (resultCode == Activity.RESULT_OK) {
                    Utils.Log(TAG,"reload data");
                    presenter.getData();
                }
                else{
                    Utils.Log(TAG,"Nothing to do on Camera");
                }
                break;
            }
            case Navigator.PHOTO_SLIDE_SHOW:{
                if (resultCode == Activity.RESULT_OK) {
                    Utils.Log(TAG,"reload data");
                    presenter.getData();
                }
                else{
                    Utils.Log(TAG,"Nothing to do on Camera");
                }
                break;
            }
            case Constants.REQUEST_CODE :{
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
                        Log.d(TAG,"file extension "+ Utils.getFileExtension(path));

                        try {
                            final MimeTypeFile mimeTypeFile = Utils.mediaTypeSupport().get(fileExtension);
                            mimeTypeFile.name = name;
                            onUploadFileRXJava(mimeTypeFile, path,id);
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                }
                else {
                    Utils.Log(TAG,"Nothing to do on Gallery");
                }
                break;
            }
            default:{
                Utils.Log(TAG,"Nothing to do");
                break;
            }
        }
    }

    public void onUploadFileRXJava(final MimeTypeFile mimeTypeFile, final String path, String id){

//        final  Storage storage = new Storage(SuperSafeApplication.getInstance());
//        storage.setEncryptConfiguration(storage.getmConfiguration());
//        Cipher mCipher = storage.getCipher(Cipher.ENCRYPT_MODE);
        subscriptions = Observable.create(subscriber -> {



            final MimeTypeFile mMimeTypeFile = mimeTypeFile;
            final EnumFormatType enumTypeFile  = mMimeTypeFile.formatType;
            final String mPath = path;
            final String mMimeType  = mMimeTypeFile.mimeType;
            final String mVideo_id = id;
            final Items items ;

            Utils.Log(TAG,"object "+ new Gson().toJson(mMimeTypeFile));

          // InputStream originalFile = null;
           Bitmap thumbnail = null;
           switch (enumTypeFile){
               case IMAGE: {
                   Utils.Log(TAG, "Start RXJava Image Progressing");
                   try {

                       final int THUMBSIZE_HEIGHT = 600;
                       final int THUMBSIZE_WIDTH = 400;

                       BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
                       bmpFactoryOptions.inJustDecodeBounds = true;
                       Bitmap bitmap = BitmapFactory.decodeFile(mPath, bmpFactoryOptions);
                       int heightRatio = (int) Math.ceil(bmpFactoryOptions.outHeight / (float) THUMBSIZE_HEIGHT);
                       int widthRatio = (int) Math.ceil(bmpFactoryOptions.outWidth / (float) THUMBSIZE_WIDTH);
                       if (heightRatio > 1 || widthRatio > 1) {
                           if (heightRatio > widthRatio) {
                               bmpFactoryOptions.inSampleSize = heightRatio;
                           } else {
                               bmpFactoryOptions.inSampleSize = widthRatio;
                           }
                       }
                       bmpFactoryOptions.inJustDecodeBounds = false;
                       bitmap = BitmapFactory.decodeFile(mPath, bmpFactoryOptions);

                       thumbnail = ThumbnailUtils.extractThumbnail(bitmap,
                               THUMBSIZE_HEIGHT,
                               THUMBSIZE_WIDTH);
                       ExifInterface exifInterface = new ExifInterface(mPath);
                       int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                       Log.d("EXIF", "Exif: " + orientation);
                       Matrix matrix = new Matrix();
                       if (orientation == 6) {
                           matrix.postRotate(90);
                       } else if (orientation == 3) {
                           matrix.postRotate(180);
                       } else if (orientation == 8) {
                           matrix.postRotate(270);
                       }
                       thumbnail = Bitmap.createBitmap(thumbnail, 0, 0, thumbnail.getWidth(), thumbnail.getHeight(), matrix, true); // rotating bitmap


//                       File mFileThumbnail =  new Compressor(AlbumDetailActivity.this)
//                               .setMaxWidth(640)
//                               .setMaxHeight(480)
//                               .setQuality(85)
//                               .setCompressFormat(Bitmap.CompressFormat.JPEG)
//                               .setDestinationDirectoryPath(SuperSafeApplication.getInstance().getPackageFolderPath(AlbumDetailActivity.this).getAbsolutePath())
//                               .compressToFile(new File(mPath),getString(R.string.key_temporary));


                       String rootPath = SuperSafeApplication.getInstance().getSuperSafe();
                       String currentTime = Utils.getCurrentDateTime();
                       String uuId = Utils.getUUId();
                       String pathContent = rootPath + uuId + "/";
                       storage.createDirectory(pathContent);
                       String thumbnailPath = pathContent + "thumbnail_" + currentTime;
                       String originalPath = pathContent + currentTime;


                       DriveDescription description = new DriveDescription();
                       description.fileExtension = mMimeTypeFile.extension;

                       description.originalPath = originalPath;
                       description.thumbnailPath = thumbnailPath;
                       description.subFolderName = uuId;
                       description.localCategories_Id = MainCategories.getInstance().intent_localCategoriesId;
                       description.nameMainCategories = MainCategories.getInstance().intent_name;
                       description.local_id = uuId;
                       description.global_original_id = null;
                       description.mimeType = mMimeType;
                       description.thumbnailName = currentTime;
                       description.globalName = uuId;
                       description.formatType = EnumFormatType.IMAGE.ordinal();
                       description.degrees = 0;
                       description.thumbnailSync = false;
                       description.originalSync = false;
                       description.global_thumbnail_id = null;
                       description.fileType = EnumFileType.NONE.ordinal();
                       description.originalName = currentTime;
                       description.title = mMimeTypeFile.name;
                       description.thumbnailName = "thumbnail_" + currentTime;

                       items = new Items(false,
                               description.originalSync,
                               description.thumbnailSync,
                               description.degrees,
                               description.fileType,
                               description.formatType,
                               description.title,
                               description.originalName,
                               description.thumbnailName,
                               description.globalName,
                               description.originalPath,
                               description.thumbnailPath,
                               description.local_id,
                               description.global_original_id,
                               description.global_thumbnail_id,
                               description.localCategories_Id,
                               description.mimeType,
                               description.fileExtension,
                               new Gson().toJson(description),
                               EnumStatus.UPLOAD);

                       Utils.Log(TAG, "start compress");
                       boolean createdThumbnail = storage.createFile(thumbnailPath, thumbnail);
                       boolean createdOriginal = storage.createFile(new File(originalPath), new File(mPath), Cipher.ENCRYPT_MODE);
                       Utils.Log(TAG, "start end");


                       if (createdThumbnail && createdOriginal) {
                           subscriber.onNext(items);
                           subscriber.onComplete();
                           Utils.Log(TAG, "CreatedFile successful");
                       } else {
                           subscriber.onNext(null);
                           subscriber.onComplete();
                           Utils.Log(TAG, "CreatedFile failed");
                       }

                   } catch (Exception e) {
                       Log.w(TAG, "Cannot write to " + e);
                       subscriber.onNext(false);
                       subscriber.onComplete();
                   } finally {
                       Utils.Log(TAG, "Finally");
                   }
                   break;
               }

               case VIDEO: {
                   Utils.Log(TAG, "Start RXJava Video Progressing");
                   try {
                       BitmapFactory.Options options = new BitmapFactory.Options();
                       options.inDither = false;
                       options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                       thumbnail = MediaStore.Video.Thumbnails.getThumbnail(getContentResolver(),
                               Long.parseLong(mVideo_id),
                               MediaStore.Images.Thumbnails.MINI_KIND,
                               options);

                       String rootPath = SuperSafeApplication.getInstance().getSuperSafe();
                       String currentTime = Utils.getCurrentDateTime();
                       String uuId = Utils.getUUId();
                       String pathContent = rootPath + uuId + "/";
                       storage.createDirectory(pathContent);
                       String thumbnailPath = pathContent + "thumbnail_" + currentTime;
                       String originalPath = pathContent + currentTime;


                       DriveDescription description = new DriveDescription();
                       description.fileExtension = mMimeTypeFile.extension;
                       description.originalPath = originalPath;
                       description.thumbnailPath = thumbnailPath;
                       description.subFolderName = uuId;
                       description.localCategories_Id = MainCategories.getInstance().intent_localCategoriesId;
                       description.nameMainCategories = MainCategories.getInstance().intent_name;
                       description.local_id = uuId;
                       description.global_original_id = null;
                       description.mimeType = mMimeType;
                       description.globalName = uuId;
                       description.formatType = EnumFormatType.VIDEO.ordinal();
                       description.degrees = 0;
                       description.thumbnailSync = false;
                       description.originalSync = false;
                       description.global_thumbnail_id = null;
                       description.fileType = EnumFileType.NONE.ordinal();
                       description.originalName = currentTime;
                       description.title = mMimeTypeFile.name;
                       description.thumbnailName = "thumbnail_" + currentTime;


                       items = new Items(false,
                               description.originalSync,
                               description.thumbnailSync,
                               description.degrees,
                               description.fileType,
                               description.formatType,
                               description.title,
                               description.originalName,
                               description.thumbnailName,
                               description.globalName,
                               description.originalPath,
                               description.thumbnailPath,
                               description.local_id,
                               description.global_original_id,
                               description.global_thumbnail_id,
                               description.localCategories_Id,
                               description.mimeType,
                               description.fileExtension,
                               new Gson().toJson(description),
                               EnumStatus.UPLOAD);


                       boolean createdThumbnail = storage.createFile(thumbnailPath, thumbnail);
                       mCiphers = mStorage.getCipher(Cipher.ENCRYPT_MODE);
                       boolean createdOriginal = mStorage.createLargeFile(new File(originalPath), new File(mPath), mCiphers);

                       if (createdThumbnail && createdOriginal) {
                           subscriber.onNext(items);
                           subscriber.onComplete();
                           Utils.Log(TAG, "CreatedFile successful");
                       } else {
                           subscriber.onNext(null);
                           subscriber.onComplete();
                           Utils.Log(TAG, "CreatedFile failed");
                       }

                   } catch (Exception e) {
                       Log.w(TAG, "Cannot write to " + e);
                       subscriber.onNext(null);
                       subscriber.onComplete();
                   } finally {
                       Utils.Log(TAG, "Finally");
                   }
                   break;
               }


               case AUDIO: {

                   Utils.Log(TAG, "Start RXJava Video Progressing");
                   try {

                       String rootPath = SuperSafeApplication.getInstance().getSuperSafe();
                       String currentTime = Utils.getCurrentDateTime();
                       String uuId = Utils.getUUId();
                       String pathContent = rootPath + uuId + "/";
                       storage.createDirectory(pathContent);
                       String originalPath = pathContent + currentTime;


                       DriveDescription description = new DriveDescription();
                       description.fileExtension = mMimeTypeFile.extension;
                       description.originalPath = originalPath;
                       description.thumbnailPath = null;
                       description.subFolderName = uuId;
                       description.localCategories_Id = MainCategories.getInstance().intent_localCategoriesId;
                       description.nameMainCategories = MainCategories.getInstance().intent_name;
                       description.local_id = uuId;
                       description.mimeType = mMimeType;
                       description.globalName = uuId;
                       description.formatType = EnumFormatType.AUDIO.ordinal();
                       description.degrees = 0;
                       description.thumbnailSync = true;
                       description.originalSync = false;
                       description.global_original_id = null;
                       description.global_thumbnail_id = null;
                       description.fileType = EnumFileType.NONE.ordinal();
                       description.originalName = currentTime;
                       description.title = mMimeTypeFile.name;
                       description.thumbnailName = null;

                       items = new Items(false,
                               description.originalSync,
                               description.thumbnailSync,
                               description.degrees,
                               description.fileType,
                               description.formatType,
                               description.title,
                               description.originalName,
                               description.thumbnailName,
                               description.globalName,
                               description.originalPath,
                               description.thumbnailPath,
                               description.local_id,
                               description.global_original_id,
                               description.global_thumbnail_id,
                               description.localCategories_Id,
                               description.mimeType,
                               description.fileExtension,
                               new Gson().toJson(description),
                               EnumStatus.UPLOAD);

                       mCiphers = mStorage.getCipher(Cipher.ENCRYPT_MODE);
                       boolean createdOriginal = mStorage.createLargeFile(new File(originalPath), new File(mPath), mCiphers);

                       if (createdOriginal) {
                           subscriber.onNext(items);
                           subscriber.onComplete();
                           Utils.Log(TAG, "CreatedFile successful");
                       } else {
                           subscriber.onNext(null);
                           subscriber.onComplete();
                           Utils.Log(TAG, "CreatedFile failed");
                       }

                   } catch (Exception e) {
                       Log.w(TAG, "Cannot write to " + e);
                       subscriber.onNext(null);
                       subscriber.onComplete();
                   } finally {
                       Utils.Log(TAG, "Finally");
                   }
                   break;
               }
           }
            Utils.Log(TAG,"End up RXJava");
        })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .subscribe(response -> {
                    final Items items = (Items) response;
                    if (items!=null){
                        InstanceGenerator.getInstance(AlbumDetailActivity.this).onInsert(items);
                        Utils.Log(TAG,"Write file successful ");
                    }else{
                        Utils.Log(TAG,"Write file Failed ");
                    }
                    this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            presenter.getData();
                        }
                    });
                    Utils.Log(TAG,new Gson().toJson(items));
                });
    }


    public Bitmap onThumbnailVideo(String path){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Cursor ca = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[] { MediaStore.MediaColumns._ID }, MediaStore.MediaColumns.DATA + "=?", new String[] {path}, null);
        if (ca != null && ca.moveToFirst()) {
            int id = ca.getInt(ca.getColumnIndex(MediaStore.MediaColumns._ID));
            ca.close();
            Bitmap bitmap = MediaStore.Video.Thumbnails.getThumbnail(getContentResolver(), id, MediaStore.Images.Thumbnails.MINI_KIND, options );
            return bitmap;
        }
        ca.close();
        return null;
    }

    public Bitmap onThumbnailVideos(String video_id){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmapThumb = MediaStore.Video.Thumbnails.getThumbnail(getContentResolver(),
                Long.parseLong(video_id),
                MediaStore.Images.Thumbnails.MINI_KIND,
                options);
        return bitmapThumb;
    }

    /**
     * Converting dp to pixel
     */

    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (subscriptions!=null){
           // subscriptions.dispose();
        }

        try {
            storage.deleteFile(Utils.getPackagePath(getApplicationContext()).getAbsolutePath());
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
