package co.tpcreative.suppersafe.model.room;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import java.util.List;
import co.tpcreative.suppersafe.model.History;

@Dao
public interface HistoryDao {
    @Insert
    void insert(History... history);

    @Update
    void update(History... history);

    @Delete
    void delete(History... history);

    @Query("Select * FROM history")
    List<History> loadAll();
}
