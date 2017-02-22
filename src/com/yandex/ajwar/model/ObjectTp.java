package com.yandex.ajwar.model;


import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Created by 53189 on 26.01.2017.
 */
public class ObjectTp {
    private final SimpleStringProperty designation;
    private final SimpleStringProperty name;
    private final SimpleIntegerProperty artId;

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

    public long getArtId() {
        return artId.get();
    }

    public SimpleIntegerProperty artIdProperty() {
        return artId;
    }

    public void setArtId(int artId) {
        this.artId.set(artId);
    }

    public ObjectTp(String designationStr, String nameStr, Integer artIdLong) {
        this.designation = new SimpleStringProperty(designationStr);
        this.name = new SimpleStringProperty(nameStr);
        this.artId=new SimpleIntegerProperty(artIdLong);
    }
}
