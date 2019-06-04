package co.tpcreative.supersafe.model;
import java.io.Serializable;
import java.util.List;

import co.tpcreative.supersafe.common.api.response.BaseResponseDrive;
import co.tpcreative.supersafe.common.response.DriveResponse;

public class DriveAbout extends BaseResponseDrive implements Serializable{
    public long inAppUsed;
    public DriveUser user;
    public StorageQuota storageQuota;
    /*Create folder*/
    /*Drive api queries*/
    public List<DriveResponse> files;
}
