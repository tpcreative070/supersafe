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
import java.io.IOException;
import co.tpcreative.suppersafe.R;
import co.tpcreative.suppersafe.common.api.RootAPI;
import co.tpcreative.suppersafe.common.controller.PrefsController;
import co.tpcreative.suppersafe.common.presenter.PresenterService;
import co.tpcreative.suppersafe.common.request.DriveApiRequest;
import co.tpcreative.suppersafe.common.util.NetworkUtil;
import co.tpcreative.suppersafe.model.User;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.HttpException;

public class SupperSafeService extends PresenterService<SupperSafeServiceView> implements SupperSafeReceiver.ConnectivityReceiverListener {


    private final IBinder mBinder = new LocalBinder(); // Binder given to clients
    private Intent mIntent;
    private SupperSafeServiceListener listener;
    private SupperSafeReceiver androidReceiver;
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
        subscriptions.add(SupperSafeApplication.serverAPI.onGetDriveAbout(access_token,RootAPI.GET_DRIVE_ABOUT)
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
        request.mimeType = "application/vnd.google-apps.folder";
        request.name = "New Folder Name";

        String access_token = user.access_token;
        Log.d(TAG,"access_token : " + access_token);
        subscriptions.add(SupperSafeApplication.serverAPI.onCrateFolder(access_token,RootAPI.CREATE_FOLDẺ,request)
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
        subscriptions.add(SupperSafeApplication.serverAPI.onGetDriveAbout(access_token,RootAPI.GET_DRIVE_ABOUT)
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
