package org.chrysaor.android.gas_station.util;

import java.util.ArrayList;

import org.chrysaor.android.gas_station.R;
import android.content.Context;
import android.graphics.Typeface;
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
//		private Drawable[] images = new Drawable[14];
		  
		public StandAdapter(Context context, int textViewResourceId, ArrayList<GSInfo> items) {  
	        super(context, textViewResourceId, items);  
		    this.items = items;
		    this.context = context;
		    this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
		}  
		  
		@Override  
		public View getView(int position, View convertView, ViewGroup parent) {

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

	            StandsHelper helper = StandsHelper.getInstance();
	            brand.setImageDrawable(context.getResources().getDrawable(helper.getBrandImage(item.Brand, Integer.valueOf(item.Price))));
	        }
	        return view;  
	    }  
	}  