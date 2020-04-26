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
        Intent activityIntent = new Intent(context, MainActivity.class);
        activityIntent.putExtra("alarmFiring", true);
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        /* 위 INTENT가 전달되어 activity 실행될 때 애니메이션 제거해서 자연스럽긴 한데,
        * activity가 2개 겹쳐있는 상태라, 뒤에꺼 제거하는 로직 추가하면서 애니메이션 제거하면 될듯 */
        context.startActivity(activityIntent);
        // 알람이 울렸을 때 activity가 작동하게 해야한다
        // Intent로 setClass를 하여 MainActivity를 붙여야 할듯

        /* https://foradun.tistory.com/entry/Activity-Single-Instance-%EC%9C%A0%EC%A7%80%ED%95%98%EA%B8%B0 */
        //위 블로그에 같은 현상 해결법(런쳐)
        /*
        * https://m.blog.naver.com/PostView.nhn?blogId=jogilsang&logNo=221513058119&proxyReferer=https:%2F%2Fwww.google.com%2F
        * 위 블로그를 참고하자.*/
    }
}
