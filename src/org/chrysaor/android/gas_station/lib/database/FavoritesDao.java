package org.chrysaor.android.gas_station.lib.database;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.chrysaor.android.gas_station.lib.dto.Stand;
import org.chrysaor.android.gas_station.util.Utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * お気に入りスタンドDao
 * 
 * @author matsuo
 * 
 */
public class FavoritesDao {

    private static final String TABLE_NAME = "favorites";
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
    private static final String COLUMN_UPDATE_DATE = "update_date";
    private static final String COLUMN_CREATE_DATE = "create_date";

    private static final String[] COLUMNS = { COLUMN_ID, COLUMN_SHOP_CD,
            COLUMN_BRAND, COLUMN_SHOP_NAME, COLUMN_LATITUDE, COLUMN_LONGITUDE,
            COLUMN_DISTANCE, COLUMN_ADDRESS, COLUMN_PRICE, COLUMN_DATE,
            COLUMN_PHOTO, COLUMN_RTC, COLUMN_SELF, COLUMN_MEMBER,
            COLUMN_UPDATE_DATE, COLUMN_CREATE_DATE };

    private SQLiteDatabase db;

    public FavoritesDao(SQLiteDatabase db) {
        this.db = db;
    }

    public long insert(Stand info) {
        Utils.logging("insert:" + info.shopCode);

        long currentTimeMillis = System.currentTimeMillis();
        Date date = new Date(currentTimeMillis);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss");

        ContentValues values = new ContentValues();
        values.put(COLUMN_SHOP_CD, info.shopCode);
        values.put(COLUMN_BRAND, info.brand);
        values.put(COLUMN_SHOP_NAME, info.shopName);
        values.put(COLUMN_LATITUDE, info.latitude);
        values.put(COLUMN_LONGITUDE, info.longitude);
        // values.put(COLUMN_DISTANCE, info.Distance);
        values.put(COLUMN_ADDRESS, info.address);
        values.put(COLUMN_PRICE, info.price);
        values.put(COLUMN_DATE, info.date);
        values.put(COLUMN_PHOTO, info.photo);
        values.put(COLUMN_RTC, info.rtc);
        values.put(COLUMN_SELF, info.self);
        values.put(COLUMN_MEMBER, info.member);
        values.put(COLUMN_UPDATE_DATE, simpleDateFormat.format(date));
        values.put(COLUMN_CREATE_DATE, simpleDateFormat.format(date));
        return db.insert(TABLE_NAME, null, values);
    }

    public long update(Stand info) {
        long currentTimeMillis = System.currentTimeMillis();
        Date date = new Date(currentTimeMillis);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss");

        ContentValues values = new ContentValues();
        values.put(COLUMN_SHOP_CD, info.shopCode);
        values.put(COLUMN_BRAND, info.brand);
        values.put(COLUMN_SHOP_NAME, info.shopName);
        values.put(COLUMN_LATITUDE, info.latitude);
        values.put(COLUMN_LONGITUDE, info.longitude);
        // values.put(COLUMN_DISTANCE, info.Distance);
        values.put(COLUMN_ADDRESS, info.address);
        values.put(COLUMN_PRICE, info.price);
        values.put(COLUMN_DATE, info.date);
        values.put(COLUMN_PHOTO, info.photo);
        values.put(COLUMN_RTC, info.rtc);
        values.put(COLUMN_SELF, info.self);
        values.put(COLUMN_MEMBER, info.member);
        values.put(COLUMN_UPDATE_DATE, simpleDateFormat.format(date));

        return db.update(TABLE_NAME, values, "rowid = " + info.rowId, null);
    }

    public int delete(int rowid) {
        return db.delete(TABLE_NAME, "rowid = " + rowid, null);
    }

    public ArrayList<Stand> findAll(String sortColumn) {
        ArrayList<Stand> gsList = new ArrayList<Stand>();

        if (sortColumn.equals("create_date")) {
            sortColumn = COLUMN_CREATE_DATE + " desc";
        } else if (sortColumn.equals("shop_name")) {
            sortColumn = COLUMN_SHOP_NAME + " asc";
        } else {
            sortColumn = COLUMN_ID;
        }

        Cursor cursor = db.query(TABLE_NAME, COLUMNS, null, null, null, null,
                sortColumn);

        while (cursor.moveToNext()) {
            Stand dtoStand = cursor2Object(cursor);
            gsList.add(dtoStand);
        }

        return gsList;
    }

    public ArrayList<Stand> getUpdateList(Integer interval) {
        ArrayList<Stand> gsList = new ArrayList<Stand>();

        String selection = null;

        if (interval > 0) {
            selection = " datetime('now') > datetime(update_date, '+"
                    + interval + " days')";
        }
        Cursor cursor = db.query(TABLE_NAME, COLUMNS, selection, null, null,
                null, null);

        while (cursor.moveToNext()) {
            Stand dtoStand = cursor2Object(cursor);
            gsList.add(dtoStand);
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
        Utils.logging("delete:" + shop_cd);
        return db.delete(TABLE_NAME, "shop_cd = '" + shop_cd + "'", null);
    }

    public int deleteAll() {
        return db.delete(TABLE_NAME, null, null);
    }

    public String[] getShopCdList() {
        ArrayList<Stand> list = findAll("");
        String[] resultList = null;

        if (list.size() > 0) {
            int size = list.size();
            resultList = new String[list.size()];

            for (int i = 0; i < size; i++) {
                resultList[i] = list.get(i).shopCode;
            }

            Arrays.sort(resultList);
        }

        return resultList;
    }

    /**
     * カーソルをオブジェクトに変換するメソッド
     * 
     * @param cursor
     * @return
     */
    private Stand cursor2Object(Cursor cursor) {

        Stand dtoStand = new Stand();
        Utils.logging(cursor.getString(13));

        dtoStand.rowId = Integer.parseInt(cursor.getString(0));
        dtoStand.shopCode = cursor.getString(1);
        dtoStand.brand = cursor.getString(2);
        dtoStand.shopName = cursor.getString(3);
        dtoStand.latitude = Double.parseDouble(cursor.getString(4));
        dtoStand.longitude = Double.parseDouble(cursor.getString(5));
        dtoStand.distance = cursor.getString(6);
        dtoStand.address = cursor.getString(7);
        dtoStand.price = cursor.getString(8);
        dtoStand.date = cursor.getString(9);
        dtoStand.photo = cursor.getString(10);
        dtoStand.rtc = cursor.getString(11);
        dtoStand.self = cursor.getString(12);
        dtoStand.member = (cursor.getString(13).equals("1") ? true : false);
        dtoStand.updateDate = cursor.getString(14);

        return dtoStand;
    }
}
