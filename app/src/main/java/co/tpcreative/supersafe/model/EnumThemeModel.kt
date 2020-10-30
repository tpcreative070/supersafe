package co.tpcreative.supersafe.model
enum class EnumThemeModel(ord : Int) {
    LIGHT(ord = 0),
    DARK(ord = 1),
    DEFAULT(ord = 1);
    companion object {
        fun byPosition(ord: Int): EnumThemeModel {
            return values()[ord]
        }
    }
}