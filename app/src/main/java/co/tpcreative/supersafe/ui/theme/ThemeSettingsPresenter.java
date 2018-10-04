package co.tpcreative.supersafe.ui.theme;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.presenter.Presenter;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.Theme;

public class ThemeSettingsPresenter extends Presenter<BaseView>{

    protected List<Theme>mList;
    protected Theme mTheme;
    private static final String TAG = ThemeSettingsPresenter.class.getSimpleName();

    public ThemeSettingsPresenter(){
        mList = new ArrayList<>();
    }

    public void getData(){
        BaseView view = view();
        mList = Theme.getInstance().getList();
        mTheme = Theme.getInstance().getThemeInfo();
        if (mTheme!=null){
            for(int i = 0;i <mList.size() ;i++){
                if (mTheme.getId()==mList.get(i).getId()){
                    mList.get(i).isCheck = true;
                }
                else{
                    mList.get(i).isCheck = false;
                }
            }
        }
        Utils.Log(TAG,"Value :" + new Gson().toJson(mList));
        view.onSuccessful("Successful", EnumStatus.SHOW_DATA);
    }

}
