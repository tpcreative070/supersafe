package co.tpcreative.supersafe.ui.photosslideshow
import android.app.Activity
import android.os.Bundle
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.presenter.Presenter
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.*
import com.google.gson.Gson
import com.snatik.storage.Storage
import java.io.File
import java.util.*

class PhotoSlideShowPresenter : Presenter<BaseView<EmptyModel>>() {
    private val TAG = PhotoSlideShowPresenter::class.java.simpleName
    protected var items: ItemModel? = null
    var mList: MutableList<ItemModel>?
    var mListShare: MutableList<File>? = ArrayList()
    var status: EnumStatus = EnumStatus.OTHER
    protected var storage: Storage? = null
    var mainCategories: MainCategoryModel? = null
    fun getIntent(context: Activity?) {
        storage = Storage(context)
        mList?.clear()
        try {
            val bundle: Bundle? = context?.getIntent()?.getExtras()
            items = bundle?.get(context?.getString(R.string.key_items)) as ItemModel
            val list: MutableList<ItemModel?> = bundle.get(context.getString(R.string.key_list_items)) as ArrayList<ItemModel?>
            mainCategories = bundle.get(SuperSafeApplication.getInstance().getString(R.string.key_main_categories)) as MainCategoryModel
            for (index in list) {
                if (index?.items_id != items?.items_id) {
                    val formatType = EnumFormatType.values()[index?.formatType!!]
                    if (formatType != EnumFormatType.FILES) {
                        mList?.add(index)
                    }
                }
            }
            mList?.add(0, items!!)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Utils.Log(TAG, Gson().toJson(items))
    }

    fun onDelete(position: Int) {
        try {
            val view: BaseView<EmptyModel>? = view()
            val items: ItemModel? = mList?.get(position)
            val mItem: ItemModel? = SQLHelper.getItemId(items?.items_id, items?.isFakePin!!)
            if (mItem != null) {
                if (mItem.isFakePin) {
                    storage?.deleteDirectory(SuperSafeApplication.getInstance().getSupersafePrivate() + mItem.items_id)
                    SQLHelper.deleteItem(mItem)
                } else {
                    mItem.isDeleteLocal = true
                    SQLHelper.updatedItem(mItem)
                }
                mList?.removeAt(position)
                view?.onSuccessful("Delete Successful", EnumStatus.DELETE)
            } else {
                Utils.Log(TAG, "Not found")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    init {
        mList = ArrayList<ItemModel>()
    }
}