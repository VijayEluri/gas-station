package org.chrysaor.android.gas_station.util;

import java.util.ArrayList;

import org.chrysaor.android.gas_station.lib.dto.GasStand;

import android.os.Handler;

public class InfoController extends Thread {
    private Handler handler;
    private final Runnable listener;
    private String url;
    private String url4all = null;
    private GasStand gsInfo;
    
    //コンストラクタ
    public InfoController(Handler handler, Runnable listener, String url, String url4all) {
        this.handler   = handler;
        this.listener  = listener;
        this.url       = url;
        if (url4all.length() > 0) {
            this.url4all = url4all;
        }
        
        //各情報取得クラスをインスタンス化
        gsInfo = new GasStand();
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
    public ArrayList<GasStand> getGSInfoList() {
        return gsInfo.getGSInfoList();
    }
    
}
