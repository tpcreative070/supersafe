package co.tpcreative.supersafe.model
import java.io.Serializable

class AlbumCoverModel : Serializable {
    val item : ItemModel?
    val category : MainCategoryModel?
    var type : EnumTypeObject


    constructor(item : ItemModel? = null,category: MainCategoryModel? = null, type : EnumTypeObject){
        this.item = item
        this.category = category
        this.type = type
    }
}

enum class EnumTypeObject {
    ITEM,
    CATEGORY
}