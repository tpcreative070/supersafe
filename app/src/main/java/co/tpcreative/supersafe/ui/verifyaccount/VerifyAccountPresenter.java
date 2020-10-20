package co.tpcreative.supersafe.ui.verifyaccount;
import com.google.gson.Gson;
import com.snatik.storage.security.SecurityUtil;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.api.RootAPI;
import co.tpcreative.supersafe.common.controller.ServiceManager;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.presenter.Presenter;
import co.tpcreative.supersafe.common.request.ChangeUserIdRequest;
import co.tpcreative.supersafe.common.request.OutlookMailRequest;
import co.tpcreative.supersafe.common.request.RequestCodeRequest;
import co.tpcreative.supersafe.common.request.SignInRequest;
import co.tpcreative.supersafe.common.request.UserRequest;
import co.tpcreative.supersafe.common.request.VerifyCodeRequest;
import co.tpcreative.supersafe.common.response.DataResponse;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.NetworkUtil;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EmailToken;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.User;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;

public class VerifyAccountPresenter extends Presenter<BaseView> {
    private static final String TAG = VerifyAccountPresenter.class.getSimpleName();
    protected User mUser;

    public VerifyAccountPresenter(){
        mUser = Utils.getUserInfo();
        if (mUser==null){
            mUser = new User();
        }
    }

    public void onVerifyCode(VerifyCodeRequest request){
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
        subscriptions.add(SuperSafeApplication.serverAPI.onVerifyCode(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.onStartLoading(EnumStatus.VERIFY_CODE))
                .subscribe(onResponse -> {
                    view.onStopLoading(EnumStatus.VERIFY_CODE);
                    if (onResponse.error){
                        view.onError(getString(R.string.the_code_not_signed_up),EnumStatus.VERIFY_CODE);
                    }
                    else{
                        final User mUser = Utils.getUserInfo();
                        if (mUser!=null){
                            mUser.verified = true;
                            final DataResponse mData = onResponse.data;
                            if (mData == null){
                                view.onError(onResponse.message,EnumStatus.VERIFY_CODE);
                                return;
                            }
                            if (mData.premium!=null){
                                mUser.premium = mData.premium;
                            }
                            SuperSafeApplication.getInstance().writeUserSecret(mUser);
                            Utils.setUserPreShare(mUser);
                        }
                        view.onSuccessful(onResponse.message,EnumStatus.VERIFY_CODE);
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
                        view.onError(onResponse.responseMessage,EnumStatus.CHANGE_EMAIL);
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
                                mUser = Utils.getUserInfo();
                                view.onSuccessful(onResponse.responseMessage,EnumStatus.CHANGE_EMAIL);
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
                    }
                    else{
                        final User mUser = Utils.getUserInfo();
                        final DataResponse mData = onResponse.data;
                        mUser.code = mData.requestCode.code ;
                        this.mUser = mUser;
                        Utils.setUserPreShare(mUser);
                        final EmailToken emailToken = EmailToken.getInstance().convertObject(mUser,EnumStatus.SIGN_IN);
                        onSendMail(emailToken);
                    }
                    Utils.Log(TAG, "Body : " + new Gson().toJson(onResponse));
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
                    view.onStopLoading(EnumStatus.RESEND_CODE);
                }));
    }

    public void onCheckUser(final String email,String other_email){
        Utils.Log(TAG,"info onCheckUser :"+email);
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
                .doOnSubscribe(__ -> view.onStartLoading(EnumStatus.CHECK))
                .subscribe(onResponse -> {
                    Utils.Log(TAG, "Body : " + new Gson().toJson(onResponse));
                    if (onResponse.error){
                        view.onError("User is not existing",EnumStatus.CHECK);
                    }
                    else{
                        view.onSuccessful("User is existing",EnumStatus.CHECK);
                        SignInRequest request = new SignInRequest();
                        request.user_id = email;
                        request.password = SecurityUtil.key_password_default_encrypted;
                        request.device_id = SuperSafeApplication.getInstance().getDeviceId();
                        onSignIn(request);
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
                            }
                            Utils.Log(TAG,"error" +bodys.string());
                            String msg = new Gson().toJson(bodys.string());
                            Utils.Log(TAG, msg);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Utils.Log(TAG, "Can not call " + throwable.getMessage());
                    }
                    view.onStopLoading(EnumStatus.CHECK);
                }));
    }

    public void onSignIn(SignInRequest request){
        Utils.Log(TAG,"onSignIn.....");
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
        Utils.Log(TAG,new Gson().toJson(mUser));
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
                        Utils.setUserPreShare(mData.user);
                        final EmailToken emailToken = EmailToken.getInstance().convertObject(mUser,EnumStatus.SIGN_IN);
                        onSendMail(emailToken);
                        ServiceManager.getInstance().onInitConfigurationFile();

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
                            view.onError(msg,EnumStatus.SIGN_IN);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Utils.Log(TAG, "Can not call " + throwable.getMessage());
                    }
                    view.onStopLoading(EnumStatus.SIGN_IN);
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
                        view.onStopLoading(EnumStatus.SEND_EMAIL);
                        view.onSuccessful("successful",EnumStatus.SEND_EMAIL);
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
        Utils.Log(TAG, "onRefreshEmailToken....." + new Gson().toJson(request));
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
        final User mUser = Utils.getUserInfo();
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
                        this.mUser = mUser;
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

        final User mUser = Utils.getUserInfo();
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

    private String getString(int res){
        BaseView view = view();
        String value = view.getContext().getString(res);
        return value;
    }
}
