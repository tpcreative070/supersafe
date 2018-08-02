package co.tpcreative.keepsafety.common.presenter;

import android.content.Context;

import java.util.List;

public interface BaseView <T> {
    void startLoading();
    void stopLoading();
    Context getContext();
}
