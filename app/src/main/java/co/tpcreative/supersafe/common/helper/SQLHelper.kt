package co.tpcreative.supersafe.common.helper

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.room.Ignore
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.entities.InstanceGenerator
import co.tpcreative.supersafe.common.entities.ItemEntity
import co.tpcreative.supersafe.common.entities.MainCategoryEntity
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

object SQLHelper {
    private val TAG = SQLHelper::class.java.simpleName
    fun getAllItemList(): MutableList<ItemModel>? {
        val list: MutableList<ItemEntityModel>? = getInstance()?.getAllListItems()
        val mList: MutableList<ItemModel> = ArrayList<ItemModel>()
        if (list!=null){
            for (index in list) {
                mList.add(ItemModel(index))
            }
            return mList
        }
        return null
    }

    /*Check request delete category*/
    fun getDeleteCategoryRequest(): MutableList<MainCategoryModel>? {
        val mList: MutableList<MainCategoryModel> = ArrayList<MainCategoryModel>()
        val deleteAlbum: MutableList<MainCategoryEntityModel>? = getInstance()?.geCategoryList(true, false)
        if (deleteAlbum != null) {
            for (index in deleteAlbum) {
                mList.add(MainCategoryModel(index))
            }
            return mList
        }
        return null
    }

    /*Check request delete item*/
    fun getDeleteItemRequest(): MutableList<ItemModel>? {
        val mList: MutableList<ItemModel> = ArrayList<ItemModel>()
        val mResult: MutableList<ItemEntityModel>? = getInstance()?.getDeleteLocalListItems(true, EnumDelete.DELETE_WAITING.ordinal, false)
        if (mResult != null) {
            for (index in mResult) {
                mList.add(ItemModel(index))
            }
            return mList
        }
        return null
    }

    /*Delete category*/
    fun deleteCategory(itemModel: MainCategoryModel) {
        getInstance()?.onDelete(MainCategoryEntity(MainCategoryEntityModel(itemModel)))
    }

    /*Delete item*/
    fun deleteItem(itemModel: ItemModel) {
        getInstance()?.onDelete(ItemEntity(ItemEntityModel(itemModel)))
    }

    /*Request download item*/
    fun getItemListDownload(): MutableList<ItemModel>? {
        val list: MutableList<ItemEntityModel>? = getInstance()?.getItemListDownload(true, false)
        val mList: MutableList<ItemModel> = ArrayList<ItemModel>()
        if (list!=null){
            for (index in list) {
                mList.add(ItemModel(index))
            }
            return mList
        }
        return null
    }

    /*Request upload item*/
    fun getItemListUpload(): MutableList<ItemModel>? {
        val mList: MutableList<ItemModel> = ArrayList<ItemModel>()
        val mResult: MutableList<ItemEntityModel>? = getInstance()?.getRequestUploadData(false)
        if (mResult != null) {
            for (index in mResult) {
                if (!Utils.isNotEmptyOrNull(index?.categories_id)) {
                    val categoryModel: MainCategoryModel? = getCategoriesLocalId(index.categories_local_id)
                    if (categoryModel != null) {
                        mList.add(ItemModel(index, categoryModel.categories_id))
                    }
                } else {
                    mList.add(ItemModel(index))
                }
            }
            return mList
        }
        return null
    }

    /*Added item*/
    fun insertedItem(itemModel: ItemModel) {
        getInstance()?.onInsert(ItemEntity(ItemEntityModel(itemModel)))
    }

    /*Updated item*/
    fun updatedItem(itemModel: ItemModel) {
        getInstance()?.onUpdate(ItemEntity(ItemEntityModel(itemModel)))
    }

    /*Added get item*/
    fun getItemById(items_id: String?): ItemModel? {
        val items: ItemEntityModel? = getInstance()?.getItemId(items_id, false)
        return if (items != null) {
            ItemModel(items)
        } else null
    }

    /*Get local item list*/
    fun getDeleteLocalListItems(isDeleteLocal: Boolean, deleteAction: Int, isFakePin: Boolean): MutableList<ItemModel>? {
        try {
            val mList: MutableList<ItemEntityModel>? = getInstance()?.getDeleteLocalListItems(isDeleteLocal, deleteAction, isFakePin)
            val mData: MutableList<ItemModel> = ArrayList<ItemModel>()
            if (mList != null) {
                for (index in mList) {
                    mData.add(ItemModel(index))
                }
                return mData
            }
        } catch (e: Exception) {
        }
        return null
    }

