package co.tpcreative.supersafe.common.services;

import java.util.List;

import co.tpcreative.supersafe.common.response.DriveResponse;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.Items;

public interface SuperSafeServiceView {
    void onError(String message, EnumStatus status);
    void onSuccessful(String message);
    void onSuccessful(String message,EnumStatus status);
    void onSuccessfulOnCheck(List<Items> lists);
    void onSuccessful(List<DriveResponse> lists);
    void onNetworkConnectionChanged(boolean isConnect);
    void onActionScreenOff();
    void onStart();
    void startLoading();
    void stopLoading();
}
