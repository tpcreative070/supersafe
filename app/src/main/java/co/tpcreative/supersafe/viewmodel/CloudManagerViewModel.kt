package co.tpcreative.supersafe.viewmodel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import co.tpcreative.supersafe.common.controller.ServiceManager
import co.tpcreative.supersafe.common.network.Resource
import co.tpcreative.supersafe.common.network.Status
import co.tpcreative.supersafe.common.presenter.BaseView
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.common.utilimport.NetworkUtil
import co.tpcreative.supersafe.model.EnumStatus
import co.tpcreative.supersafe.model.ItemModel
import co.tpcreative.supersafe.ui.cloudmanager.CloudManagerPresenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CloudManagerViewModel : BaseViewModel<ItemModel>(){
    val TAG = this::class.java.simpleName
    override val isLoading: MutableLiveData<Boolean>
        get() = super.isLoading

    fun getDriveAbout()  = liveData(Dispatchers.Main){
        val mResult =  ServiceManager.getInstance()?.getDriveAbout()
        when(mResult?.status){
            Status.SUCCESS -> {
                val mResultList = ServiceManager.getInstance()?.getInAppList()
                when(mResultList?.status){
                    Status.SUCCESS -> {
                        Utils.Log(TAG, "Fetch data completely")
                        emit(Resource.success(true))
                    }
                    else -> {
                        Utils.Log(TAG,mResultList?.message)
                        emit(Resource.error(mResultList?.code ?: Utils.CODE_EXCEPTION,mResultList?.message ?:"", null))
                    }
                }
            }else -> {
            Utils.Log(TAG,mResult?.message)
            emit(Resource.error(mResult?.code ?: Utils.CODE_EXCEPTION,mResult?.message ?:"", null))
            }
        }
    }


}