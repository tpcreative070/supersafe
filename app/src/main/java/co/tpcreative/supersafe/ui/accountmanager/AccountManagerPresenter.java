package co.tpcreative.supersafe.ui.accountmanager;
import co.tpcreative.supersafe.common.presenter.Presenter;

public class AccountManagerPresenter extends Presenter<AccountManagerView>{
    private static final String TAG = AccountManagerPresenter.class.getSimpleName();

    public AccountManagerPresenter(){

    }

    private String getString(int res){
        AccountManagerView view = view();
        String value = view.getContext().getString(res);
        return value;
    }

}
