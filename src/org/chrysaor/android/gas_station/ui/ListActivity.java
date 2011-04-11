package org.chrysaor.android.gas_station.ui;

import java.util.ArrayList;
import java.util.Arrays;

import jp.co.nobot.libYieldMaker.libYieldMaker;
import net.londatiga.android.ActionItem;
import net.londatiga.android.QuickAction;

import org.chrysaor.android.gas_station.R;
import org.chrysaor.android.gas_station.util.DatabaseHelper;
import org.chrysaor.android.gas_station.util.FavoritesDao;
import org.chrysaor.android.gas_station.util.GSInfo;
import org.chrysaor.android.gas_station.util.StandAdapter;
import org.chrysaor.android.gas_station.util.StandsDao;
import org.chrysaor.android.gas_station.util.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class ListActivity extends Activity {
    private ArrayList<GSInfo> list = null;
    private StandAdapter adapter = null;
    private DatabaseHelper dbHelper = null;
    private SQLiteDatabase db = null;
    private StandsDao standsDao = null;
    private static String mode = "none";
    private SharedPreferences pref = null;
    GoogleAnalyticsTracker tracker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);
        
        tracker = GoogleAnalyticsTracker.getInstance();
        
        // Start the tracker in manual dispatch mode...
        tracker.start("UA-20090562-2", 20, this);
        tracker.trackPageView("/ListActivity");

        // 初期化
        pref = PreferenceManager.getDefaultSharedPreferences(ListActivity.this);
        mode = pref.getString("settings_sort", getResources().getString(R.string.settings_sort_default));
        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getWritableDatabase();
        Utils.logging(mode);
        standsDao = new StandsDao(db);
        list = standsDao.findAll(mode);
        db.close();
        init();

        Spinner spinSort = (Spinner) findViewById(R.id.spin_sort);
        
        if (mode.equals("distance")) {
            spinSort.setSelection(0);
        } else if (mode.equals("price")) {
            spinSort.setSelection(1);
        }else  if (mode.equals("date")) {
            spinSort.setSelection(2);
        }
        spinSort.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                    int position, long id) {
                
                switch (position) {
                case 0:
                    mode = "dist";
                    break;
                case 1:
                    mode = "price";
                    break;
                case 2:
                    mode = "date";
                    break;
                }
                
                db = dbHelper.getWritableDatabase();
                standsDao = new StandsDao(db);
                list = standsDao.findAll(mode);
                db.close();
                init();
                
                // イベントトラック（並び順）
                tracker.trackEvent(
                    "List",       // Category
                    "Sort",       // Action
                    mode,         // Label
                    0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {}
        });
        /*
        RadioButton priceButton = (RadioButton) findViewById(R.id.sort_price);
        
        priceButton.setOnClickListener(new OnClickListener() {
 
            @Override
            public void onClick(View v) {
                db = dbHelper.getWritableDatabase();
                standsDao = new StandsDao(db);
                mode = "price";
                list = standsDao.findAll(mode);
                db.close();
                init();
                
                // イベントトラック（並び順）
                tracker.trackEvent(
                    "List",       // Category
                    "Sort",       // Action
                    "price",      // Label
                    0);
            }
        });

        RadioButton distanceButton = (RadioButton) findViewById(R.id.sort_distance);
        
        distanceButton.setOnClickListener(new OnClickListener() {
 
            @Override
            public void onClick(View v) {
                db = dbHelper.getWritableDatabase();
                standsDao = new StandsDao(db);
                mode = "dist";
                list = standsDao.findAll(mode);
                db.close();
                init();
                
                // イベントトラック（並び順）
                tracker.trackEvent(
                    "List",       // Category
                    "Sort",       // Action
                    "dist",       // Label
                    0);

            }
        });
        
        if (mode.equals("price")) {
            priceButton.setChecked(true);
        } else {
            distanceButton.setChecked(true);
        }
        */

        if (Utils.isDonate(this)) {
            LinearLayout head = (LinearLayout) findViewById(R.id.header_ad);
            head.setVisibility(View.GONE);
        } else {
    //        LinearLayout head = (LinearLayout) findViewById(R.id.header_ad);
            libYieldMaker mv = (libYieldMaker)findViewById(R.id.admakerview);
            mv.setActivity(this);
            mv.setUrl("http://images.ad-maker.info/apps/x0umfpssg2zu.html");
            // IS03の場合、高さを100pxに変更
            if (Build.MODEL.equals("IS03")) {
                mv.setMinimumHeight(100);
                mv.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        100));
            }

            mv.startView();
        }
        
        Button backButton = (Button) findViewById(R.id.btn_back);
        backButton.setOnClickListener(new OnClickListener() {
 
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    
    private void init() {
        if (list.size() > 0) {
            ListView savedList = (ListView) findViewById(R.id.savedList);
            adapter = new StandAdapter(this, R.layout.list, list);
            savedList.setAdapter(adapter);  
        
            savedList.setOnItemClickListener(new OnItemClickListener() {
     
                @Override
                public void onItemClick(AdapterView<?> adapter,
                        View view, int position, long id) {
                    final GSInfo item = list.get(position);
                    
                    // イベントトラック（GSタップ）
                    tracker.trackEvent(
                        "List",         // Category
                        "Stand",        // Action
                        item.ShopCode,  // Label
                        0);

                    ActionItem item1 = new ActionItem();
                    item1.setTitle("地図");
                    item1.setIcon(getResources().getDrawable(R.drawable.map_blue));
                    item1.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            
                            // イベントトラック（地図）
                            tracker.trackEvent(
                                "List",         // Category
                                "Map",          // Action
                                item.ShopCode,  // Label
                                0);
                            
                            Intent intent =new Intent();
                            intent.putExtra("lat", item.Latitude);
                            intent.putExtra("lon", item.Longitude);
                            setResult(Activity.RESULT_OK,intent);
                            //アクティビティの終了
                            finish();
                        }
                    });
                    
                    ActionItem item2 = new ActionItem();
                    item2.setTitle("詳細");
                    item2.setIcon(getResources().getDrawable(R.drawable.info));
                    item2.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            
                            // イベントトラック（詳細）
                            tracker.trackEvent(
                                "List",         // Category
                                "Detail",          // Action
                                item.ShopCode,  // Label
                                0);
                            
                            Intent intent1 = new Intent(ListActivity.this, DetailActivity.class);
                            intent1.putExtra("shopcode", item.ShopCode);
                            startActivity(intent1);  
                        }
                    });
                    
                    ActionItem item3 = new ActionItem();
                    item3.setTitle("ルート検索");
                    item3.setIcon(getResources().getDrawable(R.drawable.green_flag));
                    item3.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            
                            // イベントトラック（ルート検索）
                            tracker.trackEvent(
                                "List",         // Category
                                "RouteSearch",  // Action
                                item.ShopCode,  // Label
                                0);
                            
                            Intent intent = new Intent(); 
                            intent.setAction(Intent.ACTION_VIEW); 
                            intent.setClassName("com.google.android.apps.maps","com.google.android.maps.MapsActivity");
                            intent.setData(Uri.parse("http://maps.google.com/maps?myl=saddr&daddr=" + item.Address + "&dirflg=d")); 
                            startActivity(intent);
                        }
                    });
                    
                    ActionItem item4 = new ActionItem();
                    item4.setTitle("価格投稿");
                    item4.setIcon(getResources().getDrawable(R.drawable.yen_currency_sign));
                    item4.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            
                            // イベントトラック（価格投稿）
                            tracker.trackEvent(
                                "List",         // Category
                                "Post",         // Action
                                item.ShopCode,  // Label
                                0);
                            
                            Intent intent = new Intent(ListActivity.this, PostActivity.class);
                            intent.putExtra("shopcode", item.ShopCode);
                            startActivity(intent);
                        }
                    });
                    ActionItem item5 = new ActionItem();
                    item5.setTitle("給油記録");
                    item5.setIcon(getResources().getDrawable(R.drawable.pencil));
                    item5.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            
                            // イベントトラック（給油記録）
                            tracker.trackEvent(
                                "Detail",      // Category
                                "Charge",      // Action
                                item.ShopCode, // Label
                                0);
                            
                            if (Utils.installedGasLogFree(ListActivity.this) || Utils.installedGasLogPayment(ListActivity.this)) {
                                Intent intent = new Intent();
                                if (Utils.installedGasLogPayment(ListActivity.this)) {
                                    intent.setClassName("jp.pinetail.android.gas_log.payment", "jp.pinetail.android.gas_log.core.FuelPostActivity");
                                } else {
                                    intent.setClassName("jp.pinetail.android.gas_log.free", "jp.pinetail.android.gas_log.core.FuelPostActivity");
                                }
                                intent.putExtra("ssid",       item.ShopCode);
                                intent.putExtra("shop_name",  item.ShopName);
                                intent.putExtra("shop_brand", item.Brand);
                                intent.putExtra("lat",        item.Latitude.toString());
                                intent.putExtra("lon",        item.Longitude);
                                startActivity(intent);
                            } else {
                                new AlertDialog.Builder(ListActivity.this)
                                .setTitle("ガスログ！インストール")
                                .setMessage("給油記録にはガスログ！が必要です。インストールしますか？")
                                .setNeutralButton("インストール", new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(); 
                                        intent.setAction(Intent.ACTION_VIEW); 
                                        intent.setData(Uri.parse("market://details?id=jp.pinetail.android.gas_log.free")); 
                                        startActivity(intent);
                                    }
                                })
                                .setNegativeButton("閉じる", new DialogInterface.OnClickListener() {
                                    
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        
                                    }
                                })
                                .create()
                                .show();

                            }
                        }
                    });

                    QuickAction qa = new QuickAction(view);
                    //onCreate()の中で作ったActionItemをセットする
                    qa.addActionItem(item1);
                    qa.addActionItem(item2);
                    if (Utils.isDonate(ListActivity.this)) {
                        qa.addActionItem(item3);
                    }
                    qa.addActionItem(item4);
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.DONUT) {
                        qa.addActionItem(item5);
                    }
                    //アニメーションを設定する
                    qa.setAnimStyle(QuickAction.ANIM_AUTO);
                    qa.show();
                    /*
                    AlertDialog dialog = new AlertDialog.Builder(ListActivity.this)
                    .setItems(items, new DialogInterface.OnClickListener() {
                        
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                            case 0:
                                Intent intent =new Intent();
                                intent.putExtra("lat", item.Latitude);
                                intent.putExtra("lon", item.Longitude);
                                setResult(Activity.RESULT_OK,intent);
                                //アクティビティの終了
                                finish();
                                break;
                            case 1:
                                Intent intent1 = new Intent(ListActivity.this, DetailActivity.class);
                                intent1.putExtra("shopcode", item.ShopCode);
                                startActivity(intent1);  
                                break;
                            case 2:
                                try {
                                    String msg = pref.getString("settings_sharemsg", getResources().getString(R.string.settings_sharemsg_default));
                                    msg = msg.replaceAll("#price", item.Price).replaceAll("#shop_name", item.ShopName);

                                    Intent intent2 = new Intent();
                                    intent2.setAction(Intent.ACTION_SEND);
                                    intent2.setType("text/plain");
                                    intent2.putExtra(Intent.EXTRA_TEXT, msg);
                                    startActivity(intent2);
                                } catch (Exception e) {
//                                    Log.d(TAG, "Error");
                                }
                                break;
                            }
                        }
                    }).create();
                    dialog.show();
                    */
                }
            });
        }    
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop the tracker when it is no longer needed.
        tracker.stop();
    }
}
