package co.tpcreative.supersafe.common.entities
import androidx.room.*

@Dao
interface ItemsDao {
    @Insert
    fun insert(vararg items: ItemEntity?)

    @Update
    fun update(vararg items: ItemEntity?)

    @Delete
    fun delete(vararg items: ItemEntity?)

    @Query("DELETE FROM items")
    fun deleteAllItems()

    @Query("Delete from items  WHERE categories_local_id = :categories_local_id AND isFakePin =:isFakePin")
    fun deleteAll(categories_local_id: String?, isFakePin: Boolean)

    @Query("Select * FROM items WHERE categories_local_id = :categories_local_id AND isFakePin =:isFakePin")
    fun loadAll(categories_local_id: String?, isFakePin: Boolean): MutableList<ItemEntity>?

    @Query("Select * FROM items WHERE categories_local_id = :categories_local_id AND formatType =:formatType AND isDeleteLocal =:isDeleteLocal AND isFakePin =:isFakePin ORDER BY originalName DESC")
    fun loadAll(categories_local_id: String?, formatType: Int, isDeleteLocal: Boolean, isFakePin: Boolean): MutableList<ItemEntity>?

    @Query("Select * FROM items WHERE categories_local_id = :categories_local_id AND isDeleteLocal =:isDeleteLocal AND isExport =:isExport AND isFakePin =:isFakePin ORDER BY originalName DESC")
    fun loadAll(categories_local_id: String?, isDeleteLocal: Boolean, isExport: Boolean, isFakePin: Boolean): MutableList<ItemEntity>?

    @Query("Select * FROM items WHERE categories_local_id = :categories_local_id AND isDeleteLocal =:isDeleteLocal AND isFakePin =:isFakePin ORDER BY originalName DESC")
    fun loadAll(categories_local_id: String?, isDeleteLocal: Boolean, isFakePin: Boolean): MutableList<ItemEntity>?

    @Query("Select * FROM items WHERE categories_local_id = :categories_local_id AND isDeleteLocal =:isDeleteLocal AND isFakePin =:isFakePin ORDER BY originalName DESC LIMIT 1")
    fun getLatestId(categories_local_id: String?, isDeleteLocal: Boolean, isFakePin: Boolean): ItemEntity?

    @Query("Select * FROM items WHERE isSyncCloud = :isSyncCloud AND isDeleteLocal = :isDeleteLocal AND statusAction =:statusAction AND isFakePin =:isFakePin  ORDER BY originalName DESC LIMIT 3")
    fun loadSyncDataItems(isSyncCloud: Boolean, isDeleteLocal: Boolean, statusAction: Int, isFakePin: Boolean): MutableList<ItemEntity>?

    @Query("Select * FROM items WHERE isSyncCloud = :isSyncCloud AND isDeleteLocal = :isDeleteLocal AND statusAction =:statusAction AND isFakePin =:isFakePin AND categories_id =:categories_id")
    fun loadSyncDataItemsByCategoriesIdNull(isSyncCloud: Boolean, isDeleteLocal: Boolean, statusAction: Int, isFakePin: Boolean, categories_id: String?): MutableList<ItemEntity>?

    @Query("Select * FROM items WHERE isSyncCloud = :isSyncCloud AND isDeleteLocal = :isDeleteLocal AND statusAction =:statusAction AND isFakePin =:isFakePin")
    fun loadRequestUploadData(isSyncCloud: Boolean, isDeleteLocal: Boolean, statusAction: Int, isFakePin: Boolean): MutableList<ItemEntity>?

    @Query("Select * FROM items WHERE categories_id = :categories_id")
    fun loadRequestItemsList(categories_id: String?): MutableList<ItemEntity>?

    @Query("Select * FROM items WHERE isDeleteLocal = :isDeleteLocal AND deleteAction = :deleteAction AND isFakePin =:isFakePin")
    fun loadDeleteLocalDataItems(isDeleteLocal: Boolean, deleteAction: Int, isFakePin: Boolean): MutableList<ItemEntity>?

    @Query("Select * FROM items WHERE isDeleteLocal = :isDeleteLocal AND isDeleteGlobal = :isDeleteGlobal AND isFakePin =:isFakePin")
    fun loadDeleteLocalAndGlobalDataItems(isDeleteLocal: Boolean, isDeleteGlobal: Boolean, isFakePin: Boolean): MutableList<ItemEntity>?

