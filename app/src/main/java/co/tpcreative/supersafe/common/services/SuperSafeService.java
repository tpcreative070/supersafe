package co.tpcreative.supersafe.common.services;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Binder;
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
import co.tpcreative.supersafe.common.controller.GalleryCameraMediaManager;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.controller.ServiceManager;
import co.tpcreative.supersafe.common.controller.SingletonPremiumTimer;
import co.tpcreative.supersafe.common.controller.SingletonPrivateFragment;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.presenter.PresenterService;
import co.tpcreative.supersafe.common.response.DriveResponse;
import co.tpcreative.supersafe.common.services.download.DownloadService;
import co.tpcreative.supersafe.common.services.upload.ProgressRequestBody;
import co.tpcreative.supersafe.common.util.NetworkUtil;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.DriveAbout;
import co.tpcreative.supersafe.model.DriveDescription;
import co.tpcreative.supersafe.model.DriveTitle;
import co.tpcreative.supersafe.model.EnumDelete;
import co.tpcreative.supersafe.model.EnumFileType;
import co.tpcreative.supersafe.model.EnumFormatType;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.Items;
import co.tpcreative.supersafe.model.MainCategories;
import co.tpcreative.supersafe.model.Premium;
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

public class SuperSafeService extends PresenterService<BaseView> implements SuperSafeReceiver.ConnectivityReceiverListener {

    private static final String TAG = SuperSafeService.class.getSimpleName();
    private final IBinder mBinder = new LocalBinder(); // Binder given to clients
    protected Storage storage;
    private Intent mIntent;
    private SuperSafeReceiver androidReceiver;
    private DownloadService downloadService;
    private HashMap<String, String> hashMapGlobal = new HashMap<>();
    private HashMap<String, String> hashMapGlobalCategories = new HashMap<>();
    public HashMap<String, String> getHashMapGlobal() {
        return hashMapGlobal;
    }
    public HashMap<String, String> getHashMapGlobalCategories() {
        return hashMapGlobalCategories;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        downloadService = new DownloadService(this);
        storage = new Storage(this);
        onInitReceiver();
        SuperSafeApplication.getInstance().setConnectivityListener(this);
    }

    public Storage getStorage() {
        return storage;
    }

