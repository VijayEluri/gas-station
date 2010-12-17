package org.chrysaor.android.gas_station.ui;

import org.chrysaor.android.gas_station.MainActivity;
import org.chrysaor.android.gas_station.R;
import org.chrysaor.android.gas_station.util.ErrorReporter;
import org.chrysaor.android.gas_station.util.SeekBarPreference;
import org.chrysaor.android.gas_station.util.Utils;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;

public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	private boolean settings_penetration_key = false;
	private boolean settings_member_key = false;
    GoogleAnalyticsTracker tracker;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.pref);
		
	    ErrorReporter.setup(this);
	    ErrorReporter.bugreport(SettingsActivity.this);
		
	    tracker = GoogleAnalyticsTracker.getInstance();
	    
	    // Start the tracker in manual dispatch mode...
	    tracker.start("UA-20090562-2", 20, this);
	    tracker.trackPageView("/SettingsActivity");

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
    	
    	// パスワード
    	Preference passwd = findPreference("settings_passwd");
    	passwd.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
    		public boolean onPreferenceChange(Preference preference, Object newValue) {
    			((EditTextPreference) preference).setText("");
    			
    			// SharedPreferencesを取得
    			SharedPreferences sp;
    			sp = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
    			
    			Editor editor = sp.edit();
    			Log.i("hogew", Utils.md5(newValue.toString()));
    			editor.putString("settings_passwd_md5", Utils.md5(newValue.toString()));
    			
    			editor.commit();
    			return false;
    		}
    	});

	}
	
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		setSummaryAll(key);
	}
	
	private void setSummaryAll(String key) {
		Log.i("goe", key);
		
		if (key.equals("settings_passwd_md5")) {
			return;
		}
		String class_name = getPreferenceScreen().findPreference(key).getClass().toString();

		if (class_name.indexOf("ListPreference") != -1) {
			ListPreference pref = (ListPreference) getPreferenceScreen().findPreference(key);
			pref.setSummary(pref.getEntry());
			
	        // イベントトラック
	        tracker.trackEvent(
	            "Settings",      // Category
	            key,             // Action
	            pref.getEntry().toString(), // Label
	            0);

			if (key.equals("settings_dist")) {
				setNoPostDataSummary();
			}
		} else if (class_name.indexOf("SeekBarPreference") != -1) {
			SeekBarPreference pref = (SeekBarPreference) getPreferenceScreen().findPreference(key);
			pref.setSummary(String.valueOf(pref.getValue()) + "%");
			
	        // イベントトラック
	        tracker.trackEvent(
	            "Settings",      // Category
	            key,             // Action
	            String.valueOf(pref.getValue()), // Label
	            0);

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
			
			
	        // イベントトラック
	        tracker.trackEvent(
	            "Settings",      // Category
	            key,             // Action
	            String.valueOf(pref.isChecked()), // Label
	            0);

		} else if (class_name.indexOf("EditTextPreference") != -1) {
			EditTextPreference pref = (EditTextPreference) getPreferenceScreen().findPreference(key);
			
			if (key.equals("settings_user_id") || key.equals("settings_sharemsg")) {
				pref.setSummary(pref.getText());				
			}
			
	        // イベントトラック
	        tracker.trackEvent(
	            "Settings",      // Category
	            key,             // Action
	            pref.getText().toString(), // Label
	            0);
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
    
    @Override
    protected void onDestroy() {
      super.onDestroy();
      // Stop the tracker when it is no longer needed.
      tracker.stop();
    }
	
}
