package com.yandex.ajwar.util;

import javafx.scene.control.DatePicker;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

/**
 * Created by 53189 on 29.11.2016.
 */
public class DateUtil {
    /**Перевожу в формат и задаю начальную дату в DataPicker*/
    public static final LocalDate NOW_LOCAL_DATE (){
        String date = new SimpleDateFormat("dd-MM-yyyy").format(Calendar.getInstance().getTime());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate localDate = LocalDate.parse(date , formatter);
        return localDate;
    }
    /**Перевожу дату в формат понятный MSSQL*/
    public static String parseDateToString (LocalDate localDate){
        SimpleDateFormat fm=new SimpleDateFormat("dd.MM.yyyy");
        return fm.format(Date.valueOf(localDate));
    }
    /**Проверка правильной даты в DatePicker*/
    public static boolean checkRightDate(DatePicker datePicker){
        if (datePicker.getValue()== null) return false;
        try {
            LocalDate.parse(datePicker.getValue().toString());
        } catch (Exception e) {
            return false;
        }return true;
    }
}
