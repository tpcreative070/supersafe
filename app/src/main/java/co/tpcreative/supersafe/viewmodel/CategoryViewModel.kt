package co.tpcreative.supersafe.viewmodel
import androidx.lifecycle.ViewModel
import co.tpcreative.supersafe.common.api.requester.CategoryService
import co.tpcreative.supersafe.common.helper.SQLHelper
import co.tpcreative.supersafe.common.network.Resource
import co.tpcreative.supersafe.common.network.Status
import co.tpcreative.supersafe.common.request.CategoriesRequest
import co.tpcreative.supersafe.common.response.DataResponse
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.MainCategoryModel
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CategoryViewModel(private val categoryService: CategoryService) : ViewModel() {
    val TAG = this::class.java.simpleName
    suspend fun syncCategoryData() : Resource<Boolean>{
        return withContext(Dispatchers.IO){
            try {
                val mResult: MutableList<MainCategoryModel>? = SQLHelper.requestSyncCategories(isSyncOwnServer = false, isFakePin = false)
                for (index in mResult!!){
                    val mResultSync = categoryService.categoriesSync(CategoriesRequest(index))
                    when(mResultSync.status){
                        Status.SUCCESS -> {
                            if (!(mResultSync.data?.error)!!){
                                updatedDataAfterSyncCategory(index,mResultSync.data.data)
                            }else{
                                Utils.Log(TAG,mResultSync.data.responseMessage)
                            }
                        }
                        else -> Utils.Log(TAG,mResultSync.message)
                    }
                }
                Resource.success(true)
            }catch (e : Exception){
                e.printStackTrace()
                Resource.error(Utils.CODE_EXCEPTION, e.message ?:"",null)
            }
        }
    }

    suspend fun updateCategoryData() : Resource<Boolean>{
        return withContext(Dispatchers.IO){
            try {
                val mResult: MutableList<MainCategoryModel>? = SQLHelper.getRequestUpdateCategoryList()
                for (index in mResult!!){
                    val mResultUpdated = categoryService.categoriesSync(CategoriesRequest(index))
                    when(mResultUpdated.status){
                        Status.SUCCESS -> {
                            if (!(mResultUpdated.data?.error)!!){
                                updatedDataAfterSyncCategory(index,mResultUpdated.data.data)
                            }else{
                                Utils.Log(TAG,mResultUpdated.data.responseMessage)
                            }
                        }
                        else -> Utils.Log(TAG,mResultUpdated.message)
                    }
                }
                Resource.success(true)
            }catch (e : Exception){
                e.printStackTrace()
                Resource.error(Utils.CODE_EXCEPTION, e.message ?:"",null)
            }
        }
    }

    suspend fun dateCategoryData() : Resource<Boolean> {
        return withContext(Dispatchers.IO){
            try {
                val listRequestDelete: MutableList<MainCategoryModel>? = SQLHelper.getDeleteCategoryRequest()
                for (index in listRequestDelete!!){
                    val mResultDeleted = categoryService.deleteCategoriesCor(CategoriesRequest(index))
                    when(mResultDeleted.status){
                        Status.SUCCESS ->{
                            SQLHelper.deleteCategory(index)
                        }
                        else ->{
                            Utils.Log(TAG,mResultDeleted.message)
                        }

                    }
                }
                Resource.success(true)
            }catch (e : Exception){
                e.printStackTrace()
                Resource.error(Utils.CODE_EXCEPTION, e.message ?:"",null)
            }
        }
    }

    fun checkAndAddCategory(mList : MutableList<MainCategoryModel>?){
       try {
           for (index in mList!!) {
               val main: MainCategoryModel? = SQLHelper.getCategoriesId(index.categories_id, false)
               if (main != null) {
                   if (!main.isChange && !main.isDelete) {
                       main.isSyncOwnServer = true
                       main.categories_name = index.categories_name
                       main.created_date = index.created_date
                       main.updated_date = index.updated_date
                       main.date_time = index.date_time
                       SQLHelper.updateCategory(main)
                       Utils.Log(TAG, "Updating new main categories => ^^ ${index.updated_date}")
                   }
               } else {
                   var mMain: MainCategoryModel? = SQLHelper.getCategoriesItemId(index.categories_hex_name, false)
                   if (mMain != null) {
                       if (!mMain.isDelete && !mMain.isChange) {
                           mMain.isSyncOwnServer = true
                           mMain.isChange = false
                           mMain.isDelete = false
                           mMain.categories_id = index.categories_id
                           mMain.created_date = index.created_date
                           mMain.updated_date = index.updated_date
                           mMain.date_time = index.date_time
                           SQLHelper.updateCategory(mMain)
                           Utils.Log(TAG, "Updating new main categories => **")
                       }
                   } else {
                       mMain = index
                       mMain.categories_local_id = Utils.getUUId()
                       mMain.items_id = Utils.getUUId()
                       mMain.isSyncOwnServer = true
                       mMain.isChange = false
                       mMain.isDelete = false
                       mMain.pin = ""
                       val count: Int = SQLHelper.getLatestItem()
                       mMain.categories_max = count.toLong()
                       SQLHelper.insertCategory(mMain)
                       Utils.Log(TAG, "Adding new main categories")
                   }
               }
           }
           onCategoryDeleteSyncedLocal(mList)
       }catch (e : Exception){
           e.printStackTrace()
       }
       finally {
           Utils.Log(TAG,"Checking is done")
       }
    }

    private fun onCategoryDeleteSyncedLocal(mList: List<MainCategoryModel>) {
        val mResultList = Utils.checkCategoryDeleteSyncedLocal(mList)
        mResultList.let {
            for (index in it) {
                SQLHelper.deleteCategory(index)
            }
            Utils.Log(TAG, "Total need to delete categories synced local.... " + it.size)
        }
    }

    private fun updatedDataAfterSyncCategory(mainCategories : MainCategoryModel, mData: DataResponse?){
        if (mData?.category != null) {
            if (mainCategories.categories_hex_name == mData.category?.categories_hex_name) {
                mainCategories.categories_id = mData.category?.categories_id
                mainCategories.isSyncOwnServer = true
                mainCategories.isChange = false
                mainCategories.isDelete = false
                SQLHelper.updateCategory(mainCategories)
            } else {
               Utils.Log(TAG,"Not found category...")
            }
        }
    }
}