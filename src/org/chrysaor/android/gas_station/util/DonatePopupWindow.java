package org.chrysaor.android.gas_station.util;

import org.chrysaor.android.gas_station.R;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;

public class DonatePopupWindow extends AbstractPopupWindow {

    // このクラスを呼び出すアクティビィティのルートビュー
    private final View mParent;

    // このクラスを呼び出すアクティビィティのコンテキスト
    private final Context mContext;

    // メニューを動的に生成するインフレーター
    private final LayoutInflater mInflater;

    private View mDialogContainer;

    /**
     * コンストラクタ
     * 
     * @param context
     * @param parent
     */
    public DonatePopupWindow(Context context, View parent) {
        super(parent);
        mParent = parent;
        mContext = parent.getContext();
        mInflater = LayoutInflater.from(mContext);

        setPopupView();

    }

    public void setPopupView() {

        setAnimationStyle(R.style.PopupAnim);

        // ポップアップウインドウのルートビューをリソースから動的に生成
        dialogView = mInflater.inflate(R.layout.popup_donate, null);

        // 生成したルートビューをこのクラスにセット
        setContentView(dialogView);

        Button btnDonate = (Button) dialogView.findViewById(R.id.btnDonate);
        btnDonate.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri
                        .parse("market://details?id=org.chrysaor.android.gas_station.plus"));
                mContext.startActivity(intent);
            }
        });

        // スーパークラスのメソッドに設定
        setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        // ポップアップウインドウの高さを、表示するビューの高さにあわせる

        setHeight(WindowManager.LayoutParams.WRAP_CONTENT);

        // Backボタンの有効化
        // これがないとBackボタンが動かない
        setBackgroundDrawable(new BitmapDrawable());
        setOutsideTouchable(false);

        setFocusable(true);
        setTouchable(true);
    }

}
