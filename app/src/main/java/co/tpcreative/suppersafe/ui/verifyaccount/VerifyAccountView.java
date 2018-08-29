package co.tpcreative.suppersafe.ui.verifyaccount;
import android.app.Activity;

import co.tpcreative.suppersafe.common.presenter.BaseView;
import co.tpcreative.suppersafe.model.User;

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
