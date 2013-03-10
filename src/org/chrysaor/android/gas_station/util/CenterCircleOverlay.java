package org.chrysaor.android.gas_station.util;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import org.chrysaor.android.gas_station.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.preference.PreferenceManager;

public class CenterCircleOverlay extends Overlay {

    private final Paint errorCirclePaint;
    private final Paint centerCirclePaint;
    private final Context context;

    public CenterCircleOverlay(Context context) {
        this.context = context;

        errorCirclePaint = new Paint();
        errorCirclePaint
                .setColor(context.getResources().getColor(R.color.blue));
        errorCirclePaint.setStyle(Paint.Style.STROKE);
        errorCirclePaint.setStrokeWidth(3);
        errorCirclePaint.setAlpha(127);
        errorCirclePaint.setAntiAlias(true);

        centerCirclePaint = new Paint();
        centerCirclePaint.setColor(context.getResources()
                .getColor(R.color.blue));
        centerCirclePaint.setStyle(Paint.Style.FILL);
        centerCirclePaint.setStrokeWidth(3);
        centerCirclePaint.setAlpha(25);
        centerCirclePaint.setAntiAlias(true);
    }

    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {

        if (shadow) {
            return;
        }

        GeoPoint geoPoint = mapView.getMapCenter();
        Point pt = new Point();
        mapView.getProjection().toPixels(geoPoint, pt);

        // Draw the error circle:
        float radius = mapView.getProjection().metersToEquatorPixels(
                (float) Integer.parseInt(PreferenceManager
                        .getDefaultSharedPreferences(context).getString(
                                "settings_dist", "10")) * 1000);
        canvas.drawCircle(pt.x, pt.y, radius, errorCirclePaint);
        canvas.drawCircle(pt.x, pt.y, radius, centerCirclePaint);
    }

    @Override
    public boolean draw(Canvas canvas, MapView mapView, boolean shadow,
            long when) {
        draw(canvas, mapView, shadow);
        return false;
    }

}