    /*Get request update item*/
    fun getRequestUpdateItemList(): MutableList<ItemModel>? {
        val mList: MutableList<ItemModel> = ArrayList<ItemModel>()
        val mResult: MutableList<ItemEntityModel>? = getInstance()?.getLoadListItemUpdate(true, true, true, false)
        if (mResult != null) {
            for (index in mResult) {
                mList.add(ItemModel(index))
            }
            return mList
        }
        return null
    }

    /*Get item list*/
    fun getListItems(categories_local_id: String?, isFakePin: Boolean): MutableList<ItemModel>? {
        try {
            val mList: MutableList<ItemEntityModel>? = getInstance()?.getListItems(categories_local_id, isFakePin)
            val mData: MutableList<ItemModel> = ArrayList<ItemModel>()
            if (mList != null) {
                for (index in mList) {
                    mData.add(ItemModel(index))
                }
                return mData
            }
        } catch (e: Exception) {
        }
        return null
    }

    fun getItemId(item_id: String?, isFakePin: Boolean): ItemModel? {
        try {
            val mResult: ItemEntityModel? = getInstance()?.getItemId(item_id, isFakePin)
            if (mResult != null) {
                return ItemModel(mResult)
            }
        } catch (e: Exception) {
        }
        return null
    }

    /*Get request update category */
    fun getRequestUpdateCategoryList(): MutableList<MainCategoryModel>? {
        val mList: MutableList<MainCategoryModel> = ArrayList<MainCategoryModel>()
        val deleteAlbum: MutableList<MainCategoryEntityModel>? = getInstance()?.getChangedCategoryList()
        if (deleteAlbum != null) {
            for (index in deleteAlbum) {
                mList.add(MainCategoryModel(index))
            }
            return mList
        }
        return null
    }

    /*Added category*/
    fun insertCategory(mainCategoryModel: MainCategoryModel) {
        getInstance()?.onInsert(MainCategoryEntityModel(mainCategoryModel))
    }

    /*Update Category*/
    fun updateCategory(model: MainCategoryModel) {
        getInstance()?.onUpdate(MainCategoryEntityModel(model))
    }

    /*Get category item by id*/
    fun getCategoriesId(categories_id: String?, isFakePin: Boolean): MainCategoryModel? {
        val mResult: MainCategoryEntityModel? = getInstance()?.getCategoriesId(categories_id, isFakePin)
        return if (mResult != null) {
            MainCategoryModel(mResult)
        } else null
    }

    /*Get category item by hex name*/
    fun getCategoriesItemId(categories_hex_name: String?, isFakePin: Boolean): MainCategoryModel? {
        val mResult: MainCategoryEntityModel? = getInstance()?.getCategoriesItemId(categories_hex_name, isFakePin)
        return if (mResult != null) {
            MainCategoryModel(mResult)
        } else null
    }

    /*Get local category*/
    fun getCategoriesLocalId(categories_local_id: String?, isFakePin: Boolean): MainCategoryModel? {
        val mResult: MainCategoryEntityModel? = getInstance()?.getCategoriesLocalId(categories_local_id, isFakePin)
        return if (mResult != null) {
            MainCategoryModel(mResult)
        } else null
    }

    fun getListCategories(isDelete: Boolean, isFakePin: Boolean): MutableList<MainCategoryModel>? {
        try {
            val mList: MutableList<MainCategoryEntityModel>? = getInstance()?.getListCategories(false, false)
            val mData: MutableList<MainCategoryModel> = ArrayList<MainCategoryModel>()
            if (mList != null) {
                for (index in mList) {
                    mData.add(MainCategoryModel(index))
                }
                return mData
            }
        } catch (e: Exception) {
        }
        return null
    }

    fun getListCategories(isFakePin: Boolean): MutableList<MainCategoryModel>? {
        try {
            val mResult: MutableList<MainCategoryEntityModel>? = getInstance()?.getListCategories(isFakePin)
            val mData: MutableList<MainCategoryModel> = ArrayList<MainCategoryModel>()
            if (mResult != null) {
                for (index in mResult) {
                    mData.add(MainCategoryModel(index))
                }
                return mData
            }
        } catch (e: Exception) {
        }
        return null
    }

