package co.tpcreative.supersafe.model;

import android.support.annotation.NonNull;

import java.util.Date;

public class Event {

    @NonNull
    private Items items;
    @NonNull
    private MainCategories mainCategories;
    @NonNull
    EnumEvent event;
    @NonNull
    private Date date;

    @NonNull
    public Date getDate() {
        return date;
    }

    @NonNull
    public Items getItems() {
        return items;
    }

    @NonNull
    public MainCategories getMainCategories() {
        return mainCategories;
    }

    @NonNull
    public EnumEvent getEvent() {
        return event;
    }

    public Event(@NonNull Items items, @NonNull MainCategories mainCategories, @NonNull EnumEvent event, @NonNull Date date) {
        this.items = items;
        this.mainCategories = mainCategories;
        this.event = event;
        this.date = date;
    }


}
