package co.tpcreative.supersafe.common.response;

import java.io.Serializable;
import java.util.List;

import co.tpcreative.supersafe.common.api.response.BaseResponse;
import co.tpcreative.supersafe.model.MainCategories;

public class SyncCategoriesResponse extends BaseResponse implements Serializable {
    public List<MainCategories>files;
    public MainCategories category;
}
