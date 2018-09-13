package co.tpcreative.supersafe.ui.verify;
import android.app.Activity;

import co.tpcreative.supersafe.common.presenter.BaseView;

public interface VerifyView extends BaseView {
    void showError(String message);
    void showSuccessful(String message);
    void onLoading();
    void onFinishing();
    Activity getActivity();
}
