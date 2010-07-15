package com.paintail.android.util;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.paintail.android.MainActivity;
import com.paintail.android.R;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.preference.PreferenceManager;

import java.util.ArrayList;

public class LocationOverlay extends Overlay {

  private final Drawable arrow[] = new Drawable[18];
  private final int arrowWidth, arrowHeight;
  private final Context context;

  private int lastHeading = 0;
  private Location myLocation;
  private GeoPoint center;
  private boolean showEndMarker = true;

  public LocationOverlay(Context context) {
    this.context = context;

    // TODO: Can we use a FrameAnimation or similar here rather
    // than individual resources for each arrow direction?

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

  @Override
  public void draw(Canvas canvas, MapView mapView, boolean shadow) {

    if (shadow) {
      return;
    }

    // Draw the arrow icon:
    if (myLocation == null) {
      return;
    }
    GeoPoint geoPoint = new GeoPoint(
        (int) (myLocation.getLatitude() * 1E6),
        (int) (myLocation.getLongitude() * 1E6));
    Point pt = new Point();
    mapView.getProjection().toPixels(geoPoint, pt);
    canvas.save();
    canvas.translate(pt.x - (arrowWidth / 2), pt.y - (arrowHeight / 2));
    arrow[lastHeading].draw(canvas);
    canvas.restore();

  }

  @Override
  public boolean draw(Canvas canvas, MapView mapView, boolean shadow,
      long when) {
    draw(canvas, mapView, shadow);
    return false;
  }

  /**
   * Sets the pointer location (will be drawn on next invalidate).
   */
  public void setMyLocation(Location myLocation) {
    this.myLocation = myLocation;
  }
  
  public void setCenter(GeoPoint center) {
	  this.center = center;
  }
}
