package org.chrysaor.android.gas_station.ui;

import org.chrysaor.android.gas_station.R;
import org.chrysaor.android.gas_station.util.SeekBarPreference;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.util.Log;

public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	private boolean settings_penetration_key = false;
	private boolean settings_member_key = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.pref);
		
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		
        for (String key : getPreferenceScreen().getSharedPreferences().getAll().keySet()) {
        	setSummaryAll(key);
        	
        	if (key.equals("settings_member")) {
        		settings_member_key = true;
        	} else if (key.equals("settings_penetration")) {
        		settings_penetration_key = true;
        	}
        }
        
    	if (settings_member_key == false) {
    		getPreferenceScreen().findPreference("settings_member").setDefaultValue(false);
    		setSummaryAll("settings_member");
    	}
    	
    	if (settings_penetration_key == false) {
    		getPreferenceScreen().findPreference("settings_penetration").setDefaultValue(SeekBarPreference.DEFAULT_ORDER);
    		setSummaryAll("settings_penetration");
    	}

	}
	
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		setSummaryAll(key);
	}
	
	private void setSummaryAll(String key) {
		
		Log.d("hoge", key);
		String class_name = getPreferenceScreen().findPreference(key).getClass().toString();
Log.d("hoge", class_name);
		if (class_name.indexOf("ListPreference") != -1) {
			ListPreference pref = (ListPreference) getPreferenceScreen().findPreference(key);
			pref.setSummary(pref.getEntry());
		} else if (class_name.indexOf("SeekBarPreference") != -1) {
			SeekBarPreference pref = (SeekBarPreference) getPreferenceScreen().findPreference(key);
			pref.setSummary(String.valueOf(pref.getValue()) + "%");
		} else if (class_name.indexOf("CheckBoxPreference") != -1) {
			CheckBoxPreference pref = (CheckBoxPreference)getPreferenceScreen().findPreference(key);
			if (pref.isChecked()) {
				pref.setSummary("会員価格");
			} else {
				pref.setSummary("現金フリー");
			}
		}
//		Log.d("hoge", "i:" + getPreferenceScreen().findPreference(key).getClass());
	}
	
    @Override
    protected void onResume() {
    	super.onResume();
    	getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

    }
	
}
