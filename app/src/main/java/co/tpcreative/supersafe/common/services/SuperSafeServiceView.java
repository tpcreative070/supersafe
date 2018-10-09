package co.tpcreative.supersafe.common.services;

import java.util.List;

import co.tpcreative.supersafe.common.response.DriveResponse;
import co.tpcreative.supersafe.model.EnumStatus;
import co.tpcreative.supersafe.model.Items;

public interface SuperSafeServiceView {
    void onError(String message, EnumStatus status);
    void onSuccessful(String message);
    void onSuccessful(String message,EnumStatus status);
    void onSuccessful(List<DriveResponse> lists);
    void onStartLoading();
    void onStopLoading();


//    void onStartLoading(EnumStatus status);
//    void onStopLoading(EnumStatus status);
//    void onError(String message);
//    void onError(String message, EnumStatus status);
//    void onSuccessful(String message);
//    void onSuccessful(String message,EnumStatus status);
//    void onSuccessful(String message,EnumStatus status,T object);
//    void onSuccessful(String message,EnumStatus status,List<T> list);

}
