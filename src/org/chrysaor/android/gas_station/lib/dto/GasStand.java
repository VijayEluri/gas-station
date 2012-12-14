package org.chrysaor.android.gas_station.lib.dto;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.chrysaor.android.gas_station.util.Price;
import org.chrysaor.android.gas_station.util.Utils;

import android.graphics.Color;

/**
 * ガソリンスタンドDTO
 * 
 * @author matsuo
 * 
 */
public class GasStand {

    public Integer RowId = null;
    public String ShopCode = null;
    public String Brand = null;
    public String ShopName = null;
    public Double Latitude = null;
    public Double Longitude = null;
    public String Distance = "0";
    public String Address = null;
    public String Price = "9999";
    public String Photo = null;
    public String Date = null;
    public String Rtc = null;
    public String Self = null;
    public boolean Member = false;
    public String UpdateDate = null;
    public String CreateDate = null;
    public Price[] Prices = null;

    /**
     * 
     * @param key
     * @param value
     */
    public void setData(String key, String value) {

        if (key.compareTo("ShopCode") == 0) {
            this.ShopCode = value;
        } else if (key.compareTo("Brand") == 0) {
            this.Brand = value;
        } else if (key.compareTo("ShopName") == 0) {
            this.ShopName = value;
        } else if (key.compareTo("Latitude") == 0) {
            Utils.logging(value);
            this.Latitude = Double.parseDouble(value);
        } else if (key.compareTo("Longitude") == 0) {
            this.Longitude = Double.parseDouble(value);
        } else if (key.compareTo("Distance") == 0) {
            this.Distance = value;
        } else if (key.compareTo("Address") == 0) {
            this.Address = value;
        } else if (key.compareTo("Price") == 0) {
            try {
                Double.parseDouble(value);
                this.Price = value;
            } catch (NumberFormatException e) {
            }
        } else if (key.compareTo("Date") == 0) {
            if (value.matches("[0-9]+")) {
                Date date = new Date(TimeUnit.SECONDS.toMillis(Long
                        .valueOf(value)));
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                        "yyyy/MM/dd");
                this.Date = simpleDateFormat.format(date);
            } else {
                this.Date = value;
            }
        } else if (key.compareTo("Photo") == 0) {
            this.Photo = value;
        } else if (key.compareTo("Rtc") == 0) {
            this.Rtc = value;
        } else if (key.compareTo("Self") == 0) {
            this.Self = value;
        } else if (key.compareTo("Member") == 0) {
            Utils.logging(value);
            this.Member = Boolean.valueOf(value);
        }

    }

    public Double getLatitude() {
        return this.Latitude;
    }

    public Double getLongitude() {
        return this.Longitude;
    }

    public String getDispPrice() {
        return (Price.equals("9999") ? "no data" : Price);
    }

    public int getDispPriceColor() {
        return (Price.equals("9999") ? Color.rgb(204, 0, 0) : Color.rgb(0, 0,
                255));
    }
}
