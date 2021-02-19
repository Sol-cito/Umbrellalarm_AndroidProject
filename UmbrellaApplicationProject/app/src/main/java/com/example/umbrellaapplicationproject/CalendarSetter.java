package com.example.umbrellaapplicationproject;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;

/*
 * 알람에 필요한 Calendar를 세팅하는 클래스
 */

public class CalendarSetter {
    private Calendar calendar;
    private int[] days;
    private int firstAlarmTime;
    private final static String DB_TABLE_NALE = "alarmData";
    private SQLiteDatabase sqLiteDatabase;

    public CalendarSetter(Context context, ArrayList<Object> box) {
        this.calendar = (Calendar) box.get(0);
        this.days = (int[]) box.get(1);
        this.firstAlarmTime = (int) box.get(2);
        sqLiteDatabase = context.openOrCreateDatabase(DB_TABLE_NALE, context.MODE_PRIVATE, null);
    }

    public ArrayList<Object> setCalender() {
        ArrayList<Object> returnBox = new ArrayList<>();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT sun, mon, tue, wed, thu, fri, sat, setHour, setMinute, " +
                "time1, time2, time3, time4, time5, time6 FROM " + DB_TABLE_NALE, null);
        cursor.moveToNext();
        int setHour = cursor.getInt(7);
        int setMinute = cursor.getInt(8);
        days = new int[7];
        for (int i = 0; i < 7; i++) {
            if (cursor.getInt(i) == 1) {
                days[i] = 1; //  if checked 1,  else 0
            }
        }
        /* get firstAlarmTime*/
        for (int i = 9; i < 15; i++) {
            if (cursor.getInt(i) == 1) {
                int parsedTime = i - 9;
                switch (parsedTime) {
                    case 0:
                        firstAlarmTime = 6;
                        break;
                    case 1:
                        firstAlarmTime = 9;
                        break;
                    case 2:
                        firstAlarmTime = 12;
                        break;
                    case 3:
                        firstAlarmTime = 15;
                        break;
                    case 4:
                        firstAlarmTime = 18;
                        break;
                    case 5:
                        firstAlarmTime = 21;
                        break;
                }
                Log.e("log", "setCalender() -> firstAlarmTime : " + firstAlarmTime);
                break;
            }
        }
        /* hour, minute, days setting */
        calendar.set(Calendar.HOUR_OF_DAY, setHour);
        calendar.set(Calendar.MINUTE, setMinute);
        calendar.set(Calendar.SECOND, 0);
//        returnBox.add(calendar);
        returnBox.add(days);
        returnBox.add(firstAlarmTime);
        return returnBox;
    }
}
