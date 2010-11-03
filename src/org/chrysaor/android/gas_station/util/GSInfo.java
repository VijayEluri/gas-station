package org.chrysaor.android.gas_station.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.util.Log;

public class GSInfo {
	private static final String LOG_TAG = "GSinfo";
	public String ShopCode = null;
	public String Brand = null;
	public String ShopName = null;
	public Double Latitude = null;
	public String Longitude = null;
	public String Distance = "0";
	public String Address = null;
	public String Price = "0";
	public String Photo = null;
	public String Date = null;
	public String Rtc = null;
	public String Self = null;
	
	//URL由来のストリーム
    protected InputStream is;
    
    protected ArrayList<GSInfo> list = new ArrayList<GSInfo>();
    
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

//        is = WebApi.accessURL(url);
		byte[] byteArray = Utils.getByteArrayFromURL(url);
		if (byteArray == null) {
			Log.i("getXmlTags", "URLの取得に失敗");
//			return result;
		}
		String data = new String(byteArray);
		
        XmlParserFromUrl xml = new XmlParserFromUrl();
        this.list.addAll(xml.getGSInfoFromXML(data));

		close();
	}
	
	public ArrayList<GSInfo> getGSInfoList() {
		return this.list;
	}

	public void setData(String key, String value) {
//        Log.d(LOG_TAG, "key:" + key + ", value" + value);

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
	
	public String getData(String sUrl) {
		DefaultHttpClient objHttp = new DefaultHttpClient();
		objHttp.getCredentialsProvider().setCredentials(
		          AuthScope.ANY, new UsernamePasswordCredentials("test", "kdlkdl"));

	    HttpParams params = objHttp.getParams();  
	    HttpConnectionParams.setConnectionTimeout(params, 1000); //接続のタイムアウト  
	    HttpConnectionParams.setSoTimeout(params, 1000); //データ取得のタイムアウト  
	    String sReturn = "";  
	    try {  
	        HttpGet objGet = new HttpGet(sUrl);
	        
	        HttpResponse objResponse = objHttp.execute(objGet);  
	        if (objResponse.getStatusLine().getStatusCode() < 400){  
	            InputStream objStream = objResponse.getEntity().getContent();  
	            InputStreamReader objReader = new InputStreamReader(objStream);  
	            BufferedReader objBuf = new BufferedReader(objReader);  
	            StringBuilder objJson = new StringBuilder();  
	            String sLine;  
	            while((sLine = objBuf.readLine()) != null){  
	                objJson.append(sLine);  
	            }  
	            sReturn = objJson.toString();  
	            objStream.close();  
	        }  
	    } catch (IOException e) {  
	        return null;  
	    }     
	    return sReturn;  
	}  
}

