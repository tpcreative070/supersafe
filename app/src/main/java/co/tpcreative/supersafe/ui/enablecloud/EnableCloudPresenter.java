package co.tpcreative.supersafe.ui.enablecloud;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.controller.ServiceManager;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.presenter.Presenter;
import co.tpcreative.supersafe.common.request.UserCloudRequest;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.NetworkUtil;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.User;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.HttpException;

public class EnableCloudPresenter extends Presenter<BaseView>{

    private static final String TAG = EnableCloudPresenter.class.getSimpleName();
    protected User mUser;
    public EnableCloudPresenter(){
        mUser = new User();
    }

    public void onUserInfo(){
        final User user = User.getInstance().getUserInfo();
        if (user!=null){
            mUser = user;
        }
    }

    private String getString(int res){
        BaseView view = view();
        String value = view.getContext().getString(res);
        return value;
    }

    public void onAddUserCloud(UserCloudRequest cloudRequest){
        Utils.Log(TAG,"info");
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
        subscriptions.add(SuperSafeApplication.serverAPI.onAddUserCloud(cloudRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.onStartLoading(EnumStatus.CREATE))
                .subscribe(onResponse -> {
                    Utils.Log(TAG, "Body : " + new Gson().toJson(onResponse));
                    if (onResponse.error){
                        view.onError(onResponse.message,EnumStatus.CREATE);
                    }
                    else{
                        view.onSuccessful(mUser.cloud_id,EnumStatus.CREATE);
                    }
                    view.onStopLoading(EnumStatus.CREATE);
                }, throwable -> {
                    if (throwable instanceof HttpException) {
                        ResponseBody bodys = ((HttpException) throwable).response().errorBody();
                        int code  = ((HttpException) throwable).response().code();
                        try {
                            if (code==401){
                                Utils.Log(TAG,"code "+code);
                                ServiceManager.getInstance().onUpdatedUserToken();
                            }
                            Utils.Log(TAG,"error" +bodys.string());
                            String msg = new Gson().toJson(bodys.string());
                            Utils.Log(TAG, msg);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Utils.Log(TAG, "Can not call" + throwable.getMessage());
                    }
                    view.onStopLoading(EnumStatus.CREATE);
                }));
    }

}
