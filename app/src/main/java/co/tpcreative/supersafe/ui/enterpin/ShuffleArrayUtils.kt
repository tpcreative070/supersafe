package co.tpcreative.supersafe.ui.enterpin
import java.util.*
object ShuffleArrayUtils {
    /**
     * Shuffle an array
     *
     * @param array
     */
    fun shuffle(array: IntArray?): IntArray? {
        val length = array?.size
        val random = Random()
        random.nextInt()
        for (i in 0 until length!!) {
            val change = i + random.nextInt(length - i)
            swap(array, i, change)
        }
        return array
    }

    private fun swap(mArray: IntArray, index: Int, change: Int) {
        val temp = mArray.get(index)
        mArray.set(index, mArray.get(change))
        mArray.set(change,temp)
    }
}