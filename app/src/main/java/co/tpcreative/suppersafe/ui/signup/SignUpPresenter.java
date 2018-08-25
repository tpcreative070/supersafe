package co.tpcreative.suppersafe.ui.signup;
import android.util.Log;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import co.tpcreative.suppersafe.R;
import co.tpcreative.suppersafe.common.controller.PrefsController;
import co.tpcreative.suppersafe.common.presenter.Presenter;
import co.tpcreative.suppersafe.common.request.SignUpRequest;
import co.tpcreative.suppersafe.common.services.SupperSafeApplication;
import co.tpcreative.suppersafe.common.util.NetworkUtil;
import co.tpcreative.suppersafe.ui.signin.SignInPresenter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.HttpException;

public class SignUpPresenter extends Presenter<SignUpView>{

    private static String TAG = SignUpPresenter.class.getSimpleName();

    public SignUpPresenter(){

    }

    public void onSignUp(SignUpRequest request){
        Log.d(TAG,"info");
        SignUpView view = view();
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
        hash.put(getString(R.string.key_name),request.name);
        hash.put(getString(R.string.key_device_id),SupperSafeApplication.getInstance().getDeviceId());
        hash.put(getString(R.string.key_device_type),getString(R.string.device_type));
        hash.put(getString(R.string.key_manufacturer),SupperSafeApplication.getInstance().getManufacturer());
        hash.put(getString(R.string.key_name_model),SupperSafeApplication.getInstance().getModel());
        hash.put(getString(R.string.key_version),""+SupperSafeApplication.getInstance().getVersion());
        hash.put(getString(R.string.key_versionRelease),SupperSafeApplication.getInstance().getVersionRelease());
        subscriptions.add(SupperSafeApplication.serverAPI.onSignUP(hash)
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
        SignUpView view = view();
        String value = view.getContext().getString(res);
        return value;
    }

}
