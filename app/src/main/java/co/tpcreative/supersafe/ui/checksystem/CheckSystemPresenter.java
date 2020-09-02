package co.tpcreative.supersafe.ui.checksystem;
import android.app.Activity;
import android.os.Bundle;
import com.google.gson.Gson;
import com.snatik.storage.security.SecurityUtil;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.api.RootAPI;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.controller.ServiceManager;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.presenter.Presenter;
import co.tpcreative.supersafe.common.request.ChangeUserIdRequest;
import co.tpcreative.supersafe.common.request.OutlookMailRequest;
import co.tpcreative.supersafe.common.request.RequestCodeRequest;
import co.tpcreative.supersafe.common.request.SignInRequest;
import co.tpcreative.supersafe.common.request.UserCloudRequest;
import co.tpcreative.supersafe.common.request.UserRequest;
import co.tpcreative.supersafe.common.request.VerifyCodeRequest;
import co.tpcreative.supersafe.common.response.DataResponse;
import co.tpcreative.supersafe.common.response.UserCloudResponse;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.NetworkUtil;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EmailToken;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.GoogleOauth;
import co.tpcreative.supersafe.model.User;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;

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
            Utils.Log(TAG,"≈ "+ new Gson().toJson(googleOauth));
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void onCheckUser(final String email,String other_email){
        Utils.Log(TAG,"onCheckUser");
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
        subscriptions.add(SuperSafeApplication.serverAPI.onCheckUserId(new UserRequest())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.onStartLoading(EnumStatus.USER_ID_EXISTING))
                .subscribe(onResponse -> {
                    Utils.Log(TAG, "Body : " + new Gson().toJson(onResponse));
                    if (onResponse.error){
                        SignInRequest request = new SignInRequest();
                        request.user_id = email;
                        request.password = SecurityUtil.key_password_default_encrypted;
                        request.device_id = SuperSafeApplication.getInstance().getDeviceId();
                        onSignIn(request);
                    }
                    else{
                        //view.onSuccessful(email,!onResponse.error);
                        view.onSuccessful(email,EnumStatus.USER_ID_EXISTING);
                        SignInRequest request = new SignInRequest();
                        request.user_id = email;
                        request.password = SecurityUtil.key_password_default_encrypted;
                        request.device_id = SuperSafeApplication.getInstance().getDeviceId();
                        onSignIn(request);
                        isUserExisting = !onResponse.error;
                    }
                }, throwable -> {
                    if (throwable instanceof HttpException) {
                        ResponseBody bodys = ((HttpException) throwable).response().errorBody();
                        int code  = ((HttpException) throwable).response().code();
                        try {
                            if (code==401){
                                Utils.Log(TAG,"code "+code);
                                ServiceManager.getInstance().onUpdatedUserToken();
                            }
                            else if (code ==403){
                                SignInRequest request = new SignInRequest();
                                request.user_id = email;
                                request.password = SecurityUtil.key_password_default_encrypted;
                                request.device_id = SuperSafeApplication.getInstance().getDeviceId();
                                onSignIn(request);
                                Utils.Log(TAG,"Login");
                            }
                            Utils.Log(TAG,"error" +bodys.string());
                            String msg = new Gson().toJson(bodys.string());
                            Utils.Log(TAG, msg);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Utils.Log(TAG, "Can not call check user" + throwable.getMessage());
                    }
                    view.onStopLoading(EnumStatus.USER_ID_EXISTING);
                }));
    }

    public void onSignIn(SignInRequest request){
        Utils.Log(TAG,"onSignIn");
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
        subscriptions.add(SuperSafeApplication.serverAPI.onSignIn(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.onStartLoading(EnumStatus.SIGN_IN))
                .subscribe(onResponse -> {
                    if (onResponse.error){
                        view.onError(onResponse.message,EnumStatus.SIGN_IN);
                    }
                    else{
                        final DataResponse mData = onResponse.data;
                        mUser = mData.user;
                        Utils.setUserPreShare(mUser);
                        String email = mUser.email;
                        if (mUser.change){
                            email = mUser.other_email;
                        }
                        final EmailToken emailToken = EmailToken.getInstance().convertObject(mUser,EnumStatus.SIGN_IN);
                        onSendMail(emailToken);
                        ServiceManager.getInstance().onInitConfigurationFile();
                    }
                    Utils.Log(TAG, "Body : " + new Gson().toJson(onResponse));
                }, throwable -> {
                    if (throwable instanceof HttpException) {
                        ResponseBody bodys = ((HttpException) throwable).response().errorBody();
                        try {
                            Utils.Log(TAG,"error" +bodys.string());
                            String msg = new Gson().toJson(bodys.string());
                            view.onError(msg,EnumStatus.SIGN_IN);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Utils.Log(TAG, "Can not call sign in" + throwable.getMessage());
                    }
                    view.onStopLoading(EnumStatus.SIGN_IN);
                }));
    }


    public void onVerifyCode(VerifyCodeRequest request){
        Utils.Log(TAG,"onVerifyCode");
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
        subscriptions.add(SuperSafeApplication.serverAPI.onVerifyCode(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.onStartLoading(EnumStatus.VERIFY_CODE))
                .subscribe(onResponse -> {
                    if (onResponse.error){
                        view.onError(getString(R.string.the_code_not_signed_up),EnumStatus.VERIFY_CODE);
                    }
                    else{
                        mUser = User.getInstance().getUserInfo();
                        if (mUser!=null){
                            mUser.verified = true;
                            final DataResponse mData = onResponse.data;
                            if (mData == null){
                                view.onError(onResponse.message,EnumStatus.VERIFY_CODE);
                                return;
                            }
                            if (mData.premium!=null){
                                mUser.premium = mData.premium;
                                Utils.Log(TAG,"Saved.............");
                            }
                            SuperSafeApplication.getInstance().writeUserSecret(mUser);
                            Utils.setUserPreShare(mUser);
                            mUser = User.getInstance().getUserInfo();
                        }
                        view.onSuccessful(onResponse.message,EnumStatus.VERIFY_CODE);
                    }
                    Utils.Log(TAG, "Body verify code : " + new Gson().toJson(onResponse));
                }, throwable -> {
                    if (throwable instanceof HttpException) {
                        ResponseBody bodys = ((HttpException) throwable).response().errorBody();
                        int code  = ((HttpException) throwable).response().code();
                        try {
                            if (code==401){
                                Utils.Log(TAG,"code "+code);
                                ServiceManager.getInstance().onUpdatedUserToken();
                            }
                            Utils.Log(TAG,"error" +bodys.string());
                            String msg = new Gson().toJson(bodys.string());
                            Utils.Log(TAG, msg);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Utils.Log(TAG, "Can not call verify code" + throwable.getMessage());
                    }
                    view.onStopLoading(EnumStatus.VERIFY_CODE);
                }));
    }


    public void onChangeEmail(VerifyCodeRequest request){
        Utils.Log(TAG,"info");
        BaseView view = view();
        if (view == null) {
            return;
        }
        if (NetworkUtil.pingIpAddress(view.getContext())) {
            view.onError("No connection",EnumStatus.CHANGE_EMAIL);
            return;
        }
        if (subscriptions == null) {
            return;
        }
        subscriptions.add(SuperSafeApplication.serverAPI.onUpdateUser(new ChangeUserIdRequest(request))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.onStartLoading(EnumStatus.CHANGE_EMAIL))
                .subscribe(onResponse -> {
                    view.onStopLoading(EnumStatus.CHANGE_EMAIL);
                    if (onResponse.error){
                        view.onError(onResponse.message,EnumStatus.CHANGE_EMAIL);
                    }
                    else{
                        if (onResponse!=null){
                            final DataResponse mData = onResponse.data;
                            if (mData.author!=null){
                                if (mUser!=null){
                                    mUser.author = mData.author;
                                    mUser.email = request.new_user_id;
                                    mUser.other_email = request.other_email;
                                    mUser.change = true;
                                    Utils.setUserPreShare(mUser);
                                }
                                else{
                                    Utils.Log(TAG,"User is null");
                                }
                                mUser = User.getInstance().getUserInfo();
                                view.onSuccessful(onResponse.message,EnumStatus.CHANGE_EMAIL);
                            }
                        }
                    }
                    Utils.Log(TAG, "Body : " + new Gson().toJson(onResponse));
                }, throwable -> {
                    if (throwable instanceof HttpException) {
                        ResponseBody bodys = ((HttpException) throwable).response().errorBody();
                        int code  = ((HttpException) throwable).response().code();
                        try {
                            if (code==401){
                                Utils.Log(TAG,"code "+code);
                                ServiceManager.getInstance().onUpdatedUserToken();
                            }
                            Utils.Log(TAG,"error" +bodys.string());
                            String msg = new Gson().toJson(bodys.string());
                            Utils.Log(TAG, msg);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Utils.Log(TAG, "Can not call" + throwable.getMessage());
                    }
                    view.onStopLoading(EnumStatus.CHANGE_EMAIL);
                }));
    }


    public void onResendCode(VerifyCodeRequest request){
        Utils.Log(TAG,"info");
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
        subscriptions.add(SuperSafeApplication.serverAPI.onResendCode(new RequestCodeRequest(request.user_id,Utils.getAccessToken(),SuperSafeApplication.getInstance().getDeviceId()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.onStartLoading(EnumStatus.RESEND_CODE))
                .subscribe(onResponse -> {
                    if (onResponse.error){
                        view.onError(onResponse.message,EnumStatus.RESEND_CODE);
                        view.onStopLoading(EnumStatus.RESEND_CODE);
                    }
                    else{

                        User mUser = User.getInstance().getUserInfo();
                        final DataResponse mData = onResponse.data;
                        mUser.code = mData.requestCode.code ;
                        Utils.setUserPreShare(mUser);
                        final EmailToken emailToken = EmailToken.getInstance().convertObject(mUser,EnumStatus.SIGN_IN);
                        onSendMail(emailToken);
                    }
                    Utils.Log(TAG, "Body : " + new Gson().toJson(onResponse));
                }, throwable -> {
                    if (throwable instanceof HttpException) {
                        ResponseBody bodys = ((HttpException) throwable).response().errorBody();
                        int code  = ((HttpException) throwable).response().code();
                        try {
                            if (code==401){
                                Utils.Log(TAG,"code "+code);
                                ServiceManager.getInstance().onUpdatedUserToken();
                            }
                            Utils.Log(TAG,"error" +bodys.string());
                            String msg = new Gson().toJson(bodys.string());
                            Utils.Log(TAG, msg);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Utils.Log(TAG, "Can not call" + throwable.getMessage());
                    }
                    view.onStopLoading(EnumStatus.RESEND_CODE);
                }));
    }

    /*Email Verify*/

    public void onSendMail(EmailToken request){
        Utils.Log(TAG, "onSendMail.....");
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
                    } else if (code == 202) {
                        Utils.Log(TAG, "code " + code);
                        view.onSuccessful("Successful",EnumStatus.SEND_EMAIL);
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
                        Utils.setUserPreShare(mUser);
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


    public void onAddEmailToken(){
        Utils.Log(TAG, "onSignIn.....");
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
        subscriptions.add(SuperSafeApplication.serverAPI.onAddEmailToken(new OutlookMailRequest(mUser.email_token.refresh_token,mUser.email_token.access_token))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onResponse -> {
                    Utils.Log(TAG, "Body : " + new Gson().toJson(onResponse));
                    final EmailToken emailToken = EmailToken.getInstance().convertObject(mUser,EnumStatus.SIGN_IN);
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

    public void onAddUserCloud(UserCloudRequest cloudRequest){
        Utils.Log(TAG,"info");
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
        subscriptions.add(SuperSafeApplication.serverAPI.onAddUserCloud(cloudRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.onStartLoading(EnumStatus.CREATE))
                .subscribe(onResponse -> {
                    Utils.Log(TAG, "Body ???: " + new Gson().toJson(onResponse));
                    if (onResponse.error){
                        view.onError(onResponse.message,EnumStatus.CREATE);
                    }
                    else{
                        final DataResponse mData = onResponse.data;
                        final UserCloudResponse mCloud = mData.userCloud;
                        mUser = User.getInstance().getUserInfo();
                        mUser.verified = true;
                        mUser.cloud_id = mCloud.cloud_id;
                        Utils.setUserPreShare(mUser);
                        view.onSuccessful(onResponse.message,EnumStatus.CREATE);
                    }
                    Utils.Log(TAG,"User info "+ new Gson().toJson(mUser));
                    //view.onShowUserCloud(onResponse.error,onResponse.message);

                }, throwable -> {
                    if (throwable instanceof HttpException) {
                        ResponseBody bodys = ((HttpException) throwable).response().errorBody();
                        int code  = ((HttpException) throwable).response().code();
                        try {
                            if (code==401){
                                Utils.Log(TAG,"code "+code);
                                ServiceManager.getInstance().onUpdatedUserToken();
                            }
                            Utils.Log(TAG,"error" +bodys.string());
                            String msg = new Gson().toJson(bodys.string());
                            Utils.Log(TAG, msg);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Utils.Log(TAG, "Can not call add user cloud" + throwable.getMessage());
                    }
                    view.onStopLoading(EnumStatus.CREATE);
                }));

    }

    public void onUserCloudChecking(){
        Utils.Log(TAG,"onUserCloudChecking");
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
        subscriptions.add(SuperSafeApplication.serverAPI.onCheckUserCloud(new UserCloudRequest(mUser.email,SuperSafeApplication.getInstance().getDeviceId()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.onStartLoading(EnumStatus.CLOUD_ID_EXISTING))
                .subscribe(onResponse -> {
                    Utils.Log(TAG, "Body : " + new Gson().toJson(onResponse));
                    if (onResponse.error){
                        view.onError(onResponse.message,EnumStatus.CLOUD_ID_EXISTING);
                    }
                    else{
                        final DataResponse mData = onResponse.data;
                        final UserCloudResponse mCloudData = mData.userCloud;
                        view.onSuccessful(mCloudData.cloud_id,EnumStatus.CLOUD_ID_EXISTING);
                    }
                    view.onStopLoading(EnumStatus.CLOUD_ID_EXISTING);
                }, throwable -> {
                    if (throwable instanceof HttpException) {
                        ResponseBody bodys = ((HttpException) throwable).response().errorBody();
                        int code  = ((HttpException) throwable).response().code();
                        try {
                            if (code==401){
                                Utils.Log(TAG,"code "+code);
                                ServiceManager.getInstance().onUpdatedUserToken();
                            }
                            Utils.Log(TAG,"error" +bodys.string());
                            String msg = new Gson().toJson(bodys.string());
                            Utils.Log(TAG, msg);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Utils.Log(TAG, "Can not call check user cloud " + throwable.getMessage());
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
