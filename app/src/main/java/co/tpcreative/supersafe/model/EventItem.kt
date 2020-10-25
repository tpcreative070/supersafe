package co.tpcreative.supersafe.model

class EventItem(private val event: Event, enumEvent: EnumEvent) : ListItem() {
    var enumEvent: EnumEvent
    fun getEvent(): Event {
        return event
    }

    // here getters and setters
    // for title and so on, built
    // using event
    override fun getType(): Int {
        return ListItem.Companion.TYPE_EVENT
    }

    override fun getTypeEvent(): EnumEvent? {
        return enumEvent
    }

    init {
        this.enumEvent = enumEvent
    }
}