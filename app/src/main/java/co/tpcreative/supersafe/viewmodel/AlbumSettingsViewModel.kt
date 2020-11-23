package co.tpcreative.supersafe.viewmodel
import android.app.Activity
import android.os.Bundle
import androidx.lifecycle.liveData
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EmptyModel
import co.tpcreative.supersafe.model.MainCategoryModel
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers

class AlbumSettingsViewModel : BaseViewModel<EmptyModel>() {
    val TAG = this::class.java.simpleName

    fun getData(activity: Activity?)  = liveData(Dispatchers.Main){
        val bundle: Bundle? = activity?.intent?.extras
        try {
            val mResult: MainCategoryModel = bundle?.get(activity.getString(R.string.key_main_categories)) as MainCategoryModel
            mainCategoryModel = mResult
            Utils.Log(TAG, Gson().toJson(mainCategoryModel))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        emit(mainCategoryModel)
    }

    fun getData() = liveData(Dispatchers.Main){
        try {
            val mainCategories: MainCategoryModel? = SQLHelper.getCategoriesLocalId(mainCategoryModel.categories_local_id, mainCategoryModel.isFakePin!!)
            if (mainCategories != null) {
                mainCategoryModel = mainCategories
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        emit(mainCategoryModel)
    }
}