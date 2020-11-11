package co.tpcreative.supersafe.model

enum class EnumResponseCode (val code :Int){
    BAD_REQUEST(400),
    INVALID_AUTHENTICATION(401),
    FORBIDDEN(403),
    NOT_FOUND(404)
}