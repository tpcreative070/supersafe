package co.tpcreative.supersafe.ui.cloudmanager;
import java.util.List;
import co.tpcreative.supersafe.common.controller.ServiceManager;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.presenter.Presenter;
import co.tpcreative.supersafe.common.response.DriveResponse;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.services.SuperSafeServiceView;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.Items;
import co.tpcreative.supersafe.model.room.InstanceGenerator;

public class CloudManagerPresenter extends Presenter<BaseView<Long>>{

    private static final String TAG = CloudManagerPresenter.class.getSimpleName();


    public CloudManagerPresenter(){

    }

    public void onGetList(){
        BaseView view  = view();
        if (ServiceManager.getInstance().getMyService()==null){
            view.onError("Service is null", EnumStatus.GET_LIST_FILES_IN_APP);
            return;
        }
        ServiceManager.getInstance().getMyService().onGetListFileInApp(new SuperSafeServiceView() {
            @Override
            public void onError(String message, EnumStatus status) {
                view.onError(message, EnumStatus.GET_LIST_FILES_IN_APP);
                Utils.Log(TAG,"error");
            }

            @Override
            public void onSuccessful(String message) {
                Utils.Log(TAG,"onSuccessful");
            }

            @Override
            public void onSuccessful(String message, EnumStatus status) {
                Utils.Log(TAG,"onSuccessful");
                final List<Items> mList = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getListItemId(true,false);
                long countSize = 0;
                try {
                    for (Items index : mList){
                        countSize = Long.parseLong(index.size);
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                view.onSuccessful(message, EnumStatus.GET_LIST_FILES_IN_APP,countSize);
            }

            @Override
            public void onSuccessful(List<DriveResponse> lists) {
                Utils.Log(TAG,"onSuccessful");
            }

            @Override
            public void onStartLoading() {

            }

            @Override
            public void onStopLoading() {

            }
        });

    }



}
