package co.tpcreative.supersafe.common.presenter;

import android.content.Context;

import co.tpcreative.supersafe.model.EnumStatus;

public interface BaseView <T> {
    void startLoading();
    void stopLoading();
    void onError(String message, EnumStatus status);
    void onError(String message);
    void onSuccessful(String message);
    void onSuccessful(String message,EnumStatus status);
    Context getContext();
}
