package co.tpcreative.supersafe.model
import java.io.Serializable
import java.util.*


class MainCategoryModel : Serializable {
    var id = 0
    var image: String? = null
    var icon: String? = null
    var categories_max: Long = 0
    var categories_local_id: String? = null
    var categories_id: String? = null
    var categories_hex_name: String? = null
    var categories_name: String? = null
    var isDelete = false
    var isChange = false
    var isSyncOwnServer = false
    var isFakePin = false
    var pin: String? = null
    var isCustom_Cover = false
    var items_id: String? = null
    var mainCategories_Local_Id: String? = null
    var unique_id: String? = null

    /*Custom field to show to view*/
    var date: Date? = null
    var isChecked = false

    /*Fetch item from local db*/
    constructor(entity: MainCategoryEntityModel) {
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
    }

    /*Added category*/
    constructor(categories_id: String?, categories_local_id: String?, categories_hex_name: String?, categories_name: String?, image: String?, icon: String?, categories_max: Long, isDelete: Boolean, isChange: Boolean, isSyncOwnServer: Boolean, isFakePin: Boolean, pin: String?, items_id: String?, mainCategories_Local_Id: String?, isCustom_Cover: Boolean) {
        this.categories_name = categories_name
        this.image = image
        this.icon = icon
        this.categories_local_id = categories_local_id
        this.categories_id = categories_id
        this.categories_hex_name = categories_hex_name
        this.categories_max = categories_max
        this.isDelete = isDelete
        this.isChange = isChange
        this.isFakePin = isFakePin
        this.pin = pin
        this.items_id = items_id
        this.mainCategories_Local_Id = mainCategories_Local_Id
        this.isCustom_Cover = isCustom_Cover
        this.isSyncOwnServer = isSyncOwnServer
    }

    constructor() {}
}