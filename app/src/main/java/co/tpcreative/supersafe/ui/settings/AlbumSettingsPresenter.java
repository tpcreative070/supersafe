package co.tpcreative.supersafe.ui.settings;
import android.app.Activity;
import android.os.Bundle;
import com.google.gson.Gson;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.common.presenter.Presenter;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.MainCategories;
import co.tpcreative.supersafe.model.room.InstanceGenerator;

public class AlbumSettingsPresenter extends Presenter<BaseView> {

    protected MainCategories mMainCategories;
    private static final String TAG = AlbumSettingsPresenter.class.getSimpleName();

    public AlbumSettingsPresenter(){
        mMainCategories = new MainCategories();
    }

    public void getData(Activity activity){
        BaseView view = view();
        Bundle bundle = activity.getIntent().getExtras();
        try{
            final MainCategories mainCategories = (MainCategories) bundle.get(activity.getString(R.string.key_main_categories));
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

    public void getData(){
        BaseView view = view();
        if (mMainCategories==null){
            return;
        }
        try{
            final MainCategories mainCategories = InstanceGenerator.getInstance(SuperSafeApplication.getInstance()).getCategoriesLocalId(mMainCategories.categories_local_id,mMainCategories.isFakePin);
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
