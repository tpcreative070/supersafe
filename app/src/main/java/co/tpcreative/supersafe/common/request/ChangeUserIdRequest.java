package co.tpcreative.supersafe.common.request;

import java.io.Serializable;

import co.tpcreative.supersafe.common.util.Utils;

public class ChangeUserIdRequest implements Serializable {
   public String user_id;
   public String new_user_id;
   public String  _id;
   public String session_token;
   public ChangeUserIdRequest(VerifyCodeRequest request){
       this.user_id = request.user_id;
       this.new_user_id = request.new_user_id;
       this._id = request._id;
       this.session_token = Utils.getAccessToken();
   }
}
