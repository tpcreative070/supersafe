package co.tpcreative.supersafe.ui.albumcover;
import android.app.Activity;
import android.os.Bundle;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;

import co.tpcreative.supersafe.common.entities.ItemEntity;
import co.tpcreative.supersafe.common.entities.MainCategoryEntity;
import co.tpcreative.supersafe.common.helper.SQLHelper;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.presenter.Presenter;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumFormatType;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.common.entities.InstanceGenerator;
import co.tpcreative.supersafe.model.ItemModel;
import co.tpcreative.supersafe.model.MainCategoryModel;

public class AlbumCoverPresenter extends Presenter<BaseView> {

    protected MainCategoryModel mMainCategories;
    protected List<ItemModel> mList;
    protected List<MainCategoryModel> mListMainCategories;
    private static final String TAG = AlbumCoverPresenter.class.getSimpleName();

    public AlbumCoverPresenter() {
        mMainCategories = new MainCategoryModel();
        mList = new ArrayList<>();
    }

    public void getData(Activity activity) {
        BaseView view = view();
        Bundle bundle = activity.getIntent().getExtras();
        try {
            final MainCategoryModel mainCategories = (MainCategoryModel) bundle.get(MainCategoryModel.class.getSimpleName());
            if (mainCategories != null) {
                this.mMainCategories = mainCategories;
                view.onSuccessful("Successful", EnumStatus.RELOAD);
            }
            Utils.Log(TAG, new Gson().toJson(this.mMainCategories));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<ItemModel> getData() {
        BaseView view = view();
        mList.clear();
        final List<ItemModel> data = SQLHelper.getListItems(mMainCategories.categories_local_id, EnumFormatType.IMAGE.ordinal(),false, mMainCategories.isFakePin);
        if (data != null) {
            mList = data;
            final ItemModel oldItem = SQLHelper.getItemId(mMainCategories.items_id);
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
        final MainCategoryModel oldMainCategories = SQLHelper.getCategoriesPosition(mMainCategories.mainCategories_Local_Id);
        mListMainCategories = SQLHelper.getCategoriesDefault();
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
