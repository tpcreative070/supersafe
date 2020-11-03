package co.tpcreative.supersafe.ui.player
import android.app.Activity
import android.os.Bundle
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.presenter.Presenter
import co.tpcreative.supersafe.model.EmptyModel
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.ItemModel
import co.tpcreative.supersafe.model.MainCategoryModel
import com.google.android.exoplayer2.source.MediaSource
import java.util.*

class PlayerPresenter : Presenter<BaseView<EmptyModel>>() {
    var mItems: ItemModel? = null
    protected var mainCategories: MainCategoryModel? = null
    var mList: MutableList<ItemModel>? = ArrayList<ItemModel>()
    var mListSource: MutableList<MediaSource>? = ArrayList<MediaSource>()
    fun onGetIntent(activity: Activity?) {
        val view: BaseView<EmptyModel>? = view()
        val bundle: Bundle? = activity?.intent?.extras
        try {
            val items : ItemModel? = bundle?.get(activity.getString(R.string.key_items)) as ItemModel
            mainCategories = bundle.get(activity.getString(R.string.key_main_categories)) as MainCategoryModel
            if (items != null) {
                mItems = items
                if (mainCategories != null) {
                    val list: MutableList<ItemModel>? = SQLHelper.getListItems(mainCategories?.categories_local_id, items.formatType, false, mainCategories!!.isFakePin)
                    if (list != null) {
                        mList?.clear()
                        for (index in list) {
                            if (index.items_id != items.items_id) {
                                mList?.add(index)
                            }
                        }
                        mItems?.isChecked = true
                        mList?.add(0, mItems!!)
                    }
                }
                view?.onSuccessful("Play", EnumStatus.PLAY)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}