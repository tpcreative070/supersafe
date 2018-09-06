package co.tpcreative.suppersafe.common.services;

import java.util.List;

import co.tpcreative.suppersafe.common.response.DriveResponse;
import co.tpcreative.suppersafe.model.EnumStatus;
import co.tpcreative.suppersafe.model.Items;

public interface SupperSafeServiceView {
    void onError(String message, EnumStatus status);
    void onSuccessful(String message);
    void onSuccessful(List<DriveResponse> lists);
    void onNetworkConnectionChanged(boolean isConnect);
    void onStart();
    void startLoading();
    void stopLoading();
}
