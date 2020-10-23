package co.tpcreative.supersafe.ui.albumcover
import android.app.Activity
import android.os.Bundle
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.presenter.Presenter
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.*
import com.google.gson.Gson
import java.util.*

class AlbumCoverPresenter : Presenter<BaseView<EmptyModel>>() {
    var mMainCategories: MainCategoryModel?
    var mList: MutableList<ItemModel>?
    var mListMainCategories: MutableList<MainCategoryModel>? = null
    fun getData(activity: Activity?) {
        val view: BaseView<EmptyModel>? = view()
        val bundle: Bundle? = activity?.getIntent()?.getExtras()
        try {
            val mainCategories: MainCategoryModel? = bundle?.get(MainCategoryModel::class.java.getSimpleName()) as MainCategoryModel
            if (mainCategories != null) {
                mMainCategories = mainCategories
                view?.onSuccessful("Successful", EnumStatus.RELOAD)
            }
            Utils.Log(TAG, Gson().toJson(mMainCategories))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getData(): MutableList<ItemModel>? {
        val view: BaseView<EmptyModel>? = view()
        mList?.clear()
        val data: MutableList<ItemModel>? = SQLHelper.getListItems(mMainCategories?.categories_local_id, EnumFormatType.IMAGE.ordinal, false, mMainCategories!!.isFakePin)
        if (data != null) {
            mList = data
            val oldItem: ItemModel? = SQLHelper.getItemId(mMainCategories?.items_id)
            if (oldItem != null) {
                for (i in mList?.indices!!) {
                    if (oldItem.items_id == mList?.get(i)?.items_id) {
                        mList?.get(i)?.isChecked = true
                        Utils.Log(TAG, "Checked item $i")
                    } else {
                        mList?.get(i)?.isChecked = false
                    }
                }
            } else {
                for (i in mList?.indices!!) {
                    mList?.get(i)?.isChecked = false
                }
            }
        }
        Utils.Log(TAG, "Count list " + mList?.size)

        //Utils.Log(TAG,"Categories "+new Gson().toJson(mMainCategories));
        val oldMainCategories: MainCategoryModel? = SQLHelper.getCategoriesPosition(mMainCategories?.mainCategories_Local_Id)
        mListMainCategories = SQLHelper.getCategoriesDefault()
        if (oldMainCategories != null) {
            Utils.Log(TAG, "Main categories " + oldMainCategories.mainCategories_Local_Id)
            for (i in mListMainCategories!!.indices) {
                if (oldMainCategories.mainCategories_Local_Id == mListMainCategories?.get(i)?.mainCategories_Local_Id) {
                    mListMainCategories?.get(i)?.isChecked = true
                    Utils.Log(TAG, "Checked categories $i")
                } else {
                    mListMainCategories?.get(i)?.isChecked = false
                }
            }
        } else {
            Utils.Log(TAG, "Main categories is null")
            for (i in mListMainCategories?.indices!!) {
                mListMainCategories?.get(i)?.isChecked = false
            }
        }
        Utils.Log(TAG, "List :" + mList?.size)
        view?.onSuccessful("Successful", EnumStatus.GET_LIST_FILE)
        return mList
    }

    private fun getString(res: Int): String? {
        val view: BaseView<EmptyModel>? = view()
        return view?.getContext()?.getString(res)
    }

    companion object {
        private val TAG = AlbumCoverPresenter::class.java.simpleName
    }

    init {
        mMainCategories = MainCategoryModel()
        mList = ArrayList<ItemModel>()
    }
}