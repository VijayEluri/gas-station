package org.chrysaor.android.gas_station.util;

import java.io.InputStream;

import org.chrysaor.android.gas_station.R;
import org.chrysaor.android.gas_station.lib.data.Price;
import org.chrysaor.android.gas_station.lib.dto.Stand;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DetailAsyncTask extends
        AbstractLoadingTask<String, Void, AsyncTaskResult<Stand>> {

    private Handler mHandler = new Handler();
    private static Typeface tf;
    private static final int PRICE_FONT_SIZE = 25;
    public TextView txtRegularPrice = null;
    public TextView txtHighocPrice = null;
    public TextView txtDieselPrice = null;
    public TextView txtLampPrice = null;
    public TextView txtRegularDate = null;
    public TextView txtHighocDate = null;
    public TextView txtDieselDate = null;
    public TextView txtLampDate = null;
    public TextView txtMemRegularPrice = null;
    public TextView txtMemHighocPrice = null;
    public TextView txtMemDieselPrice = null;
    public TextView txtMemLampPrice = null;
    public TextView txtMemRegularDate = null;
    public TextView txtMemHighocDate = null;
    public TextView txtMemDieselDate = null;
    public TextView txtMemLampDate = null;
    private Stand info = null;
    private DetailTaskCallback callback;

    public DetailAsyncTask(Context context, View viewLoading, View viewMain) {
        super(context, viewLoading, viewMain);

        tf = Typeface.createFromAsset(context.getAssets(), "fonts/7barPBd.TTF");
    }

    public DetailAsyncTask(Context context, View viewLoading, View viewMain,
            DetailTaskCallback callback) {
        super(context, viewLoading, viewMain);
        this.callback = callback;

        tf = Typeface.createFromAsset(context.getAssets(), "fonts/7barPBd.TTF");
    }

    @Override
    protected AsyncTaskResult<Stand> doInBackground(String... params) {
        StandsHelper helper = StandsHelper.getInstance();
        Stand info = helper.getGsInfo(context, params[0]);

        if (info == null) {
            return AsyncTaskResult.createErrorResult(0);
        } else {
            return AsyncTaskResult.createSuccessResult(info);
        }
    }

    @Override
    protected void onPreExecute() {
        showLoading();
    }

    protected void onPostExecute(AsyncTaskResult<Stand> result) {

        hideLoading();

        if (result.isError()) {
            // エラーをコールバックで返す
            callback.onFailed(result.getErrorCode());
            return;
        } else {
            info = result.getContent();
        }

        // ブランド
        ImageView imgBrand = (ImageView) viewMain
                .findViewById(R.id.brand_image);
        StandsHelper helper = StandsHelper.getInstance();
        imgBrand.setImageResource(helper.getBrandImage(info.brand,
                Integer.valueOf(info.price)));

        // セルフ
        if (info.self != null && info.self.compareTo("SELF") != 0) {
            ImageView imgSelf = (ImageView) viewMain.findViewById(R.id.self);
            imgSelf.setVisibility(View.INVISIBLE);
        }

        // 24時間営業
        if (info.rtc != null && info.rtc.compareTo("24H") != 0) {
            ImageView imgRtc = (ImageView) viewMain.findViewById(R.id.rtc);
            imgRtc.setVisibility(View.INVISIBLE);
        }

        // 店名
        TextView textShopName = (TextView) viewMain
                .findViewById(R.id.shop_text);
        textShopName.setText(info.shopName);

        // 住所
        TextView textAddress = (TextView) viewMain
                .findViewById(R.id.address_text);
        textAddress.setText(info.address);

        // 距離
        TextView textDistance = (TextView) viewMain
                .findViewById(R.id.distance_text);
        if (info.distance != null) {
            Float dist = Float.parseFloat(info.distance) / 1000;
            textDistance.setText(dist.toString() + "km");
        }

        new Thread(new Runnable() {
            public void run() {
                // 画像
                // Bitmap imgBitmap;
                final ImageView imgView = (ImageView) viewMain
                        .findViewById(R.id.shop_image);

                final ProgressBar progressBar = (ProgressBar) viewMain
                        .findViewById(R.id.ProgressBar01);
                final String url = "http://gogo.gs/images/rally/"
                        + info.shopCode + "-" + info.photo + ".jpg";

                BitmapFactory.Options bfo = new BitmapFactory.Options();
                InputStream in = null;
                try {
                    bfo.inPurgeable = true;
                    bfo.inPreferredConfig = Bitmap.Config.ARGB_4444;
                    // bfo.inSampleSize = 2;

                    in = WebApi.getHttpInputStream(url);
                    // imgBitmap = BitmapFactory.decodeStream(in, null, bfo);
                    ImageCache.setImage(url,
                            BitmapFactory.decodeStream(in, null, bfo));
                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // imgBitmap = WebApi.getImageBitmapOnWeb(url, imgBitmap);

                mHandler.post(new Runnable() {
                    public void run() {

                        Bitmap imgBitmap = ImageCache.getImage(url);
                        if (imgBitmap != null) {
                            imgView.setMaxWidth(viewMain.getWidth() - 40);
                            imgView.setVisibility(View.VISIBLE);
                            imgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            imgView.setImageBitmap(imgBitmap);
                        }

                        progressBar.setVisibility(View.GONE);
                    }
                });

            }
        }).start();

        txtRegularPrice = (TextView) viewMain
                .findViewById(R.id.txt_regular_price);
        txtRegularPrice.setTextSize(PRICE_FONT_SIZE);
        txtRegularPrice.setTypeface(tf);

        txtHighocPrice = (TextView) viewMain
                .findViewById(R.id.txt_highoc_price);
        txtHighocPrice.setTextSize(PRICE_FONT_SIZE);
        txtHighocPrice.setTypeface(tf);

        txtDieselPrice = (TextView) viewMain
                .findViewById(R.id.txt_diesel_price);
        txtDieselPrice.setTextSize(PRICE_FONT_SIZE);
        txtDieselPrice.setTypeface(tf);

        txtLampPrice = (TextView) viewMain.findViewById(R.id.txt_lamp_price);
        txtLampPrice.setTextSize(PRICE_FONT_SIZE);
        txtLampPrice.setTypeface(tf);

        txtRegularDate = (TextView) viewMain
                .findViewById(R.id.txt_regular_date);
        txtHighocDate = (TextView) viewMain.findViewById(R.id.txt_highoc_date);
        txtDieselDate = (TextView) viewMain.findViewById(R.id.txt_diesel_date);
        txtLampDate = (TextView) viewMain.findViewById(R.id.txt_lamp_date);

        txtMemRegularPrice = (TextView) viewMain
                .findViewById(R.id.txt_mem_regular_price);
        txtMemRegularPrice.setTextSize(PRICE_FONT_SIZE);
        txtMemRegularPrice.setTypeface(tf);

        txtMemHighocPrice = (TextView) viewMain
                .findViewById(R.id.txt_mem_highoc_price);
        txtMemHighocPrice.setTextSize(PRICE_FONT_SIZE);
        txtMemHighocPrice.setTypeface(tf);

        txtMemDieselPrice = (TextView) viewMain
                .findViewById(R.id.txt_mem_diesel_price);
        txtMemDieselPrice.setTextSize(PRICE_FONT_SIZE);
        txtMemDieselPrice.setTypeface(tf);

        txtMemLampPrice = (TextView) viewMain
                .findViewById(R.id.txt_mem_lamp_price);
        txtMemLampPrice.setTextSize(PRICE_FONT_SIZE);
        txtMemLampPrice.setTypeface(tf);

        txtMemRegularDate = (TextView) viewMain
                .findViewById(R.id.txt_mem_regular_date);
        txtMemHighocDate = (TextView) viewMain
                .findViewById(R.id.txt_mem_highoc_date);
        txtMemDieselDate = (TextView) viewMain
                .findViewById(R.id.txt_mem_diesel_date);
        txtMemLampDate = (TextView) viewMain
                .findViewById(R.id.txt_mem_lamp_date);

        Thread thread = new Thread() {
            public void run() {
                try {
                    final Price[] prices;

                    if (info.priceList == null) {
                        prices = GoGoGsApi.getPrices(info.shopCode);
                    } else {
                        prices = info.priceList;
                    }

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {

                            if (prices == null) {
                                return;
                            }

                            for (Price price : prices) {
                                if (price == null) {
                                    continue;
                                }
                                int mode = price.getMode();
                                int member = price.getMember();

                                // レギュラー（現金価格）
                                if (mode == GoGoGsApi.MODE_REGULAR
                                        && GoGoGsApi.isMember(member) == false) {
                                    Utils.logging("price:"
                                            + price.getPrice().toString());
                                    Utils.logging("date:"
                                            + price.getDate().toString());
                                    txtRegularPrice.setText(price.getPrice()
                                            .toString());
                                    txtRegularDate.setText(price.getDate());
                                }

                                // ハイオク（現金価格）
                                if (mode == GoGoGsApi.MODE_HIGHOC
                                        && GoGoGsApi.isMember(member) == false) {

                                    txtHighocPrice.setText(price.getPrice()
                                            .toString());
                                    txtHighocDate.setText(price.getDate());
                                }

                                // 軽油（現金価格）
                                if (mode == GoGoGsApi.MODE_DIESEL
                                        && GoGoGsApi.isMember(member) == false) {
                                    txtDieselPrice.setText(price.getPrice()
                                            .toString());
                                    txtDieselDate.setText(price.getDate());
                                }

                                // 灯油（現金価格）
                                if (mode == GoGoGsApi.MODE_LAMP
                                        && GoGoGsApi.isMember(member) == false) {

                                    txtLampPrice.setText(price.getPrice()
                                            .toString());
                                    txtLampDate.setText(price.getDate());
                                }

                                // レギュラー（会員価格）
                                if (mode == GoGoGsApi.MODE_REGULAR
                                        && GoGoGsApi.isMember(member) == true) {
                                    txtMemRegularPrice.setText(price.getPrice()
                                            .toString());
                                    txtMemRegularDate.setText(price.getDate());

                                }

                                // ハイオク（会員価格）
                                if (mode == GoGoGsApi.MODE_HIGHOC
                                        && GoGoGsApi.isMember(member) == true) {
                                    txtMemHighocPrice.setText(price.getPrice()
                                            .toString());
                                    txtMemHighocDate.setText(price.getDate());
                                }

                                // 軽油（会員価格）
                                if (mode == GoGoGsApi.MODE_DIESEL
                                        && GoGoGsApi.isMember(member) == true) {
                                    txtMemDieselPrice.setText(price.getPrice()
                                            .toString());
                                    txtMemDieselDate.setText(price.getDate());
                                }

                                // 灯油（会員価格）
                                if (mode == GoGoGsApi.MODE_LAMP
                                        && GoGoGsApi.isMember(member) == true) {
                                    txtMemLampPrice.setText(price.getPrice()
                                            .toString());
                                    txtMemLampDate.setText(price.getDate());
                                }

                            }
                        }
                    });

                } catch (final Exception e) {
                    e.printStackTrace();
                }

            }
        };
        thread.start();

        callback.onSuccess(result.getContent());
    }
}
