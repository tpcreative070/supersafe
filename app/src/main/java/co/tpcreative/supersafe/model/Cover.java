package co.tpcreative.supersafe.model;

public class Cover {

    public Categories categories;
    public MainCategories mainCategories;
    public Items items;
    public boolean isSelected;

    public int getCategoryId() {
        return categories.id;
    }

    public String getCategoryName() {
        return categories.name;
    }


}
