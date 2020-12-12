package co.tpcreative.supersafe.viewmodel
import androidx.lifecycle.liveData
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.BreakInAlertsModel
import kotlinx.coroutines.Dispatchers

class BreakInAlertsViewModel : BaseViewModel<BreakInAlertsModel>()  {
    fun getData()  = liveData(Dispatchers.Main){
        dataList.clear()
        SQLHelper.getBreakInAlertsList()?.let { it ->
            it.sortByDescending {
                it.id
            }
            dataList.addAll(it)
        }
        emit(dataList)
    }

    fun deleteAll() =  liveData(Dispatchers.Main){
        for (index in dataList) {
            Utils.onDeleteFile(index.fileName)
            SQLHelper.onDelete(index)
        }
        emit(true)
    }
}