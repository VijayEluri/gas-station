package org.chrysaor.android.gas_station.util;

import java.util.ArrayList;

import org.chrysaor.android.gas_station.MainActivity;

import com.google.android.maps.GeoPoint;

import android.app.Service;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class StandsWidgetService extends Service {
	private SQLiteDatabase db;

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		
		Uri uri = intent.getData();
		if (uri != null) {
			String[] measurements = uri.getQueryParameter("measurements").split(",");
		
		    String url_string = "http://api.gogo.gs/v1.2/?apid=gsearcho0o0";
            url_string += "&dist=" + measurements[0];
            url_string += "&num="  + measurements[1];
            url_string += "&span=" + measurements[2];
            if (measurements[3].equals("true")) {
        	    url_string += "&member=1";
            }
            url_string += "&kind=" + measurements[4];
            url_string += "&lat="  + measurements[5];
            url_string += "&lon="  + measurements[6];

            String url = url_string + "&sort=d";
     		//Toast.makeText(this, url, Toast.LENGTH_LONG).show();

            GSInfo gsInfo = new GSInfo();
            String urls[] = {url};
	    	gsInfo.setGSInfoList(urls);

//	    	DatabaseHelper dbHelper = new DatabaseHelper(this);
//        	db = dbHelper.getWritableDatabase();
//        	StandsWidgetDao standsWidgetDao = new StandsWidgetDao(db);
//        	standsWidgetDao.deleteAll();
        	ArrayList<GSInfo> lists = gsInfo.getGSInfoList();

            Intent retIntent = new Intent();
            retIntent.setAction("org.chrysaor.StandsWidgetService.VIEW");
            String uri_str = "stands:///result?info=1";

            int cnt = 0;
        	for(GSInfo row : lists) {
//    	        standsWidgetDao.insert(row);
    	        
    	        uri_str +=  "," + row.Price;
    			Float dist = Float.parseFloat(row.Distance) / 1000;
    	        uri_str +=  "," + dist.toString();
    	        uri_str +=  "," + row.Brand;
    	        uri_str +=  "," + row.Self;
    	        uri_str +=  "," + row.Rtc;
    	        cnt++;

        	}        	
            Uri resUri = Uri.parse(uri_str);
            retIntent.setData(resUri);
            startService(retIntent);

            Log.d("hoge", "url = " + url.toString());
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
