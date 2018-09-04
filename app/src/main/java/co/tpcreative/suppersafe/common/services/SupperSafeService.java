package co.tpcreative.suppersafe.common.services;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import com.google.android.gms.drive.DriveFolder;
import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import co.tpcreative.suppersafe.R;
import co.tpcreative.suppersafe.common.api.request.DownloadFileRequest;
import co.tpcreative.suppersafe.common.controller.PrefsController;
import co.tpcreative.suppersafe.common.presenter.PresenterService;
import co.tpcreative.suppersafe.common.request.DriveApiRequest;
import co.tpcreative.suppersafe.common.response.DriveResponse;
import co.tpcreative.suppersafe.common.services.download.DownloadService;
import co.tpcreative.suppersafe.common.services.upload.ProgressRequestBody;
import co.tpcreative.suppersafe.common.util.NetworkUtil;
import co.tpcreative.suppersafe.common.util.Utils;
import co.tpcreative.suppersafe.model.MainCategories;
import co.tpcreative.suppersafe.model.User;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;

public class SupperSafeService extends PresenterService<SupperSafeServiceView> implements SupperSafeReceiver.ConnectivityReceiverListener {

    private final IBinder mBinder = new LocalBinder(); // Binder given to clients
    private Intent mIntent;
    private SupperSafeServiceListener listener;
    private SupperSafeReceiver androidReceiver;
    private DownloadService downloadService;
    private static final String TAG = SupperSafeService.class.getSimpleName();

