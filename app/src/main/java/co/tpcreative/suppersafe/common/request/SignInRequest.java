package co.tpcreative.suppersafe.common.request;

import co.tpcreative.suppersafe.model.Authorization;

public class SignInRequest extends Authorization{
    public String email;
    public String password;
}
