package co.tpcreative.supersafe.ui.signin;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;
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
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.NetworkUtil;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.Email;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.User;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.HttpException;

public class SignInPresenter extends Presenter<BaseView<User>>{

    private static final String TAG = SignInPresenter.class.getSimpleName();

    public void onSignIn(SignInRequest request){
        Log.d(TAG,"onSignIn");
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
        hash.put(getString(R.string.key_version).toLowerCase(),""+ SuperSafeApplication.getInstance().getVersion());
        hash.put(getString(R.string.key_versionRelease), SuperSafeApplication.getInstance().getVersionRelease());
        hash.put(getString(R.string.key_appVersionRelease), SuperSafeApplication.getInstance().getAppVersionRelease());
        subscriptions.add(SuperSafeApplication.serverAPI.onSignIn(hash)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.onStartLoading(EnumStatus.SIGN_IN))
                .subscribe(onResponse -> {
                    view.onStopLoading(EnumStatus.SIGN_IN);
                    if (onResponse.error){
                        view.onError(onResponse.message,EnumStatus.SIGN_IN);
                    }
                    else{
                        view.onSuccessful(onResponse.message,EnumStatus.SIGN_IN,onResponse.user);
                        PrefsController.putString(getString(R.string.key_user),new Gson().toJson(onResponse.user));
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
                    view.onStopLoading(EnumStatus.OTHER);
                }));
    }

    private String getString(int res){
        BaseView view = view();
        String value = view.getContext().getString(res);
        return value;
    }

    public void onSendGmail(String email,String code){
        BaseView view = view();
        String title = String.format(getString(R.string.send_code_title),code);
        BackgroundMail.newBuilder(view.getActivity())
                .withUsername(SecurityUtil.user_name)
                .withPassword(SecurityUtil.password)
                .withMailto(email)
                .withType(BackgroundMail.TYPE_HTML)
                .withSubject(title)
                .withBody(Email.getInstance().getValue(code,"SignIn"))
                .withOnSuccessCallback(new BackgroundMail.OnSuccessCallback() {
                    @Override
                    public void onSuccess() {
                        //do some magic
                        Log.d(TAG,"Successful");
                    }
                })
                .withOnFailCallback(new BackgroundMail.OnFailCallback() {
                    @Override
                    public void onFail() {
                        //do some magic
                        Log.d(TAG,"Failed");
                    }
                })
                .send();
    }

    private Spanned getSpannedText(String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT);
        } else {
            return Html.fromHtml(text);
        }
    }




}
