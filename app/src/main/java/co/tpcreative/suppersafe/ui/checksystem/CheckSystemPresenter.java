package co.tpcreative.suppersafe.ui.checksystem;
import android.util.Log;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import co.tpcreative.suppersafe.R;
import co.tpcreative.suppersafe.common.presenter.Presenter;
import co.tpcreative.suppersafe.common.request.UserCloudRequest;
import co.tpcreative.suppersafe.common.services.SupperSafeApplication;
import co.tpcreative.suppersafe.common.util.NetworkUtil;
import co.tpcreative.suppersafe.model.User;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.HttpException;

public class CheckSystemPresenter extends Presenter<CheckSystemView>{

    private final String TAG = CheckSystemPresenter.class.getSimpleName();
    protected User mUser;


    public CheckSystemPresenter(){
        mUser = new User();
        final User user = User.getInstance().getUserInfo();
        if (user!=null){
            mUser = user;
        }
    }

    public void onUserCloudChecking(){
        Log.d(TAG,"info");
        CheckSystemView view = view();
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
        hash.put(getString(R.string.key_user_id),mUser.email);
        hash.put(getString(R.string.key_device_id), SupperSafeApplication.getInstance().getDeviceId());
        subscriptions.add(SupperSafeApplication.serverAPI.onCheckUserCloud(hash)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.startLoading())
                .subscribe(onResponse -> {
                    Log.d(TAG, "Body : " + new Gson().toJson(onResponse));
                    if (onResponse.error){
                        view.showError(onResponse.message);
                    }
                    else{
                        view.showSuccessful(onResponse.cloud_id);
                    }
                    view.stopLoading();
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
        CheckSystemView view = view();
        String value = view.getContext().getString(res);
        return value;
    }





}
