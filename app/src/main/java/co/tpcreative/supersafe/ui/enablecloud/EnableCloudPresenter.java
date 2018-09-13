package co.tpcreative.supersafe.ui.enablecloud;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.presenter.Presenter;
import co.tpcreative.supersafe.common.request.UserCloudRequest;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.NetworkUtil;
import co.tpcreative.supersafe.model.User;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.HttpException;

public class EnableCloudPresenter extends Presenter<EnableCloudView>{

    private static final String TAG = EnableCloudPresenter.class.getSimpleName();

    protected User mUser;
    public EnableCloudPresenter(){
        mUser = new User();
    }

    public void onUserInfo(){
        String value = PrefsController.getString(getString(R.string.key_user),"");
        final User user = new Gson().fromJson(value,User.class);
        if (user!=null){
            mUser = user;
        }
    }

    private String getString(int res){
        EnableCloudView view = view();
        String value = view.getContext().getString(res);
        return value;
    }


    public void onAddUserCloud(UserCloudRequest cloudRequest){
        Log.d(TAG,"info");
        EnableCloudView view = view();
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
        hash.put(getString(R.string.key_user_id),cloudRequest.user_id);
        hash.put(getString(R.string.key_cloud_id),cloudRequest.cloud_id);
        hash.put(getString(R.string.key_device_id), SuperSafeApplication.getInstance().getDeviceId());
        subscriptions.add(SuperSafeApplication.serverAPI.onAddUserCloud(hash)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.startLoading())
                .subscribe(onResponse -> {
                    Log.d(TAG, "Body : " + new Gson().toJson(onResponse));
                    if (onResponse.error){
                        view.showError(onResponse.message);
                    }
                    else{
                        view.showSuccessful(mUser.cloud_id);
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


}
