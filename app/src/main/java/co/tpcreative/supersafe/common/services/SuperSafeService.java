package co.tpcreative.supersafe.common.services;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import com.google.gson.Gson;
import com.snatik.storage.Storage;
import com.snatik.storage.security.SecurityUtil;
import org.greenrobot.eventbus.EventBus;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import co.tpcreative.supersafe.BuildConfig;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.api.RootAPI;
import co.tpcreative.supersafe.common.api.request.DownloadFileRequest;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.controller.ServiceManager;
import co.tpcreative.supersafe.common.controller.SingletonPrivateFragment;
import co.tpcreative.supersafe.common.presenter.BaseServiceView;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.presenter.PresenterService;
import co.tpcreative.supersafe.common.response.DriveResponse;
import co.tpcreative.supersafe.common.services.download.DownloadService;
import co.tpcreative.supersafe.common.services.upload.ProgressRequestBody;
import co.tpcreative.supersafe.common.util.NetworkUtil;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.Authorization;
import co.tpcreative.supersafe.model.DriveAbout;
import co.tpcreative.supersafe.model.DriveEvent;
import co.tpcreative.supersafe.model.EmailToken;
import co.tpcreative.supersafe.model.EnumDelete;
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

public class SuperSafeService extends PresenterService<BaseServiceView> implements SuperSafeReceiver.ConnectivityReceiverListener {
    private static final String TAG = SuperSafeService.class.getSimpleName();
    private final IBinder mBinder = new LocalBinder(); // Binder given to clients
    protected Storage storage;
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
        Utils.Log(TAG, "onCreate");
        downloadService = new DownloadService();
        storage = new Storage(this);
        onInitReceiver();
        SuperSafeApplication.getInstance().setConnectivityListener(this);
    }

    public Storage getStorage() {
        return storage;
    }

    public void onInitReceiver() {
        Utils.Log(TAG,"onInitReceiver");
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
        Utils.Log(TAG, "onDestroy");
        if (androidReceiver != null) {
            unregisterReceiver(androidReceiver);
        }
        stopSelf();
        stopForeground(true);
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        Utils.Log(TAG, "Connected :" + isConnected);
        BaseServiceView view = view();
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
        BaseServiceView view = view();
        if (view != null) {
            view.onSuccessful("Screen Off",EnumStatus.SCREEN_OFF);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // If we get killed, after returning from here, restart
        Utils.Log(TAG, "onStartCommand");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Bundle extras = intent.getExtras();
        Utils.Log(TAG, "onBind");
        // Get messager from the Activity
        if (extras != null) {
            Utils.Log("service", "onBind with extra");
        }
        return mBinder;
    }

    public void onGetUserInfo(){
        Utils.Log(TAG,"onGetUserInfo 1");
        BaseServiceView view = view();
        if (view == null) {
            return;
        }
        if (NetworkUtil.pingIpAddress(SuperSafeApplication.getInstance())) {
            return;
        }
        Utils.Log(TAG,"onGetUserInfo 2");
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
        hash.put(getString(R.string.key_device_id), SuperSafeApplication.getInstance().getDeviceId());
        subscriptions.add(SuperSafeApplication.serverAPI.onUserInfo(hash)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onResponse -> {
                    if (onResponse.error){
                        view.onError(onResponse.message,EnumStatus.USER_INFO);
                    }
                    else{
                        if (onResponse.premium!=null && onResponse.email_token!=null){
                            mUser.premium = onResponse.premium;
                            mUser.email_token = onResponse.email_token;
                            PrefsController.putString(getString(R.string.key_user),new Gson().toJson(mUser));
                            view.onSuccessful("Successful",EnumStatus.USER_INFO);
                        }
                    }
                    Utils.Log(TAG,"onGetUserInfo 3");
                    Utils.Log(TAG, "Body user info : " + new Gson().toJson(mUser));
                }, throwable -> {
                    if (throwable instanceof HttpException) {
                        ResponseBody bodys = ((HttpException) throwable).response().errorBody();
                        int code  = ((HttpException) throwable).response().code();
                        try {
                            if (code==403){
                                Utils.Log(TAG,"code "+code);
                                ServiceManager.getInstance().onUpdatedUserToken();
                            }
                            Utils.Log(TAG,"error " +bodys.string());
                            String msg = new Gson().toJson(bodys.string());
                            Utils.Log(TAG, msg);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Utils.Log(TAG, "Can not call " + throwable.getMessage());
                    }
                }));
    }

    public void onUpdateUserToken(){
        BaseServiceView view = view();
        if (isCheckNull(view,EnumStatus.UPDATE_USER_TOKEN)){
            return;
        }
        final User mUser = User.getInstance().getUserInfo();
        if (mUser==null){
            return;
        }
        Map<String,String> hash = new HashMap<>();
        hash.put(getString(R.string.key_user_id),mUser.email);
        hash.put(getString(R.string.key_other_email),mUser.other_email);
        hash.put(getString(R.string.key_change_email),""+mUser.change);
        hash.put(getString(R.string.key_active),""+mUser.active);
        hash.put(getString(R.string.key_device_id), SuperSafeApplication.getInstance().getDeviceId());
        Utils.onWriteLog(new Gson().toJson(hash),EnumStatus.REFRESH_EMAIL_TOKEN);
        subscriptions.add(SuperSafeApplication.serverAPI.onUpdateToken(hash)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onResponse -> {
                    if (onResponse.error){
                        view.onError(onResponse.message,EnumStatus.UPDATE_USER_TOKEN);
                    }
                    else{
                        if (onResponse.user!=null){
                            if (onResponse.user.author!=null){
                                final Authorization authorization = mUser.author;
                                authorization.session_token = onResponse.user.author.session_token;
                                mUser.author = authorization;
                                PrefsController.putString(getString(R.string.key_user),new Gson().toJson(mUser));
                                view.onSuccessful(onResponse.message,EnumStatus.UPDATE_USER_TOKEN);
                                Utils.onWriteLog(new Gson().toJson(mUser),EnumStatus.UPDATE_USER_TOKEN);
                            }
                        }
                    }
                    Utils.Log(TAG, "Body Update token: " + new Gson().toJson(onResponse));
                }, throwable -> {
                    if (throwable instanceof HttpException) {
                        ResponseBody bodys = ((HttpException) throwable).response().errorBody();
                        int code  = ((HttpException) throwable).response().code();
                        try {
                            if (code==403){
                                Utils.Log(TAG,"code "+code);
                                ServiceManager.getInstance().onUpdatedUserToken();
                            }
                            else if (code == 401){
                                final User user = User.getInstance().getUserInfo();
                                if (user!=null){
                                    onSignIn(user);
                                }
                            }
                            final String errorMessage = bodys.string();
                            Utils.Log(TAG, "error" + errorMessage);
                            view.onError(errorMessage, EnumStatus.UPDATE_USER_TOKEN);
                            Utils.onWriteLog(errorMessage,EnumStatus.UPDATE_USER_TOKEN);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Utils.Log(TAG, "Can not call " + throwable.getMessage());
                    }
                }));
    }

    public void onSignIn(final User request) {
        Utils.Log(TAG, "onSignIn");
        BaseServiceView view = view();
        if (isCheckNull(view,EnumStatus.SIGN_IN)){
            return;
        }
        Map<String, String> hash = new HashMap<>();
        hash.put(getString(R.string.key_email), request.email);
        hash.put(getString(R.string.key_password), SecurityUtil.key_password_default);
        hash.put(getString(R.string.key_device_id), SuperSafeApplication.getInstance().getDeviceId());
        hash.put(getString(R.string.key_device_type), getString(R.string.device_type));
        hash.put(getString(R.string.key_manufacturer), SuperSafeApplication.getInstance().getManufacturer());
        hash.put(getString(R.string.key_name_model), SuperSafeApplication.getInstance().getModel());
        hash.put(getString(R.string.key_version).toLowerCase(), "" + SuperSafeApplication.getInstance().getVersion());
        hash.put(getString(R.string.key_versionRelease), SuperSafeApplication.getInstance().getVersionRelease());
        hash.put(getString(R.string.key_appVersionRelease), SuperSafeApplication.getInstance().getAppVersionRelease());
        subscriptions.add(SuperSafeApplication.serverAPI.onSignIn(hash)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onResponse -> {
                    Utils.Log(TAG, "Body : " + new Gson().toJson(onResponse));
                    if (onResponse.error) {
                        view.onError(onResponse.message, EnumStatus.SIGN_IN);
                    } else {
                        final User user = User.getInstance().getUserInfo();
                        if (onResponse.user!=null){
                            final Authorization authorization = user.author;
                            authorization.session_token = onResponse.user.author.session_token;
                            user.author = authorization;
                        }
                        PrefsController.putString(getString(R.string.key_user), new Gson().toJson(user));
                    }
                }, throwable -> {
                    if (throwable instanceof HttpException) {
                        ResponseBody bodys = ((HttpException) throwable).response().errorBody();
                        try {
                            Utils.Log(TAG, "error" + bodys.string());
                            String msg = new Gson().toJson(bodys.string());
                            Utils.Log(TAG, msg);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Utils.Log(TAG, "Can not call " + throwable.getMessage());
                    }
                }));
    }

    public void getDriveAbout() {
        BaseServiceView view = view();
        if (isCheckNull(view,EnumStatus.GET_DRIVE_ABOUT)){
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
        Utils.Log(TAG, "access_token : " + access_token);
        view.onSuccessful(access_token, EnumStatus.GET_DRIVE_ABOUT);
        subscriptions.add(SuperSafeApplication.serverDriveApi.onGetDriveAbout(access_token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onResponse -> {
                    if (view == null) {
                        view.onError("View is null", EnumStatus.GET_DRIVE_ABOUT);
                        return;
                    }
                    if (onResponse.error != null) {
                        view.onError("Error " + new Gson().toJson(onResponse.error), EnumStatus.REQUEST_ACCESS_TOKEN);
                    } else {
                        final User mUser = User.getInstance().getUserInfo();
                        mUser.driveAbout = onResponse;
                        PrefsController.putString(getString(R.string.key_user), new Gson().toJson(mUser));
                        view.onSuccessful(new Gson().toJson(onResponse), EnumStatus.GET_DRIVE_ABOUT);
                    }
                    Utils.Log(TAG, "Body : " + new Gson().toJson(onResponse));
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
                            Utils.Log(TAG,"Exception....");
                            view.onError("Exception " + e.getMessage(), EnumStatus.GET_DRIVE_ABOUT);
                        }
                    } else {
                        Utils.Log(TAG, "Can not call " + throwable.getMessage());
                        view.onError("Error ^^:" + throwable.getMessage(), EnumStatus.GET_DRIVE_ABOUT);
                    }
                }));
    }

    public void onGetListFileInApp(BaseView<Integer> view) {
        Utils.Log(TAG, "onGetListFolderInApp");
        if (isCheckNull(view, EnumStatus.GET_LIST_FILES_IN_APP)){
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
        Utils.Log(TAG, "access_token : " + access_token);
        subscriptions.add(SuperSafeApplication.serverDriveApi.onGetListFileInAppFolder(access_token,SuperSafeApplication.getInstance().getString(R.string.key_appDataFolder))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.onStartLoading(EnumStatus.GET_LIST_FILES_IN_APP))
                .subscribe(onResponse -> {
                    Utils.Log(TAG, "Response data from items " + new Gson().toJson(onResponse));
                    if (view == null) {
                        Utils.Log(TAG, "View is null");
                        return;
                    }
                    view.onStopLoading(EnumStatus.GET_LIST_FILES_IN_APP);
                    if (onResponse.error != null) {
                        Utils.Log(TAG, "onError:" + new Gson().toJson(onResponse));
                        view.onError("Not found this id.... :" + new Gson().toJson(onResponse.error), EnumStatus.GET_LIST_FILES_IN_APP);
                    } else {
                        final int count = onResponse.files.size();
                        Utils.Log(TAG,"Total count request :" + count);
                        view.onSuccessful("Successful", EnumStatus.GET_LIST_FILES_IN_APP,count);
                    }
                }, throwable -> {
                    if (view == null) {
                        Utils.Log(TAG, "View is null");
                        return;
                    }
                    if (throwable instanceof HttpException) {
                        ResponseBody bodys = ((HttpException) throwable).response().errorBody();
                        int code  = ((HttpException) throwable).response().code();
                        try {
                            if (code==403){
                                Utils.Log(TAG,"code "+code);
                                ServiceManager.getInstance().onUpdatedUserToken();
                            }
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
                        Utils.Log(TAG, "Can not call " + throwable.getMessage());
                        view.onError("Error ^^:" + throwable.getMessage(), EnumStatus.GET_LIST_FILES_IN_APP);
                    }
                    view.onStopLoading(EnumStatus.GET_LIST_FILES_IN_APP);
                }));
    }

    /*Network request*/
    public void onCategoriesSync(MainCategories mainCategories, ServiceManager.ServiceManagerShortListener view) {
        Utils.Log(TAG, "onCategoriesSync");
        if (isCheckNull(view,EnumStatus.CATEGORIES_SYNC)){
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
        hashMap.put(getString(R.string.key_device_id), SuperSafeApplication.getInstance().getDeviceId());
        String access_token = user.access_token;
        Utils.Log(TAG, "access_token : " + access_token);
        subscriptions.add(SuperSafeApplication.serverAPI.onCategoriesSync(hashMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onResponse -> {
                    if (view == null) {
                        Utils.Log(TAG, "View is null");
                        view.onError("View is null", EnumStatus.CATEGORIES_SYNC);
                        return;
                    }
                    if (onResponse.error) {
                        Utils.Log(TAG, "onError 1");
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
                                    view.onSuccessful("Not found categories_hex_name - " + onResponse.category.categories_id,EnumStatus.CATEGORIES_SYNC);
                                }
                            }
                        }
                    }
                }, throwable -> {
                    if (view == null) {
                        Utils.Log(TAG, "View is null");
                        view.onError("View is null", EnumStatus.CATEGORIES_SYNC);
                        return;
                    }
                    if (throwable instanceof HttpException) {
                        ResponseBody bodys = ((HttpException) throwable).response().errorBody();
                        int code  = ((HttpException) throwable).response().code();
                        try {
                            if (code==403){
                                Utils.Log(TAG,"code "+code);
                                ServiceManager.getInstance().onUpdatedUserToken();
                            }
                            Utils.Log(TAG, "error" + bodys.string());
                            String msg = new Gson().toJson(bodys.string());
                            Utils.Log(TAG, msg);
                            view.onError("" + msg, EnumStatus.CATEGORIES_SYNC);
                        } catch (IOException e) {
                            e.printStackTrace();
                            view.onError("" + e.getMessage(), EnumStatus.CATEGORIES_SYNC);
                        }
                    } else {
                        Utils.Log(TAG, "Can not call " + throwable.getMessage());
                        view.onError("Error :" + throwable.getMessage(), EnumStatus.CATEGORIES_SYNC);
                    }
                }));
    }

    public void onDeleteCategoriesSync(MainCategories mainCategories, ServiceManager.ServiceManagerShortListener view) {
        Utils.Log(TAG, "onDeleteCategoriesSync");
        if (isCheckNull(view,EnumStatus.DELETE_CATEGORIES)){
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
        hashMap.put(getString(R.string.key_device_id), SuperSafeApplication.getInstance().getDeviceId());
        String access_token = user.access_token;
        Utils.Log(TAG, "access_token : " + access_token);
        subscriptions.add(SuperSafeApplication.serverAPI.onDeleteCategories(hashMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onResponse -> {
                    if (view == null) {
                        Utils.Log(TAG, "View is null");
                        view.onError("View is null", EnumStatus.DELETE_CATEGORIES);
                        return;
                    }
                    if (onResponse.error) {
                        Utils.Log(TAG, "onError 1");
                        view.onError(onResponse.message, EnumStatus.DELETE_CATEGORIES);
                    } else {
                        InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onDelete(mainCategories);
                        view.onSuccessful(onResponse.message, EnumStatus.DELETE_CATEGORIES);
                    }
                }, throwable -> {
                    if (view == null) {
                        Utils.Log(TAG, "View is null");
                        view.onError("View is null", EnumStatus.DELETE_CATEGORIES);
                        return;
                    }
                    if (throwable instanceof HttpException) {
                        ResponseBody bodys = ((HttpException) throwable).response().errorBody();
                        int code  = ((HttpException) throwable).response().code();
                        try {
                            if (code==403){
                                Utils.Log(TAG,"code "+code);
                                ServiceManager.getInstance().onUpdatedUserToken();
                            }
                            Utils.Log(TAG, "error" + bodys.string());
                            String msg = new Gson().toJson(bodys.string());
                            Utils.Log(TAG, msg);
                            view.onError("" + msg, EnumStatus.DELETE_CATEGORIES);
                        } catch (IOException e) {
                            e.printStackTrace();
                            view.onError("" + e.getMessage(), EnumStatus.DELETE_CATEGORIES);
                        }
                    } else {
                        Utils.Log(TAG, "Can not call " + throwable.getMessage());
                        view.onError("Error :" + throwable.getMessage(), EnumStatus.DELETE_CATEGORIES);
                    }
                }));
    }

    /*Create/Update for Categories*/
    public void onGetListCategoriesSync(ServiceManager.ServiceManagerShortListener view) {
        Utils.Log(TAG, "onGetListCategoriesSync");
        if (isCheckNull(view,EnumStatus.LIST_CATEGORIES_SYNC)){
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
        hashMap.put(getString(R.string.key_device_id), SuperSafeApplication.getInstance().getDeviceId());
        String access_token = user.access_token;
        Utils.Log(TAG, "access_token : " + access_token);
        subscriptions.add(SuperSafeApplication.serverAPI.onListCategoriesSync(hashMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onResponse -> {
                    if (view == null) {
                        Utils.Log(TAG, "View is null");
                        view.onError("View is null", EnumStatus.LIST_CATEGORIES_SYNC);
                        return;
                    }
                    if (onResponse.error) {
                        Utils.Log(TAG, "onError 1");
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
                                            Utils.Log(TAG,"Adding new main categories.......................................1");
                                        }
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
                        Utils.Log(TAG, "View is null");
                        view.onError("View is null", EnumStatus.LIST_CATEGORIES_SYNC);
                        return;
                    }
                    if (throwable instanceof HttpException) {
                        ResponseBody bodys = ((HttpException) throwable).response().errorBody();
                        int code  = ((HttpException) throwable).response().code();
                        try {
                            if (code==403){
                                Utils.Log(TAG,"code "+code);
                                ServiceManager.getInstance().onUpdatedUserToken();
                            }
                            Utils.Log(TAG, "error" + bodys.string());
                            String msg = new Gson().toJson(bodys.string());
                            Utils.Log(TAG, msg);
                            view.onError("" + msg, EnumStatus.LIST_CATEGORIES_SYNC);
                        } catch (IOException e) {
                            e.printStackTrace();
                            view.onError("" + e.getMessage(), EnumStatus.LIST_CATEGORIES_SYNC);
                        }
                    } else {
                        Utils.Log(TAG, "Can not call " + throwable.getMessage());
                        view.onError("Error :" + throwable.getMessage(), EnumStatus.LIST_CATEGORIES_SYNC);
                    }
                }));
    }

    public void onUpdateItems(final Items mItem, ServiceManager.ServiceManagerShortListener view) {
        final Items items = mItem;
        Utils.Log(TAG, "onAddItems");
        if (isCheckNull(view,EnumStatus.UPDATE)){
            return;
        }
        final User user = User.getInstance().getUserInfo();
        if (user == null) {
            view.onError("no user", EnumStatus.UPDATE);
            return;
        }
        if (!user.driveConnected) {
            view.onError("No Drive connected", EnumStatus.REQUEST_ACCESS_TOKEN);
            return;
        }
        if (items.categories_id == null || items.categories_id.equals("null")){
            view.onError("Categories id is null", EnumStatus.UPDATE);
        }
        // Map<String, Object> hashMap = new HashMap<>();
        final Map<String, Object> hashMap = Items.getInstance().objectToHashMap(items);
        if (hashMap != null) {
            hashMap.put(getString(R.string.key_user_id), user.email);
            hashMap.put(getString(R.string.key_cloud_id), user.cloud_id);
            hashMap.put(getString(R.string.key_kind), getString(R.string.key_drive_file));
            DriveEvent contentTitle = new DriveEvent();
            contentTitle.items_id = items.items_id;
            String hex = DriveEvent.getInstance().convertToHex(new Gson().toJson(contentTitle));
            hashMap.put(getString(R.string.key_name), hex);
            hashMap.put(getString(R.string.key_device_id), SuperSafeApplication.getInstance().getDeviceId());
        }
        String access_token = user.access_token;
        Utils.Log(TAG, "access_token : " + access_token);
        subscriptions.add(SuperSafeApplication.serverAPI.onSyncData(hashMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onResponse -> {
                    if (view == null) {
                        Utils.Log(TAG, "View is null");
                        return;
                    }
                    if (onResponse.error) {
                        Utils.Log(TAG, "onError:" + new Gson().toJson(onResponse));
                        mItem.isUpdate = false;
                        InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onUpdate(mItem);
                        view.onError("Queries add items is failed :" + onResponse.message, EnumStatus.UPDATE);
                    } else {
                        mItem.isUpdate = false;
                        InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onUpdate(mItem);
                        view.onSuccessful("Status Items :" + onResponse.message, EnumStatus.UPDATE);
                    }
                    Utils.Log(TAG,"Adding item Response "+ new Gson().toJson(onResponse));
                }, throwable -> {
                    if (view == null) {
                        Utils.Log(TAG, "View is null");
                        return;
                    }
                    if (throwable instanceof HttpException) {
                        ResponseBody bodys = ((HttpException) throwable).response().errorBody();
                        int code  = ((HttpException) throwable).response().code();
                        try {
                            if (code==403){
                                Utils.Log(TAG,"code "+code);
                                ServiceManager.getInstance().onUpdatedUserToken();
                            }
                            Utils.Log(TAG, "error" + bodys.string());
                            Utils.Log(TAG,"Adding item Response error"+ bodys.string());
                            view.onError("" +  bodys.string(), EnumStatus.UPDATE);
                        } catch (IOException e) {
                            e.printStackTrace();
                            view.onError("" + e.getMessage(), EnumStatus.UPDATE);
                        }
                    } else {
                        Utils.Log(TAG, "Can not call " + throwable.getMessage());
                        view.onError("Error :" + throwable.getMessage(), EnumStatus.UPDATE);
                    }
                }));
    }

    /*Date for Categories*/
    public void onAddItems(final Items mItem, ServiceManager.ServiceManagerShortListener view) {
        final Items items = mItem;
        Utils.Log(TAG, "onAddItems");
        if (isCheckNull(view,EnumStatus.ADD_ITEMS)){
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
            DriveEvent contentTitle = new DriveEvent();
            contentTitle.items_id = items.items_id;
            String hex = DriveEvent.getInstance().convertToHex(new Gson().toJson(contentTitle));
            hashMap.put(getString(R.string.key_name), hex);
            hashMap.put(getString(R.string.key_device_id), SuperSafeApplication.getInstance().getDeviceId());
        }
        String access_token = user.access_token;
        Utils.Log(TAG, "access_token : " + access_token);
        subscriptions.add(SuperSafeApplication.serverAPI.onSyncData(hashMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onResponse -> {
                    if (view == null) {
                        Utils.Log(TAG, "View is null");
                        return;
                    }
                    if (onResponse.error) {
                        Utils.Log(TAG, "onError:" + new Gson().toJson(onResponse));
                        view.onError("Queries add items is failed :" + onResponse.message, EnumStatus.ADD_ITEMS);
                    } else {
                        view.onSuccessful("Status Items :" + onResponse.message, EnumStatus.ADD_ITEMS);
                    }
                    Utils.Log(TAG,"Adding item Response "+ new Gson().toJson(onResponse));
                }, throwable -> {
                    if (view == null) {
                        Utils.Log(TAG, "View is null");
                        return;
                    }
                    if (throwable instanceof HttpException) {
                        ResponseBody bodys = ((HttpException) throwable).response().errorBody();
                        int code  = ((HttpException) throwable).response().code();
                        try {
                            if (code==403){
                                Utils.Log(TAG,"code "+code);
                                ServiceManager.getInstance().onUpdatedUserToken();
                            }
                            Utils.Log(TAG, "error" + bodys.string());
                            Utils.Log(TAG,"Adding item Response error"+ bodys.string());
                            view.onError("" +  bodys.string(), EnumStatus.ADD_ITEMS);
                        } catch (IOException e) {
                            e.printStackTrace();
                            view.onError("" + e.getMessage(), EnumStatus.ADD_ITEMS);
                        }
                    } else {
                        Utils.Log(TAG, "Can not call " + throwable.getMessage());
                        view.onError("Error :" + throwable.getMessage(), EnumStatus.ADD_ITEMS);
                    }
                }));
    }

    /*Get List Categories*/
    public void onDeleteCloudItems(final Items items, final boolean isOriginalGlobalId, final ServiceManager.ServiceManagerShortListener view) {
        Utils.Log(TAG, "onDeleteCloudItems");
        if (isCheckNull(view, EnumStatus.DELETE_SYNC_CLOUD_DATA)){
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
        Utils.Log(TAG, "access_token : " + access_token);
        String id = "";
        if (isOriginalGlobalId) {
            id = items.global_original_id;
        } else {
            id = items.global_thumbnail_id;
        }
        subscriptions.add(SuperSafeApplication.serverDriveApi.onDeleteCloudItem(access_token, id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
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
                        Utils.Log(TAG, "View is null");
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
                        Utils.Log(TAG, "Can not call " + throwable.getMessage());
                        view.onError("Error 0:" + throwable.getMessage(), EnumStatus.DELETE_SYNC_CLOUD_DATA);
                    }
                }));
    }

    public void onDeleteOwnSystem(final Items items, final ServiceManager.ServiceManagerShortListener view) {
        Utils.Log(TAG, "onDeleteOwnSystem");
        if (isCheckNull(view,EnumStatus.DELETE_SYNC_OWN_DATA)){
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
        hashMap.put(getString(R.string.key_device_id), SuperSafeApplication.getInstance().getDeviceId());
        String access_token = user.access_token;
        Utils.Log(TAG, "access_token : " + access_token);
        subscriptions.add(SuperSafeApplication.serverAPI.onDeleteOwnItems(hashMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onResponse -> {
                    Utils.Log(TAG, "Response data from items " + new Gson().toJson(onResponse));
                    if (view == null) {
                        Utils.Log(TAG, "View is null");
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
                }, throwable -> {
                    if (view == null) {
                        Utils.Log(TAG, "View is null");
                        return;
                    }
                    if (throwable instanceof HttpException) {
                        ResponseBody bodys = ((HttpException) throwable).response().errorBody();
                        int code  = ((HttpException) throwable).response().code();
                        try {
                            if (code==403){
                                Utils.Log(TAG,"code "+code);
                                ServiceManager.getInstance().onUpdatedUserToken();
                            }
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
                        Utils.Log(TAG, "Can not call " + throwable.getMessage());
                        view.onError("Error :" + throwable.getMessage(), EnumStatus.DELETE_SYNC_OWN_DATA);
                    }
                }));
    }

    public void onGetListSync(String nextPage, ServiceManager.ServiceManagerShortListener view) {
        Utils.Log(TAG, "onGetListSync");
        if (isCheckNull(view,EnumStatus.GET_LIST_FILE)){
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
        hashMap.put(getString(R.string.key_device_id), SuperSafeApplication.getInstance().getDeviceId());
        String access_token = user.access_token;
        Utils.Log(TAG, "access_token : " + access_token);
        subscriptions.add(SuperSafeApplication.serverAPI.onListFilesSync(hashMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onResponse -> {
                    if (view == null) {
                        Utils.Log(TAG, "View is null");
                        view.onError("View is null", EnumStatus.GET_LIST_FILE);
                        return;
                    }
                    if (onResponse.error) {
                        Utils.Log(TAG, "onError 1");
                        view.onError(onResponse.message, EnumStatus.GET_LIST_FILE);
                    } else {
                        final List<MainCategories> listCategories = onResponse.listCategories;
                        final List<Items> driveResponse = onResponse.files;
                        final HashMap<String,MainCategories> currentCategories = MainCategories.getInstance().getMainCurrentCategories();
                        boolean isNewCategories = false;
                        if (onResponse.nextPage == null) {
                            try {
                                //Utils.Log(TAG,"Special values "+new Gson().toJson(listCategories));
                                for (MainCategories index : listCategories) {
                                    hashMapGlobalCategories.put(index.categories_id, index.categories_id);
                                    MainCategories main = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getCategoriesId(index.categories_id,false);
                                    if (main != null) {
                                        if (!main.isChange && !main.isDelete) {
                                            main.isSyncOwnServer = true;
                                            main.categories_name = index.categories_name;
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
                                            Utils.Log(TAG,"Adding new main categories.......................................2");
                                            isNewCategories = true;
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
                                Utils.Log(TAG, "Ready for sync");
                                if (isNewCategories){
                                    view.onSuccessful(onResponse.nextPage, EnumStatus.RELOAD);
                                }
                                else{
                                    view.onSuccessful(onResponse.nextPage, EnumStatus.SYNC_READY);
                                }
                            }
                        } else {
                            try {
                                view.onSuccessful(onResponse.message, EnumStatus.GET_LIST_FILE);
                                for (Items index : driveResponse) {
                                    hashMapGlobal.put(index.items_id, index.items_id);
                                    Items itemsResponse = new Items(index);
                                        DriveEvent driveTitle = DriveEvent.getInstance().hexToObject(index.name);
                                        if (driveTitle != null) {
                                            final Items items = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getItemId(driveTitle.items_id,false);
                                            EnumFormatType formatTypeFile = EnumFormatType.values()[itemsResponse.formatType];
                                            if (items == null) {
                                                switch (formatTypeFile) {
                                                    case AUDIO: {
                                                        itemsResponse.thumbnailSync = true;
                                                        break;
                                                    }
                                                    case FILES:{
                                                        itemsResponse.thumbnailSync = true;
                                                        break;
                                                    }
                                                    default: {
                                                        itemsResponse.originalSync = false;
                                                        itemsResponse.thumbnailSync = false;
                                                        break;
                                                    }
                                                }
                                                final MainCategories main = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getCategoriesId(index.categories_id,false);
                                                if (main != null) {
                                                    itemsResponse.categories_local_id = main.categories_local_id;
                                                    onSaveItem(itemsResponse);
                                                } else {
                                                    Utils.Log(TAG,"..................categories_id is nul............."+index.categories_id);
                                                }
                                            } else {
                                                items.global_original_id = index.global_original_id;
                                                items.global_thumbnail_id = index.global_thumbnail_id;
                                                items.categories_id = index.categories_id;
                                                if (currentCategories!=null){
                                                    final MainCategories main = currentCategories.get(index.categories_id);
                                                    if (main!=null){
                                                        final String categories_local_id = main.categories_local_id;
                                                        if (categories_local_id!=null){
                                                            items.categories_local_id = categories_local_id;
                                                        }
                                                    }
                                                }
                                                InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onUpdate(items);
                                                Utils.Log(TAG, "This item is existing");
                                            }
                                        } else {
                                            view.onError("Can not convert item", EnumStatus.GET_LIST_FILE);
                                            Utils.Log(TAG, "Can not convert item");
                                        }
                                }
                            } catch (Exception e) {
                                view.onError("Error "+e.getMessage(), EnumStatus.GET_LIST_FILE);
                                e.printStackTrace();
                            } finally {
                                Utils.Log(TAG, "Load more");
                                view.onSuccessful(onResponse.nextPage, EnumStatus.LOAD_MORE);
                            }
                        }
                    }
                }, throwable -> {
                    if (view == null) {
                        Utils.Log(TAG, "View is null");
                        view.onError("View is null", EnumStatus.GET_LIST_FILE);
                        return;
                    }
                    if (throwable instanceof HttpException) {
                        ResponseBody bodys = ((HttpException) throwable).response().errorBody();
                        int code  = ((HttpException) throwable).response().code();
                        try {
                            if (code==403){
                                Utils.Log(TAG,"code "+code);
                                ServiceManager.getInstance().onUpdatedUserToken();
                            }
                            Utils.Log(TAG, "error" + bodys.string());
                            String msg = new Gson().toJson(bodys.string());
                            Utils.Log(TAG, msg);
                            view.onError("" + msg, EnumStatus.GET_LIST_FILE);
                        } catch (IOException e) {
                            e.printStackTrace();
                            view.onError("" + e.getMessage(), EnumStatus.GET_LIST_FILE);
                        }
                    } else {
                        Utils.Log(TAG, "Can not call " + throwable.getMessage());
                        view.onError("Error :" + throwable.getMessage(), EnumStatus.GET_LIST_FILE);
                    }
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
            //GalleryCameraMediaManager.getInstance().onUpdatedView();
            EventBus.getDefault().post(EnumStatus.RELOAD);
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
            //GalleryCameraMediaManager.getInstance().onUpdatedView();
            EventBus.getDefault().post(EnumStatus.RELOAD);
            /*Note here*/
        }
    }

    public void onSaveItem(final Items mItem) {
        final Items items = new Items(mItem);
        Utils.Log(TAG, "onSaveItem");
        Utils.onWriteLog(new Gson().toJson(items),EnumStatus.CREATE);
        boolean isSaver;
        EnumFormatType formatType = EnumFormatType.values()[items.formatType];
        switch (formatType){
            case IMAGE:{
                isSaver = PrefsController.getBoolean(getString(R.string.key_saving_space),false);
                items.originalSync = isSaver;
                break;
            }
            default:{
                items.originalSync = false;
                break;
            }
        }
        items.isExport = false;
        items.isWaitingForExporting = false;
        items.custom_items = 0;
        items.isSyncCloud = false;
        items.isSyncOwnServer = false;
        items.isUpdate = false;
        items.statusAction = EnumStatus.DOWNLOAD.ordinal();
        InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onInsert(items);
    }

    public void onDownloadFile(final Items items,String destination, final ServiceManager.DownloadServiceListener listener) {
        Utils.Log(TAG, "onDownloadFile !!!!");
        final User mUser = User.getInstance().getUserInfo();
        if (!mUser.driveConnected) {
            listener.onError("No Drive api connected", EnumStatus.DOWNLOAD);
            return;
        }
        if (mUser.access_token==null){
            listener.onError("No Access token", EnumStatus.DOWNLOAD);
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
        request.Authorization = mUser.access_token;
        request.id = id;
        if (destination==null){
            String path = SuperSafeApplication.getInstance().getSupersafePrivate();
            String pathFolder = path + items.items_id + "/";
            destination = pathFolder;
        }
        request.path_folder_output = destination;
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
                Utils.Log(TAG, "Progressing downloaded " + percent + "%");
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
                        /*Not Found file*/
                        if (code == 404) {
                            Utils.Log(TAG,"isDelete local id error");
                            mItem.isDeleteLocal = true;
                            mItem.originalSync = true;
                            mItem.thumbnailSync = true;
                            mItem.deleteAction = EnumDelete.DELETE_WAITING.ordinal();
                            InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onUpdate(items);
                        }
                    }
                }
            }
            @Override
            public Map<String, String> onHeader() {
                return new HashMap<>();
            }
        });
        downloadService.downloadFileFromGoogleDrive(request);
    }

    public void onUploadFileInAppFolder(final Items items, final ServiceManager.UploadServiceListener listener) {
        Utils.Log(TAG, "onUploadFileInAppFolder");
        final User mUser = User.getInstance().getUserInfo();
        MediaType contentType = MediaType.parse("application/json; charset=UTF-8");
        HashMap<String, Object> content = new HashMap<>();
        DriveEvent contentEvent = new DriveEvent();
        File file = null;
        if (items.isOriginalGlobalId) {
            contentEvent.fileType = EnumFileType.ORIGINAL.ordinal();
            file = new File(items.originalPath);
        } else {
            contentEvent.fileType = EnumFileType.THUMBNAIL.ordinal();
            file = new File(items.thumbnailPath);
        }
        if (!storage.isFileExist(file.getAbsolutePath())) {
            InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onDelete(items);
            listener.onError("This path is not found", EnumStatus.UPLOAD);
            return;
        }
        contentEvent.items_id = items.items_id;
        String hex = DriveEvent.getInstance().convertToHex(new Gson().toJson(contentEvent));
        content.put(getString(R.string.key_name), hex);
        List<String> list = new ArrayList<>();
        list.add(getString(R.string.key_appDataFolder));
        content.put(getString(R.string.key_parents), list);
        MultipartBody.Part metaPart = MultipartBody.Part.create(RequestBody.create(contentType, new Gson().toJson(content)));
        ProgressRequestBody fileBody = new ProgressRequestBody(file, new ProgressRequestBody.UploadCallbacks() {
            @Override
            public void onProgressUpdate(int percentage) {
                Utils.Log(TAG, "Progressing uploaded " + percentage + "%");
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
        Call<DriveResponse> request = SuperSafeApplication.serverDriveApi.uploadFileMultipleInAppFolder(mUser.access_token, metaPart, dataPart, items.mimeType);
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
        Utils.Log(TAG, "getDriveAbout");
        if (isCheckNull(view,EnumStatus.GET_DRIVE_ABOUT)){
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
        Utils.Log(TAG, "access_token : " + access_token);
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
                    Utils.Log(TAG, "Body : " + new Gson().toJson(onResponse));
                }, throwable -> {
                    if (view == null) {
                        Utils.Log(TAG, "View is null");
                        return;
                    }
                    if (throwable instanceof HttpException) {
                        ResponseBody bodys = ((HttpException) throwable).response().errorBody();
                        try {
                            if (view==null){
                                return;
                            }
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
                        Utils.Log(TAG, "Can not call " + throwable.getMessage());
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

    /*TrackHandler*/
    public void onCheckVersion(){
        Utils.Log(TAG,"onCheckVersion");
        BaseServiceView view = view();
        if (isCheckNull(view,EnumStatus.CHECK_VERSION)){
            return;
        }
        subscriptions.add(SuperSafeApplication.serverAPI.onCheckVersion()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onResponse -> {
                    if (onResponse!=null){
                        if (onResponse.version!=null){
                            view.onSuccessful("Successful",EnumStatus.CHECK_VERSION);
                            final User user = User.getInstance().getUserInfo();
                            user.version = onResponse.version;
                            PrefsController.putString(getString(R.string.key_user),new Gson().toJson(user) );
                        }
                    }
                    Utils.Log(TAG, "Body check version: " + new Gson().toJson(onResponse));
                }, throwable -> {
                    if (throwable instanceof HttpException) {
                        ResponseBody bodys = ((HttpException) throwable).response().errorBody();
                        try {
                            Utils.Log(TAG,"error" +bodys.string());
                            String msg = new Gson().toJson(bodys.string());
                            Utils.Log(TAG, msg);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Utils.Log(TAG, "Can not call" + throwable.getMessage());
                    }
                }));
    }

    public void onSyncAuthorDevice(){
        Utils.Log(TAG,"onSyncAuthorDevice");
        BaseServiceView view = view();
        if (isCheckNull(view,EnumStatus.AUTHOR_SYNC)){
            return;
        }
        final User user = User.getInstance().getUserInfo();
        String user_id = "null@gmail.com";
        if (user!=null){
            user_id = user.email;
        }
        Map<String,String> hash = new HashMap<>();
        hash.put(getString(R.string.key_device_id), SuperSafeApplication.getInstance().getDeviceId());
        hash.put(getString(R.string.key_device_type),getString(R.string.device_type));
        hash.put(getString(R.string.key_manufacturer), SuperSafeApplication.getInstance().getManufacturer());
        hash.put(getString(R.string.key_name_model), SuperSafeApplication.getInstance().getModel());
        hash.put(getString(R.string.key_version),""+ SuperSafeApplication.getInstance().getVersion());
        hash.put(getString(R.string.key_versionRelease), SuperSafeApplication.getInstance().getVersionRelease());
        hash.put(getString(R.string.key_appVersionRelease),BuildConfig.VERSION_NAME);
        hash.put(getString(R.string.key_user_id),user_id);
        subscriptions.add(SuperSafeApplication.serverAPI.onAuthor(hash)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onResponse -> {
                    Utils.Log(TAG, "Body author device: " + new Gson().toJson(onResponse));
                }, throwable -> {
                    if (throwable instanceof HttpException) {
                        ResponseBody bodys = ((HttpException) throwable).response().errorBody();
                        int code  = ((HttpException) throwable).response().code();
                        try {
                            if (code==403){
                                Utils.Log(TAG,"code "+code);
                                ServiceManager.getInstance().onUpdatedUserToken();
                            }
                            Utils.Log(TAG,"Author error" +bodys.string());
                            String msg = new Gson().toJson(bodys.string());
                            Utils.Log(TAG, msg);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Utils.Log(TAG, "Author Can not call" + throwable.getMessage());
                    }
                }));
    }

    /*Email token*/
    public void onSendMail(EmailToken request){
        Utils.Log(TAG, "onSendMail.....");
        BaseServiceView view = view();
        if (isCheckNull(view,EnumStatus.SEND_EMAIL)){
            return;
        }
        final User mUser = User.getInstance().getUserInfo();
        if (mUser==null){
            return;
        }
        Call<ResponseBody> response = SuperSafeApplication.serviceGraphMicrosoft.onSendMail(request.access_token, request);
        response.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    final int code = response.code();
                    if (code == 401) {
                        Utils.Log(TAG, "code " + code);
                        onRefreshEmailToken(request);
                        final String errorMessage = response.errorBody().string();
                        Utils.Log(TAG, "error" + errorMessage);
                        view.onError(errorMessage, EnumStatus.SEND_EMAIL);
                        mUser.isWaitingSendMail = false;
                        PrefsController.putString(getString(R.string.key_user),new Gson().toJson(mUser));
                    } else if (code == 202) {
                        Utils.Log(TAG, "code " + code);
                        view.onSuccessful("successful", EnumStatus.SEND_EMAIL);
                        final User mUser = User.getInstance().getUserInfo();
                        mUser.isWaitingSendMail = false;
                        PrefsController.putString(getString(R.string.key_user),new Gson().toJson(mUser));
                        ServiceManager.getInstance().onDismissServices();
                        Utils.Log(TAG, "Body : Send email Successful");
                    } else {
                        Utils.Log(TAG, "code " + code);
                        Utils.Log(TAG, "Nothing to do");
                        view.onError("Null", EnumStatus.SEND_EMAIL);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utils.Log(TAG, "response failed :" + t.getMessage());
            }
        });
    }

    public void onRefreshEmailToken(EmailToken request) {
        Utils.Log(TAG, "onRefreshEmailToken.....");
        BaseServiceView view = view();
        if (isCheckNull(view,EnumStatus.REFRESH)){
            return;
        }
        final User mUser = User.getInstance().getUserInfo();
        Map<String, Object> hash = new HashMap<>();
        hash.put(getString(R.string.key_client_id), request.client_id);
        hash.put(getString(R.string.key_redirect_uri), request.redirect_uri);
        hash.put(getString(R.string.key_grant_type), request.grant_type);
        hash.put(getString(R.string.key_refresh_token), request.refresh_token);
        subscriptions.add(SuperSafeApplication.serviceGraphMicrosoft.onRefreshEmailToken(RootAPI.REFRESH_TOKEN, hash)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onResponse -> {
                    if (onResponse != null) {
                        EmailToken token = mUser.email_token;
                        token.access_token = onResponse.token_type + " " + onResponse.access_token;
                        token.refresh_token = onResponse.refresh_token;
                        token.token_type = onResponse.token_type;
                        mUser.email_token = token;
                        PrefsController.putString(getString(R.string.key_user), new Gson().toJson(mUser));
                        onAddEmailToken();
                    }
                    view.onSuccessful("successful", EnumStatus.REFRESH);
                    Utils.Log(TAG, "Body refresh : " + new Gson().toJson(onResponse));
                }, throwable -> {
                    if (throwable instanceof HttpException) {
                        ResponseBody bodys = ((HttpException) throwable).response().errorBody();
                        int code = ((HttpException) throwable).response().code();
                        try {
                            if (code == 401) {
                                Utils.Log(TAG, "code " + code);
                            }
                            Utils.Log(TAG, "error" + bodys.string());
                            String msg = new Gson().toJson(bodys.string());
                            view.onError(msg, EnumStatus.SEND_EMAIL);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Utils.Log(TAG, "Can not call " + throwable.getMessage());
                    }
                }));
    }

    public void onAddEmailToken() {
        Utils.Log(TAG, "onSignIn.....");
        BaseServiceView view = view();
        if (isCheckNull(view,EnumStatus.ADD_EMAIL_TOKEN)){
            return;
        }
        final User mUser = User.getInstance().getUserInfo();
        Map<String, Object> hash = new HashMap<>();
        hash.put(getString(R.string.key_user_id), mUser.email);
        hash.put(getString(R.string.key_device_id), SuperSafeApplication.getInstance().getDeviceId());
        hash.put(getString(R.string.key_refresh_token), mUser.email_token.refresh_token);
        hash.put(getString(R.string.key_access_token), mUser.email_token.access_token);
        subscriptions.add(SuperSafeApplication.serverAPI.onAddEmailToken(hash)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onResponse -> {
                    Utils.Log(TAG, "Body : " + new Gson().toJson(onResponse));
                    final EmailToken emailToken = EmailToken.getInstance().convertObject(mUser,EnumStatus.RESET);
                    onSendMail(emailToken);
                }, throwable -> {
                    if (throwable instanceof HttpException) {
                        ResponseBody bodys = ((HttpException) throwable).response().errorBody();
                        int code = ((HttpException) throwable).response().code();
                        try {
                            if (code == 403) {
                                Utils.Log(TAG, "code " + code);
                                ServiceManager.getInstance().onUpdatedUserToken();
                            }
                            final String errorMessage = bodys.string();
                            Utils.Log(TAG, "error" + errorMessage);
                            view.onError(errorMessage, EnumStatus.ADD_EMAIL_TOKEN);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Utils.Log(TAG, "Can not call " + throwable.getMessage());
                    }
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
        }
    }

    public <T> boolean isCheckNull(T view,EnumStatus status){
        if(subscriptions==null){
            Utils.Log(TAG,"Subscriptions is null " + status.name());
            return true;
        }
        else if (NetworkUtil.pingIpAddress(SuperSafeApplication.getInstance())) {
            Utils.Log(TAG,"No connection " + status.name());
            return true;
        }
        else if(view==null){
            Utils.Log(TAG,"View is null " + status.name());
            return true;
        }
        return false;
    }
}
