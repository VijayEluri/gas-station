package org.chrysaor.android.gas_station.util;

import android.view.Gravity;

public class ViewAlign {

    public static int leftTop() {
        return Gravity.LEFT | Gravity.TOP;
    }

    public static int rightTop() {
        return Gravity.RIGHT | Gravity.TOP;
    }

    public static int leftBottom() {
        return Gravity.LEFT | Gravity.BOTTOM;
    }

    public static int rightBottom() {
        return Gravity.RIGHT | Gravity.BOTTOM;
    }

    public static int centerTop() {
        return Gravity.CENTER | Gravity.TOP;
    }

    public static int centerBottom() {
        return Gravity.CENTER | Gravity.BOTTOM;
    }

    public static int centerLeft() {
        return Gravity.CENTER | Gravity.LEFT;
    }

    public static int centerRight() {
        return Gravity.CENTER | Gravity.RIGHT;
    }

    // 今回はダイアログなので画面中心

    public static int center() {
        return Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
    }
}
