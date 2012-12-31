package org.chrysaor.android.gas_station.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.chrysaor.android.gas_station.R;
import org.chrysaor.android.gas_station.lib.database.DatabaseHelper;
import org.chrysaor.android.gas_station.lib.database.FavoritesDao;
import org.chrysaor.android.gas_station.lib.database.StandsDao;
import org.chrysaor.android.gas_station.lib.dto.Stand;
import org.chrysaor.android.gas_station.util.AbstractLoadingTask;
import org.chrysaor.android.gas_station.util.LoadingManager;
import org.chrysaor.android.gas_station.util.StandAdapter;
import org.chrysaor.android.gas_station.util.Utils;
import org.chrysaor.android.gas_station.util.XmlParserFromUrl;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.ToggleButton;

public class ListActivity extends AbstractMyActivity {
    public ArrayList<Stand> dtoStandList = new ArrayList<Stand>();
    public StandAdapter<Stand> adapter = null;
    private static DatabaseHelper dbHelper = null;
    private SQLiteDatabase db = null;
    private StandsDao standsDao = null;
    private FavoritesDao favoritesDao = null;
    private static String modeSearchSort = "none";
    private static String modeFavoriteSort = "none";
    private SharedPreferences pref = null;
    private View mHeader = null;
    private View mFooter = null;
    private ListView mListView = null;
    private ToggleButton toggleList;
    private SearchListTask task;
    private LoadingManager loading;

