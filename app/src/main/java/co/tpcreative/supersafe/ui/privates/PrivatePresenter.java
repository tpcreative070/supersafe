package co.tpcreative.supersafe.ui.privates;
import com.google.gson.Gson;
import com.snatik.storage.Storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.controller.ServiceManager;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.presenter.Presenter;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumDelete;
import co.tpcreative.supersafe.model.EnumFormatType;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.Items;
import co.tpcreative.supersafe.model.MainCategories;
import co.tpcreative.supersafe.model.room.InstanceGenerator;

public class PrivatePresenter extends Presenter<BaseView> {
    protected List<MainCategories> mList;
    protected Storage storage;
    private static final String TAG = PrivatePresenter.class.getSimpleName();
    public PrivatePresenter(){
        mList = new ArrayList<>();
    }

    public void  getData(){
        BaseView view = view();
        mList = MainCategories.getInstance().getList();
        storage = new Storage(SuperSafeApplication.getInstance());
        view.onSuccessful("Successful", EnumStatus.RELOAD);
        Utils.Log(TAG,new Gson().toJson(mList));
    }


    public void onDeleteAlbum(int position){
        BaseView view = view();
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
            getData();
            ServiceManager.getInstance().onSyncDataOwnServer("0");
        }
    }


    public void onEmptyTrash(){
        BaseView view = view();
        try {
            final List<Items> mList = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getDeleteLocalListItems(true, EnumDelete.NONE.ordinal(),false);
            for (int i = 0 ;i <mList.size();i++){
                EnumFormatType formatTypeFile = EnumFormatType.values()[mList.get(i).formatType];
                if (formatTypeFile == EnumFormatType.AUDIO && mList.get(i).global_original_id==null){
                    InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onDelete(mList.get(i));
                }
                else if (formatTypeFile == EnumFormatType.FILES && mList.get(i).global_original_id==null){
                    InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onDelete(mList.get(i));
                }
                else if (mList.get(i).global_original_id==null & mList.get(i).global_thumbnail_id == null){
                    InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onDelete(mList.get(i));
                }
                else{
                    mList.get(i).deleteAction = EnumDelete.DELETE_WAITING.ordinal();
                    InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onUpdate(mList.get(i));
                    Utils.Log(TAG,"ServiceManager waiting for delete");
                }
                storage.deleteDirectory(SuperSafeApplication.getInstance().getSupersafePrivate()+mList.get(i).items_id);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            getData();
            ServiceManager.getInstance().onSyncDataOwnServer("0");
        }

    }


}
