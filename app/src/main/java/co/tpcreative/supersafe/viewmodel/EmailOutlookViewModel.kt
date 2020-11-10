package co.tpcreative.supersafe.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.api.requester.MicService
import co.tpcreative.supersafe.common.api.requester.UserService
import co.tpcreative.supersafe.common.extension.getString
import co.tpcreative.supersafe.common.network.Resource
import co.tpcreative.supersafe.common.network.Status
import co.tpcreative.supersafe.common.request.MicRequest
import co.tpcreative.supersafe.common.request.OutlookMailRequest
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EmailToken
import co.tpcreative.supersafe.model.EnumResponseCode
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.User
import kotlinx.coroutines.Dispatchers
import java.util.HashMap

class EmailOutlookViewModel(private val service: UserService, private val micService: MicService) : ViewModel(){
    fun sendEmail() = liveData(Dispatchers.IO){
        val mMicRequest = getEmailContent()
        val mResultSentEmail = micService.sendMail(mMicRequest)
        when(mResultSentEmail.status){
            Status.LOADING ->{}
            Status.SUCCESS -> {
                emit(mResultSentEmail)
            }
            Status.ERROR ->{
                if (EnumResponseCode.INVALID_AUTHENTICATION.code==mResultSentEmail.code){
                    val mRequestEmailToken = getRefreshContent(mMicRequest.data)
                    val mResultRefreshToken = micService.refreshEmailToken(mRequestEmailToken)
                    when(mResultRefreshToken.status){
                        Status.LOADING ->{}
                        Status.SUCCESS ->{
                            Utils.setEmailToken(mResultRefreshToken.data)
                            val mResultAddedMailToken = micService.addEmailToken(getAddedEmailToken())
                            when(mResultAddedMailToken.status){
                                Status.LOADING -> {}
                                Status.SUCCESS ->{
                                    val mSentEmail = micService.sendMail(getEmailContent())
                                    when(mSentEmail.status){
                                        Status.LOADING ->{}
                                        Status.SUCCESS ->{
                                            emit(mResultSentEmail)
                                        }
                                        Status.ERROR ->{
                                            emit(Resource.error(mSentEmail.code?:Utils.CODE_EXCEPTION, mSentEmail.message ?:"",null))
                                        }
                                    }
                                }
                                Status.ERROR ->{
                                    emit(Resource.error(mResultAddedMailToken.code?:Utils.CODE_EXCEPTION, mResultAddedMailToken.message ?:"",null))
                                }
                            }
                        }
                        Status.ERROR -> {
                            emit(Resource.error(mResultRefreshToken.code?:Utils.CODE_EXCEPTION, mResultRefreshToken.message ?:"",null))
                        }
                    }
                }
            }
        }
    }

    private fun getEmailContent() : MicRequest {
        val mUser: User? = Utils.getUserInfo()
        val mEmailToken =  mUser?.let { EmailToken.getInstance()?.convertObject(it, EnumStatus.SIGN_IN) }
        return MicRequest(Utils.getMicAccessToken()!!,mEmailToken!!)
    }

    private fun getRefreshContent(request: EmailToken?) : MutableMap<String?,Any?>{
        val hash: MutableMap<String?, Any?> = HashMap()
        hash[getString(R.string.key_client_id)] = request?.client_id
        hash[getString(R.string.key_redirect_uri)] = request?.redirect_uri
        hash[getString(R.string.key_grant_type)] = request?.grant_type
        hash[getString(R.string.key_refresh_token)] = request?.refresh_token
        return hash
    }

    private fun getAddedEmailToken() : OutlookMailRequest {
        val mUser = Utils.getUserInfo()
        return OutlookMailRequest(mUser?.email_token?.refresh_token, mUser?.email_token?.access_token)
    }
}