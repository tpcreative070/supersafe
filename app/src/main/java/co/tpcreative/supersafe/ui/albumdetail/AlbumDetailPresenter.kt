package co.tpcreative.supersafe.ui.albumdetail
import android.app.Activity
import android.os.Bundle
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.presenter.Presenter
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EnumFormatType
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.ItemModel
import co.tpcreative.supersafe.model.MainCategoryModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

class AlbumDetailPresenter : Presenter<BaseView<Int>>() {
    var mList: MutableList<ItemModel>?
    var mainCategories: MainCategoryModel? = null
    var videos = 0
    var photos = 0
    var audios = 0
    var others = 0
    var mListShare: MutableList<File>? = ArrayList()
    var status: EnumStatus? = EnumStatus.OTHER
    protected var mListHashExporting: MutableList<HashMap<Int, ItemModel>>?
    suspend fun getData(activity: Activity?) = withContext(Dispatchers.Main) {
        val view: BaseView<Int>? = view()
        mList?.clear()
        try {
            val bundle: Bundle? = activity?.intent?.extras
            mainCategories = bundle?.get(SuperSafeApplication.getInstance().getString(R.string.key_main_categories)) as MainCategoryModel
            if (mainCategories != null) {
                val data: MutableList<ItemModel>? = SQLHelper.getListItems(mainCategories?.categories_local_id, false, false, mainCategories!!.isFakePin)
                if (data != null) {
                    mList = data
                    onCalculate()
                }
                view?.onSuccessful("Successful", EnumStatus.RELOAD)
            } else {
                Utils.onWriteLog("Main categories is null", EnumStatus.WRITE_FILE)
            }
        } catch (e: Exception) {
            Utils.onWriteLog("" + e.message, EnumStatus.WRITE_FILE)
        }
    }

    private fun onCalculate() {
        photos = 0
        videos = 0
        audios = 0
        others = 0
        for (index in mList!!) {
            when (EnumFormatType.values()[index.formatType]) {
                EnumFormatType.IMAGE -> {
                    photos += 1
                }
                EnumFormatType.VIDEO -> {
                    videos += 1
                }
                EnumFormatType.AUDIO -> {
                    audios += 1
                }
                EnumFormatType.FILES -> {
                    others += 1
                }
            }
        }
    }

    fun getData(enumStatus: EnumStatus?) {
        val view: BaseView<Int>? = view()
        mList?.clear()
        try {
            if (mainCategories != null) {
                val data: MutableList<ItemModel>? = SQLHelper.getListItems(mainCategories?.categories_local_id, false, false, mainCategories!!.isFakePin)
                if (data != null) {
                    mList = data
                    onCalculate()
                }
                view?.onSuccessful("Successful", enumStatus)
            } else {
                Utils.onWriteLog("Main categories is null", EnumStatus.WRITE_FILE)
            }
        } catch (e: Exception) {
            Utils.onWriteLog("" + e.message, EnumStatus.WRITE_FILE)
        }
    }

    fun onDelete() = CoroutineScope(Dispatchers.Main).launch{
        val view: BaseView<Int>? = view()
        for (i in mList?.indices!!) {
            if (mList!![i].isChecked()!!) {
                Utils.Log(TAG, "Delete position at $i")
                mList?.get(i)?.isDeleteLocal = true
                mList?.get(i)?.let { SQLHelper.updatedItem(it) }
                view?.onSuccessful("Successful", EnumStatus.DELETE, i)
            }
        }
        view?.onSuccessful("Successful", EnumStatus.DELETE)
        getData(EnumStatus.REFRESH)
    }

    companion object {
        private val TAG = AlbumDetailPresenter::class.java.simpleName
    }

    init {
        mList = ArrayList<ItemModel>()
        mListShare = ArrayList()
        mListHashExporting = ArrayList<HashMap<Int, ItemModel>>()
    }
}