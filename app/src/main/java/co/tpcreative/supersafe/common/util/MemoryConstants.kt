package co.tpcreative.supersafe.common.util
import androidx.annotation.IntDef

/**
 * <pre>
 * author: Blankj
 * blog  : http://blankj.com
 * time  : 2017/03/13
 * desc  : constants of memory
</pre> *
 */
object MemoryConstants {
    const val BYTE = 1
    const val KB = 1024
    const val MB = 1048576
    const val GB = 1073741824

    @IntDef(BYTE, KB, MB, GB)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Unit
}