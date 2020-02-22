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

public class MainActivity extends AppCompatActivity implements Fragment_alarmSetting.ThrowData {
    private Button addButton;
    private Fragment_alarmSetting fragment_alarmSetting;
    private long lastBackPresseed = 0;
    private AlertDialog alertDialog;

    /* 강수확률 request 관련 변수*/
    private String currentDate;
    private String currentTime;

    /* 프래그먼트로부터 받은 데이터 */
    private boolean[] dayListFromFragment;
    private String[] locationListFromFragment;
    private boolean[] timeListFromFragment;
    private int precipitationFromFragment;
    private int alarmPointFromFragment;
    private int pickedHourFromFragment;
    private int pickedMinuteFromFragment;

    /* Korean Weather API service Key */
    private static final String SERVICE_KEY = "c1g26jTnByGW5kb0HXyLjLfpLsO%2FcByKq4WxxOygJ2GBxWCHOVvFPVSbrHJ6LY2uMqkHDT7kkLVAUKyit3ykEg%3D%3D";

    /* DataBase */
    private SQLiteDatabase sqLiteDatabase;
    private String dbTableName = "alarmData";

    private LinearLayout addAndDeleteLayout;
    private LinearLayout dataBoard;

    private TextView dayText;
    private TextView locationText;
    private TextView timeText;
    private TextView precipitationText;
    private TextView alarmTimeText;
    private TextView alarmPointText;

    /* 임시 DB삭제버튼 */
    private Button tempDeleteButton;

    private Button thisIsBranch;

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
                getDataFromFragment();
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

    public void getDataFromFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment_alarmSetting fragment_alarmSetting = (Fragment_alarmSetting) fragmentManager.findFragmentByTag("fragment");
        fragment_alarmSetting.throwData();
        dataInsertToDB();
        addAndDeleteHideAndShow(true);
    }

    /* Data receiver method from fragment*/
    @Override
    public void receiveData(boolean[] dayList, String[] locationList, boolean[] timeList, int precipitation,
                            int alarmPoint, int pickedHour, int pickedMinute) {
        dayListFromFragment = dayList;
        locationListFromFragment = locationList;
        timeListFromFragment = timeList;
        precipitationFromFragment = precipitation;
        alarmPointFromFragment = alarmPoint;
        pickedHourFromFragment = pickedHour;
        pickedMinuteFromFragment = pickedMinute;
        finishFragment();
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

    public void dataInsertToDB() {
        /*
        아래 Fragment에서 담아온 놈들을 검증한 후 아래 String value 에 담으면 됨.
        boolean[] dayListFromFragment; -> true(1) or false(0)
        String[] locationListFromFragment; ->String 으로 받아옴
        boolean[] timeListFromFragment; -> true(1) or false(0)
        int precipitationFromFragment; -> 30, 50, 70
        int alarmPointFromFragment; -> 1 : a day ahead , 2 : on the very day
        int pickedHourFromFragment; -> int
        int pickedMinuteFromFragment; -> int
        */

        /* dayListFromFragment insert */
        String dayList = "";
        for (int i = 0; i < dayListFromFragment.length; i++) {
            if (dayListFromFragment[i] == true) {
                dayList += "1, ";
            } else {
                dayList += "0, ";
            }
        }
        /* locationListFromFragment insert */
        String prov = locationListFromFragment[0];
        String subProv = locationListFromFragment[1];

        /* timeListFromFragment insert */
        String timeList = "";
        for (int i = 0; i < timeListFromFragment.length; i++) {
            if (timeListFromFragment[i] == true) {
                timeList += "1, ";
            } else {
                timeList += "0, ";
            }
        }
        String precipitation = "" + precipitationFromFragment;
        String alarmPoint = "" + alarmPointFromFragment;
        String hour = "" + pickedHourFromFragment;
        String minute = "" + pickedMinuteFromFragment;

        String values = dayList + "'" + prov + "', '" + subProv + "', " +
                timeList + precipitation + ", " + alarmPoint + ", " + hour + ", " + minute;
        String querie = "insert into " + dbTableName + "(mon, tue, wed, thu, fri, sat, sun, prov, subProv, " +
                "time1, time2, time3, time4, time5, time6, precipitation, alarmPoint, setHour, setMinute) " +
                " values ( " + values + " )";
        sqLiteDatabase.execSQL(querie);
        selectDB();
    }

    public void selectDB() {
        /* 데이터 조회 후 얘네 넣으면 됨
        dayText
        locationText
        timeText
        precipitationText
        alarmTimeText
        alarmPointText
        */
        Cursor cursor = sqLiteDatabase.rawQuery("select precipitation from " + dbTableName, null);
        cursor.moveToNext();
        precipitationText.setText("설정 강수량 : " + cursor.getString(0) + "%");
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
}
