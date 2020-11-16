package co.tpcreative.supersafe.viewmodel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import co.tpcreative.supersafe.common.extension.toJson
import co.tpcreative.supersafe.common.network.Resource
import co.tpcreative.supersafe.common.network.Status
import co.tpcreative.supersafe.common.request.UserCloudRequest
import co.tpcreative.supersafe.common.response.DataResponse
import co.tpcreative.supersafe.common.response.UserCloudResponse
import co.tpcreative.supersafe.common.util.Utils
import kotlinx.coroutines.Dispatchers

class EnableCloudViewModel(private val userViewModel: UserViewModel) : BaseViewModel(){
    val TAG = this::class.java.simpleName
    override val errorMessages: MutableLiveData<MutableMap<String, String?>?>
        get() = super.errorMessages

    override val errorResponseMessage: MutableLiveData<MutableMap<String, String?>?>
        get() = super.errorResponseMessage

    override val isLoading: MutableLiveData<Boolean>
        get() = super.isLoading

    fun addUserCloud() = liveData(Dispatchers.IO){
        try {
            isLoading.postValue(true)
            val mResult = userViewModel.addUserCloud(UserCloudRequest(Utils.getUserId(), Utils.getUserCloudId(), Utils.getDeviceId() ?:"null"))
            when(mResult.status){
                Status.SUCCESS -> {
                    if (mResult.data?.error!!){
                        emit(Resource.error(mResult.data.responseCode ?: Utils.CODE_EXCEPTION, mResult.data.responseMessage  ?: "",null))
                    }else{
                        mResult.data.data?.let { setUserData(it) }
                        emit(mResult)
                    }
                }
                else -> emit(Resource.error(mResult.code ?: Utils.CODE_EXCEPTION, mResult.message ?: "",null))
            }
        }catch (e : Exception){
            Resource.error(Utils.CODE_EXCEPTION,e.message ?: "",null)
        }
        finally {
            isLoading.postValue(false)
        }
    }

    private fun setUserData(mData : DataResponse){
        val mUser = Utils.getUserInfo()
        val mCloudData: UserCloudResponse? = mData.userCloud
        mUser?.cloud_id = mCloudData?.cloud_id
        Utils.Log(TAG,"Response ${mData.toJson()}")
        Utils.setUserPreShare(mUser)
    }
}