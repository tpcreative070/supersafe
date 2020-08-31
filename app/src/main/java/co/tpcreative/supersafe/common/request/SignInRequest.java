package co.tpcreative.supersafe.common.request;

import java.io.Serializable;

import co.tpcreative.supersafe.model.Authorization;

public class SignInRequest extends Authorization implements  Serializable{
    public String email;
    public String password;
}
