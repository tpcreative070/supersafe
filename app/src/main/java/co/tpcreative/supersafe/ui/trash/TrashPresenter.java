package co.tpcreative.supersafe.ui.trash;
import android.app.Activity;
import com.google.gson.Gson;
import com.snatik.storage.Storage;
import java.util.ArrayList;
import java.util.List;

import co.tpcreative.supersafe.common.entities.ItemEntity;
import co.tpcreative.supersafe.common.helper.SQLHelper;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.presenter.Presenter;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumDelete;
import co.tpcreative.supersafe.model.EnumFormatType;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.common.entities.MainCategoryEntity;
import co.tpcreative.supersafe.common.entities.InstanceGenerator;
import co.tpcreative.supersafe.model.ItemModel;
import co.tpcreative.supersafe.model.MainCategoryModel;

public class TrashPresenter extends Presenter<BaseView>{

    private static final String TAG = TrashPresenter.class.getSimpleName();
    protected List<ItemModel> mList;
    protected Storage storage;
    protected int videos = 0;
    protected int photos = 0;
    protected int audios = 0;
    protected int others = 0;


    public TrashPresenter(){
        mList = new ArrayList<>();
        storage = new Storage(SuperSafeApplication.getInstance());
    }

    public void  getData(Activity activity){
        BaseView view = view();
        mList.clear();
        try {
            final List<ItemModel> data = SQLHelper.getDeleteLocalListItems(true,EnumDelete.NONE.ordinal(),false);
            if (data!=null){
                mList = data;
                onCalculate();
            }
            Utils.Log(TAG,new Gson().toJson(data));
            view.onSuccessful("successful",EnumStatus.RELOAD);
        }
        catch (Exception e){
            Utils.onWriteLog(""+e.getMessage(), EnumStatus.WRITE_FILE);
        }
    }

    public void onCalculate(){
        photos = 0;
        videos = 0;
        audios = 0;
        others = 0;
        for (ItemModel index : mList){
            final EnumFormatType enumTypeFile = EnumFormatType.values()[index.formatType];
            switch (enumTypeFile){
                case IMAGE:{
                    photos+=1;
                    break;
                }
                case VIDEO:{
                    videos+=1;
                    break;
                }
                case AUDIO:{
                    audios+=1;
                    break;
                }
                case FILES:{
                    others+=1;
                    break;
                }
            }
        }
    }

    public void onDeleteAll(boolean isEmpty){
        BaseView view = view();
        for (int i = 0 ;i <mList.size();i++){
            if (isEmpty){
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
            else{
               final ItemModel items =  mList.get(i);
               items.isDeleteLocal = false;
                if (mList.get(i).isChecked){
                    final MainCategoryModel mainCategories  = SQLHelper.getCategoriesLocalId(items.categories_local_id);
                    if (mainCategories!=null){
                        mainCategories.isDelete = false;
                        SQLHelper.updateCategory(mainCategories);
                    }
                    SQLHelper.updatedItem(items);
                }
            }
        }
        view.onSuccessful("Done",EnumStatus.DONE);
    }

}
