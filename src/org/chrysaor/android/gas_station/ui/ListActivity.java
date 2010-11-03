package org.chrysaor.android.gas_station.ui;

import java.util.ArrayList;

import org.chrysaor.android.gas_station.R;
import org.chrysaor.android.gas_station.util.DatabaseHelper;
import org.chrysaor.android.gas_station.util.GSInfo;
import org.chrysaor.android.gas_station.util.StandAdapter;
import org.chrysaor.android.gas_station.util.StandsDao;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.AdapterView.OnItemClickListener;

public class ListActivity extends Activity {
    private ArrayList<GSInfo> list = null;
    private StandAdapter adapter = null;
    private DatabaseHelper dbHelper = null;
    private SQLiteDatabase db = null;
    private StandsDao standsDao = null;
    private static String mode = "none";


	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.list);
	    
    	dbHelper = new DatabaseHelper(this);
    	db = dbHelper.getWritableDatabase();
    	
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
    	    	mode = "distance";
    	        list = standsDao.findAll(mode);
    	        db.close();
    	        init();
            }
        });
	    
	    if (mode == "price") {
	    	priceButton.setChecked(true);
	    } else {
//	    } else if (mode == "distance") {
	    	distanceButton.setChecked(true);
	    	
	    }


	    Button closeButton = (Button) findViewById(R.id.close);
    
	    closeButton.setOnClickListener(new OnClickListener() {
 
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
	    	        GSInfo item = list.get(position);
	        		Intent intent =new Intent();
	                intent.putExtra("lat", item.Latitude);
	                intent.putExtra("lon", item.Longitude);
	                setResult(Activity.RESULT_OK,intent);
	                
	                //アクティビティの終了
	                finish();	
	            }
	        });
        }	
	}
}
