package com.yandex.ajwar.view;

import com.yandex.ajwar.MainApp;
import com.yandex.ajwar.util.AlertUtilNew;
import com.yandex.ajwar.util.S4AppUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import org.apache.log4j.Logger;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class MainWindowController implements Initializable {
    private MainApp mainApp;
    private static final Logger log=Logger.getLogger(MainWindowController.class);
    private S4AppUtil S4App = S4AppUtil.getInstance();
    private Preferences preferencesScanKdAndTd = MainApp.getPreferencesScanKdAndTd();
    //private static final String LOGIN = preferencesScanKdAndTd.get();
    //private static final String PASSWORD = "ra0843766";
    private static volatile boolean FL=false;
    /*@FXML
    private ScrollPane scrollerMainWindow;*/
    /**
     * Выход
     */
    @FXML
    private void handleExit() {
        log.info("Юзер " + System.getProperty("user.name") + " завершил работу ScanKdAndTd.");
        System.exit(0);
    }

    /**
     * Информация о разработчике
     */
    @FXML
    private void handleAboutMe() {
        AlertUtilNew.message("Программа для сканирования документации.", "Автор:Шагов Айвар\r\nОтдел АСУ\r\nтелефон 22-35", "Информация о разработчике.", Alert.AlertType.INFORMATION);
    }

    /**
     * Загрузка формы Options,с проверкой логина или пароля
     */
    @FXML
    private void handleMenuItemOptions() {
        Platform.runLater(() -> getMainApp().showOptionsWindow());
        AlertUtilNew.messageLogAndPass(preferencesScanKdAndTd.get("login","sysdba"), preferencesScanKdAndTd.get("password","masterkey"));
        if (FL) {
            getMainApp().getOptionsController().getOptionsDialogStage().showAndWait();
        } else{
            AlertUtilNew.message("Произошла ошибка.", "Введен неправильгый логин или пароль.Проверьте раскладку клавиатуры и Caps Lock.",
                    "Ошибка при заполнении данных.", Alert.AlertType.ERROR);
        }
        FL=false;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (S4App.login(S4App) == 0) {
            if (S4App.isLoggedIn(S4App)==0)
                Platform.runLater(()->{
                log.info("Юзер " + System.getProperty("user.name") + " завершил работу ScanKdAndTd.");
                AlertUtilNew.message("Внимание!", "Перед запуском программы зайдите в Search под своим логином и запустите повторно.", "Не выполнен вход в систему Search.", Alert.AlertType.WARNING);
                System.exit(0);
                });
        }
    }

    public MainApp getMainApp() {
        return mainApp;
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    public static boolean isFL() {
        return FL;
    }

    public static void setFL(boolean FL) {
        MainWindowController.FL = FL;
    }
}
