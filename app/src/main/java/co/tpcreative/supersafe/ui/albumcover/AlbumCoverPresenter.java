package co.tpcreative.supersafe.ui.albumcover;
import android.app.Activity;
import android.os.Bundle;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.presenter.Presenter;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.Categories;
import co.tpcreative.supersafe.model.Cover;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.Items;
import co.tpcreative.supersafe.model.MainCategories;
import co.tpcreative.supersafe.model.room.InstanceGenerator;

public class AlbumCoverPresenter extends Presenter<BaseView> {

    protected MainCategories mMainCategories;
    protected List<Items>mList;
    private static final String TAG = AlbumCoverPresenter.class.getSimpleName();

    public AlbumCoverPresenter(){
        mMainCategories = new MainCategories();
        mList = new ArrayList<>();
    }

    public void getData(Activity activity){
        BaseView view = view();
        Bundle bundle = activity.getIntent().getExtras();
        try{
            final MainCategories mainCategories = (MainCategories) bundle.get(MainCategories.class.getSimpleName());
            if (mainCategories!=null){
                this.mMainCategories = mainCategories;
                view.onSuccessful("Successful",EnumStatus.RELOAD);
            }
            Utils.Log(TAG,new Gson().toJson(this.mMainCategories));
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void getData(){
        BaseView view = view();
        mList.clear();
        final List<Items> data = InstanceGenerator.getInstance(view.getContext()).getListItems(mMainCategories.categories_local_id,false,mMainCategories.isFakePin);
        if (data!=null){
            mList = data;
            final Items oldItem = Items.getInstance().getObject(mMainCategories.item);
            if (oldItem!=null){
                for (int i = 0 ; i<mList.size() ; i++){
                    if (oldItem.local_id.equals(mList.get(i).local_id)){
                        mList.get(i).isChecked = true;
                    }
                    else{
                        mList.get(i).isChecked = false;
                    }
                }
            }
        }
        Utils.Log(TAG,"List :"+ mList.size());
        view.onSuccessful("Successful",EnumStatus.GET_LIST_FILE);
    }

    private String getString(int res){
        BaseView view = view();
        String value = view.getContext().getString(res);
        return value;
    }



}
