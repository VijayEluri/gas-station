package org.chrysaor.android.gas_station.util;

import org.chrysaor.android.gas_station.R;
import org.chrysaor.android.gas_station.lib.database.DatabaseHelper;
import org.chrysaor.android.gas_station.lib.database.FavoritesDao;
import org.chrysaor.android.gas_station.lib.database.StandsDao;
import org.chrysaor.android.gas_station.lib.dto.Stand;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class StandsHelper {

    private int[] images = new int[14];
    private DatabaseHelper dbHelper = null;
    private SQLiteDatabase db = null;
    private Stand info = null;
    private ProgressDialog dialog;

    // このクラスに唯一のインスタンス
    private static StandsHelper instance = new StandsHelper();

    private StandsHelper() {
        images[0] = R.drawable.pin_jomo;
        images[1] = R.drawable.pin_esso;
        images[2] = R.drawable.pin_eneos;
        images[3] = R.drawable.pin_kygnus;
        images[4] = R.drawable.pin_cosmo;
        images[5] = R.drawable.pin_shell;
        images[6] = R.drawable.pin_idemitsu;
        images[7] = R.drawable.pin_mitsui;
        images[8] = R.drawable.pin_mobile;
        images[9] = R.drawable.pin_sorato;
        images[10] = R.drawable.pin_ja;
        images[11] = R.drawable.pin_zeneral;
        images[12] = R.drawable.pin_itochu;
        images[13] = R.drawable.pin_original;

    }

    // インスタンス取得メソッド
    public static StandsHelper getInstance() {
        return instance;
    }

    public int getBrandImage(String brand_name, Integer price) {
        int res = 0;

        if (brand_name.compareTo("JOMO") == 0) {
            res = images[0];
        } else if (brand_name.compareTo("ESSO") == 0) {
            res = images[1];
        } else if (brand_name.compareTo("ENEOS") == 0) {
            res = images[2];
        } else if (brand_name.compareTo("KYGNUS") == 0) {
            res = images[3];
        } else if (brand_name.compareTo("COSMO") == 0) {
            res = images[4];
        } else if (brand_name.compareTo("SHELL") == 0) {
            res = images[5];
        } else if (brand_name.compareTo("IDEMITSU") == 0) {
            res = images[6];
        } else if (brand_name.compareTo("MITSUI") == 0) {
            res = images[7];
        } else if (brand_name.compareTo("MOBIL") == 0) {
            res = images[8];
        } else if (brand_name.compareTo("SOLATO") == 0) {
            res = images[9];
        } else if (brand_name.compareTo("JA-SS") == 0) {
            res = images[10];
        } else if (brand_name.compareTo("GENERAL") == 0) {
            res = images[11];
        } else if (brand_name.compareTo("ITOCHU") == 0) {
            res = images[12];
        } else {
            res = images[13];
        }

        return res;
    }

    public Stand getGsInfo(Context context, final String ssId) {
        info = null;

        try {
            dbHelper = new DatabaseHelper(context);
            db = dbHelper.getReadableDatabase();
            StandsDao standsDao = new StandsDao(db);
            info = standsDao.findByShopCd(ssId);

            if (info == null) {
                FavoritesDao favoritesDao = new FavoritesDao(db);
                info = favoritesDao.findByShopCd(ssId);
            }

            if (info == null) {
                info = GoGoGsApi.getShopInfoAndPrices(ssId);
            }

            return info;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db instanceof SQLiteDatabase) {
                db.close();
            }
        }

        return info;
    }

    public static String getBrandName(String brandId) {

        String brandName = null;
        switch (Integer.parseInt(brandId)) {
        case 1:
            brandName = "JOMO";
            break;
        case 2:
            brandName = "ESSO";
            break;
        case 3:
            brandName = "ENEOS";
            break;
        case 4:
            brandName = "KYGNUS";
            break;
        case 6:
            brandName = "COSMO";
            break;
        case 7:
            brandName = "SHELL";
            break;
        case 8:
            brandName = "IDEMITSU";
            break;
        case 9:
            brandName = "MITSUI";
            break;
        case 10:
            brandName = "MOBIL";
            break;
        case 11:
            brandName = "SOLATO";
            break;
        case 12:
            brandName = "JA-SS";
            break;
        case 13:
            brandName = "GENERAL";
            break;
        case 14:
            brandName = "ITOCHU";
            break;
        case 99:
            brandName = "ETC";
            break;
        }
        return brandName;

    }

}
