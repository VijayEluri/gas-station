package org.chrysaor.android.gas_station.ui;

import java.util.ArrayList;

import jp.co.nobot.libYieldMaker.libYieldMaker;

import net.londatiga.android.ActionItem;
import net.londatiga.android.QuickAction;

import org.chrysaor.android.gas_station.MainActivity;
import org.chrysaor.android.gas_station.R;
import org.chrysaor.android.gas_station.util.DatabaseHelper;
import org.chrysaor.android.gas_station.util.GSInfo;
import org.chrysaor.android.gas_station.util.StandAdapter;
import org.chrysaor.android.gas_station.util.StandsDao;
import org.chrysaor.android.gas_station.util.Utils;

import com.google.android.maps.GeoPoint;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class ListActivity extends Activity {
    private ArrayList<GSInfo> list = null;
    private StandAdapter adapter = null;
    private DatabaseHelper dbHelper = null;
    private SQLiteDatabase db = null;
    private StandsDao standsDao = null;
    private static String mode = "none";
    private SharedPreferences pref = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.list);
	    
	    // 初期化
	    pref = PreferenceManager.getDefaultSharedPreferences(ListActivity.this);
	    mode = pref.getString("settings_sort", getResources().getString(R.string.settings_sort_default));
    	dbHelper = new DatabaseHelper(this);
    	db = dbHelper.getWritableDatabase();
    	Utils.logging(mode);
    	standsDao = new StandsDao(db);
        list = standsDao.findAll(mode);
        db.close();
        init();

	    RadioButton priceButton = (RadioButton) findViewById(R.id.sort_price);
	    
	    priceButton.setOnClickListener(new OnClickListener() {
 
        	@Override
            public void onClick(View v) {
            	db = dbHelper.getWritableDatabase();
    	    	standsDao = new StandsDao(db);
    	    	mode = "price";
    	        list = standsDao.findAll(mode);
    	        db.close();
    	        init();
            }
        });

	    RadioButton distanceButton = (RadioButton) findViewById(R.id.sort_distance);
	    
	    distanceButton.setOnClickListener(new OnClickListener() {
 
        	@Override
            public void onClick(View v) {
            	db = dbHelper.getWritableDatabase();
    	    	standsDao = new StandsDao(db);
    	    	mode = "dist";
    	        list = standsDao.findAll(mode);
    	        db.close();
    	        init();
            }
        });
	    
	    if (mode.equals("price")) {
	    	priceButton.setChecked(true);
	    } else {
	    	distanceButton.setChecked(true);
	    }

	    if (Utils.isDonate(this)) {
	    	LinearLayout head = (LinearLayout) findViewById(R.id.header_ad);
	    	head.setVisibility(View.GONE);
	    } else {
	//    	LinearLayout head = (LinearLayout) findViewById(R.id.header_ad);
	        libYieldMaker mv = (libYieldMaker)findViewById(R.id.admakerview);
	        mv.setActivity(this);
	        mv.setUrl("http://images.ad-maker.info/apps/x0umfpssg2zu.html");
	        mv.startView();
	    }
        
	    Button backButton = (Button) findViewById(R.id.btn_back);
	    backButton.setOnClickListener(new OnClickListener() {
 
        	@Override
            public void onClick(View v) {
                finish();	
            }
        });
    }
	
	private void init() {
        if (list.size() > 0) {
		    ListView savedList = (ListView) findViewById(R.id.savedList);
		    adapter = new StandAdapter(this, R.layout.list, list);  
		    savedList.setAdapter(adapter);  
        
	        savedList.setOnItemClickListener(new OnItemClickListener() {
	 
	        	@Override
	            public void onItemClick(AdapterView<?> adapter,
	                    View view, int position, long id) {
	    	        final GSInfo item = list.get(position);
	    	        
	    	        ActionItem item1 = new ActionItem();
	    	        item1.setTitle("地図");
	    	        item1.setIcon(getResources().getDrawable(R.drawable.map_blue));
	    	        item1.setOnClickListener(new OnClickListener() {
	    	        	@Override
	    	        	public void onClick(View v) {
							Intent intent =new Intent();
							intent.putExtra("lat", item.Latitude);
							intent.putExtra("lon", item.Longitude);
							setResult(Activity.RESULT_OK,intent);
							//アクティビティの終了
							finish();
	    	        	}
	    	        });
	    	        
	    	        ActionItem item2 = new ActionItem();
	    	        item2.setTitle("詳細");
	    	        item2.setIcon(getResources().getDrawable(R.drawable.info));
	    	        item2.setOnClickListener(new OnClickListener() {
	    	        	@Override
	    	        	public void onClick(View v) {
					        Intent intent1 = new Intent(ListActivity.this, DetailActivity.class);
			                intent1.putExtra("shopcode", item.ShopCode);
					        startActivity(intent1);  
	    	        	}
	    	        });
	    	        
	    	        ActionItem item3 = new ActionItem();
	    	        item3.setTitle("ルート検索");
	    	        item3.setIcon(getResources().getDrawable(R.drawable.green_flag));
	    	        item3.setOnClickListener(new OnClickListener() {
	    	        	@Override
	    	        	public void onClick(View v) {
			            	Intent intent = new Intent(); 
			                intent.setAction(Intent.ACTION_VIEW); 
			                intent.setClassName("com.google.android.apps.maps","com.google.android.maps.MapsActivity");
			                intent.setData(Uri.parse("http://maps.google.com/maps?myl=saddr&daddr=" + item.Address + "&dirflg=d")); 
			                startActivity(intent);
	    	        	}
	    	        });
	    	        
	    	        ActionItem item4 = new ActionItem();
	    	        item4.setTitle("価格投稿");
	    	        item4.setIcon(getResources().getDrawable(R.drawable.pencil));
	    	        item4.setOnClickListener(new OnClickListener() {
	    	        	@Override
	    	        	public void onClick(View v) {
	    	        		//TODO
	    	        	}
	    	        });
	    	        

    	            QuickAction qa = new QuickAction(view);
    	            //onCreate()の中で作ったActionItemをセットする
    	            qa.addActionItem(item1);
    	            qa.addActionItem(item2);
    	            if (Utils.isDonate(ListActivity.this)) {
    	            	qa.addActionItem(item3);
    	            }
//    	            qa.addActionItem(item4);
    	            //アニメーションを設定する
    	            qa.setAnimStyle(QuickAction.ANIM_AUTO);
    	            qa.show();
	    	        /*
	    	        AlertDialog dialog = new AlertDialog.Builder(ListActivity.this)
	    	        .setItems(items, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							switch (which) {
							case 0:
				        		Intent intent =new Intent();
				                intent.putExtra("lat", item.Latitude);
				                intent.putExtra("lon", item.Longitude);
				                setResult(Activity.RESULT_OK,intent);
				                //アクティビティの終了
				                finish();
								break;
							case 1:
						        Intent intent1 = new Intent(ListActivity.this, DetailActivity.class);
				                intent1.putExtra("shopcode", item.ShopCode);
						        startActivity(intent1);  
								break;
							case 2:
				                try {
				                    String msg = pref.getString("settings_sharemsg", getResources().getString(R.string.settings_sharemsg_default));
				                    msg = msg.replaceAll("#price", item.Price).replaceAll("#shop_name", item.ShopName);

				                	Intent intent2 = new Intent();
				                	intent2.setAction(Intent.ACTION_SEND);
				                	intent2.setType("text/plain");
				                	intent2.putExtra(Intent.EXTRA_TEXT, msg);
				                	startActivity(intent2);
				                } catch (Exception e) {
//				                	Log.d(TAG, "Error");
				                }
				                break;
							}							
						}
					}).create();
	    	        dialog.show();
	    	        */
	            }
	        });
        }	
	}
}
