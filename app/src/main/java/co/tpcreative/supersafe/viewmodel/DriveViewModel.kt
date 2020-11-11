package co.tpcreative.supersafe.viewmodel
import androidx.lifecycle.ViewModel
import co.tpcreative.supersafe.common.api.requester.DriveService
import co.tpcreative.supersafe.common.api.requester.ItemService
import co.tpcreative.supersafe.common.extension.toJson
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.network.Status
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.*
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DriveViewModel(private val driveService: DriveService, itemService: ItemService) : ViewModel(){
    private val itemViewModel = ItemViewModel(itemService)
    val TAG = this::class.java.simpleName
    suspend fun downLoadData(isDownloadToExport : Boolean,globalList : MutableList<ItemModel>) {
        withContext(Dispatchers.IO){
            try {
                val mListLocal: MutableList<ItemModel>? = SQLHelper.getItemListDownload()
                Utils.Log(TAG, "onPreparingDownloadData ==> Local original list " + Gson().toJson(mListLocal))
                if (mListLocal != null) {
                    Utils.Log(TAG, "onPreparingDownloadData ==> Local list " + Gson().toJson(mListLocal))
                    val mergeList: MutableList<ItemModel>? = Utils.clearListFromDuplicate(globalList, mListLocal)
                    for (index in mergeList!!){
                        val mResult = driveService.downloadFile(index)
                        when(mResult.status){
                            Status.SUCCESS -> {
                                updatedDateAfterDownloadedFile(isDownloadToExport,index)
                            }
                            else -> {
                                if (mResult.code==EnumResponseCode.NOT_FOUND.code){
                                    val mResultDeleteItem = itemViewModel.deleteItemSystem(index)
                                    when(mResultDeleteItem.status){
                                        Status.SUCCESS -> Utils.Log(TAG,mResultDeleteItem.data?.responseMessage)
                                        else -> Utils.Log(TAG,mResultDeleteItem.message)
                                    }
                                }
                                Utils.Log(TAG,"Nothing")
                            }
                        }
                    }
                }
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
    }

    suspend fun uploadData() {
        return withContext(Dispatchers.IO){
            try {
                val mResult: MutableList<ItemModel>? = SQLHelper.getItemListUpload()
                val listUpload: MutableList<ItemModel>? = Utils.getMergedOriginalThumbnailList(true, mResult!!)
                for (index in listUpload!!){
                    val mResultUpload = driveService.uploadFile(index)
                    when(mResultUpload.status){
                        Status.SUCCESS ->{
                            val mResultInserted = mResultUpload.data?.id?.let { itemViewModel.insertItemToSystem(index, it) }
                            when(mResultInserted?.status){
                                Status.SUCCESS -> Utils.Log(TAG,"Inserted ${mResultInserted.data?.toJson()}")
                                else -> Utils.Log(TAG,"Error inserted ${mResultInserted?.message}")
                            }
                        }
                        else -> {
                            Utils.Log(TAG,"Error inserted ${mResultUpload.message}")
                        }
                    }
                }
            }catch (e : Exception){
                e.printStackTrace()
            }
        }
    }

    suspend fun deleteItemFromCloud(){
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
                                Status.SUCCESS -> Utils.Log(TAG,"Deleted item from system ${mResultDeleteSystem.data?.data?.toJson()}")
                                else -> Utils.Log(TAG,"Deleted error item from system ${mResultDeleteSystem.message}")
                            }
                        } else ->{
                        Utils.Log(TAG,"Delelte cloud error ${mResultDeleteCloud.message}")
                        }
                    }
                }
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
    }

    /*Updated data after downloaded file*/
    private fun updatedDateAfterDownloadedFile(isDownloadToExport : Boolean,items: ItemModel){
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

    /*Check imported data after sync data*/
    private fun checkImportedDataBeforeSyncData(itemModel: ItemModel) {
        val categoryModel: MainCategoryModel? = SQLHelper.getCategoriesLocalId(itemModel.categories_local_id)
        Utils.Log(TAG, "checkImportedDataBeforeSyncData " + Gson().toJson(categoryModel))
        if (categoryModel != null) {
            if (!Utils.isNotEmptyOrNull(itemModel.categories_id)) {
                itemModel.categories_id = categoryModel.categories_id
                Utils.Log(TAG, "checkImportedDataBeforeSyncData ==> isNotEmptyOrNull")
            }
        }
    }
}