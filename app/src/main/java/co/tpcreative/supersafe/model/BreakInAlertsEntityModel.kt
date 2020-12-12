package co.tpcreative.supersafe.model
import co.tpcreative.supersafe.common.entities.BreakInAlertsEntity
import java.io.Serializable

class BreakInAlertsEntityModel : Serializable {
    var id: Int
    var fileName: String?
    var time: Long
    var pin: String?

    constructor(index: BreakInAlertsEntity) {
        id = index.id
        fileName = index.fileName
        time = index.time
        pin = index.pin
    }

    constructor(index: BreakInAlertsModel) {
        id = index.id
        fileName = index.fileName
        time = index.time
        pin = index.pin
    }
}