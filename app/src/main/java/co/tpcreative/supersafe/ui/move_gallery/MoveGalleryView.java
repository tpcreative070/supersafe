package co.tpcreative.supersafe.ui.move_gallery;

import java.util.List;

import co.tpcreative.supersafe.common.presenter.BaseView;
import co.tpcreative.supersafe.model.Items;

public interface MoveGalleryView extends BaseView<Items>{
    List<Items>getListItems();
}
