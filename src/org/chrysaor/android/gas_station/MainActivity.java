package org.chrysaor.android.gas_station;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import jp.co.nobot.libYieldMaker.libYieldMaker;

import org.chrysaor.android.gas_station.ui.AboutActivity;
import org.chrysaor.android.gas_station.ui.DetailActivity;
import org.chrysaor.android.gas_station.ui.FavoriteListActivity;
import org.chrysaor.android.gas_station.ui.ListActivity;
import org.chrysaor.android.gas_station.ui.SettingsActivity;
import org.chrysaor.android.gas_station.util.CenterCircleOverlay;
import org.chrysaor.android.gas_station.util.DatabaseHelper;
import org.chrysaor.android.gas_station.util.ErrorReporter;
import org.chrysaor.android.gas_station.util.GSInfo;
import org.chrysaor.android.gas_station.util.InfoController;
import org.chrysaor.android.gas_station.util.LocationOverlay;
import org.chrysaor.android.gas_station.util.SeekBarPreference;
import org.chrysaor.android.gas_station.util.StandController;
import org.chrysaor.android.gas_station.util.StandsDao;
import org.chrysaor.android.gas_station.util.StandsHelper;
import org.chrysaor.android.gas_station.util.UpdateFavoritesService;
import org.chrysaor.android.gas_station.util.Utils;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class MainActivity extends MapActivity implements Runnable {

    private static final int E6 = 1000000;
    private static final int MIN_IMAGE = 100;
    private static final int MAX_IMAGE = 160;
    private MapController mMapController = null;
    private MapView mMapView = null;
    private LocationManager mLocationManager;
    private static final long LOCATION_MIN_TIME = 10000 * 1;
    private static final float LOCATION_MIN_DISTANCE = 5.0F;
    public static Location myLocation = null;
    private static Boolean donate = false; 
    protected InputStream is;
    private ProgressDialog dialog;
    private Resources resource;
    private LocationOverlay overlay;
    private SQLiteDatabase db;
    public ArrayList<GSInfo> list;
    public static final String DONATE_PACKAGE = "org.chrysaor.android.gas_station.plus";
    public static final String ACTION_FAVORITE = "org.chrysaor.android.intent.receive.FAVORITE";
    private Drawable[] images = new Drawable[61];
    private static final Integer pressed_color = Color.argb(80, 255, 255, 255);
    GoogleAnalyticsTracker tracker;

    //天候情報生成クラス
    private InfoController infoController;
    
    private final Handler handler = new Handler();
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        ErrorReporter.setup(this);
        ErrorReporter.bugreport(MainActivity.this);
        
        tracker = GoogleAnalyticsTracker.getInstance();
        
        // Start the tracker in manual dispatch mode...
        tracker.start("UA-20090562-2", 20, this);
        tracker.trackPageView("/MainActivity");
        tracker.setCustomVar(1, "Model", Build.MODEL, 1);
        try {
            tracker.setCustomVar(2, "Version", getPackageManager().getPackageInfo(getPackageName(), 1 ).versionName, 1);
        } catch (NameNotFoundException e) {}
        
        String num = PreferenceManager.getDefaultSharedPreferences(this).getString("settings_dist", "60");
//        Log.d(LOG_TAG, LOG_TAG + num);

        // 操作パネルの透過率設定
        setPenetration();

        // Donateの確認
        checkDonate();
             
        mMapView = (MapView) findViewById(R.id.main_map);
//        mMapView.setBuiltInZoomControls(true);

        mMapController = mMapView.getController();

        CenterCircleOverlay location = new CenterCircleOverlay(this);
        mMapView.getOverlays().add(location);
        
        ToggleButton toggle = (ToggleButton) findViewById(R.id.trace_mylocation);

        // 今回の主役。有効にすることでGPSの取得が可能に
        overlay = new LocationOverlay(getApplicationContext(), mMapView);
        overlay.enableMyLocation();
        overlay.enableCompass();
        overlay.setTraceToggle(toggle);

        // GPS取得が可能な状態になり、GPS初取得時の動作を決定（らしい）
        overlay.runOnFirstFix(new Runnable(){
            public void run() {
                try {
                    // animateTo(GeoPoint)で指定GeoPoint位置に移動
                    // この場合、画面中央がGPS取得による現在位置になる
                    mMapView.getController().animateTo(overlay.getMyLocation());
                } catch (Exception e) {
                    
                }
            }
        });

        // Overlayとして登録
        mMapView.getOverlays().add(overlay);
        
        // 検索ボタンのonClick設定
        ImageView search_img = (ImageView) findViewById(R.id.search_img);
        search_img.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                    v.setBackgroundColor(pressed_color);
                } else if(event.getAction() == MotionEvent.ACTION_UP) {
                    v.setBackgroundColor(Color.TRANSPARENT);
                    MainActivity.this.searchAction();
                }
                return true;
            }
        });

        // リストボタンのonClick設定
        ImageView list = (ImageView) findViewById(R.id.main_list);
        list.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                    v.setBackgroundColor(pressed_color);
                } else if(event.getAction() == MotionEvent.ACTION_UP) {
                    v.setBackgroundColor(Color.TRANSPARENT);
                    // イベントトラック（リスト）
                    tracker.trackEvent(
                        "Main",     // Category
                        "List",     // Action
                        null,       // Label
                        0);
                    
                    Intent intent = new Intent(MainActivity.this, ListActivity.class);
                    startActivityForResult(intent, 0);
                }
                return true;
            }
        });
        
        // リストボタンのonClick設定
        ImageView favList = (ImageView) findViewById(R.id.favorite);
        favList.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                    v.setBackgroundColor(pressed_color);
                } else if(event.getAction() == MotionEvent.ACTION_UP) {
                    v.setBackgroundColor(Color.TRANSPARENT);
                    // イベントトラック（リスト）
                    tracker.trackEvent(
                        "Main",     // Category
                        "FavoriteList",     // Action
                        null,       // Label
                        0);
                    
                    Intent intent = new Intent(MainActivity.this, FavoriteListActivity.class);
                    startActivityForResult(intent, 0);
                }
                return true;
            }
        });
        
        // ズームアウトボタンのonClick設定
        ImageView zoomout_img = (ImageView) findViewById(R.id.zoomout);
        zoomout_img.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                    v.setBackgroundColor(pressed_color);
                } else if(event.getAction() == MotionEvent.ACTION_UP) {
                    v.setBackgroundColor(Color.TRANSPARENT);
                    mMapController.zoomOut();
                }
                return true;
            }    
        });
        
        // ズームインボタンのonClick設定
        ImageView zoomin_img = (ImageView) findViewById(R.id.zoomin);
        zoomin_img.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                    v.setBackgroundColor(pressed_color);
                } else if(event.getAction() == MotionEvent.ACTION_UP) {
                    v.setBackgroundColor(Color.TRANSPARENT);
                    mMapController.zoomIn();
                }
                return true;
            }    
        });

        if (donate == false) {
            LinearLayout head = (LinearLayout) findViewById(R.id.header_ad);
            head.setVisibility(View.VISIBLE);
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
        } else {
            View header = (View) findViewById(R.id.header);
            header.setVisibility(View.VISIBLE);
        }

        images[0] = getResources().getDrawable(R.drawable.p100);
        images[1] = getResources().getDrawable(R.drawable.p101);
        images[2] = getResources().getDrawable(R.drawable.p102);
        images[3] = getResources().getDrawable(R.drawable.p103);
        images[4] = getResources().getDrawable(R.drawable.p104);
        images[5] = getResources().getDrawable(R.drawable.p105);
        images[6] = getResources().getDrawable(R.drawable.p106);
        images[7] = getResources().getDrawable(R.drawable.p107);
        images[8] = getResources().getDrawable(R.drawable.p108);
        images[9] = getResources().getDrawable(R.drawable.p109);
        images[10] = getResources().getDrawable(R.drawable.p110);
        images[11] = getResources().getDrawable(R.drawable.p111);
        images[12] = getResources().getDrawable(R.drawable.p112);
        images[13] = getResources().getDrawable(R.drawable.p113);
        images[14] = getResources().getDrawable(R.drawable.p114);
        images[15] = getResources().getDrawable(R.drawable.p115);
        images[16] = getResources().getDrawable(R.drawable.p116);
        images[17] = getResources().getDrawable(R.drawable.p117);
        images[18] = getResources().getDrawable(R.drawable.p118);
        images[19] = getResources().getDrawable(R.drawable.p119);
        images[20] = getResources().getDrawable(R.drawable.p120);
        images[21] = getResources().getDrawable(R.drawable.p121);
        images[22] = getResources().getDrawable(R.drawable.p122);
        images[23] = getResources().getDrawable(R.drawable.p123);
        images[24] = getResources().getDrawable(R.drawable.p124);
        images[25] = getResources().getDrawable(R.drawable.p125);
        images[26] = getResources().getDrawable(R.drawable.p126);
        images[27] = getResources().getDrawable(R.drawable.p127);
        images[28] = getResources().getDrawable(R.drawable.p128);
        images[29] = getResources().getDrawable(R.drawable.p129);
        images[30] = getResources().getDrawable(R.drawable.p130);
        images[31] = getResources().getDrawable(R.drawable.p131);
        images[32] = getResources().getDrawable(R.drawable.p132);
        images[33] = getResources().getDrawable(R.drawable.p133);
        images[34] = getResources().getDrawable(R.drawable.p134);
        images[35] = getResources().getDrawable(R.drawable.p135);
        images[36] = getResources().getDrawable(R.drawable.p136);
        images[37] = getResources().getDrawable(R.drawable.p137);
        images[38] = getResources().getDrawable(R.drawable.p138);
        images[39] = getResources().getDrawable(R.drawable.p139);
        images[40] = getResources().getDrawable(R.drawable.p140);
        images[41] = getResources().getDrawable(R.drawable.p141);
        images[42] = getResources().getDrawable(R.drawable.p142);
        images[43] = getResources().getDrawable(R.drawable.p143);
        images[44] = getResources().getDrawable(R.drawable.p144);
        images[45] = getResources().getDrawable(R.drawable.p145);
        images[46] = getResources().getDrawable(R.drawable.p146);
        images[47] = getResources().getDrawable(R.drawable.p147);
        images[48] = getResources().getDrawable(R.drawable.p148);
        images[49] = getResources().getDrawable(R.drawable.p149);
        images[50] = getResources().getDrawable(R.drawable.p150);
        images[51] = getResources().getDrawable(R.drawable.p151);
        images[52] = getResources().getDrawable(R.drawable.p152);
        images[53] = getResources().getDrawable(R.drawable.p153);
        images[54] = getResources().getDrawable(R.drawable.p154);
        images[55] = getResources().getDrawable(R.drawable.p155);
        images[56] = getResources().getDrawable(R.drawable.p156);
        images[57] = getResources().getDrawable(R.drawable.p157);
        images[58] = getResources().getDrawable(R.drawable.p158);
        images[59] = getResources().getDrawable(R.drawable.p159);
        images[60] = getResources().getDrawable(R.drawable.p160);
        
        mMapView.invalidate();
        /*
        // 位置情報の取得を開始
        mLocationManager = ((LocationManager) getSystemService(Context.LOCATION_SERVICE));
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_MIN_TIME,
                LOCATION_MIN_DISTANCE, mListener);
                */
        
        Intent service = new Intent(this, UpdateFavoritesService.class);
        service.setAction(UpdateFavoritesService.START_ACTION);
        startService(service);

    }
    
       //アクティビティ呼び出し結果の取得
    @Override
    protected void onActivityResult(int requestCode,
        int resultCode,Intent intent) {
        if (requestCode==0 && resultCode==RESULT_OK) {
            //インテントからのパラメータ取得
            Bundle extras=intent.getExtras();
            if (extras!=null) {
                Double lan = extras.getDouble("lat");
                String lon = extras.getString("lon");
                
                GeoPoint point = new GeoPoint(
                        (int) ((double) lan * E6),
                        (int) (Double.parseDouble(lon) * E6));
                mMapController.animateTo(point);
            }
        }
    }
    
    @Override
    protected void onResume() {
        overlay.enableMyLocation();
        overlay.enableCompass();
        if (mLocationManager != null) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_MIN_TIME,
                    LOCATION_MIN_DISTANCE, mListener);
        }
        
           // Donateの確認
        checkDonate();
        
        if (donate == true) {
            View header = (View) findViewById(R.id.header);
            header.setVisibility(View.VISIBLE);
        }
        
        // 操作パネルの透過率設定
        setPenetration();

