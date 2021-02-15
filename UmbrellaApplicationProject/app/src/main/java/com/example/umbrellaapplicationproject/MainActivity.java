/*

현재 버그 리스트 (메인액티비티)
1. 메인액티비티의 onCreate 에서 createDB() 메소드가 작동한다. 즉,
DB삭제(tmp)버튼을 누르면 table 자체가 drop되어서, 그 다음에 프래그먼트로 간 다음 데이터를 insert하려 해도
테이블 자체가 없으니 에러남   -> 이건 걍 tmp button 때문에 그럼. '삭제'버튼은 이런 현상 없음.
2. Activity finish 하고 다시 intent로 받아오는거 animation이 너무 별로임...

 */


package com.example.umbrellaapplicationproject;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private Button addButton;
    private int whichButtonClicked; // 0 = add clicked , 1 = modification clicked
    private Fragment_alarmSetting fragment_alarmSetting;
    private long lastBackPresseed = 0;

    /* DataBase */
    private SQLiteDatabase sqLiteDatabase;
    private final String dbTableName = "alarmData";

    private LinearLayout addAndDeleteLayout;
    private LinearLayout dataBoard;

    /* Description text */
    private TextView description_1;
    private TextView description_2;
    private TextView description_3;
    private TextView description_4;
    private TextView[] descriptionTextArr;

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

    /* Delete & Modify Buttons and flag */
    private Button deleteButton;
    private Button modifyButton;

    /* switch button and transparent layout*/
    private Switch firstAlarmSwitch;

    /* Alarm components */
    private Calendar calendar;

    /* set days to toss over to the AlarmManager by intent */
    private int[] days;

    /* first set alarm time to toss over to the AlarmManager by intent */
    private int firstAlarmTime;

    /* selected View List */
    private ArrayList<TextView> selectedView;

    /* SharedPreference */
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createDB();

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
        alarmTimeText = findViewById(R.id.alarmTimeText);

        description_1 = findViewById(R.id.description_1);
        description_2 = findViewById(R.id.description_2);
        description_3 = findViewById(R.id.description_3);
        description_4 = findViewById(R.id.description_4);
        descriptionTextArr = new TextView[4];
        descriptionTextArr[0] = description_1;
        descriptionTextArr[1] = description_2;
        descriptionTextArr[2] = description_3;
        descriptionTextArr[3] = description_4;

        /* View ArrayList */
        selectedView = new ArrayList<>();

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

        /* Switch button function with shared preference*/
        sharedPreferences = getSharedPreferences("switch", Activity.MODE_PRIVATE);

        firstAlarmSwitch = findViewById(R.id.firstAlarmSwitch);
        firstAlarmSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if (firstAlarmSwitch.isChecked()) {
                    editor.putInt("switch", 1);
                    editor.commit();
                    switchViewColor(1);
                } else {
                    editor.putInt("switch", 0);
                    editor.commit();
                    switchViewColor(0);
                }
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

    /* view color switch - 0 : on // 1 : off*/
    public void switchViewColor(int switchNum) {
        if (switchNum == 0) {
            for (int i = 0; i < descriptionTextArr.length; i++) {
                descriptionTextArr[i].setTextColor(Color.parseColor("#9D8D8D"));
            }
            for (TextView each : selectedView) {
                each.setTextColor(Color.parseColor("#9D8D8D"));
            }
        } else {
            for (int i = 0; i < descriptionTextArr.length; i++) {
                descriptionTextArr[i].setTextColor(Color.parseColor("#5D48ED"));
            }
            for (TextView each : selectedView) {
                each.setTextColor(Color.parseColor("#ffffff"));
            }
        }
    }

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
                    cancelAlarm();
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
                dataInsertOrUpdate(whichButtonClicked);
                setAlarm();
                removeFragment();
                if (whichButtonClicked == 0) {
                    Toast.makeText(MainActivity.this, "알람이 설정되었습니다", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "알람이 수정되었습니다", Toast.LENGTH_SHORT).show();
                }
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

    /* Set alarm */
    public void setAlarm() {
        Log.e("log", "셋 알람");
        Intent intent = new Intent(this, AlarmReceiver.class);
        calendar = Calendar.getInstance();
        setCalender();
        intent.putExtra("days", days);
        intent.putExtra("firstAlarmTime", firstAlarmTime);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT < 19) { // 19 이하는 setReapeating 함수로 오차 없는 알람 설정
            Log.e("log", "알람 함수 : setRepeating()");
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
            // setRepeating INTERVAL : 하루에 한 번씩 울려야 하므로 AlarmManager.INTERVAL_DAY로 설정
        } else if (Build.VERSION.SDK_INT < 23) { // 22 이하는 setExact 함수로 오차 없는 알람 설정
            Log.e("log", "알람 함수 : setExact()");
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } else { // 23부터는 setAlarmClock() 함수로 오차 없는 알람 설정
            Log.e("log", "알람 함수 : setAlarmClock()");
            AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(calendar.getTimeInMillis(), pendingIntent);
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);
        }
    }

    public void setCalender() {
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT sun, mon, tue, wed, thu, fri, sat, setHour, setMinute, " +
                "time1, time2, time3, time4, time5, time6 FROM " + dbTableName, null);
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
    }

    /* cancel Alarm when delete it */
    public void cancelAlarm() {
        Log.e("log", "캔슬 알람");
        Intent intent = new Intent(this, AlarmReceiver.class);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
    }

    /* DB create */
    public void createDB() {
        sqLiteDatabase = openOrCreateDatabase(dbTableName, MODE_PRIVATE, null);
        String querie = "create table if not exists " + dbTableName + "( id integer PRIMARY KEY autoincrement, " +
                " mon integer, tue integer, wed integer, thu integer, fri integer, sat integer, sun integer, " +
                "prov string, subProv string, subProvSeq integer, time1 integer, time2 integer, time3 integer, time4 integer," +
                " time5 integer, time6 integer, setHour integer, setMinute integer)";
        sqLiteDatabase.execSQL(querie);
    }


    public void selectDBAndDisplayDataOnMainActivity() {
        selectedView.clear(); // ArrayList initialize
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
                    selectedView.add(dayText_mon);
                } else if (i == 2) {
                    dayText_tue.setTextColor(Color.parseColor(whiteColor));
                    selectedView.add(dayText_tue);
                } else if (i == 3) {
                    dayText_wed.setTextColor(Color.parseColor(whiteColor));
                    selectedView.add(dayText_wed);
                } else if (i == 4) {
                    dayText_thu.setTextColor(Color.parseColor(whiteColor));
                    selectedView.add(dayText_thu);
                } else if (i == 5) {
                    dayText_fri.setTextColor(Color.parseColor(whiteColor));
                    selectedView.add(dayText_fri);
                } else if (i == 6) {
                    dayText_sat.setTextColor(Color.parseColor(whiteColor));
                    selectedView.add(dayText_sat);
                } else {
                    dayText_sun.setTextColor(Color.parseColor(whiteColor));
                    selectedView.add(dayText_sun);
                }
            }
        }

        /* set location */
        String getProvince = cursor.getString(8);
        String getSubProvince = cursor.getString(9);
        locationText.setText(getProvince + " " + getSubProvince);
        selectedView.add(locationText);

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
                    selectedView.add(timeText_6to9);
                } else if (i == 12) {
                    timeText_9to12.setTextColor(Color.parseColor(whiteColor));
                    selectedView.add(timeText_9to12);
                } else if (i == 13) {
                    timeText_12to15.setTextColor(Color.parseColor(whiteColor));
                    selectedView.add(timeText_12to15);
                } else if (i == 14) {
                    timeText_15to18.setTextColor(Color.parseColor(whiteColor));
                    selectedView.add(timeText_15to18);
                } else if (i == 15) {
                    timeText_18to21.setTextColor(Color.parseColor(whiteColor));
                    selectedView.add(timeText_18to21);
                } else if (i == 16) {
                    timeText_21to24.setTextColor(Color.parseColor(whiteColor));
                    selectedView.add(timeText_21to24);
                }
            }
        }

        /* 2021.02.15 선택한 시간대의 강수확률을 모두 보여주기 위해 주석처리 */
