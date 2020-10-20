package co.tpcreative.supersafe.common.response

import java.io.Serializable

class DriveResponse : Serializable {
    var kind: String? = null
    var id: String? = null
    var name: String? = null
    var mimeType: String? = null
    var description: String? = null
    var global_original_id: String? = null
    var global_thumbnail_id: String? = null
    var items_id: String? = null
    var categories_id: String? = null
}