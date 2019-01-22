package co.tpcreative.supersafe.model.room;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

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
