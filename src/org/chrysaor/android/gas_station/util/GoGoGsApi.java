package org.chrysaor.android.gas_station.util;


public class GoGoGsApi {

    public static final String SHOP_PRICE_URL = "http://api.gogo.gs/ap/gsst/shopInfo.php?id=";
    public static final int MODE_REGULAR = 0;
    public static final int MODE_HIGHOC  = 1;
    public static final int MODE_DIESEL  = 2;
    public static final int MODE_LAMP    = 3;
    public static final int KUBUN_GENKIN_FREE      = 0;
    public static final int KUBUN_MEMBER           = 1;
    public static final int KUBUN_UNKOWN           = 2;
    public static final int KUBUN_GENKIN_FREE_SIGN = 3;
    public static final int KUBUN_MEMBER_SIGN      = 4;
    
    public static boolean isMember(int member) {
        
        boolean res = false;
        
        switch (member) {
        case KUBUN_MEMBER:
        case KUBUN_MEMBER_SIGN:
            res = true;
            break;
        case KUBUN_GENKIN_FREE:
        case KUBUN_UNKOWN:
        case KUBUN_GENKIN_FREE_SIGN:
        default:
            break;
        }

        return res;
    }
    
    public static Price[] getPrices(String ssId) {
        
        XmlParserFromUrl xml = new XmlParserFromUrl();
        
        byte[] byteArray = Utils.getByteArrayFromURL(SHOP_PRICE_URL + ssId, "GET");
        if (byteArray == null) {
            Utils.logging("URLの取得に失敗");
            return null;
        }
        String data = new String(byteArray);
        
        return xml.getShopPrices(data);
    }

    public static GSInfo getShopInfoAndPrices(String ssId) {
        
        XmlParserFromUrl xml = new XmlParserFromUrl();
        
        byte[] byteArray = Utils.getByteArrayFromURL(SHOP_PRICE_URL + ssId, "GET");
        if (byteArray == null) {
            Utils.logging("URLの取得に失敗");
            return null;
        }
        String data = new String(byteArray);
        
        GSInfo info = xml.getShopInfo(data);
        info.Prices = xml.getShopPrices(data);
        return info;
    }


}
