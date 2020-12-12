package co.tpcreative.supersafe.viewmodel
import androidx.lifecycle.liveData
import co.tpcreative.supersafe.model.EmptyModel
import co.tpcreative.supersafe.model.ThemeApp
import kotlinx.coroutines.Dispatchers

class ThemeSettingsViewModel : BaseViewModel<EmptyModel>() {
    var mThemeApp: ThemeApp? = ThemeApp()
    fun getData()  = liveData(Dispatchers.Main){
        val mData = ThemeApp.getInstance()?.getList()
        mThemeApp = ThemeApp.getInstance()?.getThemeInfo()
        if (mThemeApp != null) {
            for (i in mData?.indices!!) {
                mData[i].isCheck = mThemeApp?.getId() == mData[i].getId()
            }
        }
        emit(mData)
    }
}