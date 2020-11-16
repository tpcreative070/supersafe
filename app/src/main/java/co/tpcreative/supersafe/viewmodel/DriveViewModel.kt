package co.tpcreative.supersafe.viewmodel
import androidx.lifecycle.ViewModel
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.api.request.DownloadFileRequest
import co.tpcreative.supersafe.common.api.requester.DriveService
import co.tpcreative.supersafe.common.api.requester.ItemService
import co.tpcreative.supersafe.common.extension.toJson
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.network.Resource
import co.tpcreative.supersafe.common.network.Status
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.services.download.ProgressResponseBody
import co.tpcreative.supersafe.common.services.upload.ProgressRequestBody
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.*
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.HashMap

class DriveViewModel(private val driveService: DriveService, itemService: ItemService) : ViewModel(){
    private val itemViewModel = ItemViewModel(itemService)
    val TAG = this::class.java.simpleName
    suspend fun downLoadData(isDownloadToExport : Boolean,globalList : MutableList<ItemModel>?) : Resource<Boolean> {
        return withContext(Dispatchers.IO){
            try {
                val mergeList: MutableList<ItemModel>?
                mergeList = if (isDownloadToExport){
                    Utils.getMergedOriginalThumbnailList(false, globalList!!)
                }else{
                    val mListLocal: MutableList<ItemModel>? = SQLHelper.getItemListDownload()
                    Utils.clearListFromDuplicate(globalList!!, mListLocal!!)
                }
                Utils.Log(TAG,"Total download ${mergeList?.size}")
                for (index in mergeList!!){
                    val mResultDownloaded = driveService.downloadFile(index,onGetContentOfDownload(index)!!,mProgressDownloading)
                    when(mResultDownloaded.status){
                        Status.SUCCESS -> {
                            Utils.Log(TAG,mResultDownloaded.data)
                            updatedDateAfterDownloadedFile(isDownloadToExport,index)
                        }
                        else -> {
                            if (mResultDownloaded.code==EnumResponseCode.NOT_FOUND.code){
                                val mResultDeleteItem = itemViewModel.deleteItemSystem(index)
                                when(mResultDeleteItem.status){
                                    Status.SUCCESS -> Utils.Log(TAG,mResultDeleteItem.data?.toJson())
                                    else -> Utils.Log(TAG,mResultDeleteItem.message)
                                }
                            }
                            Utils.Log(TAG,mResultDownloaded.message)
                        }
                    }
                }
                Resource.success(true)
            }catch (e: Exception){
                e.printStackTrace()
                Resource.error(Utils.CODE_EXCEPTION, e.message ?:"",null)
            }
        }
    }

    suspend fun uploadData() : Resource<Boolean>{
        return withContext(Dispatchers.IO){
            try {
                val mResult: MutableList<ItemModel>? = SQLHelper.getItemListUpload()
                val listUpload: MutableList<ItemModel>? = Utils.getMergedOriginalThumbnailList(true, mResult!!)
                for (index in listUpload!!){
                    val mResultUpload = driveService.uploadFile(index,onGetContentOfUpload(index),mProgressUploading,onGetFilePath(index))
                    when(mResultUpload.status){
                        Status.SUCCESS ->{
                            val mResultInserted = mResultUpload.data?.id?.let { itemViewModel.insertItemToSystem(index, it) }
                            when(mResultInserted?.status){
                                Status.SUCCESS -> Utils.Log(TAG,mResultInserted.data?.responseMessage)
                                else -> Utils.Log(TAG,mResultInserted?.message)
                            }
                        }
                        else -> {
                            Utils.Log(TAG,mResultUpload.message)
                        }
                    }
                }
                Resource.success(true)
            }catch (e : Exception){
                e.printStackTrace()
                Resource.error(Utils.CODE_EXCEPTION, e.message ?:"",null)
            }
        }
    }

    suspend fun deleteItemFromCloud() : Resource<Boolean>{
        return withContext(Dispatchers.IO){
            try {
                val mResult: MutableList<ItemModel>? = SQLHelper.getDeleteItemRequest()
                /*Merged original and thumbnail*/
                val listRequestDelete: MutableList<ItemModel>? = Utils.getMergedOriginalThumbnailList(false, mResult!!)
                for (index in listRequestDelete!!){
                   val mResultDeleteCloud =  driveService.deleteCloudItemCor(index.global_id!!)
                    when(mResultDeleteCloud.status){
                        Status.SUCCESS ->{
                            val mResultDeleteSystem = itemViewModel.deleteItemSystem(index)
                            when(mResultDeleteSystem.status){
                                Status.SUCCESS -> {
                                    Utils.Log(TAG,mResultDeleteSystem.data?.responseMessage)
                                    /*Delete local db and folder name*/
                                    Utils.onDeleteItemFolder(index.items_id)
                                    SQLHelper.deleteItem(index)
                                }
                                else -> Utils.Log(TAG,mResultDeleteSystem.message)
                            }
                        } else ->{
                        Utils.Log(TAG,mResultDeleteCloud.message)
                        }
                    }
                }
                Resource.success(true)
            }catch (e:Exception){
                e.printStackTrace()
                Resource.error(Utils.CODE_EXCEPTION, e.message ?:"",null)
            }
        }
    }

