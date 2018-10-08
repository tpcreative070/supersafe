package co.tpcreative.supersafe.ui.trash;
import android.app.Activity;
import com.google.gson.Gson;
import com.snatik.storage.Storage;
import java.util.ArrayList;
import java.util.List;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.presenter.Presenter;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumDelete;
import co.tpcreative.supersafe.model.EnumFormatType;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.Items;
import co.tpcreative.supersafe.model.room.InstanceGenerator;

public class TrashPresenter extends Presenter<BaseView>{

    private static final String TAG = TrashPresenter.class.getSimpleName();
    protected List<Items> mList;
    protected Storage storage;
    public TrashPresenter(){
        mList = new ArrayList<>();
        storage = new Storage(SuperSafeApplication.getInstance());
    }

    public void  getData(Activity activity){
        BaseView view = view();
        mList.clear();
        try {
            final List<Items> data = InstanceGenerator.getInstance(view.getContext()).getDeleteLocalListItems(true,EnumDelete.NONE.ordinal(),false);
            if (data!=null){
                mList = data;
            }
            Utils.Log(TAG,new Gson().toJson(data));
            view.onSuccessful("successful",EnumStatus.RELOAD);
        }
        catch (Exception e){
            Utils.onWriteLog(""+e.getMessage(), EnumStatus.WRITE_FILE);
        }
    }

    public void onDeleteAll(boolean isEmpty){
        BaseView view = view();
        for (int i = 0 ;i <mList.size();i++){
            if (isEmpty){
                EnumFormatType formatTypeFile = EnumFormatType.values()[mList.get(i).formatType];
                if (formatTypeFile == EnumFormatType.AUDIO && mList.get(i).global_original_id==null){
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
                storage.deleteDirectory(SuperSafeApplication.getInstance().getSupersafePrivate()+mList.get(i).local_id);
            }
            else{
                mList.get(i).isDeleteLocal = false;
                InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onUpdate(mList.get(i));
            }
        }
        view.onSuccessful("Done",EnumStatus.DONE);
    }

}