    public void onInitReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        androidReceiver = new SuperSafeReceiver();
        registerReceiver(androidReceiver, intentFilter);
        SuperSafeApplication.getInstance().setConnectivityListener(this);
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
        BaseView view = view();
        if (view != null) {
            if (isConnected){
                view.onSuccessful("Connected network",EnumStatus.CONNECTED);
            }
            else{
                view.onSuccessful("Disconnected network",EnumStatus.DISCONNECTED);
            }
        }
    }

    @Override
    public void onActionScreenOff() {
        BaseView view = view();
        if (view != null) {
            view.onSuccessful("Screen Off",EnumStatus.SCREEN_OFF);
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

    public void onGetUserInfo(){
        Log.d(TAG,"onGetUserInfo");
        BaseView view = view();
        if (view == null) {
            return;
        }
        if (NetworkUtil.pingIpAddress(SuperSafeApplication.getInstance())) {

            final boolean isPremiumComplimentary  = User.getInstance().isPremiumComplimentary();
            if (!isPremiumComplimentary){
                return;
            }

            final User mUser = User.getInstance().getUserInfo();
            if (mUser!=null){
                final Premium premium = mUser.premium;
                if (mUser.premium!=null){
                    if (mUser.premium.status){
                        long currentDatetime = System.currentTimeMillis();
                        long device_milliseconds = premium.device_milliseconds;
                        if (device_milliseconds>0){
                            long result  = currentDatetime - device_milliseconds;
                            mUser.premium.current_milliseconds = mUser.premium.current_milliseconds+result;
                            PrefsController.putString(getString(R.string.key_user),new Gson().toJson(mUser));
                            SingletonPremiumTimer.getInstance().onStartTimer();
                        }
                    }
                }
            }
            return;
        }

        if (subscriptions == null) {
            return;
        }

        final User mUser = User.getInstance().getUserInfo();
        if (mUser==null){
            return;
        }

        String email = mUser.email;
        Map<String,String> hash = new HashMap<>();
        hash.put(getString(R.string.key_user_id),email);
        subscriptions.add(SuperSafeApplication.serverAPI.onUserInfo(hash)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.onStartLoading(EnumStatus.USER_INFO))
                .subscribe(onResponse -> {
                    view.onStopLoading(EnumStatus.USER_INFO);
                    if (onResponse.error){
                        view.onError(onResponse.message,EnumStatus.USER_INFO);
                    }
                    else{
                        if (onResponse.premium!=null){
                            mUser.premium = onResponse.premium;
                            PrefsController.putString(getString(R.string.key_user),new Gson().toJson(mUser));
                            view.onSuccessful("Successful",EnumStatus.USER_INFO);
                        }
                    }
                    Log.d(TAG, "Body : " + new Gson().toJson(onResponse));
                }, throwable -> {
                    if (throwable instanceof HttpException) {
                        ResponseBody bodys = ((HttpException) throwable).response().errorBody();
                        try {
                            Log.d(TAG,"error" +bodys.string());
                            String msg = new Gson().toJson(bodys.string());
                            Log.d(TAG, msg);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.d(TAG, "Can not call " + throwable.getMessage());
                    }
                    view.onStopLoading(EnumStatus.USER_INFO);
                }));
    }

    public void getDriveAbout() {
        BaseView view = view();
        if (view == null) {
            view.onError("View is null", EnumStatus.GET_DRIVE_ABOUT);
            return;
        }
        if (NetworkUtil.pingIpAddress(SuperSafeApplication.getInstance())) {
            view.onError("No connection", EnumStatus.GET_DRIVE_ABOUT);
            return;
        }
        final User user = User.getInstance().getUserInfo();
        if (user == null) {
            view.onError("User is null", EnumStatus.GET_DRIVE_ABOUT);
            return;
        }

        if (user.access_token == null) {
            view.onError("access token is null", EnumStatus.GET_DRIVE_ABOUT);
            return;
        }

        String access_token = user.access_token;
        Log.d(TAG, "access_token : " + access_token);
        view.onSuccessful(access_token, EnumStatus.GET_DRIVE_ABOUT);
        subscriptions.add(SuperSafeApplication.serverDriveApi.onGetDriveAbout(access_token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.onStartLoading(EnumStatus.GET_DRIVE_ABOUT))
                .subscribe(onResponse -> {
                    if (view == null) {
                        view.onError("View is null", EnumStatus.GET_DRIVE_ABOUT);
                        return;
                    }
                    view.onStopLoading(EnumStatus.GET_DRIVE_ABOUT);
                    if (onResponse.error != null) {
                        view.onError("Error " + new Gson().toJson(onResponse.error), EnumStatus.REQUEST_ACCESS_TOKEN);
                    } else {
                        final User mUser = User.getInstance().getUserInfo();
                        mUser.driveAbout = onResponse;
                        PrefsController.putString(getString(R.string.key_user), new Gson().toJson(mUser));
                        view.onSuccessful(new Gson().toJson(onResponse), EnumStatus.GET_DRIVE_ABOUT);
                    }
                    Log.d(TAG, "Body : " + new Gson().toJson(onResponse));
                }, throwable -> {
                    if (view == null) {
                        view.onError("View is null", EnumStatus.GET_DRIVE_ABOUT);
                        return;
                    }
                    if (throwable instanceof HttpException) {
                        ResponseBody bodys = ((HttpException) throwable).response().errorBody();
                        try {
                            final String value = bodys.string();
                            final DriveAbout driveAbout = new Gson().fromJson(value, DriveAbout.class);
                            if (driveAbout != null) {
                                if (driveAbout.error != null) {
                                    view.onError(EnumStatus.GET_DRIVE_ABOUT.name()+"-"+new Gson().toJson(driveAbout.error), EnumStatus.REQUEST_ACCESS_TOKEN);
                                }
                            } else {
                                view.onError(EnumStatus.GET_DRIVE_ABOUT.name()+" - Error null ", EnumStatus.REQUEST_ACCESS_TOKEN);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            view.onError("Exception " + e.getMessage(), EnumStatus.GET_DRIVE_ABOUT);
                        }
                    } else {
                        Log.d(TAG, "Can not call " + throwable.getMessage());
                        view.onError("Error ^^:" + throwable.getMessage(), EnumStatus.GET_DRIVE_ABOUT);
                    }
                    view.onStopLoading(EnumStatus.GET_DRIVE_ABOUT);
                }));
    }

    public void onGetListFileInApp(BaseView<Integer> view) {
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

        if (!user.driveConnected) {
            view.onError("No Drive connected", EnumStatus.GET_LIST_FILES_IN_APP);
            return;
        }

        String access_token = user.access_token;
        Log.d(TAG, "access_token : " + access_token);
        subscriptions.add(SuperSafeApplication.serverDriveApi.onGetListFileInAppFolder(access_token,SuperSafeApplication.getInstance().getString(R.string.key_appDataFolder))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.onStartLoading(EnumStatus.GET_LIST_FILES_IN_APP))
                .subscribe(onResponse -> {
                    Utils.Log(TAG, "Response data from items " + new Gson().toJson(onResponse));
                    if (view == null) {
                        Log.d(TAG, "View is null");
                        return;
                    }
                    view.onStopLoading(EnumStatus.GET_LIST_FILES_IN_APP);
                    if (onResponse.error != null) {
                        Log.d(TAG, "onError:" + new Gson().toJson(onResponse));
                        view.onError("Not found this id.... :" + new Gson().toJson(onResponse.error), EnumStatus.GET_LIST_FILES_IN_APP);
                    } else {
                        final int count = onResponse.files.size();
                        Utils.Log(TAG,"Total count request :" + count);
                        view.onSuccessful("Successful", EnumStatus.GET_LIST_FILES_IN_APP,count);
                    }
                }, throwable -> {
                    if (view == null) {
                        Log.d(TAG, "View is null");
                        return;
                    }
                    if (throwable instanceof HttpException) {
                        ResponseBody bodys = ((HttpException) throwable).response().errorBody();
                        try {
                            final String value = bodys.string();
                            final DriveAbout driveAbout = new Gson().fromJson(value, DriveAbout.class);
                            if (driveAbout != null) {
                                if (driveAbout.error != null) {
                                    view.onError(EnumStatus.GET_LIST_FILES_IN_APP.name()+"-"+new Gson().toJson(driveAbout.error), EnumStatus.REQUEST_ACCESS_TOKEN);
                                }
                            } else {
                                view.onError(EnumStatus.GET_LIST_FILES_IN_APP.name()+" - Error null ", EnumStatus.REQUEST_ACCESS_TOKEN);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            view.onError("Exception " + e.getMessage(), EnumStatus.GET_LIST_FILES_IN_APP);
                        }
                    } else {
                        Log.d(TAG, "Can not call " + throwable.getMessage());
                        view.onError("Error ^^:" + throwable.getMessage(), EnumStatus.GET_LIST_FILES_IN_APP);
                    }
                    view.onStopLoading(EnumStatus.GET_LIST_FILES_IN_APP);
                }));
    }

    /*Network request*/

    public void onCategoriesSync(MainCategories mainCategories, BaseView view) {
        Utils.Log(TAG, "onCategoriesSync");
        if (view == null) {
            view.onError("no view", EnumStatus.CATEGORIES_SYNC);
            return;
        }
        if (NetworkUtil.pingIpAddress(SuperSafeApplication.getInstance())) {
            view.onError("no connection", EnumStatus.CATEGORIES_SYNC);
            return;
        }
        if (subscriptions == null) {
            view.onError("no subscriptions", EnumStatus.CATEGORIES_SYNC);
            return;
        }
        final User user = User.getInstance().getUserInfo();
        if (user == null) {
            view.onError("no user", EnumStatus.CATEGORIES_SYNC);
            return;
        }

        if (user.access_token == null) {
            view.onError("no access_token", EnumStatus.CATEGORIES_SYNC);
            return;
        }

        Map<String, Object> hashMap = MainCategories.getInstance().objectToHashMap(mainCategories);
        hashMap.put(getString(R.string.key_user_id), user.email);
        hashMap.put(getString(R.string.key_cloud_id), user.cloud_id);
        hashMap.put(getString(R.string.key_categories_max), mainCategories.categories_max + "");
        String access_token = user.access_token;
        view.onSuccessful("access_token" + getString(R.string.access_token, access_token));
        Log.d(TAG, "access_token : " + access_token);
        subscriptions.add(SuperSafeApplication.serverAPI.onCategoriesSync(hashMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.onStartLoading(EnumStatus.CATEGORIES_SYNC))
                .subscribe(onResponse -> {
                    if (view == null) {
                        Log.d(TAG, "View is null");
                        view.onError("View is null", EnumStatus.CATEGORIES_SYNC);
                        return;
                    }
                    view.onStopLoading(EnumStatus.CATEGORIES_SYNC);
                    if (onResponse.error) {
                        Log.d(TAG, "onError 1");
                        view.onError(onResponse.message, EnumStatus.CATEGORIES_SYNC);
                    } else {
                        if (onResponse != null) {
                            if (onResponse.category != null) {
                                if (mainCategories.categories_hex_name.equals(onResponse.category.categories_hex_name)) {
                                    mainCategories.categories_id = onResponse.category.categories_id;
                                    mainCategories.isSyncOwnServer = true;
                                    mainCategories.isChange = false;
                                    mainCategories.isDelete = false;
                                    InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onUpdate(mainCategories);
                                    view.onSuccessful(onResponse.message + " - " + onResponse.category.categories_id + " - ", EnumStatus.CATEGORIES_SYNC);
                                } else {
                                    view.onSuccessful("Not found categories_hex_name - " + onResponse.category.categories_id);
                                }
                            }
                        }
                    }
                }, throwable -> {
                    if (view == null) {
                        Log.d(TAG, "View is null");
                        view.onError("View is null", EnumStatus.CATEGORIES_SYNC);
                        return;
                    }
                    if (throwable instanceof HttpException) {
                        ResponseBody bodys = ((HttpException) throwable).response().errorBody();
                        try {
                            Log.d(TAG, "error" + bodys.string());
                            String msg = new Gson().toJson(bodys.string());
                            Log.d(TAG, msg);
                            view.onError("" + msg, EnumStatus.CATEGORIES_SYNC);
                        } catch (IOException e) {
                            e.printStackTrace();
                            view.onError("" + e.getMessage(), EnumStatus.CATEGORIES_SYNC);
                        }
                    } else {
                        Log.d(TAG, "Can not call " + throwable.getMessage());
                        view.onError("Error :" + throwable.getMessage(), EnumStatus.CATEGORIES_SYNC);
                    }
                    view.onStopLoading(EnumStatus.CATEGORIES_SYNC);
                }));
    }

    public void onDeleteCategoriesSync(MainCategories mainCategories, BaseView view) {
        Utils.Log(TAG, "onDeleteCategoriesSync");
        if (view == null) {
            view.onError("no view", EnumStatus.DELETE_CATEGORIES);
            return;
        }
        if (NetworkUtil.pingIpAddress(SuperSafeApplication.getInstance())) {
            view.onError("no connection", EnumStatus.DELETE_CATEGORIES);
            return;
        }
        if (subscriptions == null) {
            view.onError("no subscriptions", EnumStatus.DELETE_CATEGORIES);
            return;
        }
        final User user = User.getInstance().getUserInfo();
        if (user == null) {
            view.onError("no user", EnumStatus.DELETE_CATEGORIES);
            return;
        }

        if (user.access_token == null) {
            view.onError("no access_token", EnumStatus.DELETE_CATEGORIES);
            return;
        }

        Map<String, Object> hashMap = MainCategories.getInstance().objectToHashMap(mainCategories);
        hashMap.put(getString(R.string.key_user_id), user.email);
        hashMap.put(getString(R.string.key_cloud_id), user.cloud_id);
        String access_token = user.access_token;
        view.onSuccessful("access_token" + getString(R.string.access_token, access_token));
        Log.d(TAG, "access_token : " + access_token);
        subscriptions.add(SuperSafeApplication.serverAPI.onDeleteCategories(hashMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.onStartLoading(EnumStatus.DELETE_CATEGORIES))
                .subscribe(onResponse -> {
                    if (view == null) {
                        Log.d(TAG, "View is null");
                        view.onError("View is null", EnumStatus.DELETE_CATEGORIES);
                        return;
                    }
                    view.onStopLoading(EnumStatus.DELETE_CATEGORIES);
                    if (onResponse.error) {
                        Log.d(TAG, "onError 1");
                        view.onError(onResponse.message, EnumStatus.DELETE_CATEGORIES);
                    } else {
                        InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onDelete(mainCategories);
                        view.onSuccessful(onResponse.message, EnumStatus.DELETE_CATEGORIES);
                    }
                }, throwable -> {
                    if (view == null) {
                        Log.d(TAG, "View is null");
                        view.onError("View is null", EnumStatus.DELETE_CATEGORIES);
                        return;
                    }
                    if (throwable instanceof HttpException) {
                        ResponseBody bodys = ((HttpException) throwable).response().errorBody();
                        try {
                            Log.d(TAG, "error" + bodys.string());
                            String msg = new Gson().toJson(bodys.string());
                            Log.d(TAG, msg);
                            view.onError("" + msg, EnumStatus.DELETE_CATEGORIES);
                        } catch (IOException e) {
                            e.printStackTrace();
                            view.onError("" + e.getMessage(), EnumStatus.DELETE_CATEGORIES);
                        }
                    } else {
                        Log.d(TAG, "Can not call " + throwable.getMessage());
                        view.onError("Error :" + throwable.getMessage(), EnumStatus.DELETE_CATEGORIES);
                    }
                    view.onStopLoading(EnumStatus.DELETE_CATEGORIES);
                }));
    }

    /*Create/Update for Categories*/

    public void onGetListCategoriesSync(BaseView view) {
        Utils.Log(TAG, "onGetListCategoriesSync");
        if (view == null) {
            view.onError("no view", EnumStatus.LIST_CATEGORIES_SYNC);
            return;
        }
        if (NetworkUtil.pingIpAddress(SuperSafeApplication.getInstance())) {
            view.onError("no connection", EnumStatus.LIST_CATEGORIES_SYNC);
            return;
        }
        if (subscriptions == null) {
            view.onError("no subscriptions", EnumStatus.LIST_CATEGORIES_SYNC);
            return;
        }
        final User user = User.getInstance().getUserInfo();
        if (user == null) {
            view.onError("no user", EnumStatus.LIST_CATEGORIES_SYNC);
            return;
        }

        if (user.access_token == null) {
            view.onError("no access_token", EnumStatus.LIST_CATEGORIES_SYNC);
            return;
        }

        if (user.cloud_id==null){
            view.onError("cloud id null", EnumStatus.LIST_CATEGORIES_SYNC);
            return;
        }

        Map<String, Object> hashMap = new HashMap<>();
        hashMap.put(getString(R.string.key_user_id), user.email);
        hashMap.put(getString(R.string.key_cloud_id), user.cloud_id);
        String access_token = user.access_token;
        view.onSuccessful("access_token" + getString(R.string.access_token, access_token));
        Log.d(TAG, "access_token : " + access_token);
        subscriptions.add(SuperSafeApplication.serverAPI.onListCategoriesSync(hashMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.onStartLoading(EnumStatus.LIST_CATEGORIES_SYNC))
                .subscribe(onResponse -> {
                    if (view == null) {
                        Log.d(TAG, "View is null");
                        view.onError("View is null", EnumStatus.LIST_CATEGORIES_SYNC);
                        return;
                    }
                    view.onStopLoading(EnumStatus.LIST_CATEGORIES_SYNC);
                    if (onResponse.error) {
                        Log.d(TAG, "onError 1");
                        view.onError(onResponse.message, EnumStatus.LIST_CATEGORIES_SYNC);
                    } else {
                        try {
                            if (onResponse.files != null) {
                                for (MainCategories index : onResponse.files) {
                                    MainCategories main = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getCategoriesId(index.categories_id,false);
                                    if (main != null) {
                                        if (!main.isChange && !main.isDelete) {
                                            main.isSyncOwnServer = true;
                                            InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onUpdate(main);
                                        }
                                        view.onSuccessful(onResponse.message);
                                    } else {
                                        MainCategories mMain = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getCategoriesItemId(index.categories_hex_name,false);
                                        if (mMain != null) {
                                            if (!mMain.isChange && !mMain.isDelete) {
                                                mMain.isSyncOwnServer = true;
                                                mMain.isChange = false;
                                                mMain.isDelete = false;
                                                mMain.categories_id = index.categories_id;
                                                InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onUpdate(mMain);
                                            }
                                        } else {
                                            mMain = index;
                                            mMain.categories_local_id = Utils.getUUId();
                                            mMain.isSyncOwnServer = true;
                                            mMain.isChange = false;
                                            mMain.isDelete = false;
                                            final int count  = InstanceGenerator.getInstance(this).getLatestItem();
                                            mMain.categories_max = count;
                                            InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onInsert(mMain);
                                        }
                                        view.onSuccessful(onResponse.message);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            view.onError("Error :" + e.getMessage(), EnumStatus.LIST_CATEGORIES_SYNC);
                        } finally {
                            view.onSuccessful(onResponse.message, EnumStatus.LIST_CATEGORIES_SYNC);
                        }
                    }
                }, throwable -> {
                    if (view == null) {
                        Log.d(TAG, "View is null");
                        view.onError("View is null", EnumStatus.LIST_CATEGORIES_SYNC);
                        return;
                    }
                    if (throwable instanceof HttpException) {
                        ResponseBody bodys = ((HttpException) throwable).response().errorBody();
                        try {
                            Log.d(TAG, "error" + bodys.string());
                            String msg = new Gson().toJson(bodys.string());
                            Log.d(TAG, msg);
                            view.onError("" + msg, EnumStatus.LIST_CATEGORIES_SYNC);
                        } catch (IOException e) {
                            e.printStackTrace();
                            view.onError("" + e.getMessage(), EnumStatus.LIST_CATEGORIES_SYNC);
                        }
                    } else {
                        Log.d(TAG, "Can not call " + throwable.getMessage());
                        view.onError("Error :" + throwable.getMessage(), EnumStatus.LIST_CATEGORIES_SYNC);
                    }
                    view.onStopLoading(EnumStatus.LIST_CATEGORIES_SYNC);
                }));
    }


    /*Date for Categories*/

    public void onAddItems(final Items mItem, BaseView view) {
        final Items items = mItem;
        Utils.Log(TAG, "onAddItems");
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

        if (!user.driveConnected) {
            view.onError("No Drive connected", EnumStatus.REQUEST_ACCESS_TOKEN);
            return;
        }

        if (items.categories_id == null || items.categories_id.equals("null")){
            view.onError("Categories id is null", EnumStatus.ADD_ITEMS);
        }

        // Map<String, Object> hashMap = new HashMap<>();

        final Map<String, Object> hashMap = Items.getInstance().objectToHashMap(items);
        if (hashMap != null) {
            hashMap.put(getString(R.string.key_user_id), user.email);
            hashMap.put(getString(R.string.key_cloud_id), user.cloud_id);
            hashMap.put(getString(R.string.key_kind), getString(R.string.key_drive_file));
            DriveTitle contentTitle = new DriveTitle();
            contentTitle.items_id = items.items_id;
            String hex = DriveTitle.getInstance().convertToHex(new Gson().toJson(contentTitle));
            hashMap.put(getString(R.string.key_name), hex);
        }

        String access_token = user.access_token;
        view.onSuccessful("access_token" + getString(R.string.access_token, access_token));
        Log.d(TAG, "access_token : " + access_token);
        subscriptions.add(SuperSafeApplication.serverAPI.onSyncData(hashMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.onStartLoading(EnumStatus.ADD_ITEMS))
                .subscribe(onResponse -> {
                    if (view == null) {
                        Log.d(TAG, "View is null");
                        return;
                    }
                    view.onStopLoading(EnumStatus.ADD_ITEMS);
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
                    view.onStopLoading(EnumStatus.ADD_ITEMS);
                }));
    }


    /*Get List Categories*/

    public void onDeleteCloudItems(final Items items, final boolean isOriginalGlobalId, final BaseView view) {
        Utils.Log(TAG, "onDeleteCloudItems");
        if (view == null) {
            view.onError("no view", EnumStatus.DELETE_SYNC_CLOUD_DATA);
            return;
        }
        if (NetworkUtil.pingIpAddress(SuperSafeApplication.getInstance())) {
            view.onError("no connection", EnumStatus.DELETE_SYNC_CLOUD_DATA);
            return;
        }
        if (subscriptions == null) {
            view.onError("no subscriptions", EnumStatus.DELETE_SYNC_CLOUD_DATA);
            return;
        }
        final User user = User.getInstance().getUserInfo();
        if (user == null) {
            view.onError("no user", EnumStatus.DELETE_SYNC_CLOUD_DATA);
            return;
        }

        if (!user.driveConnected) {
            view.onError("No Drive connected", EnumStatus.REQUEST_ACCESS_TOKEN);
            return;
        }

        String access_token = user.access_token;
        view.onSuccessful("access_token" + getString(R.string.access_token, access_token));
        Log.d(TAG, "access_token : " + access_token);


        String id = "";
        if (isOriginalGlobalId) {
            id = items.global_original_id;
        } else {
            id = items.global_thumbnail_id;
        }

        subscriptions.add(SuperSafeApplication.serverDriveApi.onDeleteCloudItem(access_token, id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.onStartLoading(EnumStatus.DELETE_SYNC_CLOUD_DATA))
                .subscribe(onResponse -> {
                    if (onResponse.code() == 204) {
                        final EnumDelete delete = EnumDelete.values()[items.deleteAction];
                        if (delete == EnumDelete.DELETE_DONE) {
                            InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onDelete(items);
                            storage.deleteDirectory(SuperSafeApplication.getInstance().getSupersafePrivate() + items.items_id);
                        }
                        view.onSuccessful("Deleted Successful : code " + onResponse.code() + " - ", EnumStatus.DELETE_SYNC_CLOUD_DATA);
                    } else if (onResponse.code() == 404) {
                        final EnumDelete delete = EnumDelete.values()[items.deleteAction];
                        if (delete == EnumDelete.DELETE_DONE) {
                            InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onDelete(items);
                            storage.deleteDirectory(SuperSafeApplication.getInstance().getSupersafePrivate() + items.items_id);
                        }
                        final String value = onResponse.errorBody().string();
                        final DriveAbout driveAbout = new Gson().fromJson(value, DriveAbout.class);
                        view.onError("Not found file :" + new Gson().toJson(driveAbout.error) + " - ", EnumStatus.DELETE_SYNC_CLOUD_DATA);
                    } else {
                        view.onError("Another cases : code " + onResponse.code() + " - ", EnumStatus.DELETE_SYNC_CLOUD_DATA);
                    }
                }, throwable -> {
                    if (view == null) {
                        Log.d(TAG, "View is null");
                        return;
                    }
                    if (throwable instanceof HttpException) {
                        ResponseBody bodys = ((HttpException) throwable).response().errorBody();
                        try {
                            final String value = bodys.string();
                            final DriveAbout driveAbout = new Gson().fromJson(value, DriveAbout.class);
                            if (driveAbout != null) {
                                if (driveAbout.error != null) {
                                    view.onError(new Gson().toJson(driveAbout.error), EnumStatus.DELETE_SYNC_CLOUD_DATA);
                                }
                            } else {
                                view.onError("Error null 1 ", EnumStatus.DELETE_SYNC_CLOUD_DATA);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            view.onError("Exception " + e.getMessage(), EnumStatus.DELETE_SYNC_CLOUD_DATA);
                        }
                    } else {
                        Log.d(TAG, "Can not call " + throwable.getMessage());
                        view.onError("Error 0:" + throwable.getMessage(), EnumStatus.DELETE_SYNC_CLOUD_DATA);
                    }
                    view.onStopLoading(EnumStatus.DELETE_SYNC_CLOUD_DATA);
                }));
    }

    public void onDeleteOwnSystem(final Items items, final BaseView view) {
        Utils.Log(TAG, "onDeleteOwnSystem");
        if (view == null) {
            view.onError("no view", EnumStatus.DELETE_SYNC_OWN_DATA);
            return;
        }
        if (NetworkUtil.pingIpAddress(SuperSafeApplication.getInstance())) {
            view.onError("no connection", EnumStatus.DELETE_SYNC_OWN_DATA);
            return;
        }
        if (subscriptions == null) {
            view.onError("no subscriptions", EnumStatus.DELETE_SYNC_OWN_DATA);
            return;
        }
        final User user = User.getInstance().getUserInfo();
        if (user == null) {
            view.onError("no user", EnumStatus.DELETE_SYNC_OWN_DATA);
            return;
        }

        if (!user.driveConnected) {
            view.onError("No Drive connected", EnumStatus.REQUEST_ACCESS_TOKEN);
            return;
        }

        final Map<String, Object> hashMap = Items.getInstance().objectToHashMap(items);
        hashMap.put(getString(R.string.key_user_id), user.email);
        String access_token = user.access_token;
        view.onSuccessful("access_token" + getString(R.string.access_token, access_token));
        Log.d(TAG, "access_token : " + access_token);
        subscriptions.add(SuperSafeApplication.serverAPI.onDeleteOwnItems(hashMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.onStartLoading(EnumStatus.DELETE_SYNC_OWN_DATA))
                .subscribe(onResponse -> {
                    Utils.Log(TAG, "Response data from items " + new Gson().toJson(onResponse));
                    if (view == null) {
                        Log.d(TAG, "View is null");
                        return;
                    }
                    if (onResponse.error) {
                        view.onError(onResponse.message, EnumStatus.DELETE_SYNC_CLOUD_DATA);
                    } else {
                        view.onSuccessful(onResponse.message, EnumStatus.DELETE_SYNC_CLOUD_DATA);
                        items.isDeleteGlobal = true;
                        items.deleteAction = EnumDelete.DELETE_DONE.ordinal();
                        InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onUpdate(items);
                    }
                    view.onStopLoading(EnumStatus.DELETE_SYNC_CLOUD_DATA);

                }, throwable -> {
                    if (view == null) {
                        Log.d(TAG, "View is null");
                        return;
                    }
                    if (throwable instanceof HttpException) {
                        ResponseBody bodys = ((HttpException) throwable).response().errorBody();
                        try {
                            final String value = bodys.string();
                            if (value != null) {
                                view.onError("Error " + value, EnumStatus.DELETE_SYNC_OWN_DATA);
                            } else {
                                view.onError("Error null ", EnumStatus.DELETE_SYNC_OWN_DATA);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            view.onError("Exception " + e.getMessage(), EnumStatus.DELETE_SYNC_OWN_DATA);
                        }
                    } else {
                        Log.d(TAG, "Can not call " + throwable.getMessage());
                        view.onError("Error :" + throwable.getMessage(), EnumStatus.DELETE_SYNC_OWN_DATA);
                    }
                    view.onStopLoading(EnumStatus.DELETE_SYNC_OWN_DATA);
                }));
    }

    public void onGetListSync(String nextPage, BaseView view) {
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

        if (nextPage.equals("0")) {
            hashMapGlobalCategories.clear();
            hashMapGlobal.clear();
        }

        Map<String, Object> hashMap = new HashMap<>();
        hashMap.put(getString(R.string.key_cloud_id), user.cloud_id);
        hashMap.put(getString(R.string.key_user_id), user.email);
        hashMap.put(getString(R.string.key_next_page), nextPage);
        hashMap.put(getString(R.string.key_isSyncCloud), true);

        String access_token = user.access_token;
        view.onSuccessful("access_token" + getString(R.string.access_token, access_token));
        Log.d(TAG, "access_token : " + access_token);
        subscriptions.add(SuperSafeApplication.serverAPI.onListFilesSync(hashMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.onStartLoading(EnumStatus.GET_LIST_FILE))
                .subscribe(onResponse -> {
                    if (view == null) {
                        Log.d(TAG, "View is null");
                        view.onError("View is null", EnumStatus.GET_LIST_FILE);
                        return;
                    }
                    view.onStopLoading(EnumStatus.GET_LIST_FILE);

                    if (onResponse.error) {
                        Log.d(TAG, "onError 1");
                        view.onError(onResponse.message, EnumStatus.GET_LIST_FILE);
                    } else {
                        final List<MainCategories> listCategories = onResponse.listCategories;
                        final List<DriveResponse> driveResponse = onResponse.files;
                        if (onResponse.nextPage == null) {
                            try {
                                Utils.Log(TAG,"Special values "+new Gson().toJson(listCategories));
                                for (MainCategories index : listCategories) {
                                    hashMapGlobalCategories.put(index.categories_id, index.categories_id);
                                    MainCategories main = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getCategoriesId(index.categories_id,false);
                                    if (main != null) {
                                        if (!main.isChange && !main.isDelete) {
                                            main.isSyncOwnServer = true;
                                            InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onUpdate(main);
                                        }
                                        view.onSuccessful(onResponse.message, EnumStatus.GET_LIST_FILE);
                                    } else {
                                        MainCategories mMain = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getCategoriesItemId(index.categories_hex_name,false);
                                        if (mMain != null) {
                                            if (!mMain.isDelete && !mMain.isChange) {
                                                mMain.isSyncOwnServer = true;
                                                mMain.isChange = false;
                                                mMain.isDelete = false;
                                                mMain.categories_id = index.categories_id;
                                                InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onUpdate(mMain);
                                            }
                                        } else {
                                            mMain = index;
                                            mMain.categories_local_id = Utils.getUUId();
                                            mMain.isSyncOwnServer = true;
                                            mMain.isChange = false;
                                            mMain.isDelete = false;
                                            final int count  = InstanceGenerator.getInstance(this).getLatestItem();
                                            mMain.categories_max = count;
                                            InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onInsert(mMain);
                                        }
                                        view.onSuccessful(onResponse.message, EnumStatus.GET_LIST_FILE);
                                    }
                                }
                            }
                            catch (Exception e){
                                view.onError("Error "+e.getMessage(), EnumStatus.GET_LIST_FILE);
                                e.printStackTrace();
                            }
                            finally {
                                final User mUser = User.getInstance().getUserInfo();
                                if (mUser!=null){
                                    mUser.syncData = onResponse.syncData;
                                    PrefsController.putString(getString(R.string.key_user),new Gson().toJson(mUser));
                                    Utils.onWriteLog(new Gson().toJson(mUser),EnumStatus.GET_LIST_FILE);
                                }
                                Log.d(TAG, "Ready for sync");
                                view.onSuccessful("Ready for sync");
                                view.onSuccessful(onResponse.nextPage, EnumStatus.SYNC_READY);
                            }
                        } else {
                            try {
                                view.onSuccessful(onResponse.message, EnumStatus.GET_LIST_FILE);
                                for (DriveResponse index : driveResponse) {
                                    hashMapGlobal.put(index.items_id, index.items_id);
                                    final DriveDescription description = DriveDescription.getInstance().hexToObject(index.description);
                                    if (description != null) {
                                        DriveTitle driveTitle = DriveTitle.getInstance().hexToObject(index.name);
                                        if (driveTitle != null) {
                                            final Items items = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getItemId(driveTitle.items_id,false);
                                            EnumFormatType formatTypeFile = EnumFormatType.values()[description.formatType];
                                            if (items == null) {
                                                description.global_original_id = index.global_original_id;
                                                description.global_thumbnail_id = index.global_thumbnail_id;
                                                description.categories_id = index.categories_id;
                                                switch (formatTypeFile) {
                                                    case AUDIO: {
                                                        description.thumbnailSync = true;
                                                        break;
                                                    }
                                                    case FILES:{
                                                        description.thumbnailSync = true;
                                                        break;
                                                    }
                                                    default: {
                                                        description.originalSync = false;
                                                        description.thumbnailSync = false;
                                                        break;
                                                    }
                                                }
                                                final MainCategories main = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getCategoriesId(index.categories_id,false);
                                                if (main != null) {
                                                    description.categories_local_id = main.categories_local_id;
                                                    onSaveItem(description);
                                                } else {
                                                    view.onSuccessful("..................categories_id is nul.............");
                                                }
                                            } else {
                                                items.global_original_id = index.global_original_id;
                                                items.global_thumbnail_id = index.global_thumbnail_id;
                                                items.categories_id = index.categories_id;
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
                                view.onError("Error "+e.getMessage(), EnumStatus.GET_LIST_FILE);
                                e.printStackTrace();
                            } finally {
                                Log.d(TAG, "Load more");
                                view.onSuccessful("Load more..."+onResponse.message);
                                view.onSuccessful(onResponse.nextPage, EnumStatus.LOAD_MORE);
                            }
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
                    view.onStopLoading(EnumStatus.GET_LIST_FILE);
                }));
    }

    public void onDeletePreviousSync(ServiceManager.DeleteServiceListener view) {
        Utils.Log(TAG, "onDeletePreviousSync");
        try {
            final List<Items> list = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getListItemId(true,true, false);
            for (Items index : list) {
                String value = hashMapGlobal.get(index.items_id);
                if (value == null) {
                    InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onDelete(index);
                    storage.deleteDirectory(SuperSafeApplication.getInstance().getSupersafePrivate() + index.items_id);
                }
            }
        } catch (Exception e) {
            e.getMessage();
        } finally {
            Utils.Log(TAG,"Delete everytime..............");
            hashMapGlobal.clear();
            SingletonPrivateFragment.getInstance().onUpdateView();
            GalleryCameraMediaManager.getInstance().onUpdatedView();
            view.onDone();
            /*Note here*/
        }
    }

    public void onDeletePreviousCategoriesSync(ServiceManager.DeleteServiceListener view) {
        Utils.Log(TAG, "onDeletePreviousCategoriesSync");
        try {
            final List<MainCategories> list = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).loadListItemCategoriesSync(true,false);
            for (MainCategories index : list) {
                String value = hashMapGlobalCategories.get(index.categories_id);
                if (value == null) {
                    final List<Items> data = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getListItems(index.categories_local_id, false);
                    if (data == null || data.size() == 0) {
                        InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onDelete(index);
                    }
                }
            }
        } catch (Exception e) {
            e.getMessage();
        } finally {
            view.onDone();
            hashMapGlobalCategories.clear();
            SingletonPrivateFragment.getInstance().onUpdateView();
            GalleryCameraMediaManager.getInstance().onUpdatedView();
            /*Note here*/
        }
    }

    public void onSaveItem(final DriveDescription description) {
        Utils.Log(TAG, "onSaveItem");
        Items items = new Items(false,
                false,
                description.originalSync,
                description.thumbnailSync,
                description.degrees,
                description.fileType,
                description.formatType,
                description.title,
                description.originalName,
                description.thumbnailName,
                description.items_id,
                description.originalPath,
                description.thumbnailPath,
                description.subFolderName,
                description.global_original_id,
                description.global_thumbnail_id,
                description.categories_id,
                description.categories_local_id,
                description.mimeType,
                description.fileExtension,
                DriveDescription.getInstance().convertToHex(new Gson().toJson(description)),
                EnumStatus.DOWNLOAD,
                description.size,
                description.statusProgress,
                description.isDeleteLocal,
                description.isDeleteGlobal,
                description.deleteAction,
                description.isFakePin);
        InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onInsert(items);
    }

    public void onDownloadFile(final Items items, final ServiceManager.DownloadServiceListener listener) {
        Utils.Log(TAG, "onDownloadFile !!!!");
        final User mUser = User.getInstance().getUserInfo();

        if (!mUser.driveConnected) {
            listener.onError("No Drive api connected", EnumStatus.DOWNLOAD);
            return;
        }

        final DownloadFileRequest request = new DownloadFileRequest();
        String id = "";
        if (items.isOriginalGlobalId) {
            id = items.global_original_id;
            request.file_name = items.originalName;
        } else {
            id = items.global_thumbnail_id;
            request.file_name = items.thumbnailName;
        }
        request.items = items;
        request.api_name = String.format(getString(R.string.url_drive_download), id);
        request.Authorization = mUser.access_token;
        String path = SuperSafeApplication.getInstance().getSupersafePrivate();
        String pathFolder = path + items.local_id + "/";
        request.path_folder_output = pathFolder;

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
                    listener.onError("Error download ", EnumStatus.DOWNLOAD);
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
                    listener.onError("Error download save ", EnumStatus.DOWNLOAD);
                }
            }

            @Override
            public void onCodeResponse(int code, DownloadFileRequest request) {
                if (listener != null) {
                    final Items mItem = request.items;
                    if (mItem != null) {
                        mItem.isDeleteLocal = true;
                        mItem.originalSync = true;
                        mItem.thumbnailSync = true;
                        mItem.deleteAction = EnumDelete.DELETE_WAITING.ordinal();
                        /*Not Found file*/
                        if (code == 404) {
                            InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onUpdate(items);
                        }
                    }
                }
            }
        }, getString(R.string.drive_api));
        request.mapHeader = new HashMap<>();
        request.mapObject = new HashMap<>();
        downloadService.downloadDriveFileByGET(request);
    }

    public void onUploadFileInAppFolder(final Items items, final ServiceManager.UploadServiceListener listener) {
        Log.d(TAG, "onUploadFileInAppFolder");
        final User mUser = User.getInstance().getUserInfo();
        MediaType contentType = MediaType.parse("application/json; charset=UTF-8");
        HashMap<String, Object> content = new HashMap<>();


        DriveTitle contentTitle = new DriveTitle();
        File file = null;
        if (items.isOriginalGlobalId) {
            contentTitle.fileType = EnumFileType.ORIGINAL.ordinal();
            file = new File(items.originalPath);
        } else {
            contentTitle.fileType = EnumFileType.THUMBNAIL.ordinal();
            file = new File(items.thumbnailPath);
        }

        if (!storage.isFileExist(file.getAbsolutePath())) {
            InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onDelete(items);
            listener.onError("This path is not found", EnumStatus.UPLOAD);
            return;
        }

        contentTitle.items_id = items.items_id;
        String hex = DriveTitle.getInstance().convertToHex(new Gson().toJson(contentTitle));
        content.put(getString(R.string.key_name), hex);
        List<String> list = new ArrayList<>();
        list.add(getString(R.string.key_appDataFolder));
        content.put(getString(R.string.key_parents), list);
        MultipartBody.Part metaPart = MultipartBody.Part.create(RequestBody.create(contentType, new Gson().toJson(content)));

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

    public void getDriveAbout(BaseView view) {
        Log.d(TAG, "getDriveAbout");
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
            view.onError("User is null",EnumStatus.GET_DRIVE_ABOUT);
            return;
        }

        if (user.access_token == null) {
            view.onError("Access token is null",EnumStatus.GET_DRIVE_ABOUT);
            return;
        }

        String access_token = user.access_token;
        Log.d(TAG, "access_token : " + access_token);
        view.onSuccessful(access_token);
        subscriptions.add(SuperSafeApplication.serverDriveApi.onGetDriveAbout(access_token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.onStartLoading(EnumStatus.GET_DRIVE_ABOUT))
                .subscribe(onResponse -> {
                    if (view == null) {
                        view.onError("View is disable",EnumStatus.GET_DRIVE_ABOUT);
                        return;
                    }
                    view.onStopLoading(EnumStatus.GET_DRIVE_ABOUT);
                    if (onResponse.error != null) {
                        final User mUser = User.getInstance().getUserInfo();
                        if (mUser != null) {
                            user.driveConnected = false;
                            PrefsController.putString(getString(R.string.key_user), new Gson().toJson(user));
                        }
                        view.onError(new Gson().toJson(onResponse.error), EnumStatus.REQUEST_ACCESS_TOKEN);
                    } else {
                        final User mUser = User.getInstance().getUserInfo();
                        if (mUser != null) {
                            user.driveConnected = true;
                            PrefsController.putString(getString(R.string.key_user), new Gson().toJson(user));
                            view.onSuccessful("Successful",EnumStatus.GET_DRIVE_ABOUT);
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
                            final String value = bodys.string();
                            final DriveAbout driveAbout = new Gson().fromJson(value, DriveAbout.class);
                            if (driveAbout != null) {
                                if (driveAbout.error != null) {
                                    final User mUser = User.getInstance().getUserInfo();
                                    if (mUser != null) {
                                        user.driveConnected = false;
                                        PrefsController.putString(getString(R.string.key_user), new Gson().toJson(user));
                                    }
                                    view.onError(EnumStatus.GET_DRIVE_ABOUT.name()+"-"+new Gson().toJson(driveAbout.error), EnumStatus.REQUEST_ACCESS_TOKEN);
                                }
                            } else {
                                final User mUser = User.getInstance().getUserInfo();
                                if (mUser != null) {
                                    user.driveConnected = false;
                                    PrefsController.putString(getString(R.string.key_user), new Gson().toJson(user));
                                }
                                view.onError(EnumStatus.GET_DRIVE_ABOUT.name()+" - Error null ", EnumStatus.REQUEST_ACCESS_TOKEN);
                            }
                        } catch (IOException e) {
                            final User mUser = User.getInstance().getUserInfo();
                            if (mUser != null) {
                                user.driveConnected = false;
                                PrefsController.putString(getString(R.string.key_user), new Gson().toJson(user));
                            }
                            view.onError("Error IOException " + e.getMessage(), EnumStatus.GET_DRIVE_ABOUT);
                        }
                    } else {
                        Log.d(TAG, "Can not call " + throwable.getMessage());
                        final User mUser = User.getInstance().getUserInfo();
                        if (mUser != null) {
                            user.driveConnected = false;
                            PrefsController.putString(getString(R.string.key_user), new Gson().toJson(user));
                        }
                        view.onError("Error else :" + throwable.getMessage(), EnumStatus.GET_DRIVE_ABOUT);
                    }
                    view.onStopLoading(EnumStatus.GET_DRIVE_ABOUT);
                }));
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
    }


}
