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

    @Query("Delete from items  WHERE categories_local_id = :categories_local_id")
    void deleteAll(String categories_local_id);

    @Query("Select * FROM items WHERE categories_local_id = :categories_local_id AND isDeleteLocal =:isDeleteLocal ORDER BY id DESC")
    List<Items> loadAll(String categories_local_id,boolean isDeleteLocal);

    @Query("Select * FROM items WHERE categories_local_id = :categories_local_id AND isDeleteLocal =:isDeleteLocal ORDER BY id DESC LIMIT 1")
    Items getLatestId(String categories_local_id,boolean isDeleteLocal);

    @Query("Select * FROM items WHERE isSyncCloud = :isSyncCloud AND isDeleteLocal = :isDeleteLocal AND statusAction =:statusAction ORDER BY id DESC LIMIT 3")
    List<Items> loadSyncDataItems(boolean isSyncCloud,boolean isDeleteLocal,int statusAction);


    @Query("Select * FROM items WHERE isDeleteLocal = :isDeleteLocal AND deleteAction = :deleteAction")
    List<Items> loadDeleteLocalDataItems(boolean isDeleteLocal,int deleteAction);


    @Query("Select * FROM items WHERE isDeleteLocal = :isDeleteLocal AND isDeleteGlobal = :isDeleteGlobal")
    List<Items> loadDeleteLocalAndGlobalDataItems(boolean isDeleteLocal,boolean isDeleteGlobal);

    @Query("Select * FROM items WHERE id = :id")
    Items loadItemId(int id);

    @Query("Select * FROM items WHERE items_id = :globalName AND isSyncCloud = :isSyncCloud")
    Items loadItemId(String globalName,boolean isSyncCloud);

    @Query("Select * FROM items WHERE isSyncCloud = :isSyncCloud AND isSyncOwnServer =:isSyncOwnServer")
    List<Items> loadListItemId(boolean isSyncCloud,boolean isSyncOwnServer);

    @Query("Select * FROM items WHERE isSyncCloud = :isSyncCloud")
    List<Items> loadListItemId(boolean isSyncCloud);

    @Query("Select * FROM items WHERE items_id = :items_id")
    Items loadItemId(String items_id);

    @Query("Select * FROM items WHERE items_id = :items_id")
    List<Items>loadListItemId(String items_id);

    @Query("Select * FROM items WHERE local_id = :local_id")
    Items loadLocalId(String local_id);

    @Query("Select * FROM items")
    List<Items> loadAll();

}
