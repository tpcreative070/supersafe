package co.tpcreative.supersafe.common.services;

import java.util.List;

import co.tpcreative.supersafe.common.response.DriveResponse;
import co.tpcreative.supersafe.model.EnumStatus;

public interface SuperSafeServiceView {
    void onError(String message, EnumStatus status);
    void onSuccessful(String message);
    void onSuccessful(List<DriveResponse> lists);
    void onNetworkConnectionChanged(boolean isConnect);
    void onStart();
    void startLoading();
    void stopLoading();
}
