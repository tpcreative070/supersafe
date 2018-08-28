package co.tpcreative.suppersafe.ui.checksystem;
import co.tpcreative.suppersafe.common.presenter.BaseView;

public interface CheckSystemView extends BaseView {
    void showError(String message);
    void showSuccessful(String user_id);
}
