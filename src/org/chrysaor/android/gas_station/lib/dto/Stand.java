package org.chrysaor.android.gas_station.lib.dto;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.chrysaor.android.gas_station.lib.data.Price;
import org.chrysaor.android.gas_station.util.Utils;

import android.graphics.Color;

/**
 * ガソリンスタンドDTO
 * 
 * @author Shinichi Matsuo
 * 
 */
public class Stand {

    public Integer rowId = null;
    public String shopCode = null;
    public String brand = null;
    public String shopName = null;
    public Double latitude = null;
    public Double longitude = null;
    public String distance = "0";
    public String address = null;
    public String price = "9999";
    public String photo = null;
    public String date = null;
    public String rtc = null;
    public String self = null;
    public boolean member = false;
    public String updateDate = null;
    public String createDate = null;
    public static SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
            "yyyy/MM/dd");
    public Price[] priceList = null;

    public void setData(String key, String value) {

        if (key.compareTo("ShopCode") == 0) {
            this.shopCode = value;
        } else if (key.compareTo("Brand") == 0) {
            this.brand = value;
        } else if (key.compareTo("ShopName") == 0) {
            this.shopName = value;
        } else if (key.compareTo("Latitude") == 0) {
            Utils.logging(value);
            this.latitude = Double.parseDouble(value);
        } else if (key.compareTo("Longitude") == 0) {
            this.longitude = Double.parseDouble(value);
        } else if (key.compareTo("Distance") == 0) {
            this.distance = value;
        } else if (key.compareTo("Address") == 0) {
            this.address = value;
        } else if (key.compareTo("Price") == 0) {
            try {
                Double.parseDouble(value);
                this.price = value;
            } catch (NumberFormatException e) {
            }
        } else if (key.compareTo("Date") == 0) {
            if (value.matches("[0-9]+")) {
                Date date = new Date(TimeUnit.SECONDS.toMillis(Long
                        .valueOf(value)));
                this.date = simpleDateFormat.format(date);
            } else {
                this.date = value;
            }
        } else if (key.compareTo("Photo") == 0) {
            this.photo = value;
        } else if (key.compareTo("Rtc") == 0) {
            this.rtc = value;
        } else if (key.compareTo("Self") == 0) {
            this.self = value;
        } else if (key.compareTo("Member") == 0) {
            Utils.logging(value);
            this.member = Boolean.valueOf(value);
        }

    }

    public String getDispPrice() {
        return (price.equals("9999") ? "no data" : price);
    }

    public int getDispPriceColor() {
        return (price.equals("9999") ? Color.rgb(204, 0, 0) : Color.rgb(0, 0,
                255));
    }
}
