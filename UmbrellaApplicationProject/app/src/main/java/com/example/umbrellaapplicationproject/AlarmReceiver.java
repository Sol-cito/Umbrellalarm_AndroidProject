package com.example.umbrellaapplicationproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("log", "알람리시버 작동?");
        int[] days = intent.getIntArrayExtra("days");
        Log.e("log", "알람리시버 데이즈 사이즈 : " + days.length);
        for (int i = 0; i < days.length; i++) {
            Log.e("log", "각각 : " + i + " / " + days[i]);
        }
        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DAY_OF_WEEK); // sun : 1 ~ sat : 7

        Log.e("log", "today : " + today);
        if (days[today - 1] != 1) {
            Log.e("log", today - 1 + " : 알람 설정한 날이 아님");
            return;
        }
        Log.e("log", "브로드캐스트 온 리시버 작동");

        WeatherDataReceiver weatherDataReceiver = new WeatherDataReceiver();
        weatherDataReceiver.getRSSdata(context);

        Toast.makeText(context, "알람 실행됨", Toast.LENGTH_SHORT).show();
    }
}
