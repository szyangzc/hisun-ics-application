package com.hisun.atc;

import com.hisun.exception.HiException;
import com.hisun.hilog4j.HiLog;
import com.hisun.hilog4j.Logger;
import com.hisun.message.HiATLParam;
import com.hisun.message.HiETF;
import com.hisun.message.HiMessage;
import com.hisun.message.HiMessageContext;
import org.apache.commons.lang.StringUtils;

public class GetHeadItem {

    public int execute(HiATLParam args, HiMessageContext ctx) throws HiException {

        HiMessage mess = ctx.getCurrentMsg();
        Logger log = HiLog.getLogger(mess);

        if (log.isDebugEnabled()) {
            log.debug("GetHeadItem process start");
        }
        String headParamName = args.get("headParamName");
        String saveParamName = args.get("saveParamName");

        HiETF etfRoot = (HiETF) mess.getBody();

        String headParamValue = mess.getHeadItem(headParamName);

        if (headParamName.equals("FID") && StringUtils.isEmpty(headParamValue)) {
            headParamValue = mess.getRequestId();
        }

        etfRoot.setChildValue(saveParamName, headParamValue);

        if (log.isDebugEnabled()) {
            log.debug("GetHeadItem process end");
        }

        return 1;

    }

}
