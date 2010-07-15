package com.paintail.android;

import com.paintail.android.util.GSInfo;
import com.paintail.android.util.WebApi;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringReader;
//import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

//import java.util.ArrayList;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;

import com.paintail.android.R;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.R.drawable;
import android.app.AlertDialog;
import android.app.ProgressDialog;
//import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.Log;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.util.Xml;

import com.admob.android.ads.AdView;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.paintail.android.ui.AboutActivity;
import com.paintail.android.ui.SettingsActivity;
import com.paintail.android.util.CenterCircleOverlay;
import com.paintail.android.util.StandController;
import com.paintail.android.util.XmlParserFromUrl;
import com.paintail.android.util.InfoController;
import com.paintail.android.util.LocationOverlay;

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

	//�V���񐶐��N���X
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
        
        AdView adView = new AdView(this); 
        adView.setVisibility(android.view.View.VISIBLE); 
        adView.requestFreshAd(); 
        adView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        mMapView.addView(adView);

//        mMapView.invalidate();
	    
        // �ʒu���̎擾���J�n
        mLocationManager = ((LocationManager) getSystemService(Context.LOCATION_SERVICE));
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_MIN_TIME,
                LOCATION_MIN_DISTANCE, mListener);
    }
	
    
    @Override
    protected void onResume() {
        if (mLocationManager != null) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_MIN_TIME,
                    LOCATION_MIN_DISTANCE, mListener);
        }
        
        super.onResume();

        Log.d(LOG_TAG, "resume");
    }
    
    @Override
    protected void onPause() {
        if (mLocationManager != null) {
            mLocationManager.removeUpdates(mListener);
        }
        
        super.onPause();
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
      String action = "";
      switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
          action = "ACTION_DOWN";
          break;
      case MotionEvent.ACTION_UP:
          action = "ACTION_UP";
          break;
      case MotionEvent.ACTION_MOVE:
          action = "ACTION_MOVE";
          CenterCircleOverlay location = new CenterCircleOverlay(this);
          mMapView.getOverlays().add(location);
          break;
      case MotionEvent.ACTION_CANCEL:
          action = "ACTION_CANCEL";
          break;
      }
  	  return true;
    }
	
	public class PinItemizedOverlay extends ItemizedOverlay<PinOverlayItem> implements Runnable {

	    private List<GeoPoint> points = new ArrayList<GeoPoint>();
	    private List<String> msgs = new ArrayList<String>();
	    private List<String> prices = new ArrayList<String>();
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
	    
	    public void setMsg(String msg) {
	        this.msgs.add(msg);
	    }
	    
	    public void setPrice(String title) {
	        this.prices.add(title);
	    }
	    
	    public void setGSInfo(GSInfo info) {
	        this.gsInfo.add(info);
	    }
	    
		/**
		 * �A�C�e�����^�b�v���ꂽ���̏���
		 */
		@Override
		protected boolean onTap(int index) {
			
			//�}�b�v���S�̎��ӂɂ���K�\�����X�^���h�����擾����
			stand = new StandController(handler, this, MainActivity.this, gsInfo.get(index));

	        //�v���O���X�_�C�A���O��\��
            resource = getResources();
            dialog = new ProgressDialog(MainActivity.this);
	        dialog.setIndeterminate(true);
	        dialog.setMessage(resource.getText(R.string.dialog_message_getting_data));
	        dialog.show();

			// �}�b�v�̒��S���W���A�^�b�v���ꂽ�A�C�e���ɍ��킹��
			// mapControler�́A�p�b�P�[�W�X�R�[�v�Ő錾
			mMapController.animateTo(this.getItem(index).getPoint());

	        stand.start();
			return true;
		}
		
		@Override
		public void run() {
			//�v���O���X�_�C�A���O�����
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
		    
		    if (pin_type.compareTo("brand") == 0) {
		    	return;
		    }
		    
            for (int i=0;i<prices.size();i++) {

            	String price = prices.get(i);
            	GeoPoint locate = points.get(i);
                
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
		// TODO �����������ꂽ���\�b�h�E�X�^�u
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu( Menu menu ) {
	    super.onCreateOptionsMenu( menu );
	    // ���j���[�A�C�e����ǉ�
	    MenuItem item1 = menu.add( 0, 2, 0, "����" );
	    MenuItem item2 = menu.add( 0, 0, 0, "�ݒ�" );
	    MenuItem item0 = menu.add( 0, 1, 0, "���ݒn" );
	    MenuItem item3 = menu.add( 0, 3, 0, "about" );
	    // �ǉ��������j���[�A�C�e���̃A�C�R����ݒ�
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
	        if (myLocation == null) {
        		Toast.makeText(MainActivity.this, "���ݒn�����ł��܂���", Toast.LENGTH_LONG).show();
	            return true;
	        }
	        
	        LocationOverlay location = new LocationOverlay(this);
	        location.setMyLocation(myLocation);
	        mMapView.getOverlays().add(location);

            // �擾�����ʒu���A�}�b�v�̒��S��ݒ�
            mMapController.animateTo( new GeoPoint(
                    (int) (myLocation.getLatitude() * E6),
                    (int) (myLocation.getLongitude() * E6)));
                        
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

                // �n�}�̒��S�ʒu���擾
                GeoPoint center = mMapView.getMapCenter();
                url_string = url_string + "&lat=" +  (double) center.getLatitudeE6() / E6;
                url_string = url_string + "&lon=" +  (double) center.getLongitudeE6() / E6;

                String url = url_string + "&sort=d";
                Log.d(LOG_TAG, "url = " + url.toString());
                
    			//�}�b�v���S�̎��ӂɂ���K�\�����X�^���h�����擾����
    			infoController = new InfoController(handler, this, url);

    	        //�v���O���X�_�C�A���O��\��
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
	//�s�s�ꗗ�̎擾�����I����Ɏ��s�����
	public void run() {
		//�v���O���X�_�C�A���O�����
		dialog.dismiss();

        ArrayList<GSInfo> list = infoController.getGSInfoList();
        PinItemizedOverlay pinOverlay = null;
        
        //�擾�Ɏ��s
		if(list == null | list.size() <= 0) {
    		Toast.makeText(this, resource.getText(R.string.dialog_message_out_of_range), Toast.LENGTH_LONG).show();
/*
			AlertDialog.Builder ad = new AlertDialog.Builder(this);
		    ad.setPositiveButton("OK",new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog,int whichButton) {
		            setResult(RESULT_OK);
		        }
		    });
			ad.setTitle(resource.getText(R.string.dialog_title_err));
			ad.setIcon(drawable.ic_dialog_alert);
			ad.setMessage(resource.getText(R.string.dialog_message_out_of_range));
			ad.show();
			*/
		} else {
    		Toast.makeText(this, list.size() + "���̃X�^���h��������܂���", Toast.LENGTH_LONG).show();

	    	// MapView��ɕ\���������r�b�g�}�b�v�����A���\�[�X����擾
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
    	        pinOverlay.addPoint( new GeoPoint(
                        (int) ((double) info.getLatitude() * E6),
                        (int) (Double.parseDouble(info.getLongitude()) * E6)));
    	        pinOverlay.setMsg(info.ShopName + "\n" + info.Brand + "\n" + info.Address + "\n" + info.Price + "�~");
    	        pinOverlay.setPrice(info.Price);
       	        pinOverlay.setGSInfo(info);
      	        mMapView.getOverlays().add(pinOverlay);

            }
            
	        LocationOverlay location = new LocationOverlay(this);
	        location.setMyLocation(myLocation);
	        mMapView.getOverlays().add(location);

            mMapView.invalidate();
        }
	}

    /**
     * �ʒu���̍X�V���������郊�X�i�[
     */
    private LocationListener mListener = new LocationListener() {
        public void onStatusChanged(String provider, int status, Bundle extras) {
        	
        	switch (status) {
        	case LocationProvider.AVAILABLE:
        		break;
        	case LocationProvider.OUT_OF_SERVICE:
        		Toast.makeText(MainActivity.this, "GPS�T�[�r�X�����p�ł��܂���", Toast.LENGTH_LONG).show();
        		break;
        	case LocationProvider.TEMPORARILY_UNAVAILABLE:
//        		Toast.makeText(MainActivity.this, "GPS�f�[�^���擾�ł��܂���", Toast.LENGTH_LONG).show();
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
