package co.tpcreative.suppersafe.ui.enablecloud;
import co.tpcreative.suppersafe.common.presenter.BaseView;

public interface EnableCloudView extends BaseView {
    void showError(String message);
    void showSuccessful(String message);
}
