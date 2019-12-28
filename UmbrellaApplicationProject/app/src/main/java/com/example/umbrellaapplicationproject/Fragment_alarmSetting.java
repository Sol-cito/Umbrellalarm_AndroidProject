package com.example.umbrellaapplicationproject;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class Fragment_alarmSetting extends Fragment {
    private LinearLayout frag_mainLayout;
    private ScrollView scrollView;
    private ImageButton frag_backButton;

    /* 요일 버튼 및 boolean */
    private Button button_monday;
    private boolean bol_monday;
    private Button button_tuesday;
    private boolean bol_tuesday;
    private Button button_wednesday;
    private boolean bol_wednesday;
    private Button button_thursday;
    private boolean bol_thursday;
    private Button button_friday;
    private boolean bol_friday;
    private Button button_saturday;
    private boolean bol_saturday;
    private Button button_sunday;
    private boolean bol_sunday;

    /* 시간 버튼 및 boolean*/
    private Button time_sixAMToNine;
    private boolean bool_time_sixAMToNine;
    private Button time_nineAMToTwelve;
    private boolean bool_time_nineAMToTwelve;
    private Button time_twelveToThree;
    private boolean bool_time_twelveToThree;
    private Button time_threeToSix;
    private boolean bool_time_threeToSix;
    private Button time_sixToNine;
    private boolean bool_time_sixToNine;
    private Button time_ninePMToTwelve;
    private boolean bool_time_ninePMToTwelve;

    /* Layout and spinners for selecting location*/
    private Spinner location_province;
    private Spinner location_seoul;
    private Spinner location_kyeunggi;
    private FrameLayout locationBundle;
    private boolean checkLocationSelection;

    private Button alarmAddButton;

    /* Data sets to transfer to MainActivity */
    private boolean[] dayList = new boolean[7];
    private String[] locationList = new String[2];

    /* Interface to deliver setting data to MainActivity */
    public interface ThrowData {
        void receiveData(boolean[] dayList);
        /* receiveData 안에 넣을 데이터 : 지금은 dayList만 넣었지만 setting할 때 모든 데이터 넘겨야 함.*/
    }

    private ThrowData throwData;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        throwData = (ThrowData) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        final ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_alarm_setting, container, false);
        scrollView = rootView.findViewById(R.id.scrollView);

        /* 뒤 액티비티 버튼 클릭 방지*/
        frag_mainLayout = rootView.findViewById(R.id.frag_mainLayout);
        frag_mainLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        /* 좌측 상단 백버튼 */
        frag_backButton = rootView.findViewById(R.id.frag_backButton);
        frag_backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).setDialogBuilder();
            }
        });

        /* 알람 요일 설정 관련 */
        button_monday = rootView.findViewById(R.id.button_monday);
        button_monday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorChangeOnClick(button_monday, bol_monday);
                bol_monday = switchBolean(bol_monday);
            }
        });
