package co.tpcreative.supersafe.ui.albumcover
import android.app.Activity
import android.os.Bundle
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.presenter.Presenter
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.*
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class AlbumCoverPresenter : Presenter<BaseView<EmptyModel>>() {
    var mMainCategories: MainCategoryModel?
    var mList: MutableList<ItemModel>?
    var mListMainCategories: MutableList<MainCategoryModel>? = null
    var previousPosition : Int? = null
    var defaultPreviousPosition : Int? = null
    fun getData(activity: Activity?) {
        val view: BaseView<EmptyModel>? = view()
        val bundle: Bundle? = activity?.intent?.extras
        try {
            val mainCategories: MainCategoryModel? = bundle?.get(MainCategoryModel::class.java.simpleName) as MainCategoryModel
            if (mainCategories != null) {
                mMainCategories = mainCategories
                view?.onSuccessful("Successful", EnumStatus.RELOAD)
            }
            Utils.Log(TAG, Gson().toJson(mMainCategories))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getFetchCustomData(position : Int,  onDone : (Int) ->Unit){
        previousPosition?.let { mResultPosition ->
            mList?.get(position)?.isChecked = !(mList?.get(position)?.isChecked!!)
            mList?.get(mResultPosition)?.isChecked = !(mList?.get(mResultPosition)?.isChecked!!)
        } ?: run {
            mList?.get(position)?.isChecked = !(mList?.get(position)?.isChecked!!)
        }
        onDone(position)
    }

    fun getFetchDefaultData(position : Int , onDone : (Int) ->Unit){
        defaultPreviousPosition?.let {mResultPosition ->
            mListMainCategories?.get(position)?.isChecked = !(mListMainCategories?.get(position)?.isChecked!!)
            mListMainCategories?.get(mResultPosition)?.isChecked = !(mListMainCategories?.get(mResultPosition)?.isChecked!!)
        } ?: run {
            mListMainCategories?.get(position)?.isChecked = !(mListMainCategories?.get(position)?.isChecked!!)
        }
        onDone(position)
    }

    fun getData(){
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
                        previousPosition = i
                        break
                    }
                }
            }
        }
        val oldMainCategories: MainCategoryModel? = SQLHelper.getCategoriesPosition(mMainCategories?.mainCategories_Local_Id)
        mListMainCategories = SQLHelper.getCategoriesDefault()
        if (oldMainCategories != null) {
            for (i in mListMainCategories!!.indices) {
                if (oldMainCategories.mainCategories_Local_Id == mListMainCategories?.get(i)?.mainCategories_Local_Id) {
                    mListMainCategories?.get(i)?.isChecked = true
                    defaultPreviousPosition = i
                    break
                }
            }
        }
        view?.onSuccessful("Successful", EnumStatus.GET_LIST_FILE)
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