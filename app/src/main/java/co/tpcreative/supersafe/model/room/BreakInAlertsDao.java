package co.tpcreative.supersafe.model.room;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import co.tpcreative.supersafe.model.BreakInAlerts;
import co.tpcreative.supersafe.model.Items;

@Dao
public interface BreakInAlertsDao {
    @Insert
    void insert(BreakInAlerts... breakinalerts);

    @Update
    void update(BreakInAlerts... breakinalerts);

    @Delete
    void delete(BreakInAlerts... breakinalerts);

    @Query("Select * FROM breakinalerts")
    List<BreakInAlerts> loadAll();

    @Query("DELETE FROM breakinalerts")
    public void deleteBreakInAlerts();

}
