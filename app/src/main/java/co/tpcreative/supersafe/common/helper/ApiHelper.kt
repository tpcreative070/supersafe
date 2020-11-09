package co.tpcreative.supersafe.common.helper
import co.tpcreative.supersafe.common.api.ApiService
import co.tpcreative.supersafe.common.request.SyncItemsRequest
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import okhttp3.MultipartBody
import retrofit2.http.Header
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

class ApiHelper() {
    suspend fun onListFilesSyncCor(data : SyncItemsRequest) =   SuperSafeApplication.serverApiCor?.onListFilesSyncCor(data)
    suspend fun uploadFileMultipleInAppFolderCor(@Header("Authorization") authToken: String?,
                        @Part metaPart: MultipartBody.Part?,
                        @Part dataPart: MultipartBody.Part?,
                        @Query("type") type: String?)  =   SuperSafeApplication.serverDriveApi?.uploadFileMultipleInAppFolderCor(authToken,metaPart,dataPart,type)

    suspend fun downloadDriveFileCor(@Header("Authorization") authToken: String?, @Path("id") id: String?, service : ApiService) =  service.downloadDriveFileCor(authToken,id)
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