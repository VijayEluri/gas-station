package org.chrysaor.android.gas_station.util;

import org.chrysaor.android.gas_station.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

public class StandController extends Thread {
    private static final String LOG_TAG = "StandContoller";
    private Handler handler;
    private final Runnable listener;
    private View scroll;
    private Context context;
    private GSInfo info;
	private LayoutInflater inflater;
    
	public StandController(Handler handler, Runnable listener, Context context, GSInfo info) {
		this.handler   = handler;
		this.listener  = listener;
		this.context   = context;
		this.info      = info;
	    this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);  

	}

    @Override
    public void run() {
    	setDispGASStand();

        handler.post(listener);
    }
    
    public void setDispGASStand() {
    	
        View view = inflater.inflate(R.layout.gsinfo, null);

		if (info != null) {
	        ImageView imgBrand = (ImageView) view.findViewById(R.id.brand_image);

        	if (info.Brand.compareTo("JOMO") == 0) {
				imgBrand.setImageResource(R.drawable.jomo);
            } else if (info.Brand.compareTo("ESSO") == 0) {
				imgBrand.setImageResource(R.drawable.esso);
            } else if (info.Brand.compareTo("ENEOS") == 0) {
				imgBrand.setImageResource(R.drawable.eneos);
            } else if (info.Brand.compareTo("KYGNUS") == 0) {
				imgBrand.setImageResource(R.drawable.kygnus);
            } else if (info.Brand.compareTo("COSMO") == 0) {
				imgBrand.setImageResource(R.drawable.icon_maker6);
            } else if (info.Brand.compareTo("SHELL") == 0) {
				imgBrand.setImageResource(R.drawable.shell);
            } else if (info.Brand.compareTo("IDEMITSU") == 0) {
				imgBrand.setImageResource(R.drawable.icon_maker8);
            } else if (info.Brand.compareTo("IDEMITSU") == 0) {
				imgBrand.setImageResource(R.drawable.icon_maker9);
            } else if (info.Brand.compareTo("MOBIL") == 0) {
				imgBrand.setImageResource(R.drawable.icon_maker10);
            } else if (info.Brand.compareTo("SOLATO") == 0) {
				imgBrand.setImageResource(R.drawable.icon_maker11);
            } else if (info.Brand.compareTo("JA-SS") == 0) {
				imgBrand.setImageResource(R.drawable.icon_maker12);
            } else if (info.Brand.compareTo("GENERAL") == 0) {
				imgBrand.setImageResource(R.drawable.icon_maker13);
            } else if (info.Brand.compareTo("ITOCHU") == 0) {
				imgBrand.setImageResource(R.drawable.icon_maker14);
            } else {
				imgBrand.setImageResource(R.drawable.icon_maker99);
            }


			// 価格
            TextView textPrice = (TextView) view.findViewById(R.id.price);
			textPrice.setText("価格" + info.Price + "円");

			// セルフ
			if (info.Self.compareTo("SELF") != 0) {
				ImageView imgSelf = (ImageView) view.findViewById(R.id.self);
				imgSelf.setVisibility(View.INVISIBLE);
			}

			// 24時間営業
			if (info.Rtc.compareTo("24H") != 0) {
				ImageView imgRtc = (ImageView) view.findViewById(R.id.rtc);
				imgRtc.setVisibility(View.INVISIBLE);
			}				

			// 店名
			TextView textShopName = (TextView) view.findViewById(R.id.shop_text);
			textShopName.setText(info.ShopName);

			// 住所
			TextView textAddress = (TextView) view.findViewById(R.id.address_text);
			textAddress.setText(info.Address);

			// 距離
			TextView textDistance = (TextView) view.findViewById(R.id.distance_text);
			Float dist = Float.parseFloat(info.Distance) / 1000;
			textDistance.setText(dist.toString() + "km");

			// 更新日
			TextView textDate = (TextView) view.findViewById(R.id.date_text);
			textDate.setText(info.Date);

            TextView link = (TextView)view.findViewById(R.id.link_text);  

			// LinkMovementMethod のインスタンスを取得します
	        MovementMethod movementmethod = LinkMovementMethod.getInstance();
	        
	        // TextView に LinkMovementMethod を登録します
	        link.setMovementMethod(movementmethod);
	        
	        // <a>タグを含めたテキストを用意します
	        String html = "<a href=\"http://gogo.gs/shop/" + info.ShopCode + ".html\">PCサイト</a> " +
	                      "<a href=\"http://m.gogo.gs/shop/?code=" + info.ShopCode + "\">携帯サイト</a>";
	        
	        // URLSpan をテキストにを組み込みます
	        CharSequence spanned = Html.fromHtml(html);
	        
	        link.setText(spanned);
	        
			//画像
			Bitmap imgBitmap;
			ImageView imgView = (ImageView) view.findViewById(R.id.shop_image);
			String url = "http://gogo.gs/images/rally/" + info.ShopCode + "-" + info.Photo + ".jpg";
//            Log.d(LOG_TAG, "url = " + url);

			imgBitmap = WebApi.getImageBitmapOnWeb(url);

			if(imgBitmap != null) {
				imgView.setImageBitmap(imgBitmap);
				imgView.setMaxWidth(view.getWidth() - 40);
			}
		}
        
		scroll = view;
    }
    
	public View getView() {
	    return scroll;
	}
	
	public GSInfo getGSInfo() {
		return info;
	}
}
