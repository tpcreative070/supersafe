package co.tpcreative.supersafe.ui.privates;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.controller.PrefsController;
import co.tpcreative.supersafe.common.presenter.Presenter;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.MainCategories;

public class PrivatePresenter extends Presenter<PrivateView> {
    protected List<MainCategories> mList;
    private static final String TAG = PrivatePresenter.class.getSimpleName();
    public PrivatePresenter(){
        mList = new ArrayList<>();
    }

    public void  getData(){
        PrivateView view = view();
        mList.clear();
        final List<MainCategories> result = MainCategories.getInstance().getMainCategoriesList();
        if (result!=null){
            mList = result;
        }
        else{
            mList = MainCategories.getInstance().getList();
            PrefsController.putString(SuperSafeApplication.getInstance().getString(R.string.key_main_categories),new Gson().toJson(mList));
        }
        view.onReload();
        Utils.Log(TAG,new Gson().toJson(mList));
    }

}