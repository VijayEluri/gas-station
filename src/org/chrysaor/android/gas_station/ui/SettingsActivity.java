package org.chrysaor.android.gas_station.ui;

import org.chrysaor.android.gas_station.MainActivity;
import org.chrysaor.android.gas_station.R;
import org.chrysaor.android.gas_station.util.ErrorReporter;
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
		
	    ErrorReporter.setup(this);
	    ErrorReporter.bugreport(SettingsActivity.this);
		
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
		
		String class_name = getPreferenceScreen().findPreference(key).getClass().toString();

		if (class_name.indexOf("ListPreference") != -1) {
			ListPreference pref = (ListPreference) getPreferenceScreen().findPreference(key);
			pref.setSummary(pref.getEntry());
			
			if (key.equals("settings_dist")) {
				setNoPostDataSummary();
			}
		} else if (class_name.indexOf("SeekBarPreference") != -1) {
			SeekBarPreference pref = (SeekBarPreference) getPreferenceScreen().findPreference(key);
			pref.setSummary(String.valueOf(pref.getValue()) + "%");
		} else if (class_name.indexOf("CheckBoxPreference") != -1) {
			CheckBoxPreference pref = (CheckBoxPreference)getPreferenceScreen().findPreference(key);
			if (key.equals("settings_member")) {
				if (pref.isChecked()) {
					pref.setSummary("会員価格");
				} else {
					pref.setSummary("現金フリー");
				}
			} else if (key.equals("settings_no_postdata")) {
				setNoPostDataSummary();
			}
		}
	}
	
	private void setNoPostDataSummary() {
		ListPreference dist_pref = (ListPreference) getPreferenceScreen().findPreference("settings_dist");
		CheckBoxPreference pref = (CheckBoxPreference)getPreferenceScreen().findPreference("settings_no_postdata");
		
		int dist = Integer.valueOf(dist_pref.getValue());
		if (dist > 10) {
			dist = 10;
		}
		String msg = dist + "km圏内の価格が無いスタンドを表示します";
		pref.setSummary(msg);
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
