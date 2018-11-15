package co.tpcreative.supersafe.ui.checksystem;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.creativityapps.gmailbackgroundlibrary.BackgroundMail;
import com.google.gson.Gson;
import com.snatik.storage.security.SecurityUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import co.tpcreative.supersafe.BuildConfig;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.presenter.Presenter;
import co.tpcreative.supersafe.common.request.SignInRequest;
import co.tpcreative.supersafe.common.request.SignUpRequest;
import co.tpcreative.supersafe.common.request.UserCloudRequest;
import co.tpcreative.supersafe.common.request.VerifyCodeRequest;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.NetworkUtil;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.GoogleOauth;
import co.tpcreative.supersafe.model.User;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.HttpException;

public class CheckSystemPresenter extends Presenter<BaseView>{

    private final String TAG = CheckSystemPresenter.class.getSimpleName();
    protected User mUser;
    protected GoogleOauth googleOauth;
    protected boolean isUserExisting;


    public CheckSystemPresenter(){
        final User user = User.getInstance().getUserInfo();
        if (user!=null){
            mUser = user;
        }
    }

    public void getIntent(Activity activity){
        try {
            Bundle bundle = activity.getIntent().getExtras();
            googleOauth = (GoogleOauth) bundle.get(getString(R.string.key_google_oauth));
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }


    public void onCheckUser(final String email){
        Log.d(TAG,"info onCheckUser");
        BaseView view = view();
        if (view == null) {
            return;
        }
        if (NetworkUtil.pingIpAddress(view.getContext())) {
            return;
        }
        if (subscriptions == null) {
            return;
        }

        Map<String,String> hash = new HashMap<>();
        hash.put(getString(R.string.key_user_id),email);
        hash.put(getString(R.string.key_device_id), SuperSafeApplication.getInstance().getDeviceId());
        subscriptions.add(SuperSafeApplication.serverAPI.onCheckUserId(hash)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.onStartLoading(EnumStatus.USER_ID_EXISTING))
                .subscribe(onResponse -> {
                    Log.d(TAG, "Body : " + new Gson().toJson(onResponse));
                    if (onResponse.error){
                        SignUpRequest request = new SignUpRequest();
                        request.email = email;
                        request.name = "Google";
                        onSignUp(request);
                    }
                    else{
                        //view.onSuccessful(email,!onResponse.error);
                        view.onSuccessful(email,EnumStatus.USER_ID_EXISTING);
                        SignInRequest request = new SignInRequest();
                        request.email = email;
                        onSignIn(request);
                        isUserExisting = !onResponse.error;
                    }
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
                        Log.d(TAG, "Can not call check user" + throwable.getMessage());
                    }
                    view.onStopLoading(EnumStatus.USER_ID_EXISTING);
                }));
    }

    public void onSignIn(SignInRequest request){
        Log.d(TAG,"info");
        BaseView view = view();
        if (view == null) {
            return;
        }
        if (NetworkUtil.pingIpAddress(view.getContext())) {
            return;
        }
        if (subscriptions == null) {
            return;
        }

        Map<String,String> hash = new HashMap<>();
        hash.put(getString(R.string.key_email),request.email);
        hash.put(getString(R.string.key_password), SecurityUtil.key_password_default);
        hash.put(getString(R.string.key_device_id), SuperSafeApplication.getInstance().getDeviceId());
        hash.put(getString(R.string.key_device_type),getString(R.string.device_type));
        hash.put(getString(R.string.key_manufacturer), SuperSafeApplication.getInstance().getManufacturer());
        hash.put(getString(R.string.key_name_model), SuperSafeApplication.getInstance().getModel());
        hash.put(getString(R.string.key_version),""+ SuperSafeApplication.getInstance().getVersion());
        hash.put(getString(R.string.key_versionRelease), SuperSafeApplication.getInstance().getVersionRelease());
        hash.put(getString(R.string.key_appVersionRelease), SuperSafeApplication.getInstance().getAppVersionRelease());
        subscriptions.add(SuperSafeApplication.serverAPI.onSignIn(hash)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.onStartLoading(EnumStatus.SIGN_IN))
                .subscribe(onResponse -> {
                    if (onResponse.error){
                        view.onError(onResponse.message,EnumStatus.SIGN_IN);
                    }
                    else{
                        mUser = onResponse.user;
                        PrefsController.putString(getString(R.string.key_user),new Gson().toJson(onResponse.user));
                        onSendGmail(mUser.email,onResponse.user.code);
                    }
                    Log.d(TAG, "Body : " + new Gson().toJson(onResponse));
                }, throwable -> {
                    if (throwable instanceof HttpException) {
                        ResponseBody bodys = ((HttpException) throwable).response().errorBody();
                        try {
                            Log.d(TAG,"error" +bodys.string());
                            String msg = new Gson().toJson(bodys.string());
                            view.onError(msg,EnumStatus.SIGN_IN);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.d(TAG, "Can not call sign in" + throwable.getMessage());
                    }
                    view.onStopLoading(EnumStatus.SIGN_IN);
                }));
    }

    public void onSignUp(SignUpRequest request){
        Log.d(TAG,"info");
        BaseView view = view();
        if (view == null) {
            return;
        }
        if (NetworkUtil.pingIpAddress(view.getContext())) {
            return;
        }
        if (subscriptions == null) {
            return;
        }

        Map<String,String> hash = new HashMap<>();
        hash.put(getString(R.string.key_email),request.email);
        hash.put(getString(R.string.key_password),SecurityUtil.key_password_default);
        hash.put(getString(R.string.key_name),request.name);
        hash.put(getString(R.string.key_device_id), SuperSafeApplication.getInstance().getDeviceId());
        hash.put(getString(R.string.key_device_type),getString(R.string.device_type));
        hash.put(getString(R.string.key_manufacturer), SuperSafeApplication.getInstance().getManufacturer());
        hash.put(getString(R.string.key_name_model), SuperSafeApplication.getInstance().getModel());
        hash.put(getString(R.string.key_version),""+ SuperSafeApplication.getInstance().getVersion());
        hash.put(getString(R.string.key_versionRelease), SuperSafeApplication.getInstance().getVersionRelease());
        hash.put(getString(R.string.key_appVersionRelease), SuperSafeApplication.getInstance().getAppVersionRelease());
        subscriptions.add(SuperSafeApplication.serverAPI.onSignUP(hash)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.onStartLoading(EnumStatus.SIGN_UP))
                .subscribe(onResponse -> {
                    if (onResponse.error){
                        view.onError(onResponse.message,EnumStatus.SIGN_UP);
                    }
                    else{
                        PrefsController.putString(getString(R.string.key_user),new Gson().toJson(onResponse.user));
                        String code = onResponse.user.code;
                        mUser = onResponse.user;
                        if (code!=null){
                            onSendGmail(request.email,code);
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
                        Log.d(TAG, "Can not call sign up" + throwable.getMessage());
                    }
                    view.onStopLoading(EnumStatus.SIGN_UP);
                }));
    }


    public void onVerifyCode(VerifyCodeRequest request){
        Log.d(TAG,"info");
        BaseView view = view();
        if (view == null) {
            return;
        }
        if (NetworkUtil.pingIpAddress(view.getContext())) {
            return;
        }
        if (subscriptions == null) {
            return;
        }

        Map<String,String> hash = new HashMap<>();
        hash.put(getString(R.string.key_user_id),request.email);
        hash.put(getString(R.string.key_device_id), SuperSafeApplication.getInstance().getDeviceId());
        hash.put(getString(R.string.key_code),request.code);
        hash.put(getString(R.string.key_appVersionRelease),SuperSafeApplication.getInstance().getAppVersionRelease());
        subscriptions.add(SuperSafeApplication.serverAPI.onVerifyCode(hash)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.onStartLoading(EnumStatus.VERIFY_CODE))
                .subscribe(onResponse -> {
                    if (onResponse.error){
                        view.onError(onResponse.message,EnumStatus.VERIFY_CODE);
                    }
                    else{
                        view.onSuccessful(onResponse.message,EnumStatus.VERIFY_CODE);
                        final User mUser = User.getInstance().getUserInfo();
                        if (mUser!=null){
                            mUser.verified = true;
                            PrefsController.putString(getString(R.string.key_user),new Gson().toJson(mUser));
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
                        Log.d(TAG, "Can not call verify code" + throwable.getMessage());
                    }
                    view.onStopLoading(EnumStatus.VERIFY_CODE);
                }));
    }

    public void onResendCode(VerifyCodeRequest request){
        Log.d(TAG,"info");
        BaseView view = view();
        if (view == null) {
            return;
        }
        if (NetworkUtil.pingIpAddress(view.getContext())) {
            return;
        }
        if (subscriptions == null) {
            return;
        }

        Map<String,String> hash = new HashMap<>();
        hash.put(getString(R.string.key_user_id),request.email);
        hash.put(getString(R.string.key_device_id), SuperSafeApplication.getInstance().getDeviceId());
        subscriptions.add(SuperSafeApplication.serverAPI.onResendCode(hash)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.onStartLoading(EnumStatus.RESEND_CODE))
                .subscribe(onResponse -> {
                    if (onResponse.error){
                        view.onError(onResponse.message,EnumStatus.RESEND_CODE);
                        view.onStopLoading(EnumStatus.RESEND_CODE);
                    }
                    else{
                        onSendGmail(googleOauth.email,onResponse.code);
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
                        Log.d(TAG, "Can not call" + throwable.getMessage());
                    }
                    view.onStopLoading(EnumStatus.RESEND_CODE);
                }));
    }


    public void onSendGmail(String email,String code){
        BaseView view = view();
        String body = String.format(getString(R.string.send_code),code);
        String title = String.format(getString(R.string.send_code_title),code);
        BackgroundMail.newBuilder(view.getActivity())
                .withUsername(SecurityUtil.user_name)
                .withPassword(SecurityUtil.password)
                .withMailto(email)
                .withType(BackgroundMail.TYPE_PLAIN)
                .withSubject(title)
                .withBody(body)
                .withOnSuccessCallback(new BackgroundMail.OnSuccessCallback() {
                    @Override
                    public void onSuccess() {
                        //do some magic
                        Log.d(TAG,"Successful");
                        if (view!=null){
                            view.onSuccessful("Successful",EnumStatus.SEND_EMAIL);
                        }
                    }
                })
                .withOnFailCallback(new BackgroundMail.OnFailCallback() {
                    @Override
                    public void onFail() {
                        //do some magic
                        Log.d(TAG,"Failed");
                        if (view!=null){
                            view.onStopLoading(EnumStatus.OTHER);
                        }
                    }
                })
                .send();
    }

    public void onAddUserCloud(UserCloudRequest cloudRequest){
        Log.d(TAG,"info");
        BaseView view = view();
        if (view == null) {
            return;
        }
        if (NetworkUtil.pingIpAddress(view.getContext())) {
            return;
        }
        if (subscriptions == null) {
            return;
        }

        Map<String,String> hash = new HashMap<>();
        hash.put(getString(R.string.key_user_id),cloudRequest.user_id);
        hash.put(getString(R.string.key_cloud_id),cloudRequest.cloud_id);
        hash.put(getString(R.string.key_device_id), SuperSafeApplication.getInstance().getDeviceId());


        subscriptions.add(SuperSafeApplication.serverAPI.onAddUserCloud(hash)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.onStartLoading(EnumStatus.CREATE))
                .subscribe(onResponse -> {
                    Log.d(TAG, "Body ???: " + new Gson().toJson(onResponse));
                    if (!onResponse.error){
                        mUser.verified = true;
                        mUser.cloud_id = onResponse.cloud_id;
                        PrefsController.putString(getString(R.string.key_user),new Gson().toJson(mUser));
                        view.onSuccessful(onResponse.message,EnumStatus.CREATE);
                    }
                    else{
                        view.onError(onResponse.message,EnumStatus.CREATE);
                    }
                    //view.onShowUserCloud(onResponse.error,onResponse.message);

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
                        Log.d(TAG, "Can not call add user cloud" + throwable.getMessage());
                    }
                    view.onStopLoading(EnumStatus.CREATE);
                }));

    }

    public void onUserCloudChecking(){
        Log.d(TAG,"onUserCloudChecking");
        BaseView view = view();
        if (view == null) {
            return;
        }
        if (NetworkUtil.pingIpAddress(view.getContext())) {
            return;
        }
        if (subscriptions == null) {
            return;
        }

        Map<String,String> hash = new HashMap<>();
        hash.put(getString(R.string.key_user_id),mUser.email);
        hash.put(getString(R.string.key_device_id), SuperSafeApplication.getInstance().getDeviceId());

        Log.d(TAG,"request :"+ new Gson().toJson(hash));

        subscriptions.add(SuperSafeApplication.serverAPI.onCheckUserCloud(hash)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.onStartLoading(EnumStatus.CLOUD_ID_EXISTING))
                .subscribe(onResponse -> {
                    Log.d(TAG, "Body : " + new Gson().toJson(onResponse));
                    if (onResponse.error){
                        view.onError(onResponse.message,EnumStatus.CLOUD_ID_EXISTING);
                    }
                    else{
                        view.onSuccessful(onResponse.cloud_id,EnumStatus.CLOUD_ID_EXISTING);
                    }
                    view.onStopLoading(EnumStatus.CLOUD_ID_EXISTING);
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
                        Log.d(TAG, "Can not call check user cloud " + throwable.getMessage());
                    }
                    view.onStopLoading(EnumStatus.CLOUD_ID_EXISTING);
                }));
    }


    private String getString(int res){
        BaseView view = view();
        String value = view.getContext().getString(res);
        return value;
    }

}
