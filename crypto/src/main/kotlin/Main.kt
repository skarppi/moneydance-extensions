import com.moneydance.modules.features.crypto.CryptoWindow
import java.io.File

fun main() {

    val book = File("../Mock.moneydance")
    val api = MockRunner(book).api

    CryptoWindow(api).isVisible = true
}