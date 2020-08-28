package co.tpcreative.supersafe.common.response;

import java.io.Serializable;
import java.util.List;

import co.tpcreative.supersafe.common.api.response.BaseResponse;
import co.tpcreative.supersafe.common.entities.MainCategoryEntity;
import co.tpcreative.supersafe.model.MainCategoryModel;

public class SyncCategoriesResponse extends BaseResponse implements Serializable {
    public List<MainCategoryModel>files;
    public MainCategoryModel category;
}
