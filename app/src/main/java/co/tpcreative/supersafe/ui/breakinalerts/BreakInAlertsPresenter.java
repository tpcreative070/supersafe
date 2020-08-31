package co.tpcreative.supersafe.ui.breakinalerts;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.presenter.Presenter;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.BreakInAlerts;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.common.entities.InstanceGenerator;

public class BreakInAlertsPresenter extends Presenter<BaseView>{
    private static final String TAG = BreakInAlertsPresenter.class.getSimpleName();
    protected List<BreakInAlerts> mList;
    public BreakInAlertsPresenter(){
        mList = new ArrayList<>();
    }

    public void onGetData(){
        BaseView view = view();
        mList = BreakInAlerts.getInstance().getList();
        view.onSuccessful("Successful", EnumStatus.RELOAD);
        Utils.Log(TAG,"Result :"+new Gson().toJson(mList));
    }

    public void onDeleteAll(){
        BaseView view = view();
        for (BreakInAlerts index : mList){
            InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onDelete(index);
        }
        view.onSuccessful("Deleted successful",EnumStatus.DELETE);
    }

}
