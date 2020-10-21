package co.tpcreative.supersafe.common.dialog
import android.content.Context
import androidx.core.content.ContextCompat
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.dialogimport.DialogListener
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme

class DialogManager {
    fun onStartDialog(context: Context, title: Int, content: Int, ls: DialogListener?) {
        val builder = MaterialDialog.Builder(context)
                .title(context.getString(title))
                .theme(Theme.LIGHT)
                .content(content)
                .titleColor(ContextCompat.getColor(context,R.color.black))
                .negativeText(context.getString(R.string.cancel))
                .positiveText(context.getString(R.string.yes))
                .onPositive { dialog, which ->
                    if (ls != null) {
                        ls.onClickButton()
                    }
                }
                .onNegative { dialog, which ->
                    if (ls != null) {
                        ls.dismiss()
                    }
                }
        builder.show()
    }

    companion object {
        private var instance: DialogManager? = null
        fun getInstance(): DialogManager? {
            if (instance == null) {
                instance = DialogManager()
            }
            return instance
        }
    }
}