    fun requestSyncCategories(isSyncOwnServer: Boolean, isFakePin: Boolean): MutableList<MainCategoryModel>? {
        try {
            val mList: MutableList<MainCategoryEntityModel>? = getInstance()?.loadListItemCategoriesSync(isSyncOwnServer, isFakePin)
            val mData: MutableList<MainCategoryModel> = ArrayList<MainCategoryModel>()
            if (mList != null) {
                for (index in mList) {
                    mData.add(MainCategoryModel(index))
                }
                return mData
            }
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
        return null
    }

    fun getList(): MutableList<MainCategoryModel>? {
        val mList: MutableList<MainCategoryModel> = ArrayList<MainCategoryModel>()
        val list: MutableList<MainCategoryModel>? = getListCategories(false, false)
        if (list != null && list.size > 0) {
            mList.addAll(list)
        } else {
            val map: MutableMap<String?, MainCategoryModel> = getMainCategoriesDefault()
            Utils.Log(TAG, "No Data " + map.size)
            for ((_, main) in map) {
                insertCategory(main)
            }
        }
        val listDelete: MutableList<ItemModel>? = getDeleteLocalListItems(true, EnumDelete.NONE.ordinal, false)
        if (listDelete != null) {
            if (listDelete.size > 0) {
                val items: MainCategoryModel? = getTrashItem()
                val count: Int? = getInstance()?.getLatestItem()
                items?.categories_max = count!!.toLong()
                if (items != null) {
                    mList.add(items)
                }
            }
        }
        Collections.sort(mList, Comparator { lhs, rhs ->
            val count_1 = lhs?.categories_max!!
            val count_2 = rhs?.categories_max!!
            count_1.toInt() - count_2.toInt()
        })
        return mList
    }

    fun getListMoveGallery(categories_local_id: String?, isFakePin: Boolean): MutableList<MainCategoryModel>? {
        val mList: MutableList<MainCategoryModel>? = getListCategories(categories_local_id, false, isFakePin)
        Collections.sort(mList, Comparator { lhs, rhs ->
            val count_1 = lhs?.categories_max!!
            val count_2 = rhs?.categories_max!!
            count_1.toInt() - count_2.toInt()
        })
        return mList
    }

    fun getListCategories(categories_local_id: String?, isDelete: Boolean, isFakePin: Boolean): MutableList<MainCategoryModel>? {
        try {
            val mList: MutableList<MainCategoryEntityModel>? = getInstance()?.getListCategories(categories_local_id, isDelete, isFakePin)
            val mData: MutableList<MainCategoryModel> = ArrayList<MainCategoryModel>()
            if (mList != null) {
                for (index in mList) {
                    mData.add(MainCategoryModel(index))
                }
                return mData
            }
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
        return null
    }

    fun getListFakePin(): MutableList<MainCategoryModel>? {
        val list: MutableList<MainCategoryModel>? = getListCategories(true)
        if (list!=null){
            getMainItemFakePin()?.let { list.add(it) }
            Collections.sort(list, Comparator { lhs, rhs ->
                val count_1 = lhs?.categories_max as Int
                val count_2 = rhs?.categories_max as Int
                count_1 - count_2
            })
            return list
        }
        return null
    }

    @Transient
    val ListIcon: Array<String?>? = arrayOf(
            "baseline_photo_white_48",
            "baseline_how_to_vote_white_48",
            "baseline_local_movies_white_48",
            "baseline_favorite_border_white_48",
            "baseline_delete_white_48",
            "baseline_cake_white_48",
            "baseline_school_white_48")

    @Transient
    val ListColor: Array<String?>? = arrayOf(
            "#34bdb7",
            "#03A9F4",
            "#9E9D24",
            "#AA00FF",
            "#371989",
            "#E040FB",
            "#9E9E9E")

    fun getMainCategoriesDefault(): MutableMap<String?, MainCategoryModel> {
        val map: MutableMap<String?, MainCategoryModel> = HashMap<String?, MainCategoryModel>()
        map[Utils.getHexCode("1234")] = MainCategoryModel("null", Utils.getHexCode("1234"), Utils.getHexCode(SuperSafeApplication.getInstance().getString(R.string.key_main_album)), SuperSafeApplication.getInstance().getString(R.string.key_main_album), ListColor?.get(0), ListIcon?.get(0), 0, false, false, false, false, "", Utils.getUUId(), null, false)
        map[Utils.getHexCode("1235")] = MainCategoryModel("null", Utils.getHexCode("1235"), Utils.getHexCode(SuperSafeApplication.getInstance().getString(R.string.key_photos)), SuperSafeApplication.getInstance().getString(R.string.key_photos), ListColor?.get(1), ListIcon?.get(1), 1, false, false, false, false, "", Utils.getUUId(), null, false)
        map[Utils.getHexCode("1236")] = MainCategoryModel("null", Utils.getHexCode("1236"), Utils.getHexCode(SuperSafeApplication.getInstance().getString(R.string.key_videos)), SuperSafeApplication.getInstance().getString(R.string.key_videos), ListColor?.get(2), ListIcon?.get(2), 2, false, false, false, false, "", Utils.getUUId(), null, false)
        map[Utils.getHexCode("1237")] = MainCategoryModel("null", Utils.getHexCode("1237"), Utils.getHexCode(SuperSafeApplication.getInstance().getString(R.string.key_significant_other)), SuperSafeApplication.getInstance().getString(R.string.key_significant_other), ListColor?.get(3), ListIcon?.get(3), 3, false, false, false, false, "", Utils.getUUId(), null, false)
        return map
    }

    fun getCategoriesDefault(): MutableList<MainCategoryModel> {
        val list: MutableList<MainCategoryModel> = ArrayList<MainCategoryModel>()
        list.add(MainCategoryModel("null", null, null, null, ListColor?.get(0), ListIcon?.get(0), 0, false, false, false, false, "", null, Utils.getHexCode("1234"), false))
        list.add(MainCategoryModel("null", null, null, null, ListColor?.get(1), ListIcon?.get(1), 1, false, false, false, false, "", null, Utils.getHexCode("1235"), false))
        list.add(MainCategoryModel("null", null, null, null, ListColor?.get(2), ListIcon?.get(2), 2, false, false, false, false, "", null, Utils.getHexCode("1236"), false))
        list.add(MainCategoryModel("null", null, null, null, ListColor?.get(3), ListIcon?.get(3), 3, false, false, false, false, "", null, Utils.getHexCode("1237"), false))
        list.add(MainCategoryModel("null", null, null, null, ListColor?.get(5), ListIcon?.get(5), 5, false, false, false, false, "", null, Utils.getHexCode("1238"), false))
        list.add(MainCategoryModel("null", null, null, null, ListColor?.get(6), ListIcon?.get(6), 6, false, false, false, false, "", null, Utils.getHexCode("1239"), false))
        return list
    }

    fun getTrashItem(): MainCategoryModel? {
        return MainCategoryModel("null", Utils.getUUId(), Utils.getHexCode(SuperSafeApplication.Companion.getInstance().getString(R.string.key_trash)), SuperSafeApplication.Companion.getInstance().getString(R.string.key_trash), ListColor?.get(4), ListIcon?.get(4), System.currentTimeMillis(), false, false, false, false, "", null, null, false)
    }

    fun getMainItemFakePin(): MainCategoryModel? {
        return MainCategoryModel("null", Utils.getHexCode("1234"), Utils.getHexCode(SuperSafeApplication.Companion.getInstance().getString(R.string.key_main_album)), SuperSafeApplication.Companion.getInstance().getString(R.string.key_main_album), ListColor?.get(0), ListIcon?.get(0), 0, false, false, false, true, "", null, null, false)
    }

    fun onAddCategories(categories_hex_name: String?, name: String?, isFakePin: Boolean): Boolean {
        try {
            val main: MainCategoryModel? = getCategoriesItemId(categories_hex_name, isFakePin)
            if (main == null) {
                val count: Int? = getInstance()?.getLatestItem()
                insertCategory(MainCategoryModel("null", Utils.getUUId(), Utils.getHexCode(name!!), name, ListColor?.get(0), ListIcon?.get(0), count!!.toLong(), false, false, false, isFakePin, "", null, null, false))
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun onAddFakePinCategories(categories_hex_name: String?, name: String?, isFakePin: Boolean): Boolean {
        try {
            val main: MainCategoryModel? = getCategoriesItemId(categories_hex_name, isFakePin)
            if (main == null) {
                val count: Int? = getInstance()?.getLatestItem()
                insertCategory(MainCategoryModel("null", Utils.getUUId(), Utils.getHexCode(name!!), name, ListColor?.get(0), ListIcon?.get(0), count!!.toLong(), false, false, false, isFakePin, "", null, null, false))
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun onChangeCategories(mainCategories: MainCategoryModel?): Boolean {
        try {
            val hex_name: String? = mainCategories?.categories_name?.let { Utils.getHexCode(it) }
            val mIsFakePin: Boolean = mainCategories!!.isFakePin
            val response: MainCategoryModel? = getCategoriesItemId(hex_name, mIsFakePin)
            if (response == null) {
                mainCategories.categories_hex_name = hex_name
                mainCategories.isChange = true
                mainCategories.isSyncOwnServer = false
                updateCategory(mainCategories)
                return true
            }
            Utils.Log(TAG, "value changed :" + Gson().toJson(response))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun getDrawable(mContext: Context?, name: String?): Drawable? {
        try {
            val resourceId = mContext?.getResources()?.getIdentifier(name, "drawable", mContext?.getPackageName())
            return ContextCompat.getDrawable(mContext!!,resourceId!!)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun objectToHashMap(items: MainCategoryEntity): MutableMap<String, Any>? {
        val type = object : TypeToken<MutableMap<String?, Any?>?>() {}.type
        return Gson().fromJson(Gson().toJson(items), type)
    }

    fun getObject(value: String?): MainCategoryEntity? {
        try {
            if (value == null) {
                return null
            }
            val items: MainCategoryEntity = Gson().fromJson(value, MainCategoryEntity::class.java)
            Utils.Log(TAG, Gson().toJson(items))
            return items
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    @Ignore
    fun getCategoriesPosition(mainCategories_Local_Id: String?): MainCategoryModel? {
        val data: MutableList<MainCategoryModel> = getCategoriesDefault()
        if (mainCategories_Local_Id == null) {
            return null
        }
        for (index in data) {
            if (index.mainCategories_Local_Id == mainCategories_Local_Id) {
                return index
            }
        }
        return null
    }

    fun getMainCurrentCategories(): HashMap<String, MainCategoryModel>? {
        val list: MutableList<MainCategoryModel>? = getListCategories(false)
        val hashMap: HashMap<String, MainCategoryModel> = HashMap<String, MainCategoryModel>()
        if (list != null) {
            for (i in list.indices) {
                val main: MainCategoryModel? = list[i]
                val categories_id: String? = main?.categories_id
                if (categories_id != null) {
                    hashMap[categories_id] = main
                }
            }
        }
        return hashMap
    }

    fun getListItems(categories_local_id: String?, formatType: Int, isDeleteLocal: Boolean, isFakePin: Boolean): MutableList<ItemModel>? {
        if (categories_local_id == null) {
            return null
        }
        try {
            val mList: MutableList<ItemEntityModel>? = getInstance()?.getListItems(categories_local_id, formatType, isDeleteLocal, isFakePin)
            val mData: MutableList<ItemModel> = ArrayList<ItemModel>()
            if (mList != null) {
                for (index in mList) {
                    mData.add(ItemModel(index))
                }
                return mData
            }
        } catch (e: Exception) {
        }
        return null
    }

    fun getItemId(item_id: String?): ItemModel? {
        try {
            val mResult: ItemEntityModel? = getInstance()?.getItemId(item_id)
            if (mResult != null) {
                return ItemModel(mResult)
            }
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
        return null
    }

    fun getListItems(categories_local_id: String?, isDeleteLocal: Boolean, isExport: Boolean, isFakePin: Boolean): MutableList<ItemModel>? {
        try {
            if (categories_local_id == null) {
                return null
            }
            try {
                val mList: MutableList<ItemEntityModel>? = getInstance()?.getListItems(categories_local_id, isDeleteLocal, isExport, isFakePin)
                val mData: MutableList<ItemModel> = ArrayList<ItemModel>()
                if (mList != null) {
                    for (index in mList) {
                        mData.add(ItemModel(index))
                    }
                    return mData
                }
            } catch (e: Exception) {
            }
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
        return null
    }

    fun getListItems(categories_local_id: String?, isDeleteLocal: Boolean, isFakePin: Boolean): MutableList<ItemModel>? {
        if (categories_local_id == null) {
            return null
        }
        try {
            val mList: MutableList<ItemEntityModel>? = getInstance()?.getListItems(categories_local_id, isDeleteLocal, isFakePin)
            val mData: MutableList<ItemModel> = ArrayList<ItemModel>()
            if (mList != null) {
                for (index in mList) {
                    mData.add(ItemModel(index))
                }
                return mData
            }
        } catch (e: Exception) {
        }
        return null
    }

    fun getListSyncData(isSyncCloud: Boolean, isSaver: Boolean, isFakePin: Boolean): MutableList<ItemModel>? {
        try {
            val mList: MutableList<ItemEntityModel>? = getInstance()?.getListSyncData(isSyncCloud, isSaver, isFakePin)
            val mData: MutableList<ItemModel> = ArrayList<ItemModel>()
            if (mList != null) {
                for (index in mList) {
                    mData.add(ItemModel(index))
                }
                return mData
            }
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
        return null
    }

    fun getListItemId(isSyncCloud: Boolean, isFakePin: Boolean): MutableList<ItemModel>? {
        try {
            val mList: MutableList<ItemEntityModel>? = getInstance()?.getListItemId(isSyncCloud, isFakePin)
            val mData: MutableList<ItemModel> = ArrayList<ItemModel>()
            if (mList != null) {
                for (index in mList) {
                    mData.add(ItemModel(index))
                }
                return mData
            }
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
        return null
    }

    fun getLatestId(categories_local_id: String?, isDeleteLocal: Boolean, isFakePin: Boolean): ItemModel? {
        try {
            val mResult: ItemEntityModel? = getInstance()?.getLatestId(categories_local_id, isDeleteLocal, isFakePin)
            if (mResult != null) {
                return ItemModel(mResult)
            }
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
        return null
    }

    fun getListSyncData(isSyncCloud: Boolean, isFakePin: Boolean): MutableList<ItemModel>? {
        try {
            val mList: MutableList<ItemEntityModel>? = getInstance()?.getListSyncData(isSyncCloud, isFakePin)
            val mData: MutableList<ItemModel> = ArrayList<ItemModel>()
            if (mList != null) {
                for (index in mList) {
                    mData.add(ItemModel(index))
                }
                return mData
            }
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
        return null
    }

    fun getListAllItems(isDeleteLocal: Boolean, isFakePin: Boolean): MutableList<ItemModel>? {
        try {
            val mList: MutableList<ItemEntityModel>? = getInstance()?.getListAllItems(isDeleteLocal, isFakePin)
            val mData: MutableList<ItemModel> = ArrayList<ItemModel>()
            if (mList != null) {
                for (index in mList) {
                    mData.add(ItemModel(index))
                }
                return mData
            }
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
        return null
    }

    fun getListAllItemsSaved(isSaved: Boolean, isSyncCloud: Boolean): MutableList<ItemModel>? {
        try {
            val mList: MutableList<ItemEntityModel>? = getInstance()?.getListAllItemsSaved(isSaved, isSyncCloud)
            val mData: MutableList<ItemModel> = ArrayList<ItemModel>()
            if (mList != null) {
                for (index in mList) {
                    mData.add(ItemModel(index))
                }
                return mData
            }
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
        return null
    }

    fun getListAllItems(isFakePin: Boolean): MutableList<ItemModel>? {
        try {
            val mList: MutableList<ItemEntityModel>? = getInstance()?.getListAllItems(isFakePin)
            val mData: MutableList<ItemModel> = ArrayList<ItemModel>()
            if (mList != null) {
                for (index in mList) {
                    mData.add(ItemModel(index))
                }
                return mData
            }
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
        return null
    }

    fun getCategoriesLocalId(categories_local_id: String?): MainCategoryModel? {
        try {
            val mResut: MainCategoryEntityModel? = getInstance()?.getCategoriesLocalId(categories_local_id)
            if (mResut != null) {
                return MainCategoryModel(mResut)
            }
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
        return null
    }

    fun getBreakInAlertsList(): MutableList<BreakInAlertsModel>? {
        val mResult: MutableList<BreakInAlertsEntityModel>? = getInstance()?.getBreakInAlertsList()
        val mList: MutableList<BreakInAlertsModel> = ArrayList<BreakInAlertsModel>()
        if (mResult != null) {
            for (index in mResult) {
                mList.add(BreakInAlertsModel(index))
            }
        }
        return mList
    }

    fun onInsert(cTalkManager: BreakInAlertsModel?) {
        try {
            if (cTalkManager == null) {
                return
            }
            getInstance()?.onInsert(BreakInAlertsEntityModel(cTalkManager))
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
    }

    fun getLatestItem(): Int {
        try {
            return getInstance()!!.getLatestItem()
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
        return 0
    }

    fun onDelete(cTalkManager: BreakInAlertsModel) {
        try {
            getInstance()?.onDelete(BreakInAlertsEntityModel(cTalkManager))
        } catch (e: Exception) {
            Utils.Log(TAG, e.message +"")
        }
    }

    fun onCleanDatabase() {
        getInstance()?.onCleanDatabase()
    }

    fun getInstance(): InstanceGenerator? {
        return InstanceGenerator.getInstance(SuperSafeApplication.getInstance())
    }
}