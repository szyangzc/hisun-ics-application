package com.hisun.handler;

import com.hisun.exception.HiException;
import com.hisun.lang.HiByteBuffer;
import com.hisun.message.HiMessage;
import com.hisun.message.HiMessageContext;
import com.hisun.pubinterface.IHandler;

/**
 * @author yzc
 */
public class HiByPassBody implements IHandler {

    private String bodyname; // body 名称
    private String charset;  // 字符编码

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getBodyname() {
        return bodyname;
    }

    public void setBodyname(String bodyname) {
        this.bodyname = bodyname;
    }

    public void process(HiMessageContext msg) throws HiException {
        // TODO Auto-generated method stub

        HiMessage mess = msg.getCurrentMsg();
        HiByteBuffer hbb = (HiByteBuffer) mess.getBody();
    }

}
