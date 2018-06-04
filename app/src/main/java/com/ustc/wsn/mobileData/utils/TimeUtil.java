package com.ustc.wsn.mobileData.utils;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TimeUtil {
    public static String getTime(long millis) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        Date curDate = new Date(millis);
        return formatter.format(curDate);
    }

    public static String getTime_name(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        return String.valueOf(c.get(Calendar.YEAR)) + "_" + String.valueOf(c.get(Calendar.MONTH) + 1) + "_" + String.valueOf(c.get(Calendar.DAY_OF_MONTH)) + "_" + String.valueOf(c.get(Calendar.HOUR_OF_DAY)) + "_" + String.valueOf(c.get(Calendar.MINUTE) + "_" + String.valueOf(c.get(Calendar.SECOND)));
    }

    /**
     * 灏嗘椂闂磋嚜鍔ㄨˉ榻�
     *
     * @param field 鏃堕棿鍩�,骞达紝鏈堬紝鏃ワ紝 灏忔椂锛屽垎閽燂紝绉掞紝寰
     * @param type  1: year; 2: month, day, hour, minute, second; 3:millis
     * @return
     */
    public static String normalize(int field, int type) {
        String s = "0000" + String.valueOf(field);
        switch (type) {
            case 1:
                s = s.substring(s.length() - 3);
                break;
            case 2:
                s = s.substring(s.length() - 1);
                break;
            case 3:
                s = s.substring(s.length() - 2);
                break;
        }
        return s;
    }

}
