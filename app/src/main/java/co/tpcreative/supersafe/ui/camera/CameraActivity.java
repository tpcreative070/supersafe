package co.tpcreative.supersafe.ui.camera;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import butterknife.BindView;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.activity.BaseActivity;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.DriveDescription;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.EnumTypeFile;
import co.tpcreative.supersafe.model.Items;
import co.tpcreative.supersafe.model.MainCategories;
import co.tpcreative.supersafe.model.room.InstanceGenerator;

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
    private Handler mBackgroundHandler;
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


    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.take_picture:
                    if (mCameraView != null) {
                        mCameraView.takePicture();
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
        if (mBackgroundHandler != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mBackgroundHandler.getLooper().quitSafely();
            } else {
                mBackgroundHandler.getLooper().quit();
            }
            mBackgroundHandler = null;
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

    private Handler getBackgroundHandler() {
        if (mBackgroundHandler == null) {
            HandlerThread thread = new HandlerThread("background");
            thread.start();
            mBackgroundHandler = new Handler(thread.getLooper());
        }
        return mBackgroundHandler;
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
            if (isProgress){
                Utils.Log(TAG, "Progressing");
                return;
            }

            if (MainCategories.getInstance().intent_localCategoriesId==null){
                Utils.Log(TAG, "Local id is null");
                return;
            }
            isReload = true;
            onSaveData(data);
        }
    };

    public void onSaveData(final byte[]data){
        isProgress = true;
        getBackgroundHandler().post(new Runnable() {
            @Override
            public void run() {
                InputStream  originalFile = null;
                Bitmap thumbnail = null;
                try {
                    thumbnail = Utils.getThumbnailScale(data);
                    originalFile = new ByteArrayInputStream(data);
                    String path = SuperSafeApplication.getInstance().getSupperSafe();
                    String currentTime = Utils.getCurrentDateTime();
                    String uuId = Utils.getUUId();
                    String pathContent = path + uuId+"/";
                    storage.createDirectory(pathContent);
                    String thumbnailPath = pathContent+"thumbnail_"+currentTime;
                    String originalPath = pathContent+currentTime;
                    storage.setEncryptConfiguration(SuperSafeApplication.getInstance().getConfigurationFile());
                    storage.createFile(thumbnailPath,thumbnail);
                    storage.createFile(originalPath,data);



                    DriveDescription description = new DriveDescription();
                    description.fileExtension = getString(R.string.key_jpg);
                    description.originalPath = originalPath;
                    description.thumbnailPath = thumbnailPath;
                    description.subFolderName = uuId;
                    description.localCategories_Id = MainCategories.getInstance().intent_localCategoriesId;
                    description.nameMainCategories = MainCategories.getInstance().intent_name;
                    description.local_id = uuId;
                    description.global_id = null;
                    description.mimeType = MediaType.JPEG.type()+"/"+MediaType.JPEG.subtype();
                    description.name = currentTime;
                    description.globalName = uuId;
                    description.fileType = EnumTypeFile.IMAGE.ordinal();
                    description.degrees = 0;

                    Items items = new Items(false,
                            description.degrees,
                            description.fileType,
                            description.name,
                            description.globalName,
                            description.thumbnailPath,
                            description.originalPath ,
                            description.local_id,
                    null,
                            description.localCategories_Id,
                            description.mimeType,
                            description.fileExtension,
                            new Gson().toJson(description),
                            EnumStatus.UPLOAD);

                    InstanceGenerator.getInstance(CameraActivity.this).onInsert(items);
                    Log.d(TAG,new Gson().toJson(items));

                } catch (Exception e) {
                    Log.w(TAG, "Cannot write to " + e);
                } finally {
                    if (originalFile!=null){
                        try {
                            originalFile.close();
                            thumbnail = null;
                        } catch (IOException ignored) {}
                    }
                    Utils.Log(TAG,"Finally");
                    isProgress = false;
                }

            }
        });
    }


}
