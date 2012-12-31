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

    private static final int[] pinImages = new int[] { R.drawable.pin_jomo,
            R.drawable.pin_esso, R.drawable.pin_eneos, R.drawable.pin_kygnus,
            R.drawable.pin_cosmo, R.drawable.pin_shell,
            R.drawable.pin_idemitsu, R.drawable.pin_mitsui,
            R.drawable.pin_mobile, R.drawable.pin_sorato, R.drawable.pin_ja,
            R.drawable.pin_zeneral, R.drawable.pin_itochu,
            R.drawable.pin_original };
    private static final int[] iconImages = new int[] { R.drawable.icon_maker1,
            R.drawable.icon_maker2, R.drawable.icon_maker3,
            R.drawable.icon_maker4, R.drawable.icon_maker6,
            R.drawable.icon_maker7, R.drawable.icon_maker8,
            R.drawable.icon_maker9, R.drawable.icon_maker10,
            R.drawable.icon_maker11, R.drawable.icon_maker12,
            R.drawable.icon_maker13, R.drawable.icon_maker14,
            R.drawable.icon_maker99 };
    private DatabaseHelper dbHelper = null;
    private SQLiteDatabase db = null;
    private Stand info = null;
    private ProgressDialog dialog;

    // このクラスに唯一のインスタンス
    private static StandsHelper instance = new StandsHelper();

    /**
     * コンストラクタ
     */
    private StandsHelper() {

    }

    // インスタンス取得メソッド
    public static StandsHelper getInstance() {
        return instance;
    }

    /**
     * ピン画像の取得
     * 
     * @param brand_name
     * @param price
     * @return
     */
    public int getPinImage(String brand_name, Integer price) {
        int res = 0;

        if (brand_name.compareTo("JOMO") == 0) {
            res = pinImages[0];
        } else if (brand_name.compareTo("ESSO") == 0) {
            res = pinImages[1];
        } else if (brand_name.compareTo("ENEOS") == 0) {
            res = pinImages[2];
        } else if (brand_name.compareTo("KYGNUS") == 0) {
            res = pinImages[3];
        } else if (brand_name.compareTo("COSMO") == 0) {
            res = pinImages[4];
        } else if (brand_name.compareTo("SHELL") == 0) {
            res = pinImages[5];
        } else if (brand_name.compareTo("IDEMITSU") == 0) {
            res = pinImages[6];
        } else if (brand_name.compareTo("MITSUI") == 0) {
            res = pinImages[7];
        } else if (brand_name.compareTo("MOBIL") == 0) {
            res = pinImages[8];
        } else if (brand_name.compareTo("SOLATO") == 0) {
            res = pinImages[9];
        } else if (brand_name.compareTo("JA-SS") == 0) {
            res = pinImages[10];
        } else if (brand_name.compareTo("GENERAL") == 0) {
            res = pinImages[11];
        } else if (brand_name.compareTo("ITOCHU") == 0) {
            res = pinImages[12];
        } else {
            res = pinImages[13];
        }

        return res;
    }

    /**
     * アイコン画像の取得
     * 
     * @param brand_name
     * @return
     */
    public int getIconImage(String brand_name) {
        int res = 0;

        if (brand_name.compareTo("JOMO") == 0) {
            res = iconImages[0];
        } else if (brand_name.compareTo("ESSO") == 0) {
            res = iconImages[1];
        } else if (brand_name.compareTo("ENEOS") == 0) {
            res = iconImages[2];
        } else if (brand_name.compareTo("KYGNUS") == 0) {
            res = iconImages[3];
        } else if (brand_name.compareTo("COSMO") == 0) {
            res = iconImages[4];
        } else if (brand_name.compareTo("SHELL") == 0) {
            res = iconImages[5];
        } else if (brand_name.compareTo("IDEMITSU") == 0) {
            res = iconImages[6];
        } else if (brand_name.compareTo("MITSUI") == 0) {
            res = iconImages[7];
        } else if (brand_name.compareTo("MOBIL") == 0) {
            res = iconImages[8];
        } else if (brand_name.compareTo("SOLATO") == 0) {
            res = iconImages[9];
        } else if (brand_name.compareTo("JA-SS") == 0) {
            res = iconImages[10];
        } else if (brand_name.compareTo("GENERAL") == 0) {
            res = iconImages[11];
        } else if (brand_name.compareTo("ITOCHU") == 0) {
            res = iconImages[12];
        } else {
            res = iconImages[13];
        }

        return res;
    }

    public Stand getStand(Context context, final String ssId) {
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
