package co.tpcreative.supersafe.common.entities;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface BreakInAlertsDao {
    @Insert
    void insert(BreakInAlertsEntity... item);

    @Update
    void update(BreakInAlertsEntity... item);

    @Delete
    void delete(BreakInAlertsEntity... item);

    @Query("Select * FROM breakinalerts")
    List<BreakInAlertsEntity> loadAll();

    @Query("DELETE FROM breakinalerts")
    public void deleteBreakInAlerts();

}
