import com.moneydance.apps.md.controller.AccountBookWrapper;
import com.moneydance.apps.md.controller.Main;
import com.moneydance.apps.md.view.gui.MoneydanceGUI;
import com.moneydance.modules.features.formula.FormulaWindow;
import com.moneydance.modules.features.formula.MDApi;

import java.io.File;
import java.lang.reflect.Field;

public class MockRunner {

    public static void main(String[] args) throws Exception {
        Main main = new Main();
        main.initializeApp();

        AccountBookWrapper wrapper = AccountBookWrapper.wrapperForFolder(new File("./Mock.moneydance"));
        wrapper.loadDataModel(null);

        try {
            Field contextField = Main.class.getDeclaredField("currentBook");
            contextField.setAccessible(true);
            contextField.set(main, wrapper);
        } catch (Exception e) {
            e.printStackTrace();
        }


        MDApi api = new MDApi(main, () -> new MoneydanceGUI(main));
        FormulaWindow formulaWindow = new FormulaWindow(api);
        formulaWindow.setVisible(true);
    }
}
