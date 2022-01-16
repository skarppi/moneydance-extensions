import com.moneydance.apps.md.controller.AccountBookWrapper
import com.moneydance.apps.md.controller.Main
import com.moneydance.apps.md.view.gui.MoneydanceGUI
import com.moneydance.modules.features.formula.FormulaWindow
import com.moneydance.modules.features.formula.MDApi
import java.io.File
import java.lang.Exception

fun main(args: Array<String>) {
    val book = File("./Mock.moneydance")
    require(book.exists(), { "Account book doesn not exist" })

    val main = Main()
    main.initializeApp()
    val wrapper = AccountBookWrapper.wrapperForFolder(book)
    wrapper.loadDataModel(null)
    try {
        val contextField = Main::class.java.getDeclaredField("currentBook")
        contextField.isAccessible = true
        contextField[main] = wrapper
    } catch (e: Exception) {
        e.printStackTrace()
    }
    val api = MDApi(main, MoneydanceGUI(main))
    val formulaWindow = FormulaWindow(api)
    formulaWindow.isVisible = true
}