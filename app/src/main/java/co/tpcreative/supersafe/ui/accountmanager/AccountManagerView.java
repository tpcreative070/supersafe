package co.tpcreative.supersafe.ui.accountmanager;

import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.model.User;

public interface AccountManagerView extends BaseView{
    void showError(String message);
    void showSuccessful(String message);
}
