package co.tpcreative.supersafe.ui.privates;
import com.snatik.storage.Storage;
import java.util.ArrayList;
import java.util.List;
import co.tpcreative.supersafe.common.controller.ServiceManager;
import co.tpcreative.supersafe.common.entities.ItemEntity;
import co.tpcreative.supersafe.common.entities.MainCategoryEntity;
import co.tpcreative.supersafe.common.helper.SQLHelper;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.presenter.Presenter;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumDelete;
import co.tpcreative.supersafe.model.EnumFormatType;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.common.entities.InstanceGenerator;
import co.tpcreative.supersafe.model.ItemModel;
import co.tpcreative.supersafe.model.MainCategoryModel;

public class PrivatePresenter extends Presenter<BaseView> {
    protected List<MainCategoryModel> mList;
    protected Storage storage;
    private static final String TAG = PrivatePresenter.class.getSimpleName();
    public PrivatePresenter(){
        mList = new ArrayList<>();
    }

    public void  getData(){
        BaseView view = view();
        mList = SQLHelper.getList();
        storage = new Storage(SuperSafeApplication.getInstance());
        view.onSuccessful("Successful", EnumStatus.RELOAD);
    }

    public void onDeleteAlbum(int position){
        BaseView view = view();
        try {
            final MainCategoryModel main = mList.get(position);
            if (main!=null){
                final List<ItemModel> mListItems = SQLHelper.getListItems(main.categories_local_id,false);
                for (ItemModel index : mListItems){
                    index.isDeleteLocal = true;
                    SQLHelper.updatedItem(index);
                }
                main.isDelete = true;
                SQLHelper.updateCategory(main);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            getData();
            ServiceManager.getInstance().onPreparingSyncData();
        }
    }

    public void onEmptyTrash(){
        BaseView view = view();
        try {
            final List<ItemModel> mList = SQLHelper.getDeleteLocalListItems(true, EnumDelete.NONE.ordinal(),false);
            for (int i = 0 ;i <mList.size();i++){
                EnumFormatType formatTypeFile = EnumFormatType.values()[mList.get(i).formatType];
                if (formatTypeFile == EnumFormatType.AUDIO && mList.get(i).global_original_id==null){
                    SQLHelper.deleteItem(mList.get(i));
                }
                else if (formatTypeFile == EnumFormatType.FILES && mList.get(i).global_original_id==null){
                    SQLHelper.deleteItem(mList.get(i));
                }
                else if (mList.get(i).global_original_id==null & mList.get(i).global_thumbnail_id == null){
                    SQLHelper.deleteItem(mList.get(i));
                }
                else{
                    mList.get(i).deleteAction = EnumDelete.DELETE_WAITING.ordinal();
                    SQLHelper.updatedItem(mList.get(i));
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
            ServiceManager.getInstance().onPreparingSyncData();
        }
    }
}
