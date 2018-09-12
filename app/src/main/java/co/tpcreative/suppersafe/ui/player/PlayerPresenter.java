package co.tpcreative.suppersafe.ui.player;
import android.app.Activity;
import android.os.Bundle;
import co.tpcreative.suppersafe.R;
import co.tpcreative.suppersafe.common.presenter.Presenter;
import co.tpcreative.suppersafe.model.Items;

public class PlayerPresenter extends Presenter<PlayerViews>{

    protected Items mItems ;

    public PlayerPresenter(){

    }

    public void onGetIntent(Activity activity){
        PlayerViews views = view();
        Bundle bundle = activity.getIntent().getExtras();
        try {
            final Items items = (Items) bundle.get(activity.getString(R.string.key_items));
            if (items!=null){
                mItems = items;
                views.onPlay();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

}
