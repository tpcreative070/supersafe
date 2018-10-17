package co.tpcreative.supersafe.ui.me;
import android.util.Log;
import android.view.View;

import com.google.gson.Gson;

import java.util.List;

import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.presenter.Presenter;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.model.EnumFormatType;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.Items;
import co.tpcreative.supersafe.model.User;
import co.tpcreative.supersafe.model.room.InstanceGenerator;

public class MePresenter extends Presenter<BaseView>{

    private static final String TAG = MePresenter.class.getSimpleName();

    protected User mUser;
    protected int videos = 0;
    protected int photos = 0;
    protected int audios = 0;



    public MePresenter(){

    }

    public void onShowUserInfo(){
       mUser = User.getInstance().getUserInfo();
       Log.d(TAG,new Gson().toJson(mUser));
    }

    public void onCalculate(){
        try {
            BaseView view = view();
            photos = 0;
            videos = 0;
            audios = 0;
            final List<Items> mList = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getListAllItems(false,false);
            for (Items index : mList){
                final EnumFormatType enumTypeFile = EnumFormatType.values()[index.formatType];
                switch (enumTypeFile){
                    case IMAGE:{
                        photos+=1;
                        break;
                    }
                    case VIDEO:{
                        videos+=1;
                        break;
                    }
                    case AUDIO:{
                        audios+=1;
                        break;
                    }
                }
            }
            view.onSuccessful("Successful", EnumStatus.RELOAD);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

}
