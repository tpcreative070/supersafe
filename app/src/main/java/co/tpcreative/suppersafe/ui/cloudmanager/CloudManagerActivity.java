package co.tpcreative.suppersafe.ui.cloudmanager;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.darsh.multipleimageselect.helpers.Constants;
import com.darsh.multipleimageselect.models.Image;
import com.ftinc.kit.util.SizeUtils;
import com.google.gson.Gson;
import com.obsez.android.lib.filechooser.ChooserDialog;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import butterknife.BindView;
import butterknife.OnClick;
import co.tpcreative.suppersafe.R;
import co.tpcreative.suppersafe.common.Navigator;
import co.tpcreative.suppersafe.common.activity.BaseGoogleApi;
import co.tpcreative.suppersafe.common.api.request.DownloadFileRequest;
import co.tpcreative.suppersafe.common.api.request.UploadingFileRequest;
import co.tpcreative.suppersafe.common.controller.ServiceManager;
import co.tpcreative.suppersafe.common.request.DriveApiRequest;
import co.tpcreative.suppersafe.common.response.DriveResponse;
import co.tpcreative.suppersafe.common.services.SupperSafeApplication;
import co.tpcreative.suppersafe.common.services.SupperSafeServiceView;
import co.tpcreative.suppersafe.common.services.download.DownloadService;
import co.tpcreative.suppersafe.common.services.upload.ProgressRequestBody;
import co.tpcreative.suppersafe.common.services.upload.UploadService;
import co.tpcreative.suppersafe.common.util.NetworkUtil;
import co.tpcreative.suppersafe.common.util.Utils;
import co.tpcreative.suppersafe.demo.UploadFileAndViewActivity;
import co.tpcreative.suppersafe.model.DriveType;
import co.tpcreative.suppersafe.model.MainCategories;
import co.tpcreative.suppersafe.model.User;
import io.reactivex.Observable;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CloudManagerActivity extends BaseGoogleApi implements UploadService.UploadServiceListener{

    private static String TAG = CloudManagerActivity.class.getSimpleName();
    private SlidrConfig mConfig;
    @BindView(R.id.btnSignIn)
    Button btnSignIn;
    @BindView(R.id.btnDisconnect)
    Button btnDisconnect;
    @BindView(R.id.btnDriveAbout)
    Button btnDriveAbout;
    @BindView(R.id.tvStatus)
    TextView tvStatus;

    private DriveType driveType;
    private UploadService uploadService;
    private List<File>mListFile;

    private DownloadService downloadService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_manager);

        //android O fix bug orientation
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
        uploadService = new UploadService();
        downloadService = new DownloadService(this);
        mListFile = new ArrayList<>();
    }

    @OnClick(R.id.btnSignIn)
    public void onClickedSignIn(View view) {
        signIn("butlerichotel@gmail.com");
    }

    @OnClick(R.id.btnDisconnect)
    public void onClickedDisconnect(View view) {
        signOut();
    }

    @OnClick(R.id.btnDriveAbout)
    public void onDriveAbout(View view) {
        getAccessToken();
    }

    @OnClick(R.id.btnChooseActivity)
    public void onChooseActivity(View view) {
        Navigator.onChooseActivity(this);
    }

    @OnClick(R.id.btnUpload)
    public void onUpload() {
        driveType = DriveType.IN_FOLDER;
        Navigator.onMoveToAlbum(this);
    }

    @OnClick(R.id.btnDownload)
    public void onDownload(View view){
        onDownloadFile();
    }

    @OnClick(R.id.btnUploadFileExtension)
    public void onUploadFileExtension(View view){
        onDriveUploadFileExtention();
    }

    @OnClick(R.id.btnCreateFolder)
    public void onCreateFolder(View view){
        ServiceManager.getInstance().onCreateFolder();
    }

    @OnClick(R.id.btnCheckInAppFolder)
    public void onCheckInAppFolder(View view){
        final String folderName = getString(R.string.key_main_album);
        ServiceManager.getInstance().onCheckInAppFolderExisting(folderName);
    }

    @OnClick(R.id.btnCreateInAppFolder)
    public void onCreateInAppFolder(View view){
        final String folderName = getString(R.string.key_main_album);
        ServiceManager.getInstance().onCheckInAppFolderExisting(folderName);
    }

    @OnClick(R.id.btnUploadFileInAppFolder)
    public void onUploadFileInAppFolder(View view){
        driveType = DriveType.IN_APP_FOLDER;
        Navigator.onMoveToAlbum(this);
    }

    @OnClick(R.id.btnGetListFolder)
    public void onClickedListInApp(){
        ServiceManager.getInstance().onGetListFileInApp();
    }

    @OnClick(R.id.btnMainCategories)
    public void onClickedInitMainCategories(View view){
        ServiceManager.getInstance().onInitMainCategories();
    }

    @OnClick(R.id.btnPrintInAppFolder)
    public void onClickedPrintInAppFolder(View view){
       Utils.Log(TAG,"Main categories : "+new Gson().toJson( MainCategories.getInstance().getMainCategoriesList()));
    }

    @Override
    protected void onDriveClientReady() {
        btnSignIn.setVisibility(View.GONE);
        btnDriveAbout.setVisibility(View.VISIBLE);
        btnDisconnect.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDriveSuccessful() {
        Log.d(TAG, "onDriveSuccessful");
        btnSignIn.setVisibility(View.GONE);
        btnDriveAbout.setVisibility(View.VISIBLE);
        btnDisconnect.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDriveError() {
        Log.d(TAG, "onDriveError");
        btnSignIn.setVisibility(View.VISIBLE);
        btnDriveAbout.setVisibility(View.GONE);
        btnDisconnect.setVisibility(View.GONE);
    }

    @Override
    protected void onDriveSignOut() {
        Log.d(TAG, "onDriveSignOut");
        btnSignIn.setVisibility(View.VISIBLE);
        btnDriveAbout.setVisibility(View.GONE);
        btnDisconnect.setVisibility(View.GONE);
    }

    @Override
    protected void onDriveRevokeAccess() {
        Log.d(TAG, "onDriveRevokeAccess");
        btnSignIn.setVisibility(View.VISIBLE);
        btnDriveAbout.setVisibility(View.GONE);
        btnDisconnect.setVisibility(View.GONE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "Selected album :");
        if (resultCode == Activity.RESULT_OK && data != null) {
            switch (driveType) {
                case IN_FOLDER:
                    mListFile.clear();
                    ArrayList<Image> images = data.getParcelableArrayListExtra(Constants.INTENT_EXTRA_IMAGES);
                    for (int i = 0, l = images.size(); i < l; i++) {
                        String path = images.get(i).path;
                        Log.d(TAG, "Selected album :" + path);

                        final File file  = new File(path);
                        mListFile.add(file);
                        onUploadFile(file);

                    }
                    break;
                case IN_APP_FOLDER:
                    images = data.getParcelableArrayListExtra(Constants.INTENT_EXTRA_IMAGES);
                    for (int i = 0, l = images.size(); i < l; i++) {
                        String path = images.get(i).path;
                        Log.d(TAG, "Selected album :" + path);
                        final File file  = new File(path);
                        onUploadFileInAppFolder(file);
                    }
                 default:
                     Log.d(TAG,"Nothing");
                     break;
            }
        }
        else{
            Log.d(TAG,"Nothing");
        }
    }

    public void onDownloadFile(){

        DownloadFileRequest request = new DownloadFileRequest();

        request.api_name = String.format(getString(R.string.url_drive_download),"1_w6_Rg-ZC9dFXqK_1hYCA1qhAr4nbla1");
        request.file_name = "20180823_161706.zip";
        request.path_folder_output  = SupperSafeApplication.getInstance().getSupperSafe();
        final User mUser = User.getInstance().getUserInfo();
        request.Authorization = mUser.access_token;
        downloadService.onProgressingDownload(new DownloadService.DownLoadServiceListener() {
            @Override
            public void onDownLoadCompleted(File file_name, DownloadFileRequest request) {
                Log.d(TAG,"onDownLoadCompleted "+ file_name.getAbsolutePath());
            }

            @Override
            public void onDownLoadError(String error) {
                Log.d(TAG,"onDownLoadError "+ error);
            }

            @Override
            public void onProgressingDownloading(int percent) {
                Log.d(TAG,"Progressing "+ percent +"%");
            }

            @Override
            public void onAttachmentElapsedTime(long elapsed) {

            }

            @Override
            public void onAttachmentAllTimeForDownloading(long all) {

            }

            @Override
            public void onAttachmentRemainingTime(long all) {

            }

            @Override
            public void onAttachmentSpeedPerSecond(double all) {

            }

            @Override
            public void onAttachmentTotalDownload(long totalByte, long totalByteDownloaded) {

            }

            @Override
            public void onSavedCompleted() {
                Log.d(TAG,"onSavedCompleted ");
            }

            @Override
            public void onErrorSave(String name) {

            }

            @Override
            public void onCodeResponse(int code, DownloadFileRequest request) {

            }
        },"https://www.googleapis.com/drive/v3/files/");
        request.mapHeader = new HashMap<>();
        request.mapObject = new HashMap<>();
        downloadService.downloadDriveFileByGET(request);
    }

    public void onUploadFile(final File file){
        final User mUser = User.getInstance().getUserInfo();
        MediaType  contentType = MediaType.parse("application/json; charset=UTF-8");
        HashMap<String,String> content = new HashMap<>();
        content.put(getString(R.string.key_name),file.getName());
        MultipartBody.Part metaPart = MultipartBody.Part.create(RequestBody.create(contentType,new Gson().toJson(content)));
        ProgressRequestBody fileBody = new ProgressRequestBody(file, new ProgressRequestBody.UploadCallbacks() {
            @Override
            public void onProgressUpdate(int percentage) {
                Log.d(TAG,"Progressing "+ percentage +"%");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvStatus.setText(""+percentage+"%");
                    }
                });
            }
            @Override
            public void onError() {
                Log.d(TAG,"onError");
                tvStatus.setText("onError");
            }
            @Override
            public void onFinish() {
                Log.d(TAG,"onFinish");
                tvStatus.setText("onFinish");
            }
        });

        fileBody.setContentType(Utils.getMimeType(file.getAbsolutePath()));
        MultipartBody.Part dataPart = MultipartBody.Part.create(fileBody);
        Call<DriveResponse> request = SupperSafeApplication.serverAPI.uploadFileMutil(getString(R.string.url_drive_upload),mUser.access_token,metaPart,dataPart,Utils.getMimeType(file.getAbsolutePath()));
        request.enqueue(new Callback<DriveResponse>(){
            @Override
            public void onResponse(Call<DriveResponse> call, Response<DriveResponse> response) {
                Log.d(TAG,"response successful :"+ new Gson().toJson(response.body()));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvStatus.setText("response successful :"+ new Gson().toJson(response.body()));
                    }
                });
            }
            @Override
            public void onFailure(Call<DriveResponse> call, Throwable t) {
                Log.d(TAG,"response failed :"+ t.getMessage());
                tvStatus.setText("response failed  :"+ t.getMessage());
            }
        });

    }



    public void onUploadFileInAppFolder(final File file){
        Log.d(TAG,"Upload File To In App Folder");
        final User mUser = User.getInstance().getUserInfo();
        MediaType  contentType = MediaType.parse("application/json; charset=UTF-8");
        HashMap<String,Object> content = new HashMap<>();
        content.put(getString(R.string.key_name),file.getName());

        List<String> list = new ArrayList<>();
        list.add("1PINsLydefzvuIqFjEPE04AELiLdRGI3Y7OedM3XS1UI_wVg5j8U");
        content.put(getString(R.string.key_parents),list);


        MultipartBody.Part metaPart = MultipartBody.Part.create(RequestBody.create(contentType,new Gson().toJson(content)));

        Log.d(TAG,"parents: " +new Gson().toJson(content));

        ProgressRequestBody fileBody = new ProgressRequestBody(file, new ProgressRequestBody.UploadCallbacks() {
            @Override
            public void onProgressUpdate(int percentage) {
                Log.d(TAG,"Progressing "+ percentage +"%");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvStatus.setText(""+percentage+"%");
                    }
                });
            }
            @Override
            public void onError() {
                Log.d(TAG,"onError");
                tvStatus.setText("onError");
            }
            @Override
            public void onFinish() {
                Log.d(TAG,"onFinish");
                tvStatus.setText("onFinish");
            }
        });

        fileBody.setContentType(Utils.getMimeType(file.getAbsolutePath()));
        MultipartBody.Part dataPart = MultipartBody.Part.create(fileBody);


        Call<DriveResponse> request = SupperSafeApplication.serverAPI.uploadFileMultipleInAppFolder(getString(R.string.url_drive_upload),mUser.access_token,metaPart,dataPart,Utils.getMimeType(file.getAbsolutePath()));
        request.enqueue(new Callback<DriveResponse>(){
            @Override
            public void onResponse(Call<DriveResponse> call, Response<DriveResponse> response) {
                Log.d(TAG,"response successful :"+ new Gson().toJson(response.body()));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvStatus.setText("response successful :"+ new Gson().toJson(response.body()));
                    }
                });
            }
            @Override
            public void onFailure(Call<DriveResponse> call, Throwable t) {
                Log.d(TAG,"response failed :"+ t.getMessage());
                tvStatus.setText("response failed  :"+ t.getMessage());
            }
        });

    }


    public void onDriveUploadFileExtention(){
        new ChooserDialog().with(this)
                .withFilter(false, false, "jpg", "jpeg", "png","mp4")
                .withStartFile("/storage/emulated/0/DCIM/Camera")
                .withResources(R.string.title_choose, R.string.title_choose, R.string.dialog_cancel)
                .withChosenListener(new ChooserDialog.Result() {
                    @Override
                    public void onChoosePath(String path, File pathFile) {
                     onUploadFile(pathFile);
                    }
                })
                .build()
                .show();
    }


    @Override
    public void onUploadCompleted(String response, UploadingFileRequest request) {

        Log.d(TAG,"onUploadCompleted :" + response);
    }

    @Override
    public void onProgressing(int percent, long total) {

        Log.d(TAG,"onProgressing :" + percent);
    }

    @Override
    public void onSpeed(double speed) {
        Log.d(TAG,"speed :" + speed);
    }
}
