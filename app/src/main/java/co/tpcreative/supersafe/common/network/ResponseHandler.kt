package co.tpcreative.supersafe.common.network
import co.tpcreative.supersafe.common.util.Utils
import retrofit2.HttpException
import java.lang.Exception
import java.net.SocketTimeoutException
enum class ErrorCodes(val code: Int) {
    SocketTimeOut(-1)
}
open class ResponseHandler {
    companion object{
        fun <T : Any> handleSuccess(data: T,code : Int): Resource<T> {
            return Resource.success(data,code)
        }

        fun <T : Any> handleException(e: Exception? = null, code : Int? = 0): Resource<T> {
            Utils.Log("TAG",e?.message)
            return when (e) {
                is HttpException -> Resource.error(e.code(),getErrorMessage(e.code()), null)
                is SocketTimeoutException -> Resource.error(ErrorCodes.SocketTimeOut.code,getErrorMessage(ErrorCodes.SocketTimeOut.code), null)
                else -> Resource.error(code ?: 0,getErrorMessage(code ?:0), null)
            }
        }

        private fun getErrorMessage(code: Int): String {
            return when (code) {
                ErrorCodes.SocketTimeOut.code -> "Timeout"
                400 -> "Bad request"
                401 -> "Unauthorised"
                403 -> "Forbidden"
                404 -> "Not found"
                405 -> "Method not allowed"
                500 -> "Internal server error"
                else -> "Something went wrong"
            }
        }
    }
}