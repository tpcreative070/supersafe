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

    @Query("Select * FROM items WHERE localCategories_Id = :localCategories_Id")
    List<Items> loadAll(String localCategories_Id);

    @Query("Select * FROM items WHERE isSync = :isSync AND statusAction =:statusAction")
    List<Items> loadSyncDataItems(boolean isSync,int statusAction);

    @Query("Select * FROM items WHERE id = :id")
    Items loadItemId(int id);


    @Query("Select * FROM items WHERE globalName = :globalName")
    Items loadItemId(String globalName);

    @Query("Select * FROM items WHERE local_id = :local_id")
    Items loadLocalId(String local_id);


    @Query("Select * FROM items")
    List<Items> loadAll();

}
