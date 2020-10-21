package co.tpcreative.supersafe.common.entities
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import co.tpcreative.supersafe.model.MainCategoryEntityModel
import java.io.Serializable
import java.util.*

@Entity(tableName = "maincategories")
class MainCategoryEntity : Serializable {
    @PrimaryKey(autoGenerate = true)
    var id = 0
    var image: String?
    var icon: String? = null
    var categories_max: Long
    var categories_local_id: String?
    var categories_id: String?
    var categories_hex_name: String?
    var categories_name: String?
    var isDelete: Boolean
    var isChange: Boolean
    var isSyncOwnServer = false
    var isFakePin: Boolean
    var pin: String?
    var isCustom_Cover: Boolean
    var items_id: String?
    var mainCategories_Local_Id: String?

    @Ignore
    var date: Date? = null

    @Ignore
    var isChecked = false

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

    @Ignore
    constructor() {
        image = null
        categories_name = null
        categories_local_id = null
        categories_id = null
        categories_hex_name = null
        categories_max = 0
        isDelete = false
        isChange = false
        isFakePin = false
        pin = ""
        items_id = null
        mainCategories_Local_Id = null
        isCustom_Cover = false
    }

    /*Delete category*/
    constructor(entityModel: MainCategoryEntityModel) {
        id = entityModel.id
        image = entityModel.image
        icon = entityModel.icon
        categories_max = entityModel.categories_max
        categories_local_id = entityModel.categories_local_id
        categories_id = entityModel.categories_id
        categories_hex_name = entityModel.categories_hex_name
        categories_name = entityModel.categories_name
        isDelete = entityModel.isDelete
        isChange = entityModel.isChange
        isSyncOwnServer = entityModel.isSyncOwnServer
        isFakePin = entityModel.isFakePin
        if (entityModel.pin == null) {
            entityModel.pin = ""
        }
        pin = entityModel.pin
        isCustom_Cover = entityModel.isCustom_Cover
        items_id = entityModel.items_id
        mainCategories_Local_Id = entityModel.mainCategories_Local_Id
    }

    companion object {
        @Ignore
        private var instance: MainCategoryEntity? = null

        @Ignore
        fun getInstance(): MainCategoryEntity? {
            if (instance == null) {
                instance = MainCategoryEntity()
            }
            return instance
        }

        /*Send data to camera action*/
        private val TAG = MainCategoryEntity::class.java.simpleName
    }
}