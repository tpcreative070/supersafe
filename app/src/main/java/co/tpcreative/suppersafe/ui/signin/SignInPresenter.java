package co.tpcreative.suppersafe.ui.signin;
import android.app.Activity;
import android.util.Log;

import com.creativityapps.gmailbackgroundlibrary.BackgroundMail;

import co.tpcreative.suppersafe.R;
import co.tpcreative.suppersafe.common.controller.ManagerNetwork;
import co.tpcreative.suppersafe.common.presenter.Presenter;
import co.tpcreative.suppersafe.ui.signup.SignUpView;

public class SignInPresenter extends Presenter<SignInView>{


    private static final String TAG = SignInPresenter.class.getSimpleName();

    public void onSignIn(String email){
        SignInView view = view();
        ManagerNetwork.getInstance().onSignIn(email, new ManagerNetwork.ManagerNetworkListener() {
            @Override
            public void showError(String message) {
                view.showError(message);
            }
            @Override
            public void showSuccessful(String message) {
                view.showSuccessful(message);
            }
        });
    }


    public void onSendGmail(){
        SignInView view = view();
        BackgroundMail.newBuilder(view.getActivity())
                .withUsername(view.getContext().getString(R.string.user_name))
                .withPassword(view.getContext().getString(R.string.password))
                .withMailto(view.getContext().getString(R.string.tpcreative))
                .withType(BackgroundMail.TYPE_PLAIN)
                .withSubject("this is the subject")
                .withBody("this is the body")
                .withOnSuccessCallback(new BackgroundMail.OnSuccessCallback() {
                    @Override
                    public void onSuccess() {
                        //do some magic
                        Log.d(TAG,"Successful");
                    }
                })
                .withOnFailCallback(new BackgroundMail.OnFailCallback() {
                    @Override
                    public void onFail() {
                        //do some magic
                        Log.d(TAG,"Failed");
                    }
                })
                .send();
    }



}
