import com.moneydance.modules.features.formula.FormulaWindow
import java.io.File

fun main() {

    val book = File("../Mock.moneydance")
    val api = MockRunner(book).api

    FormulaWindow(api).isVisible = true
}