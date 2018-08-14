package co.tpcreative.suppersafe.ui.signin;

import android.app.Activity;
import android.content.Context;

public interface SignInView {
    void showError(String message);
    void showSuccessful(String message);
    Context getContext();
    Activity getActivity();
}
