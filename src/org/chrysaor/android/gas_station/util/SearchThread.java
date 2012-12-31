package org.chrysaor.android.gas_station.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.chrysaor.android.gas_station.lib.dto.Stand;

import android.os.Handler;

public class SearchThread extends Thread {
    private Handler handler;
    private final Runnable listener;
    private String[] url;

    // URL由来のストリーム
    protected InputStream is;

    private ArrayList<Stand> dtoStandList = new ArrayList<Stand>();

    /**
     * コンストラクタ
     * 
     * @param handler
     * @param listener
     * @param url
     */
    public SearchThread(Handler handler, Runnable listener, String... url) {
        this.handler = handler;
        this.listener = listener;
        this.url = url;
    }

    @Override
    public void run() {
        setGSInfoList(url);

        // 終了を通知
        handler.post(listener);
    }

    // ガソリンスタンド情報を取得
    public ArrayList<Stand> getGSInfoList() {
        return dtoStandList;
    }

    // ストリームを閉じる処理を共通メソッドとして定義
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
    public void setGSInfoList(String[] urls) {

        XmlParserFromUrl xml = new XmlParserFromUrl();

        for (int i = 0; i < urls.length; i++) {
            if (urls[i].length() == 0) {
                continue;
            }

            byte[] byteArray = Utils.getByteArrayFromURL(urls[i], "GET");
            if (byteArray == null) {
                Utils.logging("URLの取得に失敗:" + urls[i]);
                continue;
            }
            String data = new String(byteArray);

            if (urls[i].contains("member=1") == true) {
                dtoStandList.addAll(xml.getGSInfoFromXML(data, "true"));
            } else {
                dtoStandList.addAll(xml.getGSInfoFromXML(data, "false"));
            }
        }

        close();
    }
}
