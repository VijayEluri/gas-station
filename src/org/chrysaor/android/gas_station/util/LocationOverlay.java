package org.chrysaor.android.gas_station.util;

import org.chrysaor.android.gas_station.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.hardware.GeomagneticField;
import android.location.Location;
import android.widget.ToggleButton;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

public class LocationOverlay extends MyLocationOverlay {

    private static final int E6 = 1000000;
    public boolean myLocationFlag = false;
    private MapView mv;
    private final Drawable arrow[] = new Drawable[18];
    private final int arrowWidth, arrowHeight;
    private int lastHeading = 0;
    private double variation;
    private ToggleButton toggle;

    public LocationOverlay(Context context, MapView mapView) {
        super(context, mapView);
        this.mv = mapView;

        arrow[0] = context.getResources().getDrawable(R.drawable.arrow_0);
        arrow[1] = context.getResources().getDrawable(R.drawable.arrow_20);
        arrow[2] = context.getResources().getDrawable(R.drawable.arrow_40);
        arrow[3] = context.getResources().getDrawable(R.drawable.arrow_60);
        arrow[4] = context.getResources().getDrawable(R.drawable.arrow_80);
        arrow[5] = context.getResources().getDrawable(R.drawable.arrow_100);
        arrow[6] = context.getResources().getDrawable(R.drawable.arrow_120);
        arrow[7] = context.getResources().getDrawable(R.drawable.arrow_140);
        arrow[8] = context.getResources().getDrawable(R.drawable.arrow_160);
        arrow[9] = context.getResources().getDrawable(R.drawable.arrow_180);
        arrow[10] = context.getResources().getDrawable(R.drawable.arrow_200);
        arrow[11] = context.getResources().getDrawable(R.drawable.arrow_220);
        arrow[12] = context.getResources().getDrawable(R.drawable.arrow_240);
        arrow[13] = context.getResources().getDrawable(R.drawable.arrow_260);
        arrow[14] = context.getResources().getDrawable(R.drawable.arrow_280);
        arrow[15] = context.getResources().getDrawable(R.drawable.arrow_300);
        arrow[16] = context.getResources().getDrawable(R.drawable.arrow_320);
        arrow[17] = context.getResources().getDrawable(R.drawable.arrow_340);
        arrowWidth = arrow[lastHeading].getIntrinsicWidth();
        arrowHeight = arrow[lastHeading].getIntrinsicHeight();
        for (int i = 0; i <= 17; i++) {
            arrow[i].setBounds(0, 0, arrowWidth, arrowHeight);
        }
    }

    public void setMyLocationFlag(boolean flag) {
        myLocationFlag = flag;
    }

    @Override
    public synchronized boolean draw(Canvas canvas, MapView mapView,
            boolean shadow, long when) {
        boolean ret = super.draw(canvas, mapView, shadow, when);

        if (myLocationFlag) {
            drawMyLocation(canvas, mv, getLastFix(), getMyLocation(), 5000);
        }

        return ret;
    }

    /**
     * Sets the pointer heading in degrees (will be drawn on next invalidate).
     * 
     * @return true if the visible heading changed (i.e. a redraw of pointer is
     *         potentially necessary)
     */
    public boolean setHeading(float heading) {
        int newhdg = Math.round(-heading / 360 * 18 + 180);
        while (newhdg < 0)
            newhdg += 18;
        while (newhdg > 17)
            newhdg -= 18;
        if (newhdg != lastHeading) {
            lastHeading = newhdg;
            return true;
        } else {
            return false;
        }
    }

    public void setVariation(Location location) {
        long timestamp = location.getTime();
        if (timestamp == 0) {
            // Hack for Samsung phones which don't populate the time field
            timestamp = System.currentTimeMillis();
        }

        GeomagneticField field = new GeomagneticField(
                (float) location.getLatitude(),
                (float) location.getLongitude(),
                (float) location.getAltitude(), timestamp);
        variation = field.getDeclination();
    }

    public void onLocationChanged(Location location) {
        super.onLocationChanged(location);

//        if (toggle.isChecked() == true) {
//            GeoPoint center = mv.getMapCenter();
//            int top = center.getLatitudeE6() - mv.getLatitudeSpan() / 2; // 画面上に見える範囲
//            int bottom = center.getLatitudeE6() + mv.getLatitudeSpan() / 2;
//            int left = center.getLongitudeE6() - mv.getLongitudeSpan() / 2;
//            int right = center.getLongitudeE6() + mv.getLongitudeSpan() / 2;
//
//            // 画面外に移動した場合、現在値を中央に表示する
//            if (location.getLatitude() * E6 < top
//                    || location.getLatitude() * E6 > bottom
//                    || location.getLongitude() * E6 < left
//                    || location.getLongitude() * E6 > right) {
//
//                GeoPoint point = new GeoPoint(
//                        (int) ((double) location.getLatitude() * E6),
//                        (int) ((double) location.getLongitude() * E6));
//
//                mv.getController().animateTo(point);
//                setVariation(location);
//            }
//        }
    }

    public void onSensorChanged(int sensor, float[] values) {
        float magneticHeading = values[0];
        double heading = magneticHeading + variation;
        if (setHeading((float) heading)) {
            mv.invalidate();
        }
    }

    public void setTraceToggle(ToggleButton toggle) {
        this.toggle = toggle;
    }
}