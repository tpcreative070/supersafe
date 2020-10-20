package co.tpcreative.supersafe.common.response
import co.tpcreative.supersafe.common.api.response.BaseResponse
import co.tpcreative.supersafe.model.ItemModel
import co.tpcreative.supersafe.model.MainCategoryModel
import java.io.Serializable

class SyncResponse : BaseResponse(), Serializable {
    var files: MutableList<ItemModel?>? = null
    var listCategories: MutableList<MainCategoryModel?>? = null
}