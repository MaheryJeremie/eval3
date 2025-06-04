package com.example.evalrh.util;

import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Util {
    public static String formatDate(String date) throws ParseException {
        if (date == null || date.trim().isEmpty()) {
            return null;
        }
        SimpleDateFormat initialFormat = new SimpleDateFormat("dd/MM/yyyy");
        initialFormat.setLenient(false);
        SimpleDateFormat finalFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date formattedDate;
        try {
            formattedDate = initialFormat.parse(date.trim());
        } catch (ParseException e) {
            throw new ParseException("Invalid date or invalid date format",e.getErrorOffset());
        }
        return finalFormat.format(formattedDate);
    }
}
