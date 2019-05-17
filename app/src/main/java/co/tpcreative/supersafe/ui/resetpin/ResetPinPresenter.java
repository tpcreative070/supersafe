package co.tpcreative.supersafe.ui.resetpin;
import android.util.Log;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.controller.ServiceManager;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.presenter.Presenter;
import co.tpcreative.supersafe.common.request.VerifyCodeRequest;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.NetworkUtil;
import co.tpcreative.supersafe.common.util.Utils;
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
            PrefsController.putString(getString(R.string.key_user),new Gson().toJson(this.mUser));
        }
    }

    public void onVerifyCode(VerifyCodeRequest request){
        Log.d(TAG,"info");
        BaseView view = view();
        if (view == null) {
            view.onError("View is null", EnumStatus.VERIFY);
            return;
        }
        if (NetworkUtil.pingIpAddress(view.getContext())) {
            view.onError("NO internet", EnumStatus.VERIFY);
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
                        final User mUser = User.getInstance().getUserInfo();
                        if (mUser!=null){
                            mUser.verified = true;
                            PrefsController.putString(getString(R.string.key_user),new Gson().toJson(mUser));
                        }
                        view.onSuccessful(onResponse.message,EnumStatus.VERIFY);
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

    public void onRequestCode(VerifyCodeRequest request){
        Log.d(TAG,"info");
        BaseView view = view();
        if (view == null) {
            view.onError("View is null", EnumStatus.REQUEST_CODE);
            return;
        }
        if (NetworkUtil.pingIpAddress(view.getContext())) {
            view.onError("NO internet", EnumStatus.REQUEST_CODE);
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
                        view.onError(onResponse.message,EnumStatus.REQUEST_CODE);
                    }
                    else{
                        final User mUser = User.getInstance().getUserInfo();
                        mUser.code = onResponse.code;
                        this.mUser = mUser;
                        PrefsController.putString(getString(R.string.key_user),new Gson().toJson(mUser));
                        view.onSuccessful(onResponse.message,EnumStatus.REQUEST_CODE);
                        Utils.Log(TAG,new Gson().toJson(mUser));
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
                            final String errorMessage = bodys.string();
                            Log.d(TAG, "error" + errorMessage);
                            view.onError(errorMessage, EnumStatus.REQUEST_CODE);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.d(TAG, "Can not call" + throwable.getMessage());
                    }
                }));
    }

    private String getString(int res){
        String value = SuperSafeApplication.getInstance().getString(res);
        return value;
    }

}
