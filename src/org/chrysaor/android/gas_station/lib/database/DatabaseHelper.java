package org.chrysaor.android.gas_station.lib.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "gas_station";

    private static final int DATABASE_VERSION = 13;

    private static final String CREATE_PLACES_TABLE_SQL = "create table stands "
            + "(rowid integer primary key autoincrement, "
            + "shop_cd text not null, "
            + "brand text not null, "
            + "shop_name text not null, "
            + "latitude text not null, "
            + "longitude text not null, "
            + "distance integer not null,"
            + "address text not null,"
            + "price text null,"
            + "date text null,"
            + "photo text null,"
            + "rtc text null,"
            + "self text null,"
            + "member text not null)";

    private static final String DROP_PLACES_TABLE_SQL = "drop table if exists stands";

    private static final String CREATE_POST_HISTORIES_TABLE_SQL = "create table if not exists post_histories "
            + "(rowid integer primary key autoincrement, "
            + "ssid text not null, "
            + "kakunin text not null, "
            + "kubun text not null, "
            + "nedan0 integer null, "
            + "nedan1 integer null, "
            + "nedan2 integer null, "
            + "nedan3 integer null, "
            + "regdategap integer not null, "
            + "regdatetime integer not null, "
            + "memo text null,"
            + "gentei integer null," + "date text not null)";

    private static final String DROP_POST_HISTORIES_TABLE_SQL = "drop table if exists post_histories";

    private static final String CREATE_FAVORITES_TABLE_SQL = "create table if not exists favorites "
            + "(rowid integer primary key autoincrement, "
            + "shop_cd text not null, "
            + "brand text not null, "
            + "shop_name text not null, "
            + "latitude text not null, "
            + "longitude text not null, "
            + "distance integer null,"
            + "address text not null,"
            + "price text null,"
            + "date text null,"
            + "photo text null,"
            + "rtc text null,"
            + "self text null,"
            + "member text not null,"
            + "update_date text not null,"
            + "create_date text not null)";

    private static final String DROP_FAVORITES_TABLE_SQL = "drop table if exists favorites";

    private static final String CREATE_STANDS_WIDGET_TABLE_SQL = "create table stands_widget "
            + "(rowid integer primary key autoincrement, "
            + "shop_cd text not null, "
            + "brand text not null, "
            + "shop_name text not null, "
            + "latitude text not null, "
            + "longitude text not null, "
            + "distance text not null,"
            + "address text not null,"
            + "price text not null,"
            + "date text not null,"
            + "photo text not null,"
            + "rtc text not null," + "self text not null)";

    private static final String DROP_STANDS_WIDGET_TABLE_SQL = "drop table if exists stands_widget";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_PLACES_TABLE_SQL);
        db.execSQL(CREATE_STANDS_WIDGET_TABLE_SQL);
        db.execSQL(CREATE_POST_HISTORIES_TABLE_SQL);
        db.execSQL(CREATE_FAVORITES_TABLE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_PLACES_TABLE_SQL);
        db.execSQL(DROP_STANDS_WIDGET_TABLE_SQL);
        // db.execSQL(DROP_POST_HISTORIES_TABLE_SQL);
        // db.execSQL(DROP_FAVORITES_TABLE_SQL);
        onCreate(db);
    }

}