    @Query("Select * FROM items WHERE  isSyncCloud = :isSyncCloud AND isFakePin =:isFakePin")
    fun loadSyncData(isSyncCloud: Boolean, isFakePin: Boolean): MutableList<ItemEntity>?

    @Query("Select * FROM items WHERE  isSyncCloud = :isSyncCloud AND isSaver =:isSaver AND isWaitingForExporting =:isWaitingForExporting AND isFakePin =:isFakePin ORDER BY originalName DESC LIMIT 1")
    fun loadSyncData(isSyncCloud: Boolean, isSaver: Boolean, isWaitingForExporting: Boolean, isFakePin: Boolean): MutableList<ItemEntity>?

    @Query("Select * FROM items WHERE  isSyncCloud = :isSyncCloud AND isSaver =:isSaver AND isFakePin =:isFakePin")
    fun loadSyncData(isSyncCloud: Boolean, isSaver: Boolean, isFakePin: Boolean): MutableList<ItemEntity>?

    @Query("Select * FROM items WHERE id = :id AND isFakePin =:isFakePin")
    fun loadItemId(id: Int, isFakePin: Boolean): ItemEntity?

    @Query("Select * FROM items WHERE items_id = :item_id")
    fun loadItemId(item_id: String?): ItemEntity?

    @Query("Select * FROM items WHERE items_id = :globalName AND isSyncCloud = :isSyncCloud AND isFakePin =:isFakePin")
    fun loadItemId(globalName: String?, isSyncCloud: Boolean, isFakePin: Boolean): ItemEntity?

    @Query("Select * FROM items WHERE isSyncCloud = :isSyncCloud AND isSyncOwnServer =:isSyncOwnServer AND isFakePin =:isFakePin")
    fun loadListItemId(isSyncCloud: Boolean, isSyncOwnServer: Boolean, isFakePin: Boolean): MutableList<ItemEntity>?

    @Query("Select * FROM items WHERE isSyncCloud = :isSyncCloud AND isFakePin =:isFakePin")
    fun loadListItemId(isSyncCloud: Boolean, isFakePin: Boolean): MutableList<ItemEntity>?

    @Query("Select * FROM items WHERE isSyncCloud = :isSyncCloud AND isFakePin =:isFakePin")
    fun loadListItem(isSyncCloud: Boolean, isFakePin: Boolean): MutableList<ItemEntity>?

    @Query("Select * FROM items WHERE items_id = :items_id AND isFakePin =:isFakePin")
    fun loadItemId(items_id: String?, isFakePin: Boolean): ItemEntity?

    @Query("Select * FROM items WHERE items_id = :items_id AND isFakePin =:isFakePin")
    fun loadListItemId(items_id: String?, isFakePin: Boolean): MutableList<ItemEntity>?

    @Query("Select * FROM items WHERE isFakePin =:isFakePin ")
    fun loadAll(isFakePin: Boolean): MutableList<ItemEntity>?

    @Query("Select * FROM items WHERE isDeleteLocal =:isDeleteLocal AND isFakePin =:isFakePin ")
    fun loadAll(isDeleteLocal: Boolean, isFakePin: Boolean): MutableList<ItemEntity>?

    @Query("Select * FROM items WHERE isSaver =:isSaver AND isSyncCloud =:isSyncCloud")
    fun loadAllSaved(isSaver: Boolean, isSyncCloud: Boolean): MutableList<ItemEntity>?

    @Query("Select * FROM items WHERE formatType =:formatType ORDER BY originalName DESC LIMIT 2")
    fun loadAllSaved(formatType: Int): MutableList<ItemEntity>?

    @Query("Select * FROM items WHERE (isUpdate =:isUpdate AND  isSyncCloud = :isSyncCloud AND isSyncOwnServer =:isSyncOwnServer AND isFakePin =:isFakePin) OR (isRequestChecking=:isRequestChecking AND isSyncCloud =:isSyncCloud)")
    fun loadListItemUpdate(isUpdate: Boolean, isSyncCloud: Boolean, isSyncOwnServer: Boolean, isFakePin: Boolean,isRequestChecking : Boolean): MutableList<ItemEntity>?
}