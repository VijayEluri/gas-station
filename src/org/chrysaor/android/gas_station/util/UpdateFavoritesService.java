package org.chrysaor.android.gas_station.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.chrysaor.android.gas_station.MainActivity;

import android.R;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class UpdateFavoritesService extends Service {

    public static final String START_ACTION = "start";
    public static final String INTENT_ACTION = "favorites_update";

    private DatabaseHelper dbHelper = null;
    private SQLiteDatabase db = null;
    private FavoritesDao favoritesDao = null;
    private static String mode = "none";
    private ArrayList<GSInfo> list = null;
    private Handler mHandler = new Handler();
    private Intent intent;
    SharedPreferences pref;

    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        this.intent = intent;
        
        final Bundle extras = intent.getExtras();
        
        Thread th = new Thread() {
            public void run() {
                // お気に入りの更新
                final int res = updateFavorite();
                
                mHandler.post(new Runnable() {
                    
                    @Override
                    public void run() {
                        if (extras != null && extras.containsKey("msg") && extras.getBoolean("msg") == true && res > 0) {
                            Toast.makeText(UpdateFavoritesService.this, res + "件のお気に入りを更新しました", Toast.LENGTH_LONG).show();
                        } else if (pref.getBoolean("settings_favorite_notification", true) && res > 0) {
                            Toast.makeText(UpdateFavoritesService.this, res + "件のお気に入りを更新しました", Toast.LENGTH_LONG).show();
                        }
                        
                        Intent service = new Intent(UpdateFavoritesService.this, UpdateFavoritesService.class);
                        stopService(service);
                        
                        if (extras != null && extras.containsKey("redraw") && extras.getBoolean("redraw") == true) {
                            sendBroadcast(new Intent(INTENT_ACTION));
                        }
                    }
                });
            }
        };
        th.start();
    }
    
    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
    
    private int updateFavorite() {

        Utils.logging("start service");
        
        String defInterval = getResources().getString(org.chrysaor.android.gas_station.R.string.settings_favorite_interval_default);
        int interval = Integer.valueOf(pref.getString("settings_favorite_interval", defInterval));
        
        if (intent == null) {
        	return 0;
        }
        
        Bundle extras = intent.getExtras();
        if (extras != null && extras.containsKey("mode") && extras.getString("mode").equals("all")) {
            interval = 0;
        } else {
            
            if (interval == 0) {
                return 0;
            }
        }
        
        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getReadableDatabase();
        favoritesDao = new FavoritesDao(db);
        
        // 期限が切れたお気に入りを取得する。
        list = favoritesDao.getUpdateList(interval);
        db.close();
        
        Utils.logging(String.valueOf(list.size()));
        if (list.size() > 0) {
            try {
                String sids = "";
                for (int i = 0; i < list.size(); i++) {
                    GSInfo item = list.get(i);
                    
                    sids += item.ShopCode;
                    
                    if (i < list.size() - 1) {
                        sids += ",";
                    }
                }

                String mode   = pref.getString("settings_kind", "0");
                Boolean member = pref.getBoolean("settings_member", false);

                XmlParserFromUrl xml = new XmlParserFromUrl();
                String url = "http://api.gogo.gs/ap/gsst/ssShopIdCsv.php?kind=" + mode +
                "&member=" + ((member == true)? 1: 0) + "&sids=" + sids;
                Utils.logging(url);
                byte[] byteArray = Utils.getByteArrayFromURL(url, "GET");
                if (byteArray == null) {
                    return 0;
                }
                String data = new String(byteArray);
                
                db = dbHelper.getWritableDatabase();
                favoritesDao = new FavoritesDao(db);
                
                // トランザクション開始
                db.beginTransaction();
                
                HashMap<String,HashMap<String,String>> res = xml.getShopInfo(data);
                
                for (GSInfo item : list) {
                    
                    if (res.containsKey(item.ShopCode)) {
                        HashMap<String,String> resItem = res.get(item.ShopCode);
//                    if (res.containsKey("date") && res.containsKey("price")) {
                        // 最新の価格を取得する。
                        Date date = new Date(TimeUnit.SECONDS.toMillis(Long.valueOf(resItem.get("date"))));
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
                        Utils.logging(simpleDateFormat.format(date));

                        item.Price = resItem.get("price");
                        item.Date  = simpleDateFormat.format(date);

                    } else {
                        // 最新の価格を取得する。
                        item.Price = "9999";
                        item.Date  = "";
                    }
                    
                    item.Member = member;
                    
//                    db = dbHelper.getWritableDatabase();
//                    favoritesDao = new FavoritesDao(db);
                    favoritesDao.update(item);
                }
                
                db.setTransactionSuccessful();

                return list.size();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                db.endTransaction();
                if (db != null && db.isOpen()) {
                    db.close();
                }
            }
        }
        
        return 0;
    }

}
