package com.hisun.app.handlers;

import com.hisun.charset.impl.HiCharsetConvert;
import com.hisun.constants.HiConstants;
import com.hisun.exception.HiException;
import com.hisun.framework.event.IServerDestroyListener;
import com.hisun.framework.event.IServerInitListener;
import com.hisun.framework.event.IServerStartListener;
import com.hisun.framework.event.ServerEvent;
import com.hisun.hilog4j.Logger;
import com.hisun.lang.HiByteBuffer;
import com.hisun.message.HiContext;
import com.hisun.message.HiMessage;
import com.hisun.message.HiMessageContext;
import com.hisun.pubinterface.IHandler;
import com.hisun.sm.HiStringManager;
import org.apache.commons.lang.StringUtils;

import java.io.UnsupportedEncodingException;

/**
 * 字符集转换。给出源串，源字符集，目标字符集。
 *
 * @author yzc
 * @since 2012.8.23
 */
public class HiCharsetTransfer implements IHandler, IServerStartListener, IServerInitListener, IServerDestroyListener {
    private static Logger log = (Logger) HiContext.getCurrentContext().getProperty(HiConstants.SERVERLOG);
    private String srcCharset = null;

    private String dstCharset = null;

    private String midCharset = null;

    final HiStringManager sm = HiStringManager.getManager();

    public void process(HiMessageContext ctx) throws HiException {
        // TODO Auto-generated method stub
        HiMessage msg = ctx.getCurrentMsg();
        HiByteBuffer hiBuffer = (HiByteBuffer) msg.getBody();
        if (hiBuffer == null) {
            throw new HiException("message body is null.");
        }
        if (log.isDebugEnabled()) {
            try {
                log.debug("before convert", srcCharset, new String(hiBuffer.getBytes(), srcCharset));
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                throw new HiException(e);
            }
        }

        byte[] toByte;
        if (StringUtils.isEmpty(midCharset)) {
            toByte = HiCharsetConvert.convert(hiBuffer.getBytes());
        } else {
            HiCharsetConvert.initCharset(srcCharset, midCharset);
            byte[] midByte = HiCharsetConvert.convert(hiBuffer.getBytes());
            HiCharsetConvert.initCharset(midCharset, dstCharset);
            toByte = HiCharsetConvert.convert(midByte);
        }

        if (log.isDebugEnabled()) {
            try {
                log.debug("after convert", dstCharset, new String(toByte, dstCharset));
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                throw new HiException(e);
            }
        }

        hiBuffer.clear();
        hiBuffer.append(toByte);
        msg.setBody(hiBuffer);

    }

    public void setDstCharset(String dstCharset) {
        this.dstCharset = dstCharset;
    }

    public void setSrcCharset(String srcCharset) {
        this.srcCharset = srcCharset;
    }

    public String getDstCharset() {
        return dstCharset;
    }

    public String getSrcCharset() {
        return srcCharset;
    }

    /**
     * @return the midCharset
     */
    public String getMidCharset() {
        return midCharset;
    }

    /**
     * @param midCharset the midCharset to set
     */
    public void setMidCharset(String midCharset) {
        this.midCharset = midCharset;
    }

    public void serverDestroy(ServerEvent arg0) throws HiException {
        // TODO Auto-generated method stub

    }

    public void serverInit(ServerEvent arg0) throws HiException {
        // TODO Auto-generated method stub

    }

    public void serverStart(ServerEvent arg0) throws HiException {
        // TODO Auto-generated method stub
        HiCharsetConvert.initCharset(srcCharset, dstCharset);

    }
}
