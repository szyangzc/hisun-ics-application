package com.hisun.handler;

import com.hisun.constants.HiConstants;
import com.hisun.exception.HiException;
import com.hisun.hilog4j.Logger;
import com.hisun.lang.HiByteBuffer;
import com.hisun.message.HiContext;
import com.hisun.message.HiMessage;
import com.hisun.message.HiMessageContext;
import com.hisun.pubinterface.IHandler;


public class SetTotalMessage implements IHandler{
	
	public Logger log = (Logger) HiContext.getCurrentContext().getProperty(
			HiConstants.SERVERLOG);
	
	public void process(HiMessageContext ctx) throws HiException {

		if (log.isDebugEnabled()){
			log.debug("SetTotalMessage process start");
		}
		HiMessage msg = ctx.getCurrentMsg();
		//byte[] data = ((HiByteBuffer) msg.getBody()).getBytes();
		HiByteBuffer hbb = (HiByteBuffer) msg.getBody();
		
			
		//String oriStr=bytesToHexString(data);
		String oriStr=hbb.toString();
				
		msg.setHeadItem("totalMessage", oriStr);
	
		//HiETF etfRoot = (HiETF) msg.getETFBody();
		
		//etfRoot.setChildValue("totalMessage", oriStr);
		
		if (log.isDebugEnabled()){
			log.debug("SetTotalMessage process end");
		}
	}
	
	public static String bytesToHexString(byte bArray[])
    {
        StringBuffer sb = new StringBuffer(bArray.length);
        for(int i = 0; i < bArray.length; i++)
        {
            String sTemp = Integer.toHexString(0xff & bArray[i]);
            if(sTemp.length() < 2)
                sb.append(0);
               
            sb.append(sTemp.toUpperCase());
        }

        return sb.toString();
    }
}
