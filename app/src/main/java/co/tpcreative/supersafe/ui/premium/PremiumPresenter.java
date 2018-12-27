package co.tpcreative.supersafe.ui.premium;
import android.util.Log;
import com.google.gson.Gson;
import org.solovyev.android.checkout.Purchase;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.controller.ServiceManager;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.presenter.Presenter;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.NetworkUtil;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.CheckoutItems;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.Items;
import co.tpcreative.supersafe.model.User;
import co.tpcreative.supersafe.model.room.InstanceGenerator;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.HttpException;

public class PremiumPresenter extends Presenter<BaseView>{

    private static final String TAG = PremiumPresenter.class.getSimpleName();
    protected User mUser;
    protected List<Items> mList ;
    protected long spaceAvailable=0;
    protected boolean isSaver;

    public PremiumPresenter(){
        mUser = User.getInstance().getUserInfo();
        mList = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getListAllItemsSaved(true,true);
        if (mList==null){
            mList = new ArrayList<>();
        }

        if (mList.size()>0){
            spaceAvailable = 0;
            for (int i = 0;i<mList.size();i++){
                final Items items = mList.get(i);
                items.isChecked = true;
                spaceAvailable +=Long.parseLong(items.size);
            }
        }
        Utils.Log(TAG,new Gson().toJson(mList));
    }


    public void onUpdatedItems(){
        if (mList==null){
            mList = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getListAllItemsSaved(true,true);
            if (mList==null){
                mList = new ArrayList<>();
            }
        }
        for (int i =0;i<mList.size();i++){
            final Items index = mList.get(i);
            index.isSaver = false;
            InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onUpdate(index);
        }
    }

    public void onAddCheckout(final Purchase purchase){
        BaseView view = view();
        if (view == null) {
            view.onError("no view", EnumStatus.CHECKOUT);
            return;
        }
        if (NetworkUtil.pingIpAddress(SuperSafeApplication.getInstance())) {
            view.onError("no connection", EnumStatus.CHECKOUT);
            return;
        }
        if (subscriptions == null) {
            view.onError("no subscriptions", EnumStatus.CHECKOUT);
            return;
        }
        final User user = User.getInstance().getUserInfo();
        if (user == null) {
            view.onError("no user", EnumStatus.CHECKOUT);
            return;
        }

        if (purchase==null){
            return;
        }

        final CheckoutItems checkout = new CheckoutItems();

        if (purchase.sku.equals(getString(R.string.six_months))){
           checkout.isPurchasedSixMonths = true;
        }

        if (purchase.sku.equals(getString(R.string.one_years))){
            checkout.isPurchasedOneYears = true;
        }

        if (purchase.sku.equals(getString(R.string.life_time))){
            checkout.isPurchasedLifeTime = true;
        }
        user.checkout = checkout;
        PrefsController.putString(getString(R.string.key_user),new Gson().toJson(user));

        Map<String, Object> hash = Utils.objectToHashMap(purchase);
        hash.put(getString(R.string.key_user_id),mUser.email);
        hash.put(getString(R.string.key_device_id), SuperSafeApplication.getInstance().getDeviceId());
        hash.put(getString(R.string.key_device_type),getString(R.string.device_type));
        hash.put(getString(R.string.key_manufacturer), SuperSafeApplication.getInstance().getManufacturer());
        hash.put(getString(R.string.key_name_model), SuperSafeApplication.getInstance().getModel());
        hash.put(getString(R.string.key_version).toLowerCase(),""+ SuperSafeApplication.getInstance().getVersion());
        hash.put(getString(R.string.key_versionRelease), SuperSafeApplication.getInstance().getVersionRelease());
        hash.put(getString(R.string.key_appVersionRelease), SuperSafeApplication.getInstance().getAppVersionRelease());
        Utils.onWriteLog(new Gson().toJson(hash),EnumStatus.CHECKOUT);
        subscriptions.add(SuperSafeApplication.serverAPI.onCheckout(hash)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.onStartLoading(EnumStatus.CHECKOUT))
                .subscribe(onResponse -> {
                    if (view == null) {
                        Log.d(TAG, "View is null");
                        return;
                    }
                    if (onResponse.error){
                        view.onError("Error",EnumStatus.CHECKOUT);
                    }
                    else{
                        view.onSuccessful("Successful",EnumStatus.CHECKOUT);
                    }
                    Utils.onWriteLog(new Gson().toJson(onResponse),EnumStatus.CHECKOUT);
                }, throwable -> {
                    if (view == null) {
                        Log.d(TAG, "View is null");
                        return;
                    }
                    if (throwable instanceof HttpException) {
                        ResponseBody bodys = ((HttpException) throwable).response().errorBody();
                        int code  = ((HttpException) throwable).response().code();
                        try {
                            if (code==403){
                                Utils.Log(TAG,"code "+code);
                                ServiceManager.getInstance().onUpdatedUserToken();
                            }
                            Log.d(TAG, "error" + bodys.string());
                            String msg = new Gson().toJson(bodys.string());
                            Log.d(TAG, msg);
                            Utils.onWriteLog("Line 1 "+msg,EnumStatus.CHECKOUT);
                            view.onError("" + msg, EnumStatus.CHECKOUT);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Utils.onWriteLog("Line 2" +e.getMessage(),EnumStatus.CHECKOUT);
                            view.onError("" + e.getMessage(), EnumStatus.CHECKOUT);
                        }
                    } else {
                        Log.d(TAG, "Can not call " + throwable.getMessage());
                        Utils.onWriteLog("Line 3 "+throwable.getMessage(),EnumStatus.CHECKOUT);
                        view.onError("Error :" + throwable.getMessage(), EnumStatus.CHECKOUT);
                    }
                    view.onStopLoading(EnumStatus.CHECKOUT);
                }));

    }

    private String getString(int res){
        BaseView view = view();
        String value = view.getContext().getString(res);
        return value;
    }

}
