package org.chrysaor.android.gas_station.util;

/**
 * AsyncTaskのdoInBackgroundからonPostExecuteに渡す引数に仕様するクラス
 * 
 * @author tomorrowkey
 * 
 * @param <T>
 */
public class AsyncTaskResult<T> {
    /**
     * AsyncTaskで取得したデータ
     */
    private T content;

    /**
     * エラーコード
     */
    private int errorCode;

    /**
     * エラーならtrueが設定されている
     */
    private boolean isError;

    /**
     * コンストラクタ
     * 
     * @param content
     *          AsyncTaskで取得したデータ
     * @param isError
     *          エラーならtrueを設定する
     * @param errorCode
     *          エラーコードを指定する
     */
    private AsyncTaskResult(T content, boolean isError, int errorCode) {
        this.content = content;
        this.isError = isError;
        this.errorCode = errorCode;
    }

    /**
     * AsyncTaskで取得したデータを返す
     * 
     * @return AsyncTaskで取得したデータ
     */
    public T getContent() {
      return content;
    }

    /**
     * エラーならtrueを返す
     * 
     * @return エラーならtrueを返す
     */
    public boolean isError() {
        return isError;
    }

    /**
     * エラーコードを返す
     * 
     * @return エラーコードを返す
     */
    public int getErrorCode() {
        return errorCode;
    }

    /**
     * AsyncTaskが正常終了した場合の結果を作る
     * 
     * @param <T>
     * @param content
     *          AsyncTaskで取得したデータを指定する
     * @return AsyncTaskResult
     */
    public static <T> AsyncTaskResult<T> createSuccessResult(T content) {
        return new AsyncTaskResult<T>(content, false, 0);
    }

    /**
     * AsyncTaskが異常終了した場合の結果を作る
     * 
     * @param <T>
     * @param errorCode
     *          エラーコードを指定する
     * @return AsyncTaskResult
     */
    public static <T> AsyncTaskResult<T> createErrorResult(int errorCode) {
        return new AsyncTaskResult<T>(null, true, errorCode);
    }
}