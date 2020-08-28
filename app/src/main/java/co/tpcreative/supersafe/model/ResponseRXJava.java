package co.tpcreative.supersafe.model;

import co.tpcreative.supersafe.common.entities.ItemEntity;
import co.tpcreative.supersafe.common.entities.MainCategoryEntity;

public class ResponseRXJava {
    public boolean isWorking;
    public ItemEntity items;
    public String originalPath;
    public MainCategoryEntity categories;
    public int position;
}
