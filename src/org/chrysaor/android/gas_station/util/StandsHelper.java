package org.chrysaor.android.gas_station.util;

import org.chrysaor.android.gas_station.R;

public class StandsHelper {

	private int[] images = new int[14];
	private int[] noprice_images = new int[14];

	// このクラスに唯一のインスタンス
	private static StandsHelper instance = new StandsHelper();
	
	private StandsHelper() {
		images[0] = R.drawable.jomo;
		images[1] = R.drawable.esso;
		images[2] = R.drawable.eneos;
		images[3] = R.drawable.kygnus;
		images[4] = R.drawable.icon_maker6;
		images[5] = R.drawable.shell;
		images[6] = R.drawable.icon_maker8;
		images[7] = R.drawable.icon_maker9;
		images[8] = R.drawable.icon_maker10;
		images[9] = R.drawable.icon_maker11;
		images[10] = R.drawable.icon_maker12;
		images[11] = R.drawable.icon_maker13;
		images[12] = R.drawable.icon_maker14;
		images[13] = R.drawable.icon_maker99;
		
		noprice_images[0] = R.drawable.jomo_r;
		noprice_images[1] = R.drawable.esso_r;
		noprice_images[2] = R.drawable.eneos_r;
		noprice_images[3] = R.drawable.kygnus_r;
		noprice_images[4] = R.drawable.icon_maker6_r;
		noprice_images[5] = R.drawable.shell_r;
		noprice_images[6] = R.drawable.icon_maker8_r;
		noprice_images[7] = R.drawable.icon_maker9_r;
		noprice_images[8] = R.drawable.icon_maker10_r;
		noprice_images[9] = R.drawable.icon_maker11_r;
		noprice_images[10] = R.drawable.icon_maker12_r;
		noprice_images[11] = R.drawable.icon_maker13_r;
		noprice_images[12] = R.drawable.icon_maker14_r;
		noprice_images[13] = R.drawable.icon_maker99_r;

	}
	
	// インスタンス取得メソッド
	public static StandsHelper getInstance() {
		return instance;
	}
	
	public int getBrandImage(String brand_name, Integer price) {
		int res = 0;
		
		switch (price) {
		case 0:
	       	if (brand_name.compareTo("JOMO") == 0) {
	    		res = noprice_images[0];
	        } else if (brand_name.compareTo("ESSO") == 0) {
	    		res = noprice_images[1];
	        } else if (brand_name.compareTo("ENEOS") == 0) {
	    		res = noprice_images[2];
	        } else if (brand_name.compareTo("KYGNUS") == 0) {
	    		res = noprice_images[3];
	        } else if (brand_name.compareTo("COSMO") == 0) {
	    		res = noprice_images[4];
	        } else if (brand_name.compareTo("SHELL") == 0) {
	    		res = noprice_images[5];
	        } else if (brand_name.compareTo("IDEMITSU") == 0) {
	    		res = noprice_images[6];
	        } else if (brand_name.compareTo("MITSUI") == 0) {
	    		res = noprice_images[7];
	        } else if (brand_name.compareTo("MOBIL") == 0) {
	    		res = noprice_images[8];
	        } else if (brand_name.compareTo("SOLATO") == 0) {
	    		res = noprice_images[9];
	        } else if (brand_name.compareTo("JA-SS") == 0) {
	    		res = noprice_images[10];
	        } else if (brand_name.compareTo("GENERAL") == 0) {
	    		res = noprice_images[11];
	        } else if (brand_name.compareTo("ITOCHU") == 0) {
	    		res = noprice_images[12];
	        } else {
	    		res = noprice_images[13];
	        }
			break;
		default:
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
			break;
		}
		
		return res;
	}
  
}
