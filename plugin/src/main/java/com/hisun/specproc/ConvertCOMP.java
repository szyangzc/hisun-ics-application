package com.hisun.specproc;

import com.hisun.lang.HiByteBuffer;
import com.hisun.message.HiMessageContext;
import org.apache.commons.lang.StringUtils;

public class ConvertCOMP {
	private static final String CharSet = "0123456789ABCDEF";

	public HiByteBuffer num2COMP(HiByteBuffer buf, HiMessageContext ctx) {
		int bufLen = buf.length();
		String bufStr = buf.toString();
		bufStr = bufStr.trim();
		if (bufStr.charAt(0) == '+') {
			bufStr = bufStr.substring(1);
		}
		String strTemp = "";
		String strHex = "";
		byte[] bTemp = null;
		int i;
		switch (bufLen) {
		case 1:
		case 2:
		case 3:
		case 4:
			i = Integer.parseInt(bufStr);
			strHex = Integer.toHexString(i);
			strTemp = StringUtils.leftPad(strHex, 4, "0");
			strTemp = strTemp.substring(strTemp.length() - 4);

			break;
		case 5:
		case 6:
		case 7:
		case 8:
		case 9:
			i = Integer.parseInt(bufStr);
			strHex = Integer.toHexString(i);
			strTemp = StringUtils.leftPad(strHex, 8, "0");
			strTemp = strTemp.substring(strTemp.length() - 8);

			break;
		case 10:
		case 11:
		case 12:
		case 13:
		case 14:
		case 15:
		case 16:
		case 17:
		case 18:
			long l = Long.parseLong(bufStr);
			strHex = Long.toHexString(l);
			strTemp = StringUtils.leftPad(strHex, 16, "0");
			strTemp = strTemp.substring(strTemp.length() - 16);
		}

		System.out.println("strTemp=" + strTemp);

		System.out.println("strTemp=" + strTemp);

		bTemp = hexStringToByte(strTemp);
		buf.clear();
		buf.append(bTemp, 0, bTemp.length);
		return buf;
	}

	public HiByteBuffer comp2Num(HiByteBuffer buf, HiMessageContext ctx) {
		System.out.println("in comp2Num");
		for (int i = 0; i < buf.length(); i++) {
			System.out.println(buf.charAt(i));
		}
		System.out.println("buf=" + buf.toString());

		byte[] bufB = buf.getBytes();
		String bufStr = bytesToHexString(bufB);
		bufStr = bufStr.trim();

		byte[] bTemp = null;
		long l = Long.parseLong(bufStr, 16);
		System.out.println("l=" + l);
		String strTemp = l + "";
		bTemp = strTemp.getBytes();
		buf.clear();
		buf.append(bTemp, 0, bTemp.length);
		return buf;
	}

	public static String str2HexStr(String str) {
		char[] chars = "0123456789ABCDEF".toCharArray();

		StringBuilder sb = new StringBuilder("");

		byte[] bs = str.getBytes();

		for (int i = 0; i < bs.length; i++) {
			int bit = (bs[i] & 0xF0) >> 4;

			sb.append(chars[bit]);

			bit = bs[i] & 0xF;

			sb.append(chars[bit]);
		}

		return sb.toString();
	}

	public static String hexStr2Str(String hexStr) {
		String str = "0123456789ABCDEF";

		char[] hexs = hexStr.toCharArray();

		byte[] bytes = new byte[hexStr.length() / 2];

		for (int i = 0; i < bytes.length; i++) {
			int n = str.indexOf(hexs[(2 * i)]) * 16;

			n += str.indexOf(hexs[(2 * i + 1)]);

			bytes[i] = (byte) (n & 0xFF);
		}

		return new String(bytes);
	}

	public static String byte2HexStr(byte[] b) {
		String hs = "";
		String stmp = "";
		for (int n = 0; n < b.length; n++) {
			stmp = Integer.toHexString(b[n] & 0xFF);
			if (stmp.length() == 1)
				hs = hs + "0" + stmp;
			else {
				hs = hs + stmp;
			}
		}
		return hs.toUpperCase();
	}

	private static byte uniteBytes(String src0, String src1) {
		byte b0 = Byte.decode("0x" + src0).byteValue();
		b0 = (byte) (b0 << 4);
		byte b1 = Byte.decode("0x" + src1).byteValue();
		byte ret = (byte) (b0 | b1);
		return ret;
	}

	public static byte[] hexStr2Bytes(String src) {
		int m = 0;
		int n = 0;
		int l = src.length() / 2;
		System.out.println(l);
		byte[] ret = new byte[l];
		for (int i = 0; i < l; i++) {
			m = i * 2 + 1;
			n = m + 1;
			ret[i] = uniteBytes(src.substring(i * 2, m), src.substring(m, n));
		}
		return ret;
	}

	public static String stringToUnicode(String strText) throws Exception {
		String strRet = "";

		for (int i = 0; i < strText.length(); i++) {
			char c = strText.charAt(i);
			int intAsc = c;
			String strHex = Integer.toHexString(intAsc);
			if (intAsc > 128) {
				strRet = strRet + "\\u" + strHex;
			} else {
				strRet = strRet + "\\u00" + strHex;
			}
		}
		return strRet;
	}

	public static String unicodeToString(String hex) {
		int t = hex.length() / 6;
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < t; i++) {
			String s = hex.substring(i * 6, (i + 1) * 6);

			String s1 = s.substring(2, 4) + "00";

			String s2 = s.substring(4);

			int n = Integer.valueOf(s1, 16).intValue() + Integer.valueOf(s2, 16).intValue();

			char[] chars = Character.toChars(n);
			str.append(new String(chars));
		}
		return str.toString();
	}

	public static byte[] hexStringToByte(String hex) {
		int len = hex.length() / 2;
		byte[] result = new byte[len];
		char[] achar = hex.toCharArray();
		for (int i = 0; i < len; i++) {
			int pos = i * 2;
			result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[(pos + 1)]));
		}
		return result;
	}

	private static byte toByte(char c) {
		byte b = (byte) "0123456789ABCDEF".indexOf(c);
		return b;
	}

	public static String bytesToHexString(byte[] bArray) {
		StringBuffer sb = new StringBuffer(bArray.length);

		for (int i = 0; i < bArray.length; i++) {
			String sTemp = Integer.toHexString(0xFF & bArray[i]);
			if (sTemp.length() < 2)
				sb.append(0);
			sb.append(sTemp.toUpperCase());
		}
		return sb.toString();
	}

	public static void main(String[] args) {
		byte[] bt = new byte[3];
		bt[0] = 45;
		bt[1] = 56;
		bt[2] = 57;
		HiByteBuffer buf = new HiByteBuffer(bt);
		ConvertCOMP tatc = new ConvertCOMP();

		HiByteBuffer buf1 = tatc.num2COMP(buf, null);
		for (int i = 0; i < buf1.length(); i++) {
			System.out.println(buf1.charAt(i));
		}

		byte[] bt1 = { -1, -9 };

		HiByteBuffer buf3 = new HiByteBuffer(bt1);
		ConvertCOMP tatc1 = new ConvertCOMP();
		HiByteBuffer buf2 = tatc1.comp2Num(buf3, null);
		System.out.println("buf2=" + buf2.toString());
		for (int i = 0; i < buf2.length(); i++)
			System.out.println(buf2.charAt(i));
	}
}
