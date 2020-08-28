package co.tpcreative.supersafe.common.response;

import java.io.Serializable;
import java.util.List;

import co.tpcreative.supersafe.common.api.response.BaseResponse;
import co.tpcreative.supersafe.common.entities.ItemEntity;
import co.tpcreative.supersafe.common.entities.MainCategoryEntity;

public class SyncResponse extends BaseResponse implements Serializable {
    public List<ItemEntity>files;
    public List<MainCategoryEntity>listCategories;
}
