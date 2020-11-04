package co.tpcreative.supersafe.common.entities
import androidx.room.*

@Dao
interface BreakInAlertsDao {
    @Insert
    fun insert(vararg item: BreakInAlertsEntity?)

    @Update
    fun update(vararg item: BreakInAlertsEntity?)

    @Delete
    fun delete(vararg item: BreakInAlertsEntity?)

    @Query("Select * FROM breakinalerts")
    fun loadAll(): MutableList<BreakInAlertsEntity>?

    @Query("DELETE FROM breakinalerts")
    fun deleteBreakInAlerts()
}