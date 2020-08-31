package co.tpcreative.supersafe.common.response;

import java.io.Serializable;

import co.tpcreative.supersafe.common.api.response.BaseResponse;

public class RootResponse extends BaseResponse implements Serializable {
    public DataResponse data;
}
