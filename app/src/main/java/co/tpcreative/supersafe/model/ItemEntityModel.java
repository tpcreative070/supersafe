package co.tpcreative.supersafe.model;

import co.tpcreative.supersafe.common.entities.ItemEntity;

public class ItemEntityModel {
    public int id;
    public String originalName;
    public String thumbnailName;
    public boolean isSyncCloud;
    public boolean isSyncOwnServer;
    public String global_original_id;
    public String global_thumbnail_id;
    public String categories_local_id;
    public String categories_id;
    public int formatType;
    public String thumbnailPath;
    public String originalPath;
    public String mimeType;
    public String fileExtension;
    public int statusAction;
    public String items_id;
    public int degrees;
    public boolean thumbnailSync;
    public boolean originalSync;
    public int fileType;
    public String title;
    public String size;
    public boolean isDeleteLocal;
    public boolean isDeleteGlobal;

    public int statusProgress;
    public int deleteAction;
    public boolean isFakePin;
    public boolean isSaver;
    public boolean isExport;
    public boolean isWaitingForExporting;
    public int custom_items;
    public boolean isUpdate;

    /*Push data to local db*/
    public ItemEntityModel(ItemModel items){
        this.id = items.id;
        this.originalName = items.originalName;
        this.thumbnailName = items.thumbnailName;
        this.items_id = items.items_id;
        this.fileType = items.fileType;
        this.formatType = items.formatType;
        this.title = items.title;
        this.isSyncCloud = items.isSyncCloud;
        this.isSyncOwnServer = items.isSyncOwnServer;
        this.thumbnailSync = items.thumbnailSync;
        this.originalSync = items.originalSync;
        this.degrees = items.degrees;
        this.global_original_id = items.global_original_id;
        this.global_thumbnail_id = items.global_thumbnail_id;
        this.categories_id = items.categories_id;
        this.categories_local_id = items.categories_local_id;
        this.originalPath = items.originalPath;
        this.thumbnailPath = items.thumbnailPath;
        this.mimeType = items.mimeType;
        this.fileExtension = items.fileExtension;
        this.statusAction = items.statusAction;
        this.size = items.size;
        this.statusProgress = items.statusProgress;
        this.isDeleteLocal = items.isDeleteLocal ;
        this.isDeleteGlobal = items.isDeleteGlobal;
        this.deleteAction = items.deleteAction;
        this.isFakePin = items.isFakePin;
        this.isSaver = items.isSaver;
        this.isExport = items.isExport;
        this.isWaitingForExporting = items.isWaitingForExporting;
        this.custom_items = items.custom_items;
        this.isUpdate = items.isUpdate;
    }

    public ItemEntityModel(ItemEntity items){
        this.id = items.id;
        this.originalName = items.originalName;
        this.thumbnailName = items.thumbnailName;
        this.items_id = items.items_id;
        this.fileType = items.fileType;
        this.formatType = items.formatType;
        this.title = items.title;
        this.isSyncCloud = items.isSyncCloud;
        this.isSyncOwnServer = items.isSyncOwnServer;
        this.thumbnailSync = items.thumbnailSync;
        this.originalSync = items.originalSync;
        this.degrees = items.degrees;
        this.global_original_id = items.global_original_id;
        this.global_thumbnail_id = items.global_thumbnail_id;
        this.categories_id = items.categories_id;
        this.categories_local_id = items.categories_local_id;
        this.originalPath = items.originalPath;
        this.thumbnailPath = items.thumbnailPath;
        this.mimeType = items.mimeType;
        this.fileExtension = items.fileExtension;
        this.statusAction = items.statusAction;
        this.size = items.size;
        this.statusProgress = items.statusProgress;
        this.isDeleteLocal = items.isDeleteLocal ;
        this.isDeleteGlobal = items.isDeleteGlobal;
        this.deleteAction = items.deleteAction;
        this.isFakePin = items.isFakePin;
        this.isSaver = items.isSaver;
        this.isExport = items.isExport;
        this.isWaitingForExporting = items.isWaitingForExporting;
        this.custom_items = items.custom_items;
        this.isUpdate = items.isUpdate;
    }

    public ItemEntityModel(){
        this.thumbnailName = "";
        this.isSyncCloud = false;
        this.isSyncOwnServer = false;
        this.isUpdate = false;
    }
}
