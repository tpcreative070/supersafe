package co.tpcreative.supersafe.model

import co.tpcreative.supersafe.common.entitiesimport.MainCategoryEntity
import co.tpcreative.supersafe.model.Categories

class Cover {
    var categories: Categories? = null
    var mainCategories: MainCategoryEntity? = null
    var items: ItemEntity? = null
    var isSelected = false
    fun getCategoryId(): Int {
        return categories.id
    }

    fun getCategoryName(): String? {
        return categories.name
    }
}