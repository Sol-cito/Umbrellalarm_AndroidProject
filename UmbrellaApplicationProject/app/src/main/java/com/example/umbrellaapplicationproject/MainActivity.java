/*

현재 버그 리스트 (메인액티비티)
1. 메인액티비티의 onCreate 에서 createDB() 메소드가 작동한다. 즉,
DB삭제(tmp)버튼을 누르면 table 자체가 drop되어서, 그 다음에 프래그먼트로 간 다음 데이터를 insert하려 해도
테이블 자체가 없으니 에러남   -> 이건 걍 tmp button 때문에 그럼. '삭제'버튼은 이런 현상 없음.
2. Activity finish 하고 다시 intent로 받아오는거 animation이 너무 별로임...

 */


package com.example.umbrellaapplicationproject;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private Button addButton;
    private int whichButtonClicked; // 0 = add clicked , 1 = modification clicked
    private Fragment_alarmSetting fragment_alarmSetting;
    private long lastBackPresseed = 0;

    /* 강수확률 request 관련 변수*/
    private String currentDate;
    private String currentTime;

    /* DataBase */
    private SQLiteDatabase sqLiteDatabase;
    private String dbTableName = "alarmData";

    private LinearLayout addAndDeleteLayout;
    private LinearLayout dataBoard;

    /* Display Data from DB */
    private TextView dayText_mon;
    private TextView dayText_tue;
    private TextView dayText_wed;
    private TextView dayText_thu;
    private TextView dayText_fri;
    private TextView dayText_sat;
    private TextView dayText_sun;
    private TextView locationText;
    private TextView timeText_6to9;
    private TextView timeText_9to12;
    private TextView timeText_12to15;
    private TextView timeText_15to18;
    private TextView timeText_18to21;
    private TextView timeText_21to24;
    private TextView precipitationText_30;
    private TextView precipitationText_50;
    private TextView precipitationText_70;
    private TextView alarmTimeText;

    /* 임시 DB삭제버튼 */
    private Button tempDeleteButton;

    /* Delete & Modify Buttons and flag */
    private Button deleteButton;
    private Button modifyButton;

    /* Alarm components */
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createDB();

        boolean extraFromAlarmReceiver = getIntent().getBooleanExtra("alarmFiring", false);
        Log.e("log", "extraFromAlarmReceiver 값 : " + extraFromAlarmReceiver);
        if (extraFromAlarmReceiver) { //알람리시버에서 값이 들어왔으면
            getRSSdata();
        }

        /* Data display by text */
        dayText_mon = findViewById(R.id.dayText_mon);
        dayText_tue = findViewById(R.id.dayText_tue);
        dayText_wed = findViewById(R.id.dayText_wed);
        dayText_thu = findViewById(R.id.dayText_thu);
        dayText_fri = findViewById(R.id.dayText_fri);
        dayText_sat = findViewById(R.id.dayText_sat);
        dayText_sun = findViewById(R.id.dayText_sun);

        locationText = findViewById(R.id.locationText);
        timeText_6to9 = findViewById(R.id.timeText_6to9);
        timeText_9to12 = findViewById(R.id.timeText_9to12);
        timeText_12to15 = findViewById(R.id.timeText_12to15);
        timeText_15to18 = findViewById(R.id.timeText_15to18);
        timeText_18to21 = findViewById(R.id.timeText_18to21);
        timeText_21to24 = findViewById(R.id.timeText_21to24);
        precipitationText_30 = findViewById(R.id.precipitationText_30);
        precipitationText_50 = findViewById(R.id.precipitationText_50);
        precipitationText_70 = findViewById(R.id.precipitationText_70);
        alarmTimeText = findViewById(R.id.alarmTimeText);

        /*추가 버튼 클릭 로직 구현*/
        addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "우산알라미를 추가합니다", Toast.LENGTH_SHORT).show();
                replaceFragment();
            }
        });

        /* Delete button function */
        deleteButton = findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDialogBuilder(2);
            }
        });

        /* Modify button function */
        modifyButton = findViewById(R.id.modifyButton);
        modifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDialogBuilder(3);
            }
        });

        /* Check if DB has been created */
        dataBoard = findViewById(R.id.dataBoard);
        addAndDeleteLayout = findViewById(R.id.addAndDeleteLayout);
        if (checkIfDBexists()) {
            addAndDeleteHideAndShow(true);
            selectDBAndDisplayDataOnMainActivity();
        } else {
            addAndDeleteHideAndShow(false);
        }
    } // onCreate End

    /*뒤로 가기 버튼 2번 누를 시 종료 & 알람 세팅 프래그먼트 끄기*/
    @Override
    public void onBackPressed() {
        if (fragment_alarmSetting == null || !fragment_alarmSetting.isAdded()) {
            if (lastBackPresseed + 2000 < System.currentTimeMillis()) {
                Toast.makeText(this, "'뒤로' 버튼을 한 번 더 누르면 종료됩니다", Toast.LENGTH_LONG).show();
                lastBackPresseed = System.currentTimeMillis();
            } else {
                finish();
            }
            return;
        }
        if (fragment_alarmSetting.isAdded()) {
            if (checkIfDBexists()) {
                setDialogBuilder(4);
            } else {
                setDialogBuilder(1);
            }
            return;
        }
    }

    public void setDialogBuilder(int inputCase) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        if (inputCase == 1) {
            alertDialogBuilder.setTitle("알람 세팅 취소");
            alertDialogBuilder.setMessage("우산알라미 설정을 취소하시겠습니까?");
            alertDialogBuilder.setCancelable(false);
            alertDialogBuilder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    scrollUptotheTopOfFragmentDisplay();
                    removeFragment();
                }
            });
        }
        if (inputCase == 2) {
            alertDialogBuilder.setTitle("알람 삭제");
            alertDialogBuilder.setMessage("설정된 알람을 삭제하시겠습니까?");
            alertDialogBuilder.setCancelable(false);
            alertDialogBuilder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    deleteDB();
                    recreate();
                }
            });
        }
        if (inputCase == 3) {
            alertDialogBuilder.setTitle("알람 설정 수정");
            alertDialogBuilder.setMessage("우산알라미 설정을 수정하시겠습니까?");
            alertDialogBuilder.setCancelable(false);
            alertDialogBuilder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    replaceFragment();
                }
            });
        }
        if (inputCase == 4) {
            alertDialogBuilder.setTitle("알람 설정 수정");
            alertDialogBuilder.setMessage("우산알라미 수정을 취소하시겠습니까?");
            alertDialogBuilder.setCancelable(false);
            alertDialogBuilder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    removeFragment();
                }
            });
        }
        alertDialogBuilder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialogBuilder.show();
    }

    public boolean checkIfDBexists() {
        Cursor cursor = sqLiteDatabase.rawQuery("select id from " + dbTableName, null);
        boolean result = cursor.moveToFirst();
        return result;
    }

    /* when clicking addButton & modification button on the fragment */
    public void setDialogForSetting() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        if (checkIfDBexists()) {
            alertDialogBuilder.setTitle("우산 알라미 수정");
            alertDialogBuilder.setMessage("우산알라미를 수정하시겠습니까?");
            alertDialogBuilder.setCancelable(false);
            whichButtonClicked = 1;
        } else {
            alertDialogBuilder.setTitle("우산 알라미 설정");
            alertDialogBuilder.setMessage("우산알라미를 설정하시겠습니까?");
            alertDialogBuilder.setCancelable(false);
            whichButtonClicked = 0;
        }
        alertDialogBuilder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                scrollUptotheTopOfFragmentDisplay();
                getCurrentDateAndTime(); //현재 시간 얻기
                dataInsertOrUpdate(whichButtonClicked);
                setCalender();
                removeFragment();
            }
        });
        alertDialogBuilder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialogBuilder.show();
    }

    public void removeFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.remove(fragment_alarmSetting);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        fragmentTransaction.commit();
    }

    public void replaceFragment() {
        fragment_alarmSetting = new Fragment_alarmSetting();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container, fragment_alarmSetting, "fragment");
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        fragmentTransaction.commit();
    }

    /* calling scollUpToTheTop method from fragment */
    public void scrollUptotheTopOfFragmentDisplay() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment_alarmSetting fragment_alarmSetting = (Fragment_alarmSetting) fragmentManager.findFragmentByTag("fragment");
        fragment_alarmSetting.scrollUptotheTop();
    }

    /* calling dataInsertToDB or dataUpdate method from fragment */
    public void dataInsertOrUpdate(int whichButtonClicked) {
        if (whichButtonClicked == 0) { //add
            FragmentManager fragmentManager = getSupportFragmentManager();
            Fragment_alarmSetting fragment_alarmSetting = (Fragment_alarmSetting) fragmentManager.findFragmentByTag("fragment");
            fragment_alarmSetting.DBdataInsertOrUpdate(0);
            addAndDeleteHideAndShow(true);
            selectDBAndDisplayDataOnMainActivity();
        } else if (whichButtonClicked == 1) { //modify
            FragmentManager fragmentManager = getSupportFragmentManager();
            Fragment_alarmSetting fragment_alarmSetting = (Fragment_alarmSetting) fragmentManager.findFragmentByTag("fragment");
            fragment_alarmSetting.DBdataInsertOrUpdate(1);
            selectDBAndDisplayDataOnMainActivity();
        }
    }

    /* Method for getting the current time */
    public void getCurrentDateAndTime() {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.KOREA);
        Date date = new Date();
        currentDate = format.format(date);
    }

    public void setCalender() {
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT setHour, setMinute FROM " + dbTableName, null);
        cursor.moveToNext();
        int setHour = cursor.getInt(0);
        int setMinute = cursor.getInt(1);
        calendar = Calendar.getInstance();
//        calendar.set(Calendar.HOUR_OF_DAY, setHour);
//        calendar.set(Calendar.MINUTE, setMinute);
        /* test */
        int currentTime = (int) System.currentTimeMillis();
        calendar.set(Calendar.SECOND, currentTime + 10);
//        setAlarm();
    }

    /* Set alarm */
    public void setAlarm() {
        /* 알람을 설정하는 메소드 */
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("ID", 1);
        intent.putExtra("time", calendar);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        // setRepeating 하면 반복알람임.
        // https://debugdaldal.tistory.com/124 참고. 설명 잘 되어있음.

        /*
         * 알람 설정을 누르면 calender가 설정되고, setAlarm이 됨..
         * AlarmReceiver에서 getRSSdata가 먼저 작동되고, DB에 있는 설정 강수확률에 따라
         * Notification 함수가 작동되어야 함.*/

    }

    /* Set notification service */
    public void notification(String notificationMessage, String location) {
        //알림 세부 내용 수정 요망
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager.getNotificationChannel("channel_1") == null) {
                notificationManager.createNotificationChannel(new NotificationChannel(
                        "channel_id", "createdChannel", NotificationManager.IMPORTANCE_DEFAULT
                ));
                builder = new NotificationCompat.Builder(this, "channel_1");
            } else {
                builder = new NotificationCompat.Builder(this, "channel_1");
            }
        } else {
            builder = new NotificationCompat.Builder(this);
        }
        builder.setContentTitle("우산알라미 알림");
        builder.setContentText(location + " 강수확률\n" + notificationMessage);
        builder.setSmallIcon(R.drawable.loading_icon); //알림 아이콘
        Notification notification = builder.build();
        notificationManager.notify(1, notification); //알림 실행
    }

    /* get subProv data from DB */
    public String[] getProvAndSubProvFromDB() {
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT prov, subProv FROM " + dbTableName, null);
        cursor.moveToNext();
        String[] provAndSub = new String[2];
        provAndSub[0] = cursor.getString(0);
        provAndSub[1] = cursor.getString(1);
        return provAndSub;
    }

    /* get data from RSS (by using AsyncTask) */
    public void getRSSdata() {
        if (!checkIfDBexists()) { // if there is no DB, this method is not executed
            return;
        }
        String[] provAndSubProvFromDBFromDB = getProvAndSubProvFromDB();
        Long zoneCode = getZoneCode(provAndSubProvFromDBFromDB[1]);
        /*
         * RSS데이터를 Dom 형태로 받아왔다.
         * tag = "pop"이 강수량이고, "pubDate" 로부터 관측시간 기준 "hour"이후의 "pop"을 알 수 있다 ! (알고리즘 짜야함)
         * 따라서, 알람을 설정한 location으로 URL zone을 설정(하드코딩)하고, => 완료
         * 이를 기준으로 알람을 설정한 시간이 되면 url에 request하여 받아온 data를 통해 pop을 얻고,
         * 얻은 pop에 따라 설정한 강수확률과 비교, 해당되면 우산 가져가라는 알람이 울리고(알람 로직 설정 완료),
         * 아니면 맑다는 메시지를 띄운다(2번째 알람).
         *
         * <Tasks>
         * 1. hour / pop 알고리즘 짜기
         * 2. DB에 들어있는 설정된 강수확률(precipitation)과 비교, return 값 내기
         * 3. 설정한 시간이 되면 특정 위 1,2번 메소드가 작동하도록 하기
         * 4. 1,2,번 메소드의 결과값으로 인해 notification 메소드가 동작하도록 하기.
         * 5. notification contents should be more elaborated
         *
         * */
        Document doc = null;
        BackgroundThreadForXML backgroundThreadForXML = new BackgroundThreadForXML();
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
            compareSetPrecipitationDataWithPopMap(entirePopMap, location);
            /* 위 두 함수 완료되면 Notification 함수 실행 */

        } catch (Exception e) {
            Log.e("log", "값 받아오기 실패");
            e.printStackTrace();
        }
    }

    /* 유저가 설정한 강수확률 예측 시간대에 속한 예보의 강수확률(pop)을 반환  */
    public HashMap<Integer, Integer> compareSetTimedataWithWeatherCast(ArrayList<Integer> castedHourList, HashMap<Integer, Integer> popMap) {
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT time1, time2, time3, time4, time5, time6 " +
                "FROM " + dbTableName, null);
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
    public void compareSetPrecipitationDataWithPopMap(HashMap<Integer, Integer> entirePopMap, String location) {
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT precipitation FROM " + dbTableName, null);
        cursor.moveToNext();
//        int setPrecipitation = cursor.getInt(0); -> 설정해놓은 강수확률
        int setPrecipitation = 0; // 테스트(강수확률 0)
        String notificationMessage = "";

        for (int i = 0; i < 6; i++) {
            if (entirePopMap.get(i) >= setPrecipitation) { //실제 예보 강수확률(pop)이 설정한 강수확률 이상일 때
                if (i < 2) {
                    notificationMessage += "오전 " + (6 + i * 3) + "시 - " + (9 + i * 3) + "시의 평균 강수확률 : " + entirePopMap.get(i) + "%\n";
                } else {
                    int time = (i - 2) * 3;
                    if (i == 2) {
                        notificationMessage += "오후 12시 - " + (time + 3) + "시의 평균 강수확률 : " + entirePopMap.get(i) + "%\n";
                    } else {
                        notificationMessage += "오후 " + time + "시 - " + (time + 3) + "시의 평균 강수확률 : " + entirePopMap.get(i) + "%\n";
                    }
                }
            }
        }
        Log.e("log", "notificationMessage : " + notificationMessage);
        notification(notificationMessage, location);
    }


    /* Hardcoding to get zone code */
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


    /* DB create */
    public void createDB() {
        sqLiteDatabase = openOrCreateDatabase(dbTableName, MODE_PRIVATE, null);
        String querie = "create table if not exists " + dbTableName + "( id integer PRIMARY KEY autoincrement, " +
                " mon integer, tue integer, wed integer, thu integer, fri integer, sat integer, sun integer, " +
                "prov string, subProv string, subProvSeq integer, time1 integer, time2 integer, time3 integer, time4 integer," +
                " time5 integer, time6 integer, precipitation integer, alarmPoint integer, setHour integer, setMinute integer)";
        sqLiteDatabase.execSQL(querie);
    }


    public void selectDBAndDisplayDataOnMainActivity() {
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM " + dbTableName, null);
        cursor.moveToNext();

        /* set days */
        String whiteColor = "#ffffff";
        String greyColor = "#606060";
        /* initialize */
        dayText_mon.setTextColor(Color.parseColor(greyColor));
        dayText_tue.setTextColor(Color.parseColor(greyColor));
        dayText_wed.setTextColor(Color.parseColor(greyColor));
        dayText_thu.setTextColor(Color.parseColor(greyColor));
        dayText_fri.setTextColor(Color.parseColor(greyColor));
        dayText_sat.setTextColor(Color.parseColor(greyColor));
        dayText_sun.setTextColor(Color.parseColor(greyColor));
        for (int i = 1; i < 8; i++) {
            if (cursor.getInt(i) == 1) {
                if (i == 1) {
                    dayText_mon.setTextColor(Color.parseColor(whiteColor));
                } else if (i == 2) {
                    dayText_tue.setTextColor(Color.parseColor(whiteColor));
                } else if (i == 3) {
                    dayText_wed.setTextColor(Color.parseColor(whiteColor));
                } else if (i == 4) {
                    dayText_thu.setTextColor(Color.parseColor(whiteColor));
                } else if (i == 5) {
                    dayText_fri.setTextColor(Color.parseColor(whiteColor));
                } else if (i == 6) {
                    dayText_sat.setTextColor(Color.parseColor(whiteColor));
                } else {
                    dayText_sun.setTextColor(Color.parseColor(whiteColor));
                }
            }
        }

        /* set location */
        String getProvince = cursor.getString(8);
        String getSubProvince = cursor.getString(9);
        locationText.setText(getProvince + " " + getSubProvince);

        /* set timeText */
        /* initialize */
        timeText_6to9.setTextColor(Color.parseColor(greyColor));
        timeText_9to12.setTextColor(Color.parseColor(greyColor));
        timeText_12to15.setTextColor(Color.parseColor(greyColor));
        timeText_15to18.setTextColor(Color.parseColor(greyColor));
        timeText_18to21.setTextColor(Color.parseColor(greyColor));
        timeText_21to24.setTextColor(Color.parseColor(greyColor));
        for (int i = 11; i < 17; i++) {
            if (cursor.getInt(i) == 1) {
                if (i == 11) {
                    timeText_6to9.setTextColor(Color.parseColor(whiteColor));
                } else if (i == 12) {
                    timeText_9to12.setTextColor(Color.parseColor(whiteColor));
                } else if (i == 13) {
                    timeText_12to15.setTextColor(Color.parseColor(whiteColor));
                } else if (i == 14) {
                    timeText_15to18.setTextColor(Color.parseColor(whiteColor));
                } else if (i == 15) {
                    timeText_18to21.setTextColor(Color.parseColor(whiteColor));
                } else if (i == 16) {
                    timeText_21to24.setTextColor(Color.parseColor(whiteColor));
                }
            }
        }

        /* set precipitation */
        /* initialize */
        precipitationText_30.setTextColor(Color.parseColor(greyColor));
        precipitationText_50.setTextColor(Color.parseColor(greyColor));
        precipitationText_70.setTextColor(Color.parseColor(greyColor));

        int precipitationFromDB = Integer.parseInt(cursor.getString(17));
        if (precipitationFromDB == 30) {
            precipitationText_30.setTextColor(Color.parseColor(whiteColor));
        } else if (precipitationFromDB == 50) {
            precipitationText_50.setTextColor(Color.parseColor(whiteColor));
        } else {
            precipitationText_70.setTextColor(Color.parseColor(whiteColor));
        }

//        /*alarmPointText*/
//        int setPoint = cursor.getInt(18);
//
//        if (setPoint == 1) {
//            alarmPointText.setText("알람 전날");
//        } else {
//            alarmPointText.setText("알람 당일");
//        }

        /*alarmTimeText*/
        String setHour = "";
        String AMorPM = "";
        int intHour = cursor.getInt(19);
        if (intHour - 13 < 0) {
            AMorPM = "am";
        } else {
            intHour -= 12;
            AMorPM = "pm";
        }
        setHour += intHour + " : ";
        int setMinute = cursor.getInt(20);
        if (setMinute < 10) {
            setHour += "0" + setMinute;
        } else {
            setHour += setMinute;
        }
        alarmTimeText.setText(setHour + " " + AMorPM);
    }

    public void deleteDB() {
        Toast.makeText(this, "DB를 삭제함", Toast.LENGTH_SHORT).show();
        String querie = "drop table " + dbTableName;
        sqLiteDatabase.execSQL(querie);
    }

    public void addAndDeleteHideAndShow(boolean check) {
        /* The buttons on the main hide & show */
        if (check) { // DB에 데이터 있을 때
            addAndDeleteLayout.setVisibility(View.VISIBLE);
            dataBoard.setVisibility(View.VISIBLE);
            addButton.setVisibility(View.GONE);
        } else {
            addAndDeleteLayout.setVisibility(View.GONE);
            dataBoard.setVisibility(View.GONE);
            addButton.setVisibility(View.VISIBLE);
        }
    }

    /*Method for the fragment to get SQLiteDatabase */
    public SQLiteDatabase sqLiteDatabaseGetter() {
        return sqLiteDatabase;
    }
}