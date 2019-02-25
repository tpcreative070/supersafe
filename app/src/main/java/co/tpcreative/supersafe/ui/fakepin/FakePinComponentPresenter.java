package co.tpcreative.supersafe.ui.fakepin;
import com.google.gson.Gson;
import com.snatik.storage.Storage;
import java.util.ArrayList;
import java.util.List;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.presenter.Presenter;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.Items;
import co.tpcreative.supersafe.model.MainCategories;
import co.tpcreative.supersafe.model.room.InstanceGenerator;

public class FakePinComponentPresenter extends Presenter<BaseView> {
    protected List<MainCategories> mList;
    protected Storage storage;
    private static final String TAG = FakePinComponentPresenter.class.getSimpleName();
    public FakePinComponentPresenter(){
        mList = new ArrayList<>();
    }

    public void  getData(){
        try {
            BaseView view = view();
            mList = MainCategories.getInstance().getListFakePin();
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
            final MainCategories main = mList.get(position);
            if (main!=null){
                final List<Items> mListItems = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getListItems(main.categories_local_id,true);
                for (Items index : mListItems){
                    InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onDelete(index);
                    storage.deleteDirectory(SuperSafeApplication.getInstance().getSupersafePrivate()+index.items_id);
                }
                InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onDelete(main);
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
