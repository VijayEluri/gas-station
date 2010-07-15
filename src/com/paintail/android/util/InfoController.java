package com.paintail.android.util;

import java.util.ArrayList;
import java.util.List;

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
    	gsInfo.setGSInfoList(this.url);
        //終了を通知
        handler.post(listener);
    }
    
    //天気提供地域を取得
	public ArrayList<GSInfo> getGSInfoList() {
		return gsInfo.getGSInfoList();
	}
    
}
