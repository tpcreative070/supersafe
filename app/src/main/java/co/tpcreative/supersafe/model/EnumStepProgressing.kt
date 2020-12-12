package co.tpcreative.supersafe.model

enum class EnumStepProgressing {
    VERIFY_CODE,
    SEND_CODE,
    RESEND_CODE,
    REQUEST_CODE,
    CHANGE_EMAIL,
    UNLOCK_ALBUMS,
    NONE,
    DOWNLOADING,
    EXPORTING
}