package co.tpcreative.supersafe.ui.privates
import android.app.Activity
import android.content.Context
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.controller.SingletonManagerProcessing
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.presenter.Presenter
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.*
import com.google.android.gms.auth.GoogleAuthException
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.gson.Gson
import com.snatik.storage.Storage
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.io.IOException
import java.util.*
import java.util.concurrent.Callable

class PrivatePresenter : Presenter<BaseView<EmptyModel>>() {
    var mList: MutableList<MainCategoryModel>?
    protected var storage: Storage? = null
    fun getData() {
        val view: BaseView<EmptyModel>? = view()
        mList = SQLHelper.getList()
        storage = Storage(SuperSafeApplication.getInstance())
        view?.onSuccessful("Successful", EnumStatus.RELOAD)
    }

    fun onDeleteAlbum(position: Int) {
        try {
            val main: MainCategoryModel? = mList?.get(position)
            main?.let {
                val mListItems: MutableList<ItemModel>? = SQLHelper.getListItems(it.categories_local_id, false)
                mListItems?.let {item ->
                    for (index in item) {
                        index.isDeleteLocal = true
                        SQLHelper.updatedItem(index)
                    }
                    it.isDelete = true
                    SQLHelper.updateCategory(it)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            getData()
            ServiceManager.getInstance()?.onPreparingSyncData()
        }
    }

    fun onEmptyTrash() {
        subscriptions?.add(Observable.fromCallable(Callable {
            try {
                val mList: MutableList<ItemModel>? = SQLHelper.getDeleteLocalListItems(true, EnumDelete.NONE.ordinal, false)
                mList?.let {
                    for (index in it) {
                        val formatTypeFile = EnumFormatType.values()[index.formatType]
                        if (formatTypeFile == EnumFormatType.AUDIO && index.global_original_id == null) {
                            SQLHelper.deleteItem(index)
                        } else if (formatTypeFile == EnumFormatType.FILES && index.global_original_id == null) {
                            SQLHelper.deleteItem(index)
                        } else if ((index.global_original_id == null) and (index.global_thumbnail_id == null)) {
                            SQLHelper.deleteItem(index)
                        } else {
                            index.deleteAction = EnumDelete.DELETE_WAITING.ordinal
                            SQLHelper.updatedItem(index)
                            Utils.Log(TAG, "ServiceManager waiting for delete")
                        }
                        storage?.deleteDirectory(SuperSafeApplication.getInstance().getSuperSafePrivate() + index.items_id)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
             ""
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe { response: String? ->
                    getData()
                    ServiceManager.getInstance()?.onPreparingSyncData()
                })
        Utils.Log(TAG,"empty data")
    }

    companion object {
        private val TAG = PrivatePresenter::class.java.simpleName
    }

    init {
        mList = ArrayList<MainCategoryModel>()
    }
}