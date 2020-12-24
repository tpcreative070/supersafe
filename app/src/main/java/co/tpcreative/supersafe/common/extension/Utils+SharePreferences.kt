package co.tpcreative.supersafe.common.extension
import android.app.Activity
import android.content.Context
import android.os.PowerManager
import co.tpcreative.supersafe.BuildConfig
import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.controller.PrefsController
import co.tpcreative.supersafe.common.services.SuperSafeApplication
import co.tpcreative.supersafe.common.util.Utils
import co.tpcreative.supersafe.model.CheckoutItems
import co.tpcreative.supersafe.model.EnumPinAction
import co.tpcreative.supersafe.model.User
import com.google.gson.Gson

fun Utils.getScreenStatus() : Int{
    return PrefsController.getInt(getString(R.string.key_screen_status), EnumPinAction.NONE.ordinal)
}

fun Utils.putScreenStatus(value: Int){
    Log(TAG,"putScreenStatus $value")
    return PrefsController.putInt(getString(R.string.key_screen_status), value)
}

fun Utils.getHomePressed() : Boolean{
    return PrefsController.getBoolean(getString(R.string.key_home_pressed), false)
}

fun Utils.putHomePressed(value: Boolean){
    return PrefsController.putBoolean(getString(R.string.key_home_pressed), value)
}

fun Utils.isScreenOn(activity: Activity) : Boolean {
    val pm = activity.getSystemService(Context.POWER_SERVICE) as PowerManager
    if (pm.isInteractive) {
        return true
    }
    return false
}

fun Utils.putFaceDown(value : Boolean){
    PrefsController.putBoolean(getString(R.string.key_face_down_lock), value)
}

fun Utils.isFaceDown() : Boolean{
    return PrefsController.getBoolean(getString(R.string.key_face_down_lock), false)
}

fun Utils.putSecretDoor(value : Boolean){
    PrefsController.putBoolean(getString(R.string.key_secret_door),value)
}

fun Utils.isSecretDoor() : Boolean{
    return PrefsController.getBoolean(getString(R.string.key_secret_door),false)
}

fun Utils.putSecretDoorOfCalculator(value : Boolean){
    PrefsController.putBoolean(getString(R.string.key_calculator),value)
}

fun Utils.isSecretDoorOfCalculator() : Boolean{
    return PrefsController.getBoolean(getString(R.string.key_calculator),false)
}

fun Utils.putFistCalculator(value : Boolean){
    PrefsController.putBoolean(getString(R.string.is_first_calculator),value)
}

fun Utils.isFirstCalculator() : Boolean{
    return PrefsController.getBoolean(getString(R.string.is_first_calculator),false)
}

fun Utils.putFistScanVirus(value : Boolean){
    PrefsController.putBoolean(getString(R.string.is_first_scan_virus),value)
}

fun Utils.isFirstScanVirus() : Boolean{
    return PrefsController.getBoolean(getString(R.string.is_first_scan_virus),false)
}

fun Utils.isEnabledTwoFactoryAuthentication() : Boolean{
    return PrefsController.getBoolean(getString(R.string.key_enable_two_factor_authentication),false)
}

fun Utils.isRunning() : Boolean {
    return  PrefsController.getBoolean(getString(R.string.key_running), false)
}
fun Utils.putIsRunning(value : Boolean){
    PrefsController.putBoolean(getString(R.string.key_running),value)
}

fun Utils.putSeekTo(value : Long){
    PrefsController.putLong(getString(R.string.key_seek_to), value)
}

fun Utils.getSeekTo() : Long {
    return PrefsController.getLong(getString(R.string.key_seek_to),0)
}

fun Utils.putLastWindowIndex(value : Int){
    PrefsController.putInt(getString(R.string.key_lastWindowIndex), value)
}

fun Utils.getLastWindowIndex() : Int {
    return PrefsController.getInt(getString(R.string.key_lastWindowIndex),0)
}

fun Utils.putRotation(value : Boolean){
    PrefsController.putBoolean(getString(R.string.key_rotate), value)
}

fun Utils.getRotation() : Boolean {
    return PrefsController.getBoolean(getString(R.string.key_rotate),false)
}

fun Utils.putFirstFiles(value : Boolean){
    PrefsController.putBoolean(getString(R.string.key_is_first_files), value)
}

fun Utils.isFirstFiles() : Boolean {
    return PrefsController.getBoolean(getString(R.string.key_is_first_files),false)
}

fun Utils.putCountToRate(value : Int){
    PrefsController.putInt(getString(R.string.key_count_to_rate), value)
}

