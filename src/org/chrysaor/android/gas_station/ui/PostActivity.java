package org.chrysaor.android.gas_station.ui;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.chrysaor.android.gas_station.R;
import org.chrysaor.android.gas_station.lib.database.DatabaseHelper;
import org.chrysaor.android.gas_station.lib.database.FavoritesDao;
import org.chrysaor.android.gas_station.lib.database.PostHistoriesDao;
import org.chrysaor.android.gas_station.lib.database.StandsDao;
import org.chrysaor.android.gas_station.util.ErrorReporter;
import org.chrysaor.android.gas_station.util.GSInfo;
import org.chrysaor.android.gas_station.util.GoGoGsApi;
import org.chrysaor.android.gas_station.util.PostItem;
import org.chrysaor.android.gas_station.util.StandsHelper;
import org.chrysaor.android.gas_station.util.Utils;
import org.chrysaor.android.gas_station.util.XmlParserFromUrl;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class PostActivity extends Activity {

    private Handler mHandler = new Handler();
    private ProgressDialog dialog;
    private DatabaseHelper dbHelper = null;
    private SQLiteDatabase db = null;
    private StandsDao standsDao = null;
    private GSInfo info = null;
    private Spinner price_kind;
    private Spinner check;
    private EditText regular;
    private EditText highoc;
    private EditText diesel;
    private EditText lamp;
    private EditText comment;
    private RadioGroup lampSelector;
    private TextView lampLabel;
    private CheckBox chkSaveSelectItem;
    private CheckBox chkTweet;
    private static final String apid = "gsearcho0o0";
    private static final String secretkey = "a456fwer7862343j4we8f54w634";
    private static final int HTTP_TIMEOUT = 10000;
    private static String ss_id;
    private SharedPreferences sp;
    private static boolean tweetFlg = false;
    GoogleAnalyticsTracker tracker;
    View loginView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setSoftInputMode(
                LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.post);

        ErrorReporter.setup(this);
        ErrorReporter.bugreport(PostActivity.this);

        tracker = GoogleAnalyticsTracker.getInstance();

        // Start the tracker in manual dispatch mode...
        tracker.start("UA-20090562-2", 20, this);
        tracker.trackPageView("/PostActivity");

        dbHelper = new DatabaseHelper(this);
        sp = PreferenceManager.getDefaultSharedPreferences(this);

        // 画面初期化
        init();

        if (info == null) {
            Toast.makeText(this, "スタンド情報が取得できません", Toast.LENGTH_SHORT).show();
            finish();
        }

        // ユーザID・パスワードの登録チェック
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(PostActivity.this);

        String user_id = pref.getString("settings_user_id", "");
        String passwd = pref.getString("settings_passwd_md5", "");

        if (user_id.equals("") || passwd.equals("")) {
            showAccountDialog();
        } else {
            try {
                auth(true);
            } catch (AuthException e) {
                Toast.makeText(PostActivity.this, e.getMessage(),
                        Toast.LENGTH_LONG).show();
                showAccountDialog();
            } catch (Exception e) {
                Toast.makeText(PostActivity.this, e.getMessage(),
                        Toast.LENGTH_LONG).show();
                showAccountDialog();
            }
        }

    }

    /**
     * アカウント設定ダイアログの表示
     */
    private void showAccountDialog() {

        loginView = getLoginView();

        new AlertDialog.Builder(this).setTitle("アカウント設定")
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        // 認証
                        try {
                            // 入力値をプリファレンスに登録する
                            EditText user_id = (EditText) loginView
                                    .findViewById(R.id.edit_user_id);
                            EditText passwd = (EditText) loginView
                                    .findViewById(R.id.edit_passwd);

                            // SharedPreferencesを取得
                            SharedPreferences sp;
                            sp = PreferenceManager
                                    .getDefaultSharedPreferences(PostActivity.this);

                            Editor editor = sp.edit();

                            editor.putString("settings_user_id", user_id
                                    .getText().toString());
                            editor.putString("settings_passwd_md5",
                                    Utils.md5(passwd.getText().toString()));
                            editor.commit();

                            // 認証処理
                            auth(true);

                            Toast.makeText(PostActivity.this, "認証に成功しました。",
                                    Toast.LENGTH_LONG).show();

                        } catch (AuthException e) {
                            Toast.makeText(PostActivity.this, e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                            showAccountDialog();
                        } catch (Exception e) {
                            Toast.makeText(PostActivity.this, e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                            showAccountDialog();
                        }

                    }

                }).setView(loginView).create().show();
    }

    private View getLoginView() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.login, null);

        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(PostActivity.this);

        EditText user_id = (EditText) view.findViewById(R.id.edit_user_id);
        user_id.setText(pref.getString("settings_user_id", ""));

        TextView textview = (TextView) view.findViewById(R.id.txt_msg);

        // LinkMovementMethod のインスタンスを取得します
        MovementMethod movementmethod = LinkMovementMethod.getInstance();

        // TextView に LinkMovementMethod を登録します
        textview.setMovementMethod(movementmethod);

        // <a>タグを含めたテキストを用意します
        String html = "gogo.gsのアカウントがない場合、<a href=\"http://gogo.gs/l/RegistrationInput/\">ユーザ登録（無料）</a> が必要です。";

        // URLSpan をテキストにを組み込みます
        CharSequence spanned = Html.fromHtml(html);

        textview.setText(spanned);
        return view;
    }

    /**
     * 画面初期化
     */
    private void init() {

        price_kind = (Spinner) findViewById(R.id.list_price_kind);
        check = (Spinner) findViewById(R.id.list_check);
        regular = (EditText) findViewById(R.id.txt_regular);
        highoc = (EditText) findViewById(R.id.txt_highoc);
        diesel = (EditText) findViewById(R.id.txt_diesel);
        lamp = (EditText) findViewById(R.id.txt_lamp);
        comment = (EditText) findViewById(R.id.edit_comment);
        lampLabel = (TextView) findViewById(R.id.label_lamp);
        comment.setLines(3);
        comment.setHint("価格の確認方法をご記入ください。");
        chkSaveSelectItem = (CheckBox) findViewById(R.id.chk_save_select_item);
        chkTweet = (CheckBox) findViewById(R.id.chk_tweet);
        chkTweet.setVisibility(View.GONE);

        lampSelector = (RadioGroup) findViewById(R.id.radioGroup1);
        lampSelector.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radio0) {
                    lampLabel.setText("円/18L");
                } else if (checkedId == R.id.radio1) {
                    lampLabel.setText("円/1L");
                }
            }
        });

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            ss_id = extras.getString("shopcode");

            // 確認方法、価格区分を記憶するのデフォルト値
            if (sp.getBoolean("save_select_item", false) == true) {
                chkSaveSelectItem.setChecked(true);

                db = dbHelper.getReadableDatabase();
                PostHistoriesDao postHistoriesDao = new PostHistoriesDao(db);
                PostItem item = postHistoriesDao.findLastOneByShopId(ss_id);
                db.close();

                if (item != null) {
                    price_kind.setSelection(Integer.parseInt(item.kubun));
                    check.setSelection(Integer.parseInt(item.kakunin));
                } else {
                    price_kind.setSelection(0);
                    check.setSelection(0);
                }
            } else {
                chkSaveSelectItem.setChecked(false);
            }

            db = dbHelper.getReadableDatabase();

            standsDao = new StandsDao(db);
            info = standsDao.findByShopCd(ss_id);
            db.close();
            if (info == null) {
                info = GoGoGsApi.getShopInfoAndPrices(ss_id);
            }

            if (info == null) {
                return;
            }

            // ブランド
            ImageView imgBrand = (ImageView) findViewById(R.id.brand_image);
            StandsHelper helper = StandsHelper.getInstance();
            imgBrand.setImageResource(helper.getBrandImage(info.Brand,
                    Integer.valueOf(info.Price)));

            // 店名
            TextView textShopName = (TextView) findViewById(R.id.shop_text);
            textShopName.setText(info.ShopName);
        }

        // 戻るボタン
        Button backButton = (Button) findViewById(R.id.btn_back);
        backButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // ヘルプボタン
        Button helpButton = (Button) findViewById(R.id.btn_help);
        helpButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                new AlertDialog.Builder(PostActivity.this)
                        .setTitle("ヘルプ")
                        .setNeutralButton("閉じる",
                                new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog,
                                            int which) {

                                    }
                                })
                        .setView(inflater.inflate(R.layout.post_help, null))
                        .create().show();
            }
        });

        // 投稿するボタン
        Button postButton = (Button) findViewById(R.id.btn_post);
        postButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                postExecute();

                // イベントトラック（価格投稿）
                tracker.trackEvent("Post", "Post", ss_id, 0);

            }
        });
    }

    /**
     * 「投稿するボタン」クリック時の処理
     * 
     */
    private void postExecute() {
        // プログレスダイアログを表示
        dialog = new ProgressDialog(this);
        dialog.setIndeterminate(true);
        dialog.setMessage(getResources().getText(
                R.string.dialog_message_posting_data));
        dialog.show();

        Thread thread = new Thread() {
            public void run() {
                try {
                    // 入力チェック
                    checkEntryData();

                    // ユーザ認証
                    auth(false);

                    // データPOST
                    post();

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();

                            if (!isFinishing()) {

                                // 結果の表示
                                Toast.makeText(getApplicationContext(),
                                        "価格投稿を受付ました。\nありがとうございました。",
                                        Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }
                    });

                } catch (final AuthException e) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (dialog != null) {
                                dialog.dismiss();
                            }

                            // エラーメッセージの表示
                            Toast.makeText(PostActivity.this, e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                            showAccountDialog();
                        }
                    });
                    e.printStackTrace();

                } catch (final PostException e) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (dialog != null) {
                                dialog.dismiss();
                            }

                            // エラーメッセージの表示
                            Toast.makeText(PostActivity.this, e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                    e.printStackTrace();
                }

            }
        };
        thread.start();
    }

    /**
     * 価格の範囲チェック
     */
    private ArrayList<String> checkPriceRange() {

        String url = "http://api.gogo.gs/ap/gsst/getPriceRange.php";
        ArrayList<String> err_msg = new ArrayList<String>();

        Utils.logging(url);

        XmlParserFromUrl xml = new XmlParserFromUrl();

        HashMap<String, String> res = null;

        for (int i = 0; i < 3; i++) {

            byte[] byteArray = Utils.getByteArrayFromURL(url, "GET");
            if (byteArray == null) {
                continue;
            }
            String data = new String(byteArray);

            res = xml.convertHashMapPrice(data);

            if (res == null || res.get("regular_min") == null) {
                continue;
            } else {
                break;
            }
        }

        if (res == null) {
            err_msg.add("サーバーに接続できませんでした。時間をおいて再度お試しください。\nエラーコード[0004]");
            return err_msg;
        }

        // レギュラー価格チェック
        if (regular.getText().length() > 0) {
            int regular_price = Integer.parseInt(regular.getText().toString());
            if (Integer.parseInt(res.get("regular_min")) > regular_price
                    || Integer.parseInt(res.get("regular_max")) < regular_price) {

                err_msg.add("レギュラー価格は" + res.get("regular_min") + "〜"
                        + res.get("regular_max") + "円の間で選択してください。");
            }
        }

        // ハイオク価格チェック
        if (highoc.getText().length() > 0) {
            int highoc_price = Integer.parseInt(highoc.getText().toString());
            if (Integer.parseInt(res.get("highoc_min")) > highoc_price
                    || Integer.parseInt(res.get("highoc_max")) < highoc_price) {

                err_msg.add("ハイオク価格は" + res.get("highoc_min") + "〜"
                        + res.get("highoc_max") + "円の間で選択してください。");
            }
        }

        // 軽油価格チェック
        if (diesel.getText().length() > 0) {
            int diesel_price = Integer.parseInt(diesel.getText().toString());
            if (Integer.parseInt(res.get("diesel_min")) > diesel_price
                    || Integer.parseInt(res.get("diesel_max")) < diesel_price) {

                err_msg.add("軽油価格は" + res.get("diesel_min") + "〜"
                        + res.get("diesel_max") + "円の間で選択してください。");
            }
        }

        // 灯油価格チェック
        if (lamp.getText().length() > 0) {
            int lamp_price = getLampPrice();
            if (Integer.parseInt(res.get("lamp_min")) > lamp_price
                    || Integer.parseInt(res.get("lamp_max")) < lamp_price) {

                err_msg.add("灯油価格は" + res.get("lamp_min") + "〜"
                        + res.get("lamp_max") + "円の間で選択してください。");
            }
        }

        return err_msg;
    }

    private int getLampPrice() {
        int price = 0;
        price = Integer.parseInt(lamp.getText().toString());

        if (lampSelector.getCheckedRadioButtonId() == R.id.radio1) {
            price *= 18;
        }

        return price;
    }

    /**
     * 入力データのチェック
     * 
     * @return
     * @throws Exception
     */
    private boolean checkEntryData() throws PostException {

        ArrayList<String> err_msg = new ArrayList<String>();

        // １つも価格が設定されていない
        if (regular.getText().toString().length() == 0
                && highoc.getText().toString().length() == 0
                && diesel.getText().toString().length() == 0
                && lamp.getText().toString().length() == 0) {

            err_msg.add("価格は１つ以上指定してください。");
        }

        // レギュラー＞ハイオクの場合
        if (regular.getText().length() > 0
                && highoc.getText().length() > 0
                && Integer.parseInt(regular.getText().toString()) > Integer
                        .parseInt(highoc.getText().toString())) {

            err_msg.add("レギュラー価格はハイオク価格より安い価格で設定してください。");
        }

        // 価格の範囲チェック
        err_msg.addAll(checkPriceRange());

        // コメントが入力されていない場合
        if (comment.getText().length() == 0) {
            err_msg.add("コメントを入力してください。");
            // コメントが200文字以上の場合
        } else if (comment.getText().length() > 200) {
            err_msg.add("コメントは200文字以内で入力してください。");
        }

        if (err_msg.size() > 0) {
            String msg = "";
            String[] array = (String[]) err_msg.toArray(new String[0]);

            // 配列の内容表示
            for (int i = 0; i < array.length; i++) {
                msg += array[i];
                if (i < array.length - 1) {
                    msg += "\n";
                }
            }
            throw new PostException(msg);
        }
        return true;
    }

    /**
     * ユーザ認証
     * 
     * @return
     * @throws Exception
     */
    private boolean auth(final boolean tweetFlg) throws AuthException,
            PostException {
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(PostActivity.this);

        String url = "http://gogo.gs/api/sp/uauth.php?apid=" + apid + "&"
                + "secretkey=" + secretkey + "&" + "key="
                + pref.getString("settings_user_id", "") + ","
                + pref.getString("settings_passwd_md5", "");

        Utils.logging(url);
        XmlParserFromUrl xml = new XmlParserFromUrl();

        for (int i = 0; i < 3; i++) {
            byte[] byteArray = Utils.getByteArrayFromURL(url, "GET");
            if (byteArray == null) {
                continue;
            }
            String data = new String(byteArray);

            final HashMap<String, String> res = xml.convertHashMap(data);

            if (res == null || res.containsKey("Result") == false) {
                continue;
            } else if (res.get("Result").equals("1")) {
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        if (res.containsKey("TwitterAuth")
                                && res.get("TwitterAuth").contains("1")) {
                            chkTweet.setVisibility(View.VISIBLE);
                            if (tweetFlg == true
                                    && sp.getBoolean("settings_twitter", false) == true) {
                                chkTweet.setChecked(true);
                            }
                        }
                    }
                });
                return true;
            } else {
                throw new AuthException("認証に失敗しました。\nユーザID、パスワードを確認してください。");
            }
        }

        throw new PostException("認証に失敗しました。時間をおいて再度お試しください。");
    }

    /**
     * 価格の投稿
     * 
     * @return
     * @throws Exception
     */
    private boolean post() throws PostException {
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(PostActivity.this);

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();

        if (info == null) {
            throw new PostException("ネットワークに接続できません。電波状況を確認してください。");
        }

        DefaultHttpClient objHttp = new DefaultHttpClient();
        HttpParams params = objHttp.getParams();
        HttpConnectionParams.setConnectionTimeout(params, HTTP_TIMEOUT); // 接続のタイムアウト
        HttpConnectionParams.setSoTimeout(params, HTTP_TIMEOUT); // データ取得のタイムアウト

        final Calendar calendar = Calendar.getInstance();

        PostItem postItem = new PostItem();
        postItem.ssid = ss_id;
        postItem.kakunin = String.valueOf(check.getSelectedItemId());
        postItem.kubun = String.valueOf(price_kind.getSelectedItemId());
        if (regular.getText().length() > 0) {
            postItem.nedan0 = Integer.valueOf(regular.getText().toString());
        }
        if (highoc.getText().length() > 0) {
            postItem.nedan1 = Integer.valueOf(highoc.getText().toString());
        }

        if (diesel.getText().length() > 0) {
            postItem.nedan2 = Integer.valueOf(diesel.getText().toString());
        }
        if (lamp.getText().length() > 0) {
            postItem.nedan3 = getLampPrice();
        }
        postItem.regdategap = 0;
        postItem.regdatetime = calendar.get(Calendar.HOUR_OF_DAY);
        postItem.memo = String.valueOf(comment.getText());

        String url = "http://gogo.gs/api/sp/post_new.php?apid=" + apid + "&"
                + "secretkey=" + secretkey + "&" + "uid="
                + pref.getString("settings_user_id", "") + "&" + "up="
                + pref.getString("settings_passwd_md5", "") + "&" + "ss_id="
                + ss_id + "&" + "kakunin=" + check.getSelectedItemId() + "&"
                + "kubun=" + price_kind.getSelectedItemId() + "&"
                + "regdategap=0&regdatetime="
                + calendar.get(Calendar.HOUR_OF_DAY) + "&" + "memo="
                + URLEncoder.encode(String.valueOf(comment.getText()));

        if (regular.getText().length() > 0) {
            url += "&nedan0=" + regular.getText().toString();
        }

        if (highoc.getText().length() > 0) {
            url += "&nedan1=" + highoc.getText().toString();
        }

        if (diesel.getText().length() > 0) {
            url += "&nedan2=" + diesel.getText().toString();
        }

        if (lamp.getText().length() > 0) {
            url += "&nedan3=" + getLampPrice();
        }

        // 投稿内容をツイートするにチェックが入っていた場合
        if (chkTweet.isChecked()) {
            url += "&twpost=y";
        }

        Utils.logging(url);

        XmlParserFromUrl xml = new XmlParserFromUrl();

        for (int i = 0; i < 3; i++) {

            byte[] byteArray = Utils.getByteArrayFromURL(url, "POST");
            if (byteArray == null) {
                continue;
            }
            String data = new String(byteArray);

            HashMap<String, String> res = xml.convertHashMap(data);

            if (res == null || res.containsKey("Status") == false) {
                continue;
            } else if (res.get("Status").equals("ok")) {

                // 確認方法、価格区分を記録する
                SharedPreferences sp = PreferenceManager
                        .getDefaultSharedPreferences(this);

                Editor editor = sp.edit();

                if (chkSaveSelectItem.isChecked()) {
                    editor.putBoolean("save_select_item", true);
                } else {
                    editor.putBoolean("save_select_item", false);
                }
                editor.commit();

                // 投稿履歴を記録
                dbHelper = new DatabaseHelper(this);
                db = dbHelper.getWritableDatabase();
                PostHistoriesDao postHistoriesDao = new PostHistoriesDao(db);
                postHistoriesDao.insert(postItem);
                db.close();

                return true;

            } else {
                throw new PostException("登録に失敗しました。\nエラーコード["
                        + res.get("Message") + "]");
            }

        }

        throw new PostException(
                "サーバーに接続できませんでした。時間をおいて再度お試しください。\nエラーコード[0001]");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop the tracker when it is no longer needed.
        tracker.stop();
    }

    public class AuthException extends Exception {
        public AuthException(String message) {
            super(message);
        }
    }

    public class PostException extends Exception {
        public PostException(String message) {
            super(message);
        }
    }

}
