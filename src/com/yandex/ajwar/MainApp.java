package com.yandex.ajwar;


import com.yandex.ajwar.model.StringData;
import com.yandex.ajwar.util.S4AppUtil;
import com.yandex.ajwar.view.*;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.util.prefs.Preferences;

public class MainApp extends Application {
    private S4AppUtil S4App = S4AppUtil.getInstance();
    private static final Logger log = Logger.getLogger(MainApp.class);
    private static Preferences preferencesScanKdAndTd = Preferences.userRoot().node("ScanKdAndTd");
    private static Preferences preferencesTableViewDocTypes = preferencesScanKdAndTd.node("tableViewDocTypes");
    private static Preferences preferencesTableViewInputMask = preferencesScanKdAndTd.node("tableViewInputMask");
    private static Preferences preferencesTableViewFormats = preferencesScanKdAndTd.node("tableViewFormats");
    private static final String SP=System.getProperty("file.separator","\\");
    private static final String USER_HOME=System.getProperty("user.home","d:\\");
    private Stage primaryStage;

    private BorderPane mainBorderPane;
    private PdfViewerController pdfViewerController;
    private OptionsController optionsController;
    private MainWindowController mainWindowController;
    private ImbaseTreeController imbaseTreeController;
    private ProgressIndicatorController progressIndicatorController;
    private ObservableList<StringData> stringNameFileData = FXCollections.observableArrayList();

    @Override
    public void start(Stage primaryStage) throws Exception{
        this.primaryStage=primaryStage;
        this.primaryStage.setTitle("Сканирование КД и ТД");
        log.info("Юзер "+System.getProperty("user.name")+" запустил программу ScanKdAndTd.");
        initMainWindow();
        showRightWindow();
        primaryStage.show();
        //getPrimaryStage().toBack();
    }


    public static void main(String[] args) throws URISyntaxException, IOException {
        double version=Double.parseDouble(System.getProperty("java.specification.version"));
        if (version<1.8){
            log.error("Версия Java, установленная на этой машине(" + version + "), меньше версии необходимой для запуска программы (1.8).");
            System.exit(0);
        }
        //узнаю где находится запускаемый Jar файл
        String currentPath=MainApp.class
                .getProtectionDomain()
                .getCodeSource().getLocation()
                .getPath()
                .replace('/', File.separator.charAt(0));
        if (currentPath.indexOf(":")<3 && currentPath.indexOf(":")>0) currentPath=currentPath.substring(1);
        //если память кучи меньше 1gb и нет входных аргументов,то перезапускаю программу с нач. 256 мб и конечной 1гб памятью
        if(args.length == 0 && Runtime.getRuntime().maxMemory() / 1024 / 1024 < 980) {
            Runtime.getRuntime().exec("java -jar -Xms256m -Xmx1024m -Dcom.jacob.autogc=TRUE " + currentPath+" restart");
            System.exit(0);
        } else {
            launch(args);
        }
    }

    /**Загрузка главной формы.*/
    public void initMainWindow(){
        try {
            FXMLLoader loader=new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/MainWindow.fxml"));
            getPrimaryStage().getIcons().add(new Image(MainApp.class.getResourceAsStream("/images/IcoMainWindowNew.png")));
            setMainBorderPane((BorderPane)loader.load());
            //getMainBorderPane().setBackground(Background.EMPTY);
            Scene scene=new Scene(getMainBorderPane());
            getPrimaryStage().centerOnScreen();
            getPrimaryStage().setScene(scene);
            //минимальные размеры основной формы
            getPrimaryStage().setMinHeight(830);
            getPrimaryStage().setMinWidth(1100);
            getPrimaryStage().setOnCloseRequest(event -> {
                log.info("Юзер " + System.getProperty("user.name") + " завершил работу программы ScanKdAndTd.");
                System.exit(0);
            });
            //getPrimaryStage().initStyle(StageStyle.UNDECORATED);
            setMainWindowController(loader.getController());
            getMainWindowController().setMainApp(this);

        } catch (IOException e) {
            log.error("Ошибка при загрузке главной формы(MainWindow.fxml).",e);
            e.printStackTrace();
        }
    }

    /**Загрузка правой формы(PDF просмотрщик).*/
    public void showRightWindow(){
        try {
            FXMLLoader loader=new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/PdfViewer.fxml"));
            TabPane pdfViewer=(TabPane)loader.load();
            mainBorderPane.setCenter(pdfViewer);
            setPdfViewerController(loader.getController());
            getPdfViewerController().setMainApp(this);
        } catch (IOException e) {
            log.error("Ошибка при загрузке правой формы (PdfViewer.fxml).",e);
            e.printStackTrace();
        }
    }

    /**Загрузка формы настроек.*/
    public void showOptionsWindow(){
        try {
            FXMLLoader loader=new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/Options.fxml"));
            AnchorPane paneOptions=(AnchorPane)loader.load();
            Stage stageOptions=new Stage();
            stageOptions.setTitle("Настройки");
            stageOptions.initModality(Modality.WINDOW_MODAL);
            stageOptions.initOwner(primaryStage);
            Scene scene=new Scene(paneOptions);
            stageOptions.setScene(scene);
            setOptionsController(loader.getController());
            getOptionsController().setMainApp(this);
            getOptionsController().setOptionsDialogStage(stageOptions);
            stageOptions.sizeToScene();
            stageOptions.setResizable(false);
            //stageOptions.hide();
        } catch (IOException e) {
            log.error("Ошибка при загрузке формы настроек (Options.fxml).",e);
            e.printStackTrace();
        }
    }

