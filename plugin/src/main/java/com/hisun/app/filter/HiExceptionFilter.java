package com.hisun.app.filter;

import com.hisun.exception.HiException;
import com.hisun.framework.filter.HiProcessFilter;
import com.hisun.message.HiMessageContext;
import com.hisun.pubinterface.IHandler;

public abstract class HiExceptionFilter extends HiProcessFilter {

	@Override
	public void process(HiMessageContext arg0, IHandler arg1) throws HiException {
		// TODO Auto-generated method stub
		
	}

	public abstract void handler();
}
