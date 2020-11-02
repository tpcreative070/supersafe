package co.tpcreative.supersafe.model

import java.io.Serializable

class SyncDataModel(val list : MutableList<ItemModel>?, val categoryList : MutableList<MainCategoryModel>?,val isDone: Boolean) : Serializable {
}