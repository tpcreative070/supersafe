package com.jaychang.srv.behavior;

import android.graphics.Canvas;
import android.support.v7.widget.RecyclerView;

public interface OnSwipeListener {
  void onSwiping(Canvas canvas, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, boolean isCurrentlyActive);
  void onSwipeSettled(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder);
}
