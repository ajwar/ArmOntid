package com.yandex.ajwar;


import com.yandex.ajwar.model.StringData;
import com.yandex.ajwar.util.S4AppUtil;
import com.yandex.ajwar.view.MainWindowController;
import com.yandex.ajwar.view.OptionsController;
import com.yandex.ajwar.view.PdfViewerController;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.prefs.Preferences;

public class MainApp extends Application {
    private S4AppUtil S4App = S4AppUtil.getInstance();
    private static final Logger log = Logger.getLogger(MainApp.class);
    private static Preferences preferencesScanKdAndTd = Preferences.userRoot().node("ScanKdAndTd");
    private static Preferences preferencesTableViewDocTypes = preferencesScanKdAndTd.node("tableViewDocTypes");
    private static Preferences preferencesTableViewInputMask = preferencesScanKdAndTd.node("tableViewInputMask");
    private static Preferences preferencesTableViewFormats = preferencesScanKdAndTd.node("tableViewFormats");
    private Stage primaryStage;

    private BorderPane mainBorderPane;
    private PdfViewerController pdfViewerController;
    private OptionsController optionsController;
    private MainWindowController mainWindowController;
    private ObservableList<StringData> stringNameFileData = FXCollections.observableArrayList();
    //private static final ExecutorService executorServiceLoad=PdfViewerController.getExecutorServiceLoad();

    @Override
    public void start(Stage primaryStage) throws Exception{
        this.primaryStage=primaryStage;
        this.primaryStage.setTitle("Сканирование КД и ТД");
        initMainWindow();
        showRightWindow();
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }

    /**Загрузка главной формы.*/
    public void initMainWindow(){
        try {
            FXMLLoader loader=new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/MainWindow.fxml"));
            getPrimaryStage().getIcons().add(new Image(MainApp.class.getResourceAsStream("/images/IcoMainWindow.png")));
            setMainBorderPane((BorderPane)loader.load());
            Scene scene=new Scene(getMainBorderPane());
            getPrimaryStage().centerOnScreen();
            getPrimaryStage().setScene(scene);
            //минимальные размеры основной формы
            getPrimaryStage().setMinHeight(830);
            getPrimaryStage().setMinWidth(1050);
            //getPrimaryStage().initStyle(StageStyle.UNDECORATED);
            setMainWindowController(loader.getController());
            getMainWindowController().setMainApp(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**Загрузка правой формы(PDF просмотрщик).*/
    public void showRightWindow(){
        try {
            FXMLLoader loader=new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/PdfViewer.fxml"));
            TabPane pdfViewer=(TabPane)loader.load();
            //ScrollPane pdfViewer=(ScrollPane)loader.load();
            mainBorderPane.setCenter(pdfViewer);
            setPdfViewerController(loader.getController());
            getPdfViewerController().setMainApp(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**Загрузка формы настроек.*/
    public void showOptionsWindow(){
        try {
            FXMLLoader loader=new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/Options.fxml"));
            TabPane pageOptions=(TabPane)loader.load();
            Stage stageOptions=new Stage();
            stageOptions.setTitle("Настройки");
            stageOptions.initModality(Modality.WINDOW_MODAL);
            stageOptions.initOwner(primaryStage);
            Scene scene=new Scene(pageOptions);
            stageOptions.setScene(scene);
            //stageOptions.initStyle(StageStyle.UNDECORATED);
            setOptionsController(loader.getController());
            getOptionsController().setMainApp(this);
            getOptionsController().setOptionsDialogStage(stageOptions);
            stageOptions.sizeToScene();
            stageOptions.setResizable(false);
            stageOptions.hide();
            //stageOptions.showAndWait();
        } catch (IOException e) {
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
}
