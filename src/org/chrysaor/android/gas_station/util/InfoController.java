package org.chrysaor.android.gas_station.util;

import java.util.ArrayList;

import org.chrysaor.android.gas_station.MainActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.preference.PreferenceManager;

public class InfoController extends Thread {
    private Handler handler;
    private final Runnable listener;
    private String url;
    private String url4all = null;
    
	private GSInfo gsInfo;
	
    //コンストラクタ
	public InfoController(Handler handler, Runnable listener, String url, String url4all) {
		this.handler   = handler;
		this.listener  = listener;
		this.url       = url;
		if (url4all.length() > 0) {
			this.url4all = url4all;
		}
		
		//各情報取得クラスをインスタンス化
        gsInfo = new GSInfo();
	}
	
    @Override
    public void run() {
    	
    	if (this.url4all == null) {
        	String[] urls = {this.url};    		
    		gsInfo.setGSInfoList(urls);
    	} else {
        	String[] urls = {this.url, this.url4all};
    		gsInfo.setGSInfoList(urls);    		
    	}

        //終了を通知
        handler.post(listener);
    }
    
    // ガソリンスタンド情報を取得
	public ArrayList<GSInfo> getGSInfoList() {
		return gsInfo.getGSInfoList();
	}
    
}
