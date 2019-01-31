package com.hisun.app.handlers;

import com.hisun.constants.HiConstants;
import com.hisun.exception.HiException;
import com.hisun.hilog4j.Logger;
import com.hisun.message.HiContext;
import com.hisun.message.HiMessage;
import com.hisun.message.HiMessageContext;
import com.hisun.pubinterface.IHandler;

public class HiSetResponse implements IHandler{
	private static Logger log = (Logger) HiContext.getCurrentContext()
			.getProperty(HiConstants.SERVERLOG);

	public void process(HiMessageContext ctx) throws HiException {
		// TODO Auto-generated method stub
		HiMessage msg = ctx.getCurrentMsg();
		if (log.isDebugEnabled()) {
			log.debug("msg: [" + msg + "]");
		}
		msg.setHeadItem(HiMessage.REQUEST_RESPONSE, HiMessage.TYPE_RESPONSE);
	}

}
