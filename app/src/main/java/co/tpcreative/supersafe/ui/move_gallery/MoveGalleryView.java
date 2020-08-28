package co.tpcreative.supersafe.ui.move_gallery;
import java.util.List;

import co.tpcreative.supersafe.common.entities.ItemEntity;
import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.model.ItemModel;

public interface MoveGalleryView extends BaseView<ItemEntity>{
    List<ItemModel>getListItems();
}
