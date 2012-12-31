package org.chrysaor.android.gas_station.activity;

import java.util.Random;

import jp.adlantis.android.AdlantisView;
import jp.adlantis.android.utils.AdlantisUtils;
import net.nend.android.NendAdView;

import org.chrysaor.android.gas_station.R;
import org.chrysaor.android.gas_station.util.ErrorReporter;
import org.chrysaor.android.gas_station.util.GasStaApplication;
import org.chrysaor.android.gas_station.util.Utils;

import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.LinearLayout.LayoutParams;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.google.android.maps.MapActivity;

public abstract class AbstractMyMapActivity extends MapActivity {

    protected GoogleAnalyticsTracker tracker;
    public static String lastEvent = null;
    protected GasStaApplication app;

    /** ブランドID定数 */
    public static final String[] brands = { "1", "2", "3", "4", "6", "7", "8",
            "9", "10", "11", "12", "13", "14", "99" };

    /** ブランド名定数 */
    public static final String[] brands_value = { "JOMO", "ESSO", "ENEOS",
            "KYGNUS", "COSMO", "SHELL", "IDEMITSU", "MITSUI", "MOBIL",
            "SOLATO", "JA-SS", "GENERAL", "ITOCHU", "OTHER" };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ErrorReporter.setup(this);
        ErrorReporter.bugreport(this);

        app = (GasStaApplication) getApplication();
        tracker = GoogleAnalyticsTracker.getInstance();

        // Start the tracker in manual dispatch mode...
        tracker.start(this.getResources().getString(R.string.tracking_code),
                20, this);
        tracker.trackPageView("/" + this.getClass().getName());
        tracker.setCustomVar(1, "Model", Build.MODEL, 1);
        try {
            tracker.setCustomVar(2, "Version", getPackageManager()
                    .getPackageInfo(getPackageName(), 1).versionName, 1);
        } catch (NameNotFoundException e) {
        }
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        // Stop the tracker when it is no longer needed.
        tracker.stop();

        ViewGroup root = (ViewGroup) getWindow().getDecorView().findViewById(
                android.R.id.content);
        cleanupView(findViewById(root.getId()));

    }

    /**
     * 指定したビュー階層内のドローワブルをクリアする。 （ドローワブルをのコールバックメソッドによるアクティビティのリークを防ぐため）
     * 
     * @param view
     */
    public final void cleanupView(View view) {

        if (view instanceof ImageButton) {
            ImageButton ib = (ImageButton) view;
            ib.setImageDrawable(null);
        } else if (view instanceof ImageView) {
            ImageView iv = (ImageView) view;
            iv.setImageDrawable(null);
        } else if (view instanceof SeekBar) {
            SeekBar sb = (SeekBar) view;
            sb.setProgressDrawable(null);
            sb.setThumb(null);
        }
        view.setBackgroundDrawable(null);
        if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;
            int size = vg.getChildCount();
            for (int i = 0; i < size; i++) {
                cleanupView(vg.getChildAt(i));
            }
        }
    }

    protected void setAdView() {
        LinearLayout layoutAd = (LinearLayout) findViewById(R.id.layoutAd);

        if (Utils.isDonate(getApplicationContext())) {
            layoutAd.setVisibility(View.INVISIBLE);
        } else {
            layoutAd.setVisibility(View.VISIBLE);
            Random rnd = new Random();

            int ran = rnd.nextInt(2);

            switch (ran) {
            case 0: // Nend
                showNendAdView(layoutAd);
                break;
            case 1:
            default:
                // AdLantis
                showAdLantisView(layoutAd);
                break;
            }
        }
    }

    /**
     * Nendの広告を表示
     * 
     * @param view
     */
    private void showNendAdView(LinearLayout view) {
        NendAdView nendAdView = new NendAdView(
                getApplicationContext(),
                Integer.parseInt(getResources().getString(R.string.nend_spotid)),
                getResources().getString(R.string.nend_apiid));
        view.addView(nendAdView, new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    }

    /**
     * Adlantisの広告を表示
     * 
     * @param view
     */
    private void showAdLantisView(LinearLayout view) {
        AdlantisView adView = new AdlantisView(this);
        view.addView(
                adView,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.FILL_PARENT, AdlantisUtils
                                .adHeightPixels(this)));
    }

    protected final SimpleOnGestureListener simpleOnGestureListener = new SimpleOnGestureListener() {

        @Override
        public boolean onDoubleTap(MotionEvent event) {
            lastEvent = "onDoubleTap";
            return super.onDoubleTap(event);
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent event) {
            lastEvent = "onDoubleTapEvent";
            return super.onDoubleTapEvent(event);
        }

        @Override
        public boolean onDown(MotionEvent event) {
            lastEvent = "onDown";
            return super.onDown(event);
        }

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                float velocityX, float velocityY) {
            lastEvent = "onFling";
            return super.onFling(event1, event2, velocityX, velocityY);
        }

        @Override
        public void onLongPress(MotionEvent event) {
            lastEvent = "onLongPress";
            super.onLongPress(event);
        }

        @Override
        public boolean onScroll(MotionEvent event1, MotionEvent event2,
                float distanceX, float distanceY) {
            lastEvent = "onScroll";
            return super.onScroll(event1, event2, distanceX, distanceY);
        }

        @Override
        public void onShowPress(MotionEvent event) {
            lastEvent = "onShowPress";
            super.onShowPress(event);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {
            lastEvent = "onSingleTapConfirmed";
            return super.onSingleTapConfirmed(event);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            lastEvent = "onSingleTapUp";
            return super.onSingleTapUp(event);
        }
    };
}
