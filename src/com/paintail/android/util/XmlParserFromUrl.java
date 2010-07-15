package com.paintail.android.util;

import java.io.*;
import java.util.ArrayList;

import org.xmlpull.v1.*;

import android.util.Log;

import com.paintail.android.util.GSInfo;

public class XmlParserFromUrl {
	private static final String LOG_TAG = "XmlParserFromUrl";
	private XmlPullParser xpp;
    private XmlPullParserFactory factory;
	private int eventType;
	
	//コンストラクタ
	public XmlParserFromUrl() {
		try {
			factory = XmlPullParserFactory.newInstance();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		}
        factory.setNamespaceAware(true);
	}
	
	//XMLからパラメータの都道府県の天気提供地域を取得
	public ArrayList<GSInfo> getGSInfoListFromXML(InputStream is) {
		
		ArrayList<GSInfo> list = new ArrayList<GSInfo>();
        String tag;
        boolean prefFlag = false;
        /*
		if (is == null)
			return null;
        try {
        	initXmlPullParser(is);
			while (eventType != XmlPullParser.END_DOCUMENT) {
				tag = xpp.getName();
				if (eventType == XmlPullParser.START_TAG) {
					//都道府県
					if(tag.compareTo(CityInfo.TAG_PREF) == 0) {
				        String title;
				        title = xpp.getAttributeValue(null, CityInfo.ATTR_TITLE);
				        //パラメータの都道府県を検索する
				        if(todohuken.compareTo(title) == 0) {
							prefFlag = true;
				        }
				    //都道府県内の天気提供地域
					} else if((tag.compareTo(CityInfo.TAG_CITY) == 0) && prefFlag) {
						CityInfo tempCity = new CityInfo();
						tempCity.city_id = xpp.getAttributeValue(null, CityInfo.ATTR_ID);
						tempCity.city_name = xpp.getAttributeValue(null, CityInfo.ATTR_TITLE);
						list.add(tempCity);
					}
				} else if (eventType == XmlPullParser.END_TAG) {
					//パラメータの都道府県の読み込み終了を判断
					if((tag.compareTo(CityInfo.TAG_PREF) == 0) && prefFlag) {
						break;
					}
				}
				eventType = xpp.next();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		*/
        return list;
        
	}

	//XMLから天候情報取得
	public ArrayList<GSInfo> getGSInfoFromXML(InputStream is) {
		GSInfo ret = new GSInfo();
        String tag;
        boolean imageFlag = false;
        boolean copyRightFlag = false;
        boolean tempMaxFlag = false;
        boolean tempMinFlag = false;
        String alert = null;
   	    String tmpName = null;
   	    int flag = 0;
        ArrayList<GSInfo> list = new ArrayList<GSInfo>();
        String value = new String();

		if (is == null) {
            Log.d(LOG_TAG, "null!!");

			return null;			
		}

        try {
        	initXmlPullParser(is);
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if(eventType == XmlPullParser.START_DOCUMENT) {
                    alert += "Start document\n";
                } else if(eventType == XmlPullParser.END_DOCUMENT) {
               	    alert += "End document\n";
                } else if(eventType == XmlPullParser.START_TAG) {
               	    if (xpp.getName().compareTo("Item") == 0) {
               		    flag = 1;
               		    ret = new GSInfo();
               	    }
                	tmpName = xpp.getName();
               	    alert += "Start tag "+xpp.getName() + "\n";
                } else if(eventType == XmlPullParser.END_TAG) {
               	    alert += "End tag "+xpp.getName() + "\n";
               	    if (xpp.getName().compareTo("Item") == 0) {
               		    flag = 0;
               		    list.add(ret);
               	    } else if (flag == 1) {
                   	    ret.setData(tmpName, value);
               	    }
                } else if(eventType == XmlPullParser.TEXT) {
               	    alert += "Text "+xpp.getText() + "\n";
               	    value = xpp.getText();
                }
                
                eventType = xpp.next();   
            }
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return list;
	}
	
	//タグからテキストを読み込むための補助メソッド
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
	
	//XmlPullParserの共通初期化メソッド
	private void initXmlPullParser(InputStream is) {
        try {
			xpp = factory.newPullParser();
	        xpp.setInput(is, "UTF-8");
	        
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		}

		eventType = 0;
	}
}
