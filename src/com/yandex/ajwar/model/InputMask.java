package com.yandex.ajwar.model;

import javafx.beans.property.SimpleStringProperty;

/**
 * Created by 53189 on 02.12.2016.
 */
public class InputMask {
    private final SimpleStringProperty mask;
    public String getMask() {
        return mask.get();
    }

    public SimpleStringProperty maskProperty() {
        return mask;
    }

    public void setMask(String mask) {
        this.mask.set(mask);
    }

    public InputMask() {
        this.mask=null;
    }

    public InputMask(String mask) {
        this.mask =new SimpleStringProperty(mask);

    }
}
