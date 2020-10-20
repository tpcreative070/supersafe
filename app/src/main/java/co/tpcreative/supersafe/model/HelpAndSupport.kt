package co.tpcreative.supersafe.model
import java.io.Serializable

class HelpAndSupport : Serializable {
    var categories: Categories?
    var title: String?
    var content: String?
    var nummberName: String?

    constructor(categories: Categories?, title: String?, content: String?, nummberName: String?) {
        this.categories = categories
        this.title = title
        this.content = content
        this.nummberName = nummberName
    }

    constructor() {
        categories = Categories()
        title = ""
        content = ""
        nummberName = null
    }

    fun getCategoryId(): Int? {
        return categories?.id
    }

    fun getCategoryName(): String? {
        return categories?.name
    }
}