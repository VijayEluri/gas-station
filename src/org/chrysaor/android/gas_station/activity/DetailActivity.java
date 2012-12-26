package org.chrysaor.android.gas_station.activity;

import org.chrysaor.android.gas_station.R;
import org.chrysaor.android.gas_station.lib.database.DatabaseHelper;
import org.chrysaor.android.gas_station.lib.database.FavoritesDao;
import org.chrysaor.android.gas_station.lib.database.StandsDao;
import org.chrysaor.android.gas_station.lib.dto.Stand;
import org.chrysaor.android.gas_station.util.DetailAsyncTask;
import org.chrysaor.android.gas_station.util.DetailTaskCallback;
import org.chrysaor.android.gas_station.util.ErrorReporter;
import org.chrysaor.android.gas_station.util.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Instrumentation;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
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

public class DetailActivity extends AbstractMyActivity implements
        DetailTaskCallback {

    private DatabaseHelper dbHelper = null;
    private SQLiteDatabase db = null;
    private StandsDao standsDao = null;
    public Stand info = null;
    private Integer favState = 0;
    private ImageButton favButton;
    private String ssId = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail);

        dbHelper = new DatabaseHelper(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String index = extras.getString("shopcode");
            ssId = index;

            new DetailAsyncTask(this, getLayoutInflater().inflate(
                    R.layout.loading, null), findViewById(R.id.layoutRoot),
                    this).execute(index);

            // db = dbHelper.getReadableDatabase();
            //
            // FavoritesDao favoritesDao = new FavoritesDao(db);
            // if (favoritesDao.findByShopCd(index) != null) {
            // setFavState(1);
            // }
            // db.close();
        }

        setHeaderView();

    }

    private void setHeaderView() {
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

        // その他
        Button btnOther = (Button) findViewById(R.id.btnOther);
        btnOther.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                KeyEventSender sender = new KeyEventSender();
                sender.execute(KeyEvent.KEYCODE_MENU);
            }
        });
    }

    private class KeyEventSender extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer... params) {
            int keyEvent = params[0];
            Instrumentation ist = new Instrumentation();
            ist.sendKeyDownUpSync(keyEvent);
            return null;
        }
    }

    private void setFavState(Integer state) {

        favState = state;
        switch (favState) {
        case 0:
            favButton.setImageDrawable(getResources().getDrawable(
                    R.drawable.star_empty));
            break;
        case 1:
            favButton.setImageDrawable(getResources().getDrawable(
                    R.drawable.star_full));
            break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // メニューアイテムを追加
        MenuItem item0 = menu.add(0, 0, 0, "ブラウザ");
        MenuItem item1 = menu.add(0, 1, 0, "共有");
        MenuItem item2 = menu.add(0, 2, 0, "設定");
        // MenuItem item4 = menu.add(0, 4, 0, "ブラウザ");

        // 追加したメニューアイテムのアイコンを設定
        item0.setIcon(android.R.drawable.ic_menu_view);
        item1.setIcon(android.R.drawable.ic_menu_share);
        item2.setIcon(android.R.drawable.ic_menu_preferences);
        // item4.setIcon(android.R.drawable.ic_menu_info_details);

        if (Utils.isDonate(this) == false) {
            MenuItem item3 = menu.add(0, 3, 0, "有料版購入");
            item3.setIcon(R.drawable.cart);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
        case 0:
            // イベントトラック（ブラウザ）
            tracker.trackEvent("Detail", "Browser", info.shopCode, 0);

            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("http://gogo.gs/shop/" + info.shopCode
                    + ".html"));
            startActivity(intent);
            break;
        case 1:
            try {
                String msg = PreferenceManager.getDefaultSharedPreferences(
                        DetailActivity.this).getString(
                        "settings_sharemsg",
                        getResources().getString(
                                R.string.settings_sharemsg_default));

                String[] kinds = getResources().getStringArray(
                        R.array.settings_list_kind);
                String kind = kinds[Integer.parseInt(PreferenceManager
                        .getDefaultSharedPreferences(this).getString(
                                "settings_kind", "0"))];

                if (info.price.equals("9999")) {
                    msg = msg.replaceAll("#price", info.getDispPrice());
                } else {
                    if (kind.equals("灯油")) {
                        msg = msg.replaceAll("#price", info.getDispPrice()
                                + "円/18L");
                    } else {
                        msg = msg.replaceAll("#price", info.getDispPrice()
                                + "円/L");
                    }
                }
                msg = msg.replaceAll("#shop_name", info.shopName).replaceAll(
                        "#kind", kind);

                // イベントトラック（共有）
                tracker.trackEvent("Detail", "Share", info.shopCode, 0);

                Intent intent2 = new Intent();
                intent2.setAction(Intent.ACTION_SEND);
                intent2.setType("text/plain");
                intent2.putExtra(Intent.EXTRA_TEXT, msg);
                startActivity(intent2);
            } catch (Exception e) {
                // Log.d(TAG, "Error");
            }
            break;
        case 2:
            // イベントトラック（設定）
            tracker.trackEvent("Detail", "Settings", "", 0);

            Intent intent3 = new Intent(DetailActivity.this,
                    SettingsActivity.class);
            startActivity(intent3);
            break;
        case 3:
            // イベントトラック（Donate）
            tracker.trackEvent("Detail", "Donate", "", 0);

            Intent intent4 = new Intent();
            intent4.setAction(Intent.ACTION_VIEW);
            intent4.setData(Uri
                    .parse("market://details?id=org.chrysaor.android.gas_station.plus"));
            startActivity(intent4);
            break;
        case 4:
            // イベントトラック（ブラウザ）
            tracker.trackEvent("Detail", "Browser", info.shopCode, 0);
            Intent intent5 = new Intent();
            intent5.setAction(Intent.ACTION_VIEW);
            intent5.setData(Uri.parse("http://gogo.gs/shop/" + info.shopCode
                    + ".html"));
            startActivity(intent5);
            break;

        }
        return false;
    }

    @Override
    public void onFailed(int errorCode) {
        Toast.makeText(this, "スタンド情報が取得できません", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onSuccess(Stand info) {
        this.info = info;

        setButtonEvent();
    }

    private void setButtonEvent() {
        Button btnRoute = (Button) findViewById(R.id.btnRoute);
        btnRoute.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // イベントトラック（ルート検索）
                tracker.trackEvent("Detail", "RouteSearch", info.shopCode, 0);

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setClassName("com.google.android.apps.maps",
                        "com.google.android.maps.MapsActivity");
                intent.setData(Uri
                        .parse("http://maps.google.com/maps?myl=saddr&daddr="
                                + info.latitude + "," + info.longitude
                                + "&dirflg=d"));
                startActivity(intent);

            }
        });

        Button btnPricePost = (Button) findViewById(R.id.btnPricePost);
        btnPricePost.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // イベントトラック（価格投稿）
                tracker.trackEvent("Detail", "Post", info.shopCode, 0);
                Intent intent = new Intent(getApplicationContext(),
                        PostActivity.class);
                intent.putExtra("shopcode", info.shopCode);
                startActivity(intent);

            }
        });

        // 給油記録
        Button btnFuelPost = (Button) findViewById(R.id.btnFuelPost);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.DONUT) {

            btnFuelPost.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // イベントトラック（給油記録）
                    tracker.trackEvent("Detail", "Charge", info.shopCode, 0);

                    if (Utils.installedGasLogFree(DetailActivity.this)
                            || Utils.installedGasLogPayment(DetailActivity.this)) {
                        Intent intent = new Intent();
                        if (Utils.installedGasLogPayment(DetailActivity.this)) {
                            intent.setClassName(
                                    "jp.pinetail.android.gas_log.payment",
                                    "jp.pinetail.android.gas_log.core.FuelPostActivity");
                        } else {
                            intent.setClassName(
                                    "jp.pinetail.android.gas_log.free",
                                    "jp.pinetail.android.gas_log.core.FuelPostActivity");
                        }
                        intent.putExtra("ssid", info.shopCode);
                        intent.putExtra("shop_name", info.shopName);
                        intent.putExtra("shop_brand", info.brand);
                        intent.putExtra("lat", info.latitude.toString());
                        intent.putExtra("lon", info.longitude);
                        startActivity(intent);
                    } else {
                        new AlertDialog.Builder(DetailActivity.this)
                                .setTitle("ガスログ！インストール")
                                .setMessage("給油記録にはガスログ！が必要です。インストールしますか？")
                                .setNeutralButton("インストール",
                                        new DialogInterface.OnClickListener() {

                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int which) {
                                                Intent intent = new Intent();
                                                intent.setAction(Intent.ACTION_VIEW);
                                                intent.setData(Uri
                                                        .parse("market://details?id=jp.pinetail.android.gas_log.free"));
                                                startActivity(intent);
                                            }
                                        })
                                .setNegativeButton("閉じる",
                                        new DialogInterface.OnClickListener() {

                                            @Override
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int which) {
                                            }
                                        }).create().show();

                    }
                }
            });
        } else {
            btnFuelPost.setVisibility(View.GONE);
        }

        // お気に入りボタン
        // favButton.setOnClickListener(new OnClickListener() {
        //
        // @Override
        // public void onClick(View v) {
        // db = dbHelper.getReadableDatabase();
        //
        // FavoritesDao favoritesDao = new FavoritesDao(db);
        //
        // switch (favState) {
        // case 0:
        // // 登録件数の確認
        // if (favoritesDao.findAll("create_date").size() >= 20) {
        // Toast.makeText(DetailActivity.this,
        // "お気に入りは20件までしか登録出来ません。", Toast.LENGTH_SHORT)
        // .show();
        // } else {
        // favoritesDao.insert(info);
        // setFavState(1);
        // Toast.makeText(DetailActivity.this, "お気に入りに登録しました",
        // Toast.LENGTH_SHORT).show();
        // }
        // break;
        // case 1:
        // favoritesDao.deleteByShopCd(info.shopCode);
        // setFavState(0);
        // Toast.makeText(DetailActivity.this, "お気に入りを解除しました",
        // Toast.LENGTH_SHORT).show();
        // break;
        // }
        // db.close();
        // }
        // });
    }
}
