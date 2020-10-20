package co.tpcreative.supersafe.common.entities
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import co.tpcreative.supersafe.model.BreakInAlertsEntityModel
import java.io.Serializable

@Entity(tableName = "breakinalerts")
class BreakInAlertsEntity : Serializable {
    @PrimaryKey(autoGenerate = true)
    var id = 0
    var fileName: String?
    var time: Long
    var pin: String?

    @Ignore
    constructor() {
        fileName = null
        time = 0
        pin = null
    }

    @Ignore
    constructor(value: BreakInAlertsEntityModel) {
        id = value?.id
        fileName = value?.fileName
        time = value?.time
        pin = value?.pin
    }

    constructor(id: Int, fileName: String?, time: Long, pin: String?) {
        this.id = id
        this.fileName = fileName
        this.time = time
        this.pin = pin
    }

    companion object {
        @Ignore
        private var instance: BreakInAlertsEntity? = null

        @Ignore
        fun getInstance(): BreakInAlertsEntity? {
            if (instance == null) {
                instance = BreakInAlertsEntity()
            }
            return instance
        }
    }
}