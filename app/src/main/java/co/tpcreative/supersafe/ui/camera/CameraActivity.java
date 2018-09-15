package co.tpcreative.supersafe.ui.camera;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import com.google.android.cameraview.CameraView;
import com.google.common.net.MediaType;
import com.google.gson.Gson;
import com.snatik.storage.Storage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.DecimalFormat;

import javax.crypto.Cipher;

import butterknife.BindView;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.activity.BaseActivity;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.DriveDescription;
import co.tpcreative.supersafe.model.EnumFileType;
import co.tpcreative.supersafe.model.EnumFormatType;
import co.tpcreative.supersafe.model.EnumStatus;

import co.tpcreative.supersafe.model.Items;
import co.tpcreative.supersafe.model.MainCategories;
import co.tpcreative.supersafe.model.room.InstanceGenerator;
import id.zelory.compressor.Compressor;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class CameraActivity extends BaseActivity implements
        ActivityCompat.OnRequestPermissionsResultCallback{

    private static final String TAG = CameraActivity.class.getSimpleName();
    private Storage storage;

    private static final int[] FLASH_OPTIONS = {
            CameraView.FLASH_AUTO,
            CameraView.FLASH_OFF,
            CameraView.FLASH_ON,
    };

    private static final int[] FLASH_ICONS = {
            R.drawable.ic_flash_auto,
            R.drawable.ic_flash_off,
            R.drawable.ic_flash_on,
    };


    private int mCurrentFlash;
    private int mOrientation = 0;
    private boolean isProgress;
    private boolean isReload;

    @BindView(R.id.camera)
    CameraView mCameraView;
    @BindView(R.id.btnSwitch)
    ImageButton btnSwitch;
    @BindView(R.id.btnDone)
    Button btnDone;
    @BindView(R.id.btnFlash)
    ImageButton btnFlash;
    @BindView(R.id.take_picture)
    FloatingActionButton take_picture;
    private Cipher mCipher;
    private Disposable subscriptions;


    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.take_picture:
                    if (mCameraView != null) {
                        if (isProgress){
                            Utils.Log(TAG, "Progressing");
                            break;
                        }
                        mCameraView.takePicture();
                        isProgress  = true;
                    }
                    break;
                case R.id.btnFlash:
                    if (mCameraView != null) {
                        mCurrentFlash = (mCurrentFlash + 1) % FLASH_OPTIONS.length;
                        btnFlash.setImageResource(FLASH_ICONS[mCurrentFlash]);
                        mCameraView.setFlash(FLASH_OPTIONS[mCurrentFlash]);
                    }
                    break;
                case R.id.btnSwitch:
                    if (mCameraView != null) {
                        int facing = mCameraView.getFacing();
                        mCameraView.setFacing(facing == CameraView.FACING_FRONT ?
                                CameraView.FACING_BACK : CameraView.FACING_FRONT);
                    }
                    break;
                case R.id.btnDone :
                    onBackPressed();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        storage = new Storage(this);

        if (mCameraView != null) {
            mCameraView.addCallback(mCallback);
        }

        take_picture.setOnClickListener(mOnClickListener);
        btnDone.setOnClickListener(mOnClickListener);
        btnFlash.setOnClickListener(mOnClickListener);
        btnSwitch.setOnClickListener(mOnClickListener);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }

        storage = new Storage(this);
        storage.setEncryptConfiguration(SuperSafeApplication.getInstance().getConfigurationFile());
        mCipher = storage.getCipher(Cipher.ENCRYPT_MODE);
    }


    @Override
    protected void onResume() {
        super.onResume();
        mCameraView.start();
        Utils.Log(TAG,"onResume");
    }

    @Override
    protected void onPause() {
        mCameraView.stop();
        Utils.Log(TAG,"onPause");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utils.Log(TAG,"onDestroy");
        try {
            storage.deleteFile(Utils.getPackagePath(getApplicationContext()).getAbsolutePath());
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void onBackPressed() {
        if (isReload){
            Intent intent = new Intent();
            setResult(RESULT_OK,intent);
            Utils.Log(TAG,"onBackPressed");
        }
        super.onBackPressed();
    }


    private CameraView.Callback mCallback
            = new CameraView.Callback() {
        @Override
        public void onCameraOpened(CameraView cameraView) {
            Utils.Log(TAG, "onCameraOpened");
        }
        @Override
        public void onCameraClosed(CameraView cameraView) {
            Utils.Log(TAG, "onCameraClosed");
        }

        @Override
        public void onPictureTaken(CameraView cameraView, final byte[] data,int orientation) {
            Utils.Log(TAG, "onPictureTaken " + data.length);
            Toast.makeText(cameraView.getContext(), R.string.picture_taken, Toast.LENGTH_SHORT).show();
            if (MainCategories.getInstance().intent_localCategoriesId==null){
                Utils.Log(TAG, "Local id is null");
                return;
            }
            isReload = true;
            onSaveData(data);
        }
    };

    public void onSaveData(final byte[]mData){
        subscriptions = Observable.create(subscriber -> {
            File thumbnail = null;
            final byte[]data = mData;
            final Bitmap mBitmap;
            try {

                mBitmap = Utils.getThumbnailScale(data);
//                thumbnail =  new Compressor(CameraActivity.this)
//                        .setMaxWidth(640)
//                        .setMaxHeight(480)
//                        .setQuality(85)
//                        .setCompressFormat(Bitmap.CompressFormat.JPEG)
//                        .setDestinationDirectoryPath(SuperSafeApplication.getInstance().getPackageFolderPath(CameraActivity.this).getAbsolutePath())
//                        .compressToFile(data,getString(R.string.key_temporary));

                String path = SuperSafeApplication.getInstance().getSuperSafe();
                String currentTime = Utils.getCurrentDateTime();
                String uuId = Utils.getUUId();
                String pathContent = path + uuId+"/";
                storage.createDirectory(pathContent);
                String thumbnailPath = pathContent+"thumbnail_"+currentTime;
                String originalPath = pathContent+currentTime;

                DriveDescription description = new DriveDescription();
                description.fileExtension = getString(R.string.key_jpg);
                description.originalPath = originalPath;
                description.thumbnailPath = thumbnailPath;
                description.subFolderName = uuId;
                description.localCategories_Id = MainCategories.getInstance().intent_localCategoriesId;
                description.nameMainCategories = MainCategories.getInstance().intent_name;
                description.local_id = uuId;
                description.global_original_id = null;
                description.mimeType = MediaType.JPEG.type()+"/"+MediaType.JPEG.subtype();
                description.thumbnailName = currentTime;
                description.globalName = uuId;
                description.formatType = EnumFormatType.IMAGE.ordinal();
                description.degrees = 0;
                description.thumbnailSync = false;
                description.originalSync = false;
                description.global_thumbnail_id = null;
                description.fileType = EnumFileType.NONE.ordinal();
                description.originalName = currentTime;
                description.title = currentTime;
                description.thumbnailName = "thumbnail_"+currentTime;

                Items items = new Items(false,
                        description.originalSync,
                        description.thumbnailSync,
                        description.degrees,
                        description.fileType,
                        description.formatType,
                        description.title,
                        description.originalName,
                        description.thumbnailName ,
                        description.globalName,
                        description.originalPath ,
                        description.thumbnailPath,
                        description.local_id,
                        description.global_original_id,
                        description.global_thumbnail_id,
                        description.localCategories_Id,
                        description.mimeType,
                        description.fileExtension,
                        new Gson().toJson(description),
                        EnumStatus.UPLOAD);

                boolean createdThumbnail = storage.createFile(thumbnailPath,mBitmap);
                boolean createdOriginal = storage.createFile(originalPath,data);
                if (createdThumbnail && createdOriginal){
                    subscriber.onNext(items);
                    subscriber.onComplete();
                    Utils.Log(TAG,"CreatedFile successful");
                }
                else{
                    subscriber.onNext(null);
                    subscriber.onComplete();
                    Utils.Log(TAG,"CreatedFile failed");
                }

            } catch (Exception e) {
                subscriber.onNext(null);
                subscriber.onComplete();
                Log.w(TAG, "Cannot write to " + e);
            } finally {
                Utils.Log(TAG,"Finally");
                isProgress = false;
            }
    })
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .observeOn(Schedulers.io())
            .subscribe(response -> {
                try {
                    final Items mItem = (Items) response;
                    if (mItem!=null){
                        InstanceGenerator.getInstance(CameraActivity.this).onInsert(mItem);
                    }
                    Utils.Log(TAG,"Insert Successful");
                    Utils.Log(TAG,new Gson().toJson(mItem));
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                isProgress = false;
    });

    }


}
