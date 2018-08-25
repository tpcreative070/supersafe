package co.tpcreative.suppersafe.ui.me;
import android.util.Log;
import com.google.gson.Gson;
import co.tpcreative.suppersafe.common.presenter.Presenter;
import co.tpcreative.suppersafe.model.User;

public class MePresenter extends Presenter<MeView>{

    private static final String TAG = MePresenter.class.getSimpleName();

    protected User mUser;

    public MePresenter(){

    }

    public void onShowUserInfo(){
       mUser = User.getInstance().getUserInfo();
       Log.d(TAG,new Gson().toJson(mUser));
    }

}
