package co.tpcreative.supersafe.ui.resetpin;
import android.util.Log;
import com.creativityapps.gmailbackgroundlibrary.BackgroundMail;
import com.google.gson.Gson;
import com.snatik.storage.security.SecurityUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.presenter.Presenter;
import co.tpcreative.supersafe.common.request.VerifyCodeRequest;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.NetworkUtil;
import co.tpcreative.supersafe.model.Email;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.User;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.HttpException;

public class ResetPinPresenter extends Presenter<BaseView> {

    private static final String TAG = ResetPinPresenter.class.getSimpleName();
    protected User mUser ;

    public ResetPinPresenter(){
        mUser = new User();
        final User user = User.getInstance().getUserInfo();
        if (user!=null){
            this.mUser = user;
        }
        else{
            this.mUser = SuperSafeApplication.getInstance().readUseSecret();
        }
    }

    public void onVerifyCode(VerifyCodeRequest request){
        Log.d(TAG,"info");
        BaseView view = view();
        if (view == null) {
            view.onError("View is null", EnumStatus.VERIFIED_ERROR);
            return;
        }
        if (NetworkUtil.pingIpAddress(view.getContext())) {
            view.onError("NO internet", EnumStatus.VERIFIED_ERROR);
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
                .doOnSubscribe(__ -> view.onStartLoading(EnumStatus.OTHER))
                .subscribe(onResponse -> {
                    if (onResponse.error){
                        view.onError(onResponse.message,EnumStatus.VERIFIED_ERROR);
                    }
                    else{
                        view.onSuccessful(onResponse.message,EnumStatus.VERIFIED_SUCCESSFUL);
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
                .withType(BackgroundMail.TYPE_HTML)
                .withSubject(title)
                .withBody(Email.getInstance().getValue(code,"Reset"))
                .withOnSuccessCallback(new BackgroundMail.OnSuccessCallback() {
                    @Override
                    public void onSuccess() {
                        //do some magic
                        Log.d(TAG,"Successful");
                        view.onSuccessful("Sent email successful",EnumStatus.SEND_EMAIL_SUCCESSFUL);
                    }
                })
                .withOnFailCallback(new BackgroundMail.OnFailCallback() {
                    @Override
                    public void onFail() {
                        //do some magic
                        view.onSuccessful("Sent email failed",EnumStatus.SEND_EMAIL_SUCCESSFUL);
                        Log.d(TAG,"Failed");
                    }
                })
                .send();
    }

    public void onRequestCode(VerifyCodeRequest request){
        Log.d(TAG,"info");
        BaseView view = view();
        if (view == null) {
            view.onError("View is null", EnumStatus.REQUEST_CODE_ERROR);
            return;
        }
        if (NetworkUtil.pingIpAddress(view.getContext())) {
            view.onError("NO internet", EnumStatus.REQUEST_CODE_ERROR);
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
                .doOnSubscribe(__ ->view.onStartLoading(EnumStatus.OTHER) )
                .subscribe(onResponse -> {
                    if (onResponse.error){
                        view.onError(onResponse.message,EnumStatus.REQUEST_CODE_ERROR);
                    }
                    else{
                        view.onSuccessful(onResponse.message,EnumStatus.REQUEST_CODE_SUCCESSFUL);
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
                }));
    }

    private String getString(int res){
        BaseView view = view();
        String value = view.getContext().getString(res);
        return value;
    }

}