    suspend fun getDriveAbout() : Resource<Boolean> {
        return withContext(Dispatchers.IO){
            try {
                val mResultDriveAbout = driveService.getDriveAbout()
                when(mResultDriveAbout.status){
                    Status.SUCCESS -> {
                        updatedDriveValue(mResultDriveAbout.data)
                        Resource.success(true)
                    }
                    else -> {
                        Utils.Log(TAG,mResultDriveAbout.message)
                        updatedDriveValue(null)
                        Resource.error(mResultDriveAbout.code ?: Utils.CODE_EXCEPTION,mResultDriveAbout.message ?:"",null)
                    }
                }
            }catch (e : Exception){
                updatedDriveValue(null)
                Resource.error(Utils.CODE_EXCEPTION, e.message ?:"",null)
            }
        }
    }

    suspend fun geInAppList() : Resource<Boolean> {
        return withContext(Dispatchers.IO){
            try {
                val mResultList = driveService.getListFileInAppFolderCor(SuperSafeApplication.getInstance().getString(R.string.key_appDataFolder))
                when(mResultList.status){
                    Status.SUCCESS -> {
                        mResultList.data?.files?.size?.let { calculatorData(it) }
                        Resource.success(true)
                    }
                    else -> {
                        Utils.Log(TAG,mResultList.message)
                        Resource.error(mResultList.code ?: Utils.CODE_EXCEPTION,mResultList.message ?:"",null)
                    }
                }
            }catch (e : Exception){
                updatedDriveValue(null)
                Resource.error(Utils.CODE_EXCEPTION, e.message ?:"",null)
            }
        }
    }

    private fun updatedDriveValue(mData : DriveAbout? = null){
        val mUser = Utils.getUserInfo()
        if (mData==null){
            if (mUser != null) {
                mUser.driveConnected = false
                Utils.setUserPreShare(mUser)
            }
        }else{
            if (mData.error != null) {
                if (mUser != null) {
                    mUser.driveConnected = false
                    Utils.setUserPreShare(mUser)
                }
            } else {
                if (mUser != null) {
                    mUser.driveConnected = true
                    Utils.setUserPreShare(mUser)
                }
            }
        }
    }

