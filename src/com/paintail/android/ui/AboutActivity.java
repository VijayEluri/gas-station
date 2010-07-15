package com.paintail.android.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.paintail.android.MainActivity;
import com.paintail.android.R;

public class AboutActivity extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.about);
	    
        Button btn = (Button) this.findViewById(R.id.Button01);  
        
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
