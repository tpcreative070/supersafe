package co.tpcreative.supersafe.ui.lockscreen;
import co.tpcreative.supersafe.common.presenter.Presenter;
import co.tpcreative.supersafe.model.EnumPinAction;

public class LockScreenPresenter extends Presenter<LockScreenView>{

    public LockScreenPresenter(){

    }

    public void onChangeStatus(EnumPinAction action){
        LockScreenView view = view();
        view.onChangeStatus(action);
    }


}
