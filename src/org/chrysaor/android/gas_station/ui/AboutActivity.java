package org.chrysaor.android.gas_station.ui;

import org.chrysaor.android.gas_station.MainActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import org.chrysaor.android.gas_station.R;

public class AboutActivity extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.about);
	    
        Button btn = (Button) this.findViewById(R.id.close);  
        
        btn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
                Button01_OnClick();  
				
			} 
        });
    }
	
	private void Button01_OnClick() {
		finish();
	}
}
