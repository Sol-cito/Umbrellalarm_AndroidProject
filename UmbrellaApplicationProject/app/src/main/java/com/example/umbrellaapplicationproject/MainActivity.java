package com.example.umbrellaapplicationproject;

import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private Button addButton;
    private Fragment_alarmSetting fragment_alarmSetting;
    private long lastBackPresseed = 0;
    private AlertDialog alertDialog;

    /* 강수확률 request 관련 변수*/
    private String currentDate;
    private String currentTime;

    /* Korean Weather API service Key */
    private static final String SERVICE_KEY = "c1g26jTnByGW5kb0HXyLjLfpLsO%2FcByKq4WxxOygJ2GBxWCHOVvFPVSbrHJ6LY2uMqkHDT7kkLVAUKyit3ykEg%3D%3D";

    /* DataBase */
    private SQLiteDatabase sqLiteDatabase;
    private String dbTableName = "alarmData";

    private LinearLayout addAndDeleteLayout;
    private LinearLayout dataBoard;

    /* Display Data from DB */
    private TextView dayText;
    private TextView locationText;
    private TextView timeText;
    private TextView precipitationText;
    private TextView alarmTimeText;
    private TextView alarmPointText;

    /* 임시 DB삭제버튼 */
    private Button tempDeleteButton;

    private Button testBranch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createDB();

        /* Data display by text */
        dayText = findViewById(R.id.dayText);
        locationText = findViewById(R.id.locationText);
        timeText = findViewById(R.id.timeText);
        precipitationText = findViewById(R.id.precipitationText);
        alarmTimeText = findViewById(R.id.alarmTimeText);
        alarmPointText = findViewById(R.id.alarmPointText);

        /*알람 세팅 프래그먼트 추가*/
        fragment_alarmSetting = new Fragment_alarmSetting();

        /*추가 버튼 클릭 로직 구현*/
        addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "우산알라미를 추가합니다", Toast.LENGTH_SHORT).show();
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.container, fragment_alarmSetting, "fragment");
                fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                fragmentTransaction.commit();
            }
        });

        /* 임시 DB삭제 버튼 */
        tempDeleteButton = findViewById(R.id.tempDeleteButton);
        tempDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteDB();
            }
        });

        /* Check if DB has been created */
        dataBoard = findViewById(R.id.dataBoard);
        addAndDeleteLayout = findViewById(R.id.addAndDeleteLayout);
        Cursor cursor = sqLiteDatabase.rawQuery("select id from " + dbTableName, null);
        int recordCount = cursor.getCount();
        if (recordCount > 0) {
            addAndDeleteHideAndShow(true);
        } else {
            addAndDeleteHideAndShow(false);
        }
    } // onCreate End

    /*뒤로 가기 버튼 2번 누를 시 종료 & 알람 세팅 프래그먼트 끄기*/
    @Override
    public void onBackPressed() {
        if (fragment_alarmSetting.isAdded()) {// 알람세팅 프래그먼트에서 뒤로가기 누를 때
            setDialogBuilder();
            return;
        }
        if (lastBackPresseed + 2000 < System.currentTimeMillis()) {
            Toast.makeText(this, "'뒤로' 버튼을 한 번 더 누르면 종료됩니다", Toast.LENGTH_LONG).show();
            lastBackPresseed = System.currentTimeMillis();
        } else {
            finish();
        }
    }

    public void setDialogBuilder() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("알람 세팅 취소");
        alertDialogBuilder.setMessage("우산알라미 설정을 취소하시겠습니까?");
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                scrollUptotheTopOfFragmentDisplay();
                cancelAlarmSetting();
                finishFragment();
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

    public void setDialogForSetting() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("우산 알라미 설정");
        alertDialogBuilder.setMessage("우산알라미를 설정하시겠습니까?");
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                scrollUptotheTopOfFragmentDisplay();
                setAlarm(); //volley 호출
                getCurrentDateAndTime(); //현재 시간 얻기
                addAndDeleteHideAndShow(true);
                finishFragment();
                /* 예 누르면 다른 함수 호출 -> 이 함수가 프래그먼트에서 값 가져오도록*/
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

    public void finishFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.remove(fragment_alarmSetting);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        fragmentTransaction.commit();
    }

    /* calling scollUpToTheTop method of the Fragment */
    public void scrollUptotheTopOfFragmentDisplay() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment_alarmSetting fragment_alarmSetting = (Fragment_alarmSetting) fragmentManager.findFragmentByTag("fragment");
        fragment_alarmSetting.scrollUptotheTop();

    }

    /* Method for getting the current time */
    public void getCurrentDateAndTime() {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.KOREA);
        Date date = new Date();
        currentDate = format.format(date);
    }

    public void setAlarm() {
        String base_Date = "";
        String base_time = "";
        String nx = "";
        String ny = "";

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        String url = "http://newsky2.kma.go.kr/service/SecndSrtpdFrcstInfoService2/ForecastSpaceData?"
                + "serviceKey=" + SERVICE_KEY +
                "&base_date=20191221&base_time=1430&nx=55&ny=127&_type=json";
        Log.e("log", url);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("log", "요청성공");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("log", "요청 실패");
                    }
                });
        requestQueue.add(stringRequest);
    }

    public void cancelAlarmSetting() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment_alarmSetting fragment_alarmSetting = (Fragment_alarmSetting) fragmentManager.findFragmentByTag("fragment");
        fragment_alarmSetting.cancelAlarmSetting();
    }

    /* DB create */
    public void createDB() {
        sqLiteDatabase = openOrCreateDatabase(dbTableName, MODE_PRIVATE, null);
        String querie = "create table if not exists " + dbTableName + "( id integer PRIMARY KEY autoincrement, " +
                " mon integer, tue integer, wed integer, thu integer, fri integer, sat integer, sun integer, " +
                "prov text, subProv text, time1 integer, time2 integer, time3 integer, time4 integer," +
                " time5 integer, time6 integer, precipitation integer, alarmPoint integer, setHour integer, setMinute integer)";
        sqLiteDatabase.execSQL(querie);
    }

    public void selectDB() {
        Cursor cursor = sqLiteDatabase.rawQuery("select * from " + dbTableName, null);
        cursor.moveToNext();

        /* set days */
        String setDays = "설정 요일 : ";
        for (int i = 1; i <= 7; i++) {
            if (cursor.getInt(i) == 1) {
                if (i == 1) {
                    setDays += "월 ";
                } else if (i == 2) {
                    setDays += "화 ";
                } else if (i == 3) {
                    setDays += "수 ";
                } else if (i == 4) {
                    setDays += "목 ";
                } else if (i == 5) {
                    setDays += "금 ";
                } else if (i == 6) {
                    setDays += "토 ";
                } else {
                    setDays += "일 ";
                }
            }
        }
        dayText.setText(setDays);

        /* set location */
        String setLocation = cursor.getString(8) + " " + cursor.getString(9);
        locationText.setText(setLocation);

        /* set timeText */
        String setTimeText = "";
        for (int i = 10; i <= 15; i++) {
            if (cursor.getInt(i) == 1) {
                if (i == 10) {
                    setTimeText += "6AM - 9AM ";
                } else if (i == 11) {
                    setTimeText += "9AM - 12PM ";
                } else if (i == 12) {
                    setTimeText += "12PM - 3PM";
                } else if (i == 13) {
                    setTimeText += "3PM - 6PM";
                } else if (i == 14) {
                    setTimeText += "6PM - 9PM";
                } else {
                    setTimeText += "9PM - 12AM";
                }
            }
        }
        timeText.setText(setTimeText);

        /* set precipitation */
        precipitationText.setText("설정 강수량 : " + cursor.getString(16) + "%");

        /*alarmPointText*/
        int setPoint = cursor.getInt(17);
        if (setPoint == 1) {
            alarmPointText.setText("알람 전날");
        } else {
            alarmPointText.setText("알람 당일");
        }

        /*alarmTimeText*/
        String setHour = "";
        int intHour = cursor.getInt(18);
        if (intHour - 13 < 0) {
            setHour = "오전 " + intHour + "시";
        } else {
            intHour -= 12;
            setHour = "오후 " + intHour + "시";
        }
        int setMinute = cursor.getInt(19);
        alarmTimeText.setText(setHour + setMinute + "분");
    }

    public void deleteDB() {
        String querie = "drop table " + dbTableName;
        try {
            sqLiteDatabase.execSQL(querie);
        } catch (SQLiteException e) {
            e.getStackTrace();
            Toast.makeText(this, "테이블 다 삭제됨", Toast.LENGTH_SHORT).show();
        }
    }

    public void addAndDeleteHideAndShow(boolean check) {
        /* The buttons on the main hide & show */
        if (check == true) { // DB에 데이터 있을 때
            addAndDeleteLayout.setVisibility(View.VISIBLE);
            dataBoard.setVisibility(View.VISIBLE);
            addButton.setVisibility(View.GONE);
            showingDataOnDisplay();
        } else {
            addAndDeleteLayout.setVisibility(View.GONE);
            dataBoard.setVisibility(View.GONE);
            addButton.setVisibility(View.VISIBLE);
        }
    }

    public void showingDataOnDisplay() {
        selectDB();
    }

    /*Method for the fragment to get SQLiteDatabase */
    public SQLiteDatabase sqLiteDatabaseGetter() {
        return sqLiteDatabase;
    }
}
