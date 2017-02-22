package com.yandex.ajwar.view;

import com.yandex.ajwar.MainApp;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by 53189 on 17.02.2017.
 */
public class ProgressIndicatorController implements Initializable {
    private MainApp mainApp;
    private Stage progressIndicatorStage;
    public static volatile double progress=0.0d;
    @FXML
    private AnchorPane anchorPaneProgInd;
    @FXML
    private ProgressIndicator progressIndicator;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initProgInd();
    }
    private void initProgInd() {
        progressIndicator.setStyle(" -fx-progress-color: #1412cb;");
    }
    public MainApp getMainApp() {
        return mainApp;
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    public Stage getProgressIndicatorStage() {
        return progressIndicatorStage;
    }

    public void setProgressIndicatorStage(Stage progressIndicatorStage) {
        this.progressIndicatorStage = progressIndicatorStage;
    }

    public ProgressIndicator getProgressIndicator() {
        return progressIndicator;
    }

    public void setProgressIndicator(ProgressIndicator progressIndicator) {
        this.progressIndicator = progressIndicator;
    }
}
