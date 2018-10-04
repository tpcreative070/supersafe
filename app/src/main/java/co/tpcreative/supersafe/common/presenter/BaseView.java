package co.tpcreative.supersafe.common.presenter;

import android.app.Activity;
import android.content.Context;

import java.util.List;

import co.tpcreative.supersafe.model.EnumStatus;

public interface BaseView <T> {
    void onStartLoading(EnumStatus status);
    void onStopLoading(EnumStatus status);
    void onError(String message, EnumStatus status);
    void onError(String message);
    void onSuccessful(String message);
    void onSuccessful(String message,EnumStatus status);
    void onSuccessful(String message,EnumStatus status,T object);
    void onSuccessful(String message,EnumStatus status,List<T> list);
    Context getContext();
    Activity getActivity();
}
