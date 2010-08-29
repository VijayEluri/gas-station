package org.chrysaor.android.gas_station.util;

import java.util.ArrayList;

import org.chrysaor.android.gas_station.MainActivity;
import org.chrysaor.android.gas_station.R;
import org.chrysaor.android.gas_station.MainActivity.PinItemizedOverlay;
import org.chrysaor.android.gas_station.ui.ListActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class StandAdapter extends ArrayAdapter {  
		  
		private ArrayList<GSInfo> items;  
		private LayoutInflater inflater;
		private Context context;
		private GSInfo item;
		private Drawable[] images = new Drawable[14];
		  
		public StandAdapter(Context context, int textViewResourceId, ArrayList<GSInfo> items) {  
	        super(context, textViewResourceId, items);  
		    this.items = items;
		    this.context = context;
		    this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
		}  
		  
		@Override  
		public View getView(int position, View convertView, ViewGroup parent) {
			images[0] = context.getResources().getDrawable(R.drawable.jomo);
			images[1] = context.getResources().getDrawable(R.drawable.esso);
			images[2] = context.getResources().getDrawable(R.drawable.eneos);
			images[3] = context.getResources().getDrawable(R.drawable.kygnus);
			images[4] = context.getResources().getDrawable(R.drawable.icon_maker6);
			images[5] = context.getResources().getDrawable(R.drawable.shell);
			images[6] = context.getResources().getDrawable(R.drawable.icon_maker8);
			images[7] = context.getResources().getDrawable(R.drawable.icon_maker9);
			images[8] = context.getResources().getDrawable(R.drawable.icon_maker10);
			images[9] = context.getResources().getDrawable(R.drawable.icon_maker11);
			images[10] = context.getResources().getDrawable(R.drawable.icon_maker12);
			images[11] = context.getResources().getDrawable(R.drawable.icon_maker13);
			images[12] = context.getResources().getDrawable(R.drawable.icon_maker14);
			images[13] = context.getResources().getDrawable(R.drawable.icon_maker99);
			
	        // ビューを受け取る  
	        View view = convertView;  
	        if (view == null) {  
	            // 受け取ったビューがnullなら新しくビューを生成  
	            view = inflater.inflate(R.layout.list_row, null);  
	            // 背景画像をセットする  
	            view.setBackgroundResource(R.drawable.back);  
	        }  

	        // 表示すべきデータの取得
	        item = (GSInfo)items.get(position);
	        
	        if (item != null) {  
	            TextView screenName = (TextView)view.findViewById(R.id.shop_name);  
	            screenName.setTypeface(Typeface.DEFAULT_BOLD);  
	            if (screenName != null) {  
	                screenName.setText(item.ShopName);  
	            }
		  
	            // スクリーンネームをビューにセット  
	            TextView text = (TextView)view.findViewById(R.id.address);  
	            // テキストをビューにセット  
	            if (text != null) {  
	                text.setText(item.Address);  
	            }

	            TextView price = (TextView)view.findViewById(R.id.price);  
	            // テキストをビューにセット  
	            if (price != null) {  
	                price.setText(item.Price + "円");  
	            }

	            TextView dist = (TextView)view.findViewById(R.id.distance);  
	            // テキストをビューにセット  
	            if (dist != null) {
	    			Float distance = Float.parseFloat(item.Distance) / 1000;
	                dist.setText(distance.toString() + "km");  
	            }
	            	            
	            ImageView brand = (ImageView)view.findViewById(R.id.icon);  

            	if (item.Brand.compareTo("JOMO") == 0) {
            		brand.setImageDrawable(images[0]);
                } else if (item.Brand.compareTo("ESSO") == 0) {
            		brand.setImageDrawable(images[1]);
                } else if (item.Brand.compareTo("ENEOS") == 0) {
            		brand.setImageDrawable(images[2]);
                } else if (item.Brand.compareTo("KYGNUS") == 0) {
            		brand.setImageDrawable(images[3]);
                } else if (item.Brand.compareTo("COSMO") == 0) {
            		brand.setImageDrawable(images[4]);
                } else if (item.Brand.compareTo("SHELL") == 0) {
            		brand.setImageDrawable(images[5]);
                } else if (item.Brand.compareTo("IDEMITSU") == 0) {
            		brand.setImageDrawable(images[6]);
                } else if (item.Brand.compareTo("IDEMITSU") == 0) {
            		brand.setImageDrawable(images[7]);
                } else if (item.Brand.compareTo("MOBIL") == 0) {
            		brand.setImageDrawable(images[8]);
                } else if (item.Brand.compareTo("SOLATO") == 0) {
            		brand.setImageDrawable(images[9]);
                } else if (item.Brand.compareTo("JA-SS") == 0) {
            		brand.setImageDrawable(images[10]);
                } else if (item.Brand.compareTo("GENERAL") == 0) {
            		brand.setImageDrawable(images[11]);
                } else if (item.Brand.compareTo("ITOCHU") == 0) {
            		brand.setImageDrawable(images[12]);
                } else {
            		brand.setImageDrawable(images[13]);
                }
	        }
	        return view;  
	    }  
	}  