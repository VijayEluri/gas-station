package org.chrysaor.android.gas_station.util;

import net.nend.android.NendAdView;

import org.chrysaor.android.gas_station.R;

import android.app.Activity;

import com.google.ads.AdSize;
import com.google.ads.mediation.MediationAdRequest;
import com.google.ads.mediation.customevent.CustomEventBanner;
import com.google.ads.mediation.customevent.CustomEventBannerListener;

public class NendAd implements CustomEventBanner {

    @Override
    public void destroy() {
        // TODO 自動生成されたメソッド・スタブ

    }

    @Override
    public void requestBannerAd(final CustomEventBannerListener listener,
            final Activity activity, String label, String serverParameter,
            AdSize adSize, MediationAdRequest request, Object customEventExtra) {

        int spotId = Integer.valueOf(activity.getResources().getString(
                R.string.nend_spotid));
        String apiKey = activity.getString(R.string.nend_apiid);

        NendAdView nendAdView = new NendAdView(
                activity.getApplicationContext(), spotId, apiKey);

        listener.onReceivedAd(nendAdView);

    }

}
