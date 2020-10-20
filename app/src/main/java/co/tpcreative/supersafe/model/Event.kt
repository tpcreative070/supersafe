package co.tpcreative.supersafe.model
import java.util.*

class Event(items: ItemModel, mainCategories: MainCategoryModel, mEvent: EnumEvent, date: Date) {
    private val items: ItemModel
    private val mainCategories: MainCategoryModel
    private val event: EnumEvent
    private val date: Date
    fun getDate(): Date {
        return date
    }

    fun getItems(): ItemModel {
        return items
    }

    fun getMainCategories(): MainCategoryModel {
        return mainCategories
    }

    fun getEvent(): EnumEvent {
        return this.event
    }

    init {
        this.items = items
        this.mainCategories = mainCategories
        this.event = mEvent
        this.date = date
    }
}