package co.tpcreative.suppersafe.common.services;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.gson.Gson;
import com.snatik.storage.Storage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import co.tpcreative.suppersafe.R;
import co.tpcreative.suppersafe.common.api.request.DownloadFileRequest;
import co.tpcreative.suppersafe.common.controller.PrefsController;
import co.tpcreative.suppersafe.common.controller.ServiceManager;
import co.tpcreative.suppersafe.common.presenter.PresenterService;
import co.tpcreative.suppersafe.common.response.DriveResponse;
import co.tpcreative.suppersafe.common.services.download.DownloadService;
import co.tpcreative.suppersafe.common.services.upload.ProgressRequestBody;
import co.tpcreative.suppersafe.common.util.NetworkUtil;
import co.tpcreative.suppersafe.common.util.Utils;
import co.tpcreative.suppersafe.model.DriveDescription;
import co.tpcreative.suppersafe.model.EnumStatus;
import co.tpcreative.suppersafe.model.Items;
import co.tpcreative.suppersafe.model.User;
import co.tpcreative.suppersafe.model.room.InstanceGenerator;
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
    protected Storage storage;
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
        storage = new Storage(this);
        onInitReceiver();
        SupperSafeApplication.getInstance().setConnectivityListener(this);
    }

    public Storage getStorage() {
        return storage;
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
        Utils.Log(TAG,"Connected :" + isConnected);
        SupperSafeServiceView view = view();
        if (view!=null){
            view.onNetworkConnectionChanged(isConnected);
        }
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
                        view.onError(new Gson().toJson(onResponse.error),EnumStatus.GET_DRIVE_ABOUT);
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
                            view.onError(""+msg,EnumStatus.GET_DRIVE_ABOUT);
                        } catch (IOException e) {
                            e.printStackTrace();
                            view.onError(""+e.getMessage(),EnumStatus.GET_DRIVE_ABOUT);
                        }
                    } else {
                        Log.d(TAG, "Can not call " + throwable.getMessage());
                        view.onError("Error :"+ throwable.getMessage(),EnumStatus.GET_DRIVE_ABOUT);
                    }
                    view.stopLoading();
                }));
    }


    /*

    public void onCreateInAppFolder(final String folderName, final boolean isLoadList){
        Utils.Log(TAG,"onCreateInAppFolder");
        SupperSafeServiceView view = view();
        if (view == null) {
            final User mUser = User.getInstance().getUserInfo();
            if (mUser!=null){
                mUser.isInitMainCategoriesProgressing = false;
                PrefsController.putString(getString(R.string.key_user),new Gson().toJson(mUser));
            }
            return;
        }
        if (NetworkUtil.pingIpAddress(SupperSafeApplication.getInstance())) {
            view.onError("No connection",EnumStatus.CREATE_FOLDERS_IN_APP);
            return;
        }
        if (subscriptions == null) {
            view.onError("no subscriptions",EnumStatus.CREATE_FOLDERS_IN_APP);
            return;
        }
        final User user = User.getInstance().getUserInfo();
        if (user==null){
            view.onError("no user",EnumStatus.CREATE_FOLDERS_IN_APP);
            return;
        }

        if (user.access_token==null){
            view.onError("no access_token",EnumStatus.CREATE_FOLDERS_IN_APP);
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
                        final User mUser = User.getInstance().getUserInfo();
                        if (mUser!=null){
                            mUser.isInitMainCategoriesProgressing = false;
                            PrefsController.putString(getString(R.string.key_user),new Gson().toJson(mUser));
                        }
                        Log.d(TAG,"View is null");
                        return;
                    }
                    view.stopLoading();
                    if (onResponse.error!=null){
                        Log.d(TAG,"onError 1");
                        view.onError(new Gson().toJson(onResponse.error),EnumStatus.CREATE_FOLDERS_IN_APP);
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
                            view.onError(""+msg,EnumStatus.CREATE_FOLDERS_IN_APP);
                        } catch (IOException e) {
                            e.printStackTrace();
                            view.onError(""+e.getMessage(),EnumStatus.CREATE_FOLDERS_IN_APP);
                        }
                    } else {
                        Log.d(TAG, "Can not call " + throwable.getMessage());
                        view.onError("Error :"+ throwable.getMessage(),EnumStatus.CREATE_FOLDERS_IN_APP);
                    }
                    view.stopLoading();
                }));
    }

    */



    /*

    public void onCheckInAppFolderExisting(final String folderName,final boolean isLoadList){
        Utils.Log(TAG,"onCheckInAppFolderExisting");
        SupperSafeServiceView view = view();
        if (view == null) {
            final User mUser = User.getInstance().getUserInfo();
            if (mUser!=null){
                mUser.isInitMainCategoriesProgressing = false;
                PrefsController.putString(getString(R.string.key_user),new Gson().toJson(mUser));
            }
            return;
        }
        if (NetworkUtil.pingIpAddress(SupperSafeApplication.getInstance())) {
            view.onError("no connection",EnumStatus.CHECK_FOLDER_EXISTING);
            return;
        }
        if (subscriptions == null) {
            view.onError("no subscriptions",EnumStatus.CHECK_FOLDER_EXISTING);
            return;
        }
        final User user = User.getInstance().getUserInfo();
        if (user==null){
            view.onError("no user",EnumStatus.CHECK_FOLDER_EXISTING);
            return;
        }

        if (user.access_token==null){
            view.onError("no access_token",EnumStatus.CHECK_FOLDER_EXISTING);
            return;
        }
        if (!user.driveConnected){
            view.onError("no driveConnected",EnumStatus.CHECK_FOLDER_EXISTING);
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
                        final User mUser = User.getInstance().getUserInfo();
                        if (mUser!=null){
                            mUser.isInitMainCategoriesProgressing = false;
                            PrefsController.putString(getString(R.string.key_user),new Gson().toJson(mUser));
                        }
                        return;
                    }
                    view.stopLoading();
                    if (onResponse.error!=null){
                        Log.d(TAG,"onError 1");
                        view.onError(new Gson().toJson(onResponse.error),EnumStatus.CHECK_FOLDER_EXISTING);
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
                            view.onError(""+msg,EnumStatus.CHECK_FOLDER_EXISTING);
                        } catch (IOException e) {
                            e.printStackTrace();
                            view.onError(""+e.getMessage(),EnumStatus.CHECK_FOLDER_EXISTING);
                        }
                    } else {
                        Log.d(TAG, "Can not call " + throwable.getMessage());
                        view.onError("Error :"+ throwable.getMessage(),EnumStatus.CHECK_FOLDER_EXISTING);
                    }
                    view.stopLoading();
                }));
    }

    */


    /*

    public void onGetListFolderInApp(){
        Utils.Log(TAG,"onGetListFolderInApp");
        SupperSafeServiceView view = view();
        if (view == null) {
            final User mUser = User.getInstance().getUserInfo();
            if (mUser!=null){
                mUser.isInitMainCategoriesProgressing = false;
                PrefsController.putString(getString(R.string.key_user),new Gson().toJson(mUser));
            }
            return;
        }
        if (NetworkUtil.pingIpAddress(SupperSafeApplication.getInstance())) {
            view.onError("no connection",EnumStatus.GET_LIST_FOLDERS_IN_APP);
            return;
        }
        if (subscriptions == null) {
            view.onError("no subscriptions",EnumStatus.GET_LIST_FOLDERS_IN_APP);
            return;
        }
        final User user = User.getInstance().getUserInfo();
        if (user==null){
            view.onError("no user",EnumStatus.GET_LIST_FOLDERS_IN_APP);
            return;
        }

        if (user.access_token==null){
            view.onError("no access_token",EnumStatus.GET_LIST_FOLDERS_IN_APP);
            return;
        }

        if (!user.driveConnected){
            view.onError("no driveConnected",EnumStatus.GET_LIST_FOLDERS_IN_APP);
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
                        view.onError(new Gson().toJson(onResponse.error),EnumStatus.GET_LIST_FOLDERS_IN_APP);
                    }
                    else{
                        Log.d(TAG,"onSuccessful 2");
                        if (onResponse.files!=null){
                            if (onResponse.files.size()>0){
                                final List<MainCategories> categories = new ArrayList<>();
                                final List<DriveResponse> mList = onResponse.files;
                                final Map<String,MainCategories> hash = new HashMap<>();

                                for (DriveResponse index : mList){
                                    hash.put(index.name,new MainCategories(Utils.getHexCode(index.name),index.id,index.name,R.drawable.face_1));
                                }

                                for (Map.Entry<String,MainCategories> index : hash.entrySet()){
                                    categories.add(index.getValue());
                                }
                                PrefsController.putString(getString(R.string.key_main_categories),new Gson().toJson(categories));
                                Log.d(TAG,"Loop???????????????" + new Gson().toJson(categories));
                            }
                            else{
                                PrefsController.putString(getString(R.string.key_main_categories),null);
                            }
                            final User mUser = User.getInstance().getUserInfo();
                            if (mUser!=null){
                               mUser.isRefresh = true;
                               mUser.isInitMainCategoriesProgressing = false;
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
                            view.onError(""+msg,EnumStatus.GET_LIST_FOLDERS_IN_APP);
                        } catch (IOException e) {
                            e.printStackTrace();
                            view.onError(""+e.getMessage(),EnumStatus.GET_LIST_FOLDERS_IN_APP);
                        }
                    } else {
                        Log.d(TAG, "Can not call " + throwable.getMessage());
                        view.onError("Error :"+ throwable.getMessage(),EnumStatus.GET_LIST_FOLDERS_IN_APP);
                    }
                    view.stopLoading();
                }));
    }

   */

    public void onGetListOnlyFilesInApp(SupperSafeServiceView view){
        Utils.Log(TAG,"onGetListFolderInApp");
        if (view == null) {
            view.onError("no view", EnumStatus.GET_LIST_FILES_IN_APP);
            return;
        }
        if (NetworkUtil.pingIpAddress(SupperSafeApplication.getInstance())) {
            view.onError("no connection", EnumStatus.GET_LIST_FILES_IN_APP);
            return;
        }
        if (subscriptions == null) {
            view.onError("no subscriptions",EnumStatus.GET_LIST_FILES_IN_APP);
            return;
        }
        final User user = User.getInstance().getUserInfo();
        if (user==null){
            view.onError("no user",EnumStatus.GET_LIST_FILES_IN_APP);
            return;
        }

        if (user.access_token==null){
            view.onError("no access_token",EnumStatus.GET_LIST_FILES_IN_APP);
            return;
        }

        if (!user.driveConnected){
            view.onError("no driveConnected",EnumStatus.GET_LIST_FILES_IN_APP);
            return;
        }

        String access_token = user.access_token;
        Log.d(TAG,"access_token : " + access_token);
        subscriptions.add(SupperSafeApplication.serverDriveApi.onGetListFileInAppFolder(access_token,getString(R.string.key_mime_type_all_files), getString(R.string.key_appDataFolder),getString(R.string.key_specific_fields))
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
                        view.onError(new Gson().toJson(onResponse.error),EnumStatus.GET_LIST_FILES_IN_APP);
                    }
                    else{
                        try {
                            final List<Items> mListItem = InstanceGenerator.getInstance(SupperSafeApplication.getInstance()).getListItems();
                            Utils.Log(TAG, "Body list only files count " + onResponse.files.size() + "/" + mListItem.size());
                            final List<DriveResponse> driveResponse = onResponse.files;
                            for (DriveResponse index : driveResponse) {

                                final DriveDescription description = DriveDescription.getInstance().getDriveDescription(index.description);
                                Utils.Log(TAG,"response special "+ new Gson().toJson(description));
                                if (description != null) {
                                    final Items items = InstanceGenerator.getInstance(SupperSafeApplication.getInstance()).getItemId(index.name);
                                    if (items == null) {
                                        description.global_id = index.id;
                                        onSaveItem(description);
                                    } else {
                                        Log.d(TAG, "This item is existing");
                                    }
                                }
                            }
                            view.onSuccessful("Body list only files count " + onResponse.files.size() + "/" + mListItem.size());
                            view.onSuccessful(onResponse.files);
                        }
                        catch(Exception e){
                            view.onError(e.getMessage(),EnumStatus.GET_LIST_FILES_IN_APP);
                            e.printStackTrace();
                        };
                    }
                    Log.d(TAG, "Body list only files : " + new Gson().toJson(onResponse.files));
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
                            view.onError(""+msg,EnumStatus.GET_LIST_FILES_IN_APP);
                        } catch (IOException e) {
                            e.printStackTrace();
                            view.onError(""+e.getMessage(),EnumStatus.GET_LIST_FILES_IN_APP);
                        }
                    } else {
                        Log.d(TAG, "Can not call " + throwable.getMessage());
                        view.onError("Error :"+ throwable.getMessage(),EnumStatus.GET_LIST_FILES_IN_APP);
                    }
                    view.stopLoading();
                }));
    }

    public void onGetListOnlyFilesInApp(){
        Utils.Log(TAG,"onGetListFolderInApp");
        SupperSafeServiceView view = view();
        if (view == null) {
            view.onError("no view", EnumStatus.GET_LIST_FILES_IN_APP);
            return;
        }
        if (NetworkUtil.pingIpAddress(SupperSafeApplication.getInstance())) {
            view.onError("no connection", EnumStatus.GET_LIST_FILES_IN_APP);
            return;
        }
        if (subscriptions == null) {
            view.onError("no subscriptions",EnumStatus.GET_LIST_FILES_IN_APP);
            return;
        }
        final User user = User.getInstance().getUserInfo();
        if (user==null){
            view.onError("no user",EnumStatus.GET_LIST_FILES_IN_APP);
            return;
        }

        if (user.access_token==null){
            view.onError("no access_token",EnumStatus.GET_LIST_FILES_IN_APP);
            return;
        }

        if (!user.driveConnected){
            view.onError("no driveConnected",EnumStatus.GET_LIST_FILES_IN_APP);
            return;
        }

        String access_token = user.access_token;
        Log.d(TAG,"access_token : " + access_token);
        subscriptions.add(SupperSafeApplication.serverDriveApi.onGetListFileInAppFolder(access_token,getString(R.string.key_mime_type_all_files), getString(R.string.key_appDataFolder),getString(R.string.key_specific_fields))
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
                        view.onError(new Gson().toJson(onResponse.error),EnumStatus.GET_LIST_FILES_IN_APP);
                    }
                    else {
                        try {
                            final List<Items> mListItem = InstanceGenerator.getInstance(SupperSafeApplication.getInstance()).getListItems();
                            Utils.Log(TAG, "Body list only files count " + onResponse.files.size() + "/" + mListItem.size());
                            final List<DriveResponse> driveResponse = onResponse.files;
                            for (DriveResponse index : driveResponse) {
                                final DriveDescription description = new Gson().fromJson(index.description, DriveDescription.class);
                                if (description != null) {
                                    final Items items = InstanceGenerator.getInstance(SupperSafeApplication.getInstance()).getItemId(index.name);
                                    if (items == null) {
                                        onSaveItem(description);
                                    } else {
                                        Log.d(TAG, "This item is existing");
                                    }
                                }
                            }
                            view.onSuccessful("Body list only files count " + onResponse.files.size() + "/" + mListItem.size());

                        }
                        catch(Exception e){
                            view.onError(e.getMessage(),EnumStatus.GET_LIST_FILES_IN_APP);
                            e.printStackTrace();
                        }
                    }

                    Log.d(TAG, "Body list only files : " + new Gson().toJson(onResponse.files));
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
                            view.onError(""+msg,EnumStatus.GET_LIST_FILES_IN_APP);
                        } catch (IOException e) {
                            e.printStackTrace();
                            view.onError(""+e.getMessage(),EnumStatus.GET_LIST_FILES_IN_APP);
                        }
                    } else {
                        Log.d(TAG, "Can not call " + throwable.getMessage());
                        view.onError("Error :"+ throwable.getMessage(),EnumStatus.GET_LIST_FILES_IN_APP);
                    }
                    view.stopLoading();
                }));
    }

    public void onSaveItem(final DriveDescription description){
        Items items = new Items(false,
                description.degrees,
                description.fileType,
                description.name,
                description.globalName,
                description.thumbnailPath,
                description.originalPath ,
                description.subFolderName,
                description.global_id,
                description.localCategories_Id,
                description.mimeType,
                description.fileExtension,
                new Gson().toJson(description),
                EnumStatus.DOWNLOAD);
        InstanceGenerator.getInstance(SupperSafeApplication.getInstance()).onInsert(items);
    }


    public void onUploadFileInAppFolder(final File file,final String id,final String mimeType){
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

        fileBody.setContentType(mimeType);
        MultipartBody.Part dataPart = MultipartBody.Part.create(fileBody);

        Call<DriveResponse> request = SupperSafeApplication.serverAPI.uploadFileMultipleInAppFolder(getString(R.string.url_drive_upload),mUser.access_token,metaPart,dataPart,mimeType);
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

    public void onUploadFileInAppFolder(final Items items ,final ServiceManager.UploadServiceListener listener){
        Log.d(TAG,"Upload File To In App Folder !!!");


        final User mUser = User.getInstance().getUserInfo();
        MediaType contentType = MediaType.parse("application/json; charset=UTF-8");
        HashMap<String,Object> content = new HashMap<>();
        final File file = new File(items.originalPath);

        if (!storage.isFileExist(items.originalPath)){
            InstanceGenerator.getInstance(SupperSafeApplication.getInstance()).onDelete(items);
            return;
        }

        content.put(getString(R.string.key_name),items.globalName);
        content.put(getString(R.string.key_description),new Gson().toJson(items.description));
        List<String> list = new ArrayList<>();
        list.add(getString(R.string.key_appDataFolder));
        content.put(getString(R.string.key_parents),list);
        MultipartBody.Part metaPart = MultipartBody.Part.create(RequestBody.create(contentType,new Gson().toJson(content)));
        Log.d(TAG,"parents: " +new Gson().toJson(content));

        SupperSafeServiceView view = view();

        ProgressRequestBody fileBody = new ProgressRequestBody(file, new ProgressRequestBody.UploadCallbacks() {
            @Override
            public void onProgressUpdate(int percentage) {
                Utils.Log(TAG,"Progressing "+ percentage +"%");
                listener.onProgressUpdate(percentage);
            }
            @Override
            public void onError() {
                Utils.Log(TAG,"onError");
                if (view!=null){
                    view.onError("Error upload",EnumStatus.UPLOAD);
                }
                listener.onError();
            }
            @Override
            public void onFinish() {
                listener.onFinish();
                Utils.Log(TAG,"onFinish");
            }
        });

        fileBody.setContentType(items.mimeType);
        MultipartBody.Part dataPart = MultipartBody.Part.create(fileBody);

        Call<DriveResponse> request = SupperSafeApplication.serverAPI.uploadFileMultipleInAppFolder(getString(R.string.url_drive_upload),mUser.access_token,metaPart,dataPart,items.mimeType);
        request.enqueue(new Callback<DriveResponse>(){
            @Override
            public void onResponse(Call<DriveResponse> call, Response<DriveResponse> response) {
                Utils.Log(TAG,"response successful :"+ new Gson().toJson(response.body()));
                listener.onResponseData(response.body());
            }
            @Override
            public void onFailure(Call<DriveResponse> call, Throwable t) {
                Utils.Log(TAG,"response failed :"+ t.getMessage());
                if (view!=null){
                    view.onError("Error upload" + t.getMessage(),EnumStatus.UPLOAD);
                }
                listener.onFailure();
            }
        });
    }


    public void onDownloadFile(final DownloadFileRequest request,final ServiceManager.DownloadServiceListener listener){
        SupperSafeServiceView view = view();
        downloadService.onProgressingDownload(new DownloadService.DownLoadServiceListener() {
            @Override
            public void onDownLoadCompleted(File file_name, DownloadFileRequest request) {
                Utils.Log(TAG,"onDownLoadCompleted "+ file_name.getAbsolutePath());
                listener.onDownLoadCompleted(file_name,request);
            }

            @Override
            public void onDownLoadError(String error) {
                listener.onError(error);
                Utils.Log(TAG,"onDownLoadError "+ error);
                if (view!=null){
                    view.onError("Error download " + error,EnumStatus.DOWNLOAD);
                }
            }

            @Override
            public void onProgressingDownloading(int percent) {
                listener.onProgressDownload(percent);
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
                listener.onError(name);
                Utils.Log(TAG,"onErrorSave");
                if (view!=null){
                    view.onError("Error download save " + name,EnumStatus.DOWNLOAD);
                }
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

        if (!user.driveConnected){
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
                        view.onError(new Gson().toJson(onResponse.error),EnumStatus.GET_DRIVE_ABOUT);
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
                            view.onError(""+msg,EnumStatus.GET_DRIVE_ABOUT);
                            final User mUser = User.getInstance().getUserInfo();
                            if (mUser!=null){
                                user.driveConnected = false;
                                PrefsController.putString(getString(R.string.key_user),new Gson().toJson(user));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            view.onError(""+e.getMessage(),EnumStatus.GET_DRIVE_ABOUT);
                            final User mUser = User.getInstance().getUserInfo();
                            if (mUser!=null){
                                user.driveConnected = false;
                                PrefsController.putString(getString(R.string.key_user),new Gson().toJson(user));
                            }
                        }
                    } else {
                        Log.d(TAG, "Can not call " + throwable.getMessage());
                        view.onError("Error :"+ throwable.getMessage(),EnumStatus.GET_DRIVE_ABOUT);
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
