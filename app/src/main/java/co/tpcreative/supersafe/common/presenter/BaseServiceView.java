package co.tpcreative.supersafe.common.presenter;
import android.content.Context;

import co.tpcreative.supersafe.model.EnumStatus;
public interface BaseServiceView <T> {
    void onError(String message, EnumStatus status);
    void onSuccessful(String message,EnumStatus status);
    Context getContext();
}
