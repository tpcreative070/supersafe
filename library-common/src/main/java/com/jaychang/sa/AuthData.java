package com.jaychang.sa;

import java.util.ArrayList;
import java.util.List;

public class AuthData {
  private List<String> scopes;
  private AuthCallback callback;

  public AuthData(List<String> scopes, AuthCallback callback) {
    this.scopes = new ArrayList<>(scopes);
    this.callback = callback;
  }

  public List<String> getScopes() {
    return scopes;
  }

  public AuthCallback getCallback() {
    return callback;
  }

  public void clearCallback() {
    callback = null;
  }
}