    public interface SupperSafeServiceListener{
        void onResponse(String message);
        void onConnectionChanged(boolean isChanged);
        void onMessageAction(String message);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"onCreate");
        downloadService = new DownloadService(this);
        onInitReceiver();
    }

    public void onInitReceiver(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            androidReceiver = new SupperSafeReceiver();
            registerReceiver(androidReceiver,intentFilter);
            SupperSafeApplication.getInstance().setConnectivityListener(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"onDestroy");
        if (androidReceiver!=null){
            unregisterReceiver(androidReceiver);
        }
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {

    }

    public void getAction(){
        Log.d(TAG,"Action");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // If we get killed, after returning from here, restart
        Log.d(TAG,"onStartCommand");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Bundle extras = intent.getExtras();
        Log.d(TAG,"onBind");
        // Get messager from the Activity
        if (extras != null) {
            Log.d("service","onBind with extra");
        }
        return mBinder;
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */

    public class LocalBinder extends Binder {
        public SupperSafeService getService() {
            // Return this instance of SignalRService so clients can call public methods
            return SupperSafeService.this;
        }
        public void setIntent(Intent intent){
            mIntent = intent;
        }

        public void setListener(SupperSafeServiceListener mListener){
            listener = mListener;
        }
    }

    /*Network request*/

    public void getDriveAbout(){
        SupperSafeServiceView view = view();
        if (view == null) {
            return;
        }
        if (NetworkUtil.pingIpAddress(SupperSafeApplication.getInstance())) {
            return;
        }
        if (subscriptions == null) {
            return;
        }
        final User user = User.getInstance().getUserInfo();
        if (user==null){
            return;
        }

        if (user.access_token==null){
            return;
        }

        String access_token = user.access_token;
        Log.d(TAG,"access_token : " + access_token);
        subscriptions.add(SupperSafeApplication.serverDriveApi.onGetDriveAbout(access_token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.startLoading())
                .subscribe(onResponse -> {
                    if (view==null){
                        Log.d(TAG,"View is null");
                        return;
                    }
                    view.stopLoading();
                    if (onResponse.error!=null){
                        Log.d(TAG,"onError 1");
                        view.onError(new Gson().toJson(onResponse.error));
                    }
                    else{
                        Log.d(TAG,"onSuccessful 2");
                        final User mUser = User.getInstance().getUserInfo();
                        mUser.driveAbout = onResponse;
                        PrefsController.putString(getString(R.string.key_user),new Gson().toJson(mUser));
                        view.onSuccessful(new Gson().toJson(onResponse));
                    }
                    Log.d(TAG, "Body : " + new Gson().toJson(onResponse));
                }, throwable -> {
                    if (view==null){
                        Log.d(TAG,"View is null");
                        return;
                    }

                    if (throwable instanceof HttpException) {
                        ResponseBody bodys = ((HttpException) throwable).response().errorBody();
                        try {
                            Log.d(TAG,"error" +bodys.string());
                            String msg = new Gson().toJson(bodys.string());
                            Log.d(TAG, msg);
                            view.onError(""+msg);
                        } catch (IOException e) {
                            e.printStackTrace();
                            view.onError(""+e.getMessage());
                        }
                    } else {
                        Log.d(TAG, "Can not call " + throwable.getMessage());
                        view.onError("Error :"+ throwable.getMessage());
                    }
                    view.stopLoading();
                }));
    }


    public void onCreateFolder(){
        SupperSafeServiceView view = view();
        if (view == null) {
            return;
        }
        if (NetworkUtil.pingIpAddress(SupperSafeApplication.getInstance())) {
            return;
        }
        if (subscriptions == null) {
            return;
        }
        final User user = User.getInstance().getUserInfo();
        if (user==null){
            return;
        }

        if (user.access_token==null){
            return;
        }

        DriveApiRequest request = new DriveApiRequest();
        request.mimeType = DriveFolder.MIME_TYPE;
        request.name = getString(R.string.key_supper_safe);

        String access_token = user.access_token;
        Utils.Log(TAG,"access_token : " + access_token);
        subscriptions.add(SupperSafeApplication.serverDriveApi.onCrateFolder(access_token,request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.startLoading())
                .subscribe(onResponse -> {
                    if (view==null){
                        Log.d(TAG,"View is null");
                        return;
                    }
                    view.stopLoading();
                    if (onResponse.error!=null){
                        Log.d(TAG,"onError 1");
                        view.onError(new Gson().toJson(onResponse.error));
                    }
                    else{
                        Log.d(TAG,"onSuccessful 2");
                        view.onSuccessful(new Gson().toJson(onResponse));
                    }
                    Log.d(TAG, "Body : " + new Gson().toJson(onResponse));
                }, throwable -> {
                    if (view==null){
                        Log.d(TAG,"View is null");
                        return;
                    }

                    if (throwable instanceof HttpException) {
                        ResponseBody bodys = ((HttpException) throwable).response().errorBody();
                        try {
                            Log.d(TAG,"error" +bodys.string());
                            String msg = new Gson().toJson(bodys.string());
                            Log.d(TAG, msg);
                            view.onError(""+msg);
                        } catch (IOException e) {
                            e.printStackTrace();
                            view.onError(""+e.getMessage());
                        }
                    } else {
                        Log.d(TAG, "Can not call " + throwable.getMessage());
                        view.onError("Error :"+ throwable.getMessage());
                    }
                    view.stopLoading();
                }));
    }

    public void onCreateInAppFolder(final String folderName){
        SupperSafeServiceView view = view();
        if (view == null) {
            return;
        }
        if (NetworkUtil.pingIpAddress(SupperSafeApplication.getInstance())) {
            return;
        }
        if (subscriptions == null) {
            return;
        }
        final User user = User.getInstance().getUserInfo();
        if (user==null){
            return;
        }

        if (user.access_token==null){
            return;
        }

        DriveApiRequest request = new DriveApiRequest();
        request.mimeType =  DriveFolder.MIME_TYPE;
        request.name =  folderName;
        List<String> mList = new ArrayList<>();
        mList.add(getString(R.string.key_appDataFolder));
        request.parents = mList;
        String access_token = user.access_token;
        Log.d(TAG,"access_token : " + access_token);
        subscriptions.add(SupperSafeApplication.serverDriveApi.onCrateFolder(access_token,request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.startLoading())
                .subscribe(onResponse -> {
                    if (view==null){
                        Log.d(TAG,"View is null");
                        return;
                    }
                    view.stopLoading();
                    if (onResponse.error!=null){
                        Log.d(TAG,"onError 1");
                        view.onError(new Gson().toJson(onResponse.error));
                    }
                    else{
                        Log.d(TAG,"onSuccessful 2");
                        view.onSuccessful(new Gson().toJson(onResponse));
                    }
                    Log.d(TAG, "Body : " + new Gson().toJson(onResponse));
                }, throwable -> {
                    if (view==null){
                        Log.d(TAG,"View is null");
                        return;
                    }

                    if (throwable instanceof HttpException) {
                        ResponseBody bodys = ((HttpException) throwable).response().errorBody();
                        try {
                            Log.d(TAG,"error" +bodys.string());
                            String msg = new Gson().toJson(bodys.string());
                            Log.d(TAG, msg);
                            view.onError(""+msg);
                        } catch (IOException e) {
                            e.printStackTrace();
                            view.onError(""+e.getMessage());
                        }
                    } else {
                        Log.d(TAG, "Can not call " + throwable.getMessage());
                        view.onError("Error :"+ throwable.getMessage());
                    }
                    view.stopLoading();
                }));
    }

    public void onCreateInAppFolder(final String folderName, final boolean isLoadList){
        SupperSafeServiceView view = view();
        if (view == null) {
            return;
        }
        if (NetworkUtil.pingIpAddress(SupperSafeApplication.getInstance())) {
            return;
        }
        if (subscriptions == null) {
            return;
        }
        final User user = User.getInstance().getUserInfo();
        if (user==null){
            return;
        }

        if (user.access_token==null){
            return;
        }

        DriveApiRequest request = new DriveApiRequest();
        request.mimeType =  DriveFolder.MIME_TYPE;
        request.name =  folderName;
        List<String> mList = new ArrayList<>();
        mList.add(getString(R.string.key_appDataFolder));
        request.parents = mList;
        String access_token = user.access_token;
        Log.d(TAG,"access_token : " + access_token);
        subscriptions.add(SupperSafeApplication.serverDriveApi.onCrateFolder(access_token,request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.startLoading())
                .subscribe(onResponse -> {
                    if (view==null){
                        Log.d(TAG,"View is null");
                        return;
                    }
                    view.stopLoading();
                    if (onResponse.error!=null){
                        Log.d(TAG,"onError 1");
                        view.onError(new Gson().toJson(onResponse.error));
                    }
                    else{
                        Log.d(TAG,"onSuccessful 2");
                        view.onSuccessful(new Gson().toJson(onResponse));
                        if (isLoadList){
                            onGetListFolderInApp();
                        }
                    }
                    Log.d(TAG, "Body : " + new Gson().toJson(onResponse));
                }, throwable -> {
                    if (view==null){
                        Log.d(TAG,"View is null");
                        return;
                    }

                    if (throwable instanceof HttpException) {
                        ResponseBody bodys = ((HttpException) throwable).response().errorBody();
                        try {
                            Log.d(TAG,"error" +bodys.string());
                            String msg = new Gson().toJson(bodys.string());
                            Log.d(TAG, msg);
                            view.onError(""+msg);
                        } catch (IOException e) {
                            e.printStackTrace();
                            view.onError(""+e.getMessage());
                        }
                    } else {
                        Log.d(TAG, "Can not call " + throwable.getMessage());
                        view.onError("Error :"+ throwable.getMessage());
                    }
                    view.stopLoading();
                }));
    }



    public void onCheckInAppFolderExisting(final String folderName){

        SupperSafeServiceView view = view();
        if (view == null) {
            return;
        }
        if (NetworkUtil.pingIpAddress(SupperSafeApplication.getInstance())) {
            return;
        }
        if (subscriptions == null) {
            return;
        }
        final User user = User.getInstance().getUserInfo();
        if (user==null){
            return;
        }

        if (user.access_token==null){
            return;
        }

        String access_token = user.access_token;
        Log.d(TAG,"access_token : " + access_token);
        subscriptions.add(SupperSafeApplication.serverDriveApi.onCheckInAppFolderExisting(access_token,"name = '"+folderName+"'", getString(R.string.key_appDataFolder))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.startLoading())
                .subscribe(onResponse -> {
                    if (view==null){
                        Log.d(TAG,"View is null");
                        return;
                    }
                    view.stopLoading();
                    if (onResponse.error!=null){
                        Log.d(TAG,"onError 1");
                        view.onError(new Gson().toJson(onResponse.error));
                    }
                    else{
                        Log.d(TAG,"onSuccessful 2");
                        view.onSuccessful(new Gson().toJson(onResponse));
                        if (onResponse.files!=null){
                            if (onResponse.files.size()==0){
                                onCreateInAppFolder(folderName);
                            }
                        }
                    }
                    Log.d(TAG, "Body : " + new Gson().toJson(onResponse));
                }, throwable -> {
                    if (view==null){
                        Log.d(TAG,"View is null");
                        return;
                    }
                    if (throwable instanceof HttpException) {
                        ResponseBody bodys = ((HttpException) throwable).response().errorBody();
                        try {
                            Log.d(TAG,"error" +bodys.string());
                            String msg = new Gson().toJson(bodys.string());
                            Log.d(TAG, msg);
                            view.onError(""+msg);
                        } catch (IOException e) {
                            e.printStackTrace();
                            view.onError(""+e.getMessage());
                        }
                    } else {
                        Log.d(TAG, "Can not call " + throwable.getMessage());
                        view.onError("Error :"+ throwable.getMessage());
                    }
                    view.stopLoading();
                }));
    }

    public void onCheckInAppFolderExisting(final String folderName,final boolean isLoadList){

        SupperSafeServiceView view = view();
        if (view == null) {
            return;
        }
        if (NetworkUtil.pingIpAddress(SupperSafeApplication.getInstance())) {
            return;
        }
        if (subscriptions == null) {
            return;
        }
        final User user = User.getInstance().getUserInfo();
        if (user==null){
            return;
        }

        if (user.access_token==null){
            return;
        }

        String access_token = user.access_token;
        Log.d(TAG,"access_token : " + access_token);
        subscriptions.add(SupperSafeApplication.serverDriveApi.onCheckInAppFolderExisting(access_token,"name = '"+folderName+"'", getString(R.string.key_appDataFolder))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.startLoading())
                .subscribe(onResponse -> {
                    if (view==null){
                        Log.d(TAG,"View is null");
                        return;
                    }
                    view.stopLoading();
                    if (onResponse.error!=null){
                        Log.d(TAG,"onError 1");
                        view.onError(new Gson().toJson(onResponse.error));
                    }
                    else{
                        Log.d(TAG,"onSuccessful 2");
                        view.onSuccessful(new Gson().toJson(onResponse));
                        if (onResponse.files!=null){
                            if (onResponse.files.size()==0){
                                onCreateInAppFolder(folderName,isLoadList);
                            }
                            else{
                                if (isLoadList){
                                    onGetListFolderInApp();
                                }
                            }
                        }
                    }
                    Log.d(TAG, "Body : " + new Gson().toJson(onResponse));
                }, throwable -> {
                    if (view==null){
                        Log.d(TAG,"View is null");
                        return;
                    }
                    if (throwable instanceof HttpException) {
                        ResponseBody bodys = ((HttpException) throwable).response().errorBody();
                        try {
                            Log.d(TAG,"error" +bodys.string());
                            String msg = new Gson().toJson(bodys.string());
                            Log.d(TAG, msg);
                            view.onError(""+msg);
                        } catch (IOException e) {
                            e.printStackTrace();
                            view.onError(""+e.getMessage());
                        }
                    } else {
                        Log.d(TAG, "Can not call " + throwable.getMessage());
                        view.onError("Error :"+ throwable.getMessage());
                    }
                    view.stopLoading();
                }));
    }


    public void onGetListFolderInApp(){
        SupperSafeServiceView view = view();
        if (view == null) {
            return;
        }
        if (NetworkUtil.pingIpAddress(SupperSafeApplication.getInstance())) {
            return;
        }
        if (subscriptions == null) {
            return;
        }
        final User user = User.getInstance().getUserInfo();
        if (user==null){
            return;
        }

        if (user.access_token==null){
            return;
        }
        String access_token = user.access_token;
        Log.d(TAG,"access_token : " + access_token);
        subscriptions.add(SupperSafeApplication.serverDriveApi.onGetListFile(access_token,getString(R.string.key_mime_type_folder), getString(R.string.key_appDataFolder))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.startLoading())
                .subscribe(onResponse -> {
                    if (view==null){
                        Log.d(TAG,"View is null");
                        return;
                    }
                    view.stopLoading();
                    if (onResponse.error!=null){
                        Log.d(TAG,"onError 1");
                        view.onError(new Gson().toJson(onResponse.error));
                    }
                    else{
                        Log.d(TAG,"onSuccessful 2");
                        if (onResponse.files!=null){
                            if (onResponse.files.size()>0){
                                final List<MainCategories> categories = new ArrayList<>();
                                final List<DriveResponse> mList = onResponse.files;
                                for (DriveResponse index : mList){
                                    categories.add(new MainCategories(Utils.getHexCode(index.name),index.id,index.name,R.drawable.face_1));
                                }
                                PrefsController.putString(getString(R.string.key_main_categories),new Gson().toJson(categories));
                            }
                            else{
                                PrefsController.putString(getString(R.string.key_main_categories),null);
                            }
                            final User mUser = User.getInstance().getUserInfo();
                            if (mUser!=null){
                               mUser.isRefresh = true;
                               PrefsController.putString(getString(R.string.key_user),new Gson().toJson(mUser));
                            }
                        }
                        view.onSuccessful(new Gson().toJson(onResponse));
                    }
                    Log.d(TAG, "Body list folder : " + new Gson().toJson(onResponse.files));
                }, throwable -> {
                    if (view==null){
                        Log.d(TAG,"View is null");
                        return;
                    }
                    if (throwable instanceof HttpException) {
                        ResponseBody bodys = ((HttpException) throwable).response().errorBody();
                        try {
                            Log.d(TAG,"error" +bodys.string());
                            String msg = new Gson().toJson(bodys.string());
                            Log.d(TAG, msg);
                            view.onError(""+msg);
                        } catch (IOException e) {
                            e.printStackTrace();
                            view.onError(""+e.getMessage());
                        }
                    } else {
                        Log.d(TAG, "Can not call " + throwable.getMessage());
                        view.onError("Error :"+ throwable.getMessage());
                    }
                    view.stopLoading();
                }));
    }


    public void onUploadFileInAppFolder(final File file,final String id){
        Log.d(TAG,"Upload File To In App Folder");
        final User mUser = User.getInstance().getUserInfo();
        MediaType contentType = MediaType.parse("application/json; charset=UTF-8");
        HashMap<String,Object> content = new HashMap<>();
        content.put(getString(R.string.key_name),file.getName());

        List<String> list = new ArrayList<>();
        list.add(id);
        content.put(getString(R.string.key_parents),list);

        MultipartBody.Part metaPart = MultipartBody.Part.create(RequestBody.create(contentType,new Gson().toJson(content)));

        Log.d(TAG,"parents: " +new Gson().toJson(content));

        ProgressRequestBody fileBody = new ProgressRequestBody(file, new ProgressRequestBody.UploadCallbacks() {
            @Override
            public void onProgressUpdate(int percentage) {
                Utils.Log(TAG,"Progressing "+ percentage +"%");
            }
            @Override
            public void onError() {
                Utils.Log(TAG,"onError");
            }
            @Override
            public void onFinish() {
                Utils.Log(TAG,"onFinish");
            }
        });

        fileBody.setContentType(Utils.getMimeType(file.getAbsolutePath()));
        MultipartBody.Part dataPart = MultipartBody.Part.create(fileBody);

        Call<DriveResponse> request = SupperSafeApplication.serverAPI.uploadFileMultipleInAppFolder(getString(R.string.url_drive_upload),mUser.access_token,metaPart,dataPart,Utils.getMimeType(file.getAbsolutePath()));
        request.enqueue(new Callback<DriveResponse>(){
            @Override
            public void onResponse(Call<DriveResponse> call, Response<DriveResponse> response) {
                Utils.Log(TAG,"response successful :"+ new Gson().toJson(response.body()));
            }
            @Override
            public void onFailure(Call<DriveResponse> call, Throwable t) {
                Utils.Log(TAG,"response failed :"+ t.getMessage());
            }
        });
    }



    public void onDownloadFile(String fileName,String pathOutput,final String id){
        DownloadFileRequest request = new DownloadFileRequest();
        request.api_name = String.format(getString(R.string.url_drive_download),id);
        request.file_name = fileName;
        request.path_folder_output  = pathOutput;
        final User mUser = User.getInstance().getUserInfo();
        request.Authorization = mUser.access_token;
        downloadService.onProgressingDownload(new DownloadService.DownLoadServiceListener() {
            @Override
            public void onDownLoadCompleted(File file_name, DownloadFileRequest request) {
                Utils.Log(TAG,"onDownLoadCompleted "+ file_name.getAbsolutePath());
            }

            @Override
            public void onDownLoadError(String error) {
                Utils.Log(TAG,"onDownLoadError "+ error);
            }

            @Override
            public void onProgressingDownloading(int percent) {
                Utils.Log(TAG,"Progressing "+ percent +"%");
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
                Utils.Log(TAG,"onSavedCompleted ");
            }

            @Override
            public void onErrorSave(String name) {
                Utils.Log(TAG,"onErrorSave");
            }

            @Override
            public void onCodeResponse(int code, DownloadFileRequest request) {

            }
        },"https://www.googleapis.com/drive/v3/files/");
        request.mapHeader = new HashMap<>();
        request.mapObject = new HashMap<>();
        downloadService.downloadDriveFileByGET(request);
    }



    public void getDriveAbout(SupperSafeServiceView view){
        if (view == null) {
            return;
        }
        if (NetworkUtil.pingIpAddress(SupperSafeApplication.getInstance())) {
            return;
        }
        if (subscriptions == null) {
            return;
        }
        final User user = User.getInstance().getUserInfo();
        if (user==null){
            return;
        }

        if (user.access_token==null){
            return;
        }

        String access_token = user.access_token;
        Log.d(TAG,"access_token : " + access_token);
        subscriptions.add(SupperSafeApplication.serverDriveApi.onGetDriveAbout(access_token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.startLoading())
                .subscribe(onResponse -> {
                    if (view==null){
                        Log.d(TAG,"View is null");
                        return;
                    }
                    view.stopLoading();
                    if (onResponse.error!=null){
                        view.onError(new Gson().toJson(onResponse.error));
                        final User mUser = User.getInstance().getUserInfo();
                        if (mUser!=null){
                            user.driveConnected = false;
                            PrefsController.putString(getString(R.string.key_user),new Gson().toJson(user));
                        }
                    }
                    else{
                        view.onSuccessful(new Gson().toJson(onResponse));
                        final User mUser = User.getInstance().getUserInfo();
                        if (mUser!=null){
                            user.driveConnected = true;
                            PrefsController.putString(getString(R.string.key_user),new Gson().toJson(user));
                        }
                    }
                    Log.d(TAG, "Body : " + new Gson().toJson(onResponse));
                }, throwable -> {
                    if (view==null){
                        Log.d(TAG,"View is null");
                        return;
                    }

                    if (throwable instanceof HttpException) {
                        ResponseBody bodys = ((HttpException) throwable).response().errorBody();
                        try {
                            Log.d(TAG,"error" +bodys.string());
                            String msg = new Gson().toJson(bodys.string());
                            Log.d(TAG, msg);
                            view.onError(""+msg);
                            final User mUser = User.getInstance().getUserInfo();
                            if (mUser!=null){
                                user.driveConnected = false;
                                PrefsController.putString(getString(R.string.key_user),new Gson().toJson(user));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            view.onError(""+e.getMessage());
                            final User mUser = User.getInstance().getUserInfo();
                            if (mUser!=null){
                                user.driveConnected = false;
                                PrefsController.putString(getString(R.string.key_user),new Gson().toJson(user));
                            }
                        }
                    } else {
                        Log.d(TAG, "Can not call " + throwable.getMessage());
                        view.onError("Error :"+ throwable.getMessage());
                        final User mUser = User.getInstance().getUserInfo();
                        if (mUser!=null){
                            user.driveConnected = false;
                            PrefsController.putString(getString(R.string.key_user),new Gson().toJson(user));
                        }
                    }
                    view.stopLoading();
                }));
    }


}
