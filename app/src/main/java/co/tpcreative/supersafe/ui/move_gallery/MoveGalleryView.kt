package co.tpcreative.supersafe.ui.move_gallery

import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.model.ItemModel

interface MoveGalleryView : BaseView<ItemModel> {
    open fun getListItems(): MutableList<ItemModel>?
}