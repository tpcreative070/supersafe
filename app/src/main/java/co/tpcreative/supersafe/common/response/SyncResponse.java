package co.tpcreative.supersafe.common.response;

import java.io.Serializable;
import java.util.List;

import co.tpcreative.supersafe.common.api.response.BaseResponse;

public class SyncResponse extends BaseResponse implements Serializable {
    public List<DriveResponse>files;
}