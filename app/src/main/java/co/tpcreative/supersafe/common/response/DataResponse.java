package co.tpcreative.supersafe.common.response;
import java.io.Serializable;
import java.util.List;
import co.tpcreative.supersafe.common.api.response.BaseResponse;
import co.tpcreative.supersafe.model.Items;
import co.tpcreative.supersafe.model.MainCategories;
import co.tpcreative.supersafe.model.User;

public class DataResponse extends BaseResponse implements Serializable  {
    /*User*/
    public User user;
    /*Main category*/
    public List<MainCategories> categoriesList;
    public MainCategories category;
    /*Items*/
    public List<Items>itemsList;
    public int nextPage;

    /*user cloud*/
    public UserCloudResponse userCloud;

    /*RequestCode*/
    public RequestCodeResponse requestCode;
}
