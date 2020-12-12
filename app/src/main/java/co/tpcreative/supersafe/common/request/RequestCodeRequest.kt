package co.tpcreative.supersafe.common.request
import java.io.Serializable

class RequestCodeRequest(var user_id: String?, var session_token: String?, var device_id: String?) : Serializable