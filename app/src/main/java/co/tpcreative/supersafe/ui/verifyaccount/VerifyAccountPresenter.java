package co.tpcreative.supersafe.ui.verifyaccount;
import android.util.Log;
import com.creativityapps.gmailbackgroundlibrary.BackgroundMail;
import com.google.gson.Gson;
import com.snatik.storage.security.SecurityUtil;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.controller.ServiceManager;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.presenter.Presenter;
import co.tpcreative.supersafe.common.request.SignInRequest;
import co.tpcreative.supersafe.common.request.VerifyCodeRequest;
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

public class VerifyAccountPresenter extends Presenter<BaseView> {
    private static final String TAG = VerifyAccountPresenter.class.getSimpleName();
    protected User mUser;

    public VerifyAccountPresenter(){
        mUser = User.getInstance().getUserInfo();
        if (mUser==null){
            mUser = new User();
        }
    }

    public void onVerifyCode(VerifyCodeRequest request){
        Log.d(TAG,"info");
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
        hash.put(getString(R.string.key_user_id),request.email);
        hash.put(getString(R.string.key_id),request._id);
        hash.put(getString(R.string.key_device_id), SuperSafeApplication.getInstance().getDeviceId());
        hash.put(getString(R.string.key_code),request.code);
        hash.put(getString(R.string.key_appVersionRelease),SuperSafeApplication.getInstance().getAppVersionRelease());
        subscriptions.add(SuperSafeApplication.serverAPI.onVerifyCode(hash)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.onStartLoading(EnumStatus.VERIFY_CODE))
                .subscribe(onResponse -> {
                    view.onStopLoading(EnumStatus.VERIFY_CODE);
                    if (onResponse.error){
                        view.onError(onResponse.message,EnumStatus.VERIFY_CODE);
                    }
                    else{
                        final User mUser = User.getInstance().getUserInfo();
                        if (mUser!=null){
                            mUser.verified = true;
                            if (onResponse.premium!=null){
                                mUser.premium = onResponse.premium;
                            }
                            SuperSafeApplication.getInstance().writeUserSecret(mUser);
                            PrefsController.putString(getString(R.string.key_user),new Gson().toJson(mUser));
                        }
                        view.onSuccessful(onResponse.message,EnumStatus.VERIFY_CODE);
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
                    view.onStopLoading(EnumStatus.VERIFY_CODE);
                }));
    }

