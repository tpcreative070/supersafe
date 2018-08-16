package co.tpcreative.suppersafe.common.request;
import co.tpcreative.suppersafe.model.Authorization;

public class SignUpRequest extends Authorization {
    public String email;
    public String password;
    public String name;
}
