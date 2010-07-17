package org.chrysaor.android.gas_station.util;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import org.chrysaor.android.gas_station.R;

import android.content.Context;
import android.graphics.Canvas;

public class LocationOverlay extends MyLocationOverlay {  
      
    public boolean myLocationFlag = false;  
    private MapView mv;  
      
    public LocationOverlay(Context context, MapView mapView) {  
        super(context, mapView);  
        this.mv = mapView;  
    }  
  
    public void setMyLocationFlag(boolean flag){  
        myLocationFlag = flag;  
          
    }  
      
    @Override  
    public synchronized boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when) {  
        boolean ret = super.draw(canvas, mapView, shadow, when);  
          
        if(myLocationFlag){  
            drawMyLocation(canvas, mv, getLastFix(), getMyLocation(), 5000);  
        }  
          
        return ret;  
    }  
}  