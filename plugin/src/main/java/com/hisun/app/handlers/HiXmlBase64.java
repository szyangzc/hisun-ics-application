package com.hisun.app.handlers;

import com.hisun.app.expr.HiAppExpr;
import com.hisun.exception.HiException;
import com.hisun.hilog4j.Logger;
import com.hisun.lang.HiByteBuffer;
import com.hisun.message.HiMessage;
import com.hisun.message.HiMessageContext;
import com.hisun.pubinterface.IHandler;
import org.apache.commons.codec.binary.Base64;
import org.dom4j.*;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;

public class HiXmlBase64 implements IHandler {
    private static Logger log = null;
    //(Logger) HiContext.getCurrentContext()
    //	.getProperty(HiConstants.SERVERLOG);
    private String charset = "GBK";
    private String base64node = "";
    private String encryptFlg;
    private String privateKey = "";

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getEncryptFlg() {
        return encryptFlg;
    }

    public void setEncryptFlg(String encryptFlg) {
        this.encryptFlg = encryptFlg;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getBase64node() {
        return base64node;
    }

    public void setBase64node(String basenode) {
        this.base64node = basenode;
    }

    public void processBase64Encrypt(HiMessageContext ctx) throws HiException {
        // TODO Auto-generated method stub
        HiMessage msg = ctx.getCurrentMsg();
        // if (log.isDebugEnabled()) {
        // log.debug("msg: [" + msg + "]");
        // }
        HiByteBuffer buff = (HiByteBuffer) msg.getBody();
        Element root = null;
        try {
            Document doc = DocumentHelper.parseText(new String(buff.getBytes(),
                    charset));
            doc.setXMLEncoding(charset);
            root = doc.getRootElement();

        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new HiException(e);
        } catch (DocumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new HiException(e);
        }

        Node node = root.selectSingleNode(base64node);
        String data = node.asXML();

        root.remove(node);

        Element dataElement = root.addElement("data");
        byte[] base64Byte;
        try {
            base64Byte = Base64.encodeBase64(data.getBytes(charset));
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new HiException(e);
        }

        try {
            dataElement.addText(new String(base64Byte, charset));
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new HiException(e);
        }

        Iterator iterElement = root.elementIterator();
        StringBuffer sb = new StringBuffer();
        while (iterElement.hasNext()) {
            Element element = (Element) iterElement.next();
            sb.append(element.getText());
        }
        sb.append(privateKey);
        String mac = HiAppExpr.MAC(null, new String[]{sb.toString()});
        Element macElement = root.addElement("mac");
        macElement.addText(mac);

        try {
            System.out.println(root.asXML());
            if (log.isInfoEnabled()) {
                log.info(root.asXML());

            }
            buff = new HiByteBuffer(root.asXML().getBytes(charset));
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new HiException(e);
        }
        msg.setBody(buff);
    }

    public void processBase64Decrypt(HiMessageContext ctx) throws HiException {
        // TODO Auto-generated method stub
        HiMessage msg = ctx.getCurrentMsg();

        HiByteBuffer buff = (HiByteBuffer) msg.getBody();
        if (log.isDebugEnabled()) {
            log.debug("response: [" + buff.getBytes() + "]");
        }

        Document doc = null;
        try {
            doc = DocumentHelper
                    .parseText(new String(buff.getBytes(), charset));
        } catch (UnsupportedEncodingException e1) {
            // TODO Auto-generated catch block
            log.error("body: [" + buff + "]");
            throw new HiException(e1);
        } catch (DocumentException e1) {
            // TODO Auto-generated catch block
            log.error("body: [" + buff + "]");
            throw new HiException(e1);
        }
        doc.setXMLEncoding(charset);
        Element root = doc.getRootElement();

        String data = root.selectSingleNode(base64node).getText();
        if (log.isDebugEnabled()) {
            log.debug("data node before decode: [" + data + "]");
        }
        byte[] base64Byte = Base64.decodeBase64(data.getBytes());

        String decodeData = null;
        try {
            decodeData = new String(base64Byte, charset);
            if (log.isDebugEnabled()) {
                log.debug("data node after decode: [" + decodeData + "]");
            }
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new HiException(e);
        }

        Document newDoc = null;
        try {
            newDoc = DocumentHelper.parseText(decodeData);
        } catch (DocumentException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            log.error("convert from base64", decodeData);
            throw new HiException(e1);
        }
        Element newNode = newDoc.getRootElement();
        root.remove(root.selectSingleNode(base64node));
        root.add(newNode);

        try {
            log.debug(root.asXML());
            buff = new HiByteBuffer(root.asXML().getBytes(charset));
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new HiException(e);
        }
        msg.setBody(buff);
    }

    public void process(HiMessageContext msg) throws HiException {
        // TODO Auto-generated method stub

    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        String data = "<rq><user_no>123456</user_no></rq>";
        byte bData[] = Base64.encodeBase64(data.getBytes("gbk"));

        System.out.println(new String(bData, "gbk"));
    }
}
