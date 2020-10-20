package co.tpcreative.supersafe.model

import co.tpcreative.supersafe.R
import co.tpcreative.supersafe.common.services.SuperSafeApplication

enum class EnumPurchase(private val text: String?) {
    SIX_MONTHS(SuperSafeApplication.getInstance().getString(R.string.six_months)), ONE_YEAR(SuperSafeApplication.Companion.getInstance().getString(R.string.one_years)), LIFETIME(SuperSafeApplication.Companion.getInstance().getString(R.string.life_time)), NONE("");
    fun getText(): String? {
        return text
    }
    companion object {
        fun fromString(text: String?): EnumPurchase? {
            for (b in co.tpcreative.supersafe.model.EnumPurchase.values()) {
                if (b.text.equals(text, ignoreCase = true)) {
                    return b
                }
            }
            return LIFETIME
        }
    }
}