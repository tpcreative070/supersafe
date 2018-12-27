package co.tpcreative.supersafe.model.room;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import java.util.List;
import co.tpcreative.supersafe.model.Items;

@Dao
public interface ItemsDao {
    @Insert
    void insert(Items... items);

    @Update
    void update(Items... items);

    @Delete
    void delete(Items... items);

    @Query("Delete from items  WHERE categories_local_id = :categories_local_id AND isFakePin =:isFakePin")
    void deleteAll(String categories_local_id,boolean isFakePin);

    @Query("Select * FROM items WHERE categories_local_id = :categories_local_id AND isFakePin =:isFakePin")
    List<Items> loadAll(String categories_local_id,boolean isFakePin);

    @Query("Select * FROM items WHERE categories_local_id = :categories_local_id AND formatType =:formatType AND isDeleteLocal =:isDeleteLocal AND isFakePin =:isFakePin ORDER BY id DESC")
    List<Items> loadAll(String categories_local_id,int formatType,boolean isDeleteLocal,boolean isFakePin);


    @Query("Select * FROM items WHERE categories_local_id = :categories_local_id AND isDeleteLocal =:isDeleteLocal AND isExport =:isExport AND isFakePin =:isFakePin ORDER BY id DESC")
    List<Items> loadAll(String categories_local_id,boolean isDeleteLocal,boolean isExport,boolean isFakePin);

    @Query("Select * FROM items WHERE categories_local_id = :categories_local_id AND isDeleteLocal =:isDeleteLocal AND isFakePin =:isFakePin ORDER BY id DESC")
    List<Items> loadAll(String categories_local_id,boolean isDeleteLocal,boolean isFakePin);

    @Query("Select * FROM items WHERE categories_local_id = :categories_local_id AND isDeleteLocal =:isDeleteLocal AND isFakePin =:isFakePin ORDER BY id DESC LIMIT 1")
    Items getLatestId(String categories_local_id,boolean isDeleteLocal,boolean isFakePin);

    @Query("Select * FROM items WHERE isSyncCloud = :isSyncCloud AND isDeleteLocal = :isDeleteLocal AND statusAction =:statusAction AND isFakePin =:isFakePin  ORDER BY id DESC LIMIT 3")
    List<Items> loadSyncDataItems(boolean isSyncCloud,boolean isDeleteLocal,int statusAction,boolean isFakePin);


    @Query("Select * FROM items WHERE isSyncCloud = :isSyncCloud AND isDeleteLocal = :isDeleteLocal AND statusAction =:statusAction AND isFakePin =:isFakePin AND categories_id =:categories_id")
    List<Items> loadSyncDataItemsByCategoriesIdNull(boolean isSyncCloud,boolean isDeleteLocal,int statusAction,boolean isFakePin,String categories_id);


    @Query("Select * FROM items WHERE isDeleteLocal = :isDeleteLocal AND deleteAction = :deleteAction AND isFakePin =:isFakePin")
    List<Items> loadDeleteLocalDataItems(boolean isDeleteLocal,int deleteAction,boolean isFakePin);


    @Query("Select * FROM items WHERE isDeleteLocal = :isDeleteLocal AND isDeleteGlobal = :isDeleteGlobal AND isFakePin =:isFakePin")
    List<Items> loadDeleteLocalAndGlobalDataItems(boolean isDeleteLocal,boolean isDeleteGlobal,boolean isFakePin);


    @Query("Select * FROM items WHERE  isSyncCloud = :isSyncCloud AND isFakePin =:isFakePin")
    List<Items> loadSyncData(boolean isSyncCloud,boolean isFakePin);

    @Query("Select * FROM items WHERE  isSyncCloud = :isSyncCloud AND isSaver =:isSaver AND isWaitingForExporting =:isWaitingForExporting AND isFakePin =:isFakePin ORDER BY id DESC LIMIT 1")
    List<Items> loadSyncData(boolean isSyncCloud,boolean isSaver,boolean isWaitingForExporting,boolean isFakePin);

    @Query("Select * FROM items WHERE  isSyncCloud = :isSyncCloud AND isSaver =:isSaver AND isFakePin =:isFakePin")
    List<Items> loadSyncData(boolean isSyncCloud,boolean isSaver,boolean isFakePin);

    @Query("Select * FROM items WHERE id = :id AND isFakePin =:isFakePin")
    Items loadItemId(int id,boolean isFakePin);

    @Query("Select * FROM items WHERE items_id = :item_id")
    Items loadItemId(String item_id);

    @Query("Select * FROM items WHERE items_id = :globalName AND isSyncCloud = :isSyncCloud AND isFakePin =:isFakePin")
    Items loadItemId(String globalName,boolean isSyncCloud,boolean isFakePin);

    @Query("Select * FROM items WHERE isSyncCloud = :isSyncCloud AND isSyncOwnServer =:isSyncOwnServer AND isFakePin =:isFakePin")
    List<Items> loadListItemId(boolean isSyncCloud,boolean isSyncOwnServer,boolean isFakePin);

    @Query("Select * FROM items WHERE isSyncCloud = :isSyncCloud AND isFakePin =:isFakePin")
    List<Items> loadListItemId(boolean isSyncCloud,boolean isFakePin);

    @Query("Select * FROM items WHERE items_id = :items_id AND isFakePin =:isFakePin")
    Items loadItemId(String items_id,boolean isFakePin);

    @Query("Select * FROM items WHERE items_id = :items_id AND isFakePin =:isFakePin")
    List<Items>loadListItemId(String items_id,boolean isFakePin);

    @Query("Select * FROM items WHERE isFakePin =:isFakePin ")
    List<Items> loadAll(boolean isFakePin);

    @Query("Select * FROM items WHERE isDeleteLocal =:isDeleteLocal AND isFakePin =:isFakePin ")
    List<Items> loadAll(boolean isDeleteLocal,boolean isFakePin);

    @Query("Select * FROM items WHERE isSaver =:isSaver AND isSyncCloud =:isSyncCloud")
    List<Items> loadAllSaved(boolean isSaver,boolean isSyncCloud);

    @Query("Select * FROM items WHERE formatType =:formatType ORDER BY id DESC LIMIT 2")
    List<Items> loadAllSaved(int formatType);


}
