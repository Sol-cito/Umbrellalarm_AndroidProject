package com.example.umbrellaapplicationproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;

public class AlarmReceiver extends BroadcastReceiver {

    private Context context;
    private SQLiteDatabase sqLiteDatabase;
    private SQLiteDatabaseHelper sqLiteDatabaseHelper;

    @Override
    public void onReceive(Context context, Intent intent) {
        int[] days = intent.getIntArrayExtra("days");
        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DAY_OF_WEEK); // sun : 1 ~ sat : 7

        Log.e("log", "today : " + today);
        if (days[today - 1] != 1) {
            Log.e("log", days[today] + " : 알람 설정한 날이 아님");
            return;
        }

        Log.e("log", "브로드캐스트 온 리시버 작동");

        RSSdataReceiver rsSdataReceiver = new RSSdataReceiver();
        rsSdataReceiver.getRSSdata(context);

//        Toast.makeText(context, "알람 실행됨", Toast.LENGTH_SHORT).show();
//        Intent activityIntent = new Intent(context, MainActivity.class);
//        activityIntent.putExtra("alarmFiring", true);
//        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
//        /* 위 INTENT가 전달되어 activity 실행될 때 애니메이션 제거해서 자연스럽긴 한데,
//         * activity가 2개 겹쳐있는 상태라, 뒤에꺼 제거하는 로직 추가하면서 애니메이션 제거하면 될듯 */
//        context.startActivity(activityIntent);

    }
}
