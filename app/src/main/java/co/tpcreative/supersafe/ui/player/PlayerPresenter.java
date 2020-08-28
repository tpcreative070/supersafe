package co.tpcreative.supersafe.ui.player;
import android.app.Activity;
import android.os.Bundle;
import com.google.android.exoplayer2.source.MediaSource;
import java.util.ArrayList;
import java.util.List;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.entities.ItemEntity;
import co.tpcreative.supersafe.common.entities.MainCategoryEntity;
import co.tpcreative.supersafe.common.helper.SQLHelper;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.presenter.Presenter;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.common.entities.InstanceGenerator;
import co.tpcreative.supersafe.model.ItemModel;
import co.tpcreative.supersafe.model.MainCategoryModel;

public class PlayerPresenter extends Presenter<BaseView>{
    protected ItemModel mItems ;
    protected MainCategoryModel mainCategories;
    protected List<ItemModel>mList ;
    protected List<MediaSource> mListSource;
    public PlayerPresenter(){
        mList = new ArrayList<>();
        mListSource = new ArrayList<>();
    }

    public void onGetIntent(Activity activity){
        BaseView view = view();
        Bundle bundle = activity.getIntent().getExtras();
        try {
            final ItemModel items = (ItemModel) bundle.get(activity.getString(R.string.key_items));
            mainCategories = (MainCategoryModel) bundle.get(activity.getString(R.string.key_main_categories));
            if (items!=null){
                mItems = items;
                if (mainCategories!=null){
                    final List<ItemModel> list = SQLHelper.getListItems(mainCategories.categories_local_id,items.formatType,false,mainCategories.isFakePin);
                    if (list!=null){
                        mList.clear();
                        for (ItemModel index : list){
                            if (!index.items_id.equals(items.items_id)){
                                mList.add(index);
                            }
                        }
                        mItems.isChecked = true;
                        mList.add(0,mItems);
                    }
                }
                view.onSuccessful("Play", EnumStatus.PLAY);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
