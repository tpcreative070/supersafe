package co.tpcreative.supersafe.common.util
import androidx.annotation.IntRange
import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    fun buildDate(@IntRange(from = 1, to = 31) dayOfMonth: Int): Date? {
        return buildDate(dayOfMonth, DateUtils.getCurrentMonth())
    }

    private fun buildDate(
            @IntRange(from = 1, to = 31) dayOfMonth: Int,
            @IntRange(from = 0, to = 11) month: Int,
            @IntRange(from = 0) year: Int = DateUtils.getCurrentYear()): Date? {
        val calendar: Calendar = GregorianCalendar()
        calendar[Calendar.DAY_OF_MONTH] = dayOfMonth
        calendar[Calendar.MONTH] = month
        calendar[Calendar.YEAR] = year
        calendar[Calendar.HOUR] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
        return calendar.time
    }

    fun formatDate(date: Date): String? {
        val df = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return df.format(date)
    }

    @IntRange(from = 0, to = 11)
    private fun getCurrentMonth(): Int {
        val now = Date(System.currentTimeMillis())
        val calendar: Calendar = GregorianCalendar()
        calendar.time = now
        return calendar[Calendar.MONTH]
    }

    @IntRange(from = 0)
    private fun getCurrentYear(): Int {
        val now = Date(System.currentTimeMillis())
        val calendar: Calendar = GregorianCalendar()
        calendar.time = now
        return calendar[Calendar.YEAR]
    }
}