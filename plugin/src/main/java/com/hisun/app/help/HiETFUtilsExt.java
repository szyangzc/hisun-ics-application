package com.hisun.app.help;

import java.util.ArrayList;
import java.util.Iterator;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.hisun.message.HiETF;
import com.hisun.message.HiXmlETF;

public class HiETFUtilsExt {
	public static HiETF convertToXmlETF(Document doc) {
		if (doc != null) {
			Element root = doc.getRootElement();
			Element etfRoot = DocumentHelper.createDocument().addElement(
					root.getName());

			xmlToETF(root, etfRoot);

			return new HiXmlETF(etfRoot);
		}

		return null;
	}

	private static void xmlToETF(Element root, Element etfRoot) {
		Iterator it = root.elementIterator();
		Element child = null, etfChild = null, attrChild;

		Iterator attrIt = null;
		Attribute attr = null;
		int i = 1;
		while (it.hasNext()) {
			child = (Element) it.next();
/**
			List lst = root.selectNodes(child.getName());
			if (lst.size() > 1)
				etfChild = etfRoot.addElement(child.getName() + "_" + i++);
			else
				etfChild = etfRoot.addElement(child.getName());
**/
			
			etfChild = etfRoot.addElement(child.getName() + "_" + i++);
			attrIt = child.attributeIterator();
			while (attrIt.hasNext()) {
				attr = (Attribute) attrIt.next();
				attrChild = etfChild.addElement(attr.getName());
				attrChild.setText(attr.getValue());
			}

			// setText
			etfChild.setText(child.getText());

			xmlToETF(child, etfChild);
		}
	}
}
