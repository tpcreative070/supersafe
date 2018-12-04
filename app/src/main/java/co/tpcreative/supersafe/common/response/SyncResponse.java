package co.tpcreative.supersafe.common.response;

import java.io.Serializable;
import java.util.List;

import co.tpcreative.supersafe.common.api.response.BaseResponse;
import co.tpcreative.supersafe.model.Items;
import co.tpcreative.supersafe.model.MainCategories;
import co.tpcreative.supersafe.model.SyncData;

public class SyncResponse extends BaseResponse implements Serializable {
    public List<Items>files;
    public List<MainCategories>listCategories;
}
