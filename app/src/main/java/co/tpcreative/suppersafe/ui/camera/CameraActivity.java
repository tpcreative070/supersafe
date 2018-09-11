package co.tpcreative.suppersafe.ui.camera;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.google.android.cameraview.AspectRatio;
import com.google.android.cameraview.CameraView;
import com.google.common.net.MediaType;
import com.google.gson.Gson;
import com.snatik.storage.Storage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import butterknife.BindView;
import co.tpcreative.suppersafe.R;
import co.tpcreative.suppersafe.common.SensorOrientationChangeNotifier;
import co.tpcreative.suppersafe.common.activity.BaseActivity;
import co.tpcreative.suppersafe.common.services.SupperSafeApplication;
import co.tpcreative.suppersafe.common.util.Utils;
import co.tpcreative.suppersafe.model.DriveDescription;
import co.tpcreative.suppersafe.model.EnumStatus;
import co.tpcreative.suppersafe.model.EnumTypeFile;
import co.tpcreative.suppersafe.model.Items;
import co.tpcreative.suppersafe.model.MainCategories;
import co.tpcreative.suppersafe.model.room.InstanceGenerator;

public class CameraActivity extends BaseActivity implements
        ActivityCompat.OnRequestPermissionsResultCallback,
        AspectRatioFragment.Listener ,SensorOrientationChangeNotifier.Listener{

    private static final String TAG = CameraActivity.class.getSimpleName();

    private static final int REQUEST_CAMERA_PERMISSION = 1;

    private static final String FRAGMENT_DIALOG = "dialog";

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

    private static final int[] FLASH_TITLES = {
            R.string.flash_auto,
            R.string.flash_off,
            R.string.flash_on,
    };

    private int mCurrentFlash;
    private Handler mBackgroundHandler;
    private int mOrientation = 0;
    private boolean isProgress;
    private boolean isReload;

    @BindView(R.id.camera)
    CameraView mCameraView;

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.take_picture:
                    if (mCameraView != null) {
                        mCameraView.takePicture();
                    }
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
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.take_picture);
        if (fab != null) {
            fab.setOnClickListener(mOnClickListener);
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }

        /*
        try{
            String path = SupperSafeApplication.getInstance().getSupperSafe()+"newFile";
            File file = new File(path);
            FileInputStream fileInputStream = new FileInputStream(file);
            path = SupperSafeApplication.getInstance().getSupperSafe()+"newFile.jpg";
            SingletonEncryptData.getInstance().onDecryptData(fileInputStream,path);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        */

        mCameraView.start();

    }

    @Override
    public void onOrientationChange(int orientation) {
        mOrientation = orientation;
        if (orientation == 90 || orientation == 270){
            Log.d(TAG,"OrientationChange Landscape :" + orientation);
        } else {
            // Do some portrait stuff
            Log.d(TAG,"OrientationChange Portrait" + orientation);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCameraView.start();
        SensorOrientationChangeNotifier.getInstance().addListener(this);
    }

    @Override
    protected void onPause() {
        mCameraView.stop();
        SensorOrientationChangeNotifier.getInstance().remove(this);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.aspect_ratio:
                FragmentManager fragmentManager = getSupportFragmentManager();
                if (mCameraView != null
                        && fragmentManager.findFragmentByTag(FRAGMENT_DIALOG) == null) {
                    final Set<AspectRatio> ratios = mCameraView.getSupportedAspectRatios();
                    final AspectRatio currentRatio = mCameraView.getAspectRatio();
                    AspectRatioFragment.newInstance(ratios, currentRatio)
                            .show(fragmentManager, FRAGMENT_DIALOG);
                }
                return true;
            case R.id.switch_flash:
                if (mCameraView != null) {
                    mCurrentFlash = (mCurrentFlash + 1) % FLASH_OPTIONS.length;
                    item.setTitle(FLASH_TITLES[mCurrentFlash]);
                    item.setIcon(FLASH_ICONS[mCurrentFlash]);
                    mCameraView.setFlash(FLASH_OPTIONS[mCurrentFlash]);
                }
                return true;
            case R.id.switch_camera:
                if (mCameraView != null) {
                    int facing = mCameraView.getFacing();
                    mCameraView.setFacing(facing == CameraView.FACING_FRONT ?
                            CameraView.FACING_BACK : CameraView.FACING_FRONT);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAspectRatioSelected(@NonNull AspectRatio ratio) {
        if (mCameraView != null) {
            Toast.makeText(this, ratio.toString(), Toast.LENGTH_SHORT).show();
            mCameraView.setAspectRatio(ratio);
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
                    thumbnail = Utils.getThumbnail(data);
                    originalFile = new ByteArrayInputStream(data);
                    String path = SupperSafeApplication.getInstance().getSupperSafe();
                    String currentTime = Utils.getCurrentDateTime();
                    String uuId = Utils.getUUId();
                    String pathContent = path + uuId+"/";
                    storage.createDirectory(pathContent);
                    String thumbnailPath = pathContent+"thumbnail_"+currentTime;
                    String originalPath = pathContent+currentTime;
                    storage.setEncryptConfiguration(SupperSafeApplication.getInstance().getConfigurationFile());
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
                    description.typeFile = EnumTypeFile.IMAGE.ordinal();

                    Items items = new Items(false,
                            description.typeFile,
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

    public InputStream getInputStream(final Bitmap bitmap){
        ByteArrayInputStream bs =null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100 /*ignored for PNG*/, bos);
            byte[] bitmapdata = bos.toByteArray();
            bs = new ByteArrayInputStream(bitmapdata);
            bos.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        finally {
            if (bs!=null){
                try {
                    bs.close();
                } catch (IOException ignored) {}
            }
        }
        return bs;
    }

    public void SaveImage(Bitmap finalBitmap) {
        String path = SupperSafeApplication.getInstance().getSupperSafe();
        File myDir = new File(path);
        myDir.mkdirs();
        String thumbnailPath = "thumbnail_"+Utils.getCurrentDateTime()+".jpg";
        File file = new File (myDir, thumbnailPath);
        if (file.exists ()) file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
