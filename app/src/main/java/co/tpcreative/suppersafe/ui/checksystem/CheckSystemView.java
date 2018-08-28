package co.tpcreative.suppersafe.ui.checksystem;
import android.app.Activity;

import co.tpcreative.suppersafe.common.presenter.BaseView;

public interface CheckSystemView extends BaseView {
    void showError(String message);
    void showSuccessful(String user_id);
    void showUserExisting(String user_id,boolean isExisting);
    void onSignUpFailed(String message);
    Activity getActivity();
    void showSuccessfulVerificationCode();
    void showFailedVerificationCode();
    void sendEmailSuccessful();
    void onShowUserCloud(boolean error,String message);
}
