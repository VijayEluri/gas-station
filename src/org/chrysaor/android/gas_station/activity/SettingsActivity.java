package org.chrysaor.android.gas_station.activity;

import org.chrysaor.android.gas_station.R;
import org.chrysaor.android.gas_station.util.ErrorReporter;
import org.chrysaor.android.gas_station.util.SeekBarPreference;
import org.chrysaor.android.gas_station.util.Utils;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.google.analytics.tracking.android.EasyTracker;

/**
 * 設定画面
 * 
 * @author Shinichi Matsuo
 * 
 */
public class SettingsActivity extends PreferenceActivity implements
        OnSharedPreferenceChangeListener {

    // private boolean settings_penetration_key = false;
    private boolean settings_member_key = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref);

        ErrorReporter.setup(this);
        ErrorReporter.bugreport(SettingsActivity.this);

        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);

        for (String key : getPreferenceScreen().getSharedPreferences().getAll()
                .keySet()) {
            setSummaryAll(key);

            if (key.equals("settings_member")) {
                settings_member_key = true;
            }
        }

        if (settings_member_key == false) {
            getPreferenceScreen().findPreference("settings_member")
                    .setDefaultValue(false);
            setSummaryAll("settings_member");
        }

        // パスワード
        Preference passwd = findPreference("settings_passwd");
        passwd.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference,
                    Object newValue) {
                ((EditTextPreference) preference).setText("");

                // SharedPreferencesを取得
                SharedPreferences sp;
                sp = PreferenceManager
                        .getDefaultSharedPreferences(SettingsActivity.this);

                Editor editor = sp.edit();
                editor.putString("settings_passwd_md5",
                        Utils.md5(newValue.toString()));

                editor.commit();
                return false;
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        EasyTracker.getInstance().activityStart(this); // Add this method.
    }

    @Override
    public void onStop() {
        super.onStop();
        EasyTracker.getInstance().activityStop(this); // Add this method.
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key) {
        setSummaryAll(key);
    }

    private void setSummaryAll(String key) {

        if (key.equals("settings_passwd_md5") || key.equals("settings_twitter")
                || key.equals("save_select_item") || key.indexOf("brand") != -1) {
            return;
        }

        if (getPreferenceScreen().findPreference(key) == null) {
            return;
        }

        String class_name = getPreferenceScreen().findPreference(key)
                .getClass().toString();

        if (class_name.indexOf("ListPreference") != -1) {
            ListPreference pref = (ListPreference) getPreferenceScreen()
                    .findPreference(key);
            pref.setSummary(pref.getEntry());

            // イベントトラック
            EasyTracker.getTracker().sendEvent("Settings", key,
                    pref.getEntry().toString(), (long) 0);

            if (key.equals("settings_dist")) {
                setNoPostDataSummary();
            }
        } else if (class_name.indexOf("SeekBarPreference") != -1) {
            SeekBarPreference pref = (SeekBarPreference) getPreferenceScreen()
                    .findPreference(key);
            pref.setSummary(String.valueOf(pref.getValue()) + "%");

            // イベントトラック
            EasyTracker.getTracker().sendEvent("Settings", key,
                    String.valueOf(pref.getValue()), (long) 0);

        } else if (class_name.indexOf("CheckBoxPreference") != -1) {
            CheckBoxPreference pref = (CheckBoxPreference) getPreferenceScreen()
                    .findPreference(key);
            if (key.equals("settings_member")) {
                if (pref.isChecked()) {
                    pref.setSummary("会員価格");
                } else {
                    pref.setSummary("現金フリー");
                }
                setNoPostDataSummary();
            } else if (key.equals("settings_no_postdata")) {
                setNoPostDataSummary();
            }

            // イベントトラック
            EasyTracker.getTracker().sendEvent("Settings", key,
                    String.valueOf(pref.isChecked()), (long) 0);

        } else if (class_name.indexOf("EditTextPreference") != -1) {
            EditTextPreference pref = (EditTextPreference) getPreferenceScreen()
                    .findPreference(key);

            if (key.equals("settings_user_id")
                    || key.equals("settings_sharemsg")) {
                pref.setSummary(pref.getText());
            }

            // イベントトラック
            EasyTracker.getTracker().sendEvent("Settings", key,
                    pref.getText().toString(), (long) 0);
        }
    }

    private void setNoPostDataSummary() {
        ListPreference dist_pref = (ListPreference) getPreferenceScreen()
                .findPreference("settings_dist");
        CheckBoxPreference pref = (CheckBoxPreference) getPreferenceScreen()
                .findPreference("settings_no_postdata");
        CheckBoxPreference member_pref = (CheckBoxPreference) getPreferenceScreen()
                .findPreference("settings_member");

        int dist = Integer.valueOf(dist_pref.getValue());
        if (dist > 10) {
            dist = 10;
        }
        String msg = dist + "km圏内の価格が無いスタンドを表示します。\n";

        // 会員価格の場合
        if (member_pref.isChecked()) {
            msg += "会員価格がない場合は現金フリーを表示します。";
        }
        pref.setSummary(msg);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

}
