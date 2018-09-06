package co.tpcreative.suppersafe.common.services;

import co.tpcreative.suppersafe.model.EnumStatus;

public interface SupperSafeServiceView {
    void onError(String message, EnumStatus status);
    void onSuccessful(String message);
    void onNetworkConnectionChanged(boolean isConnect);
    void onStart();
    void startLoading();
    void stopLoading();
}
