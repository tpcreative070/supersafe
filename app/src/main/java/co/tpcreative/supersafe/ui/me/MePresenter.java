package co.tpcreative.supersafe.ui.me;
import com.google.gson.Gson;
import java.util.List;

import co.tpcreative.supersafe.common.entities.ItemEntity;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.presenter.Presenter;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumFormatType;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.User;
import co.tpcreative.supersafe.common.entities.InstanceGenerator;

public class MePresenter extends Presenter<BaseView>{

    private static final String TAG = MePresenter.class.getSimpleName();

    protected User mUser;
    protected int videos = 0;
    protected int photos = 0;
    protected int audios = 0;
    protected int others = 0;

    public MePresenter(){
    }

    public void onShowUserInfo(){
       mUser = User.getInstance().getUserInfo();
       Utils.onWriteLog(new Gson().toJson(mUser),EnumStatus.USER_INFO);
    }

    public void onCalculate(){
        try {
            BaseView view = view();
            photos = 0;
            videos = 0;
            audios = 0;
            others = 0;
            final List<ItemEntity> mList = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getListAllItems(false,false);
            for (ItemEntity index : mList){
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
                    case FILES:{
                        others +=1;
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
