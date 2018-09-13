package co.tpcreative.supersafe.ui.signup;

import android.app.Activity;

import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.model.User;

public interface SignUpView extends BaseView {
    void showError(String message);
    void showSuccessful(String message, User user);
    Activity getActivity();
}
