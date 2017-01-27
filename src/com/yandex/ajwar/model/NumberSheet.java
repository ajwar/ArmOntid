package com.yandex.ajwar.model;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TextField;

/**
 * Created by 53189 on 26.01.2017.
 */
public class NumberSheet {
    private final SimpleObjectProperty<TextField> textFieldDesignNumberSheet;

    public NumberSheet(TextField textField) {
        this.textFieldDesignNumberSheet = new SimpleObjectProperty<>(textField);
    }

    public TextField getTextFieldDesignNumberSheet() {
        return textFieldDesignNumberSheet.get();
    }

    public SimpleObjectProperty<TextField> textFieldDesignNumberSheetProperty() {
        return textFieldDesignNumberSheet;
    }

    public void setTextFieldDesignNumberSheet(TextField textFieldDesignNumberSheet) {
        this.textFieldDesignNumberSheet.set(textFieldDesignNumberSheet);
    }
}
