package org.chrysaor.android.gas_station.util;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.SeekBar;

public abstract class AbstractPopupWindow extends PopupWindow {

    // このクラスを呼び出すアクティビィティのルートビュー
    protected View mParent;

    public View dialogView;

    public AbstractPopupWindow(View parent) {
        super(parent);

        mParent = parent;

        this.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss() {
                if (dialogView instanceof View) {
                    cleanupView(dialogView);
                }
            }
        });
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

    /**
     * 呼び出し側の画面に表示する。
     * 
     * @param gravity
     *            表示方向
     * @param x
     *            ウインドウのX軸の開始位置オフセット
     * @param y
     *            ウインドウのY軸の開始位置オフセット
     */
    public void showAtLocation(int gravity, int x, int y) {
        // Buttons setup.
        // applyButtons();
        // PopupWindowクラスのメソッドを呼び出す
        super.showAtLocation(mParent, gravity, x, y);
    }

}