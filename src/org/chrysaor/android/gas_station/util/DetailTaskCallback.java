package org.chrysaor.android.gas_station.util;

import org.chrysaor.android.gas_station.lib.dto.Stand;

import android.graphics.Bitmap;

/**
 * DownloadImageTaskのコールバックインターフェイス
 * 
 * @author tomorrowkey
 * 
 */
public interface DetailTaskCallback {
  /**
   * 画像のダウンロードが成功した時に呼ばれるメソッド
   * 
   * @param image
   *          ダウンロードした画像
   */
  void onSuccess(Stand info);

  /**
   * 画像のダウンロードが失敗した時に呼ばれるメソッド
   * 
   * @param errorCode
   *          エラーコード
   */
  void onFailed(int errorCode);
}