package com.example.umbrellaapplicationproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("log", "브로드캐스트 온 리시버 작동");
        Toast.makeText(context, "알람 실행됨", Toast.LENGTH_SHORT).show();
    }
}
