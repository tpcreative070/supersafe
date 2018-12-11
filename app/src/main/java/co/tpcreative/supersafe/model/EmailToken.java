package co.tpcreative.supersafe.model;
import java.io.Serializable;
import java.util.ArrayList;

import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;

public class EmailToken implements Serializable {

    public Message message;
    public String code;
    public boolean saveToSentItems;
    public String client_id;
    public String redirect_uri;
    public String grant_type;
    public String refresh_token;
    public String access_token;
    public String token_type;

    private static EmailToken instance;

    public static EmailToken getInstance(){
        if (instance==null){
            instance = new EmailToken();
        }
        return instance;
    }

    public EmailToken convertObject(final User mUser,EnumStatus status){
        String code = mUser.code;
        final EmailToken token = new EmailToken();
        token.access_token = mUser.email_token.access_token;
        token.refresh_token = mUser.email_token.refresh_token;
        token.grant_type = mUser.email_token.grant_type;
        token.client_id = mUser.email_token.client_id;
        token.redirect_uri = mUser.email_token.redirect_uri;
        token.saveToSentItems = false;

        EmailToken.Message messages = new EmailToken.Message();

        String subject = String.format(SuperSafeApplication.getInstance().getString(R.string.send_code_title),code);
        messages.subject = subject;

        EmailToken.Body body = new EmailToken.Body();
        body.contentType = "HTML";

        String content = "";
        switch (status){
            case SIGN_IN:{
                content =   Email.getInstance().getValue(code, "SignIn");
                break;
            }
            case RESET:{
                content =   Email.getInstance().getValue(code, "Reset");
                break;
            }
        }

        body.content = content;
        messages.body = body;

        EmailToken.EmailAddress emailAddress = new EmailToken.EmailAddress();
        emailAddress.address = mUser.email;

        EmailToken.EmailObject emailObject = new EmailToken.EmailObject();
        emailObject.emailAddress = emailAddress;

        messages.toRecipients.add(emailObject);

        token.message = messages;
        token.code = mUser.code;
        return token;
    }

    public EmailToken convertTextObject(final User mUser,final String content){
        final EmailToken token = new EmailToken();
        token.access_token = mUser.email_token.access_token;
        token.refresh_token = mUser.email_token.refresh_token;
        token.grant_type = mUser.email_token.grant_type;
        token.client_id = mUser.email_token.client_id;
        token.redirect_uri = mUser.email_token.redirect_uri;
        token.saveToSentItems = false;

        EmailToken.Message messages = new EmailToken.Message();

        String subject = String.format(SuperSafeApplication.getInstance().getString(R.string.support_help_title));
        String result_content = String.format(SuperSafeApplication.getInstance().getString(R.string.support_help_email),mUser.email,content);

        messages.subject = subject;

        EmailToken.Body body = new EmailToken.Body();
        body.contentType = "TEXT";


        body.content = result_content;
        messages.body = body;

        EmailToken.EmailAddress emailAddress = new EmailToken.EmailAddress();
        emailAddress.address = SuperSafeApplication.getInstance().getString(R.string.care_email);

        EmailToken.EmailObject emailObject = new EmailToken.EmailObject();
        emailObject.emailAddress = emailAddress;

        messages.toRecipients.add(emailObject);

        token.message = messages;
        token.code = mUser.code;
        return token;
    }



    public static class Message implements Serializable{
        public String subject;
        public Body body;
        public ArrayList<EmailObject> toRecipients = new ArrayList<EmailObject>();
        // Getter Methods
    }

    public static class Body implements Serializable{
        public String contentType;
        public String content;
    }

    public static class EmailObject implements Serializable{
       public EmailAddress emailAddress;
    }

    public static class EmailAddress implements Serializable{
        public String address;
    }



}