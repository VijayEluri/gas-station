package org.chrysaor.android.gas_station.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
	
	private static final String DATABASE_NAME = "gas_station";
	
	private static final int DATABASE_VERSION = 2;
	
	private static final String CREATE_PLACES_TABLE_SQL = "create table stands " +
			"(rowid integer primary key autoincrement, " +
			"shop_cd text not null, " +
			"brand text not null, " +
			"shop_name text not null, " +
			"latitude text not null, " +
			"longitude text not null, " +
			"distance text not null," +
			"address text not null," +
			"price text not null," +
			"date text not null," +
			"photo text not null," +
			"rtc text not null," +
			"self text not null)";
	
	private static final String DROP_PLACES_TABLE_SQL = "drop table if exists stands";
	
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_PLACES_TABLE_SQL);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(DROP_PLACES_TABLE_SQL);
		onCreate(db);
	}

}
