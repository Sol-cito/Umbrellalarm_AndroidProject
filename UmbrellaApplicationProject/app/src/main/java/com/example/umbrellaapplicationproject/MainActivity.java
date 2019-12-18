package com.example.umbrellaapplicationproject;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private Bundle bundle;

    private Button addButton;
    private Fragment_alarmSetting fragment_alarmSetting;
    private long lastBackPresseed = 0;
    private AlertDialog alertDialog;

    /* 강수확률 request 관련 변수*/
    private String currentDate;
    private String currentTime;

    /* 프래그먼트로부터 받은 데이터 */
    private boolean[] dayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*알람 세팅 프래그먼트 추가*/
        fragment_alarmSetting = new Fragment_alarmSetting();
        bundle = new Bundle();

        /*추가 버튼 클릭 로직 구현*/
        addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "우산알라미를 추가합니다", Toast.LENGTH_SHORT).show();
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.container, fragment_alarmSetting);
                fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                fragmentTransaction.commit();
            }
        });

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
                setAlarm();
                getCurrentDateAndTime(); //현재 시간 얻기
                bundle.putString("dialogClick", "yes");
                fragment_alarmSetting.setArguments(bundle);
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

    /* 현재 시간 구하기 메소드*/
    public void getCurrentDateAndTime() {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.KOREA);
        Date date = new Date();
        currentDate = format.format(date);
    }

    public void setAlarm() {
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        String url = "http://newsky2.kma.go.kr/service/SecndSrtpdFrcstInfoService2/ForecastSpaceData?" +
                "base_date=20191215&base_time=0230&nx=55&ny=127&_type=json";
    }

    /* 프래그먼트로부터 데이터 받는 메소드 */
    public void getDataFromFragment(boolean[] dayListFromFragment) {
        dayList = dayListFromFragment;
        for (boolean each : dayList) {
            Log.e("log", "요일 불 : " + each);
        }
        finishFragment();
    }
}
