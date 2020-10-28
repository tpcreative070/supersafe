package co.tpcreative.supersafe.ui.help
import co.tpcreative.supersafe.common.util.Utils
import kotlinx.android.synthetic.main.activity_help_and_support_content.*

fun HelpAndSupportContentAct.iniUI(){
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    presenter = HelpAndSupportPresenter()
    presenter?.bindView(this)
    presenter?.onGetDataIntent(this)
    mUser = Utils.getUserInfo()
    tvEmail?.text = mUser?.email
    edtSupport?.addTextChangedListener(mTextWatcher)
    edtSupport?.setOnEditorActionListener(this)
}