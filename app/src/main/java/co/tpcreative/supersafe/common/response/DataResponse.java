package co.tpcreative.supersafe.common.response;
import java.io.Serializable;
import java.util.List;

import co.tpcreative.supersafe.common.entities.ItemEntity;
import co.tpcreative.supersafe.common.entities.MainCategoryEntity;
import co.tpcreative.supersafe.model.Authorization;
import co.tpcreative.supersafe.model.EmailToken;
import co.tpcreative.supersafe.model.ItemModel;
import co.tpcreative.supersafe.model.MainCategoryModel;
import co.tpcreative.supersafe.model.Premium;
import co.tpcreative.supersafe.model.User;

public class DataResponse  implements Serializable  {
    /*User*/
    public User user;
    /*Main category*/
    public List<MainCategoryModel> categoriesList;
    public MainCategoryModel category;
    /*Items*/
    public List<ItemModel>itemsList;

    /*user cloud*/
    public UserCloudResponse userCloud;

    /*RequestCode*/
    public RequestCodeResponse requestCode;

    public Authorization author;

    public Premium premium;
    public EmailToken email_token;

    public String nextPage;
}
