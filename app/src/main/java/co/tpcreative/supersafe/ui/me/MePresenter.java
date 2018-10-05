package co.tpcreative.supersafe.ui.me;
import android.util.Log;
import com.google.gson.Gson;

import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.presenter.Presenter;
import co.tpcreative.supersafe.model.User;

public class MePresenter extends Presenter<BaseView>{

    private static final String TAG = MePresenter.class.getSimpleName();

    protected User mUser;

    public MePresenter(){

    }

    public void onShowUserInfo(){
       mUser = User.getInstance().getUserInfo();
       Log.d(TAG,new Gson().toJson(mUser));
    }

}
