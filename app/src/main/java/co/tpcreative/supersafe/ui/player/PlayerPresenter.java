package co.tpcreative.supersafe.ui.player;
import android.app.Activity;
import android.os.Bundle;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.presenter.Presenter;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.Items;

public class PlayerPresenter extends Presenter<BaseView>{

    protected Items mItems ;
    public PlayerPresenter(){

    }

    public void onGetIntent(Activity activity){
        BaseView views = view();
        Bundle bundle = activity.getIntent().getExtras();
        try {
            final Items items = (Items) bundle.get(activity.getString(R.string.key_items));
            if (items!=null){
                mItems = items;
                views.onSuccessful("Play", EnumStatus.PLAY);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

}
