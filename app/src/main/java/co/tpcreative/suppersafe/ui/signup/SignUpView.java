package co.tpcreative.suppersafe.ui.signup;

import android.app.Activity;
import android.content.Context;

import co.tpcreative.suppersafe.common.presenter.BaseView;
import co.tpcreative.suppersafe.model.User;

public interface SignUpView extends BaseView {
    void showError(String message);
    void showSuccessful(String message, User user);
    Activity getActivity();
}
