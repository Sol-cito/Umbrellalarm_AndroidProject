package com.example.umbrellaapplicationproject;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class WeatherDataReceiver {

    private SQLiteDatabase sqLiteDatabase;
    private final String DBNAME = "alarmData";
    private BackgroundThreadForXML backgroundThreadForXML;
    private final static String SERVICE_KEY = "c1g26jTnByGW5kb0HXyLjLfpLsO%2FcByKq4WxxOygJ2GBxWCHOVvFPVSbrHJ6LY2uMqkHDT7kkLVAUKyit3ykEg%3D%3D";
    private int firstAlarmTimeFromAlarmReceiver;
    private String location;
    private String[] notificationMessage = new String[6];
    private HashMap<Integer, Integer> entirePopMap;

    public void getRSSdata(Context context) {
        sqLiteDatabase = context.openOrCreateDatabase(DBNAME, context.MODE_PRIVATE, null);

        String[] provAndSubProvFromDBFromDB = getProvAndSubProvFromDB();
        Long zoneCode = getZoneCode(provAndSubProvFromDBFromDB[1]);

        Document doc = null;
        backgroundThreadForXML = new BackgroundThreadForXML();
        try {
            doc = backgroundThreadForXML.execute(zoneCode).get();
            NodeList nodeList = doc.getElementsByTagName("pop");
            location = provAndSubProvFromDBFromDB[0] + " " + provAndSubProvFromDBFromDB[1];
            Log.e("log", "지역 : " + doc.getElementsByTagName("category").item(0).getTextContent());

            /* 현재 시간 구하기 */
            long currentTime = System.currentTimeMillis();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH");
            int currentHour = Integer.parseInt(simpleDateFormat.format(currentTime));
            int[] castedHourArr = new int[8];
            HashMap<Integer, Integer> popMap = new HashMap<>();
            for (int i = 0; i < 8; i++) { // 총 16개 중 8개만 받아옴 ( 3*8 = 24 이므로 당일 전체 데이터를 받음
                int castedHour = currentHour + 3 * (i + 1);
                Log.e("log", "받아온 시간 : " + castedHour);
                castedHourArr[i] = castedHour; //3시간 후의 시간을 array에 저장
                popMap.put(castedHour, Integer.parseInt(nodeList.item(i).getTextContent()));
            }
            /* 설정한 시간대+강수확률과 실제 받아온 데이터를 비교 */
            /*
             * 로직이 분기한다.
             * getAPIdata 는 백그라운드 쓰레드이기 때문에, response 콜백 함수에서 setPrecipitationDataWithPopMap 및 notification 함수를 실행해야 한다.
             * 따라서, timeZoneStartPointdl -1일 때(알람 시간과 현재 시간이 겹치지 않을 때)는 바로 다음 함수로 진행,
             * 그렇지 않을 때는 getAPIdata의 onResponse 함수에서 다음 함수로 진행한다.
             * */
            int timeZoneStartPoint = compareSetTimedataWithWeatherCast(castedHourArr, popMap, currentHour);
            if (timeZoneStartPoint == -1) {
                setPrecipitationDataWithPopMap(entirePopMap);
                notification(notificationMessage, location, context);
            } else {
                getAPIdata(context, timeZoneStartPoint);
            }
            Log.e("log", "엔타이어 팝맵 : " + entirePopMap);
            Log.e("log", "RSSdata 리시버 성공");
        } catch (Exception e) {
            Log.e("log", "값 받아오기 실패");
            e.printStackTrace();
        }
    }


    public int compareSetTimedataWithWeatherCast(int[] castedHourArr, HashMap<Integer, Integer> popMap, int currentHour) {
        int returnTimeZone = -1;
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT time1, time2, time3, time4, time5, time6 " +
                "FROM " + DBNAME, null);
        cursor.moveToNext();
        int[] timeArr = new int[6];
        for (int i = 0; i < 6; i++) {
            timeArr[i] = cursor.getInt(i);
            Log.e("log", i + "번째 설정 시간 확인 : " + timeArr[i]);
        }
        entirePopMap = new HashMap<>(); // 설정 안해놓은 시간의 value는 -2
        for (int i = 0; i < 6; i++) {
            int eachPopValue = 0;
            int numOfStoredPopValue = 0;
            if (timeArr[i] == 1) { //설정해놓은 시간일 때
                Log.e("log", "알람 설정 시간 : " + i);
                int timeZoneStartPoint = (3 * i) + 6;
                int timeZoneEndPoint = timeZoneStartPoint + 3;
                if (currentHour >= timeZoneStartPoint && currentHour < timeZoneEndPoint) { // 설정해놓은 시간 중 현재 시간과 겹칠 때
                    Log.e("log", "설정 시간 중 현재 시간과 겹침");
                    entirePopMap.put(i, -1); // 얘도 -1 저장
                    Log.e("log", "returnTimeZone : " + returnTimeZone);
                    Log.e("log", "timeZoneStartPoint : " + timeZoneStartPoint);
                    returnTimeZone = timeZoneStartPoint;
                    continue;
                }
                for (int each : castedHourArr) {
                    if (each > timeZoneEndPoint) {
                        break;
                    }
                    if (each >= timeZoneStartPoint && each <= timeZoneEndPoint) {
                        eachPopValue += popMap.get(each);
                        numOfStoredPopValue++;
                    }
                }
                if (numOfStoredPopValue <= 0) { // 설정은 해놓았지만 알람 시간이 설정시간보다 느릴 때 (notification 줄 필요 없음)
                    Log.e("log", "설정은 해놓았지만 알람 시간이 설정시간보다 느릴 때");
                    Log.e("log", "-1 저장");
                    entirePopMap.put(i, -1); // value : -1
                } else {
                    entirePopMap.put(i, eachPopValue / numOfStoredPopValue);
                    Log.e("log", "현재 시간과 겹치지 않고 이후 시간이라 entirePop 저장");
                }
            } else { // 설정 안해놓은 시간일 때
                Log.e("log", "설정 안해놓은 시간임");
                entirePopMap.put(i, -2); // value : -2
            }
        }
        Log.e("log", "최종 리턴 returnTimeZone : " + returnTimeZone);
        return returnTimeZone;
    }

    public String basetimeParsing(int timeZoneStartPoint) {
        /*
         * 동네예보 basetime은
         * Base_time : 0200, 0500, 0800, 1100, 1400, 1700, 2000, 2300 (1일 8회)
         * timeZoneStartPoint : 0600, 0900, 1200, 1500, 1800, 2100
         * 이므로, 가장 가까운 시간에 매핑한다.
         * */
        String[] basetimeList = {"0500", "0800", "1100", "1400", "1700", "2000"};
        int[] timeZoneStartPointList = {6, 9, 12, 15, 18, 21};
        String baseTime = "";
        for (int i = 0; i < timeZoneStartPointList.length; i++) {
            if (timeZoneStartPoint == timeZoneStartPointList[i]) {
                baseTime = basetimeList[i];
            }
        }
        return baseTime;
    }

    public void getAPIdata(Context context, int timeZoneStartPoint) {
        Log.e("log", "getAPIdata 작동");
        final Context FINALCONTEXT = context;
        firstAlarmTimeFromAlarmReceiver = timeZoneStartPoint;
        final RequestQueue requestQueue = Volley.newRequestQueue(context);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        String baseDate = simpleDateFormat.format(new Date());
        String url = "http://apis.data.go.kr/1360000/VilageFcstInfoService/getVilageFcst?serviceKey="
                + SERVICE_KEY + "&numOfRows=1&pageNo=1&dataType=JSON&base_date=" + baseDate +
                "&base_time=" + basetimeParsing(timeZoneStartPoint) + "&nx=55&ny=127"; // 위도 경도 조절해야 함 + baseTime 조절해야 함
        Log.e("log", "URL : " + url);
        Log.e("log", "-----basetime : " + basetimeParsing(timeZoneStartPoint));
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject body = response.getJSONObject("response").getJSONObject("body");
                    JSONObject item = body.getJSONObject("items").getJSONArray("item").getJSONObject(0);
                    String fcstValue = item.getString("fcstValue");
                    Log.e("log", "volley 성공 / 강수량 : " + fcstValue);
                    int messageIndex = firstAlarmTimeFromAlarmReceiver / 3 - 2;
                    notificationMessage[messageIndex] = firstAlarmTimeFromAlarmReceiver + "시 - " + (firstAlarmTimeFromAlarmReceiver + 3) + "시의 평균 강수확률 : " + fcstValue + "%";
                    Log.e("log", messageIndex + "번째 메시지 세팅(getAPIdata) : " + notificationMessage[messageIndex]);
                    /* response가 오면 message 세팅 및 notification*/
                    setPrecipitationDataWithPopMap(entirePopMap);
                    notification(notificationMessage, location, FINALCONTEXT);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("log", "volley 실패 : " + error);
            }
        });
        jsonObjectRequest.setShouldCache(false);
        requestQueue.add(jsonObjectRequest);
    }

    /* 실제 예보 강수확률의 비교 결과를 notification()으로 보냄 */
    public void setPrecipitationDataWithPopMap(HashMap<Integer, Integer> entirePopMap) {
        Log.e("log", "===========메시지 설정 시작");
        for (int i = 0; i < 6; i++) {
            if (entirePopMap.get(i) < 0) {
                continue; // -1이 value로 저장되어있으면 pass
            }
            Log.e("log", "<----메시지" + i + "번째");
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
            Log.e("log", "getRSS에서 설정한 메시지 : " + notificationMessage[i]);
        }
        /* test */
        for (String each : notificationMessage) {
            Log.e("log", "메시지 ; " + each);
        }
    }

    /* Set notification service */
    public void notification(String[] notificationMessage, String location, Context context) {
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
}
