package co.tpcreative.suppersafe.ui.verify;
import android.app.Activity;

import co.tpcreative.suppersafe.common.presenter.BaseView;
import co.tpcreative.suppersafe.model.User;

public interface VerifyView extends BaseView {
    void showError(String message);
    void showSuccessful(String message);
    void onLoading();
    void onFinishing();
    Activity getActivity();
}
