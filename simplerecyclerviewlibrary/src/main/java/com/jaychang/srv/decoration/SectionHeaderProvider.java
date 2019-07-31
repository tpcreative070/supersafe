package com.jaychang.srv.decoration;
import android.view.View;

import androidx.annotation.NonNull;

public interface SectionHeaderProvider<T> {
  @NonNull
  View getSectionHeaderView(@NonNull T item, int position);
  boolean isSameSection(@NonNull T item, @NonNull T nextItem);
  boolean isSticky();
  int getSectionHeaderMarginTop(@NonNull T item, int position);
}
