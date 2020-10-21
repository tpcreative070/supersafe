package co.tpcreative.supersafe.common.response
import co.tpcreative.supersafe.model.*
import java.io.Serializable

class DataResponse : Serializable {
    /*User*/
    var user: User? = null

    /*Main category*/
    var categoriesList: MutableList<MainCategoryModel?>? = null
    var category: MainCategoryModel? = null

    /*Items*/
    var itemsList: MutableList<ItemModel?>? = null

    /*user cloud*/
    var userCloud: UserCloudResponse? = null

    /*RequestCode*/
    var requestCode: RequestCodeResponse? = null
    var author: Authorization? = null
    var premium: Premium? = null
    var email_token: EmailToken? = null
    var syncData: SyncData? = null
    var nextPage: String? = null
}