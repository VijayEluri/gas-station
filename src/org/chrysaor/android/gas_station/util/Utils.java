package org.chrysaor.android.gas_station.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Utils {
	public static byte[] getByteArrayFromURL(String strUrl) {
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
			con.setRequestMethod("GET");
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
}
