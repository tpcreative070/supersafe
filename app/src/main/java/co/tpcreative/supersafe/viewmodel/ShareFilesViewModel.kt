package co.tpcreative.supersafe.viewmodel
import androidx.lifecycle.MutableLiveData
import co.tpcreative.supersafe.model.ImportFilesModel

class ShareFilesViewModel : BaseViewModel<ImportFilesModel>(){
    override val isLoading: MutableLiveData<Boolean>
        get() = super.isLoading

}
