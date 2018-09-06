package co.tpcreative.suppersafe.ui.privates;

import android.util.Log;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import co.tpcreative.suppersafe.R;
import co.tpcreative.suppersafe.common.controller.PrefsController;
import co.tpcreative.suppersafe.common.presenter.Presenter;
import co.tpcreative.suppersafe.common.services.SupperSafeApplication;
import co.tpcreative.suppersafe.common.util.Utils;
import co.tpcreative.suppersafe.model.MainCategories;

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
            PrefsController.putString(SupperSafeApplication.getInstance().getString(R.string.key_main_categories),new Gson().toJson(mList));
        }
        view.onReload();
        Utils.Log(TAG,new Gson().toJson(mList));
    }

}
