package co.tpcreative.supersafe.viewmodel
import androidx.lifecycle.liveData
import co.tpcreative.supersafe.model.EmptyModel
import co.tpcreative.supersafe.model.EnumPinAction
import co.tpcreative.supersafe.model.EnumStatus
import kotlinx.coroutines.Dispatchers

class LockScreenViewModel : BaseViewModel<EmptyModel>() {
    fun changeStatus(status: EnumStatus?, action: EnumPinAction?)  = liveData(Dispatchers.Main){
        emit(LockScreenResponse(status,action))
    }
}

class LockScreenResponse(val status: EnumStatus?,val action: EnumPinAction?) {

}