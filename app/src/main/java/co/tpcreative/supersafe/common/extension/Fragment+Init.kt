package co.tpcreative.supersafe.common.extension
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

fun FragmentManager.instantiate(className: String): Fragment {
    return fragmentFactory.instantiate(ClassLoader.getSystemClassLoader(), className)
}