//        /* set precipitation */
//        /* initialize */
//        precipitationText_30.setTextColor(Color.parseColor(greyColor));
//        precipitationText_50.setTextColor(Color.parseColor(greyColor));
//        precipitationText_70.setTextColor(Color.parseColor(greyColor));
//
//        int precipitationFromDB = Integer.parseInt(cursor.getString(17));
//        if (precipitationFromDB == 30) {
//            precipitationText_30.setTextColor(Color.parseColor(whiteColor));
//            selectedView.add(precipitationText_30);
//        } else if (precipitationFromDB == 50) {
//            precipitationText_50.setTextColor(Color.parseColor(whiteColor));
//            selectedView.add(precipitationText_50);
//        } else {
//            precipitationText_70.setTextColor(Color.parseColor(whiteColor));
//            selectedView.add(precipitationText_70);
//        }

        /*alarmTimeText*/
        String setHour = "";
        String AMorPM = "";
        int intHour = cursor.getInt(17);
        if (intHour - 13 < 0) {
            AMorPM = "am";
        } else {
            intHour -= 12;
            AMorPM = "pm";
        }
        setHour += intHour + " : ";
        int setMinute = cursor.getInt(18);
        if (setMinute < 10) {
            setHour += "0" + setMinute;
        } else {
            setHour += setMinute;
        }
        alarmTimeText.setText(setHour + " " + AMorPM);
        selectedView.add(alarmTimeText);

        /* set switch */
        int alarmSwitch = sharedPreferences.getInt("switch", 0);
        if (alarmSwitch == 1) {
            firstAlarmSwitch.setChecked(true);
            switchViewColor(1);
        } else {
            firstAlarmSwitch.setChecked(false);
            switchViewColor(0);
        }
    }

    public void deleteDB() {
        Toast.makeText(this, "알람이 삭제되었습니다", Toast.LENGTH_SHORT).show();
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

    /*Method for the fragment to get SharedPreference */
    public SharedPreferences sharedPreferenceGetter() {
        return sharedPreferences;
    }
}