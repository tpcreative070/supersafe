package co.tpcreative.supersafe.common.request;
import com.google.gson.Gson;

import java.io.Serializable;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.model.DriveEvent;
import co.tpcreative.supersafe.model.Items;

public class SyncItemsRequest implements Serializable {

    public String user_id;
    public String device_id;
    public long size;
    public String cloud_id;
    public String kind;
    public String items_id;
    public String name;
    public String mimeType;
    public String global_original_id;
    public String global_thumbnail_id;
    public String categories_id;
    public boolean isSyncCloud;
    public boolean isSyncOwnServer;
    public boolean thumbnailSync;
    public boolean originalSync;
    public boolean isDeleteLocal;
    public boolean isDeleteGlobal;
    public String originalName;
    public String thumbnailName;
    public int formatType;
    public String thumbnailPath;
    public String originalPath;
    public String fileExtension;
    public int statusAction;
    public int degrees;
    public int fileType;
    public String title;
    public String nextPage;

    /*Sync items*/
    public SyncItemsRequest(String user_id,String cloud_id,String device_id,Items items){
        this.user_id = user_id;
        this.cloud_id = cloud_id;
        this.device_id = device_id;
        this.size = Long.parseLong(items.size);
        this.kind = SuperSafeApplication.getInstance().getString(R.string.key_drive_file);
        this.items_id = items.items_id;
        this.mimeType = items.mimeType;
        this.global_original_id = items.global_original_id;
        this.global_thumbnail_id = items.global_thumbnail_id;
        this.categories_id = items.categories_id;
        this.isSyncCloud = items.isSyncCloud;
        this.isSyncOwnServer = items.isSyncOwnServer;
        this.thumbnailSync = items.thumbnailSync;
        this.originalSync = items.originalSync;
        this.isDeleteLocal = items.isDeleteLocal;
        this.isDeleteGlobal = items.isDeleteGlobal;
        this.originalName = items.originalName;
        this.thumbnailName = items.thumbnailName;
        this.formatType = items.formatType;
        this.thumbnailPath = items.thumbnailPath;
        this.originalPath = items.originalPath;
        this.fileExtension = items.fileExtension;
        this.statusAction = items.statusAction;
        this.degrees = items.degrees;
        this.fileType = items.fileType;
        this.title = items.title;
        DriveEvent contentTitle = new DriveEvent();
        contentTitle.items_id = items.items_id;
        String hex = DriveEvent.getInstance().convertToHex(new Gson().toJson(contentTitle));
        this.name = hex;
    }

    /*Delete item*/
    public SyncItemsRequest(String user_id,String cloud_id,String items_id){
        this.user_id = user_id;
        this.cloud_id = cloud_id;
        this.items_id = items_id;
    }

    /*Get items list*/
    public SyncItemsRequest(String user_id,String cloud_id,String device_id,boolean isSyncCloud, String nextPage){
        this.user_id = user_id;
        this.cloud_id = cloud_id;
        this.isSyncCloud = isSyncCloud;
        this.nextPage = nextPage;
    }
}
