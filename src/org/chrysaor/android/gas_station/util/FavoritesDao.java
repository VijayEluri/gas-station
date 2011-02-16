package org.chrysaor.android.gas_station.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class FavoritesDao {
    
    private static final String TABLE_NAME       = "favorites";
    private static final String COLUMN_ID        = "rowid";
    private static final String COLUMN_SHOP_CD   = "shop_cd";
    private static final String COLUMN_BRAND     = "brand";
    private static final String COLUMN_SHOP_NAME = "shop_name";
    private static final String COLUMN_LATITUDE  = "latitude";
    private static final String COLUMN_LONGITUDE = "longitude";
    private static final String COLUMN_DISTANCE  = "distance";
    private static final String COLUMN_ADDRESS   = "address";
    private static final String COLUMN_PRICE     = "price";
    private static final String COLUMN_DATE      = "date";
    private static final String COLUMN_PHOTO     = "photo";
    private static final String COLUMN_RTC       = "rtc";
    private static final String COLUMN_SELF      = "self";
    private static final String COLUMN_MEMBER    = "member";
    private static final String COLUMN_UPDATE_DATE = "update_date";
    private static final String COLUMN_CREATE_DATE = "create_date";
    
    private static final String[] COLUMNS = 
         {COLUMN_ID, COLUMN_SHOP_CD, COLUMN_BRAND, COLUMN_SHOP_NAME, COLUMN_LATITUDE, COLUMN_LONGITUDE, 
          COLUMN_DISTANCE, COLUMN_ADDRESS, COLUMN_PRICE, COLUMN_DATE, COLUMN_PHOTO, COLUMN_RTC, COLUMN_SELF,
          COLUMN_MEMBER, COLUMN_UPDATE_DATE, COLUMN_CREATE_DATE};

    private SQLiteDatabase db;
    
    public FavoritesDao(SQLiteDatabase db) {
        this.db = db;
    }
    
    public long insert(GSInfo info) {
    	Utils.logging("insert:" + info.ShopCode);

        long currentTimeMillis = System.currentTimeMillis();
        Date date = new Date(currentTimeMillis);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        ContentValues values = new ContentValues();
        values.put(COLUMN_SHOP_CD,   info.ShopCode);
        values.put(COLUMN_BRAND,     info.Brand);
        values.put(COLUMN_SHOP_NAME, info.ShopName);
        values.put(COLUMN_LATITUDE,  info.Latitude);
        values.put(COLUMN_LONGITUDE, info.Longitude);
//        values.put(COLUMN_DISTANCE,  info.Distance);
        values.put(COLUMN_ADDRESS,   info.Address);
        values.put(COLUMN_PRICE,     info.Price);
        values.put(COLUMN_DATE,      info.Date);
        values.put(COLUMN_PHOTO,     info.Photo);
        values.put(COLUMN_RTC,       info.Rtc);
        values.put(COLUMN_SELF,      info.Self);
        values.put(COLUMN_MEMBER,    info.Member);
        values.put(COLUMN_UPDATE_DATE, simpleDateFormat.format(date));
        values.put(COLUMN_CREATE_DATE, simpleDateFormat.format(date));
        return db.insert(TABLE_NAME, null, values);
    }
    
    
    public long update(GSInfo info) {
        long currentTimeMillis = System.currentTimeMillis();
        Date date = new Date(currentTimeMillis);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        ContentValues values = new ContentValues();
        values.put(COLUMN_SHOP_CD,   info.ShopCode);
        values.put(COLUMN_BRAND,     info.Brand);
        values.put(COLUMN_SHOP_NAME, info.ShopName);
        values.put(COLUMN_LATITUDE,  info.Latitude);
        values.put(COLUMN_LONGITUDE, info.Longitude);
//        values.put(COLUMN_DISTANCE,  info.Distance);
        values.put(COLUMN_ADDRESS,   info.Address);
        values.put(COLUMN_PRICE,     info.Price);
        values.put(COLUMN_DATE,      info.Date);
        values.put(COLUMN_PHOTO,     info.Photo);
        values.put(COLUMN_RTC,       info.Rtc);
        values.put(COLUMN_SELF,      info.Self);
        values.put(COLUMN_MEMBER,    info.Member);
        values.put(COLUMN_UPDATE_DATE, simpleDateFormat.format(date));
        
        return db.update(TABLE_NAME, values, "rowid = " + info.RowId, null);
    }
    
    public int delete(int rowid) {
        return db.delete(TABLE_NAME, "rowid = " + rowid, null);
    }
    
    public ArrayList<GSInfo> findAll(String sortColumn) {
        ArrayList<GSInfo> gsList = new ArrayList<GSInfo>();
        
        if (sortColumn.equals("create_date")) {
            sortColumn = COLUMN_CREATE_DATE + " desc";
        } else if (sortColumn.equals("shop_name")) {
            sortColumn = COLUMN_SHOP_NAME + " asc";
        } else {
            sortColumn = COLUMN_ID;
        }
        
        Cursor cursor = db.query(TABLE_NAME, COLUMNS, null, null, null, null, sortColumn);
        
        while(cursor.moveToNext()) {
            GSInfo gsInfo = new GSInfo();
            gsInfo.RowId     = Integer.parseInt(cursor.getString(0));
            gsInfo.ShopCode  = cursor.getString(1);
            gsInfo.Brand     = cursor.getString(2);
            gsInfo.ShopName  = cursor.getString(3);
            gsInfo.Latitude  = Double.parseDouble(cursor.getString(4));
            gsInfo.Longitude = cursor.getString(5);
            gsInfo.Distance  = cursor.getString(6);
            gsInfo.Address   = cursor.getString(7);
            gsInfo.Price     = cursor.getString(8);
            gsInfo.Date      = cursor.getString(9);
            gsInfo.Photo     = cursor.getString(10);
            gsInfo.Rtc       = cursor.getString(11);
            gsInfo.Self      = cursor.getString(12);
            gsInfo.Member    = (cursor.getString(13).equals("1") ? true: false);
            gsList.add(gsInfo);
        }
        
        return gsList;
    }
    
    public ArrayList<GSInfo> getUpdateList(Integer interval) {
        ArrayList<GSInfo> gsList = new ArrayList<GSInfo>();
        
        String selection = null;
        
        if (interval > 0) {
            selection  = " datetime('now') > datetime(update_date, '+" + interval + " days')";
        }
        Cursor cursor = db.query(TABLE_NAME, COLUMNS, selection, null, null, null, null);
        
        while(cursor.moveToNext()) {
            GSInfo gsInfo = new GSInfo();
            gsInfo.RowId     = Integer.parseInt(cursor.getString(0));
            gsInfo.ShopCode  = cursor.getString(1);
            gsInfo.Brand     = cursor.getString(2);
            gsInfo.ShopName  = cursor.getString(3);
            gsInfo.Latitude  = Double.parseDouble(cursor.getString(4));
            gsInfo.Longitude = cursor.getString(5);
            gsInfo.Distance  = cursor.getString(6);
            gsInfo.Address   = cursor.getString(7);
            gsInfo.Price     = cursor.getString(8);
            gsInfo.Date      = cursor.getString(9);
            gsInfo.Photo     = cursor.getString(10);
            gsInfo.Rtc       = cursor.getString(11);
            gsInfo.Self      = cursor.getString(12);
            gsInfo.Member    = (cursor.getString(13).equals("1") ? true: false);
            gsInfo.UpdateDate = cursor.getString(14);
            gsList.add(gsInfo);
        }
        
        return gsList;
    }

    public GSInfo findByShopCd(String shop_cd) {
        String selection = "shop_cd = '" + shop_cd +"'";
        Cursor cursor = db.query(TABLE_NAME, COLUMNS, selection, null, null, null, null);
        
        while(cursor.moveToNext()) {
            GSInfo gsInfo = new GSInfo();
            Utils.logging(cursor.getString(13));

            gsInfo.RowId     = Integer.parseInt(cursor.getString(0));
            gsInfo.ShopCode  = cursor.getString(1);
            gsInfo.Brand     = cursor.getString(2);
            gsInfo.ShopName  = cursor.getString(3);
            gsInfo.Latitude  = Double.parseDouble(cursor.getString(4));
            gsInfo.Longitude = cursor.getString(5);
            gsInfo.Distance  = cursor.getString(6);
            gsInfo.Address   = cursor.getString(7);
            gsInfo.Price     = cursor.getString(8);
            gsInfo.Date      = cursor.getString(9);
            gsInfo.Photo     = cursor.getString(10);
            gsInfo.Rtc       = cursor.getString(11);
            gsInfo.Self      = cursor.getString(12);
            gsInfo.Member    = (cursor.getString(13).equals("1") ? true: false);
            return gsInfo;
        }
        
        return null;
    }
    
    public int deleteByShopCd(String shop_cd) {
    	Utils.logging("delete:" + shop_cd);
        return db.delete(TABLE_NAME, "shop_cd = '" + shop_cd +"'", null);
    }
    
    public int deleteAll() {
        return db.delete(TABLE_NAME, null, null);
    }
    
    public String[] getShopCdList() {
        ArrayList<GSInfo> list = findAll("");
        String[] resultList = null;
        
        if (list.size() > 0) {
        	int size = list.size();
            resultList = new String[list.size()];

        	for (int i = 0; i < size; i++) {
        		resultList[i] = list.get(i).ShopCode;
            }
        	
        	Arrays.sort(resultList);
        }
        
        return resultList;
    }
}
