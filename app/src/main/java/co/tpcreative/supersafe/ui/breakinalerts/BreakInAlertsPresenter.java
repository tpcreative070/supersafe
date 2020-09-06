package co.tpcreative.supersafe.ui.breakinalerts;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import co.tpcreative.supersafe.common.helper.SQLHelper;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.presenter.Presenter;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.BreakInAlertsModel;
import co.tpcreative.supersafe.model.EnumStatus;

public class BreakInAlertsPresenter extends Presenter<BaseView>{
    private static final String TAG = BreakInAlertsPresenter.class.getSimpleName();
    protected List<BreakInAlertsModel> mList;
    public BreakInAlertsPresenter(){
        mList = new ArrayList<>();
    }

    public void onGetData(){
        BaseView view = view();
        mList = SQLHelper.getBreakInAlertsList();
        view.onSuccessful("Successful", EnumStatus.RELOAD);
        Utils.Log(TAG,"Result :"+new Gson().toJson(mList));
    }

    public void onDeleteAll(){
        BaseView view = view();
        for (BreakInAlertsModel index : mList){
             Utils.onDeleteFile(index.fileName);
             SQLHelper.onDelete(index);
        }
        view.onSuccessful("Deleted successful",EnumStatus.DELETE);
    }
}
