package co.tpcreative.supersafe.common.presenter;

import android.content.Context;

public interface BaseView <T> {
    void startLoading();
    void stopLoading();
    Context getContext();
}
