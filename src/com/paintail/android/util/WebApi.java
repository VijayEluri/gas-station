package com.paintail.android.util;

import java.io.IOException;
import java.io.InputStream;


import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class WebApi {

	private static InputStream getHttpInputStream(String url)
			throws ClientProtocolException, IOException {
		HttpClient client = new DefaultHttpClient();
		HttpGet getMethod = new HttpGet(url);
		HttpResponse response = client.execute(getMethod);

		if (response.getStatusLine().getStatusCode() < 400) {
			return response.getEntity().getContent();
		} else {
			return null;
		}
	}
	
	public static Bitmap getImageBitmapOnWeb(String url) {
		Bitmap bm = null;
		InputStream in = null;
		try {
			in = getHttpInputStream(url);
			bm = BitmapFactory.decodeStream(in);
			in.close();
			return bm;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public static InputStream accessURL(String url) {
		try {
			InputStream is = getHttpInputStream(url);
			return is;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
