package co.tpcreative.supersafe.ui.verifyaccount;
import android.app.Activity;

import co.tpcreative.supersafe.common.presenter.BaseView;

public interface VerifyAccountView extends BaseView{
    void showError(String message);
    void showSuccessful(String message);
    void showSuccessfulVerificationCode();
    void onChangeEmailSuccessful();
    void showUserExisting(String user_id,boolean isExisting);
    void onSignUpFailed(String message);
    void onSignInFailed(String message);
    void onLoading();
    void onFinishing();
    Activity getActivity();
}
