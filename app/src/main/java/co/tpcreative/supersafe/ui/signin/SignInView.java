package co.tpcreative.supersafe.ui.signin;

import android.app.Activity;

import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.model.User;

public interface SignInView extends BaseView{
    void showError(String message);
    void showSuccessful(String message, User user);
    Activity getActivity();
}
