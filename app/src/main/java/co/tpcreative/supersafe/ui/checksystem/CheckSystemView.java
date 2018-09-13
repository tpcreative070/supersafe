package co.tpcreative.supersafe.ui.checksystem;
import android.app.Activity;

import co.tpcreative.supersafe.common.presenter.BaseView;

public interface CheckSystemView extends BaseView {
    void showError(String message);
    void showSuccessful(String user_id);
    void showUserExisting(String user_id,boolean isExisting);
    void onSignUpFailed(String message);
    void onSignInFailed(String message);
    Activity getActivity();
    void showSuccessfulVerificationCode();
    void showFailedVerificationCode();
    void sendEmailSuccessful();
    void onShowUserCloud(boolean error,String message);
}
