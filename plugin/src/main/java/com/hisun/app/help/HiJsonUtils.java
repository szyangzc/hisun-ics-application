package com.hisun.app.help;

import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.hisun.exception.HiException;
import com.hisun.message.HiXmlETF;

import net.sf.json.JSON;
import net.sf.json.JSONSerializer;
import net.sf.json.xml.XMLSerializer;

public class HiJsonUtils {
	private static XMLSerializer jsonSerializer = new XMLSerializer();

	/**
	 * 从XML转为JOSN格式
	 * 
	 * @param xml
	 * @return
	 */
	public static String getJsonFromXml(String xml) {
		JSON json = jsonSerializer.read(xml);
		return json.toString();
	}

	/**
	 * JSON串转成XML
	 * 
	 * @param json
	 * @return
	 * @throws DocumentException 
	 */
	public static String getXmlFromJson(String json) throws DocumentException {
		JSON jsonObject = JSONSerializer.toJSON(json);
		String xmlString =  jsonSerializer.write(jsonObject);
		Element e =  DocumentHelper.parseText(xmlString).getRootElement();
		e = elementToUpper(e);
		return e.asXML();
	}
	
	/**
	 * 把XML节点变大写
	 * @param ele
	 * @return
	 */
	public static Element elementToUpper(Element ele){
		ele.setName(ele.getName().toUpperCase());
		List<Attribute> attrList = ele.attributes();
		if(!attrList.isEmpty()){
			for(int i=0, m = attrList.size(); i < m; i++){
				Attribute attr = attrList.get(i);
				//System.out.println(attr.getName());
				//attr.setName(attr.getName().toUpperCase());
			}
		}
		List<Element> eleList = ele.elements();
		if(!eleList.isEmpty()){
			for(int i=0, m=eleList.size(); i<m; i++){
				Element element = eleList.get(i);
				//System.out.println(element.getName());
				//element.setName(element.getName().toUpperCase());
				elementToUpper(element);
			}
		}
		return ele;
	}

	public static void main(String[] args) throws HiException, DocumentException {
//		String xml = "<?xml version='1.0' encoding='utf-8'?><root><json name=\"ab\">test</json></root>";
//		String json = HiJsonUtils.getJsonFromXml(xml);
//		System.out.println(json);
		String json="{'ResponseHD_PINKEY':{'ResponseCode':'99'}}";
		String xml = HiJsonUtils.getXmlFromJson(json);
		Element e =  DocumentHelper.parseText(xml).getRootElement();
		 e = elementToUpper(e);
		HiXmlETF xmlETF = new HiXmlETF(e);
		
		System.out.println(xmlETF.toString());
	}
}
