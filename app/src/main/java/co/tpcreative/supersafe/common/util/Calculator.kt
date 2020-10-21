package co.tpcreative.supersafe.common.util
interface Calculator {
    open fun setValue(value: String?)
    open fun setValueDouble(d: Double)
    open fun setFormula(value: String?)
}