package org.chrysaor.android.gas_station.ui;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.chrysaor.android.gas_station.R;
import org.chrysaor.android.gas_station.util.DatabaseHelper;
import org.chrysaor.android.gas_station.util.ErrorReporter;
import org.chrysaor.android.gas_station.util.FavoritesDao;
import org.chrysaor.android.gas_station.util.GSInfo;
import org.chrysaor.android.gas_station.util.PostHistoriesDao;
import org.chrysaor.android.gas_station.util.PostItem;
import org.chrysaor.android.gas_station.util.StandsDao;
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
import android.database.sqlite.SQLiteConstraintException;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class PostActivity extends Activity {
    
    private static int MinRegularPrice = 100;
    private static int MaxRegularPrice = 160;
    private static int MinHighOcPrice = 110;
    private static int MaxHighOcPrice = 170;
    private static int MinDieselPrice = 80;
    private static int MaxDieselPrice = 140;
    private static int MinLampPrice = 1000;
    private static int MaxLampPrice = 1700;
    private Handler mHandler = new Handler();
    private ProgressDialog dialog;
    private DatabaseHelper dbHelper = null;
    private SQLiteDatabase db = null;
    private StandsDao standsDao = null;
    private GSInfo info = null;
    private Spinner price_kind;
    private Spinner check;
    private Spinner regular;
    private Spinner highoc;
    private Spinner diesel;
    private Spinner lamp;
    private EditText comment;
    private CheckBox chkSaveSelectItem;
    private static final String apid = "gsearcho0o0";
    private static final String secretkey = "a456fwer7862343j4we8f54w634";
    private static String ss_id;
    private SharedPreferences sp;
    GoogleAnalyticsTracker tracker;
    View loginView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
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
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(PostActivity.this);

        String user_id = pref.getString("settings_user_id", "");
        String passwd  = pref.getString("settings_passwd_md5", "");
        
        if (user_id.equals("") || passwd.equals("")) {
            showAccountDialog();
        }
    }
    
    /**
     * アカウント設定ダイアログの表示
     */
    private void showAccountDialog() {

        loginView = getLoginView();
        
        new AlertDialog.Builder(this)
        .setTitle("アカウント設定")
        .setNeutralButton("OK", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                // 認証
                try {
                    // 入力値をプリファレンスに登録する
                    EditText user_id = (EditText) loginView.findViewById(R.id.edit_user_id);
                    EditText passwd  = (EditText) loginView.findViewById(R.id.edit_passwd);
                    
                    // SharedPreferencesを取得
                    SharedPreferences sp;
                    sp = PreferenceManager.getDefaultSharedPreferences(PostActivity.this);
                    
                    Editor editor = sp.edit();
                    
                    editor.putString("settings_user_id",    user_id.getText().toString());
                    editor.putString("settings_passwd_md5", Utils.md5(passwd.getText().toString()));
                    editor.commit();

                    // 認証処理
                    auth();
                    
                    Toast.makeText(PostActivity.this, "認証に成功しました。", Toast.LENGTH_LONG).show();

                } catch (AuthException e) {
                    Toast.makeText(PostActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    showAccountDialog();
                } catch (Exception e) {
                    Toast.makeText(PostActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    showAccountDialog();
                }
                
            }
            
        })
        .setView(loginView)
        .create()
        .show();

    }
    
    private View getLoginView() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        View view = inflater.inflate(R.layout.login, null);
                
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(PostActivity.this);

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
        check      = (Spinner) findViewById(R.id.list_check);
        regular    = (Spinner) findViewById(R.id.list_regular_price);
        highoc     = (Spinner) findViewById(R.id.list_highoc_price);
        diesel     = (Spinner) findViewById(R.id.list_diesel_price);
        lamp       = (Spinner) findViewById(R.id.list_lamp_price);
        comment   = (EditText) findViewById(R.id.edit_comment);
        comment.setLines(3);
        comment.setHint("価格の確認方法をご記入ください。");
        chkSaveSelectItem = (CheckBox) findViewById(R.id.chk_save_select_item);

        Bundle extras=getIntent().getExtras();
        if (extras!=null) {
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
            
            if (extras.containsKey("from") && extras.getString("from").equals("FavoriteListActivity")) {
                FavoritesDao favoritesDao = new FavoritesDao(db);
                info = favoritesDao.findByShopCd(ss_id);
            } else {
                standsDao = new StandsDao(db);
                info = standsDao.findByShopCd(ss_id);
            }
            db.close();
            
            if (info == null) {
                return;
            }
            
            // ブランド
            ImageView imgBrand = (ImageView) findViewById(R.id.brand_image);
            StandsHelper helper = StandsHelper.getInstance();
            imgBrand.setImageResource(helper.getBrandImage(info.Brand, Integer.valueOf(info.Price)));

            // 店名
            TextView textShopName = (TextView) findViewById(R.id.shop_text);
            textShopName.setText(info.ShopName);
        }
        
        // レギュラー
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // アイテムを追加します
        adapter.add("");
        for (int i = MinRegularPrice; i<= MaxRegularPrice; i++) {
            adapter.add(String.valueOf(i));
        }
        
        regular.setAdapter(adapter);

        // ハイオク
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // アイテムを追加します
        adapter2.add("");
        for (int i = MinHighOcPrice; i<= MaxHighOcPrice; i++) {
            adapter2.add(String.valueOf(i));
        }
        
        highoc.setAdapter(adapter2);
        
        // 軽油
        ArrayAdapter<String> adapter3 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // アイテムを追加します
        adapter3.add("");
        for (int i = MinDieselPrice; i<= MaxDieselPrice; i++) {
            adapter3.add(String.valueOf(i));
        }
        
        diesel.setAdapter(adapter3);
        
        // 灯油
        ArrayAdapter<String> adapter4 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        adapter4.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // アイテムを追加します
        adapter4.add("");
        for (int i = MinLampPrice; i<= MaxLampPrice; i++) {
            if (i % 18 == 0) {
                adapter4.add(String.valueOf(i) + "(" + i/18 + "円/L)");
            } else {
                adapter4.add(String.valueOf(i));
            }
        }
        
        lamp.setAdapter(adapter4);
        
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
                .setNeutralButton("閉じる", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        
                    }
                })
                .setView(inflater.inflate(R.layout.post_help, null))
                .create()
                .show();
            }
        });
        
        // 投稿するボタン
        Button postButton = (Button) findViewById(R.id.btn_post);
        postButton.setOnClickListener(new OnClickListener() {
 
            @Override
            public void onClick(View v) {
                postExecute();
                
                // イベントトラック（価格投稿）
                tracker.trackEvent(
                    "Post",     // Category
                    "Post",     // Action
                    ss_id,      // Label
                    0);

            }
        });
    }
    
    /**
     * 「投稿するボタン」クリック時の処理
     * 
     */
    private void postExecute() {
        //プログレスダイアログを表示
        dialog = new ProgressDialog(this);
        dialog.setIndeterminate(true);
        dialog.setMessage(getResources().getText(R.string.dialog_message_posting_data));
        dialog.show();

        Thread thread = new Thread() {
            public void run() {
                try {
                    // 入力チェック
                    checkEntryData();
                    
                    //ユーザ認証
                    auth();
                    
                    // データPOST
                    post();
                    
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                            
                            // 結果の表示
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PostActivity.this);
                            alertDialogBuilder.setMessage("価格投稿を受付ました。\nありがとうございました。");
                            
                            // アラートダイアログの否定ボタンがクリックされた時に呼び出されるコールバックを登録します
                            alertDialogBuilder.setPositiveButton("閉じる", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }});
                            
                            // アラートダイアログのキャンセルが可能かどうかを設定します
                            alertDialogBuilder.setCancelable(true);

                            alertDialogBuilder.show();
                        }
                    });

                } catch (final AuthException e) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                            
                            // エラーメッセージの表示
                            Toast.makeText(PostActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            showAccountDialog();
                        }
                    });
                    e.printStackTrace();

                } catch (final PostException e) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                            
                            // エラーメッセージの表示
                            Toast.makeText(PostActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                    e.printStackTrace();

                } catch (final Exception e) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                            
                            // エラーメッセージの表示
                            Toast.makeText(PostActivity.this, "サーバーに接続できませんでした。時間をおいて再度お試しください。", Toast.LENGTH_LONG).show();
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
        
        try {
            XmlParserFromUrl xml = new XmlParserFromUrl();
        
            byte[] byteArray = Utils.getByteArrayFromURL(url, "GET");
            if (byteArray == null) {
                return err_msg;
            }
            String data = new String(byteArray);
            
            HashMap<String, String> res = xml.convertHashMapPrice(data);
            
            // レギュラー価格チェック
            if (regular.getSelectedItem() != "") {
                int regular_price = Integer.parseInt(regular.getSelectedItem().toString());
                if (   Integer.parseInt(res.get("regular_min")) > regular_price
                    || Integer.parseInt(res.get("regular_max")) < regular_price) {
                    
                    err_msg.add("レギュラー価格は" + res.get("regular_min") + "〜" + res.get("regular_max") + "円の間で選択してください。");
                }
            }
            
            // ハイオク価格チェック
            if (highoc.getSelectedItem() != "") {
                int highoc_price = Integer.parseInt(highoc.getSelectedItem().toString());
                if (   Integer.parseInt(res.get("highoc_min")) > highoc_price
                    || Integer.parseInt(res.get("highoc_max")) < highoc_price) {
                    
                    err_msg.add("ハイオク価格は" + res.get("highoc_min") + "〜" + res.get("highoc_max") + "円の間で選択してください。");
                }
            }

            // 軽油価格チェック
            if (diesel.getSelectedItem() != "") {
                int diesel_price = Integer.parseInt(diesel.getSelectedItem().toString());
                if (   Integer.parseInt(res.get("diesel_min")) > diesel_price
                    || Integer.parseInt(res.get("diesel_max")) < diesel_price) {
                    
                    err_msg.add("軽油価格は" + res.get("diesel_min") + "〜" + res.get("diesel_max") + "円の間で選択してください。");
                }
            }
            
            // 灯油価格チェック
            if (lamp.getSelectedItem() != "") {
                int lamp_price = Integer.parseInt(lamp.getSelectedItem().toString());
                if (   Integer.parseInt(res.get("lamp_min")) > lamp_price
                    || Integer.parseInt(res.get("lamp_max")) < lamp_price) {
                    
                    err_msg.add("灯油価格は" + res.get("lamp_min") + "〜" + res.get("lamp_max") + "円の間で選択してください。");
                }
            }

        } catch (Exception e) {
        }
        
        return err_msg;
    }

    /**
     * 入力データのチェック
     * 
     * @return
     * @throws Exception
     */
    private boolean checkEntryData() throws Exception {
        
        ArrayList<String> err_msg = new ArrayList<String>();
        
        // １つも価格が設定されていない
        if (   regular.getSelectedItem() == ""
            && highoc.getSelectedItem() == ""
            && diesel.getSelectedItem() == ""
            && lamp.getSelectedItem() == "") {
            
            err_msg.add("価格は１つ以上指定してください。");
        }
        
        // レギュラー＞ハイオクの場合
        if (   regular.getSelectedItem() != ""
            && highoc.getSelectedItem() != ""
            && Integer.parseInt(regular.getSelectedItem().toString()) > Integer.parseInt(highoc.getSelectedItem().toString())) {
            
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

            //配列の内容表示 
            for (int i = 0; i < array.length; i++) { 
                msg += array[i];
                if (i < array.length-1) {
                    msg += "\n"; 
                }
            } 
            throw new Exception(msg);
        }
        return true;
    }
    
    /**
     * ユーザ認証
     * 
     * @return
     * @throws Exception
     */
    private boolean auth() throws AuthException, Exception {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(PostActivity.this);

        String url = "http://gogo.gs/api/sp/uauth.php?apid=" + apid + "&" +
                "secretkey=" + secretkey + "&" +
                "key=" + pref.getString("settings_user_id", "") + "," + pref.getString("settings_passwd_md5", "");
        
        Utils.logging(url);
        XmlParserFromUrl xml = new XmlParserFromUrl();

        byte[] byteArray = Utils.getByteArrayFromURL(url, "GET");
        if (byteArray == null) {
            throw new Exception("サーバーに接続できませんでした。時間をおいて再度お試しください。");
        }
        String data = new String(byteArray);
        
        HashMap<String, String> res = xml.convertHashMap(data);
        
        if (res.get("Result").equals("1")) {
            return true;
        } else {
            throw new AuthException("認証に失敗しました。\nユーザID、パスワードを確認してください。");
        }
    }
    
    /**
     * 価格の投稿
     * 
     * @return
     * @throws Exception
     */
    private boolean post() throws Exception, PostException {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(PostActivity.this);

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE); 
        NetworkInfo info = cm.getActiveNetworkInfo();
        
        if (info == null) {
            throw new PostException("ネットワークに接続できません。電波状況を確認してください。");
        }
        
        DefaultHttpClient objHttp = new DefaultHttpClient();
        HttpParams params = objHttp.getParams();  
        HttpConnectionParams.setConnectionTimeout(params, 3000); //接続のタイムアウト  
        HttpConnectionParams.setSoTimeout(params, 3000); //データ取得のタイムアウト  
        
        final Calendar calendar = Calendar.getInstance();

        PostItem postItem = new PostItem();
        postItem.ssid    = ss_id;
        postItem.kakunin = String.valueOf(check.getSelectedItemId());
        postItem.kubun   = String.valueOf(price_kind.getSelectedItemId());
        if (regular.getSelectedItem() != "") {
            postItem.nedan0 = Integer.valueOf(regular.getSelectedItem().toString());
        }
        if (highoc.getSelectedItem() != "") {
            postItem.nedan1 = Integer.valueOf(highoc.getSelectedItem().toString());
        }
        if (diesel.getSelectedItem() != "") {
            postItem.nedan2 = Integer.valueOf(diesel.getSelectedItem().toString());
        }
        if (lamp.getSelectedItem() != "") {
            postItem.nedan3 = Integer.valueOf(lamp.getSelectedItem().toString());
        }
        postItem.regdategap  = 0;
        postItem.regdatetime = calendar.get(Calendar.HOUR_OF_DAY);
        postItem.memo        = String.valueOf(comment.getText());
        
        String url = "http://gogo.gs/api/sp/post_new.php?apid=" + apid + "&" +
                "secretkey=" + secretkey + "&" +
                "uid=" + pref.getString("settings_user_id", "") + "&" +
                "up=" + pref.getString("settings_passwd_md5", "") + "&" +
                "ss_id=" + ss_id + "&" +
                "kakunin=" + check.getSelectedItemId() + "&" +
                "kubun=" + price_kind.getSelectedItemId() + "&" +
                "regdategap=0&regdatetime=" + calendar.get(Calendar.HOUR_OF_DAY) + "&" +
                "memo=" + URLEncoder.encode(String.valueOf(comment.getText()));

        if (regular.getSelectedItem() != "") {
            url += "&nedan0=" + regular.getSelectedItem();
        }
        
        if (highoc.getSelectedItem() != "") {
            url += "&nedan1=" + highoc.getSelectedItem();
        }
        
        if (diesel.getSelectedItem() != "") {
            url += "&nedan2=" + diesel.getSelectedItem();
        }
        
        if (lamp.getSelectedItem() != "") {
            url += "&nedan3=" + lamp.getSelectedItem();
        }

        Utils.logging(url);
        
        XmlParserFromUrl xml = new XmlParserFromUrl();

        byte[] byteArray = Utils.getByteArrayFromURL(url, "POST");
        if (byteArray == null) {
            throw new PostException("サーバーに接続できませんでした。時間をおいて再度お試しください。");
        }
        String data = new String(byteArray);
        
        HashMap<String, String> res = xml.convertHashMap(data);
        
        if (res.get("Status").equals("ok")) {
        
            // 確認方法、価格区分を記録する
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            
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
            throw new Exception("登録に失敗しました。\nエラーコード[" + res.get("Message") + "]");
        }
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
