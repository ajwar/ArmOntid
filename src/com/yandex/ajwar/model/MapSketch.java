package com.yandex.ajwar.model;

import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

/**
 * Created by 53189 on 26.01.2017.
 */
public class MapSketch {
    private final SimpleObjectProperty<TextField> textFieldDesignMapSketch;
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

    public MapSketch(TextField textField, Button buttonScan) {
        this.textFieldDesignMapSketch = new SimpleObjectProperty<>(textField);
        this.buttonScan = new SimpleObjectProperty<>(buttonScan);
    }


}


