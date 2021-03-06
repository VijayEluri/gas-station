package org.chrysaor.android.gas_station.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

public class Utils {
    public static final String DONATE_PACKAGE = "org.chrysaor.android.gas_station.plus";
    public static final String GASLOG_FREE_PACKAGE = "jp.pinetail.android.gas_log.free";
    public static final String GASLOG_PAYMENT_PACKAGE = "jp.pinetail.android.gas_log.payment";

    public static byte[] getByteArrayFromURL(String strUrl, String method) {
        byte[] line = new byte[1024];
        byte[] result = null;
        HttpURLConnection con = null;
        InputStream in = null;
        ByteArrayOutputStream out = null;
        int size = 0;
        try {
            // HTTP接続のオープン
            URL url = new URL(strUrl);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod(method);
            con.connect();
            in = con.getInputStream();

            // バイト配列の読み込み
            out = new ByteArrayOutputStream();
            while (true) {
                size = in.read(line);
                if (size <= 0) {
                    break;
                }
                out.write(line, 0, size);
            }
            result = out.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (con != null)
                    con.disconnect();
                if (in != null)
                    in.close();
                if (out != null)
                    out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }
    
    public static boolean isDonate(Context context) {
           // PackageManagerの取得
        PackageManager manager = context.getPackageManager();
            
        // 特定のパッケージがインストールされているか判定
        try {
            ApplicationInfo ai = manager.getApplicationInfo(DONATE_PACKAGE, 0);
            return true;
        } catch (NameNotFoundException e) {}
        
        return false;
    }

    public static boolean installedGasLogFree(Context context) {
           // PackageManagerの取得
        PackageManager manager = context.getPackageManager();
            
        // 特定のパッケージがインストールされているか判定
        try {
            ApplicationInfo ai = manager.getApplicationInfo(GASLOG_FREE_PACKAGE, 0);
            return true;
        } catch (NameNotFoundException e) {}
        
        return false;
    }
    
    public static boolean installedGasLogPayment(Context context) {
        // PackageManagerの取得
     PackageManager manager = context.getPackageManager();
         
     // 特定のパッケージがインストールされているか判定
     try {
         ApplicationInfo ai = manager.getApplicationInfo(GASLOG_PAYMENT_PACKAGE, 0);
         return true;
     } catch (NameNotFoundException e) {}
     
     return false;
 }

    public static void logging(String msg) {
        Log.i("GasSta!", msg);
    }
    
    public static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            BigInteger number = new BigInteger(1, digest);
            
            String md5 = number.toString(16);
            while (md5.length() < 32) {
                md5 = "0" + md5;
            }
            
            return md5;
        } catch (NoSuchAlgorithmException e) {
            logging(e.getMessage());
            return null;
        }
    }
    
    
    public static boolean isDeguggable(Context context) {
        PackageManager manager = context.getPackageManager();
        ApplicationInfo appInfo = null;
        
        try {
            appInfo = manager.getApplicationInfo(context.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            return false;
        }
        
        if ((appInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) == ApplicationInfo.FLAG_DEBUGGABLE) {
            return true;
        }
        
        return false;
    }
}
