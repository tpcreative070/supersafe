package co.tpcreative.supersafe.model;
import androidx.annotation.NonNull;
import java.util.Date;

import co.tpcreative.supersafe.common.entities.ItemEntity;
import co.tpcreative.supersafe.common.entities.MainCategoryEntity;

public class Event {

    @NonNull
    private ItemEntity items;
    @NonNull
    private MainCategoryEntity mainCategories;
    @NonNull
    EnumEvent event;
    @NonNull
    private Date date;

    @NonNull
    public Date getDate() {
        return date;
    }

    @NonNull
    public ItemEntity getItems() {
        return items;
    }

    @NonNull
    public MainCategoryEntity getMainCategories() {
        return mainCategories;
    }

    @NonNull
    public EnumEvent getEvent() {
        return event;
    }

    public Event(@NonNull ItemEntity items, @NonNull MainCategoryEntity mainCategories, @NonNull EnumEvent event, @NonNull Date date) {
        this.items = items;
        this.mainCategories = mainCategories;
        this.event = event;
        this.date = date;
    }


}
