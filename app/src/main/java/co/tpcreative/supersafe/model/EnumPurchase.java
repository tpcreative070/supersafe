package co.tpcreative.supersafe.model;
import co.tpcreative.supersafe.R;
import co.tpcreative.supersafe.common.services.SuperSafeApplication;

public enum EnumPurchase {
    SIX_MONTHS(SuperSafeApplication.getInstance().getString(R.string.six_months)),
    ONE_YEAR(SuperSafeApplication.getInstance().getString(R.string.one_years)),
    LIFETIME(SuperSafeApplication.getInstance().getString(R.string.life_time)),
    NONE("");

    private String text;
    EnumPurchase(String text) {
        this.text = text;
    }

    public String getText() {
        return this.text;
    }


    public static EnumPurchase fromString(String text) {
        for (EnumPurchase b : EnumPurchase.values()) {
            if (b.text.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return EnumPurchase.LIFETIME;
    }
}
