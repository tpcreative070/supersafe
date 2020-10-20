package co.tpcreative.supersafe.common.request;
import java.io.Serializable;

import co.tpcreative.supersafe.common.services.SuperSafeApplication;
import co.tpcreative.supersafe.common.util.Utils;
import co.tpcreative.supersafe.model.Authorization;
import co.tpcreative.supersafe.model.User;

public class UserRequest implements Serializable {
    public String user_id;
    public String device_id;
    public String session_token;
    public String refresh_token;
    public String public_key;

    /*Refresh user token*/
    public UserRequest(){
        final User mUser = Utils.getUserInfo();
        if (mUser==null){
            return;
        }
        final Authorization mAuthor = mUser.author;
        if (mAuthor==null){
            return;
        }
        this.user_id = mUser.email;
        this.refresh_token = mAuthor.refresh_token;
        this.public_key = mAuthor.public_key;
        this.device_id = SuperSafeApplication.getInstance().getDeviceId();
        this.session_token = Utils.getAccessToken();
    }
}
