package org.chrysaor.android.gas_station.ui;

import jp.co.nobot.libYieldMaker.libYieldMaker;

import org.chrysaor.android.gas_station.MainActivity;
import org.chrysaor.android.gas_station.R;
import org.chrysaor.android.gas_station.util.DatabaseHelper;
import org.chrysaor.android.gas_station.util.ErrorReporter;
import org.chrysaor.android.gas_station.util.GSInfo;
import org.chrysaor.android.gas_station.util.SeekBarPreference;
import org.chrysaor.android.gas_station.util.StandController;
import org.chrysaor.android.gas_station.util.StandsDao;
import org.chrysaor.android.gas_station.util.Utils;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.google.android.maps.GeoPoint;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class DetailActivity extends Activity implements Runnable {
	
    private DatabaseHelper dbHelper = null;
    private SQLiteDatabase db = null;
    private StandsDao standsDao = null;
	private final Handler handler = new Handler();
    private StandController stand;
    private GSInfo info = null;
    private static final Integer pressed_color = Color.argb(80, 255, 255, 255);
    GoogleAnalyticsTracker tracker;

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
//	    setContentView(R.layout.gsinfo);
	    
	    ErrorReporter.setup(this);
	    ErrorReporter.bugreport(DetailActivity.this);

	    tracker = GoogleAnalyticsTracker.getInstance();
	    
	    // Start the tracker in manual dispatch mode...
	    tracker.start("UA-20090562-2", 20, this);
	    tracker.trackPageView("/DetailActivity");

	    Bundle extras=getIntent().getExtras();
        if (extras!=null) {
        	String index = extras.getString("shopcode");
        	
        	dbHelper = new DatabaseHelper(this);
        	db = dbHelper.getReadableDatabase();
        	
        	standsDao = new StandsDao(db);
            info = standsDao.findByShopCd(index);
            db.close();
            
			//マップ中心の周辺にあるガソリンスタンド情報を取得する
			stand = new StandController(handler, (Runnable) this, this, info);

			stand.setDispGASStand();
			
	    	setContentView(stand.getView());
	    	
	    	LinearLayout route = (LinearLayout) findViewById(R.id.layout_route);
	    	route.setOnTouchListener(new View.OnTouchListener()	{
	    		public boolean onTouch(View v, MotionEvent event) {
	    			if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
	    				v.setBackgroundColor(pressed_color);
	    			} else if(event.getAction() == MotionEvent.ACTION_UP) {
	    				v.setBackgroundColor(Color.TRANSPARENT);
	    				
	    		        // イベントトラック（ルート検索）
	    		        tracker.trackEvent(
	    		            "Detail",      // Category
	    		            "RouteSearch",     // Action
	    		            info.ShopCode, // Label
	    		            0);
	    		        
	    			    if (Utils.isDonate(DetailActivity.this)) {
			            	Intent intent = new Intent(); 
			                intent.setAction(Intent.ACTION_VIEW); 
			                intent.setClassName("com.google.android.apps.maps","com.google.android.maps.MapsActivity");
			                intent.setData(Uri.parse("http://maps.google.com/maps?myl=saddr&daddr=" + info.Address + "&dirflg=d")); 
			                startActivity(intent);
	    			    } else {
	    			    	Toast.makeText(DetailActivity.this, "無料版では使用できません", Toast.LENGTH_SHORT).show();
	    			    }

	    			}
	    			return true;
	    		}	
	    	});
	    	
		    LinearLayout post = (LinearLayout) findViewById(R.id.layout_post);
	    	post.setOnTouchListener(new View.OnTouchListener()	{
	    		public boolean onTouch(View v, MotionEvent event) {
	    			if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
	    				v.setBackgroundColor(pressed_color);
	    			} else if(event.getAction() == MotionEvent.ACTION_UP) {
	    				v.setBackgroundColor(Color.TRANSPARENT);
	    				
	    		        // イベントトラック（価格投稿）
	    		        tracker.trackEvent(
	    		            "Detail",      // Category
	    		            "Post",        // Action
	    		            info.ShopCode, // Label
	    		            0);
				        Intent intent = new Intent(DetailActivity.this, PostActivity.class);
		                intent.putExtra("shopcode", info.ShopCode);
				        startActivity(intent);
	    			}
	    			return true;
	    		}	
	    	});

		    LinearLayout browser = (LinearLayout) findViewById(R.id.layout_browser);
		    browser.setOnTouchListener(new View.OnTouchListener()	{
	    		public boolean onTouch(View v, MotionEvent event) {
	    			if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
	    				v.setBackgroundColor(pressed_color);
	    			} else if(event.getAction() == MotionEvent.ACTION_UP) {
	    				v.setBackgroundColor(Color.TRANSPARENT);
	    				
	    		        // イベントトラック（ブラウザ）
	    		        tracker.trackEvent(
	    		            "Detail",      // Category
	    		            "Browser",     // Action
	    		            info.ShopCode, // Label
	    		            0);
	    		        
	    	        	Intent intent = new Intent(); 
	    	            intent.setAction(Intent.ACTION_VIEW); 
	    	            intent.setData(Uri.parse("http://gogo.gs/shop/" + info.ShopCode + ".html")); 
	    	            startActivity(intent);
	    			}
	    			return true;
	    		}	
	    	});

        }
        
	    Button backButton = (Button) findViewById(R.id.btn_back);
	    backButton.setOnClickListener(new OnClickListener() {
 
        	@Override
            public void onClick(View v) {
                finish();	
            }
        });
    }

	@Override
	public void run() {
		// TODO 自動生成されたメソッド・スタブ
	}
	
	@Override
	public boolean onCreateOptionsMenu( Menu menu ) {
	    super.onCreateOptionsMenu( menu );
	    // メニューアイテムを追加
//	    MenuItem item0 = menu.add( 0, 0, 0, "ブラウザ" );
	    MenuItem item1 = menu.add( 0, 1, 0, "共有" );
	    MenuItem item2 = menu.add( 0, 2, 0, "設定" );

	    // 追加したメニューアイテムのアイコンを設定
//	    item0.setIcon( android.R.drawable.ic_menu_view);
	    item1.setIcon( android.R.drawable.ic_menu_share);
	    item2.setIcon( android.R.drawable.ic_menu_preferences);
	    
	    if (Utils.isDonate(this) == false) {
	        MenuItem item3 = menu.add( 0, 3, 0, "有料版購入" );
	        item3.setIcon( R.drawable.cart);
	    }

	    return true;
	}
	
	@Override  
	public boolean onOptionsItemSelected(MenuItem item){

	    switch(item.getItemId()){
	    case 0:
	    	
	        // イベントトラック（ブラウザ）
	        tracker.trackEvent(
	            "Detail",      // Category
	            "Browser",     // Action
	            info.ShopCode, // Label
	            0);
	        
        	Intent intent = new Intent(); 
            intent.setAction(Intent.ACTION_VIEW); 
            intent.setData(Uri.parse("http://gogo.gs/shop/" + info.ShopCode + ".html")); 
            startActivity(intent);
	        break;
	    case 1:
            try {
                String msg = PreferenceManager.getDefaultSharedPreferences(DetailActivity.this).getString("settings_sharemsg", getResources().getString(R.string.settings_sharemsg_default));
                
                String[] kinds = getResources().getStringArray(R.array.settings_list_kind);
                String kind = kinds[Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString("settings_kind", "0"))];

                if (info.Price.equals("9999")) {
                	msg = msg.replaceAll("#price", info.getDispPrice());
                } else {                	
                    if (kind.equals("灯油")) {
                    	msg = msg.replaceAll("#price", info.getDispPrice() + "円/18L");                    	
                    } else {
                    	msg = msg.replaceAll("#price", info.getDispPrice() + "円/L");
                    }
                }
                msg = msg.replaceAll("#shop_name", info.ShopName).replaceAll("#kind", kind);

    	        // イベントトラック（共有）
    	        tracker.trackEvent(
    	            "Detail",      // Category
    	            "Share",       // Action
    	            info.ShopCode, // Label
    	            0);
    	        
            	Intent intent2 = new Intent();
            	intent2.setAction(Intent.ACTION_SEND);
            	intent2.setType("text/plain");
            	intent2.putExtra(Intent.EXTRA_TEXT, msg);
            	startActivity(intent2);
            } catch (Exception e) {
//            	Log.d(TAG, "Error");
            }
	        break;
	    case 2:
	    	
	        // イベントトラック（設定）
	        tracker.trackEvent(
	            "Detail",   // Category
	            "Settings", // Action
	            "",         // Label
	            0);

	        Intent intent3 = new Intent(DetailActivity.this, SettingsActivity.class);
	        startActivity(intent3);  
	        break;
	    case 3:
	        // イベントトラック（Donate）
	        tracker.trackEvent(
	            "Detail",      // Category
	            "Donate",      // Action
	            "",            // Label
	            0);
	        
        	Intent intent4 = new Intent(); 
            intent4.setAction(Intent.ACTION_VIEW); 
            intent4.setData(Uri.parse("market://details?id=org.chrysaor.android.gas_station.plus")); 
            startActivity(intent4);
	        break;

	    }
	    return false;  
	}
	
    @Override
    protected void onDestroy() {
      super.onDestroy();
      // Stop the tracker when it is no longer needed.
      tracker.stop();
    }
}
