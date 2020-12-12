package co.tpcreative.supersafe.model
import co.tpcreative.supersafe.common.entities.MainCategoryEntity
import co.tpcreative.supersafe.common.util.Utils
import java.io.Serializable

class MainCategoryEntityModel : Serializable {
    var id: Int
    var image: String?
    var icon: String?
    var categories_max: Long
    var categories_local_id: String?
    var categories_id: String?
    var categories_hex_name: String?
    var categories_name: String?
    var isDelete: Boolean
    var isChange: Boolean
    var isSyncOwnServer: Boolean
    var isFakePin: Boolean
    var pin: String?
    var isCustom_Cover: Boolean
    var items_id: String?
    var mainCategories_Local_Id: String?
    var unique_id: String?

    /*Added more fields*/
    var created_date : String? = null
    var updated_date : String? = null
    var date_time  : String? = null

    /*Fetch data from local db*/
    constructor(entity: MainCategoryEntity) {
        id = entity.id
        image = entity.image
        icon = entity.icon
        categories_max = entity.categories_max
        categories_local_id = entity.categories_local_id
        categories_id = entity.categories_id
        categories_hex_name = entity.categories_hex_name
        categories_name = entity.categories_name
        isDelete = entity.isDelete
        isChange = entity.isChange
        isSyncOwnServer = entity.isSyncOwnServer
        isFakePin = entity.isFakePin
        pin = entity.pin
        isCustom_Cover = entity.isCustom_Cover
        items_id = entity.items_id
        mainCategories_Local_Id = entity.mainCategories_Local_Id
        unique_id = Utils.getUUId()
        created_date = entity.created_date
        updated_date = entity.updated_date
        date_time = entity.date_time
    }

    /*push data to local db*/
    constructor(entity: MainCategoryModel) {
        id = entity.id
        image = entity.image
        icon = entity.icon
        categories_max = entity.categories_max
        categories_local_id = entity.categories_local_id
        categories_id = entity.categories_id
        categories_hex_name = entity.categories_hex_name
        categories_name = entity.categories_name
        isDelete = entity.isDelete
        isChange = entity.isChange
        isSyncOwnServer = entity.isSyncOwnServer
        isFakePin = entity.isFakePin
        if (entity.pin == null) {
            entity.pin = ""
        }
        pin = entity.pin
        isCustom_Cover = entity.isCustom_Cover
        items_id = entity.items_id
        mainCategories_Local_Id = entity.mainCategories_Local_Id
        unique_id = entity.unique_id
        created_date = entity.created_date
        updated_date = entity.updated_date
        date_time = entity.date_time
    }
}