package com.yandex.ajwar.util;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.util.StringConverter;

import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by 53189 on 08.12.2016.
 */
public class TextFormatterUtil {
    /**Форматирую текст согласно регулярным выражениям*/
    public static TextFormatter<String> gTextFormatter(String str, TextField textFieldDesignation){
        UnaryOperator<TextFormatter.Change> filter=null;
        StringConverter<String> converter=null;
        String defaultText="";
        if ("***".equals(str)){
            defaultText="";
            converter=new StringConverter<String>() {
                @Override
                public String toString(String object) {
                    if (object == null) return "";
                    return object.toString();
                }
                @Override
                public String fromString(String string) {
                    Pattern regexp = Pattern.compile("^([1-9]{1}|[1-9]{1}[0-9]{1}|100)$"); //Пример строки для 5ВЩ.162.677
                    Matcher m = regexp.matcher(string);
                    if (m.matches()) return string;
                    else return "";
                }
            };
            filter= c -> {
                if (c.getControlNewText().length()==3 && !c.getControlNewText().matches("^([1-9]{1}|[1-9]{1}[0-9]{1}|100)$")) {
                    return null;
                }
                if ((!c.getText().matches("\\d") && c.isAdded()) || c.getControlNewText().length()>3) return null;
                return c;
            };
        }
        if ("**-****".equals(str) || "**-**** Дополнение №*".equals(str)){
            defaultText="";
            converter=new StringConverter<String>() {
                @Override
                public String toString(String object) {
                    if (object == null) return "";
                    return object.toString();
                }
                @Override
                public String fromString(String string) {
                    Pattern regexp = Pattern.compile("^\\d\\d-\\d\\d\\d\\d$"); //Пример строки для 5ВЩ.162.677
                    Matcher m = regexp.matcher(string);
                    if (string.length() == 7 && m.matches()) return string;
                    else return "";
                }
            };
            filter= c -> {
                if (c.getControlNewText().length() == 7 && !c.getControlNewText().matches("^\\d\\d-\\d\\d\\d\\d$")) {
                    textFieldDesignation.setText("");
                    textFieldDesignation.positionCaret(1);
                    return null;
                }
                if ((!c.getText().matches("\\d") && !c.isDeleted()) || c.getControlNewText().length() > 7)
                    return null;
            /*if (c.getText().matches("[a-zA-Zа-яА-ЯёЁ]") || c.getControlNewText().length() > 7 || c.getText().matches("\\W") || c.getText().matches("_"))
                return null;*/
                if (c.getControlNewText().length() == 3 && c.isAdded()) {
                    c.setText("-");
                    transferCaretPosition(c);
                }
                if (c.getControlNewText().length() == 2 && !c.isDeleted()) {
                    c.setText(c.getText() + "-");
                    transferCaretPosition(c);
                }
                return c;
            };
        }
        if ("ВИЕЛ.**.*.***.***".equals(str)) {
            defaultText="ВИЕЛ.";
            converter = new StringConverter<String>() {
                @Override
                public String toString(String object) {
                    if (object == null) return "ВИЕЛ.";
                    return object.toString();
                }
                @Override
                public String fromString(String string) {
                    Pattern regexp = Pattern.compile("ВИЕЛ.\\d\\d\\.\\d\\.\\d\\d\\d\\.\\d\\d\\d"); //Пример строки для ВИЕЛ.16.9.154.320
                    Matcher m = regexp.matcher(string);
                    if (string.length() == 17 && m.matches()) return string;
                    else return "ВИЕЛ.";

                }
            };
            filter = c -> {
                if (c.getControlCaretPosition()<"ВИЕЛ.".length()){
                    c.setCaretPosition("ВИЕЛ.".length());
                    c.setAnchor("ВИЕЛ.".length());
                }
                if (c.getControlNewText().length() == 17 && !c.getControlNewText().matches("ВИЕЛ.\\d\\d\\.\\d\\.\\d\\d\\d\\.\\d\\d\\d")) {
                    textFieldDesignation.setText("ВИЕЛ.");
                    textFieldDesignation.positionCaret("ВИЕЛ.".length() + 1);
                    return null;
                }
                if ((!c.getText().matches("\\d") && !c.isDeleted()) || c.getControlNewText().length() < 5 || c.getControlNewText().length() > 17 || (c.getRangeStart() >= 0 && c.getRangeEnd() < 5))
                    return null;
                /*if (c.getText().matches("[a-zA-Zа-яА-ЯёЁ]") || c.getControlNewText().length() < 6 || c.getControlNewText().length() > 17
                        || (c.getRangeStart() >= 0 && c.getRangeEnd() < 5)|| c.getText().matches("\\W") || c.getText().matches("_"))
                    return null;*/
                if (c.isAdded()){
                    int length=c.getControlNewText().length();
                    if (length==8 || length==10 || length==14) {
                        c.setText(".");
                        transferCaretPosition(c);
                    }
                }
                if (!c.isDeleted()) {
                    int caretPosition = c.getControlCaretPosition();
                    if (caretPosition == 6 || caretPosition == 8 || caretPosition == 12) {
                        c.setText(c.getText() + ".");
                        transferCaretPosition(c);
                    }
                }
                return c;
            };
        }
        if ("ВИЕЛ.******.***".equals(str)){
            defaultText="ВИЕЛ.";
            converter=new StringConverter<String>() {
                @Override
                public String toString(String object) {
                    if (object == null) return "ВИЕЛ.";
                    return object.toString();
                }
                @Override
                public String fromString(String string) {
                    Pattern regexp = Pattern.compile("ВИЕЛ.\\d\\d\\d\\d\\d\\d\\.\\d\\d\\d"); //Пример строки для ВИЕЛ.169154.320
                    Matcher m = regexp.matcher(string);
                    if (string.length() == 15 && m.matches()) return string;
                    else return "ВИЕЛ.";
                }
            };
            filter= c -> {
                if (c.getControlCaretPosition()<"ВИЕЛ.".length()){
                    c.setCaretPosition("ВИЕЛ.".length());
                    c.setAnchor("ВИЕЛ.".length());
                }
                if (c.getControlNewText().length() == 15 && !c.getControlNewText().matches("ВИЕЛ.\\d\\d\\d\\d\\d\\d\\.\\d\\d\\d")) {
                    textFieldDesignation.setText("ВИЕЛ.");
                    textFieldDesignation.positionCaret("ВИЕЛ.".length() + 1);
                    return null;
                }
                if ((!c.getText().matches("\\d") && !c.isDeleted()) || c.getControlNewText().length() < 5 || c.getControlNewText().length() > 15 || (c.getRangeStart() >= 0 && c.getRangeEnd() < 5))
                    return null;
               /* if (c.getText().matches("[a-zA-Zа-яА-ЯёЁ]") || c.getControlNewText().length() < 6 || c.getControlNewText().length() > 15
                        || (c.getRangeStart() >= 0 && c.getRangeEnd() < 5)|| c.getText().matches("\\W") || c.getText().matches("_"))
                    return null;*/
                if (c.isAdded()){
                    int length=c.getControlNewText().length();
                    if (length==12) {
                        c.setText(".");
                        transferCaretPosition(c);
                    }
                }
                if (!c.isDeleted()) {
                    int caretPosition = c.getControlCaretPosition();
                    if (caretPosition == 10) {
                        c.setText(c.getText() + ".");
                        transferCaretPosition(c);
                    }
                }
                return c;
            };
        }

        if ("ЭСКИЗ-*.**.*.***.***".equals(str)){
            defaultText="ЭСКИЗ-";
            converter=new StringConverter<String>() {

                @Override
                public String toString(String object) {
                    if (object == null) return "ЭСКИЗ-";
                    return object.toString();
                }
                @Override
                public String fromString(String string) {
                    Pattern regexp = Pattern.compile("ЭСКИЗ-\\d\\.\\d\\d\\.\\d\\.\\d\\d\\d\\.\\d\\d\\d"); //Пример строки для ЭСКИЗ-2.19.162.555
                    Matcher m = regexp.matcher(string);
                    if (string.length() == 20 && m.matches()) return string;
                    else return "ЭСКИЗ-";
                }
            };
            filter= c -> {
                if (c.getControlCaretPosition()<"ЭСКИЗ-".length()){
                    c.setCaretPosition("ЭСКИЗ-".length());
                    c.setAnchor("ЭСКИЗ-".length());
                }
                if (c.getControlNewText().length() == 20 && !c.getControlNewText().matches("ЭСКИЗ-\\d\\.\\d\\d\\.\\d\\.\\d\\d\\d\\.\\d\\d\\d")) {
                    textFieldDesignation.setText("ЭСКИЗ-");
                    textFieldDesignation.positionCaret("ЭСКИЗ-".length() + 1);
                    return null;
                }
                if ((!c.getText().matches("\\d") && !c.isDeleted()) || c.getControlNewText().length() < 6 || c.getControlNewText().length() > 20 || (c.getRangeStart() >= 0 && c.getRangeEnd() < 6)) return null;
                /*(c.getText().matches("[a-zA-Zа-яА-ЯёЁ]") || c.getControlNewText().length() < 6 || c.getControlNewText().length() > 20
                        || (c.getRangeStart() >= 0 && c.getRangeEnd() < 6) || c.getText().matches("\\W") || c.getText().matches("_"))*/
                if (c.isAdded()){
                    int length=c.getControlNewText().length();
                    if (length==8 || length==11 || length==13 || length==17) {
                        c.setText(".");
                        transferCaretPosition(c);
                    }
                }
                if (!c.isDeleted()) {
                    int caretPosition = c.getControlCaretPosition();
                    if (caretPosition == 6 || caretPosition == 9 || caretPosition == 11 || caretPosition == 15) {
                        c.setText(c.getText() + ".");
                        transferCaretPosition(c);
                    }
                }
                return c;
            };
        }
        if ("Э-*.**.*.***.***".equals(str)){
            defaultText="Э-";
            converter=new StringConverter<String>() {
                @Override
                public String toString(String object) {
                    if (object == null) {
                        return "Э-";
                    }
                    return object.toString();
                }
                @Override
                public String fromString(String string) {
                    Pattern regexp = Pattern.compile("[Э][-]\\d\\.\\d\\d\\.\\d\\.\\d\\d\\d\\.\\d\\d\\d"); //Пример строки для Э-2.19.8.162.560
                    Matcher m = regexp.matcher(string);
                    if (string.length() == 16 && m.matches()) return string;
                    else {
                        return "Э-";
                    }
                }
            };
            filter= c -> {
                if (c.getControlCaretPosition()<2){
                    c.setCaretPosition("Э-".length());
                    c.setAnchor("Э-".length());
                }
                if (c.getControlNewText().length() == 16 && !c.getControlNewText().matches("Э-\\d\\.\\d\\d\\.\\d\\.\\d\\d\\d\\.\\d\\d\\d")) {
                    textFieldDesignation.setText("Э-");
                    textFieldDesignation.positionCaret("Э-".length() + 1);
                    return null;
                }
                if (c.getControlNewText().length() < 2 || c.getControlNewText().length() > 16 || (c.getRangeStart() >= 0 && c.getRangeEnd() < 2) || (!c.getText().matches("\\d") && !c.isDeleted()))
                    return null;
                //if (c.getText().matches("[a-zA-Zа-яА-ЯёЁ]") || c.getControlNewText().length() < 2 || c.getControlNewText().length() > 16 || (c.getRangeStart() >= 0 && c.getRangeEnd() < 2))
                //    return null;
                if (c.isAdded()){
                    int length=c.getControlNewText().length();
                    if (length==4 || length==7 || length==9 || length==13) {
                        c.setText(".");
                        transferCaretPosition(c);
                    }
                }
                if (!c.isDeleted()) {
                    int caretPosition = c.getControlCaretPosition();
                    if (caretPosition == 2 || caretPosition == 5 || caretPosition == 7 || caretPosition == 11) {
                        c.setText(c.getText() + ".");
                        transferCaretPosition(c);
                    }
                }
                return c;
            };
        }
        if ("*ВЩ.***.***".equals(str)){
            defaultText="";
            converter=new StringConverter<String>() {
                @Override
                public String toString(String object) {
                    if (object == null) return "";
                    return object.toString();
                }
                @Override
                public String fromString(String string) {
                    Pattern regexp = Pattern.compile("\\d[В][Щ]\\.\\d\\d\\d\\.\\d\\d\\d"); //Пример строки для 5ВЩ.162.677
                    Matcher m = regexp.matcher(string);
                    if (string.length() == 11 && m.matches()) return string;
                    else return "";
                }
            };
            filter= c -> {
                if (c.getControlNewText().length() == 12) return null;
                if (!c.getText().matches("\\d") && !c.isDeleted()) return null;
                if (c.isAdded()){
                    int length=c.getControlNewText().length();
                    if (length==8 || length==4) {
                        c.setText(".");
                        transferCaretPosition(c);
                    }
                    if (length==3) {
                        c.setText("Щ");
                        transferCaretPosition(c);
                    }
                    if (length==2) {
                        c.setText("В");
                        transferCaretPosition(c);
                    }
                }
                if (!c.isDeleted()) {
                    int caretPosition = c.getControlCaretPosition();
                    if (caretPosition == 0){
                        c.setText(c.getText() + "ВЩ.");
                        transferCaretPosition(c);
                    }
                    if (caretPosition == 6) {
                        c.setText(c.getText() + ".");
                        transferCaretPosition(c);
                    }
                }
                return c;
            };
        }
        return new TextFormatter<>(converter, defaultText, filter);
    }
    private static void transferCaretPosition(TextFormatter.Change c){
        c.setCaretPosition(c.getControlNewText().length());
        c.setAnchor(c.getControlNewText().length());
    }
    public TextFormatterUtil() {
    }
}
