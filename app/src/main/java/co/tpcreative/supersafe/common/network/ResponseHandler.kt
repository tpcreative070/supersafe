package co.tpcreative.supersafe.common.network
import co.tpcreative.supersafe.common.api.response.BaseResponse
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.extension.toJson
import co.tpcreative.supersafe.common.extension.toObjectMayBeNull
import co.tpcreative.supersafe.common.util.Utils
import okhttp3.ResponseBody
import retrofit2.HttpException
import java.io.IOException
import kotlin.Exception

enum class ErrorCodes(val code: Int) {
    SocketTimeOut(-1),
}
open class ResponseHandler {
    companion object{
        val TAG = ResponseHandler::class.java.simpleName
        fun <T : Any> handleSuccess(data: T): Resource<T> {
            return Resource.success(data)
        }
        fun <T : Any> handleException(e: Exception? = null): Resource<T> {
            return if (e is HttpException) {
                val mBody: ResponseBody? = (e as HttpException?)?.response()?.errorBody()
                val mCode = (e as HttpException?)?.response()?.code()
                try {
                    val mMessage = mBody?.string()
                    val mObject = mMessage?.toObjectMayBeNull(BaseResponse::class.java)
                    Utils.Log(TAG,mMessage)
                    getErrorCode(mCode!!)
                    mObject?.let {
                        Resource.error(mCode,it.toJson(), null)
                    } ?:
                    Resource.error(mCode,mMessage ?: "Unknown", null)
                } catch (e: IOException) {
                    e.printStackTrace()
                    Resource.error(mCode!!,e.message!!, null)
                }
            } else {
                val mMessage = getErrorMessage(ErrorCodes.SocketTimeOut.code)
                Utils.Log(TAG,mMessage)
                Resource.error(ErrorCodes.SocketTimeOut.code,mMessage, null)
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

        private fun getErrorCode(code: Int){
             when (code) {
                400 -> Utils.Log(TAG ,"Bad request")
                401 -> {
                    Utils.Log(TAG ,"Unauthorised")
                    if (ServiceManager.getInstance()?.isRequestingUpdatedUserToken()!!){
                        ServiceManager.getInstance()?.updatedUserToken()
                    }
                }
                403 -> Utils.Log(TAG,"Forbidden")
                404 -> Utils.Log(TAG ,"Not found")
                405 -> Utils.Log(TAG ,"Method not allowed")
                500 -> Utils.Log(TAG ,"Internal server error")
                else -> Utils.Log(TAG ,"Something went wrong")
            }
        }
    }
}