//        button_monday.setOnTouchListener();
        button_tuesday = rootView.findViewById(R.id.button_tuesday);
        button_tuesday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorChangeOnClick(button_tuesday, bol_tuesday);
                bol_tuesday = switchBolean(bol_tuesday);
            }
        });
        button_wednesday = rootView.findViewById(R.id.button_wednesday);
        button_wednesday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorChangeOnClick(button_wednesday, bol_wednesday);
                bol_wednesday = switchBolean(bol_wednesday);
            }
        });
        button_thursday = rootView.findViewById(R.id.button_thursday);
        button_thursday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorChangeOnClick(button_thursday, bol_thursday);
                bol_thursday = switchBolean(bol_thursday);
            }
        });
        button_friday = rootView.findViewById(R.id.button_friday);
        button_friday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorChangeOnClick(button_friday, bol_friday);
                bol_friday = switchBolean(bol_friday);
            }
        });
        button_saturday = rootView.findViewById(R.id.button_saturday);
        button_saturday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorChangeOnClick(button_saturday, bol_saturday);
                bol_saturday = switchBolean(bol_saturday);
            }
        });
        button_sunday = rootView.findViewById(R.id.button_sunday);
        button_sunday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorChangeOnClick(button_sunday, bol_sunday);
                bol_sunday = switchBolean(bol_sunday);
            }
        });

        /* 알람 시간 설정 클릭 관련 */
        time_sixAMToNine = rootView.findViewById(R.id.time_sixAMToNine);
        time_sixAMToNine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorChangeOnClick(time_sixAMToNine, bool_time_sixAMToNine);
                bool_time_sixAMToNine = switchBolean(bool_time_sixAMToNine);
            }
        });

        time_nineAMToTwelve = rootView.findViewById(R.id.time_nineAMToTwelve);
        time_nineAMToTwelve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorChangeOnClick(time_nineAMToTwelve, bool_time_nineAMToTwelve);
                bool_time_nineAMToTwelve = switchBolean(bool_time_nineAMToTwelve);
            }
        });

        time_twelveToThree = rootView.findViewById(R.id.time_twelveToThree);
        time_twelveToThree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorChangeOnClick(time_twelveToThree, bool_time_twelveToThree);
                bool_time_twelveToThree = switchBolean(bool_time_twelveToThree);
            }
        });

        time_threeToSix = rootView.findViewById(R.id.time_threeToSix);
        time_threeToSix.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorChangeOnClick(time_threeToSix, bool_time_threeToSix);
                bool_time_threeToSix = switchBolean(bool_time_threeToSix);
            }
        });

        time_sixToNine = rootView.findViewById(R.id.time_sixToNine);
        time_sixToNine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorChangeOnClick(time_sixToNine, bool_time_sixToNine);
                bool_time_sixToNine = switchBolean(bool_time_sixToNine);
            }
        });

        time_ninePMToTwelve = rootView.findViewById(R.id.time_ninePMToTwelve);
        time_ninePMToTwelve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorChangeOnClick(time_ninePMToTwelve, bool_time_ninePMToTwelve);
                bool_time_ninePMToTwelve = switchBolean(bool_time_ninePMToTwelve);
            }
        });

        locationBundle = rootView.findViewById(R.id.locationBundle);
        location_kyeunggi = rootView.findViewById(R.id.location_kyeunggi);
        location_seoul = rootView.findViewById(R.id.location_seoul);

        location_province = rootView.findViewById(R.id.location_province);
        location_province.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                /* 지역선택 : 0 , 서울코드 : 1, 경기 : 2*/
                if (position == 1) { //서울
                    locationBundle.setVisibility(View.VISIBLE);
                    location_seoul.setVisibility(View.VISIBLE);
                    location_kyeunggi.setVisibility(View.INVISIBLE);
                } else if (position == 2) { //경기
                    locationBundle.setVisibility(View.VISIBLE);
                    location_seoul.setVisibility(View.INVISIBLE);
                    location_kyeunggi.setVisibility(View.VISIBLE);
                } else { // 아무선택안함
                    locationBundle.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        location_seoul.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    checkLocationSelection = false;
                    Log.e("log", "서울에서 아무선택 안함");
                } else {
                    checkLocationSelection = true;
                    locationList[0] = "서울";
                    locationList[1] = location_seoul.getSelectedItem().toString();
                    Log.e("log", "서울에서 선택 : "+location_seoul.getSelectedItem().toString());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        location_kyeunggi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    checkLocationSelection = false;
                    Log.e("log", "경기에서 아무선택 안함");
                } else {
                    checkLocationSelection = true;
                    locationList[0] = "경기";
                    locationList[1] = location_kyeunggi.getSelectedItem().toString();
                    Log.e("log", "경기에서 선택 : "+location_kyeunggi.getSelectedItem().toString());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        /* 설정 버튼 클릭*/
        alarmAddButton = rootView.findViewById(R.id.alarmAddButton);
        alarmAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView valid_day = rootView.findViewById(R.id.valid_day);
                /* validation */
                if (bol_monday == false && bol_tuesday == false && bol_wednesday == false &&
                        bol_thursday == false && bol_friday == false && bol_saturday == false && bol_sunday == false) {
                    scrollView.smoothScrollTo(0, 0);
                    valid_day.setVisibility(View.VISIBLE);
                    return;
                } else {
                    valid_day.setVisibility(View.GONE);
                }
                dayList[0] = bol_monday;
                dayList[1] = bol_tuesday;
                dayList[2] = bol_wednesday;
                dayList[3] = bol_thursday;
                dayList[4] = bol_friday;
                dayList[5] = bol_saturday;
                dayList[6] = bol_sunday;
                ((MainActivity) getActivity()).setDialogForSetting();
            }
        });

        return rootView;
    }

    /* 요일, 날짜 버튼 클릭 시 색 변경 메소드*/
    public void colorChangeOnClick(Button intputButton, Boolean dayBoolean) {
        if (dayBoolean == false) {
            intputButton.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.btn_customized_day_selected));
        } else {
            intputButton.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.btn_customized_day_unselected));
        }
    }

    /* 요일, 날짜 boolean 변경 메소드*/
    public boolean switchBolean(boolean target) {
        if (target) {
            return false;
        } else {
            return true;
        }
    }

    /* Method for the MainActivity to get the data set on the fragment*/
    public void throwData() {
        throwData.receiveData(dayList);
    }

    /* Method for scrolling up the the top of the fragment (called by the MainActivity) */
    public void scrollUptotheTop() {
        scrollView.scrollTo(0, 0);
    }
}
