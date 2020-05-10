/*

현재 버그 리스트(프래그먼트)
1. timePicker 안건들면 DB에 들어가는 값이 0시 0분임 -> 현재 API로는 setHour, setMinute 안먹힘

 */


package com.example.umbrellaapplicationproject;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class Fragment_alarmSetting extends Fragment {
    private LinearLayout frag_mainLayout;
    private ScrollView scrollView;
    private ImageButton frag_backButton;

    /* 요일 버튼 및 boolean */
    private Button button_monday;
    private Button button_tuesday;
    private Button button_wednesday;
    private Button button_thursday;
    private Button button_friday;
    private Button button_saturday;
    private Button button_sunday;
    private Button[] dayButtonArr;

    /* 시간 버튼 및 boolean*/
    private Button time_sixAMToNine;
    private Button time_nineAMToTwelve;
    private Button time_twelveToThree;
    private Button time_threeToSix;
    private Button time_sixToNine;
    private Button time_ninePMToTwelve;
    private Button[] timeButtonArr;

    /* precipitation radio */
    private RadioGroup radioGroupOfPrecipitation;

    /* Layout and spinners for selecting location*/
    private Spinner location_province;
    private Spinner location_seoul;
    private Spinner location_kyeunggi;
    private FrameLayout locationBundle;
    private int spinnerPositionSeoul;
    private int spinnerPositionKyeunggi;

    /* Alarm Add & Modification button */
    private Button alarmAddButton;
    private Button alarmModificationButton;

    /* TextView for validation */
    private TextView valid_day;
    private TextView valid_location;
    private TextView valid_time;
    private TextView valid_precipitation;
    private TextView valid_alarmTime;

    /* Data sets to transfer to MainActivity / to save in DB */
    private boolean[] dayList;
    private String[] locationList = new String[2];
    private int subProvSeq;
    private boolean[] timeList;
    private int precipitation;
    private int pickedHour;
    private int pickedMinute;

    /* boolean whether DB exists */
    private boolean DBexist;

    /* timePicker vars */
    private TimePicker timePicker;

    /* DB */
    private String dbTableName = "alarmData";
    private SQLiteDatabase sqLiteDatabase;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_alarm_setting, container, false);
        scrollView = rootView.findViewById(R.id.scrollView);

        /* When attached, verify if there is DB already inserted */
        sqLiteDatabase = ((MainActivity) getActivity()).sqLiteDatabaseGetter();
        Cursor cursor = sqLiteDatabase.rawQuery("select id from " + dbTableName, null);
        DBexist = cursor.moveToFirst(); // when no DB, false will be returned

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
                if (DBexist) {
                    ((MainActivity) getActivity()).setDialogBuilder(4);
                    // if user clicks the back button on the left-top when DB exists, parameter is 4,
                } else {
                    ((MainActivity) getActivity()).setDialogBuilder(1);
                    // if user clicks the back button on the left-top when there is no DB, parameter is 1,
                }
            }
        });

        /* 알람 요일 설정 관련 */
        dayButtonArr = new Button[7];
        /* dayList 초기화 */
        dayList = new boolean[7];
        for (int i = 0; i < 7; i++) {
            dayList[i] = false;
        }
        button_monday = rootView.findViewById(R.id.button_monday);
        dayButtonArr[0] = button_monday;
        button_monday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorChangeOnClick(button_monday, dayList[0]);
                boolean switchBool = switchBoolean(dayList[0]);
                dayList[0] = switchBool;
            }
        });
        button_tuesday = rootView.findViewById(R.id.button_tuesday);
        dayButtonArr[1] = button_tuesday;
        button_tuesday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorChangeOnClick(button_tuesday, dayList[1]);
                boolean switchBool = switchBoolean(dayList[1]);
                dayList[1] = switchBool;
            }
        });
        button_wednesday = rootView.findViewById(R.id.button_wednesday);
        dayButtonArr[2] = button_wednesday;
        button_wednesday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorChangeOnClick(button_wednesday, dayList[2]);
                boolean switchBool = switchBoolean(dayList[2]);
                dayList[2] = switchBool;
            }
        });
        button_thursday = rootView.findViewById(R.id.button_thursday);
        dayButtonArr[3] = button_thursday;
        button_thursday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorChangeOnClick(button_thursday, dayList[3]);
                boolean switchBool = switchBoolean(dayList[3]);
                dayList[3] = switchBool;
            }
        });
        button_friday = rootView.findViewById(R.id.button_friday);
        dayButtonArr[4] = button_friday;
        button_friday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorChangeOnClick(button_friday, dayList[4]);
                boolean switchBool = switchBoolean(dayList[4]);
                dayList[4] = switchBool;
            }
        });
        button_saturday = rootView.findViewById(R.id.button_saturday);
        dayButtonArr[5] = button_saturday;
        button_saturday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorChangeOnClick(button_saturday, dayList[5]);
                boolean switchBool = switchBoolean(dayList[5]);
                dayList[5] = switchBool;
            }
        });
        button_sunday = rootView.findViewById(R.id.button_sunday);
        dayButtonArr[6] = button_sunday;
        button_sunday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorChangeOnClick(button_sunday, dayList[6]);
                boolean switchBool = switchBoolean(dayList[6]);
                dayList[6] = switchBool;
            }
        });

        /* 알람 시간 설정 클릭 관련 */
        timeButtonArr = new Button[6];
        /* timeList 초기화 */
        timeList = new boolean[6];
        for (int i = 0; i < 6; i++) {
            timeList[i] = false;
        }

        time_sixAMToNine = rootView.findViewById(R.id.time_sixAMToNine);
        timeButtonArr[0] = time_sixAMToNine;
        time_sixAMToNine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorChangeOnClick(time_sixAMToNine, timeList[0]);
                boolean switchBool = switchBoolean(timeList[0]);
                timeList[0] = switchBool;
            }
        });

        time_nineAMToTwelve = rootView.findViewById(R.id.time_nineAMToTwelve);
        timeButtonArr[1] = time_nineAMToTwelve;
        time_nineAMToTwelve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorChangeOnClick(time_nineAMToTwelve, timeList[1]);
                boolean switchBool = switchBoolean(timeList[1]);
                timeList[1] = switchBool;
            }
        });

        time_twelveToThree = rootView.findViewById(R.id.time_twelveToThree);
        timeButtonArr[2] = time_twelveToThree;
        time_twelveToThree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorChangeOnClick(time_twelveToThree, timeList[2]);
                boolean switchBool = switchBoolean(timeList[2]);
                timeList[2] = switchBool;
            }
        });

        time_threeToSix = rootView.findViewById(R.id.time_threeToSix);
        timeButtonArr[3] = time_threeToSix;
        time_threeToSix.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorChangeOnClick(time_threeToSix, timeList[3]);
                boolean switchBool = switchBoolean(timeList[3]);
                timeList[3] = switchBool;
            }
        });

        time_sixToNine = rootView.findViewById(R.id.time_sixToNine);
        timeButtonArr[4] = time_sixToNine;
        time_sixToNine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorChangeOnClick(time_sixToNine, timeList[4]);
                boolean switchBool = switchBoolean(timeList[4]);
                timeList[4] = switchBool;
            }
        });

        time_ninePMToTwelve = rootView.findViewById(R.id.time_ninePMToTwelve);
        timeButtonArr[5] = time_ninePMToTwelve;
        time_ninePMToTwelve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorChangeOnClick(time_ninePMToTwelve, timeList[5]);
                boolean switchBool = switchBoolean(timeList[5]);
                timeList[5] = switchBool;
            }
        });

        /* 지역선택 관련 */
        /* 스피너 포지션 초기화 */
        spinnerPositionSeoul = 0;
        spinnerPositionKyeunggi = 0;

        locationBundle = rootView.findViewById(R.id.locationBundle);
        location_kyeunggi = rootView.findViewById(R.id.location_kyeunggi);
        location_seoul = rootView.findViewById(R.id.location_seoul);

        location_province = rootView.findViewById(R.id.location_province);
        location_province.setSelection(0, false);
        location_province.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                /* 지역선택 : 0 , 서울코드 : 1, 경기 : 2*/
                if (position == 1) { //서울
                    locationBundle.setVisibility(View.VISIBLE);
                    location_seoul.setVisibility(View.VISIBLE);
                    location_kyeunggi.setVisibility(View.INVISIBLE);
                    location_kyeunggi.setSelection(0);
                    spinnerPositionKyeunggi = 0;
                } else if (position == 2) { //경기
                    locationBundle.setVisibility(View.VISIBLE);
                    location_seoul.setVisibility(View.INVISIBLE);
                    location_kyeunggi.setVisibility(View.VISIBLE);
                    location_seoul.setSelection(0);
                    spinnerPositionSeoul = 0;
                } else { // 아무선택안함
                    locationBundle.setVisibility(View.INVISIBLE);
                    location_seoul.setSelection(0);
                    location_kyeunggi.setSelection(0);
                    spinnerPositionSeoul = 0;
                    spinnerPositionKyeunggi = 0;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        location_seoul.setSelection(0, false);
        location_seoul.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                locationList[0] = "서울";
                locationList[1] = location_seoul.getSelectedItem().toString();
                spinnerPositionSeoul = position;
                subProvSeq = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        /* When clicking '경기' */
        location_kyeunggi.setSelection(0, false);
        /* 위 selection을 왜 했는지 궁금하면
         * https://stackoverflow.com/questions/2562248/how-to-keep-onitemselected-from-firing-off-on-a-newly-instantiated-spinner?rq=1
         * 참고 */
        location_kyeunggi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                locationList[0] = "경기";
                locationList[1] = location_kyeunggi.getSelectedItem().toString();
                spinnerPositionKyeunggi = position;
                subProvSeq = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        /* Set precipitation */
        radioGroupOfPrecipitation = rootView.findViewById(R.id.radioGroupOfPrecipitation);
        precipitation = 0;
        radioGroupOfPrecipitation.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.precipitationRadioButtonAbove30) {
                    precipitation = 30;
                } else if (checkedId == R.id.precipitationRadioButtonAbove50) {
                    precipitation = 50;
                } else {
                    precipitation = 70;
                }
            }
        });

        valid_day = rootView.findViewById(R.id.valid_day);
        valid_location = rootView.findViewById(R.id.valid_location);
        valid_time = rootView.findViewById(R.id.valid_time);
        valid_precipitation = rootView.findViewById(R.id.valid_precipitation);
        valid_alarmTime = rootView.findViewById(R.id.valid_alarmTime);

        /* 설정 버튼 클릭*/
        alarmAddButton = rootView.findViewById(R.id.alarmAddButton);
        alarmAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* validation */
                if (validation()) {
                    ((MainActivity) getActivity()).setDialogForSetting();
                }
            }
        });

        /* Modification Button click function */
        alarmModificationButton = rootView.findViewById(R.id.alarmModificationButton);
        alarmModificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* validation */
                if (validation()) {
                    ((MainActivity) getActivity()).setDialogForSetting();
                }
            }
        });


        /* if onTimeChanged is not called, default hour(6) and minute(0) are selected */
        pickedHour = 6;
        pickedMinute = 00;
        timePicker = rootView.findViewById(R.id.timePicker);
        if (Build.VERSION.SDK_INT >= 23) {
            timePicker.setHour(6);
            timePicker.setMinute(0);
        } else {
            timePicker.setCurrentHour(6);
            timePicker.setCurrentMinute(0);
        }

        /* otherwise */
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                pickedHour = hourOfDay;
                pickedMinute = minute;
            }
        });

        // if DB exists
        if (DBexist) {
            /* hide add button and show modification button */
            alarmAddButton.setVisibility(View.GONE);
            alarmModificationButton.setVisibility(View.VISIBLE);
            setFragmentDatawhenDBexists(sqLiteDatabase);
        } else {
            alarmAddButton.setVisibility(View.VISIBLE);
            alarmModificationButton.setVisibility(View.GONE);
        }
        return rootView;
    }

    public void setFragmentDatawhenDBexists(SQLiteDatabase sqLiteDatabase) {
        String querie = "SELECT * FROM " + dbTableName;
        Cursor cursor = sqLiteDatabase.rawQuery(querie, null);
        cursor.moveToNext();
        /* set days and switch the color of the buttons */
        for (int i = 1; i < 8; i++) {
            if (cursor.getInt(i) == 1) {
                colorChangeOnClick(dayButtonArr[i - 1], false);
                boolean switchBool = switchBoolean(dayList[i - 1]);
                dayList[i - 1] = switchBool;
            }
        }
        /* set location and display of the spinner */
        String getProvince = cursor.getString(8);
        String getSubProv = cursor.getString(9);
        subProvSeq = cursor.getInt(10);
        locationList[0] = getProvince;
        locationList[1] = getSubProv;

        if (getProvince.charAt(0) == '서') {
            location_province.setSelection(1);
            location_seoul.setSelection(subProvSeq);
            spinnerPositionSeoul = subProvSeq;
        } else {
            location_province.setSelection(2);
            location_kyeunggi.setSelection(subProvSeq);
            spinnerPositionKyeunggi = subProvSeq;
        }

        /* set time that is already set and display it */
        for (int j = 11; j < 17; j++) {
            int getSetTime = cursor.getInt(j);
            if (getSetTime == 1) {
                if (cursor.getInt(j) == 1) {
                    colorChangeOnClick(timeButtonArr[j - 11], false);
                    boolean switchBool = switchBoolean(timeList[j - 11]);
                    timeList[j - 11] = switchBool;
                }
            }
        }
        /* set precipitation and display them */
        int getPrecipitation = cursor.getInt(17);
        if (getPrecipitation == 30) {
            radioGroupOfPrecipitation.check(R.id.precipitationRadioButtonAbove30);
        } else if (getPrecipitation == 50) {
            radioGroupOfPrecipitation.check(R.id.precipitationRadioButtonAbove50);
        } else {
            radioGroupOfPrecipitation.check(R.id.precipitationRadioButtonAbove70);
        }
        precipitation = getPrecipitation;

        /* set alarmtime and display them */
        int getHour = cursor.getInt(18);
        int getMinute = cursor.getInt(19);
        if (Build.VERSION.SDK_INT >= 23) {
            timePicker.setHour(getHour);
            timePicker.setMinute(getMinute);
        } else {
            timePicker.setCurrentHour(getHour);
            timePicker.setCurrentMinute(getMinute);
        }
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
    public boolean switchBoolean(boolean target) {
        if (target) {
            return false;
        } else {
            return true;
        }
    }

    /* Method for scrolling up the the top of the fragment (called by the MainActivity) */
    public void scrollUptotheTop() {
        scrollView.scrollTo(0, 0);
    }

    public boolean validation() {
        /* Checking set day*/
        int dayListCheck = 0;
        for (boolean each : dayList) {
            if (each == false) {
                dayListCheck++;
            }
        }
        if (dayListCheck == 7) {
            scrollView.smoothScrollTo(0, 0);
            valid_day.setVisibility(View.VISIBLE);
        } else {
            valid_day.setVisibility(View.GONE);
        }

        /* checking set location */
        if (spinnerPositionSeoul == 0 && spinnerPositionKyeunggi == 0) {
            valid_location.setVisibility(View.VISIBLE);
            scrollView.smoothScrollTo(0, 0);
        } else {
            valid_location.setVisibility(View.GONE);
        }

        /* checking set time */
        int checkTimeList = 0;
        for (boolean each : timeList) {
            if (each == false) {
                checkTimeList++;
            }
        }
        if (checkTimeList == 6) {
            scrollView.smoothScrollTo(0, 0);
            valid_time.setVisibility(View.VISIBLE);
        } else {
            valid_time.setVisibility(View.GONE);
        }

        /* checking set precipitation */
        if (precipitation == 0) {
            scrollView.smoothScrollTo(0, 600);
            valid_precipitation.setVisibility(View.VISIBLE);
        } else {
            valid_precipitation.setVisibility(View.GONE);
        }
        if (valid_day.getVisibility() == View.VISIBLE || valid_location.getVisibility() == View.VISIBLE
                || valid_time.getVisibility() == View.VISIBLE || valid_precipitation.getVisibility() == View.VISIBLE
                || valid_alarmTime.getVisibility() == View.VISIBLE) {
            return false;
        }
        return true;
    }

    public void DBdataInsertOrUpdate(int flag) {
        /* dayList insert */
        String dayListResult = "";
        int[] dayListResult_int = new int[7];
        for (int i = 0; i < dayList.length; i++) {
            if (dayList[i] == true) {
                dayListResult_int[i] = 1;
                dayListResult += "1, ";
            } else {
                dayListResult_int[i] = 0;
                dayListResult += "0, ";
            }
        }
        /* location insert */
        String prov = locationList[0];
        String subProv = locationList[1];

        /* timeList insert */
        String timeListResult = "";
        int[] timeListResult_int = new int[6];
        for (int i = 0; i < timeList.length; i++) {
            if (timeList[i] == true) {
                timeListResult += "1, ";
                timeListResult_int[i] = 1;
            } else {
                timeListResult += "0, ";
                timeListResult_int[i] = 0;
            }
        }
        sqLiteDatabase = ((MainActivity) getActivity()).sqLiteDatabaseGetter();
        String query = "";
        if (flag == 0) {
            String values = dayListResult + "'" + prov + "', '" + subProv + "', " + subProvSeq + ", " +
                    timeListResult + precipitation + ", " + pickedHour + ", " + pickedMinute;
            query = "INSERT INTO " + dbTableName + "(mon, tue, wed, thu, fri, sat, sun, prov, subProv, subProvSeq, " +
                    "time1, time2, time3, time4, time5, time6, precipitation, setHour, setMinute) " +
                    " values ( " + values + " )";
        } else if (flag == 1) {
            query = "UPDATE " + dbTableName + " SET " +
                    "mon = " + dayListResult_int[0] + ", " +
                    "tue = " + dayListResult_int[1] + ", " +
                    "wed = " + dayListResult_int[2] + ", " +
                    "thu = " + dayListResult_int[3] + ", " +
                    "fri = " + dayListResult_int[4] + ", " +
                    "sat = " + dayListResult_int[5] + ", " +
                    "sun = " + dayListResult_int[6] + ", " +
                    /* 아래 두개는 왜 안될까....*/
                    "prov = '" + locationList[0] + "', " +
                    "subProv = '" + locationList[1] + "', " +
                    "subProvSeq = " + subProvSeq + ", " +
                    "time1 = " + timeListResult_int[0] + ", " +
                    "time2 = " + timeListResult_int[1] + ", " +
                    "time3 = " + timeListResult_int[2] + ", " +
                    "time4 = " + timeListResult_int[3] + ", " +
                    "time5 = " + timeListResult_int[4] + ", " +
                    "time6 = " + timeListResult_int[5] + ", " +
                    "precipitation = " + precipitation + ", " +
                    "setHour = " + pickedHour + ", " +
                    "setMinute = " + pickedMinute;
        }
        sqLiteDatabase.execSQL(query);
    }
}
