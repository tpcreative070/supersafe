package co.tpcreative.suppersafe.common.services;

public interface SupperSafeServiceView {
    void onError(String message);
    void onSuccessful(String message);
    void onStart();
    void startLoading();
    void stopLoading();
}
