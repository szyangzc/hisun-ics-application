package com.hisun.app.tools;

import java.security.MessageDigest;
import java.util.List;

public class HiMd5Utils {

	//he-api support
	public final static String MD5Encrpytion(String source) {
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'a', 'b', 'c', 'd', 'e', 'f' };
		try {
			byte[] strTemp = source.getBytes();

			// System.out.println("[MD5Utils] [source String]" + source);
			MessageDigest mdTemp = MessageDigest.getInstance("MD5");
			mdTemp.update(strTemp);
			byte[] md = mdTemp.digest();
			int j = md.length;
			char str[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}
			for (int m = 0; m < str.length; m++) {
				if (str[m] >= 'a' && str[m] <= 'z')
					str[m] = (char) ((int) str[m] - 32);
			}

			System.out.println("[MD5Utils] [source String]" + source);
			System.out.println("[MD5Utils] [MD5    String]" + new String(str));
			return new String(str);
		} catch (Exception e) {
			return null;
		}

	}

	/**
	 * list轉換成string再MD5
	 * 
	 * @param list
	 * @return
	 */
	public static String MD5(List<String> list) {
		String listString = "";
		String md5 = null;
		if (list != null && list.size() > 0) {
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i) == null) {
					listString += "";
				} else {
					listString += list.get(i);
				}
			}
			md5 = md5(listString);
		}
		return md5;
	}

	/**
	 * string[]轉換成string再MD5
	 * 
	 * @param str
	 */
	public static String MD5(String[] strs) {
		String string = "";
		String md5 = null;
		if (strs != null) {
			for (int i = 0; i < strs.length; i++) {
				if (strs[i] == null) {
					string += "";
				} else {
					string += strs[i];
				}
			}
			md5 = md5(string);
		}
		return md5;
	}

	/**
	 * MD5轉換
	 * 
	 * @param plainText
	 * @return
	 */
	public static String md5(String plainText) {
		String md5 = null;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(plainText.getBytes());
			byte b[] = md.digest();
			int i;
			StringBuffer buf = new StringBuffer("");
			for (int offset = 0; offset < b.length; offset++) {
				i = b[offset];
				if (i < 0)
					i += 256;
				if (i < 16)
					buf.append("0");
				buf.append(Integer.toHexString(i));
				md5 = buf.toString();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return md5;
	}
	
	public static String md5(byte[] source) {
		String md5 = null;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(source);
			byte b[] = md.digest();
			int i;
			StringBuffer buf = new StringBuffer("");
			for (int offset = 0; offset < b.length; offset++) {
				i = b[offset];
				if (i < 0)
					i += 256;
				if (i < 16)
					buf.append("0");
				buf.append(Integer.toHexString(i));
				md5 = buf.toString();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return md5;
	}
}
