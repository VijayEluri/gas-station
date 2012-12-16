package org.chrysaor.android.gas_station.activity;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang.StringUtils;
import org.chrysaor.android.gas_station.R;
import org.chrysaor.android.gas_station.lib.database.DatabaseHelper;
import org.chrysaor.android.gas_station.lib.database.StandsDao;
import org.chrysaor.android.gas_station.lib.dto.Stand;
import org.chrysaor.android.gas_station.util.CenterCircleOverlay;
import org.chrysaor.android.gas_station.util.LocationOverlay;
import org.chrysaor.android.gas_station.util.SearchThread;
import org.chrysaor.android.gas_station.util.SeekBarPreference;
import org.chrysaor.android.gas_station.util.StandsHelper;
import org.chrysaor.android.gas_station.util.UpdateFavoritesService;
import org.chrysaor.android.gas_station.util.Utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewStub;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class MainActivity extends AbstractMyMapActivity implements Runnable {

    private static final int E6 = 1000000;
    private static final long LOCATION_MIN_TIME = 10000 * 1;
    private static final float LOCATION_MIN_DISTANCE = 5.0F;
    public static final String ACTION_FAVORITE = "org.chrysaor.android.intent.receive.FAVORITE";

    private MapController mMapController = null;
    private MapView mMapView = null;
    private LocationManager mLocationManager;
    public static Location myLocation = null;
    private static Boolean donate = false;
    protected InputStream is;
    private ProgressDialog dialog;
    private Resources resource;
    private LocationOverlay overlay;
    private SQLiteDatabase db;
    private ArrayList<Stand> dtoStandList;
    private static Typeface tf;
    public static Display display;
    private SharedPreferences sp;
    private GestureDetector gestureDetector;
    private Timer mTimer = null;
    private Handler mHandler = new Handler();

    /** ガソリンスタンド検索スレッド */
    private SearchThread searchThread;

    /** ローディングレイアウト */
    private LinearLayout layoutHeader2;

    private final Handler handler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // ローディングレイアウト
        layoutHeader2 = (LinearLayout) findViewById(R.id.header2);

        gestureDetector = new GestureDetector(this, simpleOnGestureListener);
        sp = PreferenceManager.getDefaultSharedPreferences(this);

        // ダイアログの初期化
        dialog = new ProgressDialog(this);

        String num = sp.getString("settings_dist", "60");

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        display = wm.getDefaultDisplay();

        // 操作パネルの透過率設定
        setPenetration();

        // Donateの確認
        donate = Utils.isDonate(getApplicationContext());

        // ガソリン価格のフォント読み込み
        tf = Typeface.createFromAsset(getAssets(), "fonts/7barPBd.TTF");

        ViewStub stub = (ViewStub) findViewById(R.id.mapview_stub);
        if (Utils.isDeguggable(this)) {
            Utils.logging("debug");
            stub.setLayoutResource(R.layout.map4dev);
        } else {
            Utils.logging("release");
            stub.setLayoutResource(R.layout.map4prod);
        }
        View inflated = stub.inflate();
        mMapView = (MapView) inflated;

        mMapView = (MapView) findViewById(R.id.main_map);
        mMapView.setBuiltInZoomControls(true);
        mMapView.getZoomButtonsController().getZoomControls()
                .setPadding(0, 0, 0, 85);

        mMapController = mMapView.getController();

        CenterCircleOverlay location = new CenterCircleOverlay(this);
        mMapView.getOverlays().add(location);

        // 現在地追跡処理 START
        ToggleButton toggle = (ToggleButton) findViewById(R.id.trace_mylocation);

        // 今回の主役。有効にすることでGPSの取得が可能に
        overlay = new LocationOverlay(getApplicationContext(), mMapView);
        overlay.enableMyLocation();
        overlay.enableCompass();
        overlay.setTraceToggle(toggle);

        // GPS取得が可能な状態になり、GPS初取得時の動作を決定
        overlay.runOnFirstFix(new Runnable() {
            public void run() {
                try {
                    // animateTo(GeoPoint)で指定GeoPoint位置に移動
                    // この場合、画面中央がGPS取得による現在位置になる
                    mMapView.getController().animateTo(overlay.getMyLocation());

                    // 検索
                    setTimer();
                } catch (Exception e) {

                }
            }
        });
        // 現在地追跡処理 END

        // ヘッダーViewの設定
        setHeaderView();

        // フッターViewの設定
        setFooterView();

        // Overlayとして登録
        mMapView.getOverlays().add(overlay);
        mMapView.invalidate();

        // お気に入り更新サービスの起動
        Intent service = new Intent(this, UpdateFavoritesService.class);
        service.setAction(UpdateFavoritesService.START_ACTION);
        startService(service);
    }

    /**
     * ヘッダーViewの設定
     */
    private void setHeaderView() {
        // 会員価格処理 START
        ToggleButton toggleMember = (ToggleButton) findViewById(R.id.toggle_member);
        toggleMember.setChecked(sp.getBoolean("settings_member", false));
        toggleMember.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked) {

                Editor editor = sp.edit();

                if (isChecked) {
                    editor.putBoolean("settings_member", true);
                } else {
                    editor.putBoolean("settings_member", false);
                }
                editor.commit();

                // 再検索
                if (dialog != null && dialog.isShowing() == false) {
                    searchAction();
                }
            }
        });
        // 会員価格処理 START

        // 24H ONLY処理 START
        ToggleButton toggleRtc = (ToggleButton) findViewById(R.id.toggle_rtc);
        toggleRtc.setChecked(sp.getBoolean("settings_rtc", false));
        toggleRtc.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked) {

                Editor editor = sp.edit();

                if (isChecked) {
                    editor.putBoolean("settings_rtc", true);
                } else {
                    editor.putBoolean("settings_rtc", false);
                }
                editor.commit();

                // 再検索
                if (dialog != null && dialog.isShowing() == false) {
                    searchAction();
                }
            }
        });
        // 24H ONLY処理 START

        if (donate == true) {
            // ADの非表示
            LinearLayout head = (LinearLayout) findViewById(R.id.header_ad);
            head.setVisibility(View.GONE);

            View header = (View) findViewById(R.id.header);
            header.setVisibility(View.VISIBLE);
        }
    }

    /**
     * フッターViewの設定
     */
    private void setFooterView() {
        // 検索ボタンのonClick設定
        ImageView imgSsearch = (ImageView) findViewById(R.id.search_img);
        imgSsearch.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                MainActivity.this.searchAction();
            }
        });

        // リストボタンのonClick設定
        ImageView imgList = (ImageView) findViewById(R.id.main_list);
        imgList.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // イベントトラック（リスト）
                tracker.trackEvent("Main", "List", null, 0);

                Intent intent = new Intent(getApplicationContext(),
                        ListActivity.class);
                startActivityForResult(intent, 0);
            }
        });

        // お気に入りボタンのonClick設定
        ImageView imgFav = (ImageView) findViewById(R.id.favorite);
        imgFav.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // イベントトラック（リスト）
                tracker.trackEvent("Main", "FavoriteList", null, 0);

                Intent intent = new Intent(getApplicationContext(),
                        FavoriteListActivity.class);
                startActivityForResult(intent, 0);
            }
        });

        // 現在地ボタンのonClick設定
        ImageView imgMyLocation = (ImageView) findViewById(R.id.imgMyLocation);
        imgMyLocation.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                GeoPoint l = overlay.getMyLocation();

                if (l == null) {
                    Toast.makeText(getApplicationContext(), "現在地を特定できません",
                            Toast.LENGTH_LONG).show();
                } else {
                    // 取得した位置をマップの中心に設定
                    mMapController.animateTo(l);
                }
            }
        });

        // 設定ボタンのonClick設定
        ImageView imgSetting = (ImageView) findViewById(R.id.imgSetting);
        imgSetting.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),
                        SettingsActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);

        switch (event.getAction()) {
        case MotionEvent.ACTION_UP:
            if (lastEvent == "onFling" || lastEvent == "onScroll") {
                setTimer();
            } else {
                if (mTimer != null) {
                    mTimer.cancel();
                }
            }
            break;
        default:
            if (mTimer != null) {
                mTimer.cancel();
            }
            break;
        }
        return super.dispatchTouchEvent(event);
    }

    /**
     * 検索のタイマーセット
     */
    private void setTimer() {

        mTimer = new Timer(true);
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                // mHandlerを通じてUI Threadへ処理をキューイング
                mHandler.post(new Runnable() {
                    public void run() {
                        searchAction();
                    }
                });
            }
        }, 750);
    }

    // アクティビティ呼び出し結果の取得
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
            Intent intent) {
        if (requestCode == 0 && resultCode == RESULT_OK) {
            // インテントからのパラメータ取得
            Bundle extras = intent.getExtras();
            if (extras != null) {
                Double lan = extras.getDouble("lat");
                String lon = extras.getString("lon");

                GeoPoint point = new GeoPoint((int) ((double) lan * E6),
                        (int) (Double.parseDouble(lon) * E6));
                mMapController.animateTo(point);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        overlay.enableMyLocation();

        // Donateの確認
        donate = Utils.isDonate(getApplicationContext());

        if (donate == true) {
            View header = (View) findViewById(R.id.header);
            header.setVisibility(View.VISIBLE);

            // 会員価格処理 START
            ToggleButton toggleMember = (ToggleButton) findViewById(R.id.toggle_member);
            toggleMember.setChecked(sp.getBoolean("settings_member", false));
            // 会員価格処理 START

            // 24H ONLY処理 START
            ToggleButton toggleRtc = (ToggleButton) findViewById(R.id.toggle_rtc);
            toggleRtc.setChecked(sp.getBoolean("settings_rtc", false));
            // 24H ONLY処理 START
        }

        // 操作パネルの透過率設定
        setPenetration();

    }

    @Override
    protected void onPause() {
        super.onPause();

        overlay.disableMyLocation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        StandsDao standsDao = new StandsDao(db);
        standsDao.deleteAll();
        db.close();

        Intent service = new Intent(this, UpdateFavoritesService.class);
        service.setAction(UpdateFavoritesService.START_ACTION);
        stopService(service);
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

    /**
     * レイアウトの透過設定
     */
    protected void setPenetration() {
        int penetration = PreferenceManager.getDefaultSharedPreferences(this)
                .getInt("settings_penetration",
                        SeekBarPreference.OPT_SEEKBAR_DEF);
        int color = Color.argb((int) ((100 - penetration) * 2.55), 0, 0, 0);

        View header = (View) findViewById(R.id.header);
        header.setBackgroundColor(color);

        View header2 = (View) findViewById(R.id.header2);
        header2.setBackgroundColor(color);

        View footer = (View) findViewById(R.id.footer);
        footer.setBackgroundColor(color);
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // メニューアイテムを追加
        MenuItem item1 = menu.add(0, 2, 0, "検索");
        MenuItem item2 = menu.add(0, 0, 0, "設定");
        MenuItem item0 = menu.add(0, 1, 0, "現在地");
        MenuItem item3 = menu.add(0, 3, 0, "about");
        // 追加したメニューアイテムのアイコンを設定
        item0.setIcon(android.R.drawable.ic_menu_mylocation);
        item1.setIcon(android.R.drawable.ic_menu_search);
        item2.setIcon(android.R.drawable.ic_menu_preferences);
        item3.setIcon(android.R.drawable.ic_menu_info_details);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
        case 0:
            // イベントトラック（設定）
            tracker.trackEvent("Main", "Settings", null, 0);

            Intent intent = new Intent(MainActivity.this,
                    SettingsActivity.class);
            startActivity(intent);
            return true;
        case 1:
            // イベントトラック（現在地）
            tracker.trackEvent("Main", "Location", null, 0);

            // overlay.setMyLocationFlag(true);
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
            tracker.trackEvent("Main", "About", null, 0);

            Intent intent2 = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(intent2);
            return true;
        }
        return false;
    }

    /**
     * 検索処理
     * 
     * @return
     */
    public Boolean searchAction() {

        showLoadingView();

        try {
            SharedPreferences pref = PreferenceManager
                    .getDefaultSharedPreferences(MainActivity.this);
            String dist = pref.getString("settings_dist", "10");
            resource = getResources();

            String url_string = "http://api.gogo.gs/v1.2/?apid=gsearcho0o0";
            url_string += "&dist=" + dist;
            url_string += "&num=" + pref.getString("settings_num", "60");
            url_string += "&span=" + pref.getString("settings_span", "");
            Boolean member = pref.getBoolean("settings_member", false);
            if (member == true) {
                url_string += "&member=1";
            }
            url_string += "&kind=" + pref.getString("settings_kind", "0");

            String sort = pref.getString("settings_sort", "dist");
            if (sort.equals("dist")) {
                url_string += "&sort=d";
            }

            // イベントトラック（検索）
            tracker.trackEvent("Main", "Search", url_string, 0);

            // 地図の中心位置を取得
            GeoPoint center = mMapView.getMapCenter();
            url_string += "&lat=" + (double) center.getLatitudeE6() / E6;
            url_string += "&lon=" + (double) center.getLongitudeE6() / E6;

            String url = url_string;
            Utils.logging("url = " + url.toString());

            Double lat = (double) center.getLatitudeE6() / E6;
            Double lon = (double) center.getLongitudeE6() / E6;

            String url4all = "";

            // 価格登録のないガソリンスタンドの取得
            if (pref.getBoolean(
                    "settings_no_postdata",
                    Boolean.valueOf(resource.getText(
                            R.string.settings_no_postdata_default).toString()))) {
                int no_dist = Integer.valueOf(dist);
                if (no_dist > 10) {
                    no_dist = 10;
                }
                url4all = "http://api.gogo.gs/ap/gsst/ssLatLonFull110303.php?"
                        + "lat_min=" + (double) (lat - 0.0083 * no_dist)
                        + "&lat_max=" + (double) (lat + 0.0083 * no_dist)
                        + "&lon_min=" + (double) (lon - 0.0125 * no_dist)
                        + "&lon_max=" + (double) (lon + 0.0125 * no_dist)
                        + "&pm=" + pref.getString("settings_kind", "0")
                        + "&n=100";

                ArrayList<String> maker = new ArrayList<String>();

                for (int i = 0; i < brands.length; i++) {

                    if (pref.getBoolean("brand" + brands[i], true)) {
                        maker.add(brands[i]);
                    }
                }

                if (maker.size() > 0) {
                    url4all += "&maker=" + StringUtils.join(maker, ',');
                }
            }

            Utils.logging(url4all);

            TextView txtMessage = (TextView) findViewById(R.id.txt_message);
            txtMessage.setText(resource
                    .getText(R.string.dialog_message_getting_data));

            mMapView.getOverlays().clear();
            CenterCircleOverlay location = new CenterCircleOverlay(this);
            mMapView.getOverlays().add(location);

            // Overlayとして登録
            mMapView.getOverlays().add(overlay);

            // マップ中心の周辺にあるガソリンスタンド情報を取得する
            searchThread = new SearchThread(handler, this, url, url4all);
            searchThread.start();
        } catch (Exception e) {
            Utils.logging(e.getMessage());
        }
        return true;
    }

    /**
     * ローディングViewの表示
     */
    private void showLoadingView() {
        Animation animation = AnimationUtils.loadAnimation(this,
                R.anim.open_header);
        animation.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                // TODO 自動生成されたメソッド・スタブ

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO 自動生成されたメソッド・スタブ

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                layoutHeader2.setVisibility(View.VISIBLE);
            }
        });
        layoutHeader2.startAnimation(animation);
    }

    /**
     * ローディングViewの非表示
     */
    private void hideLoadingView() {
        Animation animation = AnimationUtils.loadAnimation(this,
                R.anim.close_header);
        animation.setStartOffset(2000);
        animation.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                // TODO 自動生成されたメソッド・スタブ

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO 自動生成されたメソッド・スタブ

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                layoutHeader2.setVisibility(View.INVISIBLE);
            }
        });
        layoutHeader2.startAnimation(animation);
    }

    @Override
    public void run() {
        // プログレスダイアログを閉じる
        dialog.dismiss();

        dtoStandList = searchThread.getGSInfoList();
        PinItemizedOverlay pinOverlay = null;

        DatabaseHelper dbHelper = new DatabaseHelper(MainActivity.this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        StandsDao standsDao = new StandsDao(db);
        standsDao.deleteAll();

        // 取得に失敗
        if (dtoStandList == null || dtoStandList.size() <= 0) {
            Toast.makeText(this,
                    resource.getText(R.string.dialog_message_out_of_range),
                    Toast.LENGTH_LONG).show();
        } else {

            Drawable speech = getResources().getDrawable(R.drawable.pbase);
            Drawable nodata = getResources().getDrawable(R.drawable.pnodata);

            String pin_type = PreferenceManager.getDefaultSharedPreferences(
                    this).getString("settings_pin_type", "price");
            ArrayList<PinItemizedOverlay> pins = new ArrayList<PinItemizedOverlay>();

            StandsHelper helper = StandsHelper.getInstance();

            db.beginTransaction();
            int dataSize = 0;

            try {
                SharedPreferences pref = PreferenceManager
                        .getDefaultSharedPreferences(MainActivity.this);

                HashMap<String, String> maker = new HashMap<String, String>();

                for (int i = 0; i < brands.length; i++) {
                    if (pref.getBoolean("brand" + brands[i], true)) {
                        maker.put(brands[i], brands_value[i]);
                    }
                }

                int size = dtoStandList.size();

                for (int i = 0; i < size; i++) {

                    Stand dto = dtoStandList.get(i);

                    // 24時間営業のGSかチェックする
                    if (pref.getBoolean("settings_rtc", false) == true
                            && dto.rtc.compareTo("24H") != 0) {
                        continue;
                    }

                    // 検索対象のブランドかチェックする
                    if (maker.containsValue(dto.brand) == false) {
                        continue;
                    }

                    dataSize++;

                    standsDao.insert(dto);

                    if (pin_type.compareTo("price") == 0) {
                        int price = Integer.parseInt(dto.price);
                        if (price == 9999) {
                            pinOverlay = new PinItemizedOverlay(nodata);
                        } else {
                            pinOverlay = new PinItemizedOverlay(speech);
                        }
                    } else {
                        pinOverlay = new PinItemizedOverlay(getResources()
                                .getDrawable(
                                        helper.getBrandImage(dto.brand,
                                                Integer.valueOf(dto.price))));
                    }

                    GeoPoint point = new GeoPoint(
                            (int) ((double) dto.latitude * E6),
                            (int) ((double) dto.longitude * E6));

                    pinOverlay.addPoint(point);
                    pinOverlay.setMsg(dto.shopName + "\n" + dto.brand + "\n"
                            + dto.address + "\n" + dto.price + "円");
                    pinOverlay.setPrice(dto.price);
                    pinOverlay.setPinType(pin_type);
                    pinOverlay.setStandData(dto);
                    pins.add(pinOverlay);

                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            mMapView.getOverlays().addAll(pins);
            mMapView.invalidate();

            TextView txtMessge = (TextView) findViewById(R.id.txt_message);
            txtMessge.setText(String.valueOf(dataSize) + "件のスタンドが見つかりました");

            hideLoadingView();
        }
        db.close();
    }

    public class PinItemizedOverlay extends ItemizedOverlay<PinOverlayItem>
            implements Runnable {

        private List<GeoPoint> points = new ArrayList<GeoPoint>();
        private List<String> msgs = new ArrayList<String>();
        private List<String> prices = new ArrayList<String>();
        private List<String> pinTypes = new ArrayList<String>();
        private List<Stand> standList = new ArrayList<Stand>();
        private int fontSize = 12;
        private int additionHeight = 12;

        public PinItemizedOverlay(Drawable defaultMarker) {
            super(boundCenterBottom(defaultMarker));

            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);

            fontSize *= metrics.density;
            additionHeight *= metrics.density;
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
            this.pinTypes.add(type);
        }

        public void setPrice(String title) {
            this.prices.add(title);
        }

        public void setStandData(Stand info) {
            this.standList.add(info);
        }

        /**
         * アイテムがタップされた時の処理
         */
        @Override
        protected boolean onTap(int index) {

            Stand info = standList.get(index);

            // イベントトラック（GSタップ）
            tracker.trackEvent("Main", // Category
                    "Stand", // Action
                    info.shopCode, // Label
                    0);

            Intent intent1 = new Intent(MainActivity.this, DetailActivity.class);
            intent1.putExtra("shopcode", info.shopCode);
            startActivity(intent1);
            return true;
        }

        @Override
        public void run() {
            // プログレスダイアログを閉じる
            dialog.dismiss();
        }

        @Override
        public void draw(Canvas canvas, MapView mapView, boolean shadow) {
            if (shadow) {
                return;
            }

            super.draw(canvas, mapView, shadow);

            Paint p = new Paint();

            // Draw point caption and its bounding rectangle
            p.setTextSize(fontSize);
            p.setColor(Color.BLUE);
            p.setAntiAlias(true);
            p.setTypeface(tf);

            int size = prices.size();

            for (int i = 0; i < size; i++) {

                String price = prices.get(i);
                String pin = pinTypes.get(i);

                if (pin.compareTo("brand") == 0) {
                    continue;
                }

                if (price.equals("9999")) {
                    continue;
                }

                GeoPoint locate = points.get(i);

                Point pt = new Point();
                mapView.getProjection().toPixels(locate, pt);

                int sw = (int) (p.measureText(price) + 0.5f);
                int sx = pt.x - sw / 2 - 1;
                int sy = pt.y - additionHeight;

                canvas.drawText(price, sx, sy, p);
            }

            return;
        }
    }

    public class PinOverlayItem extends OverlayItem {

        public PinOverlayItem(GeoPoint point) {
            super(point, "", "");
        }
    }
}
