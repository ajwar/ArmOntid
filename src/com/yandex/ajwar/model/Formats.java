package com.yandex.ajwar.model;

import javafx.beans.property.SimpleStringProperty;

/**
 * Created by 53189 on 05.12.2016.
 */
public class Formats {
    private final SimpleStringProperty format;

    public String getFormat() {
        return format.get();
    }

    public SimpleStringProperty formatProperty() {
        return format;
    }

    public void setFormat(String format) {
        this.format.set(format);
    }

    public Formats() {
        this.format=null;
    }

    public Formats(String format) {
        this.format = new SimpleStringProperty(format);
    }
}
