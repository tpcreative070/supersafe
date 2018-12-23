package co.tpcreative.supersafe.ui.unlockalbum;

import android.util.Log;

import com.creativityapps.gmailbackgroundlibrary.BackgroundMail;
import com.google.gson.Gson;
import com.snatik.storage.security.SecurityUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.api.RootAPI;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.controller.ServiceManager;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.presenter.Presenter;
import co.tpcreative.supersafe.common.request.VerifyCodeRequest;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.NetworkUtil;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.Email;
import co.tpcreative.supersafe.model.EmailToken;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.MainCategories;
import co.tpcreative.supersafe.model.User;
import co.tpcreative.supersafe.model.room.InstanceGenerator;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;

public class UnlockAllAlbumPresenter extends Presenter<BaseView> {

    private static final String TAG = UnlockAllAlbumPresenter.class.getSimpleName();


    protected List<MainCategories> mListCategories;

    public UnlockAllAlbumPresenter(){
        mListCategories = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getListCategories(false);
    }

    public void onVerifyCode(VerifyCodeRequest request){
        Log.d(TAG,"info");
        BaseView view = view();
        if (view == null) {
            view.onError("View is null", EnumStatus.VERIFIED_ERROR);
            return;
        }
        if (NetworkUtil.pingIpAddress(view.getContext())) {
            view.onError(getString(R.string.no_internet_connection), EnumStatus.VERIFIED_ERROR);
            return;
        }
        if (subscriptions == null) {
            return;
        }

        Map<String,String> hash = new HashMap<>();
        hash.put(getString(R.string.key_user_id),request.email);
        hash.put(getString(R.string.key_id),request._id);
        hash.put(getString(R.string.key_device_id), SuperSafeApplication.getInstance().getDeviceId());
        hash.put(getString(R.string.key_code),request.code);
        hash.put(getString(R.string.key_appVersionRelease),SuperSafeApplication.getInstance().getAppVersionRelease());
        subscriptions.add(SuperSafeApplication.serverAPI.onVerifyCode(hash)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.onStartLoading(EnumStatus.VERIFY))
                .subscribe(onResponse -> {
                    if (onResponse.error){
                        view.onError(onResponse.message,EnumStatus.VERIFY);
                    }
                    else{
                        view.onSuccessful(onResponse.message,EnumStatus.VERIFY);
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
                        int code  = ((HttpException) throwable).response().code();
                        try {
                            if (code==403){
                                Utils.Log(TAG,"code "+code);
                                ServiceManager.getInstance().onUpdatedUserToken();
                            }
                            Log.d(TAG,"error" +bodys.string());
                            String msg = new Gson().toJson(bodys.string());
                            Log.d(TAG, msg);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.d(TAG, "Can not call" + throwable.getMessage());
                    }
                }));
    }

    /*Email Verify*/

    public void onSendMail(EmailToken request){
        Log.d(TAG, "onSendMail.....");
        BaseView view = view();
        if (view == null) {
            view.onError("Null", EnumStatus.SEND_EMAIL);
            return;
        }
        if (NetworkUtil.pingIpAddress(SuperSafeApplication.getInstance())) {
            view.onError(getString(R.string.no_internet_connection), EnumStatus.SEND_EMAIL);
            return;
        }
        if (subscriptions == null) {
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
                        Log.d(TAG, "error" + errorMessage);
                        //view.onError(errorMessage, EnumStatus.SEND_EMAIL);
                    } else if (code == 202) {
                        Utils.Log(TAG, "code " + code);
                        view.onSuccessful("Sent email successful",EnumStatus.SEND_EMAIL);
                        Log.d(TAG, "Body : Send email Successful");
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
        Log.d(TAG, "onRefreshEmailToken.....");
        BaseView view = view();
        if (view == null) {
            return;
        }
        if (NetworkUtil.pingIpAddress(SuperSafeApplication.getInstance())) {
            return;
        }
        if (subscriptions == null) {
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
                        PrefsController.putString(getString(R.string.key_user), new Gson().toJson(mUser));
                        onAddEmailToken();
                    }
                    view.onSuccessful("successful", EnumStatus.REFRESH);
                    Log.d(TAG, "Body refresh : " + new Gson().toJson(onResponse));
                }, throwable -> {
                    if (throwable instanceof HttpException) {
                        ResponseBody bodys = ((HttpException) throwable).response().errorBody();
                        int code = ((HttpException) throwable).response().code();
                        try {
                            if (code == 401) {
                                Utils.Log(TAG, "code " + code);
                            }
                            Log.d(TAG, "error" + bodys.string());
                            String msg = new Gson().toJson(bodys.string());
                            view.onError(msg, EnumStatus.SEND_EMAIL);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.d(TAG, "Can not call " + throwable.getMessage());
                    }
                }));
    }


    public void onAddEmailToken(){
        Log.d(TAG, "onSignIn.....");
        BaseView view = view();
        if (view == null) {
            return;
        }
        if (NetworkUtil.pingIpAddress(SuperSafeApplication.getInstance())) {
            return;
        }
        if (subscriptions == null) {
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
                    Log.d(TAG, "Body : " + new Gson().toJson(onResponse));
                    final EmailToken emailToken = EmailToken.getInstance().convertObject(mUser,EnumStatus.UNLOCK_ALBUMS);
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
                            Log.d(TAG, "error" + errorMessage);
                            view.onError(errorMessage, EnumStatus.ADD_EMAIL_TOKEN);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.d(TAG, "Can not call " + throwable.getMessage());
                    }
                }));
    }


    public void onRequestCode(VerifyCodeRequest request){
        Log.d(TAG,"info");
        BaseView view = view();
        if (view == null) {
            view.onError("View is null", EnumStatus.REQUEST_CODE);
            return;
        }
        if (NetworkUtil.pingIpAddress(view.getContext())) {
            view.onError(getString(R.string.no_internet_connection), EnumStatus.REQUEST_CODE);
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
                .doOnSubscribe(__ ->view.onStartLoading(EnumStatus.REQUEST_CODE) )
                .subscribe(onResponse -> {
                    if (onResponse.error){
                        view.onError(onResponse.message,EnumStatus.REQUEST_CODE);
                    }
                    else{
                        final User mUser = User.getInstance().getUserInfo();
                        mUser.code = onResponse.code;
                        PrefsController.putString(getString(R.string.key_user),new Gson().toJson(mUser));
                        final EmailToken emailToken = EmailToken.getInstance().convertObject(mUser,EnumStatus.UNLOCK_ALBUMS);
                        onSendMail(emailToken);
                        view.onSuccessful(onResponse.message,EnumStatus.REQUEST_CODE);
                    }
                    Log.d(TAG, "Body : " + new Gson().toJson(onResponse));
                }, throwable -> {
                    if (throwable instanceof HttpException) {
                        ResponseBody bodys = ((HttpException) throwable).response().errorBody();
                        int code  = ((HttpException) throwable).response().code();
                        try {
                            if (code==403){
                                Utils.Log(TAG,"code "+code);
                                ServiceManager.getInstance().onUpdatedUserToken();
                            }
                            Log.d(TAG,"error" +bodys.string());
                            String msg = new Gson().toJson(bodys.string());
                            Log.d(TAG, msg);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.d(TAG, "Can not call" + throwable.getMessage());
                    }
                }));
    }

    private String getString(int res){
        BaseView view = view();
        String value = view.getContext().getString(res);
        return value;
    }

}
