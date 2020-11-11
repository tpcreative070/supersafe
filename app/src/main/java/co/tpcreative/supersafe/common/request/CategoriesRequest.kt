package co.tpcreative.supersafe.common.request
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.MainCategoryModel
import java.io.Serializable

class CategoriesRequest : Serializable {
    var user_id: String?
    var cloud_id: String?
    var device_id: String?
    var categories_id: String? = null
    var categories_name: String? = null
    var categories_hex_name: String? = null
    var icon: String? = null
    var image: String? = null
    var categories_max: Long = 0
    var isDelete = false
    var isChange = false
    var isSyncOwnServer = false

    /*Get list*/
    constructor(user_id: String?, cloud_id: String?, device_id: String?) {
        this.user_id = user_id
        this.cloud_id = cloud_id
        this.device_id = device_id
    }

    /*Delete category*/
    constructor(user_id: String?, cloud_id: String?, device_id: String?, categories_id: String?) {
        this.user_id = user_id
        this.cloud_id = cloud_id
        this.device_id = device_id
        this.categories_id = categories_id
    }

    /*Sync category*/
    constructor(user_id: String?, cloud_id: String?, device_id: String?, mainCategories: MainCategoryModel) {
        this.user_id = user_id
        this.cloud_id = cloud_id
        this.device_id = device_id
        categories_id = mainCategories.categories_id
        categories_name = mainCategories.categories_name
        categories_hex_name = mainCategories.categories_hex_name
        icon = mainCategories.icon
        image = mainCategories.image
        categories_max = mainCategories.categories_max
        isDelete = mainCategories.isDelete
        isChange = mainCategories.isChange
        isSyncOwnServer = mainCategories.isSyncOwnServer
    }

    /*Sync category*/
    constructor(mainCategories: MainCategoryModel) {
        this.user_id = Utils.getUserId()
        this.cloud_id = Utils.getUserCloudId()
        this.device_id = SuperSafeApplication.getInstance().getDeviceId()
        categories_id = mainCategories.categories_id
        categories_name = mainCategories.categories_name
        categories_hex_name = mainCategories.categories_hex_name
        icon = mainCategories.icon
        image = mainCategories.image
        categories_max = mainCategories.categories_max
        isDelete = mainCategories.isDelete
        isChange = mainCategories.isChange
        isSyncOwnServer = mainCategories.isSyncOwnServer
    }
}