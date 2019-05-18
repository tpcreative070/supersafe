package co.tpcreative.supersafe.ui.lockscreen;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.presenter.Presenter;
import co.tpcreative.supersafe.model.EnumPinAction;
import co.tpcreative.supersafe.model.EnumStatus;

public class LockScreenPresenter extends Presenter<BaseView<EnumPinAction>>{
    public LockScreenPresenter(){
    }

    public void onChangeStatus(EnumStatus status,EnumPinAction action){
        BaseView view = view();
        view.onSuccessful("Successful",status,action);
    }
}