    public void showImbaseTree() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/ImbaseTree.fxml"));
            AnchorPane paneImbase = (AnchorPane) loader.load();
            Stage stageImbase = new Stage();
            stageImbase.setTitle("Маршрут изготовления.");
            stageImbase.initModality(Modality.WINDOW_MODAL);
            stageImbase.initOwner(primaryStage);
            Scene scene = new Scene(paneImbase);
            stageImbase.setScene(scene);
            setImbaseTreeController(loader.getController());
            getImbaseTreeController().setMainApp(this);
            getImbaseTreeController().setImbaseDialogStage(stageImbase);
            //stageImbase.sizeToScene();
            stageImbase.initStyle(StageStyle.UNDECORATED);
            //stageImbase.setResizable(false);
        } catch (IOException e) {
            log.error("Ошибка при загрузке формы настроек (ImbaseTree.fxml).", e);
            e.printStackTrace();
        }
    }

    public void showProgressIndicator(){
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/ProgressIndicator.fxml"));
            AnchorPane pane = (AnchorPane) loader.load();
            pane.setBackground(Background.EMPTY);
            Stage stageProgInd = new Stage();
            stageProgInd.initModality(Modality.WINDOW_MODAL);
            stageProgInd.initOwner(primaryStage);
            Scene scene = new Scene(pane, Color.TRANSPARENT);
            //scene.setFill(Color.TRANSPARENT);
            stageProgInd.setScene(scene);
            progressIndicatorController=loader.getController();
            progressIndicatorController.setMainApp(this);
            progressIndicatorController.setProgressIndicatorStage(stageProgInd);
            //scene.getStylesheets().add("");
            //stageImbase.sizeToScene();
            stageProgInd.initStyle(StageStyle.TRANSPARENT);
        } catch (IOException e) {
            log.error("Ошибка при загрузке формы настроек (ProgressIndicator.fxml).", e);
            e.printStackTrace();
        }
    }

    public MainApp(){}
    public MainApp(String str){
        stringNameFileData.add(new StringData(str));

    }

    public ObservableList<StringData> getStringNameFileData() {
        return stringNameFileData;
    }

    public void setStringNameFileData(ObservableList<StringData> stringNameFileData) {
        this.stringNameFileData = stringNameFileData;
    }
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public PdfViewerController getPdfViewerController() {
        return pdfViewerController;
    }

    public void setPdfViewerController(PdfViewerController pdfViewerController) {
        this.pdfViewerController = pdfViewerController;
    }

    public OptionsController getOptionsController() {
        return optionsController;
    }

    public void setOptionsController(OptionsController optionsController) {
        this.optionsController = optionsController;
    }

    public BorderPane getMainBorderPane() {
        return mainBorderPane;
    }

    public void setMainBorderPane(BorderPane mainBorderPane) {
        this.mainBorderPane = mainBorderPane;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public static Preferences getPreferencesScanKdAndTd() {
        return preferencesScanKdAndTd;
    }

    public static void setPreferencesScanKdAndTd(Preferences preferencesScanKdAndTd) {
        MainApp.preferencesScanKdAndTd = preferencesScanKdAndTd;
    }

    public static Preferences getPreferencesTableViewDocTypes() {
        return preferencesTableViewDocTypes;
    }

    public static void setPreferencesTableViewDocTypes(Preferences preferencesTableViewDocTypes) {
        MainApp.preferencesTableViewDocTypes = preferencesTableViewDocTypes;
    }

    public static Preferences getPreferencesTableViewInputMask() {
        return preferencesTableViewInputMask;
    }

    public static void setPreferencesTableViewInputMask(Preferences preferencesTableViewInputMask) {
        MainApp.preferencesTableViewInputMask = preferencesTableViewInputMask;
    }

    public static Preferences getPreferencesTableViewFormats() {
        return preferencesTableViewFormats;
    }

    public static void setPreferencesTableViewFormats(Preferences preferencesTableViewFormats) {
        MainApp.preferencesTableViewFormats = preferencesTableViewFormats;
    }

    public MainWindowController getMainWindowController() {
        return mainWindowController;
    }

    public void setMainWindowController(MainWindowController mainWindowController) {
        this.mainWindowController = mainWindowController;
    }

    public static String getSP() {
        return SP;
    }

    public static String getUserHome() {
        return USER_HOME;
    }

    public ImbaseTreeController getImbaseTreeController() {
        return imbaseTreeController;
    }

    public void setImbaseTreeController(ImbaseTreeController imbaseTreeController) {
        this.imbaseTreeController = imbaseTreeController;
    }

    public ProgressIndicatorController getProgressIndicatorController() {
        return progressIndicatorController;
    }

    public void setProgressIndicatorController(ProgressIndicatorController progressIndicatorController) {
        this.progressIndicatorController = progressIndicatorController;
    }
}
