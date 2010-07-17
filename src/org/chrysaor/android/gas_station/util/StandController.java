package org.chrysaor.android.gas_station.util;

import org.chrysaor.android.gas_station.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class StandController extends Thread {
    private static final String LOG_TAG = "StandContoller";
    private Handler handler;
    private final Runnable listener;
    private ScrollView scroll;
    private Context context;
    private GSInfo info;

    
	public StandController(Handler handler, Runnable listener, Context context, GSInfo info) {
		this.handler   = handler;
		this.listener  = listener;
		this.context   = context;
		this.info      = info;
	}

    @Override
    public void run() {
    	setDispGASStand();

        handler.post(listener);
    }
    
    public void setDispGASStand() {

    	ScrollView view =  new ScrollView(context);

		LinearLayout layoutDialogMain = new LinearLayout(context);
		LinearLayout layoutPrice      = new LinearLayout(context);
		LinearLayout layoutShopName   = new LinearLayout(context);
		LinearLayout layoutAddress    = new LinearLayout(context);
		LinearLayout layoutDistance   = new LinearLayout(context);
		LinearLayout layoutDate       = new LinearLayout(context);
		LinearLayout layoutIcon       = new LinearLayout(context);
		layoutDialogMain.setOrientation(LinearLayout.VERTICAL);
//		layoutIcon.setOrientation(LinearLayout.HORIZONTAL);
		if (info != null) {
			ImageView imgBrand = new ImageView(context);
			imgBrand.setPadding(5, 5, 0, 0);

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

			layoutPrice.addView(imgBrand);

			// 価格
			TextView textPrice = new TextView(context);
			textPrice.setText("価格" + info.Price + "円");
//			textPrice.setGravity(Gravity.RIGHT);
//			textIcon.setTextSize(WHEATER_FONT_SIZE);
			textPrice.setPadding(5, 10, 0, 0);
			layoutPrice.addView(textPrice);

			// セルフ
			if (info.Self.compareTo("SELF") == 0) {
				ImageView imgSelf = new ImageView(context);
				imgSelf.setPadding(5, 5, 0, 0);
				imgSelf.setImageResource(R.drawable.service002);
				layoutPrice.addView(imgSelf);
			}

			// 24時間営業
			if (info.Rtc.compareTo("24H") == 0) {
				ImageView imgRtc = new ImageView(context);
				imgRtc.setPadding(5, 5, 0, 0);
				imgRtc.setImageResource(R.drawable.service001);
				layoutPrice.addView(imgRtc);
			}				

			layoutDialogMain.addView(layoutPrice);

			// 店名
			TextView textShopName = new TextView(context);
			textShopName.setText("店名" + info.ShopName);
//			textShopName.setGravity(Gravity.RIGHT);
//			textIcon.setTextSize(WHEATER_FONT_SIZE);
			textShopName.setPadding(5, 5, 0, 0);
			layoutShopName.addView(textShopName);
			layoutDialogMain.addView(layoutShopName);

			// 住所
			TextView textAddress = new TextView(context);
			textAddress.setText("住所" + info.Address);
//			textAddress.setGravity(Gravity.RIGHT);
//			textIcon.setTextSize(WHEATER_FONT_SIZE);
			textAddress.setPadding(5, 5, 0, 0);
			layoutAddress.addView(textAddress);
			layoutDialogMain.addView(layoutAddress);

			// 距離
			TextView textDistance = new TextView(context);
			Float dist = Float.parseFloat(info.Distance) / 1000;
			textDistance.setText("距離" + dist.toString() + "km");
//			textDistance.setGravity(Gravity.RIGHT);
//			textDistance.setTextSize(WHEATER_FONT_SIZE);
			textDistance.setPadding(5, 5, 0, 0);
			layoutDistance.addView(textDistance);
			layoutDialogMain.addView(layoutDistance);

			// 更新日
			TextView textDate = new TextView(context);
			textDate.setText("更新日" + info.Date);
//			textDate.setGravity(Gravity.RIGHT);
//			textIcon.setTextSize(WHEATER_FONT_SIZE);
			textDate.setPadding(5, 5, 0, 0);
			layoutDate.addView(textDate);
			layoutDialogMain.addView(layoutDate);

			//画像
			Bitmap imgBitmap;
			ImageView imgView = new ImageView(context);
			String url = "http://gogo.gs/images/rally/" + info.ShopCode + "-" + info.Photo + ".jpg";
            Log.d(LOG_TAG, "url = " + url);

			imgBitmap = WebApi.getImageBitmapOnWeb(url);

			if(imgBitmap != null) {
				imgView.setImageBitmap(imgBitmap);
				imgView.setPadding(5, 0, 0, 0);
				layoutIcon.addView(imgView);
				layoutDialogMain.addView(layoutIcon);
			}
		}
        
		view.addView(layoutDialogMain);
		scroll = view;
    }
    
	public ScrollView getView() {
	    return scroll;
	}

    
}
