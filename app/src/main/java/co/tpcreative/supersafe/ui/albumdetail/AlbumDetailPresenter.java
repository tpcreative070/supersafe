package co.tpcreative.supersafe.ui.albumdetail;
import android.app.Activity;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.List;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.presenter.Presenter;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.Items;
import co.tpcreative.supersafe.model.MainCategories;
import co.tpcreative.supersafe.model.room.InstanceGenerator;

public class AlbumDetailPresenter extends Presenter<BaseView> {

    protected List<Items> mList;
    protected MainCategories mainCategories;
    public AlbumDetailPresenter(){
        mList = new ArrayList<>();
    }

    public void  getData(Activity activity){
        BaseView view = view();
        mList.clear();
        try {
            Bundle bundle = activity.getIntent().getExtras();
            mainCategories = (MainCategories) bundle.get(SuperSafeApplication.getInstance().getString(R.string.key_main_categories));
            if (mainCategories!=null){
                final List<Items> data = InstanceGenerator.getInstance(view.getContext()).getListItems(mainCategories.categories_local_id,false,mainCategories.isFakePin);
                if (data!=null){
                    mList = data;
                }
                view.onSuccessful("Successful",EnumStatus.RELOAD);
            }
            else{
                Utils.onWriteLog("Main categories is null", EnumStatus.WRITE_FILE);
            }
        }
        catch (Exception e){
            Utils.onWriteLog(""+e.getMessage(), EnumStatus.WRITE_FILE);
        }
    }

    public void  getData(){
        BaseView view = view();
        mList.clear();
        try {
            if (mainCategories!=null){
                final List<Items> data = InstanceGenerator.getInstance(view.getContext()).getListItems(mainCategories.categories_local_id,false,mainCategories.isFakePin);
                if (data!=null){
                    mList = data;
                }
                view.onSuccessful("Successful",EnumStatus.RELOAD);
            }
            else{
                Utils.onWriteLog("Main categories is null", EnumStatus.WRITE_FILE);
            }
        }
        catch (Exception e){
            Utils.onWriteLog(""+e.getMessage(), EnumStatus.WRITE_FILE);
        }
    }

}
