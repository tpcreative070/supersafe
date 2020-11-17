package co.tpcreative.supersafe.viewmodel
import androidx.lifecycle.ViewModel
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.api.requester.MicService
import co.tpcreative.supersafe.common.extension.getString
import co.tpcreative.supersafe.common.network.Resource
import co.tpcreative.supersafe.common.network.Status
import co.tpcreative.supersafe.common.request.MicRequest
import co.tpcreative.supersafe.common.request.OutlookMailRequest
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.HashMap

class EmailOutlookViewModel(private val micService: MicService) : BaseViewModel<ItemModel>(){
    suspend fun sendEmail(enumStatus : EnumStatus) : Resource<String>{
        return withContext(Dispatchers.IO){
            try {
                val mMicRequest = getEmailContent(enumStatus)
                val mResultSentEmail = micService.sendMail(mMicRequest)
                when(mResultSentEmail.status){
                    Status.SUCCESS -> {
                        mResultSentEmail
                    }
                    else -> {
                        if (EnumResponseCode.INVALID_AUTHENTICATION.code==mResultSentEmail.code){
                            val mRequestEmailToken = getRefreshContent(mMicRequest.data)
                            val mResultRefreshToken = micService.refreshEmailToken(mRequestEmailToken)
                            when(mResultRefreshToken.status){
                                Status.SUCCESS ->{
                                    Utils.setEmailToken(mResultRefreshToken.data)
                                    val mResultAddedMailToken = micService.addEmailToken(getAddedEmailToken())
                                    when(mResultAddedMailToken.status){
                                        Status.SUCCESS ->{
                                            val mSentEmail = micService.sendMail(getEmailContent(enumStatus))
                                            when(mSentEmail.status){
                                                Status.SUCCESS ->{
                                                    mSentEmail
                                                }
                                                else ->  Resource.error(mSentEmail.code?:Utils.CODE_EXCEPTION, mSentEmail.message ?:"",null)
                                            }
                                        }
                                        else -> Resource.error(mResultAddedMailToken.code?:Utils.CODE_EXCEPTION, mResultAddedMailToken.message ?:"",null)
                                    }
                                }
                                else ->   Resource.error(mResultRefreshToken.code?:Utils.CODE_EXCEPTION, mResultRefreshToken.message ?:"",null)
                            }
                        }else{
                            Resource.error(mResultSentEmail.code?:Utils.CODE_EXCEPTION, mResultSentEmail.message ?:"",null)
                        }
                    }
                }
            }catch (e : Exception){
                e.printStackTrace()
                Resource.error(Utils.CODE_EXCEPTION, e.message ?:"",null)
            }
        }
    }

    private fun getEmailContent(enumStatus: EnumStatus) : MicRequest {
        val mUser: User? = Utils.getUserInfo()
        val mEmailToken =  mUser?.let { EmailToken.getInstance()?.convertObject(it, enumStatus) }
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