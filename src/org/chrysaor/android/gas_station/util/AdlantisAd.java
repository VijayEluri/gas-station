package org.chrysaor.android.gas_station.util;

import jp.adlantis.android.AdlantisView;
import jp.adlantis.android.utils.AdlantisUtils;

import org.chrysaor.android.gas_station.R;

import android.app.Activity;
import android.widget.LinearLayout;

import com.google.ads.AdSize;
import com.google.ads.mediation.MediationAdRequest;
import com.google.ads.mediation.customevent.CustomEventBanner;
import com.google.ads.mediation.customevent.CustomEventBannerListener;

public class AdlantisAd implements CustomEventBanner {

    @Override
    public void destroy() {
        // TODO 自動生成されたメソッド・スタブ

    }

    @Override
    public void requestBannerAd(final CustomEventBannerListener listener,
            final Activity activity, String label, String serverParameter,
            AdSize adSize, MediationAdRequest request, Object customEventExtra) {

        String publisherId = activity.getResources().getString(
                R.string.adlantis_publisher_id);

        AdlantisView adView = new AdlantisView(activity);
        adView.setPublisherID(publisherId);
        adView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT, AdlantisUtils
                        .adHeightPixels(activity)));

        listener.onReceivedAd(adView);

    }

}
