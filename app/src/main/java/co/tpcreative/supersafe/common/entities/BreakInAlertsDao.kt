package co.tpcreative.supersafe.common.entities
import androidx.room.*
import co.tpcreative.supersafe.common.entities.BreakInAlertsEntity

@Dao
interface BreakInAlertsDao {
    @Insert
    open fun insert(vararg item: BreakInAlertsEntity?)

    @Update
    open fun update(vararg item: BreakInAlertsEntity?)

    @Delete
    open fun delete(vararg item: BreakInAlertsEntity?)

    @Query("Select * FROM breakinalerts")
    open fun loadAll(): MutableList<BreakInAlertsEntity>?

    @Query("DELETE FROM breakinalerts")
    open fun deleteBreakInAlerts()
}