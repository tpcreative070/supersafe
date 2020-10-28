package co.tpcreative.supersafe.common.dialog
import android.content.Context
import co.tpcreative.supersafe.R
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.checkbox.checkBoxPrompt

class DialogManager {
    fun onStartDialog(context: Context, title: Int, content: Int, ls: DialogListener?) {
        val builder = MaterialDialog(context).show {
            title (title)
            message(content)
            negativeButton(R.string.cancel){
                ls?.dismiss()
            }
            positiveButton(R.string.yes){
            }
        }
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