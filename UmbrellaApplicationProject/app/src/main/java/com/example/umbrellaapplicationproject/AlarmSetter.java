package com.example.umbrellaapplicationproject;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/*
 * 알람을 세팅하는 클래스
 * call class : CalendarSetter
 */

public class AlarmSetter {
    private Context context;
    private Calendar calendar;
    private int[] days;
    private int firstAlarmTime;
    private ArrayList<Object> inputBox;
    private ArrayList<Object> returnBox;

    public AlarmSetter(Context context) {
        this.context = context;
        inputBox = new ArrayList<>();
        returnBox = new ArrayList<>();
    }

    public void setAlarm() {
        Intent intent = new Intent(context, AlarmReceiver.class);
        calendar = Calendar.getInstance();
        /* 박스로 필요 데이터를 넣어서 CalenderSetter로 넘김 */
        inputBox.add(calendar);
        inputBox.add(days);
        inputBox.add(firstAlarmTime);
        CalendarSetter calendarSetter = new CalendarSetter(context, inputBox);
        returnBox = calendarSetter.setCalender();
        days = (int[]) returnBox.get(0);
        firstAlarmTime = (int) returnBox.get(1);

        /* 알람 시간이 현재 시간보다 빠르면 하루 뒤로 맞춤 */
        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DATE, 1);
        }
        Log.e("log", "******새 알람 세팅 : " + calendar.getTime());

        intent.putExtra("days", days);
        intent.putExtra("firstAlarmTime", firstAlarmTime);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT < 19) { // 19 이하는 setReapeating 함수로 오차 없는 알람 설정
            Log.e("log", "알람 함수 : setRepeating()");
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
            // setRepeating INTERVAL : 하루에 한 번씩 울려야 하므로 AlarmManager.INTERVAL_DAY로 설정
        } else if (Build.VERSION.SDK_INT < 23) { // 22 이하는 setExact 함수로 오차 없는 알람 설정
            Log.e("log", "알람 함수 : setExact()");
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } else { // 23부터는 setExactAndAllowWhileIdle() 함수로 오차 없는 알람 설정
            Log.e("log", "알람 함수 : setExactAndAllowWhileIdle()");
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }
}
