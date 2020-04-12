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
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private Button addButton;
    private int whichButtonClicked; // 0 = add clicked , 1 = modification clicked
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
        if (fragment_alarmSetting.isAdded()) {
            if (checkIfDBexists()) {
                setDialogBuilder(4);
            } else {
                setDialogBuilder(1);
            }
            return;
        }
        if (lastBackPresseed + 2000 < System.currentTimeMillis()) {
            Toast.makeText(this, "'뒤로' 버튼을 한 번 더 누르면 종료됩니다", Toast.LENGTH_LONG).show();
            lastBackPresseed = System.currentTimeMillis();
        } else {
            finish();
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

    /* get data from RSS (by using AsyncTask) */
    public void getRSSdata() {
        /*
        * RSS데이터를 Dom 형태로 받아왔다.
        * tag = "pop"이 강수량이고, "pubDate" 로부터 관측시간 기준 "hour"이후의 "pop"을 알 수 있다 !
        * 따라서, 알람을 설정한 location으로 URL zone을 설정(하드코딩)하고,
        * 이를 기준으로 알람을 설정한 시간이 되면 url에 request하여 받아온 data를 통해 pop을 얻고,
        * 얻은 pop에 따라 설정한 강수확률과 비교, 해당되면 우산 가져가라는 알람이 울리고, 아니면 맑다는 메시지를 띄운다.*/

        Document doc = null;
        BackgroundThreadForXML backgroundThreadForXML = new BackgroundThreadForXML();
        try {
            doc = backgroundThreadForXML.execute(0, 0, 0).get();
            NodeList nodeList = doc.getElementsByTagName("wdKor");
            for(int i = 0; i < nodeList.getLength(); i++){
                Log.e("log", i+"번째 값 : "+nodeList.item(i).getTextContent());
            }
        } catch (Exception e) {
            Log.e("log", "값 받아오기 실패");
            e.printStackTrace();
        }
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