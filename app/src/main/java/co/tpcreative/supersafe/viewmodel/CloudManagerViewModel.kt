package co.tpcreative.supersafe.viewmodel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.Navigator
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.extension.deleteFile
import co.tpcreative.supersafe.common.extension.getString
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.network.Resource
import co.tpcreative.supersafe.common.network.Status
import co.tpcreative.supersafe.common.util.ConvertUtils
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EnumFormatType
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.ItemModel
import kotlinx.coroutines.Dispatchers

class CloudManagerViewModel : BaseViewModel<ItemModel>(){
    val TAG = this::class.java.simpleName
    override val isLoading: MutableLiveData<Boolean>
        get() = super.isLoading

    val superSafeSpace : MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val otherSpace : MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val freeSpace : MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val leftItems : MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val uploadedItems : MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val driveAccount : MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val deviceSaving : MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun getDriveAbout()  = liveData(Dispatchers.Main){
        val mResult =  ServiceManager.getInstance()?.getDriveAbout()
        when(mResult?.status){
            Status.SUCCESS -> {
                val mResultList = ServiceManager.getInstance()?.getInAppList()
                when(mResultList?.status){
                    Status.SUCCESS -> {
                        Utils.Log(TAG, "Fetch data completely")
                        emit(Resource.success(true))
                    }
                    else -> {
                        Utils.Log(TAG,mResultList?.message)
                        emit(Resource.error(mResultList?.code ?: Utils.CODE_EXCEPTION,mResultList?.message ?:"", null))
                    }
                }
            }else -> {
            Utils.Log(TAG,mResult?.message)
            emit(Resource.error(mResult?.code ?: Utils.CODE_EXCEPTION,mResult?.message ?:"", null))
            }
        }
    }

    fun getSaveData() = liveData(Dispatchers.Main){
        var sizeSaverFiles : Long = 0
        val mList: MutableList<ItemModel>? = SQLHelper.getListSyncData(isSyncCloud = true, isFakePin = false)
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

        val mUser = Utils.getUserInfo()
        driveAccount.postValue(mUser?.cloud_id ?: "")
        superSafeSpace.postValue( ConvertUtils.byte2FitMemorySize(mUser?.driveAbout?.inAppUsed ?: 0))
        otherSpace.postValue(ConvertUtils.byte2FitMemorySize(mUser?.driveAbout?.storageQuota?.usage ?: 0))
        val result = (mUser?.driveAbout?.storageQuota?.limit ?: 0) - (mUser?.driveAbout?.storageQuota?.usage ?: 0)
        val freeSpaceValue: String? = ConvertUtils.byte2FitMemorySize(result)
        freeSpace.postValue(freeSpaceValue)

        val lefFiles: String = kotlin.String.format(getString(R.string.left), "" + mUser?.syncData?.left)
        leftItems.postValue(lefFiles)
        val uploadedFiles: String = kotlin.String.format(getString(R.string.uploaded), "" + (Navigator.LIMIT_UPLOAD - (mUser?.syncData?.left ?: Navigator.LIMIT_UPLOAD)))
        uploadedItems.postValue(uploadedFiles)
        if (Utils.getSaverSpace()) {
            deviceSaving.postValue(ConvertUtils.byte2FitMemorySize(sizeSaverFiles))
        } else {
            deviceSaving.postValue(ConvertUtils.byte2FitMemorySize(0))
        }
        emit(true)
    }

    fun enableSaverSpace()  = liveData(Dispatchers.Main){
        val mList: MutableList<ItemModel>? = SQLHelper.getListSyncData(isSyncCloud = true, isSaver = false, isFakePin = false)
        if (mList != null && mList.size > 0) {
            for (i in mList.indices) {
                when (EnumFormatType.values()[mList[i].formatType]) {
                    EnumFormatType.IMAGE -> {
                        mList[i].isSaver = true
                        SQLHelper.updatedItem(mList[i])
                        Utils.Log(TAG, "Continue updating...")
                    }
                    else -> Utils.Log(TAG,"Nothing")
                }
            }
        } else {
            Utils.Log(TAG, "Already saver files")
        }
        emit(true)
    }

    fun disableSaverSpace(enumStatus: EnumStatus?) = liveData(Dispatchers.Main){
        var sizeFile : Long = 0
        val mList: MutableList<ItemModel>? = SQLHelper.getListSyncData(isSyncCloud = true, isSaver = true, isFakePin = false)
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
        emit(sizeFile)
    }

    fun destroySaver(){
        val mList: MutableList<ItemModel>? = SQLHelper.getListSyncData(isSyncCloud = true, isSaver = true, isFakePin = false)
        mList?.let {
            for (index in it) {
                when (EnumFormatType.values()[index.formatType]) {
                    EnumFormatType.IMAGE -> {
                        index.getOriginal().deleteFile()
                    }
                    else -> Utils.Log(TAG,"Nothing")
                }
            }
        }
    }
}