package com.hisun.app.handlers;

import com.hisun.sw.HiConvert;
import com.hisun.sw.big5.HiBIG5SwCode;

public class HiSwCodeEBCDIIC2BIG5 {
	/**
	 * EBCDII->BIG5转码
	 * 
	 * @param buffer
	 *            EBCDII数据
	 * @param len
	 *            EBCDII数据的长度
	 * @param temp
	 *            转码后的BIG5数据
	 * @return 返回转码后数据的长度
	 */
	public static int HostToClient(byte[] buffer, int len, byte[] temp) {
		int i, j, ilen, olen, ipos, templen;
		int iend;
		int icount;
		ilen = len;
		olen = 0;

		ipos = 0;
		for (i = 0, j = 0; i < ilen;) {
			/* Special meaning unsigned character, reserved */
			if (buffer[i] == 0x01 || buffer[i] == 0x02) {
				temp[j++] = buffer[i++];
				continue;
			}

			if (buffer[i] == 0x0e) { /* Chinese code flag */
				/* Search chinese string end flag : 0x02 */
				for (icount = 1, iend = 0, ipos = i + 1, templen = 1; ipos < ilen;) {
					if (buffer[ipos] == 0x0e) {
						icount++; /* Count 0e number */
						if (icount > 1) {
							break; /* Unmatch */
						}
					}
					if (buffer[ipos] == 0x0f) {
						icount--;
					}

					if (buffer[ipos] == 0x02) {
						iend = 1; /* 0x02 found */
						break;
					}
					ipos++; /* Skip to next byte */
					templen++; /* Increase chinese code length */
				}

				if (icount % 2 == 1) {
					iend = 0; // Incomplete chinese
				}
				if (iend == 1) {/* Found 0x02 */
					HiConvert.IbmBig5ToBig5(buffer, temp, ipos);
				} else {/* Not found 0x02 */
					HiConvert.IbmBig5ToBig5(buffer, temp, ipos);
					// HiBIG5SwCode.toGB2(buffer, i, temp, j, templen);
				}
				i += templen;
				j += templen;
				continue;
			} /* End of chinese code process */

			if (i + 1 < ilen && buffer[i + 1] == 0x01) {
				// 现在主机不需要做如下特殊处理
				// temp[j] = HiBIG5SwCode.signedConvert(buffer[i]);
				temp[j] = HiBIG5SwCode.bEbcdToAscii(buffer[i]);
				i++;
				j++;
			} else {/* Normal byte */
				temp[j++] = HiBIG5SwCode.bEbcdToAscii(buffer[i++]);
			}
		}
		return j;
	}
}
