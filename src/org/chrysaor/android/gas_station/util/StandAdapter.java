package org.chrysaor.android.gas_station.util;

import java.util.ArrayList;
import java.util.Arrays;

import org.chrysaor.android.gas_station.R;
import org.chrysaor.android.gas_station.ui.DetailActivity;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class StandAdapter extends ArrayAdapter {
  
    private ArrayList<GSInfo> items;
    private LayoutInflater inflater;
    private Context context;
    private GSInfo item;
    private String[] favList;
    private DatabaseHelper dbHelper = null;
    private SQLiteDatabase db = null;
    private Integer[] favStates;

    
    public StandAdapter(Context context, int textViewResourceId, ArrayList<GSInfo> items) {
        super(context, textViewResourceId, items);
        this.items = items;
        this.context = context;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        favStates = new Integer[items.size()];
        dbHelper = new DatabaseHelper(context);
        updateFavList();

    }
    
    private void updateFavList() {
        db = dbHelper.getReadableDatabase();
        FavoritesDao favoriteDao = new FavoritesDao(db);
        favList = favoriteDao.getShopCdList();
        db.close();
    }
    
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

    	
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
                if (item.Price.equals("9999")) {
                    price.setText("no data");
                } else {
                    price.setText(item.Price + "円");
                }
                price.setTextColor(item.getDispPriceColor());
            }

            TextView dist = (TextView)view.findViewById(R.id.distance);
            // テキストをビューにセット
            if (dist != null) {
            	if (item.Distance != null) {
	                Float distance = Float.parseFloat(item.Distance) / 1000;
	                dist.setText(distance.toString() + "km");
            	} else {
                	// 距離が登録されてない（お気に入りGS）の場合、非表示
            		dist.setVisibility(View.GONE);
            	}
            }
        
            ImageView brand = (ImageView)view.findViewById(R.id.icon);

            StandsHelper helper = StandsHelper.getInstance();
            brand.setImageDrawable(context.getResources().getDrawable(helper.getBrandImage(item.Brand, Integer.valueOf(item.Price))));
            
            final ImageView imgFavorite = (ImageView) view.findViewById(R.id.img_favorite);
            
            if (favList == null || Arrays.binarySearch(favList, item.ShopCode) < 0) {
            	imgFavorite.setImageDrawable(context.getResources().getDrawable(R.drawable.star_empty24));
            	favStates[position] = 0;
            } else {
                imgFavorite.setImageDrawable(context.getResources().getDrawable(R.drawable.star_full24));
            	favStates[position] = 1;
            }
            imgFavorite.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					GSInfo item = (GSInfo)items.get(position);
	                
			        db = dbHelper.getReadableDatabase();
	                FavoritesDao favoritesDao = new FavoritesDao(db);
	                
	                switch (favStates[position]) {
	                case 0:
	                    // 登録件数の確認
	                    if (favoritesDao.findAll("create_date").size() >= 20) {
	                        Toast.makeText(context, "お気に入りは20件までしか登録出来ません。", Toast.LENGTH_SHORT).show();
	                    } else {
	                        favoritesDao.insert(item);
	                        imgFavorite.setImageDrawable(context.getResources().getDrawable(R.drawable.star_full24));
		                	favStates[position] = 1;
	                    }
	                    break;
	                case 1:
	                    favoritesDao.deleteByShopCd(item.ShopCode);
	                	imgFavorite.setImageDrawable(context.getResources().getDrawable(R.drawable.star_empty24));
	                	favStates[position] = 0;
	                    break;
	                }
	                db.close();
	                updateFavList();
	                Utils.logging(favStates[position].toString());
				}
			});
        }
        return view;
    }
    
}