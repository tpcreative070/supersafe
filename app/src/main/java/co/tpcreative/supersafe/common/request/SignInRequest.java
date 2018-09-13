package co.tpcreative.supersafe.common.request;

import co.tpcreative.supersafe.model.Authorization;

public class SignInRequest extends Authorization{
    public String email;
    public String password;
}
