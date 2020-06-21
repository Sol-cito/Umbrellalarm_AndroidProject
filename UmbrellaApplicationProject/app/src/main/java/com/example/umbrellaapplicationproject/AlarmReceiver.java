package com.example.umbrellaapplicationproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;

public class AlarmReceiver extends BroadcastReceiver {

    private SharedPreferences sharedPreferences;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("log", "알람리시버 작동");
        sharedPreferences = context.getSharedPreferences("switch", Context.MODE_PRIVATE);
        int switchValue = sharedPreferences.getInt("switch", 0);
        if (switchValue == 0) {
            Log.e("log", "스위치값 0이라 알람 안함");
            return;
        }
        int[] days = intent.getIntArrayExtra("days");
        int firstAlarmTime = intent.getIntExtra("firstAlarmTime", 0);
        Log.e("log", "firstAlarmTime(알리시버) : " + firstAlarmTime);
        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DAY_OF_WEEK); // sun : 1 ~ sat : 7
        int hourRightNow = calendar.get(Calendar.HOUR_OF_DAY);

        if (days[today - 1] != 1) {
            Log.e("log", today - 1 + " : 알람 설정한 날이 아님");
            return;
        }
        WeatherDataReceiver weatherDataReceiver = new WeatherDataReceiver();
        /* test */
//        weatherDataReceiver.getAPIdata(context, firstAlarmTime);
        /* test end */
        if (hourRightNow > firstAlarmTime && hourRightNow <= firstAlarmTime + 3) {
            weatherDataReceiver.getAPIdata(context, firstAlarmTime);
        } else {
            weatherDataReceiver.getRSSdata(context);
        }
        Toast.makeText(context, "test : 알람 실행됨", Toast.LENGTH_SHORT).show();
    }
}
