package co.tpcreative.supersafe.model;
import java.io.Serializable;

public class SyncItemModel implements Serializable {
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


    /*Added more condition fields*/
    public String global_id;
    public boolean isOriginalGlobalId;

    public SyncItemModel(ItemModel itemModel,boolean isOriginalGlobalId){
        this.id = itemModel.id;
        this.originalName = itemModel.originalName;
        this.thumbnailName = itemModel.thumbnailName;
        this.isSyncCloud = itemModel.isSyncCloud;
        this.isSyncOwnServer = itemModel.isSyncOwnServer;
        this.global_original_id = itemModel.global_original_id;
        this.global_thumbnail_id  = itemModel.global_thumbnail_id;
        this.categories_local_id = itemModel.categories_local_id;
        this.categories_id = itemModel.categories_id;
        this.formatType = itemModel.formatType;
        this.thumbnailPath = itemModel.thumbnailPath;
        this.originalPath = itemModel.originalPath;
        this.mimeType = itemModel.mimeType;
        this.fileExtension = itemModel.fileExtension;
        this.statusAction = itemModel.statusAction;
        this.items_id = itemModel.items_id;
        this.degrees = itemModel.degrees;
        this.thumbnailSync = itemModel.thumbnailSync;
        this.originalSync = itemModel.originalSync;
        this.fileType = itemModel.fileType;
        this.title = itemModel.title;
        this.size = itemModel.size;
        this.isDeleteLocal = itemModel.isDeleteLocal;
        this.isDeleteGlobal = itemModel.isDeleteGlobal;
        this.statusProgress = itemModel.statusProgress;
        this.deleteAction = itemModel.deleteAction;
        this.isFakePin = itemModel.isFakePin;
        this.isSaver = itemModel.isSaver;
        this.isExport = itemModel.isExport;
        this.isWaitingForExporting = itemModel.isWaitingForExporting;
        this.custom_items = itemModel.custom_items;
        this.isUpdate = itemModel.isUpdate;

        /*Added more condition fields*/
        if (isOriginalGlobalId){
            global_id =  itemModel.global_original_id;
        }else{
            global_id =  itemModel.global_thumbnail_id;
        }
        this.isOriginalGlobalId = isOriginalGlobalId;
    }
}