//        Log.d(LOG_TAG, "donate:" + donate.toString());
        
        super.onResume();

//        Log.d(LOG_TAG, "gas resume");
    }
    
    @Override
    protected void onPause() {
        overlay.disableMyLocation();
        overlay.disableCompass();
        if (mLocationManager != null) {
            mLocationManager.removeUpdates(mListener);
        }
        
//        Log.d(LOG_TAG, "gas pause");

        super.onPause();
    }
    
    @Override
    protected void onDestroy() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        StandsDao standsDao = new StandsDao(db);
        standsDao.deleteAll();
        db.close();
        
        // Stop the tracker when it is no longer needed.
        tracker.stop();
        
        super.onDestroy();
        
        Intent service = new Intent(this, UpdateFavoritesService.class);
        service.setAction(UpdateFavoritesService.START_ACTION);
        stopService(service);
        
        cleanupView(findViewById(R.id.screen));
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
      switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_CANCEL:
          break;
      case MotionEvent.ACTION_MOVE:
          CenterCircleOverlay location = new CenterCircleOverlay(this);
          mMapView.getOverlays().add(location);
          break;
      }
        return true;
    }
    
    protected void checkDonate() {
           // PackageManagerの取得
        PackageManager manager = getPackageManager();
            
        // 特定のパッケージがインストールされているか判定
        try {
            ApplicationInfo ai = manager.getApplicationInfo(DONATE_PACKAGE, 0);
            donate = true;
        } catch (NameNotFoundException e) {

        }
    }
    
    protected void setPenetration() {
        int penetration = PreferenceManager.getDefaultSharedPreferences(this).getInt("settings_penetration", SeekBarPreference.OPT_SEEKBAR_DEF);
        View header = (View) findViewById(R.id.header);
        header.setBackgroundColor(Color.argb((int)((100 - penetration) * 2.55), 0, 0, 0));

        View footer = (View) findViewById(R.id.footer);
        footer.setBackgroundColor(Color.argb((int)((100 - penetration) * 2.55), 0, 0, 0));
    }
    
    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        super.onCreateOptionsMenu( menu );
        // メニューアイテムを追加
        MenuItem item1 = menu.add( 0, 2, 0, "検索" );
        MenuItem item2 = menu.add( 0, 0, 0, "設定" );
        MenuItem item0 = menu.add( 0, 1, 0, "現在地" );
        MenuItem item3 = menu.add( 0, 3, 0, "about" );
        // 追加したメニューアイテムのアイコンを設定
        item0.setIcon( android.R.drawable.ic_menu_mylocation);
        item1.setIcon( android.R.drawable.ic_menu_search );
        item2.setIcon( android.R.drawable.ic_menu_preferences );
        item3.setIcon( android.R.drawable.ic_menu_info_details );
        return true;
    }
    
    @Override  
    public boolean onOptionsItemSelected(MenuItem item){

        switch(item.getItemId()){  
        case 0:
            // イベントトラック（設定）
            tracker.trackEvent(
                "Main",      // Category
                "Settings",  // Action
                null,        // Label
                0);
            
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);  
            return true;
        case 1:
            // イベントトラック（現在地）
            tracker.trackEvent(
                "Main",      // Category
                "Location",  // Action
                null,        // Label
                0);
            
//            overlay.setMyLocationFlag(true);
            GeoPoint l = overlay.getMyLocation();
            
            if (l == null) {
                Toast.makeText(this, "現在地を特定できません", Toast.LENGTH_LONG).show();
            } else {
                // 取得した位置をマップの中心に設定
                mMapController.animateTo(l);
            }
            return true;
        case 2:
            return searchAction();
        case 3:
            // イベントトラック（about）
            tracker.trackEvent(
                "Main",      // Category
                "About",     // Action
                null,        // Label
                0);
            
            Intent intent2 = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(intent2);  
            return true;
        }  
        return false;  
    }
    
    public Boolean searchAction() {
        try{
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            String dist = pref.getString("settings_dist", "10");
            resource = getResources();

            String url_string = "http://api.gogo.gs/v1.2/?apid=gsearcho0o0";
            url_string = url_string + "&dist=" + dist;
            url_string = url_string + "&num=" + pref.getString("settings_num", "60");
            url_string = url_string + "&span=" + pref.getString("settings_span", "");
            Boolean member = pref.getBoolean("settings_member", false);
            if (member == true) {
                url_string = url_string + "&member=1";
            }
            url_string = url_string + "&kind=" + pref.getString("settings_kind", "0");

            String sort = pref.getString("settings_sort", "dist");
            if (sort.equals("dist")) {
                url_string = url_string + "&sort=d";
            }

            // イベントトラック（検索）
            tracker.trackEvent(
                "Main",      // Category
                "Search",    // Action
                url_string,      // Label
                0);
            
            // 地図の中心位置を取得
            GeoPoint center = mMapView.getMapCenter();
            url_string = url_string + "&lat=" +  (double) center.getLatitudeE6() / E6;
            url_string = url_string + "&lon=" +  (double) center.getLongitudeE6() / E6;
            
            String url = url_string;
            Utils.logging("url = " + url.toString());
            
            Double lat = (double) center.getLatitudeE6() / E6;
            Double lon = (double) center.getLongitudeE6() / E6;
            
            String url4all = "";
            
            if (pref.getBoolean("settings_no_postdata", Boolean.valueOf(resource.getText(R.string.settings_no_postdata_default).toString()))) {
                int no_dist = Integer.valueOf(dist);
                if (no_dist > 10) {
                    no_dist = 10;
                }
                url4all = "http://api.gogo.gs/ap/gsst/ssLatLonFull.php?" +
                        "lat_min=" + (double) (lat - 0.0083 * no_dist) +
                        "&lat_max=" + (double) (lat + 0.0083 * no_dist) +
                        "&lon_min=" + (double) (lon - 0.0125 * no_dist) +
                        "&lon_max=" + (double) (lon + 0.0125 * no_dist) +
                        "&pm=" + pref.getString("settings_kind", "0") +
                        "&n=100";
            }
            
            Utils.logging(url4all);
            
            //マップ中心の周辺にあるガソリンスタンド情報を取得する
            infoController = new InfoController(handler, this, url, url4all);

            //プログレスダイアログを表示
            dialog = new ProgressDialog(this);
            dialog.setIndeterminate(true);
            dialog.setMessage(resource.getText(R.string.dialog_message_getting_data));
            dialog.show();
            
            mMapView.getOverlays().clear();
            CenterCircleOverlay location = new CenterCircleOverlay(this);
            mMapView.getOverlays().add(location);
            
            // Overlayとして登録
            mMapView.getOverlays().add(overlay);
            
            infoController.start();
        }catch(Exception e){
            Utils.logging(e.getMessage());
        }
        return true;
    }
    
    @Override
    public void run() {
        //プログレスダイアログを閉じる
        dialog.dismiss();

        list = infoController.getGSInfoList();
        PinItemizedOverlay pinOverlay = null;
                
        DatabaseHelper dbHelper = new DatabaseHelper(MainActivity.this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        StandsDao standsDao = new StandsDao(db);
        standsDao.deleteAll();

        ImageView btn = (ImageView) findViewById(R.id.main_list);

        //取得に失敗
        if(list == null || list.size() <= 0) {
            btn.setVisibility(View.INVISIBLE);
            Toast.makeText(this, resource.getText(R.string.dialog_message_out_of_range), Toast.LENGTH_LONG).show();
        } else {

            Drawable speech  = getResources().getDrawable(R.drawable.pbase);
            Drawable nodata  = getResources().getDrawable(R.drawable.pnodata);
            
            String pin_type = PreferenceManager.getDefaultSharedPreferences(this).getString("settings_pin_type", "price");
            ArrayList<PinItemizedOverlay> pins = new ArrayList<PinItemizedOverlay>();;

            StandsHelper helper = StandsHelper.getInstance();

            db.beginTransaction();
            try {
                int size = list.size();
                for (int i=0;i<size;i++) {

                    GSInfo info = list.get(i);
                    standsDao.insert(info);
                    
                    if (pin_type.compareTo("price") == 0) {
                        int price = Integer.parseInt(info.Price);
                        if (MIN_IMAGE <= price && price <= MAX_IMAGE) {
                            pinOverlay = new PinItemizedOverlay(images[price - MIN_IMAGE]);
                        } else if (price == 9999) {
                            pinOverlay = new PinItemizedOverlay(nodata);                            
                        } else {
                            pinOverlay = new PinItemizedOverlay(speech);
                        }
                    } else {
                        pinOverlay = new PinItemizedOverlay(getResources().getDrawable(helper.getBrandImage(info.Brand, Integer.valueOf(info.Price))));
                    }
                    
                    GeoPoint point = new GeoPoint(
                            (int) ((double) info.getLatitude() * E6),
                            (int) (Double.parseDouble(info.getLongitude()) * E6));
                    
                    pinOverlay.addPoint(point);
                    pinOverlay.setMsg(info.ShopName + "\n" + info.Brand + "\n" + info.Address + "\n" + info.Price + "円");
                    pinOverlay.setPrice(info.Price);
                    pinOverlay.setPinType(pin_type);
                    pinOverlay.setGSInfo(info);
//                      mMapView.getOverlays().add(pinOverlay);
                       pins.add(pinOverlay);

                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            mMapView.getOverlays().addAll(pins);
            mMapView.invalidate();
            
            btn.setVisibility(View.VISIBLE);
            Toast.makeText(this, list.size() + "件のスタンドが見つかりました", Toast.LENGTH_LONG).show();
        }
        db.close();
    }

    /**
     * 位置情報の更新を処理するリスナー
     */
    private LocationListener mListener = new LocationListener() {
        public void onStatusChanged(String provider, int status, Bundle extras) {
            
            switch (status) {
            case LocationProvider.AVAILABLE:
                break;
            case LocationProvider.OUT_OF_SERVICE:
                Toast.makeText(MainActivity.this, "GPSサービスが利用できません", Toast.LENGTH_LONG).show();
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
//                Toast.makeText(MainActivity.this, "GPSデータを取得できません", Toast.LENGTH_LONG).show();
                break;
            }
        }
        
        public void onProviderEnabled(String provider) { }
        public void onProviderDisabled(String provider) { }

        public void onLocationChanged(Location location) {
            
            Utils.logging("longitude = " + location.getLongitude());
            Utils.logging("latitude = " + location.getLatitude());

            MainActivity.myLocation = location;     
        }
    };
    
    public class PinItemizedOverlay extends ItemizedOverlay<PinOverlayItem> implements Runnable {

        private List<GeoPoint> points = new ArrayList<GeoPoint>();
        private List<String> msgs = new ArrayList<String>();
        private List<String> prices = new ArrayList<String>();
        private List<String> pin_types = new ArrayList<String>();
        private List<GSInfo> gsInfo = new ArrayList<GSInfo>();
        private StandController stand;

        public PinItemizedOverlay(Drawable defaultMarker) {
            super( boundCenterBottom(defaultMarker) );
        }

        @Override
        protected PinOverlayItem createItem(int i) {
            GeoPoint point = points.get(i);
            return new PinOverlayItem(point);
        }

        @Override
        public int size() {
            return points.size();
        }

        public void addPoint(GeoPoint point) {
            points.add(point);
            populate();
        }
        
        public void clearPoint() {
            points.clear();
            populate();
        }
                
        public void setMsg(String msg) {
            this.msgs.add(msg);
        }

        public void setPinType(String type) {
            this.pin_types.add(type);
        }

        public void setPrice(String title) {
            this.prices.add(title);
        }
        
        public void setGSInfo(GSInfo info) {
            this.gsInfo.add(info);
        }
        
        /**
         * アイテムがタップされた時の処理
         */
        @Override
        protected boolean onTap(int index) {
            
            GSInfo info = gsInfo.get(index);
            
            // イベントトラック（GSタップ）
            tracker.trackEvent(
                "Main",      // Category
                "Stand",     // Action
                info.ShopCode,   // Label
                0);

            Intent intent1 = new Intent(MainActivity.this, DetailActivity.class);
            intent1.putExtra("shopcode", info.ShopCode);
            startActivity(intent1);
            return true;
        }
        
        @Override
        public void run() {
            //プログレスダイアログを閉じる
            dialog.dismiss();
        }
        
        @Override
        public void draw(Canvas canvas, MapView mapView, boolean shadow) {
            if (shadow) {
                return;
            }
            
            super.draw(canvas, mapView, shadow);
            
            int size = prices.size();
            for (int i=0;i<size;i++) {

                String price = prices.get(i);
                String pin = pin_types.get(i);
                
                if (pin.compareTo("brand") == 0) {
                    continue;
                }
                
                if ((MIN_IMAGE <= Integer.parseInt(price) && Integer.parseInt(price) <= MAX_IMAGE)
                    || price.equals("9999")) {
                    continue;
                }
                
                GeoPoint locate = points.get(i);
                Paint p = new Paint();
                int sz = 5;
                
                Point pt = new Point();
                mapView.getProjection().toPixels(locate, pt);
                
                // Draw point caption and its bounding rectangle
                p.setTextSize(14);
                p.setAntiAlias(true);
                int sw = (int)(p.measureText(price) + 0.5f);
                int sh = 25;
                int sx = pt.x - sw / 2 - 5;
                int sy = pt.y - sh - sz - 2;

                canvas.drawText(price, sx + 5, sy + sh - 8, p);
            }
            
            return;
        }
    }
    
    public class PinOverlayItem extends OverlayItem {

        public PinOverlayItem(GeoPoint point){
            super(point, "", "");
        }
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
}
