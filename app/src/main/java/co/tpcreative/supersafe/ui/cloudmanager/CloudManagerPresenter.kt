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
        ServiceManager.getInstance()?.getMyService()?.getDriveAbout(object : BaseView<Long> {
            override fun onStartLoading(status: EnumStatus) {}
            override fun onStopLoading(status: EnumStatus) {}
            override fun onError(message: String?) {
                Utils.Log(TAG, message+"")
            }
            override fun onError(message: String?, status: EnumStatus?) {
                Utils.Log(TAG, message + "--" + status?.name)
                view.onError(message, status)
            }
            override fun onSuccessful(message: String?) {}
            override fun onSuccessful(message: String?, status: EnumStatus?) {
                onGetList()
                view.onSuccessful(message, status)
                Utils.Log(TAG, message + "--" + status?.name)
            }

            override fun onSuccessful(message: String?, status: EnumStatus?, `object`: Long?) {
                Utils.Log(TAG, message + "--" + status?.name)
            }
            override fun onSuccessful(message: String?, status: EnumStatus?, list: MutableList<Long>?) {}
            override fun getContext(): Context? {
                return null
            }
            override fun getActivity(): Activity? {
                return null
            }
        })
    }

    private fun onGetList() {
        Utils.Log(TAG, "onGetList" + "--" + EnumStatus.GET_LIST_FILES_IN_APP.name)
        val view: BaseView<Long>? = view()
        if (view?.getContext()?.let { NetworkUtil.pingIpAddress(it) }!!) {
            view.onError("No internet connection", EnumStatus.GET_LIST_FILES_IN_APP)
            return
        }
        if (ServiceManager.getInstance()?.getMyService() == null) {
            view.onError("Service is null", EnumStatus.GET_LIST_FILES_IN_APP)
            return
        }
        ServiceManager.getInstance()?.getMyService()?.onGetListFileInApp(object : BaseView<Int> {
            override fun onError(message: String?, status: EnumStatus?) {
                view.onError(message, status)
                Utils.Log(TAG, "error")
            }

            override fun onSuccessful(message: String?) {
                Utils.Log(TAG, "onSuccessful ??")
            }

            override fun onSuccessful(message: String?, status: EnumStatus?) {
                Utils.Log(TAG, "onSuccessful !! " + status?.name)
            }

            override fun onSuccessful(message: String?, status: EnumStatus?, `object`: Int?) {
                if (`object` == 0) {
                    val mUser: User? = Utils.getUserInfo()
                    if (mUser != null) {
                        Utils.Log(TAG, "onSuccessful 2")
                        if (mUser.driveAbout != null) {
                            Utils.Log(TAG, "onSuccessful 3")
                            mUser.driveAbout?.inAppUsed = 0
                            Utils.setUserPreShare(mUser)
                            view.onSuccessful("Successful", status)
                        }
                    }
                    Utils.Log(TAG, "onSuccessful 4")
                } else {
                    val mList: MutableList<ItemModel>? = SQLHelper.getListItemId(true, false)
                    var countSize: Long = 0
                    try {
                        if (mList != null) {
                            for (index in mList) {
                                countSize += index.size?.toLong()!!
                            }
                        }
                        val mUser: User? = Utils.getUserInfo()
                        if (mUser != null) {
                            if (mUser.driveAbout != null) {
                                mUser.driveAbout?.inAppUsed = countSize
                                Utils.setUserPreShare(mUser)
                                view.onSuccessful("Successful", status)
                            }
                        }
                        Utils.Log(TAG, "onSuccessful 5")
                    } catch (e: Exception) {
                        Utils.Log(TAG, "onSuccessful 6")
                        e.printStackTrace()
                    }
                }
            }

            override fun onSuccessful(message: String?, status: EnumStatus?, list: MutableList<Int>?) {}
            override fun onStartLoading(status: EnumStatus) {}
            override fun onStopLoading(status: EnumStatus) {}
            override fun onError(message: String?) {}
            override fun getContext(): Context? {
                return null
            }

            override fun getActivity(): Activity? {
                return null
            }
        })
    }

    companion object {
        private val TAG = CloudManagerPresenter::class.java.simpleName
    }

}