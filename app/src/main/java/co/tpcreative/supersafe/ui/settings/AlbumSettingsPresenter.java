package co.tpcreative.supersafe.ui.settings;
import android.app.Activity;
import android.os.Bundle;
import com.google.gson.Gson;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.presenter.Presenter;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.MainCategories;

public class AlbumSettingsPresenter extends Presenter<AlbumSettingsView> {

    protected MainCategories mMainCategories;
    private static final String TAG = AlbumSettingsPresenter.class.getSimpleName();

    public AlbumSettingsPresenter(){
        mMainCategories = new MainCategories();
    }

    public void getData(Activity activity){
        Bundle bundle = activity.getIntent().getExtras();
        try{
            final MainCategories mainCategories = (MainCategories) bundle.get(activity.getString(R.string.key_main_categories));
            if (mainCategories!=null){
                this.mMainCategories = mainCategories;
            }
            Utils.Log(TAG,new Gson().toJson(this.mMainCategories));
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

}
