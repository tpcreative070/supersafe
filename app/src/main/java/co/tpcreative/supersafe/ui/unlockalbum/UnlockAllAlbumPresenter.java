package co.tpcreative.supersafe.ui.unlockalbum;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.api.RootAPI;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.controller.ServiceManager;
import co.tpcreative.supersafe.common.helper.SQLHelper;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.presenter.Presenter;
import co.tpcreative.supersafe.common.request.OutlookMailRequest;
import co.tpcreative.supersafe.common.request.RequestCodeRequest;
import co.tpcreative.supersafe.common.request.VerifyCodeRequest;
import co.tpcreative.supersafe.common.response.DataResponse;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.NetworkUtil;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EmailToken;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.common.entities.MainCategoryEntity;
import co.tpcreative.supersafe.model.MainCategoryModel;
import co.tpcreative.supersafe.model.User;
import co.tpcreative.supersafe.common.entities.InstanceGenerator;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;

public class UnlockAllAlbumPresenter extends Presenter<BaseView> {

    private static final String TAG = UnlockAllAlbumPresenter.class.getSimpleName();


    protected List<MainCategoryModel> mListCategories;

    public UnlockAllAlbumPresenter(){
        mListCategories = SQLHelper.getListCategories(false);
    }

    public void onVerifyCode(VerifyCodeRequest request){
        Utils.Log(TAG,"info");
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
        subscriptions.add(SuperSafeApplication.serverAPI.onVerifyCode(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.onStartLoading(EnumStatus.VERIFY))
                .subscribe(onResponse -> {
                    if (onResponse.error){
                        view.onError(getString(R.string.the_code_not_signed_up),EnumStatus.VERIFY_CODE);
                    }
                    else{
                        view.onSuccessful(onResponse.message,EnumStatus.VERIFY);
                        final User mUser = User.getInstance().getUserInfo();
                        if (mUser!=null){
                            mUser.verified = true;
                            Utils.setUserPreShare(mUser);
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
                }));
    }

    /*Email Verify*/

    public void onSendMail(EmailToken request){
        Utils.Log(TAG, "onSendMail.....");
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
                        Utils.Log(TAG, "error" + errorMessage);
                        //view.onError(errorMessage, EnumStatus.SEND_EMAIL);
                    } else if (code == 202) {
                        Utils.Log(TAG, "code " + code);
                        view.onSuccessful("Sent email successful",EnumStatus.SEND_EMAIL);
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


    public void onRequestCode(VerifyCodeRequest request){
        Utils.Log(TAG,"info");
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

        subscriptions.add(SuperSafeApplication.serverAPI.onResendCode(new RequestCodeRequest(request.user_id,Utils.getAccessToken(),SuperSafeApplication.getInstance().getDeviceId()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ ->view.onStartLoading(EnumStatus.REQUEST_CODE) )
                .subscribe(onResponse -> {
                    if (onResponse.error){
                        view.onError(onResponse.message,EnumStatus.REQUEST_CODE);
                    }
                    else{
                        final User mUser = User.getInstance().getUserInfo();
                        final DataResponse mData = onResponse.data;
                        mUser.code = mData.requestCode.code ;
                        Utils.setUserPreShare(mUser);
                        final EmailToken emailToken = EmailToken.getInstance().convertObject(mUser,EnumStatus.UNLOCK_ALBUMS);
                        onSendMail(emailToken);
                        view.onSuccessful(onResponse.message,EnumStatus.REQUEST_CODE);
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
                }));
    }

    private String getString(int res){
        BaseView view = view();
        String value = view.getContext().getString(res);
        return value;
    }

}
