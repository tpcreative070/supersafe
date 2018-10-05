package co.tpcreative.supersafe.ui.accountmanager;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.presenter.Presenter;

public class AccountManagerPresenter extends Presenter<BaseView>{
    private static final String TAG = AccountManagerPresenter.class.getSimpleName();

    public AccountManagerPresenter(){

    }

    private String getString(int res){
        BaseView view = view();
        String value = view.getContext().getString(res);
        return value;
    }

}
