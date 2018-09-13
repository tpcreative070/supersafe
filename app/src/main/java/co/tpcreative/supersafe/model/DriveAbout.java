package co.tpcreative.supersafe.model;
import java.io.Serializable;
import java.util.List;

import co.tpcreative.supersafe.common.api.response.BaseResponseDrive;
import co.tpcreative.supersafe.common.response.DriveResponse;

public class DriveAbout extends BaseResponseDrive implements Serializable{
    public String name;
    public String quotaBytesTotal;
    public String quotaBytesUsed;
    public String quotaBytesUsedAggregate;
    public String quotaBytesUsedInTrash;
    public String quotaType;
    public DriveUser user;

    /*Create folder*/

    public String kind ;
    public String id;
    public String mimeType;


    /*Drive api queries*/

    public List<DriveResponse> files;
    public boolean incompleteSearch;



}
