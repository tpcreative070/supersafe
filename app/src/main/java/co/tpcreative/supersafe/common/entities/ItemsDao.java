package co.tpcreative.supersafe.common.entities;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface ItemsDao {
    @Insert
    void insert(ItemEntity... items);

    @Update
    void update(ItemEntity... items);

    @Delete
    void delete(ItemEntity... items);

    @Query("DELETE FROM items")
    public void deleteAllItems();

    @Query("Delete from items  WHERE categories_local_id = :categories_local_id AND isFakePin =:isFakePin")
    void deleteAll(String categories_local_id,boolean isFakePin);

    @Query("Select * FROM items WHERE categories_local_id = :categories_local_id AND isFakePin =:isFakePin")
    List<ItemEntity> loadAll(String categories_local_id, boolean isFakePin);

    @Query("Select * FROM items WHERE categories_local_id = :categories_local_id AND formatType =:formatType AND isDeleteLocal =:isDeleteLocal AND isFakePin =:isFakePin ORDER BY originalName DESC")
    List<ItemEntity> loadAll(String categories_local_id, int formatType, boolean isDeleteLocal, boolean isFakePin);


    @Query("Select * FROM items WHERE categories_local_id = :categories_local_id AND isDeleteLocal =:isDeleteLocal AND isExport =:isExport AND isFakePin =:isFakePin ORDER BY originalName DESC")
    List<ItemEntity> loadAll(String categories_local_id, boolean isDeleteLocal, boolean isExport, boolean isFakePin);

    @Query("Select * FROM items WHERE categories_local_id = :categories_local_id AND isDeleteLocal =:isDeleteLocal AND isFakePin =:isFakePin ORDER BY originalName DESC")
    List<ItemEntity> loadAll(String categories_local_id, boolean isDeleteLocal, boolean isFakePin);

    @Query("Select * FROM items WHERE categories_local_id = :categories_local_id AND isDeleteLocal =:isDeleteLocal AND isFakePin =:isFakePin ORDER BY originalName DESC LIMIT 1")
    ItemEntity getLatestId(String categories_local_id, boolean isDeleteLocal, boolean isFakePin);

    @Query("Select * FROM items WHERE isSyncCloud = :isSyncCloud AND isDeleteLocal = :isDeleteLocal AND statusAction =:statusAction AND isFakePin =:isFakePin  ORDER BY originalName DESC LIMIT 3")
    List<ItemEntity> loadSyncDataItems(boolean isSyncCloud, boolean isDeleteLocal, int statusAction, boolean isFakePin);


    @Query("Select * FROM items WHERE isSyncCloud = :isSyncCloud AND isDeleteLocal = :isDeleteLocal AND statusAction =:statusAction AND isFakePin =:isFakePin AND categories_id =:categories_id")
    List<ItemEntity> loadSyncDataItemsByCategoriesIdNull(boolean isSyncCloud, boolean isDeleteLocal, int statusAction, boolean isFakePin, String categories_id);


    @Query("Select * FROM items WHERE isDeleteLocal = :isDeleteLocal AND deleteAction = :deleteAction AND isFakePin =:isFakePin")
    List<ItemEntity> loadDeleteLocalDataItems(boolean isDeleteLocal, int deleteAction, boolean isFakePin);


    @Query("Select * FROM items WHERE isDeleteLocal = :isDeleteLocal AND isDeleteGlobal = :isDeleteGlobal AND isFakePin =:isFakePin")
    List<ItemEntity> loadDeleteLocalAndGlobalDataItems(boolean isDeleteLocal, boolean isDeleteGlobal, boolean isFakePin);


    @Query("Select * FROM items WHERE  isSyncCloud = :isSyncCloud AND isFakePin =:isFakePin")
    List<ItemEntity> loadSyncData(boolean isSyncCloud, boolean isFakePin);

    @Query("Select * FROM items WHERE  isSyncCloud = :isSyncCloud AND isSaver =:isSaver AND isWaitingForExporting =:isWaitingForExporting AND isFakePin =:isFakePin ORDER BY originalName DESC LIMIT 1")
    List<ItemEntity> loadSyncData(boolean isSyncCloud, boolean isSaver, boolean isWaitingForExporting, boolean isFakePin);

    @Query("Select * FROM items WHERE  isSyncCloud = :isSyncCloud AND isSaver =:isSaver AND isFakePin =:isFakePin")
    List<ItemEntity> loadSyncData(boolean isSyncCloud, boolean isSaver, boolean isFakePin);

    @Query("Select * FROM items WHERE id = :id AND isFakePin =:isFakePin")
    ItemEntity loadItemId(int id, boolean isFakePin);

    @Query("Select * FROM items WHERE items_id = :item_id")
    ItemEntity loadItemId(String item_id);

    @Query("Select * FROM items WHERE items_id = :globalName AND isSyncCloud = :isSyncCloud AND isFakePin =:isFakePin")
    ItemEntity loadItemId(String globalName, boolean isSyncCloud, boolean isFakePin);

    @Query("Select * FROM items WHERE isSyncCloud = :isSyncCloud AND isSyncOwnServer =:isSyncOwnServer AND isFakePin =:isFakePin")
    List<ItemEntity> loadListItemId(boolean isSyncCloud, boolean isSyncOwnServer, boolean isFakePin);

    @Query("Select * FROM items WHERE isSyncCloud = :isSyncCloud AND isFakePin =:isFakePin")
    List<ItemEntity> loadListItemId(boolean isSyncCloud, boolean isFakePin);

    @Query("Select * FROM items WHERE isSyncCloud = :isSyncCloud AND isFakePin =:isFakePin")
    List<ItemEntity> loadListItem(boolean isSyncCloud, boolean isFakePin);

    @Query("Select * FROM items WHERE items_id = :items_id AND isFakePin =:isFakePin")
    ItemEntity loadItemId(String items_id, boolean isFakePin);

    @Query("Select * FROM items WHERE items_id = :items_id AND isFakePin =:isFakePin")
    List<ItemEntity>loadListItemId(String items_id, boolean isFakePin);

    @Query("Select * FROM items WHERE isFakePin =:isFakePin ")
    List<ItemEntity> loadAll(boolean isFakePin);

    @Query("Select * FROM items WHERE isDeleteLocal =:isDeleteLocal AND isFakePin =:isFakePin ")
    List<ItemEntity> loadAll(boolean isDeleteLocal, boolean isFakePin);

    @Query("Select * FROM items WHERE isSaver =:isSaver AND isSyncCloud =:isSyncCloud")
    List<ItemEntity> loadAllSaved(boolean isSaver, boolean isSyncCloud);

    @Query("Select * FROM items WHERE formatType =:formatType ORDER BY originalName DESC LIMIT 2")
    List<ItemEntity> loadAllSaved(int formatType);

    @Query("Select * FROM items WHERE isUpdate =:isUpdate AND  isSyncCloud = :isSyncCloud AND isSyncOwnServer =:isSyncOwnServer AND isFakePin =:isFakePin")
    List<ItemEntity> loadListItemUpdate(boolean isUpdate, boolean isSyncCloud, boolean isSyncOwnServer, boolean isFakePin);
}
