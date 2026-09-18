package com.ls.cmd_ai.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

    public static String getTimeTableDate(Date d) {
        SimpleDateFormat transFormatYearMonth = new SimpleDateFormat("yyyyMM");
        return transFormatYearMonth.format(d);
    }

    public static String getDateFormatyyyyMMddHHmmSS(Date d) {
        SimpleDateFormat transFormatYearMonth = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return transFormatYearMonth.format(d);
    }

}
