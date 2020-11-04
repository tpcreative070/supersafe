package co.tpcreative.supersafe.model
abstract class ListItem {
    abstract fun getType(): Int
    abstract fun getTypeEvent(): EnumEvent?
    companion object {
        const val TYPE_HEADER = 0
        const val TYPE_EVENT = 1
    }
}