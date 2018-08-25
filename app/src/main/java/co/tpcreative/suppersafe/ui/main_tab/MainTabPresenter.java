package co.tpcreative.suppersafe.ui.main_tab;

import android.util.Log;

import com.google.gson.Gson;

import co.tpcreative.suppersafe.common.presenter.Presenter;
import co.tpcreative.suppersafe.model.User;

public class MainTabPresenter extends Presenter<MainTabView> {

    private static final String TAG = MainTabPresenter.class.getSimpleName();

    public MainTabPresenter(){

    }

    public void onGetUserInfo(){
        Log.d(TAG,new Gson().toJson(User.getInstance().getUserInfo()));
    }

}
