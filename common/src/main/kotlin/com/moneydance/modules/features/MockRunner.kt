import com.moneydance.apps.md.controller.AccountBookWrapper
import com.moneydance.apps.md.controller.Main
import com.moneydance.apps.md.view.gui.MoneydanceGUI
import com.moneydance.modules.features.MDApi
import java.io.File
import java.lang.Exception

class MockRunner(book: File) {
    val api: MDApi

    init {
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
        api = MDApi(main, MoneydanceGUI(main))
    }
}