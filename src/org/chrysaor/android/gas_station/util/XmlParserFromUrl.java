package org.chrysaor.android.gas_station.util;

import java.io.*;
import java.util.ArrayList;

import org.chrysaor.android.gas_station.util.GSInfo;
import org.xmlpull.v1.*;

import android.util.Log;


public class XmlParserFromUrl {
	private static final String LOG_TAG = "XmlParserFromUrl";
	private XmlPullParser xpp;
    private XmlPullParserFactory factory;
	private int eventType;
	
	public XmlParserFromUrl() {
		try {
			factory = XmlPullParserFactory.newInstance();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		}
        factory.setNamespaceAware(true);
	}
	
	public ArrayList<GSInfo> getGSInfoListFromXML(InputStream is) {
		
		ArrayList<GSInfo> list = new ArrayList<GSInfo>();
        String tag;
        boolean prefFlag = false;

        return list;
        
	}

	public ArrayList<GSInfo> getGSInfoFromXML(String is) {
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
//            	Log.i("hoge", String.valueOf(eventType));
            	switch (eventType) {
            	case XmlPullParser.START_DOCUMENT:
//                    alert += "Start document\n";
                    break;
            	case XmlPullParser.END_DOCUMENT:
//               	    alert += "End document\n";
               	    break;
            	case XmlPullParser.START_TAG:
               	    if (xpp.getName().compareTo("Item") == 0) {
               		    flag = 1;
               		    ret = new GSInfo();
               	    }
                	tmpName = xpp.getName();
//               	    alert += "Start tag "+xpp.getName() + "\n";
            		break;
            	case XmlPullParser.END_TAG:
//               	    alert += "End tag "+xpp.getName() + "\n";
               	    if (xpp.getName().compareTo("Item") == 0) {
               		    flag = 0;
               		    list.add(ret);
               	    } else if (flag == 1) {
                   	    ret.setData(tmpName, value);
               	    }
               	    break;
            	case XmlPullParser.TEXT:
//               	    alert += "Text "+xpp.getText() + "\n";
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
//	        xpp.setInput(is, "UTF-8");
	        xpp.setInput(new StringReader(is));
	        	        
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		}

		eventType = 0;
	}
}
