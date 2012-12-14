package org.chrysaor.android.gas_station.ui;

import org.chrysaor.android.gas_station.R;
import org.chrysaor.android.gas_station.lib.database.DatabaseHelper;
import org.chrysaor.android.gas_station.lib.database.FavoritesDao;
import org.chrysaor.android.gas_station.lib.database.StandsDao;
import org.chrysaor.android.gas_station.lib.dto.GasStand;
import org.chrysaor.android.gas_station.util.DetailAsyncTask;
import org.chrysaor.android.gas_station.util.DetailTaskCallback;
import org.chrysaor.android.gas_station.util.ErrorReporter;
import org.chrysaor.android.gas_station.util.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class DetailActivity extends Activity implements DetailTaskCallback {
    
    private DatabaseHelper dbHelper = null;
    private SQLiteDatabase db = null;
    private StandsDao standsDao = null;
    public GasStand info = null;
    private static final Integer pressed_color = Color.argb(80, 255, 255, 255);
    GoogleAnalyticsTracker tracker;
    private Integer favState = 0;
    private ImageButton favButton;
    private String ssId = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gsinfo);
        
        ErrorReporter.setup(this);
        ErrorReporter.bugreport(DetailActivity.this);

        tracker = GoogleAnalyticsTracker.getInstance();
        
        // Start the tracker in manual dispatch mode...
        tracker.start("UA-20090562-2", 20, this);
        tracker.trackPageView("/DetailActivity");
        dbHelper = new DatabaseHelper(this);
        
        favButton = (ImageButton) findViewById(R.id.btn_favorite);
        
        Bundle extras=getIntent().getExtras();
        if (extras != null) {
            String index = extras.getString("shopcode");
            ssId = index;
            
            new DetailAsyncTask(this, getLayoutInflater().inflate(R.layout.loading, null), findViewById(R.id.layout_main), this).execute(index);
            
            db = dbHelper.getReadableDatabase();
            
            FavoritesDao favoritesDao = new FavoritesDao(db);
            if (favoritesDao.findByShopCd(index) != null) {
                setFavState(1);
            }
            db.close();
            
        }
        
        // 戻るボタン
        Button backButton = (Button) findViewById(R.id.btn_back);
        backButton.setOnClickListener(new OnClickListener() {
 
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
    }
    
    private void setFavState(Integer state) {
        
        favState = state;
        switch (favState) {
        case 0:
            favButton.setImageDrawable(getResources().getDrawable(R.drawable.star_empty));
            break;
        case 1:
            favButton.setImageDrawable(getResources().getDrawable(R.drawable.star_full));
            break;
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        super.onCreateOptionsMenu( menu );
        // メニューアイテムを追加
//        MenuItem item0 = menu.add( 0, 0, 0, "ブラウザ" );
        MenuItem item1 = menu.add( 0, 1, 0, "共有" );
        MenuItem item2 = menu.add( 0, 2, 0, "設定" );

        // 追加したメニューアイテムのアイコンを設定
//        item0.setIcon( android.R.drawable.ic_menu_view);
        item1.setIcon( android.R.drawable.ic_menu_share);
        item2.setIcon( android.R.drawable.ic_menu_preferences);
        
        if (Utils.isDonate(this) == false) {
            MenuItem item3 = menu.add( 0, 3, 0, "有料版購入" );
            item3.setIcon( R.drawable.cart);
        }

        return true;
    }
    
    @Override  
    public boolean onOptionsItemSelected(MenuItem item){

        switch(item.getItemId()){
        case 0:
            
            // イベントトラック（ブラウザ）
            tracker.trackEvent(
                "Detail",      // Category
                "Browser",     // Action
                info.ShopCode, // Label
                0);
            
            Intent intent = new Intent(); 
            intent.setAction(Intent.ACTION_VIEW); 
            intent.setData(Uri.parse("http://gogo.gs/shop/" + info.ShopCode + ".html")); 
            startActivity(intent);
            break;
        case 1:
            try {
                String msg = PreferenceManager.getDefaultSharedPreferences(DetailActivity.this).getString("settings_sharemsg", getResources().getString(R.string.settings_sharemsg_default));
                
                String[] kinds = getResources().getStringArray(R.array.settings_list_kind);
                String kind = kinds[Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString("settings_kind", "0"))];

                if (info.Price.equals("9999")) {
                    msg = msg.replaceAll("#price", info.getDispPrice());
                } else {
                    if (kind.equals("灯油")) {
                        msg = msg.replaceAll("#price", info.getDispPrice() + "円/18L");
                    } else {
                        msg = msg.replaceAll("#price", info.getDispPrice() + "円/L");
                    }
                }
                msg = msg.replaceAll("#shop_name", info.ShopName).replaceAll("#kind", kind);

                // イベントトラック（共有）
                tracker.trackEvent(
                    "Detail",      // Category
                    "Share",       // Action
                    info.ShopCode, // Label
                    0);
                
                Intent intent2 = new Intent();
                intent2.setAction(Intent.ACTION_SEND);
                intent2.setType("text/plain");
                intent2.putExtra(Intent.EXTRA_TEXT, msg);
                startActivity(intent2);
            } catch (Exception e) {
//                Log.d(TAG, "Error");
            }
            break;
        case 2:
            
            // イベントトラック（設定）
            tracker.trackEvent(
                "Detail",   // Category
                "Settings", // Action
                "",         // Label
                0);

            Intent intent3 = new Intent(DetailActivity.this, SettingsActivity.class);
            startActivity(intent3);  
            break;
        case 3:
            // イベントトラック（Donate）
            tracker.trackEvent(
                "Detail",      // Category
                "Donate",      // Action
                "",            // Label
                0);
            
            Intent intent4 = new Intent(); 
            intent4.setAction(Intent.ACTION_VIEW); 
            intent4.setData(Uri.parse("market://details?id=org.chrysaor.android.gas_station.plus")); 
            startActivity(intent4);
            break;

        }
        return false;
    }
    
    @Override
    protected void onDestroy() {
      super.onDestroy();
      // Stop the tracker when it is no longer needed.
      tracker.stop();
      
      cleanupView(findViewById(R.id.RelativeLayout01));
    }
    
    /**
     * 指定したビュー階層内のドローワブルをクリアする。
     * （ドローワブルをのコールバックメソッドによるアクティビティのリークを防ぐため）
     * @param view
     */
    public static final void cleanupView(View view) {
        if(view instanceof ImageButton) {
            ImageButton ib = (ImageButton)view;
            ib.setImageDrawable(null);
        } else if(view instanceof ImageView) {
            ImageView iv = (ImageView)view;
            iv.setImageDrawable(null);
        } else if(view instanceof SeekBar) {
            SeekBar sb = (SeekBar)view;
            sb.setProgressDrawable(null);
            sb.setThumb(null);
        }
        view.setBackgroundDrawable(null);
        if(view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup)view;
            int size = vg.getChildCount();
            for(int i = 0; i < size; i++) {
                cleanupView(vg.getChildAt(i));
            }
        }
    }

    @Override
    public void onFailed(int errorCode) {
        Toast.makeText(this, "スタンド情報が取得できません", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onSuccess(GasStand info) {
        this.info = info;

        setButtonEvent();
    }
    
    private void setButtonEvent() {
        LinearLayout route = (LinearLayout) findViewById(R.id.layout_route);
        route.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
            
                if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                    v.setBackgroundColor(pressed_color);
                } else if(event.getAction() == MotionEvent.ACTION_UP) {
                    v.setBackgroundColor(Color.TRANSPARENT);
                    
                    // イベントトラック（ルート検索）
                    tracker.trackEvent(
                        "Detail",      // Category
                        "RouteSearch", // Action
                        info.ShopCode, // Label
                        0);
                    
                    if (Utils.isDonate(DetailActivity.this)) {
                        Intent intent = new Intent(); 
                        intent.setAction(Intent.ACTION_VIEW); 
                        intent.setClassName("com.google.android.apps.maps","com.google.android.maps.MapsActivity");
                        intent.setData(Uri.parse("http://maps.google.com/maps?myl=saddr&daddr=" + info.Address + "&dirflg=d")); 
                        DetailActivity.this.startActivity(intent);
                    } else {
                        Toast.makeText(DetailActivity.this, "無料版では使用できません", Toast.LENGTH_SHORT).show();
                    }
                }
                return true;
            }
        });

        LinearLayout post = (LinearLayout) findViewById(R.id.layout_post);
        post.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                    v.setBackgroundColor(pressed_color);
                } else if(event.getAction() == MotionEvent.ACTION_UP) {
                    v.setBackgroundColor(Color.TRANSPARENT);
                    
                    // イベントトラック（価格投稿）
                    tracker.trackEvent(
                        "Detail",      // Category
                        "Post",        // Action
                        info.ShopCode, // Label
                        0);
                    Intent intent = new Intent(DetailActivity.this, PostActivity.class);
                    intent.putExtra("shopcode", info.ShopCode);
                    DetailActivity.this.startActivity(intent);
                }
                return true;
            }
        });
        
        // 給油記録
        LinearLayout charge = (LinearLayout) findViewById(R.id.layout_charge);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.DONUT) {

            charge.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    
                    if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                        v.setBackgroundColor(pressed_color);
                    } else if(event.getAction() == MotionEvent.ACTION_UP) {
                        v.setBackgroundColor(Color.TRANSPARENT);
                        
                        // イベントトラック（給油記録）
                        tracker.trackEvent(
                            "Detail",      // Category
                            "Charge",      // Action
                            info.ShopCode, // Label
                            0);
                        
                        if (Utils.installedGasLogFree(DetailActivity.this) || Utils.installedGasLogPayment(DetailActivity.this)) {
                            Intent intent = new Intent();
                            if (Utils.installedGasLogPayment(DetailActivity.this)) {
                                intent.setClassName("jp.pinetail.android.gas_log.payment", "jp.pinetail.android.gas_log.core.FuelPostActivity");
                            } else {
                                intent.setClassName("jp.pinetail.android.gas_log.free", "jp.pinetail.android.gas_log.core.FuelPostActivity");
                            }
                            intent.putExtra("ssid",       info.ShopCode);
                            intent.putExtra("shop_name",  info.ShopName);
                            intent.putExtra("shop_brand", info.Brand);
                            intent.putExtra("lat",        info.Latitude.toString());
                            intent.putExtra("lon",        info.Longitude);
                            DetailActivity.this.startActivity(intent);
                        } else {
                            new AlertDialog.Builder(DetailActivity.this)
                            .setTitle("ガスログ！インストール")
                            .setMessage("給油記録にはガスログ！が必要です。インストールしますか？")
                            .setNeutralButton("インストール", new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(); 
                                    intent.setAction(Intent.ACTION_VIEW);
                                    intent.setData(Uri.parse("market://details?id=jp.pinetail.android.gas_log.free")); 
                                    DetailActivity.this.startActivity(intent);
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
                    return true;
                }
            });
        } else {
            charge.setVisibility(View.GONE);
        }

        // ブラウザ
        LinearLayout browser = (LinearLayout) findViewById(R.id.layout_browser);
        browser.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                
                if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                    v.setBackgroundColor(pressed_color);
                } else if(event.getAction() == MotionEvent.ACTION_UP) {
                    v.setBackgroundColor(Color.TRANSPARENT);
                    
                    // イベントトラック（ブラウザ）
                    tracker.trackEvent(
                        "Detail",      // Category
                        "Browser",     // Action
                        info.ShopCode, // Label
                        0);
                    
                    Intent intent = new Intent(); 
                    intent.setAction(Intent.ACTION_VIEW); 
                    intent.setData(Uri.parse("http://gogo.gs/shop/" + info.ShopCode + ".html")); 
                    DetailActivity.this.startActivity(intent);
                }
                return true;
            }    
        });
        
        // お気に入りボタン
        favButton.setOnClickListener(new OnClickListener() {
 
            @Override
            public void onClick(View v) {
                db = dbHelper.getReadableDatabase();
                
                FavoritesDao favoritesDao = new FavoritesDao(db);

                switch (favState) {
                case 0:
                    // 登録件数の確認
                    if (favoritesDao.findAll("create_date").size() >= 20) {
                        Toast.makeText(DetailActivity.this, "お気に入りは20件までしか登録出来ません。", Toast.LENGTH_SHORT).show();
                    } else {
                        favoritesDao.insert(info);
                        setFavState(1);
                        Toast.makeText(DetailActivity.this, "お気に入りに登録しました", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 1:
                    favoritesDao.deleteByShopCd(info.ShopCode);
                    setFavState(0);
                    Toast.makeText(DetailActivity.this, "お気に入りを解除しました", Toast.LENGTH_SHORT).show();
                    break;
                }
                db.close();
            }
        });
    }
}
