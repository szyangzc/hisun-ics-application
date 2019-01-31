package com.hisun.app.handlers;

import com.hisun.app.utils.HiByteHelper;
import com.hisun.constants.HiConstants;
import com.hisun.exception.HiException;
import com.hisun.hilog4j.Logger;
import com.hisun.lang.HiByteBuffer;
import com.hisun.message.HiContext;
import com.hisun.message.HiMessage;
import com.hisun.message.HiMessageContext;
import com.hisun.pubinterface.IHandler;
import com.hisun.util.HiICSProperty;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 
 * @author yzc 
 * 以流的方式保存文件中。
 */
public class HiSaveStreamFile implements IHandler {
	private static final Logger log = (Logger) HiContext.getCurrentContext().getProperty(HiConstants.SERVERLOG);
	private String type = "bin";

	public void process(HiMessageContext ctx) throws HiException {
		FileOutputStream fo = null;
		HiMessage msg = ctx.getCurrentMsg();
		
		String fileName = HiICSProperty.getTrcDir() + File.separator + "dump.out";
		try {
			fo = new FileOutputStream(new File(fileName), true);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.warn("fileName", e.getMessage());
		}
		HiByteBuffer hiBuff = (HiByteBuffer) msg.getBody();

		
		try {
			fo.write(msg.getRequestId().getBytes());
			fo.write("|".getBytes());
			if ("bin".equals(type)) {
				fo.write(hiBuff.getBytes());
			} else if ("hex".equals(type)) {
				String hex = HiByteHelper.bytesToHexString(hiBuff.getBytes());
				fo.write(hex.getBytes());
			}
			fo.write("\n".getBytes());
			fo.flush();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fo != null)
				try {
					fo.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}

	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

}
