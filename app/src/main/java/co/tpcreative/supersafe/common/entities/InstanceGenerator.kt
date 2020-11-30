package co.tpcreative.supersafe.common.entities
import android.content.Context
import androidx.room.Database
import androidx.room.Ignore
import androidx.room.Room
import androidx.room.RoomDatabase
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.BreakInAlertsEntityModel
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.ItemEntityModel
import co.tpcreative.supersafe.model.MainCategoryEntityModel
import java.util.*

@Database(entities = [ItemEntity::class, MainCategoryEntity::class, BreakInAlertsEntity::class], version = 6, exportSchema = false)
abstract class InstanceGenerator : RoomDatabase() {
    @Ignore
    abstract fun itemsDao(): ItemsDao?

    @Ignore
    abstract fun mainCategoriesDao(): MainCategoriesDao?

    @Ignore
    abstract fun breakInAlertsDao(): BreakInAlertsDao?

    fun getUUId(): String? {
        return try {
            UUID.randomUUID().toString()
        } catch (e: Exception) {
            "" + System.currentTimeMillis()
        }
    }

    /*Items action*/
    fun onInsert(cTalkManager: ItemEntity?) {
        try {
            if (cTalkManager == null) {
                return
            }
            instance?.itemsDao()?.insert(cTalkManager)
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
    }

    fun onUpdate(cTalkManager: ItemEntity?) {
        try {
            if (cTalkManager == null) {
                Utils.Log(TAG, "Null???? ")
                return
            }
            instance?.itemsDao()?.update(cTalkManager)
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
    }

    fun getListItems(categories_local_id: String?, isDeleteLocal: Boolean, isExport: Boolean, isFakePin: Boolean): MutableList<ItemEntityModel>? {
        try {
            if (categories_local_id == null) {
                return null
            }
            try {
                val mList: MutableList<ItemEntity>? = instance?.itemsDao()?.loadAll(categories_local_id, isDeleteLocal, isExport, isFakePin)
                val mData: MutableList<ItemEntityModel> = ArrayList<ItemEntityModel>()
                if (mList != null) {
                    for (index in mList) {
                        mData.add(ItemEntityModel(index))
                    }
                    return mData
                }
            } catch (e: Exception) {
            }
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
        return null
    }

    fun getListItems(categories_local_id: String?, isDeleteLocal: Boolean, isFakePin: Boolean): MutableList<ItemEntityModel>? {
        if (categories_local_id == null) {
            return null
        }
        try {
            val mList: MutableList<ItemEntity>? = instance?.itemsDao()?.loadAll(categories_local_id, isDeleteLocal, isFakePin)
            val mData: MutableList<ItemEntityModel> = ArrayList<ItemEntityModel>()
            if (mList != null) {
                for (index in mList) {
                    mData.add(ItemEntityModel(index))
                }
                return mData
            }
        } catch (e: Exception) {
        }
        return null
    }

    fun getListItems(categories_local_id: String?, formatType: Int, isDeleteLocal: Boolean, isFakePin: Boolean): MutableList<ItemEntityModel>? {
        if (categories_local_id == null) {
            return null
        }
        try {
            val mList: MutableList<ItemEntity>? = instance?.itemsDao()?.loadAll(categories_local_id, formatType, isDeleteLocal, isFakePin)
            val mData: MutableList<ItemEntityModel> = ArrayList<ItemEntityModel>()
            if (mList != null) {
                for (index in mList) {
                    mData.add(ItemEntityModel(index))
                }
                return mData
            }
        } catch (e: Exception) {
        }
        return null
    }

    fun getListItems(categories_local_id: String?, isFakePin: Boolean): MutableList<ItemEntityModel>? {
        try {
            val mList: MutableList<ItemEntity>? = instance?.itemsDao()?.loadAll(categories_local_id, isFakePin)
            val mData: MutableList<ItemEntityModel> = ArrayList<ItemEntityModel>()
            if (mList != null) {
                for (index in mList) {
                    mData.add(ItemEntityModel(index))
                }
                return mData
            }
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
        return null
    }

    fun getListItems(categories_id: String?): MutableList<ItemEntityModel>? {
        try {
            val mList: MutableList<ItemEntity>? = instance?.itemsDao()?.loadRequestItemsList(categories_id,)
            val mData: MutableList<ItemEntityModel> = ArrayList<ItemEntityModel>()
            if (mList != null) {
                for (index in mList) {
                    mData.add(ItemEntityModel(index))
                }
                return mData
            }
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
        return null
    }


    fun getListAllItems(isFakePin: Boolean): MutableList<ItemEntityModel>? {
        try {
            val mList: MutableList<ItemEntity>? = instance?.itemsDao()?.loadAll(isFakePin)
            val mData: MutableList<ItemEntityModel> = ArrayList<ItemEntityModel>()
            if (mList != null) {
                for (index in mList) {
                    mData.add(ItemEntityModel(index))
                }
                return mData
            }
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
        return null
    }

    fun getListAllItemsSaved(isSaved: Boolean, isSyncCloud: Boolean): MutableList<ItemEntityModel>? {
        try {
            val mList: MutableList<ItemEntity>? = instance?.itemsDao()?.loadAllSaved(isSaved, isSyncCloud)
            val mData: MutableList<ItemEntityModel> = ArrayList<ItemEntityModel>()
            if (mList != null) {
                for (index in mList) {
                    mData.add(ItemEntityModel(index))
                }
                return mData
            }
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
        return null
    }

    @Synchronized
    fun getListAllItemsSaved(formatType: Int): MutableList<ItemEntity>? {
        try {
            return instance?.itemsDao()?.loadAllSaved(formatType)
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
        return null
    }

    fun getListAllItems(isDeleteLocal: Boolean, isFakePin: Boolean): MutableList<ItemEntityModel>? {
        try {
            val mList: MutableList<ItemEntity>? = instance?.itemsDao()?.loadAll(isDeleteLocal, isFakePin)
            val mData: MutableList<ItemEntityModel>? = ArrayList<ItemEntityModel>()
            if (mList != null) {
                for (index in mList) {
                    mData?.add(ItemEntityModel(index))
                }
                return mData
            }
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
        return null
    }

    fun getDeleteLocalListItems(isDeleteLocal: Boolean, deleteAction: Int, isFakePin: Boolean): MutableList<ItemEntityModel>? {
        try {
            val mList: MutableList<ItemEntity>? = instance?.itemsDao()?.loadDeleteLocalDataItems(isDeleteLocal, deleteAction, isFakePin)
            val mData: MutableList<ItemEntityModel> = ArrayList<ItemEntityModel>()
            if (mList != null) {
                for (index in mList) {
                    mData.add(ItemEntityModel(index))
                }
                return mData
            }
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
        return null
    }

    @Synchronized
    fun getDeleteLocalAndGlobalListItems(isDeleteLocal: Boolean, isDeleteGlobal: Boolean, isFakePin: Boolean): MutableList<ItemEntity>? {
        try {
            return instance?.itemsDao()?.loadDeleteLocalAndGlobalDataItems(isDeleteLocal, isDeleteGlobal, isFakePin)
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
        return null
    }

    @Synchronized
    fun getListSyncUploadDataItems(isFakePin: Boolean): MutableList<ItemEntity>? {
        try {
            return instance?.itemsDao()?.loadSyncDataItems(false, false, EnumStatus.UPLOAD.ordinal, isFakePin)
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
        return null
    }

    @Synchronized
    fun getListSyncUploadDataItemsByNull(isFakePin: Boolean): MutableList<ItemEntity>? {
        try {
            return instance?.itemsDao()?.loadSyncDataItemsByCategoriesIdNull(false, false, EnumStatus.UPLOAD.ordinal, isFakePin, "null")
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
        return null
    }

    fun getRequestUploadData(isFakePin: Boolean): MutableList<ItemEntityModel>? {
        try {
            val mList: MutableList<ItemEntity>? = instance?.itemsDao()?.loadRequestUploadData(false, false, EnumStatus.UPLOAD.ordinal, isFakePin)
            val mData: MutableList<ItemEntityModel>? = ArrayList<ItemEntityModel>()
            if (mList != null) {
                for (index in mList) {
                    mData?.add(ItemEntityModel(index))
                }
                return mData
            }
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
        return null
    }

    @Synchronized
    fun getListSyncDownloadDataItems(isFakePin: Boolean): MutableList<ItemEntity>? {
        try {
            return instance?.itemsDao()?.loadSyncDataItems(false, false, EnumStatus.DOWNLOAD.ordinal, isFakePin)
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
        return null
    }

    fun getListSyncData(isSyncCloud: Boolean, isFakePin: Boolean): MutableList<ItemEntityModel>? {
        try {
            val mList: MutableList<ItemEntity>? = instance?.itemsDao()?.loadSyncData(isSyncCloud, isFakePin)
            val mData: MutableList<ItemEntityModel> = ArrayList<ItemEntityModel>()
            if (mList != null) {
                for (index in mList) {
                    mData.add(ItemEntityModel(index))
                }
                return mData
            }
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
        return null
    }

    @Synchronized
    fun getListSyncData(isSyncCloud: Boolean, isSaver: Boolean, isWaitingForExporting: Boolean, isFakePin: Boolean): MutableList<ItemEntity>? {
        try {
            return instance?.itemsDao()?.loadSyncData(isSyncCloud, isSaver, isWaitingForExporting, isFakePin)
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
        return null
    }

    fun getListSyncData(isSyncCloud: Boolean, isSaver: Boolean, isFakePin: Boolean): MutableList<ItemEntityModel>? {
        try {
            val mList: MutableList<ItemEntity>? = instance?.itemsDao()?.loadSyncData(isSyncCloud, isSaver, isFakePin)
            val mData: MutableList<ItemEntityModel> = ArrayList<ItemEntityModel>()
            if (mList != null) {
                for (index in mList) {
                    mData.add(ItemEntityModel(index))
                }
                return mData
            }
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
        return null
    }

    @Synchronized
    fun getItemId(id: Int, isFakePin: Boolean): ItemEntity? {
        try {
            return instance?.itemsDao()?.loadItemId(id, isFakePin)
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
        return null
    }

    fun getItemId(item_id: String?): ItemEntityModel? {
        try {
            val mResult: ItemEntity? = instance?.itemsDao()?.loadItemId(item_id)
            if (mResult != null) {
                return ItemEntityModel(mResult)
            }
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
        return null
    }

    fun getLatestId(categories_local_id: String?, isDeleteLocal: Boolean, isFakePin: Boolean): ItemEntityModel? {
        try {
            val mResult: ItemEntity? = instance?.itemsDao()?.getLatestId(categories_local_id, isDeleteLocal, isFakePin)
            if (mResult != null) {
                return ItemEntityModel(mResult)
            }
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
        return null
    }

    fun getItemId(item_id: String?, isFakePin: Boolean): ItemEntityModel? {
        try {
            val mResult: ItemEntity? = instance?.itemsDao()?.loadItemId(item_id, isFakePin)
            if (mResult != null) {
                return ItemEntityModel(mResult)
            }
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
        return null
    }

    @Synchronized
    fun getListItemId(item_id: String?, isFakePin: Boolean): MutableList<ItemEntity>? {
        try {
            return instance?.itemsDao()?.loadListItemId(item_id, isFakePin)
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
        return null
    }

    @Synchronized
    fun getItemId(item_id: String?, isSyncCloud: Boolean, isFakePin: Boolean): ItemEntity? {
        try {
            return instance?.itemsDao()?.loadItemId(item_id, isSyncCloud, isFakePin)
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
        return null
    }

    fun getListItemId(isSyncCloud: Boolean, isFakePin: Boolean): MutableList<ItemEntityModel>? {
        try {
            val mList: MutableList<ItemEntity>? = instance?.itemsDao()?.loadListItemId(isSyncCloud, isFakePin)
            val mData: MutableList<ItemEntityModel> = ArrayList<ItemEntityModel>()
            if (mList != null) {
                for (index in mList) {
                    mData.add(ItemEntityModel(index))
                }
                return mData
            }
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
        return null
    }

    fun getItemList(isSyncCloud: Boolean, isFakePin: Boolean): MutableList<ItemEntityModel>? {
        try {
            val mList: MutableList<ItemEntity>? = instance?.itemsDao()?.loadListItemId(isSyncCloud, isFakePin)
            val mData: MutableList<ItemEntityModel> = ArrayList<ItemEntityModel>()
            if (mList != null) {
                for (index in mList) {
                    mData.add(ItemEntityModel(index))
                }
                return mData
            }
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
        return null
    }

    fun getLoadListItemUpdate(isUpdate: Boolean, isSyncCloud: Boolean, isSyncOwnServer: Boolean, isFakePin: Boolean,isRequestChecking : Boolean): MutableList<ItemEntityModel>? {
        try {
            val mList: MutableList<ItemEntity>? = instance?.itemsDao()?.loadListItemUpdate(isUpdate, isSyncCloud, isSyncOwnServer, isFakePin,isRequestChecking)
            val mData: MutableList<ItemEntityModel> = ArrayList<ItemEntityModel>()
            if (mList != null) {
                for (index in mList) {
                    mData.add(ItemEntityModel(index))
                }
                return mData
            }
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
        return null
    }

    @Synchronized
    fun getListItemId(isSyncCloud: Boolean, isSyncOwnServer: Boolean, isFakePin: Boolean): MutableList<ItemEntity>? {
        try {
            return instance?.itemsDao()?.loadListItemId(isSyncCloud, isSyncOwnServer, isFakePin)
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
        return null
    }

    fun onDelete(entity: ItemEntity?): Boolean {
        try {
            instance?.itemsDao()?.delete(entity)
            return true
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
        return false
    }

    fun onDelete(entity: MainCategoryEntity?): Boolean {
        try {
            instance?.mainCategoriesDao()?.delete(entity)
            return true
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
        return false
    }

    @Synchronized
    fun onDeleteAll(categories_local_id: String?, isFakePin: Boolean): Boolean {
        try {
            instance?.itemsDao()?.deleteAll(categories_local_id, isFakePin)
            return true
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
        return false
    }

    /*Main categories*/
    fun onInsert(item: MainCategoryEntityModel?) {
        try {
            if (item == null) {
                return
            }
            instance?.mainCategoriesDao()?.insert(MainCategoryEntity(item))
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
    }

    fun onUpdate(cTalkManager: MainCategoryEntityModel) {
        try {
            Utils.Log(TAG, "Updated :" + cTalkManager.categories_id)
            instance?.mainCategoriesDao()?.update(MainCategoryEntity(cTalkManager))
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
    }

    fun getListCategories(isFakePin: Boolean): MutableList<MainCategoryEntityModel>? {
        try {
            val mResult: MutableList<MainCategoryEntity>? = instance?.mainCategoriesDao()?.loadAll(isFakePin)
            val mData: MutableList<MainCategoryEntityModel> = ArrayList<MainCategoryEntityModel>()
            if (mResult != null) {
                for (index in mResult) {
                    mData.add(MainCategoryEntityModel(index))
                }
                return mData
            }
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
        return null
    }

    fun getListCategories(categories_local_id: String?, isDelete: Boolean, isFakePin: Boolean): MutableList<MainCategoryEntityModel>? {
        try {
            val mList: MutableList<MainCategoryEntity>? = instance?.mainCategoriesDao()?.loadAll(categories_local_id, isDelete, isFakePin)
            val mData: MutableList<MainCategoryEntityModel> = ArrayList<MainCategoryEntityModel>()
            if (mList != null) {
                for (index in mList) {
                    mData.add(MainCategoryEntityModel(index))
                }
                return mData
            }
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
        return null
    }

    fun getListCategories(isDelete: Boolean, isFakePin: Boolean): MutableList<MainCategoryEntityModel>? {
        try {
            val mList: MutableList<MainCategoryEntity>? = instance?.mainCategoriesDao()?.loadAll(isDelete, isFakePin)
            val mData: MutableList<MainCategoryEntityModel> = ArrayList<MainCategoryEntityModel>()
            if (mList != null) {
                for (index in mList) {
                    mData.add(MainCategoryEntityModel(index))
                }
                return mData
            }
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
        return null
    }

    @Synchronized
    fun getChangedCategoryList(): MutableList<MainCategoryEntityModel>? {
        try {
            val mList: MutableList<MainCategoryEntity>? = instance?.mainCategoriesDao()?.loadAllChangedItem(true, false)
            val mData: MutableList<MainCategoryEntityModel> = ArrayList<MainCategoryEntityModel>()
            if (mList != null) {
                for (index in mList) {
                    mData.add(MainCategoryEntityModel(index))
                }
                return mData
            }
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
        return null
    }

    fun geCategoryList(isDelete: Boolean, isFakePin: Boolean): MutableList<MainCategoryEntityModel>? {
        try {
            val mList: MutableList<MainCategoryEntity>? = instance?.mainCategoriesDao()?.loadAll(isDelete, isFakePin)
            val mData: MutableList<MainCategoryEntityModel> = ArrayList<MainCategoryEntityModel>()
            if (mList != null) {
                for (index in mList) {
                    mData.add(MainCategoryEntityModel(index))
                }
                return mData
            }
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
        return null
    }

    fun getCategoriesItemId(categories_hex_name: String?, isFakePin: Boolean): MainCategoryEntityModel? {
        try {
            val mResult: MainCategoryEntity? = instance?.mainCategoriesDao()?.loadItemId(categories_hex_name, isFakePin)
            if (mResult != null) {
                return MainCategoryEntityModel(mResult)
            }
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
        return null
    }

    fun getLatestItem(): Int {
        try {
            val categories: MainCategoryEntity? = instance?.mainCategoriesDao()?.loadLatestItem(1)
            var count = 0
            if (categories != null) {
                count += categories.id + 1
                return count
            }
            return 0
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
        return 0
    }

    @Synchronized
    fun loadListAllItemId(categories_hex_name: String?, isFakePin: Boolean): MutableList<MainCategoryEntity>? {
        try {
            return instance?.mainCategoriesDao()?.loadListAllItemId(categories_hex_name, isFakePin)
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
        return null
    }

    fun getCategoriesLocalId(categories_local_id: String?, isFakePin: Boolean): MainCategoryEntityModel? {
        try {
            val mResult: MainCategoryEntity? = instance?.mainCategoriesDao()?.loadItemLocalId(categories_local_id, isFakePin)
            if (mResult != null) {
                return MainCategoryEntityModel(mResult)
            }
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
        return null
    }

    fun getCategoriesLocalId(categories_local_id: String?): MainCategoryEntityModel? {
        try {
            val mResut: MainCategoryEntity? = instance?.mainCategoriesDao()?.loadLocalId(categories_local_id)
            if (mResut != null) {
                return MainCategoryEntityModel(mResut)
            }
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
        return null
    }

    fun getCategoriesId(categories_id: String?, isFakePin: Boolean): MainCategoryEntityModel? {
        try {
            val mResult: MainCategoryEntity? = instance?.mainCategoriesDao()?.loadItemCategoriesId(categories_id, isFakePin)
            if (mResult != null) {
                return MainCategoryEntityModel(mResult)
            }
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
        return null
    }

    @Synchronized
    fun loadItemCategoriesSync(isSyncOwnServer: Boolean, isFakePin: Boolean): MainCategoryEntity? {
        try {
            return instance?.mainCategoriesDao()?.loadItemCategoriesSync(isSyncOwnServer, isFakePin)
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
        return null
    }

    fun loadListItemCategoriesSync(isSyncOwnServer: Boolean, isFakePin: Boolean): MutableList<MainCategoryEntityModel>? {
        try {
            val mList: MutableList<MainCategoryEntity>? = instance?.mainCategoriesDao()?.loadListItemCategoriesSync(isSyncOwnServer, isFakePin)
            val mData: MutableList<MainCategoryEntityModel> = ArrayList<MainCategoryEntityModel>()
            if (mList != null) {
                for (index in mList) {
                    mData.add(MainCategoryEntityModel(index))
                }
                return mData
            }
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
        return null
    }

    @Synchronized
    fun loadListItemCategoriesSync(isSyncOwnServer: Boolean, limit: Int, isFakePin: Boolean): MutableList<MainCategoryEntity>? {
        try {
            return instance?.mainCategoriesDao()?.loadListItemCategoriesSync(isSyncOwnServer, limit, isFakePin)
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
        return null
    }

    /*Break In Alerts*/ /*Items action*/
    fun onInsert(cTalkManager: BreakInAlertsEntityModel?) {
        try {
            if (cTalkManager == null) {
                return
            }
            instance?.breakInAlertsDao()?.insert(BreakInAlertsEntity(cTalkManager))
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
    }

    fun onUpdate(cTalkManager: BreakInAlertsEntityModel?) {
        try {
            if (cTalkManager == null) {
                Utils.Log(TAG, "Null???? ")
                return
            }
            instance?.breakInAlertsDao()?.update(BreakInAlertsEntity(cTalkManager))
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
    }

    fun onDelete(cTalkManager: BreakInAlertsEntityModel?) {
        try {
            if (cTalkManager == null) {
                Utils.Log(TAG, "Null???? ")
                return
            }
            instance?.breakInAlertsDao()?.delete(BreakInAlertsEntity(cTalkManager))
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
    }

    fun onCleanDatabase() {
        instance?.breakInAlertsDao()?.deleteBreakInAlerts()
        instance?.itemsDao()?.deleteAllItems()
        instance?.mainCategoriesDao()?.deleteAllCategories()
    }

    /*Improved sqlite*/
    fun getAllListItems(): MutableList<ItemEntityModel>? {
        try {
            val mValue: MutableList<ItemEntity>? = instance?.itemsDao()?.loadAll(false)
            val mList: MutableList<ItemEntityModel> = ArrayList<ItemEntityModel>()
            if (mValue != null) {
                for (index in mValue) {
                    mList.add(ItemEntityModel(index))
                }
            }
            return mList
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
        return null
    }

    fun getAllListItems(isSyncCloud: Boolean, isFake: Boolean): MutableList<ItemEntityModel>? {
        try {
            val mValue: MutableList<ItemEntity>? = instance?.itemsDao()?.loadAll(false)
            val mList: MutableList<ItemEntityModel> = ArrayList<ItemEntityModel>()
            if (mValue != null) {
                for (index in mValue) {
                    mList.add(ItemEntityModel(index))
                }
            }
            return mList
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
        return null
    }

    fun getItemListDownload(isCloudSync: Boolean, isFake: Boolean): MutableList<ItemEntityModel>? {
        try {
            val mValue: MutableList<ItemEntity>? = instance?.itemsDao()?.loadListItem(isCloudSync, isFake)
            val mList: MutableList<ItemEntityModel> = ArrayList<ItemEntityModel>()
            if (mValue != null) {
                for (index in mValue) {
                    mList.add(ItemEntityModel(index))
                }
            }
            return mList
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
        return null
    }

    fun getBreakInAlertsList(): MutableList<BreakInAlertsEntityModel> {
        val mResult: MutableList<BreakInAlertsEntity>? = instance?.breakInAlertsDao()?.loadAll()
        val mList: MutableList<BreakInAlertsEntityModel> = ArrayList<BreakInAlertsEntityModel>()
        if (mResult != null) {
            for (index in mResult) {
                mList.add(BreakInAlertsEntityModel(index))
            }
            Collections.sort(mList, Comparator { lhs, rhs ->
                lhs.id - rhs.id
            })
        }
        return mList
    }

    fun cleanUp(){
        instance = null
    }

    companion object {
        @Ignore
        private var instance: InstanceGenerator? = null
        @Ignore
        val TAG = InstanceGenerator::class.java.simpleName
        fun getInstance(context: Context): InstanceGenerator? {
            if (instance == null) {
                instance = Room.databaseBuilder(context,
                        InstanceGenerator::class.java, SuperSafeApplication.getInstance().getString(R.string.key_database))
                        .addMigrations(SuperSafeApplication.getInstance().migrationFrom4To5,SuperSafeApplication.getInstance().migrationFrom5To6)
                        .allowMainThreadQueries()
                        .build()
            }
            return instance
        }
    }
}