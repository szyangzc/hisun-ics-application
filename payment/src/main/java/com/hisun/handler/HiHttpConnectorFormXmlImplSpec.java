package com.hisun.handler;

import com.hisun.exception.HiException;
import com.hisun.lang.HiByteBuffer;
import com.hisun.protocol.http.HiHttpConnectorFormXmlImpl;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.Iterator;

public class HiHttpConnectorFormXmlImplSpec extends HiHttpConnectorFormXmlImpl {
    private String prefixStr = "STP_";
    private HttpMethod method;

    public HiHttpConnectorFormXmlImplSpec() {
        super();
    }

    protected void setGetParams(GetMethod method, Object request)
            throws HiException {
        Element root;
        try {
            root = DocumentHelper
                    .parseText(((HiByteBuffer) request).toString())
                    .getRootElement();
        } catch (DocumentException e) {
            throw new HiException(e);
        }

        NameValuePair[] pairs = new NameValuePair[root.elements().size()];
        int i = 0;
        Iterator iter = root.elementIterator();
        while (iter.hasNext()) {
            Element node = (Element) iter.next();
            String nodeName = node.getName();
            if (nodeName.startsWith(prefixStr)) {
                nodeName = nodeName.replace(prefixStr, "");
            }

            pairs[i++] = new NameValuePair(nodeName, node.getText());
        }
        method.setQueryString(pairs);
    }

    protected void setPostParams(PostMethod method, Object request)
            throws HiException {
        Element root;
        try {
            root = DocumentHelper
                    .parseText(((HiByteBuffer) request).toString())
                    .getRootElement();
        } catch (DocumentException e) {
            throw new HiException(e);
        }
        Iterator iter = root.elementIterator();
        while (iter.hasNext()) {
            Element node = (Element) iter.next();
            String nodeName = node.getName();
            if (nodeName.startsWith(prefixStr)) {
                nodeName = nodeName.replace(prefixStr, "");
            }
            method.setParameter(nodeName, node.getText());
        }
    }

    protected Object getResponseObject(byte[] responseBody) {
        // Element root = DocumentHelper.createElement("ROOT");
        // String[] pairs = StringUtils.split(new String(responseBody), '&');
        // for (int i = 0; i < pairs.length; i++) {
        // String[] names = StringUtils.split(pairs[i], '=');
        //
        // if (names.length == 2) {
        // Element node = root.addElement(names[0]);
        // node.setText(names[1]);
        // }
        // }
        String xmlString = new String(responseBody);
        System.out.println(xmlString);
        Document doc = null;
        try {
            doc = DocumentHelper.parseText(xmlString);
        } catch (DocumentException e) {
        }
        if (doc == null)
            responseBody = "<MSG></MSG>".getBytes();
        return new HiByteBuffer(responseBody);
    }

    public String getPrefixStr() {
        return prefixStr;
    }

    public void setPrefixStr(String prefixStr) {
        this.prefixStr = prefixStr;
    }


}
