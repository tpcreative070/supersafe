package co.tpcreative.supersafe.model;

import co.tpcreative.supersafe.common.entities.ItemEntity;
import co.tpcreative.supersafe.common.entities.MainCategoryEntity;

public class Cover {

    public Categories categories;
    public MainCategoryEntity mainCategories;
    public ItemEntity items;
    public boolean isSelected;

    public int getCategoryId() {
        return categories.id;
    }

    public String getCategoryName() {
        return categories.name;
    }


}
