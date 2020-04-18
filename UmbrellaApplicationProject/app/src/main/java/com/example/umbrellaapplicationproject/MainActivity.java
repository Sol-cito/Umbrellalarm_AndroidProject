/*

현재 버그 리스트 (메인액티비티)
1. 메인액티비티의 onCreate 에서 createDB() 메소드가 작동한다. 즉,
DB삭제(tmp)버튼을 누르면 table 자체가 drop되어서, 그 다음에 프래그먼트로 간 다음 데이터를 insert하려 해도
테이블 자체가 없으니 에러남   -> 이건 걍 tmp button 때문에 그럼. '삭제'버튼은 이런 현상 없음.
2. Activity finish 하고 다시 intent로 받아오는거 animation이 너무 별로임...

 */


package com.example.umbrellaapplicationproject;

import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    private TextView dayText;
    private TextView locationText;
    private TextView timeText;
    private TextView precipitationText;
    private TextView alarmTimeText;
    private TextView alarmPointText;

    /* 임시 DB삭제버튼 */
    private Button tempDeleteButton;

    /* Delete & Modify Buttons and flag */
    private Button deleteButton;
    private Button modifyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createDB();

        /* test. 이 메소드는 '설정한 알람 시간이 되면' 작동해야 함. */
        getRSSdata();

        /* Data display by text */
        dayText = findViewById(R.id.dayText);
        locationText = findViewById(R.id.locationText);
        timeText = findViewById(R.id.timeText);
        precipitationText = findViewById(R.id.precipitationText);
        alarmTimeText = findViewById(R.id.alarmTimeText);
        alarmPointText = findViewById(R.id.alarmPointText);

        /*추가 버튼 클릭 로직 구현*/
        addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "우산알라미를 추가합니다", Toast.LENGTH_SHORT).show();
                replaceFragment();
            }
        });

        /* 임시 DB삭제 버튼 */
        tempDeleteButton = findViewById(R.id.tempDeleteButton);
        tempDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkIfDBexists()) {
                    deleteDB();
                } else {
                    Toast.makeText(MainActivity.this, "DB전부삭제해서없음", Toast.LENGTH_SHORT).show();
                }
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

    /* Set alarm */
    public void setAlarm() {
        /* 알람을 설정하는 메소드 */
    }

    /* get subProv data from DB */
    public String getSubProvFromDB() {
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT subProv FROM " + dbTableName, null);
        cursor.moveToNext();
        String subProvFromDB = cursor.getString(0);
        return subProvFromDB;
    }

    /* get data from RSS (by using AsyncTask) */
    public void getRSSdata() {
        if (!checkIfDBexists()) { // if there is no DB, this method is not executed
            return;
        }
        String subProvFromDB = getSubProvFromDB();
        Long zoneCode = getZoneCode(subProvFromDB);
        /*
         * RSS데이터를 Dom 형태로 받아왔다.
         * tag = "pop"이 강수량이고, "pubDate" 로부터 관측시간 기준 "hour"이후의 "pop"을 알 수 있다 ! (알고리즘 짜야함)
         * 따라서, 알람을 설정한 location으로 URL zone을 설정(하드코딩)하고, => 완료
         * 이를 기준으로 알람을 설정한 시간이 되면 url에 request하여 받아온 data를 통해 pop을 얻고,
         * 얻은 pop에 따라 설정한 강수확률과 비교, 해당되면 우산 가져가라는 알람이 울리고, 아니면 맑다는 메시지를 띄운다.
         *
         * <Tasks>
         * 1. hour / pop 알고리즘 짜기
         * 2. DB에 들어있는 설정된 강수확률(precipitation)과 비교, return 값 내기
         * 3.
         *
         * */
        Document doc = null;
        BackgroundThreadForXML backgroundThreadForXML = new BackgroundThreadForXML();
        try {
            doc = backgroundThreadForXML.execute(zoneCode).get();
            NodeList nodeList = doc.getElementsByTagName("pop");
            /* test */
            Log.e("log", "지역 : " + doc.getElementsByTagName("category").item(0).getTextContent());
            for (int i = 0; i < nodeList.getLength(); i++) {
                Log.e("log", i + "번째 pop 값 : " + nodeList.item(i).getTextContent());
            }
        } catch (Exception e) {
            Log.e("log", "값 받아오기 실패");
            e.printStackTrace();
        }
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
        String setDays = "설정 요일 : ";
        for (int i = 1; i < 8; i++) {
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
        String getProvince = cursor.getString(8);
        String getSubProvince = cursor.getString(9);
        locationText.setText(getProvince + " " + getSubProvince);

        /* set timeText */
        String setTimeText = "";
        for (int i = 11; i < 17; i++) {
            if (cursor.getInt(i) == 1) {
                if (i == 11) {
                    setTimeText += "6AM - 9AM ";
                } else if (i == 12) {
                    setTimeText += "9AM - 12PM ";
                } else if (i == 13) {
                    setTimeText += "12PM - 3PM";
                } else if (i == 14) {
                    setTimeText += "3PM - 6PM";
                } else if (i == 15) {
                    setTimeText += "6PM - 9PM";
                } else {
                    setTimeText += "9PM - 12AM";
                }
            }
        }
        timeText.setText(setTimeText);

        /* set precipitation */
        precipitationText.setText("설정 강수량 : " + cursor.getString(17) + "%");

        /*alarmPointText*/
        int setPoint = cursor.getInt(18);

        if (setPoint == 1) {
            alarmPointText.setText("알람 전날");
        } else {
            alarmPointText.setText("알람 당일");
        }

        /*alarmTimeText*/
        String setHour = "";
        int intHour = cursor.getInt(19);
        if (intHour - 13 < 0) {
            setHour = "오전 " + intHour + "시";
        } else {
            intHour -= 12;
            setHour = "오후 " + intHour + "시";
        }
        int setMinute = cursor.getInt(20);
        alarmTimeText.setText(setHour + setMinute + "분");
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