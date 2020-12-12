package co.tpcreative.supersafe.model

import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import java.io.Serializable

class EmailToken : java.io.Serializable {
    var message: Message? = null
    var code: String? = null
    var saveToSentItems = false
    var client_id: String? = null
    var redirect_uri: String? = null
    var grant_type: String? = null
    var refresh_token: String? = null
    var access_token: String? = null
    var token_type: String? = null
    fun convertObject(mUser: User, status: EnumStatus): EmailToken {
        val code: String = mUser.code!!
        val token = EmailToken()
        token.access_token = mUser.email_token?.access_token
        token.refresh_token = mUser.email_token?.refresh_token
        token.grant_type = mUser.email_token?.grant_type
        token.client_id = mUser.email_token?.client_id
        token.redirect_uri = mUser.email_token?.redirect_uri
        token.saveToSentItems = false
        val messages = Message()
        val subject: String = String.format(SuperSafeApplication.getInstance().getString(R.string.send_code_title), code)
        messages.subject = subject
        val body = Body()
        body.contentType = "HTML"
        var content: String? = ""
        when (status) {
            EnumStatus.SIGN_IN -> {

                //content =   Email.getInstance().getValue(code, "SignIn");
                content = String.format(SuperSafeApplication.Companion.getInstance().getString(R.string.use_your_code), code, "SignIn")
            }
            EnumStatus.RESET -> {

                //content =   Email.getInstance().getValue(code, "Reset");
                content = String.format(SuperSafeApplication.Companion.getInstance().getString(R.string.use_your_code), code, "Reset")
            }
            EnumStatus.UNLOCK_ALBUMS -> {

                //content =   Email.getInstance().getValue(code, "Unlock albums");
                content = String.format(SuperSafeApplication.Companion.getInstance().getString(R.string.use_your_code), code, "Unlock albums")
            }
        }
        body.content = content
        messages.body = body
        val emailAddress = EmailAddress()
        emailAddress.address = mUser.email
        val emailObject = EmailObject()
        emailObject.emailAddress = emailAddress
        messages.toRecipients?.add(emailObject)
        token.message = messages
        token.code = mUser.code
        return token
    }

    fun convertTextObject(mUser: User, content: String): EmailToken? {
        val token = EmailToken()
        token.access_token = mUser.email_token?.access_token
        token.refresh_token = mUser.email_token?.refresh_token
        token.grant_type = mUser.email_token?.grant_type
        token.client_id = mUser.email_token?.client_id
        token.redirect_uri = mUser.email_token?.redirect_uri
        token.saveToSentItems = false
        val messages = co.tpcreative.supersafe.model.EmailToken.Message()
        val subject = String.format(SuperSafeApplication.Companion.getInstance().getString(R.string.support_help_title))
        val resultContent: String = kotlin.String.format(SuperSafeApplication.Companion.getInstance().getString(R.string.support_help_email), mUser.email, content)
        messages.subject = subject
        val body = co.tpcreative.supersafe.model.EmailToken.Body()
        body.contentType = "TEXT"
        body.content = resultContent
        messages.body = body
        val emailAddress = EmailAddress()
        emailAddress.address = SuperSafeApplication.Companion.getInstance().getString(R.string.care_email)
        val emailObject = EmailObject()
        emailObject.emailAddress = emailAddress
        messages.toRecipients?.add(emailObject)
        token.message = messages
        token.code = mUser.code
        return token
    }

    class Message : Serializable {
        var subject: String? = null
        var body: Body? = null
        var toRecipients: ArrayList<EmailObject?>? = ArrayList() // Getter Methods
    }

    class Body : Serializable {
        var contentType: String? = null
        var content: String? = null
    }

    class EmailObject : Serializable {
        var emailAddress: EmailAddress? = null
    }

    class EmailAddress : Serializable {
        var address: String? = null
    }

    companion object {
        private var instance: EmailToken? = null
        fun getInstance(): EmailToken? {
            if (instance == null) {
                instance = EmailToken()
            }
            return instance
        }
    }
}