package co.tpcreative.supersafe.model
import java.io.Serializable

class BreakInAlertsModel : Serializable {
    var id = 0
    var fileName: String? = null
    var time: Long = 0
    var pin: String? = null

    constructor(index: BreakInAlertsEntityModel) {
        id = index.id
        fileName = index.fileName
        time = index.time
        pin = index.pin
    }

    constructor() {}
}