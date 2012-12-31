package org.chrysaor.android.gas_station.util;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class GasStaApplication extends Application {

    protected SharedPreferences sharedPreferences;

    @Override
    public void onCreate() {
        sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
    }

    /**
     * 油種の取得
     * 
     * @return
     */
    public String getKind() {
        return sharedPreferences.getString("settings_kind", "0");
    }

    /**
     * 油種の設定
     * 
     * @param value
     */
    public void setKind(String value) {
        sharedPreferences.edit().putString("settings_kind", value).commit();
    }

    /**
     * セルフの取得
     * 
     * @return
     */
    public boolean getSelf() {
        return sharedPreferences.getBoolean("settings_self", false);
    }

    /**
     * セルフの設定
     * 
     * @param value
     */
    public void setSelf(boolean value) {
        sharedPreferences.edit().putBoolean("settings_self", value).commit();
    }

    /**
     * 中心からの距離の取得
     * 
     * @return
     */
    public String getDistance() {
        return sharedPreferences.getString("settings_dist", "10");
    }

    /**
     * 中心からの距離の設定
     * 
     * @param value
     */
    public void setDistance(String value) {
        sharedPreferences.edit().putString("settings_dist", value).commit();
    }

    /**
     * ２４時間営業
     * 
     * @return
     */
    public boolean get24h() {
        return sharedPreferences.getBoolean("settings_rtc", false);
    }

    /**
     * 
     * @param value
     */
    public void set24h(boolean value) {
        sharedPreferences.edit().putBoolean("settings_rtc", value).commit();
    }

    /**
     * 会員価格検索の取得
     * 
     * @return
     */
    public boolean getMember() {
        return sharedPreferences.getBoolean("settings_member", false);
    }

    /**
     * 会員価格検索の設定
     * 
     * @param value
     */
    public void setMember(boolean value) {
        sharedPreferences.edit().putBoolean("settings_member", value).commit();
    }

    /**
     * 価格のないスタンド検索の取得
     * 
     * @return
     */
    public boolean getNoData() {
        return sharedPreferences.getBoolean("settings_no_postdata", true);
    }

    /**
     * 価格のないスタンド検索の設定
     * 
     * @param value
     */
    public void setNoData(boolean value) {
        sharedPreferences.edit().putBoolean("settings_no_postdata", value)
                .commit();
    }
}
