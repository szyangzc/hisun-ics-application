package com.hisun.specproc;

import com.hisun.hilog4j.HiLog;
import com.hisun.hilog4j.Logger;
import com.hisun.lang.HiByteBuffer;
import com.hisun.message.HiMessageContext;
import org.apache.commons.lang.StringUtils;

public class ConvertCOMP3 {
	/**
	 * 数字转COM3有符号
	 * 
	 * @param buf
	 *            源数据字节码
	 * @param ctx
	 *            交易上下文
	 * @return
	 */
	public HiByteBuffer num2COMP3WithSign(HiByteBuffer buf, HiMessageContext ctx) {
		int bufLen = buf.length();
		String bufStr = buf.toString();
		Logger log = HiLog.getLogger(ctx.getCurrentMsg());
		if (log.isDebugEnabled())
			log.debug("num2COMP3WithSign", bufStr);

		bufStr = bufStr.trim();
		int digit = 12;
		if ((bufStr.charAt(0) == '+') || (bufStr.charAt(0) == '-')) {
			digit = buf.charAt(0) == 43 ? 12 : 13;
			bufStr = bufStr.substring(1);
			bufStr = StringUtils.leftPad(bufStr, bufLen - 1, "0");
		} else {
			bufStr = StringUtils.leftPad(bufStr, bufLen, "0");
		}
		byte[] bTemp = bufStr.getBytes();
		buf.clear();
		buf.append(bTemp, 0, bTemp.length);
		int startAt = 0;
		if ((buf.charAt(0) == 43) || (buf.charAt(0) == 45)) {
			digit = buf.charAt(0) == 43 ? 12 : 13;
			startAt = 1;
		}
		byte[] packed = new byte[(buf.length() - startAt) / 2 + 1];
		int inIdx = buf.length() - 1;
		int outIdx = packed.length - 1;
		int temp = (buf.charAt(inIdx--) & 0xF) << 4;
		digit |= temp;
		packed[(outIdx--)] = (byte) (digit & 0xFF);
		for (; (outIdx >= 0) && (inIdx >= 0); outIdx--) {
			digit = buf.charAt(inIdx--) & 0xF;
			if (inIdx >= 0) {
				temp = (buf.charAt(inIdx--) & 0xF) << 4;
				digit |= temp;
			}
			packed[outIdx] = (byte) digit;
		}
		bytesToHex(packed);
		buf.clear();
		buf.append(packed, 0, packed.length);
		System.out.println("num2COMP3WithSign buf=" + buf.toString());

		return buf;
	}

	/**
	 * 数字转comp3无符号
	 * @param buf
	 * @param ctx
	 * @return
	 */
	public HiByteBuffer num2COMP3WithoutSign(HiByteBuffer buf, HiMessageContext ctx) {
		int bufLen = buf.length();
		String bufStr = buf.toString();
		bufStr = bufStr.trim();
		System.out.println("num2COMP3WithoutSign bufStr=" + bufStr);
		bufStr = StringUtils.leftPad(bufStr, bufLen, "0");
		byte[] bTemp = bufStr.getBytes();
		buf.clear();
		buf.append(bTemp, 0, bTemp.length);

		int digit = 15;
		int startAt = 0;
		byte[] packed = new byte[(buf.length() - startAt) / 2 + 1];
		int inIdx = buf.length() - 1;
		int outIdx = packed.length - 1;
		int temp = (buf.charAt(inIdx--) & 0xF) << 4;
		digit |= temp;
		packed[(outIdx--)] = (byte) (digit & 0xFF);
		for (; (outIdx >= 0) && (inIdx >= 0); outIdx--) {
			digit = buf.charAt(inIdx--) & 0xF;
			if (inIdx >= 0) {
				temp = (buf.charAt(inIdx--) & 0xF) << 4;
				digit |= temp;
			}
			packed[outIdx] = (byte) digit;
		}
		bytesToHex(packed);
		buf.clear();
		buf.append(packed, 0, packed.length);
		System.out.println("num2COMP3WithoutSign buf=" + buf.toString());
		return buf;
	}

	/**
	 * com3转数字
	 * @param buf
	 * @param ctx
	 * @return
	 */
	public HiByteBuffer comp32Num(HiByteBuffer buf, HiMessageContext ctx) {
		int PlusSign = 12;
		int MinusSign = 13;
		int NoSign = 15;
		int DropHO = 255;
		int GetLO = 15;
		long val = 0L;
		byte[] pdIn = buf.getBytes();
		for (int i = 0; i < pdIn.length; i++) {
			int aByte = pdIn[i] & 0xFF;
			if (i == pdIn.length - 1) {
				int digit = aByte >> 4;
				val = val * 10L + digit;
				int sign = aByte & 0xF;
				if (sign == 13) {
					val = -val;
				} else if ((sign != 12) && (sign != 15))
					System.out.println("sign error");
			} else {
				int digit = aByte >> 4;
				val = val * 10L + digit;
				digit = aByte & 0xF;
				val = val * 10L + digit;
			}
		}
		System.out.println("num=" + val);
		String sval = String.valueOf(val);
		byte[] bval = sval.getBytes();
		buf.clear();
		buf.append(bval, 0, bval.length);
		return buf;
	}

	static String bytesToHex(byte[] buf) {
		String HexChars = "0123456789ABCDEF";
		StringBuffer sb = new StringBuffer(buf.length / 2 * 5 + 3);
		for (int i = 0; i < buf.length; i++) {
			byte b = buf[i];
			b = (byte) (b >> 4);
			b = (byte) (b & 0xF);
			sb.append("0123456789ABCDEF".charAt(b));
			b = buf[i];
			b = (byte) (b & 0xF);
			sb.append("0123456789ABCDEF".charAt(b));
			if (i % 2 == 1)
				sb.append(' ');
		}
		return sb.toString();
	}

	public static void main(String[] args) {
		byte[] bt = new byte[3];
		bt[0] = 0;
		bt[1] = 0;
		bt[2] = 0;
		bt[3] = 0;
		bt[4] = 0;
		bt[5] = 0;
		bt[6] = 12;
		HiByteBuffer buf = new HiByteBuffer(bt);
		ConvertCOMP3 tatc = new ConvertCOMP3();
		HiByteBuffer buf1 = tatc.comp32Num(buf, null);
		for (int i = 0; i < buf1.length(); i++)
			System.out.println(buf1.charAt(i));
	}
}
