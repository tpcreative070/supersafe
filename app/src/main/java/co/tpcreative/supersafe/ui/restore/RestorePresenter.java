package co.tpcreative.supersafe.ui.restore;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.presenter.Presenter;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.model.User;

public class RestorePresenter extends Presenter<BaseView> {

    protected User mUser;

    public RestorePresenter(){

    }

    public void onGetData(){
        mUser = SuperSafeApplication.getInstance().readUseSecret();
    }


    public void onRestoreData(){

    }


}
