package co.tpcreative.supersafe.common.entitiesimport
import androidx.room.*
import co.tpcreative.supersafe.common.entities.ItemEntity

@Dao
interface ItemsDao {
    @Insert
    open fun insert(vararg items: ItemEntity?)

    @Update
    open fun update(vararg items: ItemEntity?)

    @Delete
    open fun delete(vararg items: ItemEntity?)

    @Query("DELETE FROM items")
    open fun deleteAllItems()

    @Query("Delete from items  WHERE categories_local_id = :categories_local_id AND isFakePin =:isFakePin")
    open fun deleteAll(categories_local_id: String?, isFakePin: Boolean)

    @Query("Select * FROM items WHERE categories_local_id = :categories_local_id AND isFakePin =:isFakePin")
    open fun loadAll(categories_local_id: String?, isFakePin: Boolean): MutableList<ItemEntity>?

    @Query("Select * FROM items WHERE categories_local_id = :categories_local_id AND formatType =:formatType AND isDeleteLocal =:isDeleteLocal AND isFakePin =:isFakePin ORDER BY originalName DESC")
    open fun loadAll(categories_local_id: String?, formatType: Int, isDeleteLocal: Boolean, isFakePin: Boolean): MutableList<ItemEntity>?

    @Query("Select * FROM items WHERE categories_local_id = :categories_local_id AND isDeleteLocal =:isDeleteLocal AND isExport =:isExport AND isFakePin =:isFakePin ORDER BY originalName DESC")
    open fun loadAll(categories_local_id: String?, isDeleteLocal: Boolean, isExport: Boolean, isFakePin: Boolean): MutableList<ItemEntity>?

    @Query("Select * FROM items WHERE categories_local_id = :categories_local_id AND isDeleteLocal =:isDeleteLocal AND isFakePin =:isFakePin ORDER BY originalName DESC")
    open fun loadAll(categories_local_id: String?, isDeleteLocal: Boolean, isFakePin: Boolean): MutableList<ItemEntity>?

    @Query("Select * FROM items WHERE categories_local_id = :categories_local_id AND isDeleteLocal =:isDeleteLocal AND isFakePin =:isFakePin ORDER BY originalName DESC LIMIT 1")
    open fun getLatestId(categories_local_id: String?, isDeleteLocal: Boolean, isFakePin: Boolean): ItemEntity?

    @Query("Select * FROM items WHERE isSyncCloud = :isSyncCloud AND isDeleteLocal = :isDeleteLocal AND statusAction =:statusAction AND isFakePin =:isFakePin  ORDER BY originalName DESC LIMIT 3")
    open fun loadSyncDataItems(isSyncCloud: Boolean, isDeleteLocal: Boolean, statusAction: Int, isFakePin: Boolean): MutableList<ItemEntity>?

    @Query("Select * FROM items WHERE isSyncCloud = :isSyncCloud AND isDeleteLocal = :isDeleteLocal AND statusAction =:statusAction AND isFakePin =:isFakePin AND categories_id =:categories_id")
    open fun loadSyncDataItemsByCategoriesIdNull(isSyncCloud: Boolean, isDeleteLocal: Boolean, statusAction: Int, isFakePin: Boolean, categories_id: String?): MutableList<ItemEntity>?

    @Query("Select * FROM items WHERE isSyncCloud = :isSyncCloud AND isDeleteLocal = :isDeleteLocal AND statusAction =:statusAction AND isFakePin =:isFakePin")
    open fun loadRequestUploadData(isSyncCloud: Boolean, isDeleteLocal: Boolean, statusAction: Int, isFakePin: Boolean): MutableList<ItemEntity>?

    @Query("Select * FROM items WHERE isDeleteLocal = :isDeleteLocal AND deleteAction = :deleteAction AND isFakePin =:isFakePin")
    open fun loadDeleteLocalDataItems(isDeleteLocal: Boolean, deleteAction: Int, isFakePin: Boolean): MutableList<ItemEntity>?

    @Query("Select * FROM items WHERE isDeleteLocal = :isDeleteLocal AND isDeleteGlobal = :isDeleteGlobal AND isFakePin =:isFakePin")
    open fun loadDeleteLocalAndGlobalDataItems(isDeleteLocal: Boolean, isDeleteGlobal: Boolean, isFakePin: Boolean): MutableList<ItemEntity>?

