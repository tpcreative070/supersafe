package co.tpcreative.supersafe.ui.theme;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.presenter.Presenter;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.ThemeApp;

public class ThemeSettingsPresenter extends Presenter<BaseView>{

    protected List<ThemeApp>mList;
    protected ThemeApp mThemeApp;
    private static final String TAG = ThemeSettingsPresenter.class.getSimpleName();

    public ThemeSettingsPresenter(){
        mList = new ArrayList<>();
    }

    public void getData(){
        BaseView view = view();
        mList = ThemeApp.getInstance().getList();
        mThemeApp = ThemeApp.getInstance().getThemeInfo();
        if (mThemeApp !=null){
            for(int i = 0;i <mList.size() ;i++){
                if (mThemeApp.getId()==mList.get(i).getId()){
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

    public void getDataReload(){
        BaseView view = view();
        mList = ThemeApp.getInstance().getList();
        mThemeApp = ThemeApp.getInstance().getThemeInfo();
        if (mThemeApp !=null){
            for(int i = 0;i <mList.size() ;i++){
                if (mThemeApp.getId()==mList.get(i).getId()){
                    mList.get(i).isCheck = true;
                }
                else{
                    mList.get(i).isCheck = false;
                }
            }
        }
        Utils.Log(TAG,"Value :" + new Gson().toJson(mList));
        view.onSuccessful("Successful", EnumStatus.RELOAD);
    }

}
