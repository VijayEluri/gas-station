package org.chrysaor.android.gas_station.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import org.chrysaor.android.gas_station.R;
import org.chrysaor.android.gas_station.util.ErrorReporter;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class AboutActivity extends Activity {
	
    GoogleAnalyticsTracker tracker;

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.about);
	    
	    ErrorReporter.setup(this);
	    ErrorReporter.bugreport(AboutActivity.this);

	    tracker = GoogleAnalyticsTracker.getInstance();
	    
	    // Start the tracker in manual dispatch mode...
	    tracker.start("UA-20090562-2", 20, this);
	    tracker.trackPageView("/AboutActivity");

	    Button backButton = (Button) findViewById(R.id.btn_back);
	    backButton.setOnClickListener(new OnClickListener() {
 
        	@Override
            public void onClick(View v) {
                finish();	
            }
        });
    }
	
    @Override
    protected void onDestroy() {
      super.onDestroy();
      // Stop the tracker when it is no longer needed.
      tracker.stop();
    }
}