    @Query("Select * FROM items WHERE  isSyncCloud = :isSyncCloud AND isFakePin =:isFakePin")
    open fun loadSyncData(isSyncCloud: Boolean, isFakePin: Boolean): MutableList<ItemEntity>?

    @Query("Select * FROM items WHERE  isSyncCloud = :isSyncCloud AND isSaver =:isSaver AND isWaitingForExporting =:isWaitingForExporting AND isFakePin =:isFakePin ORDER BY originalName DESC LIMIT 1")
    open fun loadSyncData(isSyncCloud: Boolean, isSaver: Boolean, isWaitingForExporting: Boolean, isFakePin: Boolean): MutableList<ItemEntity>?

    @Query("Select * FROM items WHERE  isSyncCloud = :isSyncCloud AND isSaver =:isSaver AND isFakePin =:isFakePin")
    open fun loadSyncData(isSyncCloud: Boolean, isSaver: Boolean, isFakePin: Boolean): MutableList<ItemEntity>?

    @Query("Select * FROM items WHERE id = :id AND isFakePin =:isFakePin")
    open fun loadItemId(id: Int, isFakePin: Boolean): ItemEntity?

    @Query("Select * FROM items WHERE items_id = :item_id")
    open fun loadItemId(item_id: String?): ItemEntity?

    @Query("Select * FROM items WHERE items_id = :globalName AND isSyncCloud = :isSyncCloud AND isFakePin =:isFakePin")
    open fun loadItemId(globalName: String?, isSyncCloud: Boolean, isFakePin: Boolean): ItemEntity?

    @Query("Select * FROM items WHERE isSyncCloud = :isSyncCloud AND isSyncOwnServer =:isSyncOwnServer AND isFakePin =:isFakePin")
    open fun loadListItemId(isSyncCloud: Boolean, isSyncOwnServer: Boolean, isFakePin: Boolean): MutableList<ItemEntity>?

    @Query("Select * FROM items WHERE isSyncCloud = :isSyncCloud AND isFakePin =:isFakePin")
    open fun loadListItemId(isSyncCloud: Boolean, isFakePin: Boolean): MutableList<ItemEntity>?

    @Query("Select * FROM items WHERE isSyncCloud = :isSyncCloud AND isFakePin =:isFakePin")
    open fun loadListItem(isSyncCloud: Boolean, isFakePin: Boolean): MutableList<ItemEntity>?

    @Query("Select * FROM items WHERE items_id = :items_id AND isFakePin =:isFakePin")
    open fun loadItemId(items_id: String?, isFakePin: Boolean): ItemEntity?

    @Query("Select * FROM items WHERE items_id = :items_id AND isFakePin =:isFakePin")
    open fun loadListItemId(items_id: String?, isFakePin: Boolean): MutableList<ItemEntity>?

    @Query("Select * FROM items WHERE isFakePin =:isFakePin ")
    open fun loadAll(isFakePin: Boolean): MutableList<ItemEntity>?

    @Query("Select * FROM items WHERE isDeleteLocal =:isDeleteLocal AND isFakePin =:isFakePin ")
    open fun loadAll(isDeleteLocal: Boolean, isFakePin: Boolean): MutableList<ItemEntity>?

    @Query("Select * FROM items WHERE isSaver =:isSaver AND isSyncCloud =:isSyncCloud")
    open fun loadAllSaved(isSaver: Boolean, isSyncCloud: Boolean): MutableList<ItemEntity>?

    @Query("Select * FROM items WHERE formatType =:formatType ORDER BY originalName DESC LIMIT 2")
    open fun loadAllSaved(formatType: Int): MutableList<ItemEntity>?

    @Query("Select * FROM items WHERE isUpdate =:isUpdate AND  isSyncCloud = :isSyncCloud AND isSyncOwnServer =:isSyncOwnServer AND isFakePin =:isFakePin")
    open fun loadListItemUpdate(isUpdate: Boolean, isSyncCloud: Boolean, isSyncOwnServer: Boolean, isFakePin: Boolean): MutableList<ItemEntity>?
}