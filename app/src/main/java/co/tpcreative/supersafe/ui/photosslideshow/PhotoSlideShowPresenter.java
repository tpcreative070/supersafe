package co.tpcreative.supersafe.ui.photosslideshow;
import android.app.Activity;
import android.os.Bundle;
import com.google.gson.Gson;
import com.snatik.storage.Storage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.entities.ItemEntity;
import co.tpcreative.supersafe.common.entities.MainCategoryEntity;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.presenter.Presenter;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumFormatType;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.common.entities.InstanceGenerator;

public class PhotoSlideShowPresenter extends Presenter<BaseView>{

    private final String TAG = PhotoSlideShowPresenter.class.getSimpleName();
    protected ItemEntity items;
    protected List<ItemEntity> mList;
    protected List<File> mListShare = new ArrayList<>();
    protected EnumStatus status = EnumStatus.OTHER;
    protected Storage storage ;
    protected MainCategoryEntity mainCategories;
    public PhotoSlideShowPresenter(){
        mList = new ArrayList<>();
    }

    public void getIntent(Activity context){
        storage = new Storage(context);
        mList.clear();
        try {
            Bundle bundle = context.getIntent().getExtras();
            items = (ItemEntity) bundle.get(context.getString(R.string.key_items));
            List<ItemEntity> list = (ArrayList<ItemEntity>)bundle.get(context.getString(R.string.key_list_items));
            mainCategories = (MainCategoryEntity) bundle.get(SuperSafeApplication.getInstance().getString(R.string.key_main_categories));
            for (ItemEntity index : list){
                if (!index.items_id.equals(items.items_id)){
                    EnumFormatType formatType = EnumFormatType.values()[index.formatType];
                    if (formatType!=EnumFormatType.FILES){
                        mList.add(index);
                    }
                }
            }
            mList.add(0,items);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        Utils.Log(TAG,new Gson().toJson(items));
    }

    public void onDelete(int position){
        try {
            BaseView view = view();
            final ItemEntity items = mList.get(position);
            final ItemEntity mItem = InstanceGenerator.getInstance(view.getContext()).getItemId(items.items_id,items.isFakePin);
            if (mItem!=null){
                if (mItem.isFakePin){
                    storage.deleteDirectory(SuperSafeApplication.getInstance().getSupersafePrivate()+mItem.items_id);
                    InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).onDelete(mItem);
                }
                else{
                    mItem.isDeleteLocal = true;
                    InstanceGenerator.getInstance(view.getContext()).onUpdate(mItem);
                }
                mList.remove(position);
                view.onSuccessful("Delete Successful", EnumStatus.DELETE);
            }
            else{
                Utils.Log(TAG,"Not found");
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

}
