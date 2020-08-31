package co.tpcreative.supersafe.model;
import androidx.annotation.NonNull;
import java.util.Date;

import co.tpcreative.supersafe.common.entities.ItemEntity;
import co.tpcreative.supersafe.common.entities.MainCategoryEntity;

public class Event {

    @NonNull
    private ItemModel items;
    @NonNull
    private MainCategoryModel mainCategories;
    @NonNull
    EnumEvent event;
    @NonNull
    private Date date;

    @NonNull
    public Date getDate() {
        return date;
    }

    @NonNull
    public ItemModel getItems() {
        return items;
    }

    @NonNull
    public MainCategoryModel getMainCategories() {
        return mainCategories;
    }

    @NonNull
    public EnumEvent getEvent() {
        return event;
    }

    public Event(@NonNull ItemModel items, @NonNull MainCategoryModel mainCategories, @NonNull EnumEvent event, @NonNull Date date) {
        this.items = items;
        this.mainCategories = mainCategories;
        this.event = event;
        this.date = date;
    }


}
