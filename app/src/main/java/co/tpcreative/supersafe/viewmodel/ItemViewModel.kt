package co.tpcreative.supersafe.viewmodel
import androidx.lifecycle.ViewModel
import co.tpcreative.supersafe.common.api.requester.CategoryService
import co.tpcreative.supersafe.common.api.requester.ItemService
import co.tpcreative.supersafe.common.extension.toJson
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.network.Resource
import co.tpcreative.supersafe.common.network.Status
import co.tpcreative.supersafe.common.request.SyncItemsRequest
import co.tpcreative.supersafe.common.response.DataResponse
import co.tpcreative.supersafe.common.response.RootResponse
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.services.SuperSafeService
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.*
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ItemViewModel(private val itemService: ItemService) : ViewModel(){
    private val categoryViewModel = CategoryViewModel(CategoryService())
    val TAG = this::class.java.simpleName
    suspend fun getItemList() : Resource<MutableList<ItemModel>> {
        return withContext(Dispatchers.IO){
           try {
               var mNextSpace : String? = "0"
               val mList = mutableListOf<ItemModel>()
               do {
                   val mResult = itemService.getListData(SyncItemsRequest(nextPage = mNextSpace))
                   when(mResult.status){
                       Status.SUCCESS -> {
                           val itemList = mResult.data?.data?.itemsList
                           itemList?.let {
                               mList.addAll(it)
                           }
                           mNextSpace = mResult.data?.data?.nextPage
                           if (mNextSpace.isNullOrBlank()){
                               Utils.Log(TAG,"Ready to sync...")
                               checkData(mResult.data?.data)
                               Utils.Log(TAG,"Ready to go")
                           }
                       }
                       else -> {
                           mNextSpace = null
                           Utils.Log(TAG,"Action here...")
                       }
                   }
               }while (mNextSpace != null)
               Resource.success(mList)
           }
           catch (e : Exception){
               Resource.error(Utils.CODE_EXCEPTION, e.message ?:"",null)
           }
       }
    }

    suspend fun deleteItemSystem(item : ItemModel) : Resource<RootResponse> {
        return withContext(Dispatchers.IO){
            try {
                val mResult = itemService.deleteOwnItems(SyncItemsRequest(item))
                when(mResult.status){
                    Status.SUCCESS ->{
                        mResult
                    }
                    else ->{
                        Resource.error(mResult.code?:Utils.CODE_EXCEPTION, mResult.message ?:"",null)
                    }
                }
            }catch (e: Exception){
                Resource.error(Utils.CODE_EXCEPTION, e.message ?:"",null)
            }
        }
    }

    suspend fun insertItemToSystem(item : ItemModel, drive_id: String) : Resource<RootResponse>{
        return withContext(Dispatchers.IO){
            try {
                val mItemContent = getItemContentToInsert(item,drive_id)
                checkImportedDataBeforeSyncData(mItemContent!!)
                val mRequest = SyncItemsRequest(Utils.getUserId(), Utils.getUserCloudId(), SuperSafeApplication.getInstance().getDeviceId(), mItemContent!!)
                val mResult = itemService.syncData(mRequest)
                when(mResult.status){
                    Status.SUCCESS ->{
                        if (!(mResult.data?.error)!!){
                            checkSaverSpace(mItemContent, item.isOriginalGlobalId)
                            SQLHelper.updatedItem(mItemContent)
                        }
                        mResult
                    }
                    else ->{
                        Resource.error(mResult.code?:Utils.CODE_EXCEPTION, mResult.message ?:"",null)
                    }
                }
            }catch (e: Exception){
                Resource.error(Utils.CODE_EXCEPTION, e.message ?:"",null)
            }
        }
    }

    suspend fun updateItemToSystem() : Resource<Boolean>{
        return withContext(Dispatchers.IO){
            try {
                val mResult: MutableList<ItemModel>? = SQLHelper.getRequestUpdateItemList()
                for (index in mResult!!){
                    val mResultUpdated =  itemService.syncData(SyncItemsRequest(index))
                    when(mResultUpdated.status){
                        Status.SUCCESS -> Utils.Log(TAG,"Updated successful ${mResultUpdated.data?.toJson()}")
                        else -> Utils.Log(TAG,"Error updated ${mResultUpdated.message}")
                    }
                }
                Resource.success(true)
            }catch (e : Exception){
                e.printStackTrace()
                Resource.error(Utils.CODE_EXCEPTION, e.message ?:"",null)
            }
        }
    }

    private fun setUserSyncData(mData : SyncData?){
        val mUser  = Utils.getUserInfo()
        mUser?.let {
            it.syncData = mData
            Utils.setUserPreShare(it)
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

    private fun checkData(mData : DataResponse?){
        setUserSyncData(mData?.syncData)
        mData?.itemsList?.let { onItemDeleteSyncedLocal(it) }
        categoryViewModel.checkAndAddCategory(mData?.categoriesList)
    }

    private fun onItemDeleteSyncedLocal(mList: List<ItemModel>) {
        val mResultList = Utils.checkItemDeleteSyncedLocal(mList)
        mResultList.let {
            for (index in it) {
                SQLHelper.deleteItem(index)
                index.items_id?.let { it1 ->
                    Utils.deleteFolderOfItemId(it1)
                }
            }
            Utils.Log(TAG, "Total need to delete items synced local.... " + it.size)
        }
    }

    private fun getItemContentToInsert(item: ItemModel, drive_id: String) : ItemModel?{
        item.isSyncOwnServer = true
        val entityModel: ItemModel = SQLHelper.getItemById(item.items_id) ?: return null
        if (item.isOriginalGlobalId) {
            if (!Utils.isNotEmptyOrNull(entityModel.global_thumbnail_id)) {
                entityModel.global_thumbnail_id = "null"
            }
            entityModel.originalSync = true
            entityModel.global_original_id = drive_id
        } else {
            if (!Utils.isNotEmptyOrNull(entityModel.global_original_id)) {
                entityModel.global_original_id = "null"
            }
            entityModel.thumbnailSync = true
            entityModel.global_thumbnail_id = drive_id
        }
        if (entityModel.originalSync && entityModel.thumbnailSync) {
            entityModel.isSyncCloud = true
            entityModel.isSyncOwnServer = true
            entityModel.statusProgress = EnumStatusProgress.DONE.ordinal
        }
        val mFormat = EnumFormatType.values()[entityModel.formatType]
        if (mFormat == EnumFormatType.AUDIO || mFormat == EnumFormatType.FILES) {
            entityModel.statusProgress = EnumStatusProgress.DONE.ordinal
        }
        return entityModel
    }
}