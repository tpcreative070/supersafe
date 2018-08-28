package co.tpcreative.suppersafe.ui.checksystem;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.creativityapps.gmailbackgroundlibrary.BackgroundMail;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import co.tpcreative.suppersafe.R;
import co.tpcreative.suppersafe.common.controller.PrefsController;
import co.tpcreative.suppersafe.common.presenter.Presenter;
import co.tpcreative.suppersafe.common.request.SignUpRequest;
import co.tpcreative.suppersafe.common.request.UserCloudRequest;
import co.tpcreative.suppersafe.common.request.VerifyCodeRequest;
import co.tpcreative.suppersafe.common.services.SupperSafeApplication;
import co.tpcreative.suppersafe.common.util.NetworkUtil;
import co.tpcreative.suppersafe.model.GoogleOauth;
import co.tpcreative.suppersafe.model.User;
import co.tpcreative.suppersafe.ui.enablecloud.EnableCloudView;
import co.tpcreative.suppersafe.ui.signin.SignInView;
import co.tpcreative.suppersafe.ui.signup.SignUpView;
import co.tpcreative.suppersafe.ui.verifyaccount.VerifyAccountView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.HttpException;

public class CheckSystemPresenter extends Presenter<CheckSystemView>{

    private final String TAG = CheckSystemPresenter.class.getSimpleName();
    protected User mUser;
    protected GoogleOauth googleOauth;
    protected boolean isUserExisting;


    public CheckSystemPresenter(){
        mUser = new User();
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
        CheckSystemView view = view();
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
        hash.put(getString(R.string.key_device_id), SupperSafeApplication.getInstance().getDeviceId());
        subscriptions.add(SupperSafeApplication.serverAPI.onCheckUserId(hash)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.startLoading())
                .subscribe(onResponse -> {
                    Log.d(TAG, "Body : " + new Gson().toJson(onResponse));
                    if (onResponse.error){
                        SignUpRequest request = new SignUpRequest();
                        request.email = email;
                        request.name = "Google";
                        onSignUp(request);
                    }
                    else{
                        view.showUserExisting(email,!onResponse.error);
                        onSendGmail(email,onResponse.code);
                        isUserExisting = !onResponse.error;
                    }

                    view.stopLoading();
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
                    view.stopLoading();
                }));
    }

    public void onSignUp(SignUpRequest request){
        Log.d(TAG,"info");
        CheckSystemView view = view();
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
        hash.put(getString(R.string.key_password),getString(R.string.key_password_default));
        hash.put(getString(R.string.key_name),request.name);
        hash.put(getString(R.string.key_device_id),SupperSafeApplication.getInstance().getDeviceId());
        hash.put(getString(R.string.key_device_type),getString(R.string.device_type));
        hash.put(getString(R.string.key_manufacturer),SupperSafeApplication.getInstance().getManufacturer());
        hash.put(getString(R.string.key_name_model),SupperSafeApplication.getInstance().getModel());
        hash.put(getString(R.string.key_version),""+SupperSafeApplication.getInstance().getVersion());
        hash.put(getString(R.string.key_versionRelease),SupperSafeApplication.getInstance().getVersionRelease());
        subscriptions.add(SupperSafeApplication.serverAPI.onSignUP(hash)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.startLoading())
                .subscribe(onResponse -> {
                    view.stopLoading();
                    if (onResponse.error){
                        view.onSignUpFailed(onResponse.message);
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
                        Log.d(TAG, "Can not call" + throwable.getMessage());
                    }
                    view.stopLoading();
                }));
    }


    public void onVerifyCode(VerifyCodeRequest request){
        Log.d(TAG,"info");
        CheckSystemView view = view();
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
        hash.put(getString(R.string.key_device_id), SupperSafeApplication.getInstance().getDeviceId());
        hash.put(getString(R.string.key_code),request.code);
        subscriptions.add(SupperSafeApplication.serverAPI.onVerifyCode(hash)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.startLoading())
                .subscribe(onResponse -> {
                    view.stopLoading();
                    if (onResponse.error){
                        view.showFailedVerificationCode();
                    }
                    else{
                        view.showSuccessfulVerificationCode();
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
                        Log.d(TAG, "Can not call" + throwable.getMessage());
                    }
                    view.stopLoading();
                }));
    }

    public void onResendCode(VerifyCodeRequest request){
        Log.d(TAG,"info");
        CheckSystemView view = view();
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
        hash.put(getString(R.string.key_device_id), SupperSafeApplication.getInstance().getDeviceId());
        subscriptions.add(SupperSafeApplication.serverAPI.onResendCode(hash)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.startLoading())
                .subscribe(onResponse -> {
                    if (onResponse.error){
                        view.showError(onResponse.message);
                        view.stopLoading();
                    }
                    else{
                        onSendGmail(mUser.email,onResponse.code);
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
                    view.stopLoading();
                }));
    }


    public void onSendGmail(String email,String code){
        CheckSystemView view = view();
        String body = String.format(getString(R.string.send_code),code);
        String title = String.format(getString(R.string.send_code_title),code);
        BackgroundMail.newBuilder(view.getActivity())
                .withUsername(view.getContext().getString(R.string.user_name))
                .withPassword(view.getContext().getString(R.string.password))
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
                            view.stopLoading();
                            view.sendEmailSuccessful();
                        }
                    }
                })
                .withOnFailCallback(new BackgroundMail.OnFailCallback() {
                    @Override
                    public void onFail() {
                        //do some magic
                        Log.d(TAG,"Failed");
                        if (view!=null){
                            view.stopLoading();
                        }
                    }
                })
                .send();
    }

    public void onAddUserCloud(UserCloudRequest cloudRequest){
        Log.d(TAG,"info");
        CheckSystemView view = view();
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
        hash.put(getString(R.string.key_device_id), SupperSafeApplication.getInstance().getDeviceId());
        subscriptions.add(SupperSafeApplication.serverAPI.onAddUserCloud(hash)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.startLoading())
                .subscribe(onResponse -> {
                    Log.d(TAG, "Body : " + new Gson().toJson(onResponse));
                    if (!onResponse.error){
                        mUser.verified = true;
                        mUser.cloud_id = onResponse.cloud_id;
                        PrefsController.putString(getString(R.string.key_user),new Gson().toJson(mUser));
                    }
                    view.onShowUserCloud(onResponse.error,onResponse.message);
                    view.stopLoading();
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
                    view.stopLoading();
                }));

    }

    public void onUserCloudChecking(){
        Log.d(TAG,"info");
        CheckSystemView view = view();
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
        hash.put(getString(R.string.key_device_id), SupperSafeApplication.getInstance().getDeviceId());
        subscriptions.add(SupperSafeApplication.serverAPI.onCheckUserCloud(hash)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.startLoading())
                .subscribe(onResponse -> {
                    Log.d(TAG, "Body : " + new Gson().toJson(onResponse));
                    if (onResponse.error){
                        view.showError(onResponse.message);
                    }
                    else{
                        view.showSuccessful(onResponse.cloud_id);
                    }
                    view.stopLoading();
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
                    view.stopLoading();
                }));
    }


    private String getString(int res){
        CheckSystemView view = view();
        String value = view.getContext().getString(res);
        return value;
    }

}
