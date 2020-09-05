package co.tpcreative.supersafe.model;

import androidx.room.Ignore;

import java.io.Serializable;
import java.util.Date;

import co.tpcreative.supersafe.common.util.Utils;

public class ItemModel implements Serializable {

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


    /*Added more custom fields*/
    public boolean isChecked;
    public boolean isDeleted;
    public boolean isOriginalGlobalId;
    public Date date;
    public String name ;
    /*Added more condition fields*/
    public String global_id;
    public String unique_id;

    /*Get data list from local db*/
    public ItemModel(ItemEntityModel items){
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
        this.unique_id = Utils.getUUId();
    }

    /*Get data list from local db*/
    public ItemModel(ItemEntityModel items,String categories_id){
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
        this.categories_id = categories_id;
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
        this.unique_id = Utils.getUUId();
    }

    /*Added item to list from fetch data*/
    public ItemModel(ItemModel items,EnumStatus enumStatus){
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
        this.statusAction = enumStatus.ordinal();
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
        this.unique_id = Utils.getUUId();
        EnumFormatType formatTypeFile = EnumFormatType.values()[formatType];
        switch (formatTypeFile) {
            case AUDIO: {
                this.originalSync = false;
                this.thumbnailSync = true;
                break;
            }
            case FILES: {
                this.originalSync = false;
                this.thumbnailSync = true;
                break;
            }
            default: {
                this.originalSync = false;
                this.thumbnailSync = false;
                break;
            }
        }
    }

    public ItemModel(boolean isSyncCloud, boolean isSyncOwnServer, boolean originalSync, boolean thumbnailSync, int degrees, int fileType, int formatType, String title, String originalName, String thumbnailName, String items_id, String originalPath, String thumbnailPath, String global_original_id, String global_thumbnail_id, String categories_id, String categories_local_id, String mimeType, String fileExtension, EnumStatus enumStatus, String size, int statusProgress, boolean isDeleteLocal, boolean isDeleteGlobal, int deleteAction, boolean isFakePin, boolean isSaver, boolean isExport, boolean isWaitingForExporting, int custom_items, boolean isUpdate){
        this.originalName = originalName;
        this.thumbnailName = thumbnailName;
        this.items_id = items_id;
        this.fileType = fileType;
        this.formatType = formatType;
        this.title = title;
        this.isSyncCloud = isSyncCloud;
        this.isSyncOwnServer = isSyncOwnServer;
        this.thumbnailSync = thumbnailSync;
        this.originalSync = originalSync;
        this.degrees = degrees;
        this.global_original_id = global_original_id;
        this.global_thumbnail_id = global_thumbnail_id;
        this.categories_id = categories_id;
        this.categories_local_id = categories_local_id;
        this.originalPath = originalPath;
        this.thumbnailPath = thumbnailPath;
        this.mimeType = mimeType;
        this.fileExtension = fileExtension;
        this.statusAction = enumStatus.ordinal();
        this.size = size;
        this.statusProgress = statusProgress;
        this.isDeleteLocal = isDeleteLocal ;
        this.isDeleteGlobal = isDeleteGlobal;
        this.deleteAction = deleteAction;
        this.isFakePin = isFakePin;
        this.isSaver = isSaver;
        this.isExport = isExport;
        this.isWaitingForExporting = isWaitingForExporting;
        this.custom_items = custom_items;
        this.isUpdate = isUpdate;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public ItemModel(){
        this.thumbnailName = "";
        this.isSyncCloud = false;
        this.isSyncOwnServer = false;
        this.isOriginalGlobalId = false;
        this.isUpdate = false;
    }
    public ItemModel(String fileExtension,
                     String originalPath,
                     String thumbnailPath,
                     String categories_id,
                     String categories_local_id,
                     String mimeType,
                     String uuId,
                     EnumFormatType formatType,
                     int degrees,
                     boolean thumbnailSync,
                     boolean originalSync,
                     String global_original_id,
                     String global_thumbnail_id,
                     EnumFileType fileType,
                     String originalName,
                     String name,
                     String thumbnailName,
                     String size,
                     EnumStatusProgress progress,
                     boolean isDeleteLocal,
                     boolean isDeleteGlobal,
                     EnumDelete deleteAction,
                     boolean isFakePin,
                     boolean isSaver,
                     boolean isExport,
                     boolean isWaitingForExporting,
                     int custom_items,
                     boolean isSyncCloud,
                     boolean isSyncOwnServer,
                     boolean isUpdate,
                     EnumStatus statusAction
    ){
        this.fileExtension = fileExtension;
        this.originalPath = originalPath;
        this.thumbnailPath = thumbnailPath;
        this.categories_id = categories_id;
        this.categories_local_id = categories_local_id;
        this.mimeType = mimeType;
        this.items_id = uuId;
        this.formatType = formatType.ordinal();
        this.degrees = degrees;
        this.thumbnailSync = thumbnailSync;
        this.originalSync = originalSync;
        this.global_original_id = global_original_id;
        this.global_thumbnail_id = global_thumbnail_id;
        this.fileType = fileType.ordinal();
        this.originalName = originalName;
        this.title = name;
        this.thumbnailName = thumbnailName;
        this.size = size;
        this.statusProgress = progress.ordinal();
        this.isDeleteLocal = isDeleteLocal;
        this.isDeleteGlobal = isDeleteGlobal;
        this.deleteAction = deleteAction.ordinal();
        this.isFakePin = isFakePin;
        this.isSaver = isSaver;
        this.isExport = isExport;
        this.isWaitingForExporting = isWaitingForExporting;
        this.custom_items = custom_items;
        this.isSyncCloud = isSyncCloud;
        this.isSyncOwnServer = isSyncOwnServer;
        this.isUpdate = isUpdate;
        this.statusAction = statusAction.ordinal();
    }


    /*Merged request list for sync data*/
    public ItemModel(ItemModel items,boolean isOriginalGlobalId ){
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
        /*Added more condition fields*/
        this.unique_id = Utils.getUUId();
        if (isOriginalGlobalId){
            global_id =  items.global_original_id;
        }else{
            global_id =  items.global_thumbnail_id;
        }
        this.isOriginalGlobalId = isOriginalGlobalId;
    }
}
