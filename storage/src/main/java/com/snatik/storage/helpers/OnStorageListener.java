package com.snatik.storage.helpers;

public interface OnStorageListener {
    void onSuccessful();
    void onSuccessful(String path);
    void onFailed();
}
