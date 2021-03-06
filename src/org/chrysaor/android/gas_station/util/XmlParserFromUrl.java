package org.chrysaor.android.gas_station.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.chrysaor.android.gas_station.lib.data.Price;
import org.chrysaor.android.gas_station.lib.dto.Stand;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.util.Log;

public class XmlParserFromUrl {
    private static final String LOG_TAG = "XmlParserFromUrl";
    private XmlPullParser xpp;
    private XmlPullParserFactory factory;
    private int eventType;
    private String[] shop_codes = new String[200];
    private int iterator = 0;

    public XmlParserFromUrl() {
        try {
            factory = XmlPullParserFactory.newInstance();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        factory.setNamespaceAware(true);
    }

    public ArrayList<Stand> getGSInfoListFromXML(InputStream is) {

        ArrayList<Stand> list = new ArrayList<Stand>();
        return list;

    }

    public ArrayList<Stand> getGSInfoFromXML(String is, String member) {
        Stand ret = new Stand();
        ret.setData("Member", member);
        String tmpName = null;
        int flag = 0;
        ArrayList<Stand> list = new ArrayList<Stand>();
        String value = new String();

        if (is == null) {
            return null;
        }

        try {
            initXmlPullParser(is);
            while (eventType != XmlPullParser.END_DOCUMENT) {
                // Log.i("hoge", String.valueOf(eventType));
                switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                    // alert += "Start document\n";
                    break;
                case XmlPullParser.END_DOCUMENT:
                    // alert += "End document\n";
                    break;
                case XmlPullParser.START_TAG:
                    if (xpp.getName().compareTo("Item") == 0) {
                        flag = 1;
                        ret = new Stand();
                        ret.setData("Member", member);
                    }
                    tmpName = xpp.getName();
                    // alert += "Start tag "+xpp.getName() + "\n";
                    break;
                case XmlPullParser.END_TAG:
                    // alert += "End tag "+xpp.getName() + "\n";
                    if (xpp.getName().compareTo("Item") == 0) {
                        flag = 0;
                        // Log.i("hoge", ret.ShopCode + ":" +
                        // Arrays.asList(shop_codes).get(0)
                        // +":"+Arrays.asList(shop_codes).contains(ret.ShopCode));
                        if (Arrays.asList(shop_codes).contains(ret.shopCode) == false) {
                            shop_codes[iterator] = ret.shopCode;
                            list.add(ret);
                            iterator++;
                        }
                    } else if (flag == 1) {
                        ret.setData(tmpName, value);
                    }
                    break;
                case XmlPullParser.TEXT:
                    // alert += "Text "+xpp.getText() + "\n";
                    value = xpp.getText();
                    break;
                }

                eventType = xpp.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    private String getText() throws XmlPullParserException, IOException {
        if (eventType != XmlPullParser.START_TAG) {
            eventType = xpp.next();
            return "UnKnown";
        }

        while (eventType != XmlPullParser.TEXT) {
            eventType = xpp.next();
        }

        return xpp.getText();
    }

    private void initXmlPullParser(String is) {
        try {

            xpp = factory.newPullParser();
            // xpp.setInput(is, "UTF-8");
            xpp.setInput(new StringReader(is));

        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }

        eventType = 0;
    }

    public boolean checkAuthResponce(String is) {

        if (is == null) {
            return false;
        }

        try {
            String key = null;
            String value = null;
            HashMap<String, String> map = new HashMap<String, String>();

            initXmlPullParser(is);
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                case XmlPullParser.END_DOCUMENT:
                    break;
                case XmlPullParser.END_TAG:
                    if (key != null && value != null) {
                        Utils.logging("key:" + key);
                        Utils.logging("value:" + value);
                        map.put(key, value);
                        key = null;
                        value = null;
                    }
                    break;
                case XmlPullParser.START_TAG:
                    key = xpp.getName();
                    break;
                case XmlPullParser.TEXT:
                    value = xpp.getText();
                    break;
                }

                eventType = xpp.next();
            }

            if (map.get("Result").equals("1")) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public HashMap<String, String> convertHashMap(String is) {

        String key = null;
        String value = null;
        HashMap<String, String> map = new HashMap<String, String>();

        try {
            initXmlPullParser(is);
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                case XmlPullParser.END_DOCUMENT:
                    break;
                case XmlPullParser.END_TAG:
                    if (key != null && value != null) {
                        Utils.logging("key:" + key);
                        Utils.logging("value:" + value);
                        map.put(key, value);
                        key = null;
                        value = null;
                    }
                    break;
                case XmlPullParser.START_TAG:
                    key = xpp.getName();
                    break;
                case XmlPullParser.TEXT:
                    value = xpp.getText();
                    break;
                }

                eventType = xpp.next();
            }

        } catch (XmlPullParserException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        } catch (IOException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        }

        return map;
    }

    public HashMap<String, String> convertHashMapPrice(String is) {

        String key = null;
        String value = null;
        String mode = null;
        HashMap<String, String> map = new HashMap<String, String>();

        try {
            initXmlPullParser(is);
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                case XmlPullParser.END_DOCUMENT:
                    break;
                case XmlPullParser.END_TAG:
                    if (key != null && key.equals("mode")) {
                        switch (Integer.parseInt(value)) {
                        case 0:
                            mode = "regular";
                            break;
                        case 1:
                            mode = "highoc";
                            break;
                        case 2:
                            mode = "diesel";
                            break;
                        case 3:
                            mode = "lamp";
                            break;
                        }

                    } else if (key != null
                            && (key.equals("min") || key.equals("max"))
                            && value != null) {

                        Utils.logging("key:" + mode + "_" + key);
                        Utils.logging("value:" + value);
                        map.put(mode + "_" + key, value);
                        key = null;
                        value = null;

                    }
                    break;
                case XmlPullParser.START_TAG:
                    key = xpp.getName();
                    break;
                case XmlPullParser.TEXT:
                    value = xpp.getText();
                    break;
                }

                eventType = xpp.next();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return map;
    }

    /**
     * 店舗情報取得
     * 
     * @param is
     * @return
     */
    public HashMap<String, HashMap<String, String>> getShopPrices4Fav(String is) {

        String key = null;
        String value = null;
        String price = null;
        String date = null;
        String shopCode = null;

        HashMap<String, HashMap<String, String>> list = new HashMap<String, HashMap<String, String>>();
        HashMap<String, String> map = new HashMap<String, String>();

        try {
            initXmlPullParser(is);
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                case XmlPullParser.END_DOCUMENT:
                    break;
                case XmlPullParser.END_TAG:
                    // Utils.logging("END_TAG:" + xpp.getName());
                    key = null;

                    if (xpp.getName().equals("Item")) {
                        map.put("price", price);
                        map.put("date", date);
                        list.put(shopCode, map);
                        Utils.logging(shopCode);
                    }
                    break;
                case XmlPullParser.START_TAG:
                    key = xpp.getName();
                    // Utils.logging("START_TAG:" + key);

                    if (key.equals("Item")) {
                        map = new HashMap<String, String>();
                        price = null;
                        date = null;
                        shopCode = null;
                    }
                    break;
                case XmlPullParser.TEXT:
                    if (key != null) {
                        Utils.logging(key);
                        Utils.logging(xpp.getText());
                        if (key.equals("Price")) {
                            price = xpp.getText();
                        } else if (key.equals("ShopCode")) {
                            shopCode = xpp.getText();
                        } else if (key.equals("Date")) {
                            date = xpp.getText();
                        }
                    }
                    break;
                }

                eventType = xpp.next();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;

    }

    /**
     * 価格情報取得
     * 
     * @param is
     * @return
     */
    public Price[] getShopPrices(String is) {
        String key = null;
        Price price = null;

        Price[] list = new Price[8];

        try {
            initXmlPullParser(is);
            int i = 0;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                case XmlPullParser.END_DOCUMENT:
                    break;
                case XmlPullParser.END_TAG:
                    // Utils.logging("END_TAG:" + xpp.getName());
                    key = null;

                    if (xpp.getName().equals("price")) {
                        list[i] = price;
                        i++;
                    }
                    break;
                case XmlPullParser.START_TAG:
                    key = xpp.getName();
                    // Utils.logging("START_TAG:" + key);

                    if (key.equals("price")) {
                        price = new Price();
                    }
                    break;
                case XmlPullParser.TEXT:
                    if (key != null) {
                        Utils.logging(key);
                        Utils.logging(xpp.getText());
                        if (key.equals("pnedan")) {
                            price.setPrice(Integer.parseInt(xpp.getText()));
                        } else if (key.equals("pmode")) {
                            price.setMode(Integer.parseInt(xpp.getText()));
                        } else if (key.equals("p_member")) {
                            price.setMember(Integer.parseInt(xpp.getText()));
                        } else if (key.equals("pregfdate")) {
                            price.setDate(xpp.getText());
                        } else if (key.equals("preguser")) {
                            price.setUser(xpp.getText());
                        } else if (key.equals("pmemo")) {
                            price.setMemo(xpp.getText());
                        }
                    }
                    break;
                }

                eventType = xpp.next();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;

    }

    /**
     * 価格情報取得
     * 
     * @param is
     * @return
     */
    public Stand getStand(String is) {
        String key = null;
        Stand item = new Stand();

        try {
            initXmlPullParser(is);
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                case XmlPullParser.END_DOCUMENT:
                    break;
                case XmlPullParser.END_TAG:
                    key = null;
                    break;
                case XmlPullParser.START_TAG:
                    key = xpp.getName();
                    break;
                case XmlPullParser.TEXT:
                    if (key != null) {
                        Utils.logging(key);
                        Utils.logging(xpp.getText());
                        if (key.equals("ShopCode")) {
                            item.shopCode = xpp.getText();
                        } else if (key.equals("Brand")) {
                            item.brand = StandsHelper.getBrandName(xpp
                                    .getText());
                        } else if (key.equals("ShopName")) {
                            item.shopName = xpp.getText();
                        } else if (key.equals("Latitude")) {
                            item.latitude = Double.parseDouble(xpp.getText());
                        } else if (key.equals("Longitude")) {
                            item.longitude = Double.parseDouble(xpp.getText());
                        } else if (key.equals("Address")) {
                            item.address = xpp.getText();
                        } else if (key.equals("Photo")) {
                            item.photo = xpp.getText();
                        } else if (key.equals("Rtc")) {
                            item.rtc = xpp.getText();
                        } else if (key.equals("Self")) {
                            item.self = xpp.getText();
                        }
                    }
                    break;
                }

                eventType = xpp.next();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return item;
    }

}