    public void onChangeEmail(VerifyCodeRequest request){
        Log.d(TAG,"info");
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
        Map<String,String> hash = new HashMap<>();
        hash.put(getString(R.string.key_user_id),request.user_id);
        hash.put(getString(R.string.key_email),request.email);
        hash.put(getString(R.string.key_other_email),request.other_email);
        hash.put(getString(R.string.key_id),request._id);
        hash.put(getString(R.string.key_device_id), SuperSafeApplication.getInstance().getDeviceId());
        hash.put(getString(R.string.key_device_type),getString(R.string.device_type));
        hash.put(getString(R.string.key_manufacturer), SuperSafeApplication.getInstance().getManufacturer());
        hash.put(getString(R.string.key_name_model), SuperSafeApplication.getInstance().getModel());
        hash.put(getString(R.string.key_version),""+ SuperSafeApplication.getInstance().getVersion());
        hash.put(getString(R.string.key_versionRelease), SuperSafeApplication.getInstance().getVersionRelease());
        hash.put(getString(R.string.key_appVersionRelease), SuperSafeApplication.getInstance().getAppVersionRelease());

        subscriptions.add(SuperSafeApplication.serverAPI.onUpdateUser(hash)
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
                            if (onResponse.user!=null){
                                if (onResponse.user.author!=null){
                                    if (mUser!=null){
                                        mUser.author = onResponse.user.author;
                                        mUser.email = request.email;
                                        mUser.other_email = request.other_email;
                                        mUser.change = true;
                                        PrefsController.putString(getString(R.string.key_user),new Gson().toJson(mUser));
                                    }
                                    else{
                                        Utils.Log(TAG,"User is null");
                                    }
                                    mUser = User.getInstance().getUserInfo();
                                    view.onSuccessful(onResponse.message,EnumStatus.CHANGE_EMAIL);
                                }
                            }
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
                    view.onStopLoading(EnumStatus.CHANGE_EMAIL);
                }));
    }

    public void onResendCode(VerifyCodeRequest request){
        Log.d(TAG,"info");
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
        hash.put(getString(R.string.key_user_id),request.email);
        hash.put(getString(R.string.key_device_id), SuperSafeApplication.getInstance().getDeviceId());
        subscriptions.add(SuperSafeApplication.serverAPI.onResendCode(hash)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.onStartLoading(EnumStatus.RESEND_CODE))
                .subscribe(onResponse -> {
                    if (onResponse.error){
                        view.onError(onResponse.message,EnumStatus.RESEND_CODE);
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
                    view.onStopLoading(EnumStatus.RESEND_CODE);
                }));
    }

    public void onCheckUser(final String email,String other_email){
        Log.d(TAG,"info onCheckUser :"+email);
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
        hash.put(getString(R.string.key_user_id),email);
        hash.put(getString(R.string.key_other_email),other_email);
        hash.put(getString(R.string.key_device_id), SuperSafeApplication.getInstance().getDeviceId());
        subscriptions.add(SuperSafeApplication.serverAPI.onCheckUserId(hash)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.onStartLoading(EnumStatus.CHECK))
                .subscribe(onResponse -> {
                    Log.d(TAG, "Body : " + new Gson().toJson(onResponse));
                    if (onResponse.error){
                        view.onError("User is not existing",EnumStatus.CHECK);
                    }
                    else{
                        view.onSuccessful("User is existing",EnumStatus.CHECK);
                        SignInRequest request = new SignInRequest();
                        request.email = email;
                        onSignIn(request);
                    }
                }, throwable -> {
                    if (throwable instanceof HttpException) {
                        ResponseBody bodys = ((HttpException) throwable).response().errorBody();
                        int code  = ((HttpException) throwable).response().code();
                        try {
                            if (code==403){
                                Utils.Log(TAG,"code "+code);
                                ServiceManager.getInstance().onUpdatedUserToken();
                            }
                            else if (code ==401){
                                SignInRequest request = new SignInRequest();
                                request.email = email;
                                onSignIn(request);
                            }
                            Log.d(TAG,"error" +bodys.string());
                            String msg = new Gson().toJson(bodys.string());
                            Log.d(TAG, msg);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.d(TAG, "Can not call " + throwable.getMessage());
                    }
                    view.onStopLoading(EnumStatus.CHECK);
                }));
    }

    public void onSignIn(SignInRequest request){
        Log.d(TAG,"onSignIn.....");
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

        Map<String,String> hash = new HashMap<>();
        hash.put(getString(R.string.key_email),request.email);
        hash.put(getString(R.string.key_password), SecurityUtil.key_password_default);
        hash.put(getString(R.string.key_device_id), SuperSafeApplication.getInstance().getDeviceId());
        hash.put(getString(R.string.key_device_type),getString(R.string.device_type));
        hash.put(getString(R.string.key_manufacturer), SuperSafeApplication.getInstance().getManufacturer());
        hash.put(getString(R.string.key_name_model), SuperSafeApplication.getInstance().getModel());
        hash.put(getString(R.string.key_version),""+ SuperSafeApplication.getInstance().getVersion());
        hash.put(getString(R.string.key_versionRelease), SuperSafeApplication.getInstance().getVersionRelease());
        hash.put(getString(R.string.key_appVersionRelease), SuperSafeApplication.getInstance().getAppVersionRelease());
        subscriptions.add(SuperSafeApplication.serverAPI.onSignIn(hash)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.onStartLoading(EnumStatus.SIGN_IN))
                .subscribe(onResponse -> {
                    if (onResponse.error){
                        view.onError(onResponse.message,EnumStatus.SIGN_IN);
                    }
                    else{
                        mUser = onResponse.user;
                        PrefsController.putString(getString(R.string.key_user),new Gson().toJson(onResponse.user));
                        String email = mUser.email;
                        if (mUser.change){
                            email = mUser.other_email;
                        }
                        onSendGmail(email,onResponse.user.code);
                        ServiceManager.getInstance().onInitConfigurationFile();
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
                            view.onError(msg,EnumStatus.SIGN_IN);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.d(TAG, "Can not call " + throwable.getMessage());
                    }
                    view.onStopLoading(EnumStatus.SIGN_IN);
                }));
    }


    public void onSendGmail(String email,String code){
        Log.d(TAG,"email :"+email);
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
                        if (view!=null){
                            view.onStopLoading(EnumStatus.SEND_EMAIL);
                            view.onSuccessful("successful",EnumStatus.SEND_EMAIL);
                        }
                    }
                })
                .withOnFailCallback(new BackgroundMail.OnFailCallback() {
                    @Override
                    public void onFail() {
                        //do some magic
                        Log.d(TAG,"Failed");
                        if (view!=null){
                            view.onStopLoading(EnumStatus.SEND_EMAIL);
                        }
                    }
                })
                .send();
    }

    private String getString(int res){
        BaseView view = view();
        String value = view.getContext().getString(res);
        return value;
    }

}
