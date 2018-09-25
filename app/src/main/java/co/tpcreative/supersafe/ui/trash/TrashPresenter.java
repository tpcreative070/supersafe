package co.tpcreative.supersafe.ui.trash;
import android.app.Activity;
import com.google.gson.Gson;
import com.snatik.storage.Storage;
import java.util.ArrayList;
import java.util.List;
import co.tpcreative.supersafe.common.presenter.Presenter;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.Items;
import co.tpcreative.supersafe.model.room.InstanceGenerator;

public class TrashPresenter extends Presenter<TrashView>{

    private static final String TAG = TrashPresenter.class.getSimpleName();

    protected List<Items> mList;
    protected Storage storage;
    public TrashPresenter(){
        mList = new ArrayList<>();
        storage = new Storage(SuperSafeApplication.getInstance());
    }

    public void  getData(Activity activity){
        TrashView view = view();
        mList.clear();
        try {
            final List<Items> data = InstanceGenerator.getInstance(view.getContext()).getDeleteLocalListItems(true,false);
            if (data!=null){
                Utils.Log(TAG,new Gson().toJson(data));
                mList = data;
            }
            view.onReloadData();
        }
        catch (Exception e){
            Utils.onWriteLog(""+e.getMessage(), EnumStatus.WRITE_FILE);
        }
    }

    public void onDeleteAll(boolean isEmpty){
        TrashView view = view();
        for (int i = 0 ;i <mList.size();i++){
            if (isEmpty){
                if (mList.get(i).global_original_id==null & mList.get(i).global_thumbnail_id ==null){
                    InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onDelete(mList.get(i));
                }
                else{
                    mList.get(i).isWaitingSyncDeleteGlobal = true;
                    InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onUpdate(mList.get(i));
                }
                storage.deleteDirectory(SuperSafeApplication.getInstance().getSupersafePrivate()+mList.get(i).local_id);
            }
            else{
                mList.get(i).isDeleteLocal = false;
                InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onUpdate(mList.get(i));
            }
        }
        view.onDone();
    }

}
