package org.chrysaor.android.gas_station;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.chrysaor.android.gas_station.ui.AboutActivity;
import org.chrysaor.android.gas_station.ui.SettingsActivity;
import org.chrysaor.android.gas_station.util.CenterCircleOverlay;
import org.chrysaor.android.gas_station.util.GSInfo;
import org.chrysaor.android.gas_station.util.InfoController;
import org.chrysaor.android.gas_station.util.LocationOverlay;
import org.chrysaor.android.gas_station.util.StandController;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;

import org.chrysaor.android.gas_station.R;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.admob.android.ads.AdView;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.OverlayItem;

public class MainActivity extends MapActivity implements Runnable {
    private static final String LOG_TAG = "MainActivity";

    private static final int E6 = 1000000;
    private MapController mMapController = null;
	private MapView mMapView = null;
    private LocationManager mLocationManager;
    private static final long LOCATION_MIN_TIME = 10000 * 1;
    private static final float LOCATION_MIN_DISTANCE = 5.0F;
    public static Location myLocation = null;
    protected InputStream is;
    private ProgressDialog dialog;
	private Resources resource;
	private LocationOverlay overlay;

	//天候情報生成クラス
	private InfoController infoController;
	
	private final Handler handler = new Handler();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.main);
        String num = PreferenceManager.getDefaultSharedPreferences(this).getString("settings_dist", "60");
        Log.d(LOG_TAG, LOG_TAG + num);

        mMapView = (MapView) findViewById(R.id.main_map);
	    mMapView.setBuiltInZoomControls(true);

        mMapController = mMapView.getController();

        CenterCircleOverlay location = new CenterCircleOverlay(this);
        mMapView.getOverlays().add(location);
        
		// 今回の主役。有効にすることでGPSの取得が可能に
		overlay = new LocationOverlay(getApplicationContext(), mMapView);
		overlay.enableMyLocation();

		// GPS取得が可能な状態になり、GPS初取得時の動作を決定（らしい）
		overlay.runOnFirstFix(new Runnable(){
			public void run() {
				// TODO 自動生成されたメソッド・スタブ
				// animateTo(GeoPoint)で指定GeoPoint位置に移動
				// この場合、画面中央がGPS取得による現在位置になる
				mMapView.getController().animateTo(overlay.getMyLocation());
			}
		});

		// Overlayとして登録
		mMapView.getOverlays().add(overlay);
		
        AdView adView = new AdView(this); 
        adView.setVisibility(android.view.View.VISIBLE); 
        adView.requestFreshAd(); 
        adView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        mMapView.addView(adView);

        mMapView.invalidate();
	    
        // 位置情報の取得を開始
        mLocationManager = ((LocationManager) getSystemService(Context.LOCATION_SERVICE));
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_MIN_TIME,
                LOCATION_MIN_DISTANCE, mListener);
    }
	
    
    @Override
    protected void onResume() {
    	overlay.enableMyLocation();
        if (mLocationManager != null) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_MIN_TIME,
                    LOCATION_MIN_DISTANCE, mListener);
        }
        
        super.onResume();

        Log.d(LOG_TAG, "gas resume");
    }
    
    @Override
    protected void onPause() {
    	overlay.disableMyLocation();
        if (mLocationManager != null) {
            mLocationManager.removeUpdates(mListener);
        }
        
        Log.d(LOG_TAG, "gas pause");

        super.onPause();
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
      switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
          break;
      case MotionEvent.ACTION_UP:
          break;
      case MotionEvent.ACTION_MOVE:
          CenterCircleOverlay location = new CenterCircleOverlay(this);
          mMapView.getOverlays().add(location);
          break;
      case MotionEvent.ACTION_CANCEL:
          break;
      }
  	  return true;
    }
	
	public class PinItemizedOverlay extends ItemizedOverlay<PinOverlayItem> implements Runnable {

	    private List<GeoPoint> points = new ArrayList<GeoPoint>();
	    private List<String> msgs = new ArrayList<String>();
	    private List<String> prices = new ArrayList<String>();
	    private List<String> pin_types = new ArrayList<String>();
	    private List<GSInfo> gsInfo = new ArrayList<GSInfo>();
	    private StandController stand;

	    public PinItemizedOverlay(Drawable defaultMarker) {
	        super( boundCenterBottom(defaultMarker) );
	    }

	    @Override
	    protected PinOverlayItem createItem(int i) {
            Log.d(LOG_TAG, "index = " + this.size());

	    	GeoPoint point = points.get(i);
	    	return new PinOverlayItem(point);
	    }

	    @Override
	    public int size() {
	        return points.size();
	    }

	    public void addPoint(GeoPoint point) {
	        this.points.add(point);
	        populate();
	    }
		
	    public void clearPoint() {
	        this.points.clear();
	        populate();
	    }
	    
	    public Boolean checkPoint(GeoPoint point) {
	    	Integer i = 0;
	    	for(i=0;i< this.points.size();i++) {
	            if (point.equals(this.points.get(i))) {
	                return true;
	             }
	    	}
	    	
	    	return false;
	    }
	    
	    public void setMsg(String msg) {
	        this.msgs.add(msg);
	    }

	    public void setPinType(String type) {
	        this.pin_types.add(type);
	    }

	    public void setPrice(String title) {
	        this.prices.add(title);
	    }
	    
	    public void setGSInfo(GSInfo info) {
	        this.gsInfo.add(info);
	    }
	    
		/**
		 * アイテムがタップされた時の処理
		 */
		@Override
		protected boolean onTap(int index) {
			
			//マップ中心の周辺にあるガソリンスタンド情報を取得する
			stand = new StandController(handler, this, MainActivity.this, gsInfo.get(index));

	        //プログレスダイアログを表示
            resource = getResources();
            dialog = new ProgressDialog(MainActivity.this);
	        dialog.setIndeterminate(true);
	        dialog.setMessage(resource.getText(R.string.dialog_message_getting_data));
	        dialog.show();

			// マップの中心座標を、タップされたアイテムに合わせる
			// mapControlerは、パッケージスコープで宣言
			mMapController.animateTo(this.getItem(index).getPoint());

	        stand.start();
			return true;
		}
		
		@Override
		public void run() {
			//プログレスダイアログを閉じる
			dialog.dismiss();

	    	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
	        alertDialogBuilder.setView(stand.getView());
	        alertDialogBuilder.show();
		}		
		
		@Override
		public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		    super.draw(canvas, mapView, shadow);
		    
		    if (shadow) {
		    	return;
		    }
		    
		    String pin_type = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString("settings_pin_type", "price");
		    
            for (int i=0;i<prices.size();i++) {

            	String price = prices.get(i);
            	GeoPoint locate = points.get(i);
            	String pin = pin_types.get(i);
                
            	if (pin.compareTo("brand") == 0) {
            		continue;
            	}
            	
    		    Paint p = new Paint();
    		    int sz = 5;
    		    
    		    Point pt = new Point();
    		    mapView.getProjection().toPixels(locate, pt);
    		        
    		    // Convert to screen coords
//    		    pc.getPointXY(mDefPoint, scoords);

    		    // Draw point caption and its bounding rectangle
    		    p.setTextSize(14);
    		    p.setAntiAlias(true);
    		    int sw = (int)(p.measureText(price) + 0.5f);
    		    int sh = 25;
    		    int sx = pt.x - sw / 2 - 5;
    		    int sy = pt.y - sh - sz - 2;

    		    canvas.drawText(price, sx + 5, sy + sh - 8, p);
            }
		    return;
		}
	}
	
	public class PinOverlayItem extends OverlayItem {

	    public PinOverlayItem(GeoPoint point){
	        super(point, "", "");
	    }
	    
	}
	
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu( Menu menu ) {
	    super.onCreateOptionsMenu( menu );
	    // メニューアイテムを追加
	    MenuItem item1 = menu.add( 0, 2, 0, "検索" );
	    MenuItem item2 = menu.add( 0, 0, 0, "設定" );
	    MenuItem item0 = menu.add( 0, 1, 0, "現在地" );
	    MenuItem item3 = menu.add( 0, 3, 0, "about" );
	    // 追加したメニューアイテムのアイコンを設定
	    item0.setIcon( android.R.drawable.ic_menu_mylocation);
	    item1.setIcon( android.R.drawable.ic_menu_search );
	    item2.setIcon( android.R.drawable.ic_menu_preferences );
	    item3.setIcon( android.R.drawable.ic_menu_info_details );
	    return true;
	}
	
	@Override  
	public boolean onOptionsItemSelected(MenuItem item){

	    switch(item.getItemId()){  
	    case 0:  
	        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
	        startActivity(intent);  
	        return true;
	    case 1:
	    	
//	    	overlay.setMyLocationFlag(true);
    		GeoPoint l = overlay.getMyLocation();
    		
    		if (l == null) {
        		Toast.makeText(this, "現在地を取得できません", Toast.LENGTH_LONG).show();	    			
    		} else {
                // 取得した位置をマップの中心に設定
                mMapController.animateTo(l);
    		}
            return true;
	    case 2:
            try{
                String url_string = "http://api.gogo.gs/v1.2/?apid=gsearcho0o0";
                url_string = url_string + "&dist=" + PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString("settings_dist", "10");
                url_string = url_string + "&num=" + PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString("settings_num", "60");
                url_string = url_string + "&span=" + PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString("settings_span", "");
                Boolean member = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getBoolean("settings_member", false);
                if (member == true) {
                	url_string = url_string + "&member=1";
                }
                url_string = url_string + "&kind=" +  PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString("settings_kind", "0");

                // 地図の中心位置を取得
                GeoPoint center = mMapView.getMapCenter();
                url_string = url_string + "&lat=" +  (double) center.getLatitudeE6() / E6;
                url_string = url_string + "&lon=" +  (double) center.getLongitudeE6() / E6;

                String url = url_string + "&sort=d";
                Log.d(LOG_TAG, "url = " + url.toString());
                
    			//マップ中心の周辺にあるガソリンスタンド情報を取得する
    			infoController = new InfoController(handler, this, url);

    	        //プログレスダイアログを表示
                resource = getResources();
                dialog = new ProgressDialog(this);
    	        dialog.setIndeterminate(true);
    	        dialog.setMessage(resource.getText(R.string.dialog_message_getting_data));
    	        dialog.show();
    	        
    	        infoController.start();
            }catch(Exception e){
                Log.d(LOG_TAG, e.getMessage());
            }
	    	return true;
	    case 3:
	        Intent intent2 = new Intent(MainActivity.this, AboutActivity.class);
	        startActivity(intent2);  
	        return true;
	    }  
	    return false;  
	}
	
	@Override
	public void run() {
		//プログレスダイアログを閉じる
		dialog.dismiss();

        ArrayList<GSInfo> list = infoController.getGSInfoList();
        PinItemizedOverlay pinOverlay = null;
        
        //取得に失敗
		if(list == null | list.size() <= 0) {
    		Toast.makeText(this, resource.getText(R.string.dialog_message_out_of_range), Toast.LENGTH_LONG).show();
		} else {
    		Toast.makeText(this, list.size() + "件のスタンドが見つかりました", Toast.LENGTH_LONG).show();

	    	// MapView上に表示したいビットマップ情報を、リソースから取得
			Drawable brand01 = getResources().getDrawable(R.drawable.jomo);
			Drawable brand02 = getResources().getDrawable(R.drawable.esso);
			Drawable brand03 = getResources().getDrawable(R.drawable.eneos);
			Drawable brand04 = getResources().getDrawable(R.drawable.kygnus);
			Drawable brand06 = getResources().getDrawable(R.drawable.icon_maker6);
			Drawable brand07 = getResources().getDrawable(R.drawable.shell);
			Drawable brand08 = getResources().getDrawable(R.drawable.icon_maker8);
			Drawable brand09 = getResources().getDrawable(R.drawable.icon_maker9);
			Drawable brand10 = getResources().getDrawable(R.drawable.icon_maker10);
			Drawable brand11 = getResources().getDrawable(R.drawable.icon_maker11);
			Drawable brand12 = getResources().getDrawable(R.drawable.icon_maker12);
			Drawable brand13 = getResources().getDrawable(R.drawable.icon_maker13);
			Drawable brand14 = getResources().getDrawable(R.drawable.icon_maker14);
			Drawable brand99 = getResources().getDrawable(R.drawable.icon_maker99);

			Drawable speech  = getResources().getDrawable(R.drawable.speech);

			PinItemizedOverlay brand01Overlay = new PinItemizedOverlay(brand01);
	        PinItemizedOverlay brand02Overlay = new PinItemizedOverlay(brand02);
	        PinItemizedOverlay brand03Overlay = new PinItemizedOverlay(brand03);
	        PinItemizedOverlay brand04Overlay = new PinItemizedOverlay(brand04);
	        PinItemizedOverlay brand06Overlay = new PinItemizedOverlay(brand06);
	        PinItemizedOverlay brand07Overlay = new PinItemizedOverlay(brand07);
	        PinItemizedOverlay brand08Overlay = new PinItemizedOverlay(brand08);
	        PinItemizedOverlay brand09Overlay = new PinItemizedOverlay(brand09);
	        PinItemizedOverlay brand10Overlay = new PinItemizedOverlay(brand10);
	        PinItemizedOverlay brand11Overlay = new PinItemizedOverlay(brand11);
	        PinItemizedOverlay brand12Overlay = new PinItemizedOverlay(brand12);
	        PinItemizedOverlay brand13Overlay = new PinItemizedOverlay(brand13);
	        PinItemizedOverlay brand14Overlay = new PinItemizedOverlay(brand14);
	        PinItemizedOverlay brand99Overlay = new PinItemizedOverlay(brand99);

	        PinItemizedOverlay speechOverlay  = new PinItemizedOverlay(speech);
	        
            String pin_type = PreferenceManager.getDefaultSharedPreferences(this).getString("settings_pin_type", "price");

            for (int i=0;i<list.size();i++) {

            	GSInfo info = list.get(i);
            	Log.d(LOG_TAG, "i:" + i);
//                Log.d(LOG_TAG, "lat:" + info.getLatitude().toString());
//                Log.d(LOG_TAG, info.getLongitude());
                Log.d(LOG_TAG, info.Brand);
                
                if (pin_type.compareTo("price") == 0) {
                	pinOverlay = speechOverlay;                	
                } else {
                	if (info.Brand.compareTo("JOMO") == 0) {
                		pinOverlay = brand01Overlay;
	                } else if (info.Brand.compareTo("ESSO") == 0) {
	                	pinOverlay = brand02Overlay;
	                } else if (info.Brand.compareTo("ENEOS") == 0) {
	                	pinOverlay = brand03Overlay;
	                } else if (info.Brand.compareTo("KYGNUS") == 0) {
	                	pinOverlay = brand04Overlay;
	                } else if (info.Brand.compareTo("COSMO") == 0) {
	                	pinOverlay = brand06Overlay;
	                } else if (info.Brand.compareTo("SHELL") == 0) {
	                    pinOverlay = brand07Overlay;
	                } else if (info.Brand.compareTo("IDEMITSU") == 0) {
	                	pinOverlay = brand08Overlay;
	                } else if (info.Brand.compareTo("IDEMITSU") == 0) {
	                	pinOverlay = brand09Overlay;
	                } else if (info.Brand.compareTo("MOBIL") == 0) {
	                	pinOverlay = brand10Overlay;
	                } else if (info.Brand.compareTo("SOLATO") == 0) {
	                	pinOverlay = brand11Overlay;
	                } else if (info.Brand.compareTo("JA-SS") == 0) {
	                	pinOverlay = brand12Overlay;
	                } else if (info.Brand.compareTo("GENERAL") == 0) {
	                	pinOverlay = brand13Overlay;
	                } else if (info.Brand.compareTo("ITOCHU") == 0) {
	                	pinOverlay = brand14Overlay;
	                } else {
	                	pinOverlay = brand99Overlay;
	                }
                }
                
                GeoPoint point = new GeoPoint(
                        (int) ((double) info.getLatitude() * E6),
                        (int) (Double.parseDouble(info.getLongitude()) * E6));
                
                if (pinOverlay.checkPoint(point) == false) {
    	            pinOverlay.addPoint(point);
    	            pinOverlay.setMsg(info.ShopName + "\n" + info.Brand + "\n" + info.Address + "\n" + info.Price + "円");
    	            pinOverlay.setPrice(info.Price);
    	            pinOverlay.setPinType(PreferenceManager.getDefaultSharedPreferences(this).getString("settings_pin_type", "price"));
       	            pinOverlay.setGSInfo(info);
      	            mMapView.getOverlays().add(pinOverlay);
                }
            }
            
            /*
	        LocationOverlay location = new LocationOverlay(this);
	        location.setMyLocation(myLocation);
	        mMapView.getOverlays().add(location);
*/
            mMapView.invalidate();
        }		
	}

    /**
     * 位置情報の更新を処理するリスナー
     */
    private LocationListener mListener = new LocationListener() {
        public void onStatusChanged(String provider, int status, Bundle extras) {
        	
        	switch (status) {
        	case LocationProvider.AVAILABLE:
        		break;
        	case LocationProvider.OUT_OF_SERVICE:
        		Toast.makeText(MainActivity.this, "GPSサービスが利用できません", Toast.LENGTH_LONG).show();
        		break;
        	case LocationProvider.TEMPORARILY_UNAVAILABLE:
//        		Toast.makeText(MainActivity.this, "GPSデータを取得できません", Toast.LENGTH_LONG).show();
        		break;
        	}
        }
        
        public void onProviderEnabled(String provider) { }
        public void onProviderDisabled(String provider) { }

        public void onLocationChanged(Location location) {
            
            Log.d(LOG_TAG, "longitude = " + location.getLongitude());
            Log.d(LOG_TAG, "latitude = " + location.getLatitude());

            MainActivity.myLocation = location;     
        }
    };
}
