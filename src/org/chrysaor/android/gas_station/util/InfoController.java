package org.chrysaor.android.gas_station.util;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;

public class InfoController extends Thread {
    private Handler handler;
    private final Runnable listener;
    private String url;
    
	private GSInfo gsInfo;
	
    //コンストラクタ
	public InfoController(Handler handler, Runnable listener, String url) {
		this.handler   = handler;
		this.listener  = listener;
		this.url       = url;
		
		//各情報取得クラスをインスタンス化
        gsInfo = new GSInfo();
	}
	
    @Override
    public void run() {
    	/*
    	String json = gsInfo.getData("http://www.shoptweet.info/shop.json");
		try {
    	    JSONObject jsons = new JSONObject(json);
			JSONArray ss = jsons.getJSONArray("ss");
			int length = ss.length();
	    	for (int i = 0; i < length; i++) {
	    	    JSONObject jsonObj = ss.getJSONObject(i);
	    	    
	            GSInfo gs = new GSInfo();
	            gs.ShopCode = jsonObj.getString("ss_id");
	            gs.ShopName = jsonObj.getString("ss_name");
	            gs.Address  = jsonObj.getString("ss_address");
	            gs.Brand    = "etc";
	            gs.Latitude = jsonObj.getDouble("ss_ido");
	            gs.Longitude = jsonObj.getString("ss_keido");
	            gs.Date      = "2010/10/30";
	            gs.Distance  = "0";
	            
	            switch (jsonObj.getInt("ss_maker")) {
	            case 1:
	            	gs.Brand = "JOMO";
	            	break;
	            case 2:
	            	gs.Brand = "ESSO";
	            	break;
	            case 3:
	            	gs.Brand = "ENEOS";
	            	break;
	            case 4:
	            	gs.Brand = "KYGNUS";
	            	break;
	            case 6:
	            	gs.Brand = "COSMO";
	            	break;
	            case 7:
	            	gs.Brand = "SHELL";
	            	break;
	            case 8:
	            	gs.Brand = "IDEMITSU";
	            	break;
	            case 9:
	            	gs.Brand = "IDEMITSU";
	            	break;
	            case 10:
	            	gs.Brand = "MOBIL";
	            	break;
	            case 11:
	            	gs.Brand = "SOLATO";
	            	break;
	            case 12:
	            	gs.Brand = "JA-SS";
	            	break;
	            case 13:
	            	gs.Brand = "GENERAL";
	            	break;
	            case 14:
	            	gs.Brand = "IDEMITSU";
	            	break;
	            case 98:
	            	continue;
	            }

	            gsInfo.list.add(gs);

	    	}
		} catch (JSONException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
    	*/
    	gsInfo.setGSInfoList(this.url);

        //終了を通知
        handler.post(listener);
    }
    
    // ガソリンスタンド情報を取得
	public ArrayList<GSInfo> getGSInfoList() {
		return gsInfo.getGSInfoList();
	}
    
}
