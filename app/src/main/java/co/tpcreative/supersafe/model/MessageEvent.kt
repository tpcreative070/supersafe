package co.tpcreative.supersafe.model

class MessageEvent {
    var enumStatus: EnumStatus?
    var `object`: String?

    init {
        enumStatus = EnumStatus.OTHER
        `object` = ""
    }
}