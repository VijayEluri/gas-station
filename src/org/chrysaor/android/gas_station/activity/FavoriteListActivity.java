package org.chrysaor.android.gas_station.activity;

import java.util.ArrayList;

import org.chrysaor.android.gas_station.R;
import org.chrysaor.android.gas_station.lib.database.DatabaseHelper;
import org.chrysaor.android.gas_station.lib.database.FavoritesDao;
import org.chrysaor.android.gas_station.lib.dto.Stand;
import org.chrysaor.android.gas_station.util.StandAdapter;
import org.chrysaor.android.gas_station.util.UpdateFavoritesService;
import org.chrysaor.android.gas_station.util.Utils;

import yanzm.products.quickaction.lib.ActionItem;
import yanzm.products.quickaction.lib.QuickAction;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.AdapterView.OnItemClickListener;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class FavoriteListActivity extends AbstractMyActivity {
    private ArrayList<Stand> list = null;
    private StandAdapter adapter = null;
    private DatabaseHelper dbHelper = null;
    private SQLiteDatabase db = null;
    private FavoritesDao favoritesDao = null;
    private static String mode = "none";
    private SharedPreferences pref = null;
    public QuickAction qa;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favorite_list);

        // 初期化
        pref = PreferenceManager
                .getDefaultSharedPreferences(FavoriteListActivity.this);
        mode = pref.getString("settings_favorite_sort", getResources()
                .getString(R.string.settings_favorite_sort_default));
        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getReadableDatabase();
        Utils.logging(mode);
        favoritesDao = new FavoritesDao(db);
        list = favoritesDao.findAll(mode);
        db.close();
        init();

        // レシーバを登録
        UpdateReceiver receiver = new UpdateReceiver();
        IntentFilter filter = new IntentFilter(
                UpdateFavoritesService.INTENT_ACTION);
        registerReceiver(receiver, filter);

        // 登録順
        RadioButton createDateButton = (RadioButton) findViewById(R.id.sort_create_date);
        createDateButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                db = dbHelper.getReadableDatabase();
                favoritesDao = new FavoritesDao(db);
                mode = "create_date";
                list = favoritesDao.findAll(mode);
                db.close();
                init();

                // イベントトラック（並び順）
                tracker.trackEvent("FavoriteList", // Category
                        "Sort", // Action
                        "CreateData", // Label
                        0);
            }
        });

        // 名前順
        RadioButton shopNameButton = (RadioButton) findViewById(R.id.sort_shop_name);
        shopNameButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                db = dbHelper.getReadableDatabase();
                favoritesDao = new FavoritesDao(db);
                mode = "shop_name";
                list = favoritesDao.findAll(mode);
                db.close();
                init();

                // イベントトラック（並び順）
                tracker.trackEvent("FavoriteList", // Category
                        "Sort", // Action
                        "ShopName", // Label
                        0);

            }
        });

        if (mode.equals("create_date")) {
            createDateButton.setChecked(true);
        } else {
            shopNameButton.setChecked(true);
        }

        if (Utils.isDonate(this)) {
            LinearLayout head = (LinearLayout) findViewById(R.id.header_ad);
            head.setVisibility(View.GONE);
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
                public void onItemClick(AdapterView<?> adapter, View view,
                        int position, long id) {
                    final Stand gasStandDto = list.get(position);

                    // イベントトラック（GSタップ）
                    tracker.trackEvent("List", // Category
                            "Stand", // Action
                            gasStandDto.shopCode, // Label
                            0);

                    ActionItem item1 = new ActionItem();
                    item1.setTitle("地図");
                    item1.setIcon(getResources().getDrawable(
                            R.drawable.map_blue));
                    item1.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            // イベントトラック（地図）
                            tracker.trackEvent("List", // Category
                                    "Map", // Action
                                    gasStandDto.shopCode, // Label
                                    0);

                            Intent intent = new Intent();
                            intent.putExtra("lat", gasStandDto.latitude);
                            intent.putExtra("lon", gasStandDto.longitude);
                            setResult(Activity.RESULT_OK, intent);
                            // アクティビティの終了
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
                            tracker.trackEvent("List", "Detail",
                                    gasStandDto.shopCode, 0);

                            Intent intent1 = new Intent(
                                    FavoriteListActivity.this,
                                    DetailActivity.class);
                            intent1.putExtra("shopcode", gasStandDto.shopCode);
                            intent1.putExtra("from", "FavoriteListActivity");
                            startActivityForResult(intent1, 0);
                        }
                    });

                    ActionItem item3 = new ActionItem();
                    item3.setTitle("ルート検索");
                    item3.setIcon(getResources().getDrawable(
                            R.drawable.green_flag));
                    item3.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            // イベントトラック（ルート検索）
                            tracker.trackEvent("List", "RouteSearch",
                                    gasStandDto.shopCode, 0);

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            intent.setClassName("com.google.android.apps.maps",
                                    "com.google.android.maps.MapsActivity");
                            intent.setData(Uri
                                    .parse("http://maps.google.com/maps?myl=saddr&daddr="
                                            + gasStandDto.address + "&dirflg=d"));
                            startActivity(intent);
                        }
                    });

                    ActionItem item4 = new ActionItem();
                    item4.setTitle("価格投稿");
                    item4.setIcon(getResources().getDrawable(
                            R.drawable.yen_currency_sign));
                    item4.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            // イベントトラック（価格投稿）
                            tracker.trackEvent("List", // Category
                                    "Post", // Action
                                    gasStandDto.shopCode, // Label
                                    0);

                            Intent intent = new Intent(
                                    FavoriteListActivity.this,
                                    PostActivity.class);
                            intent.putExtra("shopcode", gasStandDto.shopCode);
                            intent.putExtra("from", "FavoriteListActivity");
                            startActivityForResult(intent, 0);
                        }
                    });
                    ActionItem item5 = new ActionItem();
                    item5.setTitle("給油記録");
                    item5.setIcon(getResources().getDrawable(R.drawable.pencil));
                    item5.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            // イベントトラック（給油記録）
                            tracker.trackEvent("Detail", "Charge",
                                    gasStandDto.shopCode, 0);

                            if (Utils
                                    .installedGasLogFree(FavoriteListActivity.this)
                                    || Utils.installedGasLogPayment(FavoriteListActivity.this)) {
                                Intent intent = new Intent();
                                if (Utils
                                        .installedGasLogPayment(FavoriteListActivity.this)) {
                                    intent.setClassName(
                                            "jp.pinetail.android.gas_log.payment",
                                            "jp.pinetail.android.gas_log.core.FuelPostActivity");
                                } else {
                                    intent.setClassName(
                                            "jp.pinetail.android.gas_log.free",
                                            "jp.pinetail.android.gas_log.core.FuelPostActivity");
                                }
                                intent.putExtra("ssid", gasStandDto.shopCode);
                                intent.putExtra("shop_name",
                                        gasStandDto.shopName);
                                intent.putExtra("shop_brand", gasStandDto.brand);
                                intent.putExtra("lat",
                                        gasStandDto.latitude.toString());
                                intent.putExtra("lon", gasStandDto.longitude);
                                startActivity(intent);
                            } else {
                                new AlertDialog.Builder(
                                        FavoriteListActivity.this)
                                        .setTitle("ガスログ！インストール")
                                        .setMessage(
                                                "給油記録にはガスログ！が必要です。インストールしますか？")
                                        .setNeutralButton(
                                                "インストール",
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
                                        .setNegativeButton(
                                                "閉じる",
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

                    qa = new QuickAction(view);
                    // onCreate()の中で作ったActionItemをセットする
                    qa.addActionItem(item1);
                    qa.addActionItem(item2);
                    if (Utils.isDonate(FavoriteListActivity.this)) {
                        qa.addActionItem(item3);
                    }
                    qa.addActionItem(item4);
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.DONUT) {
                        qa.addActionItem(item5);
                    }
                    // アニメーションを設定する
                    qa.setAnimStyle(QuickAction.ANIM_AUTO);
                    qa.show();
                }
            });
        }
    }

    protected void onActivityResult(int requestCode, int resultCode,
            Intent intent) {

        if (qa != null) {
            qa.dismiss();
        }

        Utils.logging("result");
        if (requestCode == 0 && resultCode == RESULT_OK) {
            // お気に入り一覧描画
            db = dbHelper.getReadableDatabase();
            Utils.logging(mode);
            favoritesDao = new FavoritesDao(db);
            list = favoritesDao.findAll(mode);
            db.close();
            init();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // メニューアイテムを追加
        MenuItem item1 = menu.add(0, 0, 0, "手動更新");
        MenuItem item2 = menu.add(0, 1, 0, "設定");
        // 追加したメニューアイテムのアイコンを設定
        item1.setIcon(android.R.drawable.ic_menu_rotate);
        item2.setIcon(android.R.drawable.ic_menu_preferences);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
        case 0:
            Intent service = new Intent(this, UpdateFavoritesService.class);
            service.setAction(UpdateFavoritesService.START_ACTION);
            service.putExtra("mode", "all");
            service.putExtra("msg", true);
            service.putExtra("redraw", true);
            startService(service);
            return true;
        case 1:
            // イベントトラック（設定）
            tracker.trackEvent("FavoriteListActivity", "Settings", null, 0);

            Intent intent = new Intent(FavoriteListActivity.this,
                    SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return false;
    }

    private class UpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // お気に入り一覧描画
            db = dbHelper.getReadableDatabase();
            Utils.logging(mode);
            favoritesDao = new FavoritesDao(db);
            list = favoritesDao.findAll(mode);
            db.close();
            init();
        }

    }

}
