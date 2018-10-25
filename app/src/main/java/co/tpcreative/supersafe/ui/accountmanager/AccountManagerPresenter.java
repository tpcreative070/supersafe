package co.tpcreative.supersafe.ui.accountmanager;
import java.util.ArrayList;
import java.util.List;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.presenter.Presenter;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.AppLists;
import co.tpcreative.supersafe.model.EnumStatus;

public class AccountManagerPresenter extends Presenter<BaseView>{

    private static final String TAG = AccountManagerPresenter.class.getSimpleName();
    protected List<AppLists> mList;

    public AccountManagerPresenter(){
        mList = new ArrayList<>();
    }

    public void getData(){
        BaseView view = view();
        boolean qrScanner = Utils.appInstalledOrNot("tpcreative.co.qrscanner.free.release");
        mList.add(new AppLists("ic_qrscanner_launcher","QRScanner","Scan code quickly by your hands",qrScanner));
        view.onSuccessful("Successful", EnumStatus.RELOAD);
    }

    private String getString(int res){
        BaseView view = view();
        String value = view.getContext().getString(res);
        return value;
    }

}
