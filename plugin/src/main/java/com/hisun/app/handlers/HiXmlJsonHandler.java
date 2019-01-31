package com.hisun.app.handlers;

import com.hisun.app.help.HiJsonUtils;
import com.hisun.exception.HiException;
import com.hisun.lang.HiByteBuffer;
import com.hisun.message.HiMessage;
import com.hisun.message.HiMessageContext;
import com.hisun.pubinterface.IHandler;
import net.sf.json.xml.XMLSerializer;
import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentException;

/**
 * @author yzc
 * @since 2015.11.11 光棍节 功能：XML和JSON互转
 */
public class HiXmlJsonHandler implements IHandler {
    private String _jsonXmlInd = "JSON2XML";

    private static XMLSerializer jsonSerializer = new XMLSerializer();

    public String getJsonXmlInd() {
        return _jsonXmlInd;
    }

    public void setJsonXmlIdx(String jsonXmlIdx) {
        _jsonXmlInd = jsonXmlIdx;
    }

    @Override
    public void process(HiMessageContext msgCtx) throws HiException {
        // TODO Auto-generated method stub
        HiMessage msg = msgCtx.getCurrentMsg();
        HiByteBuffer hiBuf = (HiByteBuffer) msg.getBody();
        String message = new String(hiBuf.getBytes());
        String newMessage = "";
        if (StringUtils.equalsIgnoreCase(_jsonXmlInd, "Json2Xml")) {
            try {
                newMessage = HiJsonUtils.getXmlFromJson(message);
            } catch (DocumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                throw new HiException(e);
            }
        } else {
            newMessage = HiJsonUtils.getJsonFromXml(message);
        }
        hiBuf.clear();
        hiBuf.append(newMessage);
        msg.setBody(hiBuf);
    }

}
