package co.tpcreative.suppersafe.model.room;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import java.util.List;
import co.tpcreative.suppersafe.model.Save;

@Dao
public interface SaveDao {
    @Insert
    void insert(Save... saves);

    @Update
    void update(Save... saves);

    @Delete
    void delete(Save... saves);

    @Query("Select * FROM save")
    List<Save> loadAll();
}
