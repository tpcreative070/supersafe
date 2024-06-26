package com.jaychang.sa;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public abstract class SimpleAuthActivity extends AppCompatActivity {

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    //supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
    super.onCreate(savedInstanceState);
  }

  protected void handCancel() {
    getAuthData().getCallback().onCancel();
    finish();
  }

  protected void handleError(Throwable error) {
    getAuthData().getCallback().onError(error);
    finish();
  }

  protected void handleSuccess(SocialUser user) {
    getAuthData().getCallback().onSuccess(user);
    finish();
  }

  protected abstract AuthData getAuthData();

  @Override
  protected void onDestroy() {
    super.onDestroy();
    getAuthData().clearCallback();
  }

}
