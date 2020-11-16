package co.tpcreative.supersafe.ui.cloudmanager
import android.app.Activity
import android.content.Context
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.presenter.Presenter
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.common.utilimport.NetworkUtil
import co.tpcreative.supersafe.model.EnumFormatType
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.ItemModel
import co.tpcreative.supersafe.model.User
import com.google.gson.Gson
import com.snatik.storage.Storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CloudManagerPresenter : Presenter<BaseView<Long>>() {
    var sizeFile: Long = 0
    var sizeSaverFiles: Long = 0
    protected var storage: Storage? = Storage(SuperSafeApplication.getInstance())
    fun onGetSaveData() {
        sizeSaverFiles = 0
        val view: BaseView<Long>? = view()
        val mList: MutableList<ItemModel>? = SQLHelper.getListSyncData(true, false)
        if (mList != null) {
            for (index in mList) {
                when (EnumFormatType.values()[index.formatType]) {
                    EnumFormatType.IMAGE -> {
                        sizeSaverFiles += index.size?.toLong()!!
                    }
                    else -> Utils.Log(TAG,"Nothing")
                }
            }
        }
        view?.onSuccessful("Successful", EnumStatus.SAVER)
    }

    fun onDisableSaverSpace(enumStatus: EnumStatus?) {
        sizeFile = 0
        val view: BaseView<Long>? = view()
        val mList: MutableList<ItemModel>? = SQLHelper.getListSyncData(true, true, false)
        if (mList != null) {
            for (i in mList.indices) {
                when (EnumFormatType.values()[mList[i].formatType]) {
                    EnumFormatType.IMAGE -> {
                        when (enumStatus) {
                            EnumStatus.GET_LIST_FILE -> {
                                sizeFile += mList[i].size?.toLong()!!
                            }
                            EnumStatus.DOWNLOAD -> {
                                sizeFile = 0
                                mList[i].isSaver = false
                                SQLHelper.updatedItem(mList[i])
                            }
                            else -> Utils.Log(TAG,"Nothing")
                        }
                    }
                    else -> Utils.Log(TAG,"Nothing")
                }
            }
        }
        view?.onSuccessful("Successful", enumStatus)
        Utils.Log(TAG, Gson().toJson(mList))
    }

    fun onEnableSaverSpace() {
        val mList: MutableList<ItemModel>? = SQLHelper.getListSyncData(true, false, false)
        if (mList != null && mList.size > 0) {
            for (i in mList.indices) {
                when (EnumFormatType.values()[mList[i].formatType]) {
                    EnumFormatType.IMAGE -> {
                        mList[i].isSaver = true
                        SQLHelper.updatedItem(mList[i])
                        //storage.deleteFile(mList.get(i).originalPath);
                        Utils.Log(TAG, "Continue updating...")
                    }
                    else -> Utils.Log(TAG,"Nothing")
                }
            }
        } else {
            Utils.Log(TAG, "Already saver files")
        }
        onGetSaveData()
    }

    fun onGetDriveAbout() {
        Utils.Log(TAG, "onGetDriveAbout" + "--" + EnumStatus.GET_DRIVE_ABOUT.name)
        val view: BaseView<Long>? = view()
        if (view?.getContext()?.let { NetworkUtil.pingIpAddress(it) }!!) {
            view.onError("No internet connection", EnumStatus.GET_DRIVE_ABOUT)
            return
        }
        if (ServiceManager.getInstance()?.getMyService() == null) {
            view.onError("Service is null", EnumStatus.GET_DRIVE_ABOUT)
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            ServiceManager.getInstance()?.getInAppList()
            view.onSuccessful("Success",EnumStatus.GET_LIST_FILES_IN_APP)
        }
    }

    companion object {
        private val TAG = CloudManagerPresenter::class.java.simpleName
    }

}