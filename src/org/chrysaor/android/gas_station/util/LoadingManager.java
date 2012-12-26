package org.chrysaor.android.gas_station.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface.OnCancelListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

/**
 * ローディング
 * 
 * @author Shinichi Matsuo
 * 
 */
public class LoadingManager {

    protected View viewMain = null;
    protected View viewLoading = null;
    protected Context context;
    protected int mode = 0;
    protected String message = "loading...";
    protected ProgressDialog dialog;

    /**
     * 
     */
    public static final int MODE_NONE = 0;
    /**
     * ページローディング
     */
    public static final int MODE_PAGE = 1;

    /**
     * ダイアログローディング
     */
    public static final int MODE_DIALOG = 2;

    /**
     * コンストラクタ
     * 
     * @param context
     * @param viewLoading
     *            ローディングのView
     * @param viewMain
     *            処理中非表示にするView
     */
    public LoadingManager(Context context, View viewLoading, View viewMain,
            int mode) {
        this.viewMain = viewMain;
        this.viewLoading = viewLoading;
        this.context = context;
        this.mode = mode;

        setDialog();
    }

    /**
     * コンストラクタ
     * 
     * @param context
     * @param mode
     */
    public LoadingManager(Context context, int mode) {
        this.context = context;
        this.mode = mode;

        setDialog();
    }

    /**
     * 
     * @param viewLoading
     * @param viewMain
     */
    public void setLoadingViews(View viewLoading, View viewMain) {
        this.viewMain = viewMain;
        this.viewLoading = viewLoading;
    }

    private void setDialog() {
        dialog = new ProgressDialog(context);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage(message);
        dialog.setCancelable(false);

    }

    public void setMessage(String message) {
        this.message = message;
        dialog.setMessage(message);
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    /**
     * ローディングレイアウトを表示するメソッド
     * 
     */
    public void showLoading() {
        showLoading(false);
    }

    /**
     * ローディングレイアウトを表示するメソッド
     * 
     */
    public void showLoading(boolean cancelable) {
        switch (mode) {
        case MODE_PAGE:
            showPageLoading();
            break;
        case MODE_DIALOG:
            showDialogLoading(cancelable);
            break;
        }
    }

    /**
     * ローディングレイアウトを非表示にするメソッド
     * 
     */
    public void hideLoading() {

        // ローディングViewの非表示
        hidePageLoading();

        // ローディングダイアログの非表示
        hideDialogLoading();
    }

    /**
     * ローディングの表示チェック
     * 
     * true:表示、false:非表示
     * 
     * @return
     */
    public boolean isShowing() {

        boolean res = false;

        switch (mode) {
        case MODE_PAGE:
            ViewGroup parent = (ViewGroup) viewMain.getParent();
            res = (parent.indexOfChild(viewLoading) != -1);
            break;
        case MODE_DIALOG:
            res = dialog.isShowing();
            break;
        }

        return res;
    }

    /**
     * ローディングViewの表示メソッド
     */
    protected void showPageLoading() {
        if (viewMain == null || viewLoading == null) {
            return;
        }

        ViewGroup parent = (ViewGroup) viewMain.getParent();
        parent.addView(viewLoading, new LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.FILL_PARENT));
        viewMain.setVisibility(View.GONE);
    }

    /**
     * ローディングViewの非表示メソッド
     */
    protected void hidePageLoading() {
        if (viewMain != null) {
            if (isShowing()) {
                ViewGroup parent = (ViewGroup) viewMain.getParent();
                parent.removeView(viewLoading);
            }
            viewMain.setVisibility(View.VISIBLE);
        }
    }

    /**
     * ダイアログ
     */
    private void showDialogLoading(boolean cancelable) {
        if (dialog.isShowing() == false) {
            dialog.setCancelable(cancelable);
            dialog.show();
        }
    }

    private void hideDialogLoading() {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public void setOnCancelListener(OnCancelListener listener) {
        dialog.setOnCancelListener(listener);
    }
}
