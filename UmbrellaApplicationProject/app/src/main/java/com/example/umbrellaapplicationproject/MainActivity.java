/*

현재 버그 리스트 (메인액티비티)
1. 메인액티비티의 onCreate 에서 createDB() 메소드가 작동한다. 즉,
DB삭제(tmp)버튼을 누르면 table 자체가 drop되어서, 그 다음에 프래그먼트로 간 다음 데이터를 insert하려 해도
테이블 자체가 없으니 에러남   -> 이건 걍 tmp button 때문에 그럼. '삭제'버튼은 이런 현상 없음.
2. Activity finish 하고 다시 intent로 받아오는거 animation이 너무 별로임...

 */


package com.example.umbrellaapplicationproject;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private Button addButton;
    private int whichButtonClicked; // 0 = add clicked , 1 = modification clicked
    private Fragment_alarmSetting fragment_alarmSetting;
    private long lastBackPresseed = 0;

    /* 강수확률 request 관련 변수*/
    private String currentDate;

    /* DataBase */
    private SQLiteDatabase sqLiteDatabase;
    private final String dbTableName = "alarmData";

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

    /* set days to toss over to the AlarmManager by intent */
    private int[] days;

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
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT sun, mon, tue, wed, thu, fri, sat, setHour, setMinute FROM " + dbTableName, null);
        cursor.moveToNext();
        int setHour = cursor.getInt(7);
        int setMinute = cursor.getInt(8);
        days = new int[7];
        for (int i = 0; i < 7; i++) {
            if (cursor.getInt(i) == 1) {
                days[i] = 1; // value is 1 if checked
            } else {
                days[i] = 0; // otherwise 0
            }
        }
        calendar = Calendar.getInstance();
        /* hour, minute, days setting */
//        calendar.set(Calendar.HOUR_OF_DAY, setHour);
//        calendar.set(Calendar.MINUTE, setMinute);
//        calendar.set(Calendar.SECOND, 0);
        /* test */
        int currentTime = (int) System.currentTimeMillis();
        calendar.set(Calendar.SECOND, currentTime + 5000);
        setAlarm();
    }

    /* Set alarm */
    public void setAlarm() {
        /* 알람을 설정하는 메소드 */
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("days", days);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        // setRepeating INTERVAL : 하루에 한 번씩 울려야 하므로 AlarmManager.INTERVAL_DAY로 설정
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
    /* DB create */
    public void createDB() {
        sqLiteDatabase = openOrCreateDatabase(dbTableName, MODE_PRIVATE, null);
        String querie = "create table if not exists " + dbTableName + "( id integer PRIMARY KEY autoincrement, " +
                " mon integer, tue integer, wed integer, thu integer, fri integer, sat integer, sun integer, " +
                "prov string, subProv string, subProvSeq integer, time1 integer, time2 integer, time3 integer, time4 integer," +
                " time5 integer, time6 integer, precipitation integer, setHour integer, setMinute integer)";
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

        /*alarmTimeText*/
        String setHour = "";
        String AMorPM = "";
        int intHour = cursor.getInt(18);
        if (intHour - 13 < 0) {
            AMorPM = "am";
        } else {
            intHour -= 12;
            AMorPM = "pm";
        }
        setHour += intHour + " : ";
        int setMinute = cursor.getInt(19);
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