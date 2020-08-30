package co.tpcreative.supersafe.common.request;
import co.tpcreative.supersafe.model.Authorization;

public class SignUpRequest extends Authorization {
    public String email;
    public String password;
    public String name;
}
