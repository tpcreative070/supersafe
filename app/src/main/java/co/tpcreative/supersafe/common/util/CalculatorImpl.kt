package co.tpcreative.supersafe.common.util
import co.tpcreative.supersafe.R

class CalculatorImpl {
    private var mDisplayedValue: String? = null
    private var mDisplayedFormula: String? = null
    private var mLastKey: String? = null
    private var mLastOperation: String? = null
    private var mCallback: Calculator?
    private var mIsFirstOperation = false
    private var mResetValue = false
    private var mBaseValue = 0.0
    private var mSecondValue = 0.0

    constructor(calculator: Calculator?) {
        mCallback = calculator
        resetValues()
        setValue("0")
        setFormula("")
    }

    constructor(calculatorInterface: Calculator?, value: String?) {
        mCallback = calculatorInterface
        resetValues()
        mDisplayedValue = value
        setFormula("")
    }

    private fun resetValueIfNeeded() {
        if (mResetValue) mDisplayedValue = "0"
        mResetValue = false
    }

    private fun resetValues() {
        mBaseValue = 0.0
        mSecondValue = 0.0
        mResetValue = false
        mLastKey = ""
        mLastOperation = ""
        mDisplayedValue = ""
        mDisplayedFormula = ""
        mIsFirstOperation = true
    }

    fun setValue(value: String?) {
        mCallback?.setValue(value)
        mDisplayedValue = value
    }

    private fun setFormula(value: String?) {
        mCallback?.setFormula(value)
        mDisplayedFormula = value
    }

    private fun updateFormula() {
        val first = Formatter.doubleToString(mBaseValue)
        val second = Formatter.doubleToString(mSecondValue)
        val sign = getSign(mLastOperation)
        if (sign == "√") {
            setFormula(sign + first)
        } else if (!sign?.isEmpty()!!) {
            setFormula(first + sign + second)
        }
    }

    fun setLastKey(mLastKey: String?) {
        this.mLastKey = mLastKey
    }

    fun addDigit(number: Int) {
        val currentValue = getDisplayedNumber()
        val newValue = formatString(currentValue + number)
        setValue(newValue)
    }

    private fun formatString(str: String): String? {
        // if the number contains a decimal, do not try removing the leading zero anymore, nor add group separator
        // it would prevent writing values like 1.02
        if (str.contains(".")) return str
        val doubleValue = Formatter.stringToDouble(str)
        return doubleValue?.let { Formatter.doubleToString(it) }
    }

    private fun updateResult(value: Double) {
        setValue(Formatter.doubleToString(value))
        mBaseValue = value
    }

    fun getDisplayedNumber(): String? {
        return mDisplayedValue
    }

    fun getDisplayedNumberAsDouble(): Double ?{
        return getDisplayedNumber()?.let { Formatter.stringToDouble(it) }
    }

    fun getDisplayedFormula(): String? {
        return mDisplayedFormula
    }

    fun handleResult() {
       getDisplayedNumberAsDouble()?.let {
            mSecondValue = it
        }
        calculateResult()
        getDisplayedNumberAsDouble()?.let {
            mBaseValue = it
        }
    }

    fun calculateResult() {
        if (!mIsFirstOperation) updateFormula()
        when (mLastOperation) {
            Constants.PLUS -> updateResult(mBaseValue + mSecondValue)
            Constants.MINUS -> updateResult(mBaseValue - mSecondValue)
            Constants.MULTIPLY -> updateResult(mBaseValue * mSecondValue)
            Constants.DIVIDE -> divideNumbers()
            Constants.MODULO -> moduloNumbers()
            Constants.POWER -> powerNumbers()
            Constants.ROOT -> updateResult(Math.sqrt(mBaseValue))
            else -> {
            }
        }
        mIsFirstOperation = false
    }

    private fun divideNumbers() {
        var resultValue = 0.0
        if (mSecondValue != 0.0) resultValue = mBaseValue / mSecondValue
        updateResult(resultValue)
    }

    private fun moduloNumbers() {
        var resultValue = 0.0
        if (mSecondValue != 0.0) resultValue = mBaseValue % mSecondValue
        updateResult(resultValue)
    }

    private fun powerNumbers() {
        var resultValue = Math.pow(mBaseValue, mSecondValue)
        if (java.lang.Double.isInfinite(resultValue) || java.lang.Double.isNaN(resultValue)) resultValue = 0.0
        updateResult(resultValue)
    }

    fun handleOperation(operation: String?) {
        if (mLastKey == Constants.DIGIT) handleResult()
        mResetValue = true
        mLastKey = operation
        mLastOperation = operation
        if (operation == Constants.ROOT) calculateResult()
    }

    fun handleClear() {
        val oldValue = getDisplayedNumber()
        var newValue: String? = "0"
        val len = oldValue?.length
        var minLen = 1
        if (oldValue?.contains("-")!!) minLen++
        if (len != null) {
            if (len > minLen) newValue = oldValue.substring(0, len - 1)
        }
        newValue = newValue?.replace("\\.$".toRegex(), "")
        newValue = newValue?.let { formatString(it) }
        setValue(newValue)
        if (newValue != null) {
            Formatter.stringToDouble(newValue)?.let {
                mBaseValue = it
            }
        }
    }

    fun handleReset() {
        resetValues()
        setValue("0")
        setFormula("")
    }

    fun handleEquals() {
        if (mLastKey == Constants.EQUALS) calculateResult()
        if (mLastKey != Constants.DIGIT) return
        getDisplayedNumberAsDouble()?.let {
            mSecondValue = it
        }
        calculateResult()
        mLastKey = Constants.EQUALS
    }

    fun decimalClicked() {
        var value = getDisplayedNumber()
        if (value != null) {
            if (!value.contains(".")) value += "."
        }
        setValue(value)
    }

    fun zeroClicked() {
        val value = getDisplayedNumber()
        if (value != "0") addDigit(0)
    }

    private fun getSign(lastOperation: String?): String? {
        when (lastOperation) {
            Constants.PLUS -> return "+"
            Constants.MINUS -> return "-"
            Constants.MULTIPLY -> return "*"
            Constants.DIVIDE -> return "/"
            Constants.MODULO -> return "%"
            Constants.POWER -> return "^"
            Constants.ROOT -> return "√"
        }
        return ""
    }

    fun numpadClicked(id: Int) {
        if (mLastKey == Constants.EQUALS) mLastOperation = Constants.EQUALS
        mLastKey = Constants.DIGIT
        resetValueIfNeeded()
        when (id) {
            R.id.btn_decimal -> decimalClicked()
            R.id.btn_0 -> zeroClicked()
            R.id.btn_1 -> addDigit(1)
            R.id.btn_2 -> addDigit(2)
            R.id.btn_3 -> addDigit(3)
            R.id.btn_4 -> addDigit(4)
            R.id.btn_5 -> addDigit(5)
            R.id.btn_6 -> addDigit(6)
            R.id.btn_7 -> addDigit(7)
            R.id.btn_8 -> addDigit(8)
            R.id.btn_9 -> addDigit(9)
            else -> {
            }
        }
    }
}