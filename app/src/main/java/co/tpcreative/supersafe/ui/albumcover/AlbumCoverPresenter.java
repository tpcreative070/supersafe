package co.tpcreative.supersafe.ui.albumcover;
import android.app.Activity;
import android.os.Bundle;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.presenter.Presenter;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumFormatType;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.Items;
import co.tpcreative.supersafe.model.MainCategories;
import co.tpcreative.supersafe.model.room.InstanceGenerator;

public class AlbumCoverPresenter extends Presenter<BaseView> {

    protected MainCategories mMainCategories;
    protected List<Items> mList;
    protected List<MainCategories> mListMainCategories;
    private static final String TAG = AlbumCoverPresenter.class.getSimpleName();

    public AlbumCoverPresenter() {
        mMainCategories = new MainCategories();
        mList = new ArrayList<>();
    }

    public void getData(Activity activity) {
        BaseView view = view();
        Bundle bundle = activity.getIntent().getExtras();
        try {
            final MainCategories mainCategories = (MainCategories) bundle.get(MainCategories.class.getSimpleName());
            if (mainCategories != null) {
                this.mMainCategories = mainCategories;
                view.onSuccessful("Successful", EnumStatus.RELOAD);
            }
            Utils.Log(TAG, new Gson().toJson(this.mMainCategories));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Items> getData() {
        BaseView view = view();
        mList.clear();
        final List<Items> data = InstanceGenerator.getInstance(view.getContext()).getListItems(mMainCategories.categories_local_id, EnumFormatType.IMAGE.ordinal(),false, mMainCategories.isFakePin);
        if (data != null) {
            mList = data;
            final Items oldItem = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getItemId(mMainCategories.items_id);
            if (oldItem != null) {
                for (int i = 0; i < mList.size(); i++) {
                    if (oldItem.items_id.equals(mList.get(i).items_id)) {
                        mList.get(i).isChecked = true;
                        Utils.Log(TAG, "Checked item " + i);
                    } else {
                        mList.get(i).isChecked = false;
                    }
                }
            } else {
                for (int i = 0; i < mList.size(); i++) {
                    mList.get(i).isChecked = false;
                }
            }
        }

        Utils.Log(TAG,"Count list "+ mList.size());

        //Utils.Log(TAG,"Categories "+new Gson().toJson(mMainCategories));
        final MainCategories oldMainCategories = MainCategories.getInstance().getCategoriesPosition(mMainCategories.mainCategories_Local_Id);
        mListMainCategories = MainCategories.getInstance().getCategoriesDefault();
        if (oldMainCategories != null) {
            Utils.Log(TAG,"Main categories " + oldMainCategories.mainCategories_Local_Id);
            for (int i = 0; i < mListMainCategories.size(); i++) {
                if (oldMainCategories.mainCategories_Local_Id.equals(mListMainCategories.get(i).mainCategories_Local_Id)) {
                    mListMainCategories.get(i).isChecked = true;
                    Utils.Log(TAG, "Checked categories " + i);
                } else {
                    mListMainCategories.get(i).isChecked = false;
                }
            }
        } else {
            Utils.Log(TAG,"Main categories is null");
            for (int i = 0; i < mListMainCategories.size(); i++) {
                mListMainCategories.get(i).isChecked = false;
            }
        }

        Utils.Log(TAG, "List :" + mList.size());
        view.onSuccessful("Successful", EnumStatus.GET_LIST_FILE);
        return mList;

    }


    private String getString(int res) {
        BaseView view = view();
        String value = view.getContext().getString(res);
        return value;
    }


}
