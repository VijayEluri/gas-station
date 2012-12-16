package org.chrysaor.android.gas_station.lib.database;

import java.util.ArrayList;
import java.util.List;

import org.chrysaor.android.gas_station.lib.dto.Stand;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class StandsWidgetDao {

    private static final String TABLE_NAME = "stands";
    private static final String COLUMN_ID = "rowid";
    private static final String COLUMN_SHOP_CD = "shop_cd";
    private static final String COLUMN_BRAND = "brand";
    private static final String COLUMN_SHOP_NAME = "shop_name";
    private static final String COLUMN_LATITUDE = "latitude";
    private static final String COLUMN_LONGITUDE = "longitude";
    private static final String COLUMN_DISTANCE = "distance";
    private static final String COLUMN_ADDRESS = "address";
    private static final String COLUMN_PRICE = "price";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_PHOTO = "photo";
    private static final String COLUMN_RTC = "rtc";
    private static final String COLUMN_SELF = "self";

    private static final String[] COLUMNS = { COLUMN_ID, COLUMN_SHOP_CD,
            COLUMN_BRAND, COLUMN_SHOP_NAME, COLUMN_LATITUDE, COLUMN_LONGITUDE,
            COLUMN_DISTANCE, COLUMN_ADDRESS, COLUMN_PRICE, COLUMN_DATE,
            COLUMN_PHOTO, COLUMN_RTC, COLUMN_SELF };

    private SQLiteDatabase db;

    public StandsWidgetDao(SQLiteDatabase db) {
        this.db = db;
    }

    public long insert(Stand info) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_SHOP_CD, info.shopCode);
        values.put(COLUMN_BRAND, info.brand);
        values.put(COLUMN_SHOP_NAME, info.shopName);
        values.put(COLUMN_LATITUDE, info.latitude);
        values.put(COLUMN_LONGITUDE, info.longitude);
        values.put(COLUMN_DISTANCE, info.distance);
        values.put(COLUMN_ADDRESS, info.address);
        values.put(COLUMN_PRICE, info.price);
        values.put(COLUMN_DATE, info.date);
        values.put(COLUMN_PHOTO, info.photo);
        values.put(COLUMN_RTC, info.rtc);
        values.put(COLUMN_SELF, info.self);
        return db.insert(TABLE_NAME, null, values);
    }

    public int delete(int rowid) {
        return db.delete(TABLE_NAME, "rowid = " + rowid, null);
    }

    public int deleteAll() {
        return db.delete(TABLE_NAME, null, null);
    }

    public ArrayList<Stand> findAll() {
        ArrayList<Stand> gsList = new ArrayList<Stand>();

        Cursor cursor = db.query(TABLE_NAME, COLUMNS, null, null, null, null,
                COLUMN_ID);

        while (cursor.moveToNext()) {
            Stand gsInfo = new Stand();

            // gsInfo.RowId = Integer.parseInt(cursor.getString(0));
            gsInfo.shopCode = cursor.getString(1);
            gsInfo.brand = cursor.getString(2);
            gsInfo.shopName = cursor.getString(3);
            gsInfo.latitude = Double.parseDouble(cursor.getString(4));
            gsInfo.longitude = Double.parseDouble(cursor.getString(5));
            gsInfo.distance = cursor.getString(6);
            gsInfo.address = cursor.getString(7);
            gsInfo.price = cursor.getString(8);
            gsInfo.date = cursor.getString(9);
            gsInfo.photo = cursor.getString(10);
            gsInfo.rtc = cursor.getString(11);
            gsInfo.self = cursor.getString(12);
            gsList.add(gsInfo);
        }

        return gsList;
    }

    public Stand findByShopCd(String shop_cd) {
        String selection = "shop_cd = " + shop_cd;
        Cursor cursor = db.query(TABLE_NAME, COLUMNS, selection, null, null,
                null, null);

        while (cursor.moveToNext()) {
            Stand gsInfo = new Stand();

            // gsInfo.RowId = Integer.parseInt(cursor.getString(0));
            gsInfo.shopCode = cursor.getString(1);
            gsInfo.brand = cursor.getString(2);
            gsInfo.shopName = cursor.getString(3);
            gsInfo.latitude = Double.parseDouble(cursor.getString(4));
            gsInfo.longitude = Double.parseDouble(cursor.getString(5));
            gsInfo.distance = cursor.getString(6);
            gsInfo.address = cursor.getString(7);
            gsInfo.price = cursor.getString(8);
            gsInfo.date = cursor.getString(9);
            gsInfo.photo = cursor.getString(10);
            gsInfo.rtc = cursor.getString(11);
            gsInfo.self = cursor.getString(12);
            return gsInfo;
        }

        return null;
    }

    public int deleteByShopCd(String shop_cd) {
        return db.delete(TABLE_NAME, "shop_cd = " + shop_cd, null);
    }

}
