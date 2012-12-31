package org.chrysaor.android.gas_station;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang.StringUtils;
import org.chrysaor.android.gas_station.activity.AboutActivity;
import org.chrysaor.android.gas_station.activity.AbstractMyMapActivity;
import org.chrysaor.android.gas_station.activity.DetailActivity;
import org.chrysaor.android.gas_station.activity.ListActivity;
import org.chrysaor.android.gas_station.activity.SettingsActivity;
import org.chrysaor.android.gas_station.lib.database.DatabaseHelper;
import org.chrysaor.android.gas_station.lib.database.StandsDao;
import org.chrysaor.android.gas_station.lib.dto.Stand;
import org.chrysaor.android.gas_station.util.CenterCircleOverlay;
import org.chrysaor.android.gas_station.util.LocationOverlay;
import org.chrysaor.android.gas_station.util.MultiDirectionSlidingDrawer;
import org.chrysaor.android.gas_station.util.MultiDirectionSlidingDrawer.OnDrawerCloseListener;
import org.chrysaor.android.gas_station.util.MultiDirectionSlidingDrawer.OnDrawerOpenListener;
import org.chrysaor.android.gas_station.util.SearchThread;
import org.chrysaor.android.gas_station.util.StandsHelper;
import org.chrysaor.android.gas_station.util.Utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewStub;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.RadioButton;
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
    private boolean slideEditFlg = false;

    /** ガソリンスタンド検索スレッド */
    private SearchThread searchThread;

    private final Handler handler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        gestureDetector = new GestureDetector(this, simpleOnGestureListener);
        sp = PreferenceManager.getDefaultSharedPreferences(this);

        // ダイアログの初期化
        dialog = new ProgressDialog(this);

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        display = wm.getDefaultDisplay();

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
        mMapView.setBuiltInZoomControls(false);

        mMapController = mMapView.getController();

        CenterCircleOverlay location = new CenterCircleOverlay(this);
        mMapView.getOverlays().add(location);

        // 今回の主役。有効にすることでGPSの取得が可能に
        overlay = new LocationOverlay(getApplicationContext(), mMapView);
        overlay.enableMyLocation();
        overlay.enableCompass();
        // overlay.setTraceToggle(toggle);

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

        // ヘッダーViewの設定
        setHeaderView();

        // フッターViewの設定
        setFooterView();

        // 広告Viewの設定
        setAdView();

        // Overlayとして登録
        mMapView.getOverlays().add(overlay);
        mMapView.invalidate();

    }

    /**
     * ヘッダーViewの設定
     */
    private void setHeaderView() {

        // リストボタンのonClick設定
        Button btnList = (Button) findViewById(R.id.btnList);
        btnList.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // イベントトラック（リスト）
                tracker.trackEvent("Main", "List", null, 0);

                Intent intent = new Intent(getApplicationContext(),
                        ListActivity.class);
                startActivityForResult(intent, 0);
            }
        });

        // 設定ボタン
        Button btnSetting = (Button) findViewById(R.id.btnSetting);
        btnSetting.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),
                        SettingsActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.push_right_in,
                        R.anim.push_right_out);
            }
        });
    }

    /**
     * スライドViewの設定
     */
    private void setSlideView() {

        MultiDirectionSlidingDrawer slidingDrawer = (MultiDirectionSlidingDrawer) findViewById(R.id.drawer);
        slidingDrawer.setOnDrawerCloseListener(new OnDrawerCloseListener() {

            @Override
            public void onDrawerClosed() {
                if (slideEditFlg) {
                    searchAction();
                }
            }
        });
        slidingDrawer.setOnDrawerOpenListener(new OnDrawerOpenListener() {

            @Override
            public void onDrawerOpened() {
                slideEditFlg = false;

                LinearLayout layoutDonate = (LinearLayout) findViewById(R.id.layoutDonate);
                if (Utils.isDonate(getApplicationContext()) == false) {
                    layoutDonate.setVisibility(View.VISIBLE);
                } else {
                    layoutDonate.setVisibility(View.GONE);
                }
            }
        });

        // レギュラー
        RadioButton radioRegular = (RadioButton) findViewById(R.id.radioRegular);

        // ハイオク
        RadioButton radioHighoc = (RadioButton) findViewById(R.id.radioHighoc);

        // 軽油
        RadioButton radioDiesel = (RadioButton) findViewById(R.id.radioDiesel);

        // 灯油
        RadioButton radioLamp = (RadioButton) findViewById(R.id.radioLamp);

        int kind = Integer.parseInt(app.getKind());
        switch (kind) {
        case 0:
            radioRegular.setChecked(true);
            break;
        case 1:
            radioHighoc.setChecked(true);
            break;
        case 2:
            radioDiesel.setChecked(true);
            break;
        case 3:
            radioLamp.setChecked(true);
            break;
        default:
            break;
        }

        // 5km
        RadioButton radioDistance5km = (RadioButton) findViewById(R.id.radioDistance5km);

        // 10km
        RadioButton radioDistance10km = (RadioButton) findViewById(R.id.radioDistance10km);

        // 25km
        RadioButton radioDistance25km = (RadioButton) findViewById(R.id.radioDistance25km);

        // 灯油
        RadioButton radioDistance50km = (RadioButton) findViewById(R.id.radioDistance50km);

        int distance = Integer.parseInt(app.getDistance());
        switch (distance) {
        case 5:
            radioDistance5km.setChecked(true);
            break;
        case 10:
            radioDistance10km.setChecked(true);
            break;
        case 25:
            radioDistance25km.setChecked(true);
            break;
        case 50:
            radioDistance50km.setChecked(true);
            break;
        default:
            break;
        }

        // 24時間営業
        ToggleButton toggle24h = (ToggleButton) findViewById(R.id.toggle24h);
        toggle24h.setChecked(app.get24h());
        toggle24h.setTextOff("");
        toggle24h.setTextOn("");
        toggle24h.setText("");

        // セルフ
        ToggleButton toggleSelf = (ToggleButton) findViewById(R.id.toggleSelf);
        toggleSelf.setTextOff("");
        toggleSelf.setTextOn("");
        toggleSelf.setText("");

        // 会員価格
        ToggleButton toggleMember = (ToggleButton) findViewById(R.id.toggleMember);
        toggleMember.setChecked(app.getMember());
        toggleMember.setTextOff("");
        toggleMember.setTextOn("");
        toggleMember.setText("");

        // 価格のないスタンド
        ToggleButton toggleNoData = (ToggleButton) findViewById(R.id.toggleNoData);
        toggleNoData.setChecked(app.getNoData());
        toggleNoData.setTextOff("");
        toggleNoData.setTextOn("");
        toggleNoData.setText("");

        if (Utils.isDonate(getApplicationContext()) == false) {
            Button btnDonate = (Button) findViewById(R.id.btnDonate);
            btnDonate.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setData(Uri
                            .parse("market://details?id=org.chrysaor.android.gas_station.plus"));
                    startActivity(intent);
                }
            });
            return;
        }

        radioRegular
                .setOnCheckedChangeListener(radioGroupKindOnCheckedChangeListener);
        radioHighoc
                .setOnCheckedChangeListener(radioGroupKindOnCheckedChangeListener);
        radioDiesel
                .setOnCheckedChangeListener(radioGroupKindOnCheckedChangeListener);
        radioLamp
                .setOnCheckedChangeListener(radioGroupKindOnCheckedChangeListener);

        radioDistance5km
                .setOnCheckedChangeListener(radioGroupDistanceOnCheckedChangeListener);
        radioDistance10km
                .setOnCheckedChangeListener(radioGroupDistanceOnCheckedChangeListener);
        radioDistance25km
                .setOnCheckedChangeListener(radioGroupDistanceOnCheckedChangeListener);
        radioDistance50km
                .setOnCheckedChangeListener(radioGroupDistanceOnCheckedChangeListener);

        toggle24h.setOnCheckedChangeListener(toggleOnCheckedChangeListener);
        toggleSelf.setOnCheckedChangeListener(toggleOnCheckedChangeListener);
        toggleMember.setOnCheckedChangeListener(toggleOnCheckedChangeListener);
        toggleNoData.setOnCheckedChangeListener(toggleOnCheckedChangeListener);
    }

    OnCheckedChangeListener toggleOnCheckedChangeListener = new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                boolean isChecked) {

            if (isChecked) {
                switch (buttonView.getId()) {
                case R.id.toggle24h:
                    app.set24h(true);
                    break;
                case R.id.toggleSelf:
                    app.setSelf(true);
                    break;
                case R.id.toggleMember:
                    app.setMember(true);
                    break;
                case R.id.toggleNoData:
                    app.setNoData(true);
                    break;
                default:
                    break;
                }
            } else {
                switch (buttonView.getId()) {
                case R.id.toggle24h:
                    app.set24h(false);
                    break;
                case R.id.toggleSelf:
                    app.setSelf(false);
                    break;
                case R.id.toggleMember:
                    app.setMember(false);
                    break;
                case R.id.toggleNoData:
                    app.setNoData(false);
                    break;
                default:
                    break;
                }
            }

            slideEditFlg = true;
        }
    };

    OnCheckedChangeListener radioGroupKindOnCheckedChangeListener = new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                boolean isChecked) {
            if (isChecked) {
                if (buttonView.getId() == R.id.radioRegular) {
                    app.setKind("0");
                } else if (buttonView.getId() == R.id.radioHighoc) {
                    app.setKind("1");
                } else if (buttonView.getId() == R.id.radioDiesel) {
                    app.setKind("2");
                } else if (buttonView.getId() == R.id.radioLamp) {
                    app.setKind("3");
                }
            }

            slideEditFlg = true;
        }
    };

    OnCheckedChangeListener radioGroupDistanceOnCheckedChangeListener = new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                boolean isChecked) {
            if (isChecked) {
                if (buttonView.getId() == R.id.radioDistance5km) {
                    app.setDistance("5");
                } else if (buttonView.getId() == R.id.radioDistance10km) {
                    app.setDistance("10");
                } else if (buttonView.getId() == R.id.radioDistance25km) {
                    app.setDistance("25");
                } else if (buttonView.getId() == R.id.radioDistance50km) {
                    app.setDistance("50");
                }
            }

            slideEditFlg = true;
        }
    };

    /**
     * フッターViewの設定
     */
    private void setFooterView() {
        // 検索ボタンのonClick設定
        Button btnSearch = (Button) findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                searchAction();
            }
        });

        // ズームアウトボタンのonClick設定
        Button btnZoomOut = (Button) findViewById(R.id.btnZoomOut);
        btnZoomOut.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mMapController.zoomOut();
            }
        });

        // ズームインボタンのonClick設定
        Button btnZoomIn = (Button) findViewById(R.id.btnZoomIn);
        btnZoomIn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mMapController.zoomIn();
            }
        });

        // 現在地ボタンのonClick設定
        Button btnLocation = (Button) findViewById(R.id.btnLocation);
        btnLocation.setOnClickListener(new OnClickListener() {

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

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);

        // switch (event.getAction()) {
        // case MotionEvent.ACTION_UP:
        // if (lastEvent == "onFling" || lastEvent == "onScroll") {
        // setTimer();
        // } else {
        // if (mTimer != null) {
        // mTimer.cancel();
        // }
        // }
        // break;
        // default:
        // if (mTimer != null) {
        // mTimer.cancel();
        // }
        // break;
        // }
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

        sp = PreferenceManager.getDefaultSharedPreferences(this);

        overlay.enableMyLocation();

        // Donateの確認
        donate = Utils.isDonate(getApplicationContext());

        setSlideView();

    }

    @Override
    protected void onPause() {
        super.onPause();

        overlay.disableMyLocation();
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
        MenuItem item3 = menu.add(0, 3, 0, "アプリについて");
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

        try {
            SharedPreferences pref = PreferenceManager
                    .getDefaultSharedPreferences(MainActivity.this);
            String dist = app.getDistance();
            resource = getResources();

            String urlString = "http://api.gogo.gs/v1.2/?apid=gsearcho0o0";
            urlString += "&dist=" + dist;
            urlString += "&num=" + pref.getString("settings_num", "60");
            urlString += "&span=" + pref.getString("settings_span", "");
            Boolean member = app.getMember();
            if (member == true) {
                urlString += "&member=1";
            }
            urlString += "&kind=" + app.getKind();

            String sort = pref.getString("settings_sort", "dist");
            if (sort.equals("dist")) {
                urlString += "&sort=d";
            }

            // イベントトラック（検索）
            tracker.trackEvent("Main", "Search", urlString, 0);

            // 地図の中心位置を取得
            GeoPoint center = mMapView.getMapCenter();
            urlString += "&lat=" + (double) center.getLatitudeE6() / E6;
            urlString += "&lon=" + (double) center.getLongitudeE6() / E6;

            String url = urlString;
            Utils.logging("url = " + url.toString());

            Double lat = (double) center.getLatitudeE6() / E6;
            Double lon = (double) center.getLongitudeE6() / E6;

            String url4all = "";

            // 価格登録のないガソリンスタンドの取得
            if (app.getNoData()) {
                int no_dist = Integer.valueOf(dist);
                if (no_dist > 10) {
                    no_dist = 10;
                }
                url4all = "http://api.gogo.gs/ap/gsst/ssLatLonFull110303.php?"
                        + "lat_min=" + (double) (lat - 0.0083 * no_dist)
                        + "&lat_max=" + (double) (lat + 0.0083 * no_dist)
                        + "&lon_min=" + (double) (lon - 0.0125 * no_dist)
                        + "&lon_max=" + (double) (lon + 0.0125 * no_dist)
                        + "&pm=" + app.getKind() + "&n=100";

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
                    if (app.get24h() == true && dto.rtc.compareTo("24H") != 0) {
                        continue;
                    }

                    // セルフのGSかチェック
                    if (app.getSelf() == true
                            && dto.self.compareTo("SELF") != 0) {
                        continue;
                    }

                    // 検索対象のブランドかチェックする
                    if (maker.containsValue(dto.brand) == false) {
                        continue;
                    }

                    dataSize++;

                    standsDao.insert(dto);

                    pinOverlay = new PinItemizedOverlay(getResources()
                            .getDrawable(
                                    helper.getPinImage(dto.brand,
                                            Integer.valueOf(dto.price))));

                    GeoPoint point = new GeoPoint(
                            (int) ((double) dto.latitude * E6),
                            (int) ((double) dto.longitude * E6));

                    pinOverlay.addPoint(point);
                    pinOverlay.setMsg(dto.shopName + "\n" + dto.brand + "\n"
                            + dto.address + "\n" + dto.price + "円");
                    pinOverlay.setPrice(dto.price);
                    pinOverlay.setStandData(dto);
                    pins.add(pinOverlay);

                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            mMapView.getOverlays().addAll(pins);
            mMapView.invalidate();

            Toast.makeText(getApplicationContext(),
                    String.valueOf(dataSize) + "件のスタンドが見つかりました",
                    Toast.LENGTH_SHORT).show();
        }

        db.close();

        TextView txtMessage = (TextView) findViewById(R.id.txt_message);
        txtMessage.setText("");
    }

    public class PinItemizedOverlay extends ItemizedOverlay<PinOverlayItem>
            implements Runnable {

        private List<GeoPoint> points = new ArrayList<GeoPoint>();
        private List<String> msgs = new ArrayList<String>();
        private List<String> prices = new ArrayList<String>();
        private List<Stand> standList = new ArrayList<Stand>();
        private int fontSize = 12;
        private int additionHeight = 11;

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
            tracker.trackEvent("Main", "Stand", info.shopCode, 0);

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
            p.setColor(Color.YELLOW);
            p.setAntiAlias(true);
            p.setTypeface(tf);

            int size = prices.size();

            for (int i = 0; i < size; i++) {

                String price = prices.get(i);

                if (price.contains("9999")) {
                    price = "---";
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
