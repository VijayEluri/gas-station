package org.chrysaor.android.gas_station.util;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

public abstract class AbstractLoadingTask<Params, Progress, Result> extends
        AsyncTask<Params, Progress, Result> {

    // アイコンを表示するビュー
    protected View viewMain;
    protected View viewLoading;
    protected Context context;

    /**
     * コンストラクタ
     * 
     * @param context
     * @param viewLoading
     *            ローディングのView
     * @param viewMain
     *            処理中非表示にするView
     */
    public AbstractLoadingTask(Context context, View viewLoading, View viewMain) {
        this.context = context;
        this.viewMain = viewMain;
        this.viewLoading = viewLoading;
    }

    /**
     * ローディングレイアウトを表示するメソッド
     * 
     * onPreExecuteメソッドで呼び出してください
     */
    protected void showLoading() {
        ViewGroup parent = (ViewGroup) viewMain.getParent();
        parent.addView(viewLoading, new LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.FILL_PARENT));
        viewMain.setVisibility(View.GONE);
    }

    /**
     * ローディングレイアウトを非表示にするメソッド
     * 
     * onPostExecuteメソッドで呼び出してください
     */
    protected void hideLoading() {
        viewLoading.setVisibility(View.GONE);
        viewMain.setVisibility(View.VISIBLE);
    }
}
