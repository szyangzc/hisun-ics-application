package com.hisun.app.handlers;

import com.hisun.constants.HiConstants;
import com.hisun.exception.HiException;
import com.hisun.hilog4j.Logger;
import com.hisun.lang.HiByteBuffer;
import com.hisun.lang.HiByteUtil;
import com.hisun.message.HiContext;
import com.hisun.message.HiMessage;
import com.hisun.message.HiMessageContext;
import com.hisun.pubinterface.IHandler;
import org.dom4j.*;

import java.io.UnsupportedEncodingException;

/**
 * 对于XML报文中的中文字段全部转成16进制表示 。
 *
 * @author yzc
 */
public class HiXmlCN2HEX implements IHandler {

    private String charSet = "utf-8";

    private static Logger log = (Logger) HiContext.getCurrentContext()
            .getProperty(HiConstants.SERVERLOG);

    /**
     * 定义自己的访问者类
     */
    private class MyVisitor extends VisitorSupport {
        /**
         * 对于属性节点，打印属性的名字和值
         */
        public void visit(Attribute node) {

            try {
                // String value = new String(node.getText().getBytes(charSet),
                // charSet);
                String value = node.getText();
                // if (log.isDebugEnabled()) {
                // log.debug("attribute " + node.getName());
                // log.debug(value.getBytes(charSet));
                // }
                if (value.getBytes(charSet).length != value.length()) {
                    node.setValue(HiByteUtil.toHex(node.getText().getBytes(
                            charSet)));
                }
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        public void visit(Element element) {
            String text = element.getText();
            if (text.getBytes().length != text.length()) {
                try {
                    element.setText(HiByteUtil.toHex(element.getText()
                            .getBytes(charSet)));
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    public void process(HiMessageContext ctx) throws HiException {
        // TODO Auto-generated method stub
        HiMessage msg = ctx.getCurrentMsg();

        HiByteBuffer hbBuff = (HiByteBuffer) msg.getBody();

        String tmpString = null;
        try {
            tmpString = hbBuff.toString(charSet);
        } catch (UnsupportedEncodingException e) {
            throw HiException.makeException(e);
        }
        if (log.isDebugEnabled())
            log.debug("before trasform", tmpString);

        Document _doc = null;
        try {
            _doc = DocumentHelper.parseText(tmpString);
            _doc.setXMLEncoding(charSet);
            traversalDocumentByVisitor(_doc);

        } catch (DocumentException e) {
            throw new HiException(e);
        } catch (UnsupportedEncodingException e) {
            throw new HiException(e);
        }

        hbBuff.clear();

        hbBuff.append(_doc.asXML());

        msg.setBody(hbBuff);
    }

    public void setCharSet(String charSet) {
        this.charSet = charSet;
    }

    public String getCharSet() {
        return charSet;
    }

    /**
     * 通过访问者模式遍历XML。
     *
     * @throws UnsupportedEncodingException
     */
    public void traversalDocumentByVisitor(Document doc)
            throws UnsupportedEncodingException {
        doc.accept(new MyVisitor());

        if (log.isDebugEnabled())
            log.debug("after trasform", doc.asXML().getBytes(charSet));
    }

}
