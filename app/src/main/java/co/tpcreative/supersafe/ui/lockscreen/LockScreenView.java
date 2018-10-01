package co.tpcreative.supersafe.ui.lockscreen;

import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.model.EnumPinAction;

public interface LockScreenView  extends BaseView{
    void onChangeStatus(EnumPinAction action);
}
