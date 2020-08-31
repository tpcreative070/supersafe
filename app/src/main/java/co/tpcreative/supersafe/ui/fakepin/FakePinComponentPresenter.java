package co.tpcreative.supersafe.ui.fakepin;
import com.google.gson.Gson;
import com.snatik.storage.Storage;
import java.util.ArrayList;
import java.util.List;

import co.tpcreative.supersafe.common.entities.ItemEntity;
import co.tpcreative.supersafe.common.entities.MainCategoryEntity;
import co.tpcreative.supersafe.common.helper.SQLHelper;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.presenter.Presenter;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.common.entities.InstanceGenerator;
import co.tpcreative.supersafe.model.ItemModel;
import co.tpcreative.supersafe.model.MainCategoryModel;

public class FakePinComponentPresenter extends Presenter<BaseView> {
    protected List<MainCategoryModel> mList;
    protected Storage storage;
    private static final String TAG = FakePinComponentPresenter.class.getSimpleName();
    public FakePinComponentPresenter(){
        mList = new ArrayList<>();
    }

    public void  getData(){
        try {
            BaseView view = view();
            mList = SQLHelper.getListFakePin();
            storage = new Storage(SuperSafeApplication.getInstance());
            view.onSuccessful("Successful", EnumStatus.RELOAD);
            Utils.Log(TAG,new Gson().toJson(mList));
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void onDeleteAlbum(int position){
        BaseView view = view();
        try {
            final MainCategoryModel main = mList.get(position);
            if (main!=null){
                final List<ItemModel> mListItems = SQLHelper.getListItems(main.categories_local_id,true);
                for (ItemModel index : mListItems){
                    SQLHelper.deleteItem(index);
                    storage.deleteDirectory(SuperSafeApplication.getInstance().getSupersafePrivate()+index.items_id);
                }
                SQLHelper.deleteCategory(main);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            getData();
        }
    }

}
