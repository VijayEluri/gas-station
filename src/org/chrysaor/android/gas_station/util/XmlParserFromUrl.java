package org.chrysaor.android.gas_station.util;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.chrysaor.android.gas_station.util.GSInfo;
import org.xmlpull.v1.*;

import android.util.Log;
import android.widget.Spinner;


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
	
	public ArrayList<GSInfo> getGSInfoListFromXML(InputStream is) {
		
		ArrayList<GSInfo> list = new ArrayList<GSInfo>();
        return list;
        
	}

	public ArrayList<GSInfo> getGSInfoFromXML(String is, String member) {
		GSInfo ret = new GSInfo();
		ret.setData("Member", member);
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
               			ret.setData("Member", member);
               	    }
                	tmpName = xpp.getName();
//               	    alert += "Start tag "+xpp.getName() + "\n";
            		break;
            	case XmlPullParser.END_TAG:
//               	    alert += "End tag "+xpp.getName() + "\n";
               	    if (xpp.getName().compareTo("Item") == 0) {
               		    flag = 0;
//               		    Log.i("hoge", ret.ShopCode + ":" + Arrays.asList(shop_codes).get(0) +":"+Arrays.asList(shop_codes).contains(ret.ShopCode));
               		    if (Arrays.asList(shop_codes).contains(ret.ShopCode) == false) {
	               		    shop_codes[iterator] = ret.ShopCode;
	               		    list.add(ret);
	               		    iterator++;
	               		    
               		    }
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
	
	public boolean checkAuthResponce(String is) {
		
		if (is == null) {
            Log.d(LOG_TAG, "null!!");
			return false;			
		}

        try {
        	String key = null;
        	String value = null;
        	HashMap<String,String> map = new HashMap<String,String>();
        	
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
	
	public HashMap<String,String> convertHashMap(String is) {
		
    	String key = null;
    	String value = null;
    	HashMap<String,String> map = new HashMap<String,String>();

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
            
		} catch (Exception e) {
			e.printStackTrace();
		}

		return map;
	}

	public HashMap<String,String> convertHashMapPrice(String is) {
		
    	String key = null;
    	String value = null;
    	String mode = null;
    	HashMap<String,String> map = new HashMap<String,String>();

    	try {	
        	initXmlPullParser(is);
            while (eventType != XmlPullParser.END_DOCUMENT) {
            	switch (eventType) {
            	case XmlPullParser.START_DOCUMENT:
            	case XmlPullParser.END_DOCUMENT:
            		break;
            	case XmlPullParser.END_TAG:
                	if (key != null && key.equals("mode")) {
                		switch(Integer.parseInt(value)) {
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
                		
                	} else if (key != null && (key.equals("min") || key.equals("max")) && value != null) {
            			
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

}
