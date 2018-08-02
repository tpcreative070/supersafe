package co.tpcreative.keepsafety.ui.verify;
import android.app.Activity;
import android.os.Bundle;
import co.tpcreative.keepsafety.R;
import co.tpcreative.keepsafety.common.presenter.Presenter;
import co.tpcreative.keepsafety.model.User;

public class VerifyPresenter extends Presenter<VerifyView>{

    protected User user;

    public VerifyPresenter(){
        user = new User();
    }

    public void getIntent(Activity activity){
        VerifyView view = view();
        Bundle bundle = activity.getIntent().getExtras();
        final User result = (User) bundle.get(activity.getString(R.string.key_data));
        if (result!=null){
            user = result;
        }
    }


}
