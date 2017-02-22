package com.yandex.ajwar.model;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

/**
 * Created by 53189 on 26.01.2017.
 */
public class MapSketch {
    private final SimpleObjectProperty<TextField> textFieldDesignMapSketch;
    private final SimpleObjectProperty<TextField> textFieldOtdRegNumMapSketch;
    private final SimpleObjectProperty<TextField> textFieldNumChangeMapSketchTd;
    private final SimpleObjectProperty<TextField> textFieldDesignIIMapSketchTd;
    private final SimpleObjectProperty<Button> buttonScan;

    public TextField getTextFieldDesignMapSketch() {
        return textFieldDesignMapSketch.get();
    }

    public SimpleObjectProperty<TextField> textFieldDesignMapSketchProperty() {
        return textFieldDesignMapSketch;
    }

    public void setTextFieldDesignMapSketch(TextField textFieldDesignMapSketch) {
        this.textFieldDesignMapSketch.set(textFieldDesignMapSketch);
    }

    public Button getButtonScan() {
        return buttonScan.get();
    }

    public SimpleObjectProperty<Button> buttonScanProperty() {
        return buttonScan;
    }

    public void setButtonScan(Button buttonScan) {
        this.buttonScan.set(buttonScan);
    }

    public MapSketch(TextField textField, TextField textFieldOtdRegNumMapSketch,TextField textFieldChamge,TextField textFieldDesign,Button buttonScan) {
        this.textFieldDesignMapSketch = new SimpleObjectProperty<>(textField);
        this.buttonScan = new SimpleObjectProperty<>(buttonScan);
        this.textFieldOtdRegNumMapSketch = new SimpleObjectProperty<>(textFieldOtdRegNumMapSketch);
        this.textFieldDesignIIMapSketchTd=new SimpleObjectProperty<>(textFieldDesign);
        this.textFieldNumChangeMapSketchTd=new SimpleObjectProperty<>(textFieldChamge);
    }

    public TextField getTextFieldOtdRegNumMapSketch() {
        return textFieldOtdRegNumMapSketch.get();
    }

    public SimpleObjectProperty<TextField> textFieldOtdRegNumMapSketchProperty() {
        return textFieldOtdRegNumMapSketch;
    }

    public void setTextFieldOtdRegNumMapSketch(TextField textFieldOtdRegNumMapSketch) {
        this.textFieldOtdRegNumMapSketch.set(textFieldOtdRegNumMapSketch);
    }

    public TextField getTextFieldNumChangeMapSketchTd() {
        return textFieldNumChangeMapSketchTd.get();
    }

    public SimpleObjectProperty<TextField> textFieldNumChangeMapSketchTdProperty() {
        return textFieldNumChangeMapSketchTd;
    }

    public void setTextFieldNumChangeMapSketchTd(TextField textFieldNumChangeMapSketchTd) {
        this.textFieldNumChangeMapSketchTd.set(textFieldNumChangeMapSketchTd);
    }

    public TextField getTextFieldDesignIIMapSketchTd() {
        return textFieldDesignIIMapSketchTd.get();
    }

    public SimpleObjectProperty<TextField> textFieldDesignIIMapSketchTdProperty() {
        return textFieldDesignIIMapSketchTd;
    }

    public void setTextFieldDesignIIMapSketchTd(TextField textFieldDesignIIMapSketchTd) {
        this.textFieldDesignIIMapSketchTd.set(textFieldDesignIIMapSketchTd);
    }

    public void setDisableAll(boolean flag){
        this.buttonScan.get().setDisable(flag);
        this.textFieldDesignMapSketch.get().setDisable(flag);
        this.textFieldDesignIIMapSketchTd.get().setDisable(flag);
        this.textFieldNumChangeMapSketchTd.get().setDisable(flag);
        this.textFieldOtdRegNumMapSketch.get().setDisable(flag);
    }
    public void clearAllMapSketch(){
        //this.textFieldDesignMapSketch.get().clear();
        if (this.textFieldDesignMapSketch.get().getTextFormatter().getValue().equals("В25.")) {
            this.textFieldDesignMapSketch.get().setText("В25.");
        } else this.textFieldDesignMapSketch.get().clear();
        this.textFieldDesignIIMapSketchTd.get().clear();
        this.textFieldNumChangeMapSketchTd.get().clear();
        this.textFieldOtdRegNumMapSketch.get().clear();
    }
}


