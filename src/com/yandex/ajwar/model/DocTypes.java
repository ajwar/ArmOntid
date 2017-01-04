package com.yandex.ajwar.model;

import javafx.beans.property.SimpleStringProperty;

/**
 * Created by 53189 on 30.11.2016.
 */
public class DocTypes {
    private final SimpleStringProperty docName;
    private final SimpleStringProperty dtCode;

    public String getDocName() {
        return docName.get();
    }

    public SimpleStringProperty docNameProperty() {
        return docName;
    }

    public void setDocName(String docName) {
        this.docName.set(docName);
    }

    public String getDtCode() {
        return dtCode.get();
    }

    public SimpleStringProperty dtCodeProperty() {
        return dtCode;
    }

    public void setDtCode(String dtCode) {
        this.dtCode.set(dtCode);
    }

    public DocTypes(String docName, String dtCode) {
        this.docName = new SimpleStringProperty(docName);
        this.dtCode = new SimpleStringProperty(dtCode);
    }
}
