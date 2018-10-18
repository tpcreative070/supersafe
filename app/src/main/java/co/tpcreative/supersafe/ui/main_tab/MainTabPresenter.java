package co.tpcreative.supersafe.ui.main_tab;
import android.util.Log;
import com.google.gson.Gson;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.presenter.Presenter;
import co.tpcreative.supersafe.model.User;

public class MainTabPresenter extends Presenter<BaseView> {
    private static final String TAG = MainTabPresenter.class.getSimpleName();
    protected User mUser ;
    public MainTabPresenter(){

    }
    public void onGetUserInfo(){
        mUser = User.getInstance().getUserInfo();
    }

}