    private fun calculatorData(mCountItem : Int){
        if (mCountItem == 0) {
            val mUser: User? = Utils.getUserInfo()
            if (mUser != null) {
                if (mUser.driveAbout != null) {
                    mUser.driveAbout?.inAppUsed = 0
                    Utils.setUserPreShare(mUser)
                }
            }
        } else {
            val mList: MutableList<ItemModel>? = SQLHelper.getListItemId(isSyncCloud = true, isFakePin = false)
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
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /*Updated data after downloaded file*/
    private fun updatedDateAfterDownloadedFile(isDownloadToExport : Boolean,items: ItemModel){
        try {
            val entityModel: ItemModel? = SQLHelper.getItemById(items.items_id)
            val categoryModel: MainCategoryModel? = SQLHelper.getCategoriesId(items.categories_id, false)
            if (entityModel != null) {
                if (categoryModel != null) {
                    entityModel.categories_local_id = categoryModel.categories_local_id
                }
                entityModel.isSaver = false
                if (items.isOriginalGlobalId) {
                    entityModel.originalSync = true
                    entityModel.global_original_id = items.global_id
                } else {
                    entityModel.thumbnailSync = true
                    entityModel.global_thumbnail_id = items.global_id
                }
                if (entityModel.originalSync && entityModel.thumbnailSync) {
                    entityModel.isSyncCloud = true
                    entityModel.isSyncOwnServer = true
                    entityModel.statusProgress = EnumStatusProgress.DONE.ordinal
                    Utils.Log(TAG, "Synced already....")
                }
                val mFormat = EnumFormatType.values()[entityModel.formatType]
                if (mFormat == EnumFormatType.AUDIO || mFormat == EnumFormatType.FILES) {
                    entityModel.statusProgress = EnumStatusProgress.DONE.ordinal
                }
                /*Check saver space*/
                if (!isDownloadToExport) {
                    checkSaverSpace(entityModel, items.isOriginalGlobalId)
                }
                entityModel.isRequestChecking = true
                SQLHelper.updatedItem(entityModel)
            } else {
                if (categoryModel != null) {
                    items.categories_local_id = categoryModel.categories_local_id
                }
                if (items.isOriginalGlobalId) {
                    items.originalSync = true
                } else {
                    items.thumbnailSync = true
                }
                val mFormat = EnumFormatType.values()[items.formatType]
                if (mFormat == EnumFormatType.AUDIO || mFormat == EnumFormatType.FILES) {
                    items.statusProgress = EnumStatusProgress.DONE.ordinal
                }
                /*Check saver space*/
                if (!isDownloadToExport) {
                    checkSaverSpace(items, items.isOriginalGlobalId)
                }
                SQLHelper.insertedItem(items)
            }
        }
        catch (e : Exception){
            e.printStackTrace()
        }
    }

    /*Check saver space*/
    private fun checkSaverSpace(itemModel: ItemModel, isOriginalGlobalId: Boolean) {
        val mType = EnumFormatType.values()[itemModel.formatType]
        if (mType == EnumFormatType.IMAGE) {
            if (Utils.getSaverSpace()) {
                itemModel.isSaver = true
                Utils.checkSaverToDelete(itemModel.getOriginal(), isOriginalGlobalId)
            }
        }
    }

    /*Updated this area*/
    private val mProgressDownloading  = object : ProgressResponseBody.ProgressResponseBodyListener{
        override fun onAttachmentDownloadedError(message: String?) {
        }
        override fun onAttachmentDownloadUpdate(percent: Int) {
            Utils.Log(TAG,"Downloading...$percent%")
        }
        override fun onAttachmentElapsedTime(elapsed: Long) {
        }
        override fun onAttachmentAllTimeForDownloading(all: Long) {
        }
        override fun onAttachmentRemainingTime(all: Long) {
        }
        override fun onAttachmentSpeedPerSecond(all: Double) {
        }
        override fun onAttachmentTotalDownload(totalByte: Long, totalByteDownloaded: Long) {
        }
        override fun onAttachmentDownloadedSuccess() {
            Utils.Log(TAG,"Download completed")
        }
    }

    private val mProgressUploading = object : ProgressRequestBody.UploadCallbacks {
        override fun onProgressUpdate(percentage: Int) {
            Utils.Log(TAG, "Progressing uploaded $percentage%")
        }
        override fun onError() {
            Utils.Log(TAG, "onError")
        }
        override fun onFinish() {
            Utils.Log(TAG, "onFinish")
        }
    }

    private fun onGetContentOfUpload(items : ItemModel) : HashMap<String?, Any?>?{
        val mContent = HashMap<String?, Any?>()
        val mContentEvent = DriveEvent()
        if (items.isOriginalGlobalId) {
            mContentEvent.fileType = EnumFileType.ORIGINAL.ordinal
        } else {
            mContentEvent.fileType = EnumFileType.THUMBNAIL.ordinal
        }
        if (!Utils.isNotEmptyOrNull(items.categories_id)) {
            return null
        }
        mContentEvent.items_id = items.items_id
        val hex: String? = DriveEvent.getInstance()?.convertToHex(Gson().toJson(mContentEvent))
        mContent[getString(R.string.key_name)] = hex
        val list: MutableList<String?> = ArrayList()
        list.add(getString(R.string.key_appDataFolder))
        mContent[getString(R.string.key_parents)] = list
        return mContent
    }

    private fun onGetContentOfDownload(item : ItemModel) : DownloadFileRequest? {
        val request = DownloadFileRequest()
        var id: String? = ""
        if (item.isOriginalGlobalId) {
            id = item.global_id
            request.file_name = item.originalName
        } else {
            id = item.global_id
            request.file_name = item.thumbnailName
        }
        request.items = item
        request.id = id
        if (!Utils.isNotEmptyOrNull(id)) {
            return null
        }
        item.setOriginal(Utils.getOriginalPath(item.originalName, item.items_id))
        request.path_folder_output = Utils.createDestinationDownloadItem(item.items_id)
        return request
    }

    private fun getString(res : Int) : String{
        return SuperSafeApplication.getInstance().applicationContext.getString(res)
    }

    private fun onGetFilePath(item : ItemModel) : File?{
        val file: File? = if (item.isOriginalGlobalId) {
            File(item.getOriginal())
        } else {
            File(item.getThumbnail())
        }
        if (!SuperSafeApplication.getInstance().getStorage()!!.isFileExist(file?.absolutePath)) {
            SQLHelper.deleteItem(item)
            return null
        }
        return file
    }
}