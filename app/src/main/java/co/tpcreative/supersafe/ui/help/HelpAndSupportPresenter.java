package co.tpcreative.supersafe.ui.help;
import android.app.Activity;
import android.os.Bundle;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.api.RootAPI;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.controller.ServiceManager;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.presenter.Presenter;
import co.tpcreative.supersafe.common.request.OutlookMailRequest;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.NetworkUtil;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.Categories;
import co.tpcreative.supersafe.model.EmailToken;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.HelpAndSupport;
import co.tpcreative.supersafe.model.User;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;

public class HelpAndSupportPresenter extends Presenter<BaseView>{

    protected List<HelpAndSupport> mList;
    protected HelpAndSupport content;
    private static final String TAG = HelpAndSupportPresenter.class.getSimpleName();
    public HelpAndSupportPresenter(){
        mList = new ArrayList<>();
        content = new HelpAndSupport();
    }

    public void onGetList(){
        mList.clear();
        Categories categories = new Categories(0,getString(R.string.faq));
        mList.add(new HelpAndSupport(categories,getString(R.string.i_have_a_new_phone),getString(R.string.i_have_a_new_phone_content),null));
        mList.add(new HelpAndSupport(categories,getString(R.string.what_about_google_drive),getString(R.string.what_about_google_drive_content),null));
        mList.add(new HelpAndSupport(categories,getString(R.string.how_do_export_my_files),getString(R.string.how_do_export_my_files_content),null));
        mList.add(new HelpAndSupport(categories,getString(R.string.how_do_i_recover_items_from_trash),getString(R.string.how_do_i_recover_items_from_trash_content),null));
        mList.add(new HelpAndSupport(categories,getString(R.string.i_forgot_the_password_how_to_unlock_my_albums),getString(R.string.i_forgot_the_password_how_to_unlock_my_albums_content),null));
        mList.add(new HelpAndSupport(categories,getString(R.string.what_is_the_fake_pin_and_how_do_i_use_it),getString(R.string.what_is_the_fake_pin_and_how_do_i_use_it_content),null));
        categories = new Categories(1,getString(R.string.contact_support));
        mList.add(new HelpAndSupport(categories,getString(R.string.contact_support),getString(R.string.contact_support_content),null));
    }

    public void onGetDataIntent(Activity activity){
        BaseView view = view();
        try{
            Bundle bundle = activity.getIntent().getExtras();
            content = (HelpAndSupport) bundle.get(HelpAndSupport.class.getSimpleName());
            view.onSuccessful("Successful", EnumStatus.RELOAD);
            Utils.Log(TAG,new Gson().toJson(content));
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private String getString(int res){
        BaseView view = view();
        String value = view.getContext().getString(res);
        return value;
    }



    public void onSendMail(EmailToken request,String content){
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
                        onRefreshEmailToken(request,content);
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

    public void onRefreshEmailToken(EmailToken request,String content) {
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
                        Utils.setUserPreShare(mUser);
                        onAddEmailToken(content);
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


    public void onAddEmailToken(String content){
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
                    final EmailToken emailToken = EmailToken.getInstance().convertTextObject(mUser,content);
                    onSendMail(emailToken,content);
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
}
