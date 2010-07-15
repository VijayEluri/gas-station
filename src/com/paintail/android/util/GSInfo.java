package com.paintail.android.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.util.Log;

public class GSInfo {
	private static final String LOG_TAG = "GSinfo";
	public String ShopCode = null;
	public String Brand = null;
	public String ShopName = null;
	public Double Latitude = null;
	public String Longitude = null;
	public String Distance = null;
	public String Address = null;
	public String Price = null;
	public String Photo = null;
	public String Date = null;
	public String Rtc = null;
	public String Self = null;
	
	//URL由来のストリーム
    protected InputStream is;
    
    protected ArrayList<GSInfo> list;
    
    //ストリームを閉じる処理を共通メソッドとして定義
	public void close() {
		if (is != null) {
			try {
				is.close();
				is = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	// ガソリンスタンド情報を取得・設定する
	public void setGSInfoList(String url) {

        is = WebApi.accessURL(url);
              
        String alert = null;
      
        XmlParserFromUrl xml = new XmlParserFromUrl();
        this.list = xml.getGSInfoFromXML(is);

		close();
	}
	
	public ArrayList<GSInfo> getGSInfoList() {
		return this.list;
	}

	public void setData(String key, String value) {
        Log.d(LOG_TAG, "key:" + key + ", value" + value);

		if (key.compareTo("ShopCode") == 0) {
			this.ShopCode = value;
		} else if (key.compareTo("Brand") == 0) {
			this.Brand = value;
		} else if (key.compareTo("ShopName") == 0) {
			this.ShopName = value;
		} else if (key.compareTo("Latitude") == 0) {
            Log.d(LOG_TAG, "lat = " + value);

			this.Latitude = Double.parseDouble(value);
		} else if (key.compareTo("Longitude") == 0) {
			this.Longitude = value;
		} else if (key.compareTo("Distance") == 0) {
			this.Distance = value;
		} else if (key.compareTo("Address") == 0) {
			this.Address = value;
		} else if (key.compareTo("Price") == 0) {
			this.Price = value;
		} else if (key.compareTo("Date") == 0) {
			this.Date = value;
		} else if (key.compareTo("Photo") == 0) {
			this.Photo = value;
		} else if (key.compareTo("Rtc") == 0) {
			this.Rtc = value;
		} else if (key.compareTo("Self") == 0) {
			this.Self = value;
		}
		
	}
	
	public Double getLatitude() {
		return this.Latitude;
	}
	
	public String getLongitude() {
		return this.Longitude;
	}
}

