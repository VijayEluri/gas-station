package org.chrysaor.android.gas_station.lib.database;

import java.util.ArrayList;

import org.chrysaor.android.gas_station.lib.dto.Stand;
import org.chrysaor.android.gas_station.util.Utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * ガソリンスタンドDao
 * 
 * @author matsuo
 * 
 */
public class StandsDao {

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
    private static final String COLUMN_MEMBER = "member";

    private static final String[] COLUMNS = { COLUMN_ID, COLUMN_SHOP_CD,
            COLUMN_BRAND, COLUMN_SHOP_NAME, COLUMN_LATITUDE, COLUMN_LONGITUDE,
            COLUMN_DISTANCE, COLUMN_ADDRESS, COLUMN_PRICE, COLUMN_DATE,
            COLUMN_PHOTO, COLUMN_RTC, COLUMN_SELF, COLUMN_MEMBER };

    private SQLiteDatabase db;

    public StandsDao(SQLiteDatabase db) {
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
        values.put(COLUMN_MEMBER, info.member);
        return db.insert(TABLE_NAME, null, values);
    }

    public int delete(int rowid) {
        return db.delete(TABLE_NAME, "rowid = " + rowid, null);
    }

    public ArrayList<Stand> findAll(String sortColumn) {
        ArrayList<Stand> gsList = new ArrayList<Stand>();

        if (sortColumn.equals("price")) {
            sortColumn = COLUMN_PRICE;
        } else if (sortColumn.equals("dist")) {
            sortColumn = COLUMN_DISTANCE;
        } else if (sortColumn.equals("date")) {
            sortColumn = COLUMN_DATE + " desc";
        } else {
            sortColumn = COLUMN_ID;
        }

        Cursor cursor = db.query(TABLE_NAME, COLUMNS, null, null, null, null,
                sortColumn);

        while (cursor.moveToNext()) {
            Stand gsInfo = cursor2Object(cursor);

            gsList.add(gsInfo);
        }

        return gsList;
    }

    public Stand findByShopCd(String shop_cd) {
        String selection = "shop_cd = '" + shop_cd + "'";
        Cursor cursor = db.query(TABLE_NAME, COLUMNS, selection, null, null,
                null, null);

        while (cursor.moveToNext()) {
            return cursor2Object(cursor);
        }

        return null;
    }

    public int deleteByShopCd(String shop_cd) {
        return db.delete(TABLE_NAME, "shop_cd = '" + shop_cd + "'", null);
    }

    public int deleteAll() {
        return db.delete(TABLE_NAME, null, null);
    }

    /**
     * カーソルをオブジェクトに変換するメソッド
     * 
     * @param cursor
     * @return
     */
    private Stand cursor2Object(Cursor cursor) {

        Stand gasStandDto = new Stand();
        Utils.logging(cursor.getString(13));

        // gasStandDto.RowId = Integer.parseInt(cursor.getString(0));
        gasStandDto.shopCode = cursor.getString(1);
        gasStandDto.brand = cursor.getString(2);
        gasStandDto.shopName = cursor.getString(3);
        gasStandDto.latitude = Double.parseDouble(cursor.getString(4));
        gasStandDto.longitude = Double.parseDouble(cursor.getString(5));
        gasStandDto.distance = cursor.getString(6);
        gasStandDto.address = cursor.getString(7);
        gasStandDto.price = cursor.getString(8);
        gasStandDto.date = cursor.getString(9);
        gasStandDto.photo = cursor.getString(10);
        gasStandDto.rtc = cursor.getString(11);
        gasStandDto.self = cursor.getString(12);
        gasStandDto.member = (cursor.getString(13).equals("1") ? true : false);

        return gasStandDto;
    }
}
