package org.chrysaor.android.gas_station.util;

import java.io.InputStream;

import org.chrysaor.android.gas_station.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class StandController extends Thread {
    private Handler handler;
    private final Runnable listener;
    private View scroll;
    private Context context;
    private GSInfo info;
    private LayoutInflater inflater;
    private Handler mHandler = new Handler();
    
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
        
        final View view = inflater.inflate(R.layout.gsinfo, null);
        
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display disp = wm.getDefaultDisplay();
        view.setMinimumWidth(disp.getWidth() - 20);
//        int width = disp.getWidth();
//        int height = disp.getHeight();

        if (info != null) {
            // ブランド
            ImageView imgBrand = (ImageView) view.findViewById(R.id.brand_image);
            StandsHelper helper = StandsHelper.getInstance();
            imgBrand.setImageResource(helper.getBrandImage(info.Brand, Integer.valueOf(info.Price)));

            // 油種
            TextView textKind = (TextView) view.findViewById(R.id.txt_kind);
            String[] kinds = this.context.getResources().getStringArray(R.array.settings_list_kind);
            textKind.setText(kinds[Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this.context).getString("settings_kind", "0"))]);

            // 価格
            TextView textMember = (TextView) view.findViewById(R.id.txt_member);
            Utils.logging(String.valueOf(info.Member));
            if (info.Member == true) {
                textMember.setText("会員価格");
            } else {
                textMember.setText("現金フリー");
            }

            // 価格
            TextView textPrice = (TextView) view.findViewById(R.id.price);
            textPrice.setText(info.getDispPrice());
            textPrice.setTextColor(info.getDispPriceColor());

            // セルフ
            if (info.Self != null && info.Self.compareTo("SELF") != 0) {
                ImageView imgSelf = (ImageView) view.findViewById(R.id.self);
                imgSelf.setVisibility(View.INVISIBLE);
            }

            // 24時間営業
            if (info.Rtc != null && info.Rtc.compareTo("24H") != 0) {
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
            
            new Thread(new Runnable() {
                public void run() {
                    //画像
//                    Bitmap imgBitmap;
                    final ImageView imgView = (ImageView) view.findViewById(R.id.shop_image);

                    final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.ProgressBar01);
                    final String url = "http://gogo.gs/images/rally/" + info.ShopCode + "-" + info.Photo + ".jpg";

                    BitmapFactory.Options bfo = new BitmapFactory.Options(); 
                    InputStream in = null;
                    try {
                        bfo.inPurgeable = true;
                        bfo.inPreferredConfig = Bitmap.Config.ARGB_4444;
//                        bfo.inSampleSize = 2;
                        
                        in = WebApi.getHttpInputStream(url);
//                        imgBitmap = BitmapFactory.decodeStream(in, null, bfo);
                        ImageCache.setImage(url, BitmapFactory.decodeStream(in, null, bfo));
                        in.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

//                    imgBitmap = WebApi.getImageBitmapOnWeb(url, imgBitmap);

                    mHandler.post(new Runnable() {
                        public void run() {
                            
                            Bitmap imgBitmap = ImageCache.getImage(url);
                            if(imgBitmap != null) {
                                imgView.setMaxWidth(view.getWidth() - 40);
                                imgView.setVisibility(View.VISIBLE);
                                imgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                imgView.setImageBitmap(imgBitmap);
                            }

                            progressBar.setVisibility(View.GONE);
                        }
                    });
                    
                }
            }).start();
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
