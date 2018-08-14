package co.tpcreative.suppersafe.ui.signup;
import co.tpcreative.suppersafe.common.controller.ManagerNetwork;
import co.tpcreative.suppersafe.common.presenter.Presenter;

public class SignUpPresenter extends Presenter<SignUpView>{

    public SignUpPresenter(){

    }

    public void onSignUp(String email,String name){
        SignUpView view = view();
        ManagerNetwork.getInstance().onSignUp(email, name, new ManagerNetwork.ManagerNetworkListener() {
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

}
