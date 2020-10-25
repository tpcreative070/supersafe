package co.tpcreative.supersafe.model
import co.tpcreative.supersafe.common.util.Utils
import java.io.Serializable

class ImportFilesModel(mainCategories: MainCategoryModel?, mimeTypeFile: MimeTypeFile?, path: String?, position: Int, isImport: Boolean) : Serializable {
    var mainCategories: MainCategoryModel?
    var path: String?
    var mimeTypeFile: MimeTypeFile?
    var position: Int
    var isImport: Boolean
    var unique_id: String?

    init {
        this.mainCategories = mainCategories
        this.mimeTypeFile = mimeTypeFile
        this.path = path
        this.position = position
        this.isImport = isImport
        unique_id = Utils.getUUId()
    }
}