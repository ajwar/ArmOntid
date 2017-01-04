package com.yandex.ajwar.util;
/**
 * Created by 53189 on 09.12.2016.
 */

import com.sun.javafx.stage.StageHelper;
import com.yandex.ajwar.view.MainWindowController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.*;
import javafx.util.Pair;

import java.io.PrintWriter;
import java.io.StringWriter;

public class AlertUtilNew extends Application {
    private static double xOffset = 0;
    private static double yOffset = 0;
    private static Stage stage;
    // private static boolean FL= false;

    /**
     * Вывод Сообщения с логином и паролем
     */
    public static boolean messageLogAndPass(String trueLogin, String truePassword) {
        StackPane root = new StackPane();
        //рисуем произвольную форму окна
        SVGPath p = new SVGPath();
        p.setContent("M0,0 l180,-150 a60,60 0 0,1 60,60 v180 a60,60 0 0,1 -60,60  h-180 a60,60 0 0,1 -60,-60 l0,-180  z");
        p.getStyleClass().add("dialogLogin");
        p.setEffect(new DropShadow());
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        //Добавляем лабели и текстфилды
        TextField username = new TextField();
        username.setPromptText("Логин");
        PasswordField password = new PasswordField();
        password.setPromptText("Пароль");
        Label labelLogin = new Label("Логин:");
        Label labelPass = new Label("Пароль:");
        labelLogin.getStyleClass().add("label-custom");
        labelPass.getStyleClass().add("label-custom");
        grid.add(labelLogin, 0, 0);
        grid.add(username, 1, 0);
        grid.add(labelPass, 0, 1);
        grid.add(password, 1, 1);
        grid.setAlignment(Pos.BOTTOM_CENTER);
        grid.setTranslateY(-80);
        grid.setTranslateX(60);

        //добавляем изображение
        Image image = new Image(AlertUtilNew.class.getResourceAsStream("/images/user_login.png"));
        ImageView imageView = new ImageView(image);
        imageView.setScaleX(1);
        imageView.setScaleY(1);
        imageView.setTranslateY(-70);
        root.getChildren().add(p);
        root.getChildren().add(imageView);
        root.getChildren().add(grid);
        root.setStyle("-fx-background-color: transparent");

        //Добавляю кнопки ок и канцел
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        ButtonType loginButtonType = new ButtonType("Подтвердить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);
        Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
        Node cancelButton = dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        loginButton.setDisable(true);
        // Валидация поля логина.
        username.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(newValue.trim().isEmpty());
        });
        Platform.runLater(() -> username.requestFocus());

        HBox hBox = new HBox(loginButton, cancelButton);
        //HBox hBox=new HBox(dialog.getDialogPane());
        hBox.setSpacing(8);
        grid.add(hBox, 1, 3);


        //делаем прозрачную сцену и окно
        Scene scene = null;
        scene = new Scene(root, 400, 400, Color.TRANSPARENT);
        Stage stage = new Stage();
        stage.setScene(scene);
        scene.getStylesheets().add(AlertUtilNew.class.getResource("/css/styleDialogLoginNew.css").toExternalForm());
        //вешаю слушателей на передвижение окна
        p.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        p.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });
        grid.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        grid.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });
        //вешаю слушателей на кнопки
        cancelButton.setOnMouseClicked(event -> {
            stage.close();
        });
        loginButton.setOnMouseClicked(event -> {
            if (username.getText().equals(trueLogin) && password.getText().equals(truePassword)) {
                MainWindowController.setFL(true);
                stage.close();
            } else {
                AlertUtilNew.message("Произошла ошибка.", "Введен неправильгый логин или пароль.Проверьте раскладку клавиатуры и Caps Lock.",
                        "Ошибка при заполнении данных.", Alert.AlertType.ERROR);
            }
        });
        scene.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ENTER)) {
                MainWindowController.setFL(true);
                stage.close();
            } else {
                AlertUtilNew.message("Произошла ошибка.", "Введен неправильгый логин или пароль.Проверьте раскладку клавиатуры и Caps Lock.",
                        "Ошибка при заполнении данных.", Alert.AlertType.ERROR);
            }
        });
        stage.initStyle(StageStyle.TRANSPARENT);
        if (!StageHelper.getStages().isEmpty()) stage.initOwner(StageHelper.getStages().get(0));
        stage.showAndWait();
        return MainWindowController.isFL();
    }

    /**
     * Вывод простого сообщения
     */
    public static void message(String title, String text, String headerText, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(text);
        //Внедряю диалог в вызывающее окно(вызывается на том мониторе,где и главное).
        alert.initOwner(StageHelper.getStages().get(0));
        alert.showAndWait();
    }

    /**
     * Вывод сообщения об ошибке
     */
    public static void showErrorMessage(String message, Throwable exception, Control control) {

        final Stage dialog = new Stage();
        dialog.initOwner(control.getScene().getWindow());
        dialog.initStyle(StageStyle.UNDECORATED);
        final VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        StringWriter errorMessage = new StringWriter();
        exception.printStackTrace(new PrintWriter(errorMessage));
        final Label detailsLabel = new Label(errorMessage.toString());
        TitledPane details = new TitledPane();
        details.setText("Details:");
        Label briefMessageLabel = new Label(message);
        final HBox detailsLabelHolder = new HBox();

        Button closeButton = new Button("Да");
        closeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                dialog.hide();
            }
        });
        HBox closeButtonHolder = new HBox();
        closeButtonHolder.getChildren().add(closeButton);
        closeButtonHolder.setAlignment(Pos.CENTER);
        closeButtonHolder.setPadding(new Insets(5));
        root.getChildren().addAll(briefMessageLabel, details, detailsLabelHolder, closeButtonHolder);
        details.setExpanded(false);
        details.setAnimated(false);

        details.expandedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable,
                                Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    detailsLabelHolder.getChildren().add(detailsLabel);
                } else {
                    detailsLabelHolder.getChildren().remove(detailsLabel);
                }
                dialog.sizeToScene();
            }

        });
        final Scene scene = new Scene(root);
        dialog.setScene(scene);
        dialog.show();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        //this.stage=primaryStage;
        //AlertUtilNew.messageLogAndPass("1","1");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
