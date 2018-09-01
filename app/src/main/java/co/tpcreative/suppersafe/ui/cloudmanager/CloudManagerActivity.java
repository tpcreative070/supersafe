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
import android.widget.Toast;

import com.darsh.multipleimageselect.helpers.Constants;
import com.darsh.multipleimageselect.models.Image;
import com.ftinc.kit.util.SizeUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
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
import co.tpcreative.suppersafe.common.api.request.UploadingFileRequest;
import co.tpcreative.suppersafe.common.controller.ServiceManager;
import co.tpcreative.suppersafe.common.response.DriveResponse;
import co.tpcreative.suppersafe.common.services.SupperSafeApplication;
import co.tpcreative.suppersafe.common.services.SupperSafeServiceView;
import co.tpcreative.suppersafe.common.services.upload.ProgressRequestBody;
import co.tpcreative.suppersafe.common.services.upload.UploadService;
import co.tpcreative.suppersafe.common.util.NetworkUtil;
import co.tpcreative.suppersafe.demo.UploadFileAndViewActivity;
import co.tpcreative.suppersafe.model.DriveType;
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

    private final String FOLDER_NAME = "SUPPER_SAFE";
    private final String FOLDER_NAME_IN_APP = "SUPPER_SAFE_IN_APP";
    private DriveType driveType;
    private UploadService uploadService;

    private List<File>mListFile;

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

    @OnClick(R.id.btnCreateFolder)
    public void onCreateFolder(View view){
        ServiceManager.getInstance().onCreateFolder();
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


                    //uploadMultipart();
                    break;
                 default:
                     Log.d(TAG,"Nothing");
                     break;
            }
        }
        else{
            Log.d(TAG,"Nothing");
        }
    }

    public void onUploadFile(final File file){
        final User mUser = User.getInstance().getUserInfo();
        MediaType  contentType = MediaType.parse("application/json; charset=UTF-8");
        String content = "{\"name\": \"" + file.getName() + "\"}";
        MultipartBody.Part metaPart =
                            MultipartBody.Part.create(RequestBody.create(contentType,content));
        ProgressRequestBody fileBody = new ProgressRequestBody(file, new ProgressRequestBody.UploadCallbacks() {
            @Override
            public void onProgressUpdate(int percentage) {
                Log.d(TAG,"Progressing "+ percentage +"%");
            }
            @Override
            public void onError() {
                Log.d(TAG,"onError");
            }
            @Override
            public void onFinish() {
                Log.d(TAG,"onFinish");
            }
        });

        MultipartBody.Part dataPart =
                            MultipartBody.Part.create(fileBody);
        Call<DriveResponse> request =
                SupperSafeApplication.serverAPI.uploadFileMutil("https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart",mUser.access_token,metaPart,dataPart);
        request.enqueue(new Callback<DriveResponse>(){
            @Override
            public void onResponse(Call<DriveResponse> call, Response<DriveResponse> response) {
                Log.d(TAG,"response successful :"+ new Gson().toJson(response.body()));
            }
            @Override
            public void onFailure(Call<DriveResponse> call, Throwable t) {
                Log.d(TAG,"response failed :"+ t.getMessage());
            }
        });
    }


    public void uploadMultipart() {
        try {
            if (NetworkUtil.pingIpAddress(getApplicationContext())) {
                Toast.makeText(this, "Please check internet", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, "Uploading", Toast.LENGTH_SHORT).show();
            UploadingFileRequest request = new UploadingFileRequest();

            HashMap<String,String> header = new HashMap<>();
            final User mUser = User.getInstance().getUserInfo();
            header.put("Authorization",mUser.access_token);
            header.put("Content-Type","application/json");


            request.mapHeader = header;
            request.list = mListFile;

            HashMap<String,String> body = new HashMap<>();
            body.put("name","abc");
            body.put("mimeType","image/jpeg");
            request.mapBody = body;

            uploadService.setListener(this, "https://www.googleapis.com/drive/v3/files");
            uploadService.postUploadFileMulti(request);

        }
        catch (Exception e){
            Log.d(TAG,"Error occurred "+ e.getMessage());
        }
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
