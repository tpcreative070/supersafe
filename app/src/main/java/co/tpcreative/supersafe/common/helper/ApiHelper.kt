package co.tpcreative.supersafe.common.helper
import co.tpcreative.supersafe.common.request.SyncItemsRequest
import co.tpcreative.supersafe.common.response.RootResponse
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import okhttp3.MultipartBody
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.Part
import retrofit2.http.Query

class ApiHelper() {
    suspend fun onListFilesSyncCor(data : SyncItemsRequest) =   SuperSafeApplication.serverApiCor?.onListFilesSyncCor(data)
    suspend fun uploadFileMultipleInAppFolderCor(@Header("Authorization") authToken: String?,
                        @Part metaPart: MultipartBody.Part?,
                        @Part dataPart: MultipartBody.Part?,
                        @Query("type") type: String?)  =   SuperSafeApplication.serverDriveApi?.uploadFileMultipleInAppFolderCor(authToken,metaPart,dataPart,type)
    companion object {
        private var mInstance : ApiHelper? = null
        fun getInstance() : ApiHelper? {
            if (mInstance==null){
                mInstance = ApiHelper()
            }
            return mInstance
        }
    }
}