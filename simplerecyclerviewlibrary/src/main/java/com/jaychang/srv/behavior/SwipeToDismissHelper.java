package com.jaychang.srv.behavior;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

public class SwipeToDismissHelper extends ItemTouchHelper
  implements OnStartDragListener {

  private SwipeToDismissHelper(Callback callback) {
    super(callback);
  }

  public static SwipeToDismissHelper create(OnItemDismissListener callback,
                                            SwipeToDismissOptions options) {
    SwipeToDismissItemCallback simpleCallback = new SwipeToDismissItemCallback(callback, options);
    return new SwipeToDismissHelper(simpleCallback);
  }

  @Override
  public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
    startDrag(viewHolder);
  }

}