fun Utils.getCountToRate() : Int {
    return PrefsController.getInt(getString(R.string.key_count_to_rate),0)
}

fun Utils.putFirstEnableSyncData(value : Boolean){
    PrefsController.putBoolean(getString(R.string.key_is_first_enable_sync_data), value)
}

fun Utils.isFirstEnableSyncData() : Boolean {
    return PrefsController.getBoolean(getString(R.string.key_is_first_enable_sync_data),false)
}

fun Utils.setLastTimeSyncData(value: String){
    PrefsController.putString(getString(R.string.key_last_time_sync_data), value)
}

fun Utils.getLastTimeSyncData() : String {
    return PrefsController.getString(getString(R.string.key_last_time_sync_data), "") ?: ""
}

fun Utils.setRequestSyncData(value: Boolean){
    PrefsController.putBoolean(getString(R.string.key_request_sync_data), value)
}

fun Utils.isRequestSyncData() : Boolean {
    return PrefsController.getBoolean(getString(R.string.key_request_sync_data), true)
}

fun Utils.putCheckoutItems(checkoutItems: CheckoutItems?) {
    PrefsController.putString(getString(R.string.key_checkout_items), Gson().toJson(checkoutItems))
}

fun Utils.getCheckoutItems(): CheckoutItems? {
    val value: String? = PrefsController.getString(getString(R.string.key_checkout_items), null)
    if (value != null) {
        val mResult: CheckoutItems? = Gson().fromJson(value, CheckoutItems::class.java)
        if (mResult != null) {
            return mResult
        }
    }
    return null
}

fun Utils.putBreakAlert(value: Boolean){
    PrefsController.putBoolean(getString(R.string.key_break_in_alert), value)
}

fun Utils.isBreakAlert() : Boolean{
    return PrefsController.getBoolean(getString(R.string.key_break_in_alert), false)
}

fun Utils.getUserInfo(): User? {
    try {
        val value: String? = PrefsController.getString(getString(R.string.key_user), null)
        if (value != null) {
            val mUser: User? = Gson().fromJson(value, User::class.java)
            if (mUser != null) {
                return mUser
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

fun Utils.putUserPreShare(user: User?) {
    Log(TAG, "User id ====================> ${user?.email}")
    Log(TAG, "Cloud id ===================> ${user?.cloud_id}")
    PrefsController.putString(getString(R.string.key_user), Gson().toJson(user))
}

fun Utils.isRequestGoogleDriveSignOut() : Boolean{
    return PrefsController.getBoolean(getString(R.string.key_request_sign_out_google_drive), false)
}

fun Utils.putRequestGoogleDriveSignOut(value: Boolean){
    PrefsController.putBoolean(getString(R.string.key_request_sign_out_google_drive), value)
}

/*Check saver space*/
fun Utils.isSaverSpace(): Boolean {
    return PrefsController.getBoolean(getString(R.string.key_saving_space), false)
}

fun Utils.putSaverSpace(isSaver: Boolean){
    PrefsController.putBoolean(getString(R.string.key_saving_space), isSaver)
}


fun Utils.putFacePin(value : Boolean){
    PrefsController.putBoolean(getString(R.string.key_fake_pin),value)
}

fun Utils.isFacePin(): Boolean {
    return PrefsController.getBoolean(getString(R.string.key_fake_pin), false)
}

fun Utils.putCurrentCodeVersion(value : Int){
    PrefsController.putInt(getString(R.string.current_code_version),value)
}

fun Utils.getCurrentCodeVersion(): Int {
    return PrefsController.getInt(getString(R.string.current_code_version), 0)
}

fun Utils.onCheckNewVersion() {
    if (getCurrentCodeVersion() == BuildConfig.VERSION_CODE) {
        Log(TAG, "Already install this version")
    } else {
        putCurrentCodeVersion(BuildConfig.VERSION_CODE)
        Log(TAG, "New install this version")
    }
}

fun Utils.onUpdatedCountRate() {
    val mCount = Utils.getCountToRate()
    if (mCount > 999) {
        Utils.putCountToRate(0)
    } else {
        Utils.putCountToRate(mCount+1)
    }
}

fun Utils.isCheckSyncSuggestion(): Boolean {
    val name: String = SuperSafeApplication.getInstance().getString(R.string.key_count_sync)
    val mCount: Int = PrefsController.getInt(name, 0)
    val mSynced = getUserInfo()?.driveConnected
    mSynced?.let {
        if (!it) {
            if (mCount == 5) {
                PrefsController.putInt(name, 0)
                return true
            } else {
                PrefsController.putInt(name, mCount + 1)
            }
        }
    }
    return false
}

