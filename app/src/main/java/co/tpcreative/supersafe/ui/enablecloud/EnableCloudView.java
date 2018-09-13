package co.tpcreative.supersafe.ui.enablecloud;
import co.tpcreative.supersafe.common.presenter.BaseView;

public interface EnableCloudView extends BaseView {
    void showError(String message);
    void showSuccessful(String message);
}
