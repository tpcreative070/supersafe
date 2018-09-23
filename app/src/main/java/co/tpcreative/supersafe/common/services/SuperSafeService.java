package co.tpcreative.supersafe.common.services;

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
import java.util.Map;

import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.api.request.DownloadFileRequest;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.controller.ServiceManager;
import co.tpcreative.supersafe.common.presenter.PresenterService;
import co.tpcreative.supersafe.common.response.DriveResponse;
import co.tpcreative.supersafe.common.services.download.DownloadService;
import co.tpcreative.supersafe.common.services.upload.ProgressRequestBody;
import co.tpcreative.supersafe.common.util.NetworkUtil;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.DriveDescription;
import co.tpcreative.supersafe.model.DriveTitle;
import co.tpcreative.supersafe.model.EnumFileType;
import co.tpcreative.supersafe.model.EnumFormatType;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.Items;
import co.tpcreative.supersafe.model.MainCategories;
import co.tpcreative.supersafe.model.User;
import co.tpcreative.supersafe.model.room.InstanceGenerator;
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

public class SuperSafeService extends PresenterService<SuperSafeServiceView> implements SuperSafeReceiver.ConnectivityReceiverListener {

    private final IBinder mBinder = new LocalBinder(); // Binder given to clients
    private Intent mIntent;
    private SupperSafeServiceListener listener;
    private SuperSafeReceiver androidReceiver;
    private DownloadService downloadService;
    private DownloadService downloadServiceThumbnail;
    protected Storage storage;
    private static final String TAG = SuperSafeService.class.getSimpleName();

    public interface SupperSafeServiceListener {
        void onResponse(String message);

        void onConnectionChanged(boolean isChanged);

        void onMessageAction(String message);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        downloadService = new DownloadService(this);
        downloadServiceThumbnail = new DownloadService(this);
        storage = new Storage(this);
        onInitReceiver();
        SuperSafeApplication.getInstance().setConnectivityListener(this);
    }

    public Storage getStorage() {
        return storage;
    }

