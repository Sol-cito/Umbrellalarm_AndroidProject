package com.example.umbrellaapplicationproject;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class WeatherDataReceiver {

    private SQLiteDatabase sqLiteDatabase;
    private final String DBNAME = "alarmData";
    private BackgroundThreadForXML backgroundThreadForXML;

    public void getRSSdata(Context context) {
        sqLiteDatabase = context.openOrCreateDatabase(DBNAME, context.MODE_PRIVATE, null);

        String[] provAndSubProvFromDBFromDB = getProvAndSubProvFromDB();
        Long zoneCode = getZoneCode(provAndSubProvFromDBFromDB[1]);

        Document doc = null;
        backgroundThreadForXML = new BackgroundThreadForXML();
        try {
            doc = backgroundThreadForXML.execute(zoneCode).get();
            NodeList nodeList = doc.getElementsByTagName("pop");
            String location = provAndSubProvFromDBFromDB[0] + " " + provAndSubProvFromDBFromDB[1];
            Log.e("log", "지역 : " + doc.getElementsByTagName("category").item(0).getTextContent());

            /* 현재 시간 구하기 */
            long currentTime = System.currentTimeMillis();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH");
            int currentHour = Integer.parseInt(simpleDateFormat.format(currentTime));
            ArrayList<Integer> castedHourList = new ArrayList<>();
            HashMap<Integer, Integer> popMap = new HashMap<>();
            for (int i = 0; i < nodeList.getLength(); i++) {
                //이거 16개(48시간)을 받아오는데, 굳이 16개 다 안받아와도 될듯..? 당일 데이터만 받아오면 될듯(3*8개 = 24시간)
                int castedHour = currentHour + 3 * (i + 1);
                castedHourList.add(castedHour); //3시간 후의 시간을 list에 저장
                popMap.put(castedHour, Integer.parseInt(nodeList.item(i).getTextContent()));
                Log.e("log", "저장한 시간 : " + castedHour + " / pop : " + Integer.parseInt(nodeList.item(i).getTextContent()));
            }
            /* 설정한 시간대+강수확률과 실제 받아온 데이터를 비교 */
            HashMap<Integer, Integer> entirePopMap = compareSetTimedataWithWeatherCast(castedHourList, popMap);
            compareSetPrecipitationDataWithPopMap(entirePopMap, location, context);
            /* 위 두 함수 완료되면 Notification 함수 실행 */
            Log.e("log", "RSSdata 리시버 성공");
        } catch (Exception e) {
            Log.e("log", "값 받아오기 실패");
            e.printStackTrace();
        }
    }

    public String[] getProvAndSubProvFromDB() {
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT prov, subProv FROM " + DBNAME, null);
        cursor.moveToNext();
        String[] provAndSub = new String[2];
        provAndSub[0] = cursor.getString(0);
        provAndSub[1] = cursor.getString(1);
        return provAndSub;
    }

    public Long getZoneCode(String subProvFromDB) {
        HashMap<String, Long> zoneMap = new HashMap<>();
        /* Seoul subProv */
        zoneMap.put("강남구", 1168066000L);
        zoneMap.put("강동구", 1174051500L);
        zoneMap.put("강북구", 1130553500L);
        zoneMap.put("강서구", 1150060300L);
        zoneMap.put("관악구", 1162058500L);
        zoneMap.put("광진구", 1121581000L);
        zoneMap.put("구로구", 1153059500L);
        zoneMap.put("금천구", 1154551000L);
        zoneMap.put("노원구", 1135059500L);
        zoneMap.put("도봉구", 1132052100L);
        zoneMap.put("동대문구", 1123060000L);
        zoneMap.put("동작구", 1159051000L);
        zoneMap.put("마포구", 1144056500L);
        zoneMap.put("서대문구", 1141069000L);
        zoneMap.put("서초구", 1165066000L);
        zoneMap.put("성동구", 1120059000L);
        zoneMap.put("성북구", 1129066000L);
        zoneMap.put("송파구", 1171063100L);
        zoneMap.put("양천구", 1147051000L);
        zoneMap.put("영등포구", 1156055000L);
        zoneMap.put("용산구", 1117053000L);
        zoneMap.put("은평구", 1138055100L);
        zoneMap.put("종로구", 1111060000L);
        zoneMap.put("중구", 1114059000L);
        zoneMap.put("중랑구", 1126065500L);

        /* Kyeunggi subProv*/
        zoneMap.put("가평군", 4182025000L);
        zoneMap.put("고양시", 4128560000L);
        zoneMap.put("과천시", 4129052000L);
        zoneMap.put("광명시", 4121051000L);
        zoneMap.put("광주시", 4161051000L);
        zoneMap.put("구리시", 4131051000L);
        zoneMap.put("군포시", 4141062000L);
        zoneMap.put("김포시", 4157025300L);
        zoneMap.put("남양주시", 4136053000L);
        zoneMap.put("동두천시", 4125055000L);
        zoneMap.put("부천시", 4119074600L);
        zoneMap.put("성남시", 4113566500L);
        zoneMap.put("수원시", 4111760000L);
        zoneMap.put("시흥시", 4139062100L);
        zoneMap.put("안산시", 4127352500L);
        zoneMap.put("안성시", 4155042000L);
        zoneMap.put("안양시", 4117363000L);
        zoneMap.put("양주시", 4163033000L);
        zoneMap.put("양평군", 4183031000L);
        zoneMap.put("여주시", 4167025000L);
        zoneMap.put("연천군", 4180031000L);
        zoneMap.put("오산시", 4137053000L);
        zoneMap.put("용인시", 4146352000L);
        zoneMap.put("의왕시", 4143051000L);
        zoneMap.put("의정부시", 4115059500L);
        zoneMap.put("이천시", 4150053000L);
        zoneMap.put("파주시", 4148035000L);
        zoneMap.put("평택시", 4122033000L);
        zoneMap.put("포천시", 4165033000L);
        zoneMap.put("하남시", 4145058000L);
        zoneMap.put("화성시", 4159056000L);

        return zoneMap.get(subProvFromDB);
    }

    public HashMap<Integer, Integer> compareSetTimedataWithWeatherCast(ArrayList<Integer> castedHourList, HashMap<Integer, Integer> popMap) {
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT time1, time2, time3, time4, time5, time6 " +
                "FROM " + DBNAME, null);
        cursor.moveToNext();
        int[] timeArr = new int[6];
        for (int i = 0; i < 6; i++) {
            timeArr[i] = cursor.getInt(i);
        }
        int timeZoneStart = 6;
        HashMap<Integer, Integer> entirePopMap = new HashMap<>(); // 설정 안해놓은 시간의 value는 -2
        for (int i = 0; i < 6; i++) {
            int eachPopValue = 0;
            int count = 0;
            if (timeArr[i] == 1) { //설정해놓은 시간일 때
                for (int each : castedHourList) {
                    if (each >= timeZoneStart + (3 * i) && each <= timeZoneStart + (3 * i) + 3) {
                        eachPopValue += popMap.get(each);
                        count++;
                    }
                    if (each > timeZoneStart + (3 * i) + 3) {
                        break;
                    }
                }
                if (count <= 0) {
                    Log.e("log", "-1 저장");
                    entirePopMap.put(i, -1); // -1 : 알람 시간이 강수예측 시간보다 느림
                } else {
                    entirePopMap.put(i, eachPopValue / count);
                }
            } else { // 설정 안해놓은 시간일 때
                entirePopMap.put(i, -2);
            }
        }
        return entirePopMap;
    }

    /* 유저가 설정한 알람 작동 강수확률과 실제 예보 강수확률의 비교 결과를 notification()으로 보냄 */
    public void compareSetPrecipitationDataWithPopMap(HashMap<Integer, Integer> entirePopMap, String location, Context context) {
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT precipitation FROM " + DBNAME, null);
        cursor.moveToNext();
//        int setPrecipitation = cursor.getInt(0); -> 설정해놓은 강수확률
        int setPrecipitation = 0; // 테스트(강수확률 0)
        String[] notificationMessage = new String[6];

        for (int i = 0; i < 6; i++) {
            if (entirePopMap.get(i) >= setPrecipitation) { //실제 예보 강수확률(pop)이 설정한 강수확률 이상일 때
                if (i < 2) {
                    notificationMessage[i] = (6 + i * 3) + "시 - " + (9 + i * 3) + "시의 평균 강수확률 : " + entirePopMap.get(i) + "%";
                } else {
                    int time = 12 + ((i - 2) * 3);
                    if (i == 2) {
                        notificationMessage[i] = "12시 - " + (time + 3) + "시의 평균 강수확률 : " + entirePopMap.get(i) + "%";
                    } else {
                        notificationMessage[i] = time + "시 - " + (time + 3) + "시의 평균 강수확률 : " + entirePopMap.get(i) + "%";
                    }
                }
            }
        }
        notification(notificationMessage, location, context);
    }

    /* Set notification service */
    public void notification(String[] notificationMessage, String location, Context context) {
        //알림 세부 내용 수정 요망
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager.getNotificationChannel("channel_1") == null) {
                notificationManager.createNotificationChannel(new NotificationChannel(
                        "channel_1", "createdChannel", NotificationManager.IMPORTANCE_DEFAULT
                ));
                builder = new NotificationCompat.Builder(context, "channel_1");
            } else {
                builder = new NotificationCompat.Builder(context, "channel_1");
            }
        } else {
            builder = new NotificationCompat.Builder(context);
        }
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        builder.setStyle(inboxStyle);
        builder.setContentTitle(context.getString(R.string.notification_title));
        builder.setSubText(location + " 강수확률");
        for (String eachLine : notificationMessage) {
            inboxStyle.addLine(eachLine);
        }
        builder.setSmallIcon(R.drawable.notification_icon); //알림 아이콘
        Notification notification = builder.build();
        notificationManager.notify(1, notification); //알림 실행
    }
}
