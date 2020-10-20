package co.tpcreative.supersafe.model
import co.tpcreative.supersafe.modelimport.ListItem
import java.util.*

class HeaderItem(private val date: Date) : ListItem() {
    fun getDate(): Date {
        return date
    }

    // here getters and setters
    // for title and so on, built
    // using date
    override fun getType(): Int {
        return ListItem.Companion.TYPE_HEADER
    }

    override fun getTypeEvent(): EnumEvent? {
        return null
    }

}