package co.tpcreative.supersafe.model
import java.io.Serializable
class Album(private val name: String?, private val imageResource: Int) : Serializable {
    fun getName(): String? {
        return name
    }
    fun getImageResource(): Int {
        return imageResource
    }
}