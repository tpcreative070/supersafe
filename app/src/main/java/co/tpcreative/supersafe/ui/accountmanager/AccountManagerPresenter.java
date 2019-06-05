package co.tpcreative.supersafe.ui.accountmanager;
import java.util.ArrayList;
import java.util.List;
import co.tpcreative.supersafe.R;
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
        boolean qrScanner = Utils.appInstalledOrNot(getString(R.string.qrscanner_package_name));
        boolean gpsSpeed = Utils.appInstalledOrNot(getString(R.string.gpsspeed_package_name));
        mList.add(new AppLists("ic_qrscanner_launcher","QRScanner","Scan code quickly by your hands",qrScanner,getString(R.string.qrscanner_package_name),getString(R.string.qrscanner_link)));
        mList.add(new AppLists("ic_gpsspeedkmh_launcher","GPSSpeedKmh","Calculate speed without internet",gpsSpeed,getString(R.string.gpsspeed_package_name),getString(R.string.gpsspeed_link)));
        view.onSuccessful("Successful", EnumStatus.RELOAD);
    }

    private String getString(int res){
        BaseView view = view();
        String value = view.getContext().getString(res);
        return value;
    }

}
