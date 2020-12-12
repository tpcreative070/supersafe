package co.tpcreative.supersafe.viewmodel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.tpcreative.supersafe.common.extension.toJson
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.EnumStepProgressing
import co.tpcreative.supersafe.model.EnumValidationKey
import co.tpcreative.supersafe.model.ItemModel
import co.tpcreative.supersafe.model.MainCategoryModel

open class BaseViewModel<T> : ViewModel() {
    open val isLoading : MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    open val errorMessages : MutableLiveData<MutableMap<String, String?>?> by lazy {
        MutableLiveData<MutableMap<String,String?>?>()
    }

    open val errorResponseMessage  : MutableLiveData<MutableMap<String,String?>?> by lazy {
        MutableLiveData<MutableMap<String,String?>?>()
    }

    open val videos : MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }
    open val photos : MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }
    open val audios : MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }
    open val others : MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    open val count : MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    open var isRequestSyncData : Boolean = false

    open var position : Int = 0

    open val isSelectAll : MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    protected open val dataList : MutableList<T> = mutableListOf()

    open lateinit var mainCategoryModel : MainCategoryModel

    open fun putErrorResponse(key: EnumValidationKey,value : String? = null){
        if (errorResponseMessage.value==null){
            try {
                errorResponseMessage.value = mutableMapOf(key.name to value)
            }catch (e : Exception){
                errorResponseMessage.postValue(mutableMapOf(key.name to value))
            }
        }else{
            if (value.isNullOrEmpty()){
                errorResponseMessage.value?.remove(key.name)
                errorResponseMessage.postValue(errorResponseMessage.value)
            }else{
                errorResponseMessage.value?.set(key.name,value)
                errorResponseMessage.postValue(errorResponseMessage.value)
            }
        }
    }

    open fun putError(key: EnumValidationKey,value : String? = null){
         if (errorMessages.value==null){
             try {
                 errorMessages.value = mutableMapOf(key.name to value)
             }catch (e : Exception){
                 errorMessages.postValue(mutableMapOf(key.name to value))
             }
         }else{
             if (value.isNullOrEmpty()){
                 errorMessages.value?.remove(key.name)
                 errorMessages.postValue(errorMessages.value)
             }else{
                 errorMessages.value?.set(key.name,value)
                 errorMessages.postValue(errorMessages.value)
             }
         }
    }
}