package org.chrysaor.android.gas_station.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class PostHistoriesDao {
    
    private static final String TABLE_NAME          = "post_histories";
    private static final String COLUMN_ID           = "rowid";
    private static final String COLUMN_SHOP_ID      = "ssid";
    private static final String COLUMN_KAKUNIN      = "kakunin";
    private static final String COLUMN_KUBUN        = "kubun";
    private static final String COLUMN_NEDAN0       = "nedan0";
    private static final String COLUMN_NEDAN1       = "nedan1";
    private static final String COLUMN_NEDAN2       = "nedan2";
    private static final String COLUMN_NEDAN3       = "nedan3";
    private static final String COLUMN_REGDATEGAP   = "regdategap";
    private static final String COLUMN_REGDATETIME  = "regdatetime";
    private static final String COLUMN_MEMO         = "memo";
    private static final String COLUMN_GENTEI       = "gentei";
    private static final String COLUMN_DATE         = "date";
    
    private static final String[] COLUMNS = 
         {COLUMN_ID, COLUMN_SHOP_ID, COLUMN_KAKUNIN, COLUMN_KUBUN, COLUMN_NEDAN0, COLUMN_NEDAN1, 
        COLUMN_NEDAN2, COLUMN_NEDAN3, COLUMN_NEDAN3, COLUMN_REGDATEGAP, COLUMN_REGDATETIME, 
        COLUMN_MEMO, COLUMN_GENTEI, COLUMN_DATE};

    private SQLiteDatabase db;
    
    public PostHistoriesDao(SQLiteDatabase db) {
        this.db = db;
    }
    
    public long insert(PostItem item) {
        long currentTimeMillis = System.currentTimeMillis();
        Date date = new Date(currentTimeMillis);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-DD HH:mm:ss");

        item.date = simpleDateFormat.format(date);

        ContentValues values = new ContentValues();
        values.put(COLUMN_SHOP_ID,     item.ssid);
        values.put(COLUMN_KAKUNIN,     item.kakunin);
        values.put(COLUMN_KUBUN,       item.kubun);
        values.put(COLUMN_NEDAN0,      item.nedan0);
        values.put(COLUMN_NEDAN1,      item.nedan1);
        values.put(COLUMN_NEDAN2,      item.nedan2);
        values.put(COLUMN_NEDAN3,      item.nedan3);
        values.put(COLUMN_REGDATEGAP,  item.regdategap);
        values.put(COLUMN_REGDATETIME, item.regdatetime);
        values.put(COLUMN_MEMO,        item.memo);
        values.put(COLUMN_GENTEI,      item.gentei);
        values.put(COLUMN_DATE,        item.date);
        return db.insert(TABLE_NAME, null, values);
    }
    
    public int delete(int rowid) {
        return db.delete(TABLE_NAME, "rowid = " + rowid, null);
    }
    
    public PostItem findLastOneByShopId(String ssid) {
    	Utils.logging(ssid);
        String selection = "ssid = '" + ssid +"'";
        Cursor cursor = db.query(TABLE_NAME, COLUMNS, selection, null, null, null, "date desc", "1");
        
        while(cursor.moveToNext()) {
            PostItem item = new PostItem();

            item.ssid        = cursor.getString(1);
            item.kakunin     = cursor.getString(2);
            item.kubun       = cursor.getString(3);
            item.nedan0      = cursor.getInt(4);
            item.nedan1      = cursor.getInt(5);
            item.nedan2      = cursor.getInt(6);
            item.nedan3      = cursor.getInt(7);
            item.regdategap  = cursor.getInt(8);
            item.regdatetime = cursor.getInt(9);
            item.memo        = cursor.getString(10);
            item.gentei      = cursor.getInt(11);
            item.date        = cursor.getString(12);
            
            return item;
        }
        
        return null;
    }
    
    public int deleteByShopCd(Integer ssid) {
        return db.delete(TABLE_NAME, "ssid = '" + ssid.toString() +"'", null);
    }
    
    public int deleteAll() {
        return db.delete(TABLE_NAME, null, null);
    }
}
