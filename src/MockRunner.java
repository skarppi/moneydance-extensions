import com.infinitekind.moneydance.model.Account;
import com.infinitekind.moneydance.model.AccountBook;
import com.moneydance.apps.md.controller.AccountBookWrapper;
import com.moneydance.apps.md.controller.FeatureModule;
import com.moneydance.apps.md.controller.FeatureModuleContext;
import com.moneydance.apps.md.extensionapi.AccountEditor;
import com.moneydance.apps.md.view.HomePageView;
import com.moneydance.modules.features.payslip.AccountListWindow;
import com.moneydance.modules.features.payslip.Main;

import java.awt.*;
import java.io.File;
import java.lang.reflect.Field;

public class MockRunner implements FeatureModuleContext {

    private AccountBook book;

    public MockRunner(File file) throws Exception {
        AccountBookWrapper wrapper = AccountBookWrapper.wrapperForFolder(file);
        wrapper.loadDataModel(null);
        this.book = wrapper.getBook();
    }

    public MockRunner(AccountBook book) throws Exception {
        this.book = book;
    }

    @Override
    public Account getRootAccount() {
        return book.getRootAccount();
    }

    @Override
    public AccountBook getCurrentAccountBook() {
        return book;
    }

    @Override
    public String getVersion() {
        return null;
    }

    @Override
    public int getBuild() {
        return 0;
    }

    @Override
    public void showURL(String s) {
    }

    @Override
    public void registerFeature(FeatureModule featureModule, String s, Image image, String s1) {
    }

    @Override
    public void registerHomePageView(FeatureModule featureModule, HomePageView homePageView) {
    }

    @Override
    public void registerAccountEditor(FeatureModule featureModule, int i, AccountEditor accountEditor) {
    }

    public static void main(String[] args) throws Exception {
        Main main = new Main();

        FeatureModuleContext context = new MockRunner(new File("Test.moneydance"));

        try {
            Field contextField = FeatureModule.class.getDeclaredField("context");
            contextField.setAccessible(true);
            contextField.set(main, context);
        } catch (Exception e) {
            e.printStackTrace();
        }

        AccountListWindow accountListWindow = new AccountListWindow(main);
        accountListWindow.setVisible(true);
    }
}
