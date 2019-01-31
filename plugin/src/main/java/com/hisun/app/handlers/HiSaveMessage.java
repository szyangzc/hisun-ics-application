package com.hisun.app.handlers;

import com.hisun.exception.HiException;
import com.hisun.lang.HiByteBuffer;
import com.hisun.message.HiMessage;
import com.hisun.message.HiMessageContext;
import com.hisun.pubinterface.IHandler;

public class HiSaveMessage implements IHandler {

	private String _fldName;

	public String getFldName() {
		return _fldName;
	}

	public void setFldName(String fldName) {
		this._fldName = fldName;
	}

	public void process(HiMessageContext ctx) throws HiException {
		// TODO Auto-generated method stub
		HiMessage msg = ctx.getCurrentMsg();
		HiByteBuffer hiBuff = (HiByteBuffer) msg.getBody();

		msg.addHeadItem(_fldName, hiBuff.toString());
	}
}
