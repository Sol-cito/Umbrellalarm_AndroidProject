package com.example.umbrellaapplicationproject;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteDatabaseHelper extends SQLiteOpenHelper {

    private final static String DBNAME = "alarmData";
    private final static int VERSION = 2;

    public SQLiteDatabaseHelper(Context context) {
        super(context, "alarmData", null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String querie = "create table if not exists " + DBNAME + "( id integer PRIMARY KEY autoincrement, " +
                " mon integer, tue integer, wed integer, thu integer, fri integer, sat integer, sun integer, " +
                "prov string, subProv string, subProvSeq integer, time1 integer, time2 integer, time3 integer, time4 integer," +
                " time5 integer, time6 integer, precipitation integer, setHour integer, setMinute integer)";
        db.execSQL(querie);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
