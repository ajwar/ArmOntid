package com.yandex.ajwar.model;


import javafx.beans.property.SimpleStringProperty;

/**
 * Created by 53189 on 26.01.2017.
 */
public class ObjectTp {
    private final SimpleStringProperty designation;
    private final SimpleStringProperty name;

    public String getDesignation() {
        return designation.get();
    }

    public SimpleStringProperty designationProperty() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation.set(designation);
    }

    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public ObjectTp(String designationStr, String nameStr) {
        this.designation = new SimpleStringProperty(designationStr);
        this.name = new SimpleStringProperty(nameStr);
    }
}
