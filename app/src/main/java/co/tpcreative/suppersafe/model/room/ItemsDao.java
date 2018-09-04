package co.tpcreative.suppersafe.model.room;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import java.util.List;
import co.tpcreative.suppersafe.model.Items;

@Dao
public interface ItemsDao {
    @Insert
    void insert(Items... items);

    @Update
    void update(Items... items);

    @Delete
    void delete(Items... items);

    @Query("Select * FROM items")
    List<Items> loadAll();
}
