package co.tpcreative.supersafe.ui.privates;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.controller.ServiceManager;
import co.tpcreative.supersafe.common.presenter.Presenter;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.Items;
import co.tpcreative.supersafe.model.MainCategories;
import co.tpcreative.supersafe.model.room.InstanceGenerator;

public class PrivatePresenter extends Presenter<PrivateView> {
    protected List<MainCategories> mList;
    private static final String TAG = PrivatePresenter.class.getSimpleName();
    public PrivatePresenter(){
        mList = new ArrayList<>();
    }

    public void  getData(){
        PrivateView view = view();
        mList = MainCategories.getInstance().getList();

        view.onReload();
        Utils.Log(TAG,new Gson().toJson(mList));
    }


    public void onDeleteAlbum(int position){
        PrivateView view = view();
        try {
            final MainCategories main = mList.get(position);
            if (main!=null){
                final List<Items> mListItems = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getListItems(main.categories_local_id,false);
                for (Items index : mListItems){
                    index.isDeleteLocal = true;
                    InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onUpdate(index);
                }
                main.isDelete = true;
                InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onUpdate(main);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            view.onReload();
            ServiceManager.getInstance().onSyncDataOwnServer("0");
        }
    }

}
