package co.tpcreative.supersafe.common.response
import co.tpcreative.supersafe.common.api.response.BaseResponse
import co.tpcreative.supersafe.model.MainCategoryModel
import java.io.Serializable

class SyncCategoriesResponse : BaseResponse(), Serializable {
    var files: MutableList<MainCategoryModel?>? = null
    var category: MainCategoryModel? = null
}