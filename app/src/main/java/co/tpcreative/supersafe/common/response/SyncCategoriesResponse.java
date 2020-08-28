package co.tpcreative.supersafe.common.response;

import java.io.Serializable;
import java.util.List;

import co.tpcreative.supersafe.common.api.response.BaseResponse;
import co.tpcreative.supersafe.common.entities.MainCategoryEntity;

public class SyncCategoriesResponse extends BaseResponse implements Serializable {
    public List<MainCategoryEntity>files;
    public MainCategoryEntity category;
}
