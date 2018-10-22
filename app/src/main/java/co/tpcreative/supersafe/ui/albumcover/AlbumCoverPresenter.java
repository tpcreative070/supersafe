package co.tpcreative.supersafe.ui.albumcover;
import android.app.Activity;
import android.os.Bundle;
import com.google.gson.Gson;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.presenter.Presenter;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.MainCategories;

public class AlbumCoverPresenter extends Presenter<BaseView> {

    protected MainCategories mMainCategories;
    private static final String TAG = AlbumCoverPresenter.class.getSimpleName();
    public AlbumCoverPresenter(){
        mMainCategories = new MainCategories();
    }

    public void getData(Activity activity){
        BaseView view = view();
        Bundle bundle = activity.getIntent().getExtras();
        try{
            final MainCategories mainCategories = (MainCategories) bundle.get(MainCategories.class.getSimpleName());
            if (mainCategories!=null){
                this.mMainCategories = mainCategories;
                view.onSuccessful("Successful",EnumStatus.RELOAD);
            }
            Utils.Log(TAG,new Gson().toJson(this.mMainCategories));
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


}
