package org.chrysaor.android.gas_station.activity;

import java.util.ArrayList;

import org.chrysaor.android.gas_station.R;
import org.chrysaor.android.gas_station.lib.database.DatabaseHelper;
import org.chrysaor.android.gas_station.lib.database.StandsDao;
import org.chrysaor.android.gas_station.lib.dto.Stand;
import org.chrysaor.android.gas_station.util.StandAdapter;
import org.chrysaor.android.gas_station.util.Utils;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;

public class ListActivity extends AbstractMyActivity {
    public ArrayList<Stand> dtoStandList = new ArrayList<Stand>();
    public StandAdapter<Stand> adapter = null;
    private DatabaseHelper dbHelper = null;
    private SQLiteDatabase db = null;
    private StandsDao standsDao = null;
    private static String mode = "none";
    private SharedPreferences pref = null;
    private View mHeader = null;
    private View mFooter = null;
    private ListView mListView = null;
    private Handler mHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);

        // 初期化
        pref = PreferenceManager.getDefaultSharedPreferences(ListActivity.this);
        mode = pref.getString("settings_sort",
                getResources().getString(R.string.settings_sort_default));
        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getReadableDatabase();
        standsDao = new StandsDao(db);
        dtoStandList = standsDao.findAll(mode);
        db.close();
        init();

        // if (mode.contains("dist")) {
        // RadioButton radioNearby = (RadioButton)
        // findViewById(R.id.radioSortNearby);
        // radioNearby.setChecked(true);
        // } else if (mode.contains("price")) {
        // RadioButton radioLowPrice = (RadioButton)
        // findViewById(R.id.radioSortLowPrice);
        // radioLowPrice.setChecked(true);
        // } else if (mode.contains("date")) {
        // RadioButton radioUpdateDate = (RadioButton)
        // findViewById(R.id.radioSortUpdateDate);
        // radioUpdateDate.setChecked(true);
        // }

        // 広告の表示
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
        if (dtoStandList.size() > 0) {
            adapter = new StandAdapter<Stand>(this, R.layout.list_row,
                    dtoStandList);
            ListView mListView = getListView();

            // ヘッダーの設定
            if (mListView.getHeaderViewsCount() == 0) {
                mListView.addHeaderView(getListHeaderView());
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
                    final Stand item = (Stand) adapter
                            .getItemAtPosition(position);

                    // イベントトラック（詳細）
                    tracker.trackEvent("List", "Detail", item.shopCode, 0);

                    Intent intent1 = new Intent(getApplicationContext(),
                            DetailActivity.class);
                    intent1.putExtra("shopcode", item.shopCode);
                    startActivityForResult(intent1, 2);

                }
            });
        }
    }

    private ListView getListView() {
        if (mListView == null) {
            mListView = (ListView) findViewById(R.id.savedList);
        }
        return mListView;
    }

    private View getListHeaderView() {
        if (mHeader == null) {
            mHeader = getLayoutInflater().inflate(R.layout.list_header, null);

            // 近い順
            RadioButton radioNearby = (RadioButton) mHeader
                    .findViewById(R.id.radioSortNearby);
            radioNearby
                    .setOnCheckedChangeListener(radioGroupSortOnCheckedChangeListener);

            // 安い順
            RadioButton radioLowPrice = (RadioButton) mHeader
                    .findViewById(R.id.radioSortLowPrice);
            radioLowPrice
                    .setOnCheckedChangeListener(radioGroupSortOnCheckedChangeListener);

            // 更新日時順
            RadioButton radioUpdateDate = (RadioButton) mHeader
                    .findViewById(R.id.radioSortUpdateDate);
            radioUpdateDate
                    .setOnCheckedChangeListener(radioGroupSortOnCheckedChangeListener);

        }
        return mHeader;
    }

    private View getListFooterView() {
        if (mFooter == null) {
            mFooter = getLayoutInflater().inflate(R.layout.list_footer, null);

        }
        return mFooter;
    }

    OnCheckedChangeListener radioGroupSortOnCheckedChangeListener = new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                boolean isChecked) {
            if (isChecked) {

                if (buttonView.getId() == R.id.radioSortNearby) {
                    mode = "dist";
                } else if (buttonView.getId() == R.id.radioSortLowPrice) {
                    mode = "price";
                } else if (buttonView.getId() == R.id.radioSortUpdateDate) {
                    mode = "date";
                }

                db = dbHelper.getReadableDatabase();
                StandsDao standsDao = new StandsDao(db);
                dtoStandList.clear();
                dtoStandList = standsDao.findAll(mode);
                db.close();

                Log.d("hoge", "size;" + dtoStandList.size());
                init();
//                adapter.notifyDataSetChanged();

                // イベントトラック（並び順）
                tracker.trackEvent("List", "Sort", mode, 0);
            }
        }
    };

    private void invisibleHeader() {
        if (mHeader != null) {
            getListView().removeHeaderView(mHeader);
        }
    }
}
