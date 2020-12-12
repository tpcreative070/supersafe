package co.tpcreative.supersafe.common.dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import co.tpcreative.supersafe.R

class DialogFragmentAskSignIn : DialogFragment() {
    private var mEditText: EditText? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_fragment_ask_signin, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Get field from view
        mEditText = view.findViewById<View?>(R.id.txt_your_name) as EditText?
        // Fetch arguments from bundle and set title
        val title = arguments?.getString("title", "Enter Name")
        dialog?.setTitle(title)
        // Show soft keyboard automatically and request focus to field
        mEditText?.requestFocus()
        dialog?.getWindow()?.setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
    }

    companion object {
        fun newInstance(title: String?): DialogFragmentAskSignIn? {
            val frag = DialogFragmentAskSignIn()
            val args = Bundle()
            args.putString("title", title)
            frag.arguments = args
            return frag
        }
    }
}