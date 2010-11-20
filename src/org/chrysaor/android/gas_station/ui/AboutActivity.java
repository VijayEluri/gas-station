package org.chrysaor.android.gas_station.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import org.chrysaor.android.gas_station.R;
import org.chrysaor.android.gas_station.util.ErrorReporter;

public class AboutActivity extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.about);
	    
	    ErrorReporter.setup(this);
	    ErrorReporter.bugreport(AboutActivity.this);

	    Button backButton = (Button) findViewById(R.id.btn_back);
	    backButton.setOnClickListener(new OnClickListener() {
 
        	@Override
            public void onClick(View v) {
                finish();	
            }
        });
    }
}
