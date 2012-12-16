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
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;

public class ListActivity extends AbstractMyActivity {
    private ArrayList<Stand> dtoStandList = null;
    private StandAdapter adapter = null;
    private DatabaseHelper dbHelper = null;
    private SQLiteDatabase db = null;
    private StandsDao standsDao = null;
    private static String mode = "none";
    private SharedPreferences pref = null;

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

        Spinner spinSort = (Spinner) findViewById(R.id.spin_sort);

        if (mode.equals("distance")) {
            spinSort.setSelection(0);
        } else if (mode.equals("price")) {
            spinSort.setSelection(1);
        } else if (mode.equals("date")) {
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
                dtoStandList = standsDao.findAll(mode);
                db.close();
                init();

                // イベントトラック（並び順）
                tracker.trackEvent("List", "Sort", mode, 0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

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
            ListView savedList = (ListView) findViewById(R.id.savedList);
            adapter = new StandAdapter(this, R.layout.list, dtoStandList);
            savedList.setAdapter(adapter);

            savedList.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> adapter, View view,
                        int position, long id) {
                    final Stand item = dtoStandList.get(position);

                    // イベントトラック（詳細）
                    tracker.trackEvent("List", // Category
                            "Detail", // Action
                            item.shopCode, // Label
                            0);

                    Intent intent1 = new Intent(ListActivity.this,
                            DetailActivity.class);
                    intent1.putExtra("shopcode", item.shopCode);
                    startActivityForResult(intent1, 2);

                }
            });
        }
    }
}
