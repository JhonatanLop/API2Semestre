/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.openjfx.api2semestre.data_utils;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;

/**
 *
 * @author User
 */
// método que converte data em LocalDate e tempo em string para Timestamp
public class DateConverter {
    public static Timestamp inputToTimestamp (LocalDate dateInput, String timeInput) {
        Date date = Date.valueOf(dateInput);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String str_date = format.format(date);
        String str_date_time = str_date + " " + (timeInput.length() <3 ? timeInput + ":00" : timeInput) + ":00";
        Timestamp timestamp = Timestamp.valueOf(str_date_time);
        return timestamp;
    }
}