    private static final int DISPLAY_MODE_SEARCH_LIST = 1;
    private static final int DISPLAY_MODE_FAVORITE_LIST = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);

        loading = new LoadingManager(getApplicationContext(),
                LoadingManager.MODE_PAGE);
        loading.setLoadingViews(
                getLayoutInflater().inflate(R.layout.loading, null),
                findViewById(R.id.layoutRoot));

        // 初期化
        pref = PreferenceManager.getDefaultSharedPreferences(ListActivity.this);
        modeSearchSort = pref.getString("settings_sort", getResources()
                .getString(R.string.settings_sort_default));
        modeFavoriteSort = pref.getString(
                "settings_favorite_sort",
                getResources().getString(
                        R.string.settings_favorite_sort_default));
        dbHelper = new DatabaseHelper(this);

        task = new SearchListTask(getApplicationContext(), getLayoutInflater()
                .inflate(R.layout.loading, null), findViewById(R.id.layoutRoot));

        // 検索結果の取得
        task.execute(DISPLAY_MODE_SEARCH_LIST);

        // ヘッダーViewの設定
        setHeaderView();

        setAdView();

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * リストViewの設定
     */
    private void setListView(ArrayList<Stand> standList, int mode) {
        dtoStandList = standList;

        adapter = new StandAdapter<Stand>(this, R.layout.list_row, dtoStandList);
        ListView mListView = getListView();
        View mHeaderView = getListHeaderView();

        if (mListView.getHeaderViewsCount() == 0) {
            mListView.addHeaderView(mHeaderView);
        }

        if (mode == DISPLAY_MODE_FAVORITE_LIST) {
            mListView.findViewById(R.id.layoutSearch).setVisibility(View.GONE);
            mListView.findViewById(R.id.layoutFavorite).setVisibility(
                    View.VISIBLE);
        } else {
            mListView.findViewById(R.id.layoutSearch).setVisibility(
                    View.VISIBLE);
            mListView.findViewById(R.id.layoutFavorite)
                    .setVisibility(View.GONE);
        }

        // フッターの設定
        if (mListView.getFooterViewsCount() == 0) {
            mListView.addFooterView(getListFooterView());
        }

        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapter, View view,
                    int position, long id) {
                final Stand item = (Stand) adapter.getItemAtPosition(position);

                // イベントトラック（詳細）
                tracker.trackEvent("List", "Detail", item.shopCode, 0);

                Intent intent1 = new Intent(getApplicationContext(),
                        DetailActivity.class);
                intent1.putExtra("shopcode", item.shopCode);
                startActivityForResult(intent1, 2);

            }
        });
    }

    private ListView getListView() {
        if (mListView == null) {
            mListView = (ListView) findViewById(R.id.savedList);
        }
        return mListView;
    }

    /**
     * ヘッダーViewの設定
     */
    private void setHeaderView() {
        toggleList = (ToggleButton) findViewById(R.id.toggleList);
        toggleList.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked) {
                if (isChecked) {
                    // イベントトラック
                    tracker.trackEvent("List", "ViewMode", "Favorite", 0);

                    loading.setMode(LoadingManager.MODE_PAGE);

                    if (task != null && task.getStatus() == Status.RUNNING) {
                        task.cancel(true);
                        task = null;
                    }

                    // お気に入り一覧の取得
                    task = new SearchListTask(
                            getApplicationContext(),
                            getLayoutInflater().inflate(R.layout.loading, null),
                            findViewById(R.id.layoutRoot));
                    task.execute(DISPLAY_MODE_FAVORITE_LIST);
                } else {
                    // イベントトラック
                    tracker.trackEvent("List", "ViewMode", "Search", 0);

                    loading.setMode(LoadingManager.MODE_PAGE);

                    if (task != null && task.getStatus() == Status.RUNNING) {
                        task.cancel(true);
                        task = null;
                    }

                    // 検索結果一覧の取得
                    task = new SearchListTask(
                            getApplicationContext(),
                            getLayoutInflater().inflate(R.layout.loading, null),
                            findViewById(R.id.layoutRoot));
                    task.execute(DISPLAY_MODE_SEARCH_LIST);
                }

            }
        });
    }

    /**
     * 検索結果一覧のヘッダーを取得
     * 
     * @return
     */
    private View getListHeaderView() {
        if (mHeader == null) {
            mHeader = getLayoutInflater().inflate(R.layout.list_header_search,
                    null);

            // 近い順
            RadioButton radioNearby = (RadioButton) mHeader
                    .findViewById(R.id.radioSortNearby);

            // 安い順
            RadioButton radioLowPrice = (RadioButton) mHeader
                    .findViewById(R.id.radioSortLowPrice);

            // 更新日時順
            RadioButton radioUpdateDate = (RadioButton) mHeader
                    .findViewById(R.id.radioSortUpdateDate);

            // 登録日順
            RadioButton radioCreateDate = (RadioButton) mHeader
                    .findViewById(R.id.radioSortCreateDate);

            // 更新日時順
            RadioButton radioShopName = (RadioButton) mHeader
                    .findViewById(R.id.radioSortShopName);

            if (modeSearchSort.contains("dist")) {
                radioNearby.setChecked(true);
            } else if (modeSearchSort.contains("price")) {
                radioLowPrice.setChecked(true);
            } else if (modeSearchSort.contains("date")) {
                radioUpdateDate.setChecked(true);
            }

            if (modeFavoriteSort.contains("create_date")) {
                radioCreateDate.setChecked(true);
            } else if (modeFavoriteSort.contains("shop_name")) {
                radioShopName.setChecked(true);
            }

            radioCreateDate
                    .setOnCheckedChangeListener(favoriteSortOnCheckedChangeListener);
            radioShopName
                    .setOnCheckedChangeListener(favoriteSortOnCheckedChangeListener);

            radioNearby
                    .setOnCheckedChangeListener(searchSortOnCheckedChangeListener);
            radioLowPrice
                    .setOnCheckedChangeListener(searchSortOnCheckedChangeListener);
            radioUpdateDate
                    .setOnCheckedChangeListener(searchSortOnCheckedChangeListener);

        }
        return mHeader;
    }

    private View getListFooterView() {
        if (mFooter == null) {
            mFooter = getLayoutInflater().inflate(R.layout.list_footer, null);

        }
        return mFooter;
    }

    /**
     * 検索結果並び順リスナー
     */
    OnCheckedChangeListener searchSortOnCheckedChangeListener = new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                boolean isChecked) {
            if (isChecked) {

                if (buttonView.getId() == R.id.radioSortNearby) {
                    modeSearchSort = "dist";
                } else if (buttonView.getId() == R.id.radioSortLowPrice) {
                    modeSearchSort = "price";
                } else if (buttonView.getId() == R.id.radioSortUpdateDate) {
                    modeSearchSort = "date";
                }

                loading.setMode(LoadingManager.MODE_NONE);

                task = new SearchListTask(getApplicationContext(),
                        getLayoutInflater().inflate(R.layout.loading, null),
                        findViewById(R.id.layoutRoot));
                task.execute(DISPLAY_MODE_SEARCH_LIST);

                // イベントトラック（並び順）
                tracker.trackEvent("List", "SearchSort", modeSearchSort, 0);
            }
        }
    };

    /**
     * お気に入り並び順リスナー
     */
    OnCheckedChangeListener favoriteSortOnCheckedChangeListener = new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                boolean isChecked) {
            if (isChecked) {

                if (buttonView.getId() == R.id.radioSortCreateDate) {
                    modeFavoriteSort = "create_date";
                } else if (buttonView.getId() == R.id.radioSortShopName) {
                    modeFavoriteSort = "shop_name";
                }

                loading.setMode(LoadingManager.MODE_NONE);

                task = new SearchListTask(getApplicationContext(),
                        getLayoutInflater().inflate(R.layout.loading, null),
                        findViewById(R.id.layoutRoot));
                task.execute(DISPLAY_MODE_FAVORITE_LIST);

                // イベントトラック（並び順）
                tracker.trackEvent("List", "FavoriteSort", modeFavoriteSort, 0);
            }
        }
    };

    private void invisibleHeader() {
        if (mHeader != null) {
            getListView().removeHeaderView(mHeader);
        }
    }

    private class SearchListTask extends
            AbstractLoadingTask<Integer, Void, ArrayList<Stand>> {

        /** 表示モード */
        private int displayMode;

        public SearchListTask(Context context, View viewLoading, View viewMain) {
            super(context, viewLoading, viewMain);

        }

        @Override
        protected void onPreExecute() {
            if (loading.isShowing() == false) {
                loading.showLoading();
            }
        }

        @Override
        protected ArrayList<Stand> doInBackground(Integer... params) {

            ArrayList<Stand> dtoStandList = new ArrayList<Stand>();
            displayMode = params[0];

            switch (displayMode) {
            case DISPLAY_MODE_SEARCH_LIST:
                dtoStandList = getSearchList();
                break;

            case DISPLAY_MODE_FAVORITE_LIST:
                dtoStandList = getFavoriteList();
                break;
            default:
                break;
            }

            return dtoStandList;
        }

        protected void onPostExecute(ArrayList<Stand> result) {

            loading.hideLoading();

            setListView(result, displayMode);

        }

        @Override
        protected void onCancelled() {
            super.onCancelled();

            if (loading.isShowing()) {
                loading.hideLoading();
            }
        }

        /**
         * 検索結果の取得
         */
        private ArrayList<Stand> getSearchList() {
            ArrayList<Stand> dtoStandList = new ArrayList<Stand>();

            db = dbHelper.getReadableDatabase();
            standsDao = new StandsDao(db);
            dtoStandList = standsDao.findAll(modeSearchSort);
            db.close();

            return dtoStandList;
        }

        private ArrayList<Stand> getFavoriteList() {

            db = dbHelper.getReadableDatabase();
            favoritesDao = new FavoritesDao(db);
            ArrayList<Stand> favoriteStandList = favoritesDao
                    .findAll(modeFavoriteSort);
            db.close();

            Utils.logging(String.valueOf(favoriteStandList.size()));
            if (favoriteStandList.size() > 0) {
                try {
                    String sids = "";
                    for (int i = 0; i < favoriteStandList.size(); i++) {
                        Stand item = favoriteStandList.get(i);

                        sids += item.shopCode;

                        if (i < favoriteStandList.size() - 1) {
                            sids += ",";
                        }
                    }

                    Boolean member = app.getMember();

                    XmlParserFromUrl xml = new XmlParserFromUrl();
                    String url = "http://api.gogo.gs/ap/gsst/ssShopIdCsv.php?kind="
                            + app.getKind()
                            + "&member="
                            + ((member == true) ? 1 : 0) + "&sids=" + sids;
                    Utils.logging(url);
                    byte[] byteArray = Utils.getByteArrayFromURL(url, "GET");
                    if (byteArray == null) {
                        return favoriteStandList;
                    }
                    String data = new String(byteArray);

                    db = dbHelper.getReadableDatabase();
                    favoritesDao = new FavoritesDao(db);
                    // トランザクション開始
                    db.beginTransaction();

                    HashMap<String, HashMap<String, String>> res = xml
                            .getShopPrices4Fav(data);

                    for (Stand item : favoriteStandList) {

                        if (res.containsKey(item.shopCode)) {
                            HashMap<String, String> resItem = res
                                    .get(item.shopCode);
                            // 最新の価格を取得する。
                            Date date = new Date(TimeUnit.SECONDS.toMillis(Long
                                    .valueOf(resItem.get("date"))));
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                                    "yyyy/MM/dd");
                            Utils.logging(simpleDateFormat.format(date));

                            item.price = resItem.get("price");
                            item.date = simpleDateFormat.format(date);

                        } else {
                            // 最新の価格を取得する。
                            item.price = "9999";
                            item.date = "";
                        }

                        item.member = member;

                        favoritesDao.update(item);
                    }

                    db.setTransactionSuccessful();

                    return favoriteStandList;
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (db != null && db.isOpen()) {
                        db.endTransaction();
                        db.close();
                    }
                }
            }

            return favoriteStandList;
        }

    }
}