    public void onInitReceiver() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            androidReceiver = new SuperSafeReceiver();
            registerReceiver(androidReceiver, intentFilter);
            SuperSafeApplication.getInstance().setConnectivityListener(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        if (androidReceiver != null) {
            unregisterReceiver(androidReceiver);
        }
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        Utils.Log(TAG, "Connected :" + isConnected);
        SuperSafeServiceView view = view();
        if (view != null) {
            view.onNetworkConnectionChanged(isConnected);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // If we get killed, after returning from here, restart
        Log.d(TAG, "onStartCommand");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Bundle extras = intent.getExtras();
        Log.d(TAG, "onBind");
        // Get messager from the Activity
        if (extras != null) {
            Log.d("service", "onBind with extra");
        }
        return mBinder;
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */

    public class LocalBinder extends Binder {
        public SuperSafeService getService() {
            // Return this instance of SignalRService so clients can call public methods
            return SuperSafeService.this;
        }

        public void setIntent(Intent intent) {
            mIntent = intent;
        }

        public void setListener(SupperSafeServiceListener mListener) {
            listener = mListener;
        }
    }

    /*Network request*/

    public void getDriveAbout() {
        SuperSafeServiceView view = view();
        if (view == null) {
            return;
        }
        if (NetworkUtil.pingIpAddress(SuperSafeApplication.getInstance())) {
            return;
        }
        if (subscriptions == null) {
            return;
        }
        final User user = User.getInstance().getUserInfo();
        if (user == null) {
            return;
        }

        if (user.access_token == null) {
            return;
        }

        String access_token = user.access_token;
        Log.d(TAG, "access_token : " + access_token);
        view.onSuccessful(access_token,EnumStatus.GET_DRIVE_ABOUT);
        subscriptions.add(SuperSafeApplication.serverDriveApi.onGetDriveAbout(access_token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.startLoading())
                .subscribe(onResponse -> {
                    if (view == null) {
                        Log.d(TAG, "View is null");
                        return;
                    }
                    view.stopLoading();
                    if (onResponse.error != null) {
                        Log.d(TAG, "onError 1");
                        view.onError(new Gson().toJson(onResponse.error), EnumStatus.REQUEST_ACCESS_TOKEN);
                    } else {
                        Log.d(TAG, "onSuccessful 2");
                        final User mUser = User.getInstance().getUserInfo();
                        mUser.driveAbout = onResponse;
                        PrefsController.putString(getString(R.string.key_user), new Gson().toJson(mUser));
                        view.onSuccessful(new Gson().toJson(onResponse),EnumStatus.GET_DRIVE_ABOUT);
                    }
                    Log.d(TAG, "Body : " + new Gson().toJson(onResponse));
                }, throwable -> {
                    if (view == null) {
                        Log.d(TAG, "View is null");
                        return;
                    }

                    if (throwable instanceof HttpException) {
                        ResponseBody bodys = ((HttpException) throwable).response().errorBody();
                        try {
                            Log.d(TAG, "error" + bodys.string());
                            String msg = new Gson().toJson(bodys.string());
                            Log.d(TAG, msg);
                            view.onError("" + msg, EnumStatus.GET_DRIVE_ABOUT);
                        } catch (IOException e) {
                            e.printStackTrace();
                            view.onError("" + e.getMessage(), EnumStatus.GET_DRIVE_ABOUT);
                        }
                    } else {
                        Log.d(TAG, "Can not call " + throwable.getMessage());
                        view.onError("Error :" + throwable.getMessage(), EnumStatus.GET_DRIVE_ABOUT);
                    }
                    view.stopLoading();
                }));
    }


    public void onAddItems(final Items mItem, SuperSafeServiceView view) {
        final  Items items = mItem;
        Utils.Log(TAG, "onGetListFolderInApp");
        if (view == null) {
            view.onError("no view", EnumStatus.ADD_ITEMS);
            return;
        }
        if (NetworkUtil.pingIpAddress(SuperSafeApplication.getInstance())) {
            view.onError("no connection", EnumStatus.ADD_ITEMS);
            return;
        }
        if (subscriptions == null) {
            view.onError("no subscriptions", EnumStatus.ADD_ITEMS);
            return;
        }
        final User user = User.getInstance().getUserInfo();
        if (user == null) {
            view.onError("no user", EnumStatus.ADD_ITEMS);
            return;
        }

        if (!user.driveConnected){
            view.onError("No Drive connected", EnumStatus.REQUEST_ACCESS_TOKEN);
            return;
        }


       // Map<String, Object> hashMap = new HashMap<>();

        final Map<String,Object> hashMap = Items.getInstance().objectToHashMap(items);
        if (hashMap!=null){
            hashMap.put(getString(R.string.key_user_id), user.email);
            hashMap.put(getString(R.string.key_cloud_id),user.cloud_id);
            hashMap.put(getString(R.string.key_kind), getString(R.string.key_drive_file));
            DriveTitle contentTitle = new DriveTitle();
            contentTitle.globalName = items.items_id;
            String hex = DriveTitle.getInstance().convertToHex(new Gson().toJson(contentTitle));
            hashMap.put(getString(R.string.key_name), hex);
        }


        String access_token = user.access_token;
        view.onSuccessful("access_token" + getString(R.string.access_token, access_token));
        Log.d(TAG, "access_token : " + access_token);
        subscriptions.add(SuperSafeApplication.serverAPI.onSyncData(hashMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.startLoading())
                .subscribe(onResponse -> {
                    if (view == null) {
                        Log.d(TAG, "View is null");
                        return;
                    }
                    view.stopLoading();
                    if (onResponse.error) {
                        Log.d(TAG, "onError:" + new Gson().toJson(onResponse));
                        view.onError("Queries add items is failed :" + onResponse.message, EnumStatus.ADD_ITEMS);
                    } else {
                        view.onSuccessful("Status Items :" + onResponse.message, EnumStatus.ADD_ITEMS);
                    }
                }, throwable -> {
                    if (view == null) {
                        Log.d(TAG, "View is null");
                        return;
                    }
                    if (throwable instanceof HttpException) {
                        ResponseBody bodys = ((HttpException) throwable).response().errorBody();
                        try {
                            Log.d(TAG, "error" + bodys.string());
                            String msg = new Gson().toJson(bodys.string());
                            Log.d(TAG, msg);
                            view.onError("" + msg, EnumStatus.ADD_ITEMS);
                        } catch (IOException e) {
                            e.printStackTrace();
                            view.onError("" + e.getMessage(), EnumStatus.ADD_ITEMS);
                        }
                    } else {
                        Log.d(TAG, "Can not call " + throwable.getMessage());
                        view.onError("Error :" + throwable.getMessage(), EnumStatus.ADD_ITEMS);
                    }
                    view.stopLoading();
                }));

    }

    public void onCheckingMissData(String nextPage, SuperSafeServiceView view) {
        Utils.Log(TAG, "onGetListSync");
        if (view == null) {
            view.onError("no view", EnumStatus.GET_LIST_FILE);
            return;
        }
        if (NetworkUtil.pingIpAddress(SuperSafeApplication.getInstance())) {
            view.onError("no connection", EnumStatus.GET_LIST_FILE);
            return;
        }
        if (subscriptions == null) {
            view.onError("no subscriptions", EnumStatus.GET_LIST_FILE);
            return;
        }
        final User user = User.getInstance().getUserInfo();
        if (user == null) {
            view.onError("no user", EnumStatus.GET_LIST_FILE);
            return;
        }

        if (user.access_token == null) {
            view.onError("no access_token", EnumStatus.GET_LIST_FILES_IN_APP);
            return;
        }

        if (!user.driveConnected) {
            view.onError("no driveConnected", EnumStatus.REQUEST_ACCESS_TOKEN);
            return;
        }

        Map<String, Object> hashMap = new HashMap<>();
        hashMap.put(getString(R.string.key_user_id), user.email);
        hashMap.put(getString(R.string.key_cloud_id),user.cloud_id);
        hashMap.put(getString(R.string.key_next_page), nextPage);
        hashMap.put(getString(R.string.key_isSync), true);

        String access_token = user.access_token;
        view.onSuccessful("access_token" + getString(R.string.access_token, access_token));
        Log.d(TAG, "access_token : " + access_token);
        subscriptions.add(SuperSafeApplication.serverAPI.onListFilesSync(hashMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.startLoading())
                .subscribe(onResponse -> {
                    if (view == null) {
                        Log.d(TAG, "View is null");
                        view.onError("View is null", EnumStatus.GET_LIST_FILE);
                        return;
                    }
                    view.stopLoading();
                    if (onResponse.error) {
                        Log.d(TAG, "onError 1");
                        view.onError(onResponse.message, EnumStatus.GET_LIST_FILE);
                    } else {

                        Map<String,Items> hash = new HashMap<>();
                        final List<Items> list = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getListItemId(true);

                        if (nextPage.equals("0") && onResponse.nextPage==null && list!=null){
                            view.onSuccessfulOnCheck(list);
                        }

                        if (onResponse.nextPage == null) {
                            Log.d(TAG, "Ready for sync");
                            final List<Items> mList = new ArrayList<>();
                            for (Map.Entry<String,Items> map : hash.entrySet()){
                                mList.add(map.getValue());
                            }
                            view.onSuccessfulOnCheck(mList);
                        } else {
                            for (DriveResponse index : onResponse.files) {
                                DriveTitle driveTitle = DriveTitle.getInstance().hexToObject(index.name);
                                try {
                                    if (driveTitle != null) {
                                        final Items items = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getItemId(driveTitle.globalName,true);
                                        if (items==null){
                                            hash.put(driveTitle.globalName,items);
                                        }
                                        else{
                                            hash.remove(driveTitle.globalName);
                                        }
                                    } else {
                                        view.onError("Drive title is null", EnumStatus.GET_LIST_FILE);
                                    }
                                } catch (Exception e) {
                                    e.getMessage();
                                }
                            }
                            view.onSuccessful(onResponse.nextPage,EnumStatus.LOAD_MORE);
                        }
                    }
                }, throwable -> {
                    if (view == null) {
                        Log.d(TAG, "View is null");
                        view.onError("View is null", EnumStatus.GET_LIST_FILE);
                        return;
                    }
                    if (throwable instanceof HttpException) {
                        ResponseBody bodys = ((HttpException) throwable).response().errorBody();
                        try {
                            Log.d(TAG, "error" + bodys.string());
                            String msg = new Gson().toJson(bodys.string());
                            Log.d(TAG, msg);
                            view.onError("" + msg, EnumStatus.GET_LIST_FILE);
                        } catch (IOException e) {
                            e.printStackTrace();
                            view.onError("" + e.getMessage(), EnumStatus.GET_LIST_FILE);
                        }
                    } else {
                        Log.d(TAG, "Can not call " + throwable.getMessage());
                        view.onError("Error :" + throwable.getMessage(), EnumStatus.GET_LIST_FILE);
                    }
                    view.stopLoading();
                }));
    }


    public void onGetListSync(String nextPage, SuperSafeServiceView view) {
        Utils.Log(TAG, "onGetListSync");
        if (view == null) {
            view.onError("no view", EnumStatus.GET_LIST_FILE);
            return;
        }
        if (NetworkUtil.pingIpAddress(SuperSafeApplication.getInstance())) {
            view.onError("no connection", EnumStatus.GET_LIST_FILE);
            return;
        }
        if (subscriptions == null) {
            view.onError("no subscriptions", EnumStatus.GET_LIST_FILE);
            return;
        }
        final User user = User.getInstance().getUserInfo();
        if (user == null) {
            view.onError("no user", EnumStatus.GET_LIST_FILE);
            return;
        }

        if (user.access_token == null) {
            view.onError("no access_token", EnumStatus.GET_LIST_FILES_IN_APP);
            return;
        }

        if (!user.driveConnected) {
            view.onError("no driveConnected", EnumStatus.REQUEST_ACCESS_TOKEN);
            return;
        }

        Map<String, Object> hashMap = new HashMap<>();
        hashMap.put(getString(R.string.key_cloud_id),user.cloud_id);
        hashMap.put(getString(R.string.key_user_id), user.email);
        hashMap.put(getString(R.string.key_next_page), nextPage);
        hashMap.put(getString(R.string.key_isSync), true);

        String access_token = user.access_token;
        view.onSuccessful("access_token" + getString(R.string.access_token, access_token));
        Log.d(TAG, "access_token : " + access_token);
        subscriptions.add(SuperSafeApplication.serverAPI.onListFilesSync(hashMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.startLoading())
                .subscribe(onResponse -> {
                    if (view == null) {
                        Log.d(TAG, "View is null");
                        view.onError("View is null", EnumStatus.GET_LIST_FILE);
                        return;
                    }
                    view.stopLoading();
                    if (onResponse.error) {
                        Log.d(TAG, "onError 1");
                        view.onError(onResponse.message, EnumStatus.GET_LIST_FILE);
                    } else {
                        if (onResponse.nextPage == null) {
                            Log.d(TAG, "Ready for sync");
                            view.onSuccessful(onResponse.nextPage, EnumStatus.SYNC_READY);
                            view.onSuccessful(onResponse.message);
                        } else {
                            try {
                                Map<String, MainCategories> hash = MainCategories.getInstance().getMainCategoriesHashList();
                                final List<DriveResponse> driveResponse = onResponse.files;
                                for (DriveResponse index : driveResponse) {
                                    final DriveDescription description = DriveDescription.getInstance().hexToObject(index.description);
                                    if (description != null) {
                                        if (hash != null) {
                                            final MainCategories result = hash.get(description.localCategories_Id);
                                            if (result == null) {
                                                view.onSuccessful("Add new categories name " + description.localCategories_Name);
                                                MainCategories.getInstance().onAddCategories(description.localCategories_Id, description.localCategories_Name, description.localCategories_Count);
                                                hash = MainCategories.getInstance().getMainCategoriesHashList();
                                            }
                                        }

                                        DriveTitle driveTitle = DriveTitle.getInstance().hexToObject(index.name);
                                        if (driveTitle != null) {
                                            final Items items = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getItemId(driveTitle.globalName);
                                            EnumFormatType formatTypeFile = EnumFormatType.values()[description.formatType];
                                            if (items == null) {
                                                description.global_original_id = index.global_original_id;
                                                description.global_thumbnail_id = index.global_thumbnail_id;
                                                switch (formatTypeFile) {
                                                    case AUDIO: {
                                                        description.thumbnailSync = true;
                                                        break;
                                                    }
                                                    default: {
                                                        description.originalSync = false;
                                                        description.thumbnailSync = false;
                                                        break;
                                                    }
                                                }
                                                onSaveItem(description);
                                            } else {
                                                items.global_original_id = index.global_original_id;
                                                items.global_thumbnail_id = index.global_thumbnail_id;
                                                InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onUpdate(items);
                                                Log.d(TAG, "This item is existing");
                                            }
                                        } else {
                                            view.onError("Can not convert item", EnumStatus.GET_LIST_FILE);
                                            Log.d(TAG, "Can not convert item");
                                        }
                                    } else {
                                        view.onError("Description item is null", EnumStatus.GET_LIST_FILE);
                                        Utils.Log(TAG, "Description item is null");
                                    }
                                }
                            } catch (Exception e) {
                                view.onError(e.getMessage(), EnumStatus.GET_LIST_FILE);
                                e.printStackTrace();
                            }
                            Log.d(TAG, "Load more");
                            view.onSuccessful(onResponse.nextPage, EnumStatus.LOAD_MORE);
                        }
                    }
                }, throwable -> {
                    if (view == null) {
                        Log.d(TAG, "View is null");
                        view.onError("View is null", EnumStatus.GET_LIST_FILE);
                        return;
                    }
                    if (throwable instanceof HttpException) {
                        ResponseBody bodys = ((HttpException) throwable).response().errorBody();
                        try {
                            Log.d(TAG, "error" + bodys.string());
                            String msg = new Gson().toJson(bodys.string());
                            Log.d(TAG, msg);
                            view.onError("" + msg, EnumStatus.GET_LIST_FILE);
                        } catch (IOException e) {
                            e.printStackTrace();
                            view.onError("" + e.getMessage(), EnumStatus.GET_LIST_FILE);
                        }
                    } else {
                        Log.d(TAG, "Can not call " + throwable.getMessage());
                        view.onError("Error :" + throwable.getMessage(), EnumStatus.GET_LIST_FILE);
                    }
                    view.stopLoading();
                }));
    }


    public void onGetListOnlyFilesInApp(SuperSafeServiceView view) {
        Utils.Log(TAG, "onGetListFolderInApp");
        if (view == null) {
            view.onError("no view", EnumStatus.GET_LIST_FILES_IN_APP);
            return;
        }
        if (NetworkUtil.pingIpAddress(SuperSafeApplication.getInstance())) {
            view.onError("no connection", EnumStatus.GET_LIST_FILES_IN_APP);
            return;
        }
        if (subscriptions == null) {
            view.onError("no subscriptions", EnumStatus.GET_LIST_FILES_IN_APP);
            return;
        }
        final User user = User.getInstance().getUserInfo();
        if (user == null) {
            view.onError("no user", EnumStatus.GET_LIST_FILES_IN_APP);
            return;
        }

        if (user.access_token == null) {
            view.onError("no access_token", EnumStatus.GET_LIST_FILES_IN_APP);
            return;
        }

        if (!user.driveConnected) {
            view.onError("no driveConnected", EnumStatus.GET_LIST_FILES_IN_APP);
            return;
        }

        String access_token = user.access_token;
        view.onSuccessful("access_token" + getString(R.string.access_token, access_token));
        Log.d(TAG, "access_token : " + access_token);
        subscriptions.add(SuperSafeApplication.serverDriveApi.onGetListFileInAppFolder(access_token, getString(R.string.key_mime_type_all_files), getString(R.string.key_appDataFolder), getString(R.string.key_specific_fields))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.startLoading())
                .subscribe(onResponse -> {
                    if (view == null) {
                        Log.d(TAG, "View is null");
                        return;
                    }
                    view.stopLoading();
                    if (onResponse.error != null) {
                        Log.d(TAG, "onError 1");
                        view.onError(new Gson().toJson(onResponse.error), EnumStatus.REQUEST_ACCESS_TOKEN);
                    } else {
                        try {
                            Map<String, MainCategories> hash = MainCategories.getInstance().getMainCategoriesHashList();
                            final List<DriveResponse> driveResponse = onResponse.files;
                            for (DriveResponse index : driveResponse) {

                                final DriveDescription description = DriveDescription.getInstance().hexToObject(index.description);
                                if (description != null) {
                                    if (hash != null) {
                                        final MainCategories result = hash.get(description.localCategories_Id);
                                        if (result == null) {
                                            view.onSuccessful("Add new categories name " + description.localCategories_Name);
                                            MainCategories.getInstance().onAddCategories(description.localCategories_Id, description.localCategories_Name, description.localCategories_Count);
                                            hash = MainCategories.getInstance().getMainCategoriesHashList();
                                        }
                                    }

                                    DriveTitle driveTitle = DriveTitle.getInstance().hexToObject(index.name);
                                    if (driveTitle != null) {
                                        final Items items = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getItemId(driveTitle.globalName);
                                        EnumFormatType formatTypeFile = EnumFormatType.values()[description.formatType];
                                        EnumFileType enumTypeFile = EnumFileType.values()[driveTitle.fileType];
                                        if (items == null) {
                                            switch (enumTypeFile) {
                                                case ORIGINAL: {
                                                    description.global_original_id = index.id;
                                                    break;
                                                }
                                                case THUMBNAIL: {
                                                    description.global_thumbnail_id = index.id;
                                                    break;
                                                }
                                            }

                                            switch (formatTypeFile) {
                                                case AUDIO: {
                                                    description.thumbnailSync = true;
                                                    break;
                                                }
                                                default: {
                                                    description.thumbnailSync = false;
                                                    break;
                                                }
                                            }

                                            String message = "This item is new :" + description.globalName + " - " + index.id;
                                            Log.d(TAG, message);
                                            view.onSuccessful(message);
                                            onSaveItem(description);
//
                                        } else {
                                            switch (enumTypeFile) {
                                                case ORIGINAL: {
                                                    items.global_original_id = index.id;
                                                    break;
                                                }
                                                case THUMBNAIL: {
                                                    items.global_thumbnail_id = index.id;
                                                    break;
                                                }
                                            }

                                            String message = "This item is existing:" + description.globalName + " - " + index.id;
                                            if (description.globalName.equals("87cdfa94-5378-4a20-86a8-71bf22c26822")) {
                                                view.onSuccessful(message + " --- " + index.name);
                                            }
                                            if (description.globalName.equals("0634714f-5687-4302-8cf9-e6ec39120119")) {
                                                view.onSuccessful(message + " --- " + index.name);
                                            }

                                            InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onUpdate(items);
                                            Log.d(TAG, "This item is existing");

                                        }
                                    } else {
                                        view.onError("Can not convert item", EnumStatus.GET_LIST_FILES_IN_APP);
                                        Log.d(TAG, "Can not convert item");
                                    }
                                } else {
                                    view.onError("Description item is null", EnumStatus.GET_LIST_FILES_IN_APP);
                                    Utils.Log(TAG, "Description item is null");
                                }
                            }

                            final List<Items> mList = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getListItemId(true);
                            int count = 0;
                            if (mList != null) {
                                count = mList.size();
                            } else {
                                count = 0;
                            }
                            view.onSuccessful("-------------Sync data------------" + onResponse.files.size() + "/" + count);
                            view.onSuccessful(onResponse.files);
                        } catch (Exception e) {
                            view.onError(e.getMessage(), EnumStatus.GET_LIST_FILES_IN_APP);
                            e.printStackTrace();
                        }
                        ;
                    }
                    Log.d(TAG, "Body list only files : " + new Gson().toJson(onResponse.files));
                }, throwable -> {
                    if (view == null) {
                        Log.d(TAG, "View is null");
                        return;
                    }
                    if (throwable instanceof HttpException) {
                        ResponseBody bodys = ((HttpException) throwable).response().errorBody();
                        try {
                            Log.d(TAG, "error" + bodys.string());
                            String msg = new Gson().toJson(bodys.string());
                            Log.d(TAG, msg);
                            view.onError("" + msg, EnumStatus.GET_LIST_FILES_IN_APP);
                        } catch (IOException e) {
                            e.printStackTrace();
                            view.onError("" + e.getMessage(), EnumStatus.GET_LIST_FILES_IN_APP);
                        }
                    } else {
                        Log.d(TAG, "Can not call " + throwable.getMessage());
                        view.onError("Error :" + throwable.getMessage(), EnumStatus.GET_LIST_FILES_IN_APP);
                    }
                    view.stopLoading();
                }));
    }


    public void onSaveItem(final DriveDescription description) {
        Items items = new Items(false,
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
                description.subFolderName,
                description.global_original_id,
                description.global_thumbnail_id,
                description.localCategories_Id,
                description.localCategories_Name,
                description.localCategories_Count,
                description.mimeType,
                description.fileExtension,
                DriveDescription.getInstance().convertToHex(new Gson().toJson(description)),
                EnumStatus.DOWNLOAD,
                description.size,
                description.statusProgress,
                description.isDeleteLocal,
                description.isDeleteGlobal);
        InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onInsert(items);
    }


    public void onUploadFileInAppFolder(final Items items, final ServiceManager.UploadServiceListener listener) {
        Log.d(TAG, "Upload File To In App Folder !!!");
        final User mUser = User.getInstance().getUserInfo();
        MediaType contentType = MediaType.parse("application/json; charset=UTF-8");
        HashMap<String, Object> content = new HashMap<>();
        final File file = new File(items.originalPath);

        if (!storage.isFileExist(items.originalPath)) {
            InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onDelete(items);
            listener.onError("This original is not found", EnumStatus.UPLOAD);
            return;
        }

        if (items.originalSync) {
            listener.onError("This original already synced", EnumStatus.UPLOAD);
            return;
        }


        DriveTitle contentTitle = new DriveTitle();
        contentTitle.globalName = items.items_id;
        contentTitle.fileType = EnumFileType.ORIGINAL.ordinal();
        String hex = DriveTitle.getInstance().convertToHex(new Gson().toJson(contentTitle));
        content.put(getString(R.string.key_name), hex);
        content.put(getString(R.string.key_description), items.description);
        List<String> list = new ArrayList<>();
        list.add(getString(R.string.key_appDataFolder));
        content.put(getString(R.string.key_parents), list);
        MultipartBody.Part metaPart = MultipartBody.Part.create(RequestBody.create(contentType, new Gson().toJson(content)));
        Log.d(TAG, "parents: " + new Gson().toJson(content));

        ProgressRequestBody fileBody = new ProgressRequestBody(file, new ProgressRequestBody.UploadCallbacks() {
            @Override
            public void onProgressUpdate(int percentage) {
                Utils.Log(TAG, "Progressing " + percentage + "%");
                listener.onProgressUpdate(percentage);
            }

            @Override
            public void onError() {
                Utils.Log(TAG, "onError");
                if (listener != null) {
                    listener.onError("Error upload", EnumStatus.UPLOAD);
                }
            }

            @Override
            public void onFinish() {
                listener.onFinish();
                Utils.Log(TAG, "onFinish");
            }
        });

        fileBody.setContentType(items.mimeType);
        MultipartBody.Part dataPart = MultipartBody.Part.create(fileBody);

        Call<DriveResponse> request = SuperSafeApplication.serverAPI.uploadFileMultipleInAppFolder(getString(R.string.url_drive_upload), mUser.access_token, metaPart, dataPart, items.mimeType);
        request.enqueue(new Callback<DriveResponse>() {
            @Override
            public void onResponse(Call<DriveResponse> call, Response<DriveResponse> response) {
                Utils.Log(TAG, "response successful :" + new Gson().toJson(response.body()));
                listener.onResponseData(response.body());
            }

            @Override
            public void onFailure(Call<DriveResponse> call, Throwable t) {
                Utils.Log(TAG, "response failed :" + t.getMessage());
                if (listener != null) {
                    listener.onError("Error upload" + t.getMessage(), EnumStatus.UPLOAD);
                }
            }
        });
    }


    public void onUploadThumbnailFileInAppFolder(final Items items, final ServiceManager.UploadServiceListener listener) {
        Log.d(TAG, "Upload File To In App Folder !!!");
        final User mUser = User.getInstance().getUserInfo();
        MediaType contentType = MediaType.parse("application/json; charset=UTF-8");
        HashMap<String, Object> content = new HashMap<>();
        final File file = new File(items.thumbnailPath);

        if (!storage.isFileExist(items.thumbnailPath)) {
            InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onDelete(items);
            listener.onError("This thumbnail is not found", EnumStatus.UPLOAD);
            return;
        }

        if (items.thumbnailSync) {
            listener.onError("This thumbnail already synced", EnumStatus.UPLOAD);
            return;
        }

        DriveTitle contentTitle = new DriveTitle();
        contentTitle.globalName = items.items_id;
        contentTitle.fileType = EnumFileType.THUMBNAIL.ordinal();
        String hex = DriveTitle.getInstance().convertToHex(new Gson().toJson(contentTitle));
        content.put(getString(R.string.key_name), hex);
        content.put(getString(R.string.key_description), items.description);
        List<String> list = new ArrayList<>();
        list.add(getString(R.string.key_appDataFolder));
        content.put(getString(R.string.key_parents), list);
        MultipartBody.Part metaPart = MultipartBody.Part.create(RequestBody.create(contentType, new Gson().toJson(content)));
        Log.d(TAG, "parents: " + new Gson().toJson(content));


        ProgressRequestBody fileBody = new ProgressRequestBody(file, new ProgressRequestBody.UploadCallbacks() {
            @Override
            public void onProgressUpdate(int percentage) {
                Utils.Log(TAG, "Progressing " + percentage + "%");
                listener.onProgressUpdate(percentage);
            }

            @Override
            public void onError() {
                Utils.Log(TAG, "onError");
                if (listener != null) {
                    listener.onError("Error upload", EnumStatus.UPLOAD);
                }
            }

            @Override
            public void onFinish() {
                listener.onFinish();
                Utils.Log(TAG, "onFinish");
            }
        });

        fileBody.setContentType(items.mimeType);
        MultipartBody.Part dataPart = MultipartBody.Part.create(fileBody);

        Call<DriveResponse> request = SuperSafeApplication.serverAPI.uploadFileMultipleInAppFolder(getString(R.string.url_drive_upload), mUser.access_token, metaPart, dataPart, items.mimeType);
        request.enqueue(new Callback<DriveResponse>() {
            @Override
            public void onResponse(Call<DriveResponse> call, Response<DriveResponse> response) {
                Utils.Log(TAG, "response successful :" + new Gson().toJson(response.body()));
                listener.onResponseData(response.body());
            }

            @Override
            public void onFailure(Call<DriveResponse> call, Throwable t) {
                Utils.Log(TAG, "response failed :" + t.getMessage());
                if (listener != null) {
                    listener.onError("Error upload" + t.getMessage(), EnumStatus.UPLOAD);
                }
            }
        });
    }


    public void onDownloadFile(final DownloadFileRequest request, final ServiceManager.DownloadServiceListener listener) {
        Utils.Log(TAG, "onDownloadFile !!!!");
        downloadService.onProgressingDownload(new DownloadService.DownLoadServiceListener() {
            @Override
            public void onDownLoadCompleted(File file_name, DownloadFileRequest request) {
                Utils.Log(TAG, "onDownLoadCompleted " + file_name.getAbsolutePath());
                listener.onDownLoadCompleted(file_name, request);
            }

            @Override
            public void onDownLoadError(String error) {
                Utils.Log(TAG, "onDownLoadError " + error);
                if (listener != null) {
                    listener.onError("Error download " + error, EnumStatus.DOWNLOAD);
                }
            }

            @Override
            public void onProgressingDownloading(int percent) {
                listener.onProgressDownload(percent);
                Utils.Log(TAG, "Progressing " + percent + "%");
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
                Utils.Log(TAG, "onSavedCompleted ");
            }

            @Override
            public void onErrorSave(String name) {
                Utils.Log(TAG, "onErrorSave");
                if (listener != null) {
                    listener.onError("Error download save " + name, EnumStatus.DOWNLOAD);
                }
            }

            @Override
            public void onCodeResponse(int code, DownloadFileRequest request) {

            }
        }, getString(R.string.drive_api));
        request.mapHeader = new HashMap<>();
        request.mapObject = new HashMap<>();
        downloadService.downloadDriveFileByGET(request);
    }

    public void onDownloadThumbnailFile(final DownloadFileRequest request, final ServiceManager.DownloadServiceListener listener) {
        downloadServiceThumbnail.onProgressingDownload(new DownloadService.DownLoadServiceListener() {
            @Override
            public void onDownLoadCompleted(File file_name, DownloadFileRequest request) {
                Utils.Log(TAG, "onDownLoadCompleted " + file_name.getAbsolutePath());
                listener.onDownLoadCompleted(file_name, request);
            }

            @Override
            public void onDownLoadError(String error) {
                Utils.Log(TAG, "onDownLoadError " + error);
                if (listener != null) {
                    listener.onError("Error download " + error, EnumStatus.DOWNLOAD);
                }
            }

            @Override
            public void onProgressingDownloading(int percent) {
                listener.onProgressDownload(percent);
                Utils.Log(TAG, "Progressing " + percent + "%");
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
                Utils.Log(TAG, "onSavedCompleted ");
            }

            @Override
            public void onErrorSave(String name) {
                Utils.Log(TAG, "onErrorSave");
                if (listener != null) {
                    listener.onError("Error download save " + name, EnumStatus.DOWNLOAD);
                }
            }

            @Override
            public void onCodeResponse(int code, DownloadFileRequest request) {

            }
        }, "https://www.googleapis.com/drive/v3/files/");
        request.mapHeader = new HashMap<>();
        request.mapObject = new HashMap<>();
        downloadServiceThumbnail.downloadDriveFileByGET(request);
    }


    public void getDriveAbout(SuperSafeServiceView view) {
        if (view == null) {
            return;
        }
        if (NetworkUtil.pingIpAddress(SuperSafeApplication.getInstance())) {
            return;
        }
        if (subscriptions == null) {
            return;
        }
        final User user = User.getInstance().getUserInfo();
        if (user == null) {
            return;
        }

        if (user.access_token == null) {
            return;
        }

        if (!user.driveConnected) {
            return;
        }

        String access_token = user.access_token;
        Log.d(TAG, "access_token : " + access_token);
        subscriptions.add(SuperSafeApplication.serverDriveApi.onGetDriveAbout(access_token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.startLoading())
                .subscribe(onResponse -> {
                    if (view == null) {
                        Log.d(TAG, "View is null");
                        return;
                    }
                    view.stopLoading();
                    if (onResponse.error != null) {
                        view.onError(new Gson().toJson(onResponse.error), EnumStatus.GET_DRIVE_ABOUT);
                        final User mUser = User.getInstance().getUserInfo();
                        if (mUser != null) {
                            user.driveConnected = false;
                            PrefsController.putString(getString(R.string.key_user), new Gson().toJson(user));
                        }
                    } else {
                        view.onSuccessful(new Gson().toJson(onResponse));
                        final User mUser = User.getInstance().getUserInfo();
                        if (mUser != null) {
                            user.driveConnected = true;
                            PrefsController.putString(getString(R.string.key_user), new Gson().toJson(user));
                        }
                    }
                    Log.d(TAG, "Body : " + new Gson().toJson(onResponse));
                }, throwable -> {
                    if (view == null) {
                        Log.d(TAG, "View is null");
                        return;
                    }

                    if (throwable instanceof HttpException) {
                        ResponseBody bodys = ((HttpException) throwable).response().errorBody();
                        try {
                            Log.d(TAG, "error" + bodys.string());
                            String msg = new Gson().toJson(bodys.string());
                            Log.d(TAG, msg);
                            view.onError("" + msg, EnumStatus.GET_DRIVE_ABOUT);
                            final User mUser = User.getInstance().getUserInfo();
                            if (mUser != null) {
                                user.driveConnected = false;
                                PrefsController.putString(getString(R.string.key_user), new Gson().toJson(user));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            view.onError("" + e.getMessage(), EnumStatus.GET_DRIVE_ABOUT);
                            final User mUser = User.getInstance().getUserInfo();
                            if (mUser != null) {
                                user.driveConnected = false;
                                PrefsController.putString(getString(R.string.key_user), new Gson().toJson(user));
                            }
                        }
                    } else {
                        Log.d(TAG, "Can not call " + throwable.getMessage());
                        view.onError("Error :" + throwable.getMessage(), EnumStatus.GET_DRIVE_ABOUT);
                        final User mUser = User.getInstance().getUserInfo();
                        if (mUser != null) {
                            user.driveConnected = false;
                            PrefsController.putString(getString(R.string.key_user), new Gson().toJson(user));
                        }
                    }
                    view.stopLoading();
                }));
    }


}
