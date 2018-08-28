package co.tpcreative.suppersafe.ui.verifyaccount;
import android.util.Log;

import com.creativityapps.gmailbackgroundlibrary.BackgroundMail;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import co.tpcreative.suppersafe.R;
import co.tpcreative.suppersafe.common.controller.PrefsController;
import co.tpcreative.suppersafe.common.presenter.Presenter;
import co.tpcreative.suppersafe.common.request.SignInRequest;
import co.tpcreative.suppersafe.common.request.VerifyCodeRequest;
import co.tpcreative.suppersafe.common.services.SupperSafeApplication;
import co.tpcreative.suppersafe.common.util.NetworkUtil;
import co.tpcreative.suppersafe.model.User;
import co.tpcreative.suppersafe.ui.signin.SignInView;
import co.tpcreative.suppersafe.ui.verify.VerifyView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.HttpException;

public class VerifyAccountPresenter extends Presenter<VerifyAccountView> {


    private static final String TAG = VerifyAccountPresenter.class.getSimpleName();

    protected User mUser;

    public VerifyAccountPresenter(){
        mUser = User.getInstance().getUserInfo();
        if (mUser==null){
            mUser = new User();
        }
    }

    public void onChangeEmail(String email){
        VerifyAccountView view = view();
        if (mUser!=null){
            mUser.email = email;
            PrefsController.putString(getString(R.string.key_user),new Gson().toJson(mUser));
            view.onChangeEmailSuccessful();
        }
    }

    public void onVerifyCode(VerifyCodeRequest request){
        Log.d(TAG,"info");
        VerifyAccountView view = view();
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
                .doOnSubscribe(__ -> view.onLoading())
                .subscribe(onResponse -> {
                    view.onFinishing();
                    if (onResponse.error){
                        view.showError(onResponse.message);
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
                    view.onFinishing();
                }));
    }

    public void onResendCode(VerifyCodeRequest request){
        Log.d(TAG,"info");
        VerifyAccountView view = view();
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
        VerifyAccountView view = view();
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
                            view.showSuccessful("successful");
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


    private String getString(int res){
        VerifyAccountView view = view();
        String value = view.getContext().getString(res);
        return value;
    }



}
