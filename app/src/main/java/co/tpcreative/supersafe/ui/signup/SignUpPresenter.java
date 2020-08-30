package co.tpcreative.supersafe.ui.signup;
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
import co.tpcreative.supersafe.common.request.SignUpRequest;
import co.tpcreative.supersafe.common.response.DataResponse;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.NetworkUtil;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.User;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.HttpException;

public class SignUpPresenter extends Presenter<BaseView<User>> {

    private static String TAG = SignUpPresenter.class.getSimpleName();

    public SignUpPresenter() {

    }

    public void onSignUp(SignUpRequest request) {
        Utils.Log(TAG, "info onSignUp");
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
        subscriptions.add(SuperSafeApplication.serverAPI.onSignUP(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.onStartLoading(EnumStatus.SIGN_UP))
                .subscribe(onResponse -> {
                    view.onStopLoading(EnumStatus.SIGN_UP);
                    if (onResponse.error) {
                        view.onError(onResponse.responseMessage, EnumStatus.SIGN_UP);
                    } else {
                        final DataResponse mData = onResponse.data;
                        PrefsController.putString(getString(R.string.key_user), new Gson().toJson(mData.user));
                        view.onSuccessful(onResponse.responseMessage, EnumStatus.SIGN_UP, mData.user);
                        ServiceManager.getInstance().onInitConfigurationFile();
                    }
                    Utils.Log(TAG, "Body : " + new Gson().toJson(onResponse));
                }, throwable -> {
                    if (throwable instanceof HttpException) {
                        ResponseBody body = ((HttpException) throwable).response().errorBody();
                        try {
                            final String value = body.string();
                            Utils.Log(TAG,value);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Utils.Log(TAG, "Can not call" + throwable.getMessage());
                    }
                    view.onStopLoading(EnumStatus.SIGN_UP);
                }));
    }

    private String getString(int res) {
        BaseView view = view();
        String value = view.getContext().getString(res);
        return value;
    }
}
