package co.tpcreative.suppersafe.ui.signin;
import android.util.Log;
import com.creativityapps.gmailbackgroundlibrary.BackgroundMail;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import co.tpcreative.suppersafe.R;
import co.tpcreative.suppersafe.common.controller.ManagerNetwork;
import co.tpcreative.suppersafe.common.controller.PrefsController;
import co.tpcreative.suppersafe.common.presenter.Presenter;
import co.tpcreative.suppersafe.common.request.SignInRequest;
import co.tpcreative.suppersafe.common.request.SignUpRequest;
import co.tpcreative.suppersafe.common.services.SupperSafeApplication;
import co.tpcreative.suppersafe.common.util.NetworkUtil;
import co.tpcreative.suppersafe.model.User;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.HttpException;


public class SignInPresenter extends Presenter<SignInView>{

    private static final String TAG = SignInPresenter.class.getSimpleName();

    public void onSignIn(SignInRequest request){
        Log.d(TAG,"info");
        SignInView view = view();
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
        subscriptions.add(SupperSafeApplication.serverAPI.onSignIn(hash)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.startLoading())
                .subscribe(onResponse -> {
                    view.stopLoading();
                    if (onResponse.error){
                        view.showError(onResponse.message);
                    }
                    else{
                        view.showSuccessful(onResponse.message,onResponse.user);
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
                        Log.d(TAG, "Can not call" + throwable.getMessage());
                    }
                    view.stopLoading();
                }));
    }

    private String getString(int res){
        SignInView view = view();
        String value = view.getContext().getString(res);
        return value;
    }

    public void onSendGmail(){
        SignInView view = view();
        BackgroundMail.newBuilder(view.getActivity())
                .withUsername(view.getContext().getString(R.string.user_name))
                .withPassword(view.getContext().getString(R.string.password))
                .withMailto(view.getContext().getString(R.string.tpcreative))
                .withType(BackgroundMail.TYPE_PLAIN)
                .withSubject("this is the subject")
                .withBody("this is the body")
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



}
