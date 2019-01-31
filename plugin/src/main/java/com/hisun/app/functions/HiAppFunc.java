package com.hisun.app.functions;

import com.hisun.app.help.HiETFUtilsExt;
import com.hisun.app.help.HiJsonUtils;
import com.hisun.app.tools.HiMd5Utils;
import com.hisun.atc.HiATCConstants;
import com.hisun.constants.HiMessageCode;
import com.hisun.exception.HiException;
import com.hisun.hilog4j.HiLog;
import com.hisun.hilog4j.Logger;
import com.hisun.lang.HiByteBuffer;
import com.hisun.message.*;
import com.hisun.regexp.HiRegExpHelp;
import com.hisun.util.HiICSProperty;
import com.hisun.util.file.HiFileInputStream;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.oro.text.regex.MalformedPatternException;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HiAppFunc {
    /**
     * 功能：把json串转换为ETF
     *
     * @param argsMap
     * @param ctx
     * @return
     * @throws HiException
     */
    public int Json2ETF(HiATLParam argsMap, HiMessageContext ctx)
            throws HiException {
        HiMessage mess = ctx.getCurrentMsg();
        Logger log = HiLog.getLogger(mess);
        if (mess == null || ctx == null) {
            throw new HiException(HiMessageCode.ERR_SYSTEM, "前置系统错");
        }

        HiETF etfRoot = (HiETF) mess.getBody();
        if (etfRoot == null || etfRoot.isNullNode()) {
            throw new HiException(HiMessageCode.ERR_ETF_GET, "ETF is NULL");
        }

        String jsonNodeName = argsMap.get("JsonNode");
        if (StringUtils.isEmpty(jsonNodeName)) {
            throw new HiException(HiMessageCode.ERR_PARAM, "JsonNode");
        }
        log.info("JsonNodeName", jsonNodeName);
        String jsonString = etfRoot.getGrandChildValue(jsonNodeName);
        String xmlString;
        try {
            xmlString = HiJsonUtils.getXmlFromJson(jsonString);
        } catch (DocumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new HiException(e);
        }

        HiXmlETF xmlETF = new HiXmlETF(xmlString);

        etfRoot.combine(xmlETF, true);

        return HiATCConstants.SUCC;
    }

    /**
     * @param argsMap
     * @param ctx
     * @return
     * @throws HiException
     */
    public int ValidateByXSD(HiATLParam argsMap, HiMessageContext ctx)
            throws HiException {
        HiMessage mess = ctx.getCurrentMsg();
        Logger log = HiLog.getLogger(mess);
        if (mess == null || ctx == null) {
            throw new HiException(HiMessageCode.ERR_SYSTEM, "前置系统错");
        }

        HiETF etfRoot = (HiETF) mess.getBody();
        if (etfRoot == null || etfRoot.isNullNode()) {
            throw new HiException(HiMessageCode.ERR_ETF_GET, "ETF is NULL");
        }

        String tranCode = argsMap.get("TranCode");
        if (StringUtils.isEmpty(tranCode)) {
            throw new HiException(HiMessageCode.ERR_PARAM, "TranCode");
        }

        String xmlText = argsMap.get("XmlText");
        if (StringUtils.isEmpty(xmlText)) {
            xmlText = mess.getHeadItem("totalMessage");
            if (StringUtils.isEmpty(xmlText)) {
                throw new HiException(HiMessageCode.ERR_PARAM, "XmlText");
            }
        }

        try {
            String schemaLanguage = XMLConstants.W3C_XML_SCHEMA_NS_URI;
            SchemaFactory schemaFactory = SchemaFactory
                    .newInstance(schemaLanguage);
            String xsdPathName = HiICSProperty.getWorkDir() + "/app/xsd/"
                    + tranCode + ".xsd";
            if (log.isDebugEnabled()) {
                log.debug("XSDPathName", xsdPathName);
            }
            File file = new File(xsdPathName);
            if (!file.exists()) {
                log.info(xsdPathName, " not exists!");
                return HiATCConstants.SUCC;
            }

            Schema schema = schemaFactory.newSchema(file);

            Validator validator = schema.newValidator();

            Document xmlDocument = DocumentHelper.parseText(xmlText);
            Element rootElement = xmlDocument.getRootElement();

            //rootElement.addNamespace("", "http://www.hisuntech.com");
            rootElement.addNamespace("xsi",
                    "http://www.w3.org/2001/XMLSchema-instance");
            rootElement.addAttribute("xsi:schemaLocation",
                    "http://www.hisuntech.com a.xsd");

            String xmlString = rootElement.asXML().replaceAll("xmlns=\"\"", "");

            Document doc = DocumentHelper.parseText(xmlString);

            InputSource inputSource = new InputSource(new StringReader(
                    doc.asXML()));
            Source source = new SAXSource(inputSource);
            validator.validate(source);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            etfRoot.setChildValue("MSG_TYP", "E");
            etfRoot.setChildValue("MSG_INF", e.getLocalizedMessage());
            return HiATCConstants.ERR;
        }

        return HiATCConstants.SUCC;
    }

    /**
     * 通过正则表达式
     *
     * @param argsMap
     * @param ctx
     * @return
     * @throws HiException
     */
    public int GetMatchLineCount(HiATLParam argsMap, HiMessageContext ctx)
            throws HiException {
        HiMessage mess = ctx.getCurrentMsg();
        Logger log = HiLog.getLogger(mess);
        if (mess == null || ctx == null) {
            throw new HiException(HiMessageCode.ERR_SYSTEM, "前置系统错");
        }

        HiETF etfRoot = (HiETF) mess.getBody();
        if (etfRoot == null || etfRoot.isNullNode()) {
            throw new HiException(HiMessageCode.ERR_ETF_GET, "ETF is NULL");
        }

        String fileName = argsMap.get("FileName");
        if (StringUtils.isEmpty(fileName)) {
            throw new HiException(HiMessageCode.ERR_PARAM, "FileName");
        }

        String regExp = argsMap.get("RegExp");
        if (StringUtils.isEmpty(fileName)) {
            throw new HiException(HiMessageCode.ERR_PARAM, "regExp");
        }

        BufferedReader in;
        Pattern pattern = Pattern.compile(regExp);
        int count = 0;
        try {
            in = new BufferedReader(new FileReader(fileName));
            String s;
            while ((s = in.readLine()) != null) {
                Matcher matcher = pattern.matcher(s);
                if (matcher.find()) {
                    count++;
                    log.debug(matcher.group());
                }
            }
            etfRoot.addNode("FIL_COUNT", String.valueOf(count));
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new HiException(e);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new HiException(e);
        }
        return HiATCConstants.SUCC;
    }

    public int TestExecSql(HiATLParam argsMap, HiMessageContext ctx)
            throws HiException {
        HiMessage mess = ctx.getCurrentMsg();
        Logger log = HiLog.getLogger(mess);
        if (mess == null || ctx == null) {
            throw new HiException(HiMessageCode.ERR_SYSTEM, "前置系统错");
        }

        HiETF etfRoot = (HiETF) mess.getBody();
        if (etfRoot == null || etfRoot.isNullNode()) {
            throw new HiException(HiMessageCode.ERR_ETF_GET, "ETF is NULL");
        }

        String sqlSentence = "insert into ics_test values('test')";

        for (int i = 0; i < 10000; i++) {
            if (ctx.getDataBaseUtil().execUpdate(sqlSentence) == 0) {
                // throw new HiAppException(2, HiMessageCode.ERR_DB_NO_RECORD,
                // sqlSentence);
                return HiATCConstants.NFND_REC;
            }
        }
        throw new HiException(HiMessageCode.ERR_SYSTEM, "前置系统错");

        // return HiATCConstants.SUCC;
    }

    /**
     * 对形如username bidusername|这样的字符串拆分后赋值为******，以便屏蔽敏感信息。
     *
     * @param argsMap
     * @param ctx
     * @return
     * @throws HiException
     */
    public int splitAndReplaceStarField(HiATLParam argsMap, HiMessageContext ctx)
            throws HiException {
        HiMessage mess = ctx.getCurrentMsg();
        Logger log = HiLog.getLogger(mess);
        if (mess == null || ctx == null) {
            throw new HiException(HiMessageCode.ERR_SYSTEM, "前置系统错");
        }

        HiETF etfRoot = (HiETF) mess.getBody();
        if (etfRoot == null || etfRoot.isNullNode()) {
            throw new HiException(HiMessageCode.ERR_ETF_GET, "ETF is NULL");
        }

        String starField = argsMap.get("StarField");
        if (StringUtils.isEmpty(starField)) {
            throw new HiException(HiMessageCode.ERR_PARAM, "StarField");
        }

        String fields[] = starField.split(" ");

        for (String field : fields) {
            String value = etfRoot.getChildValue(field);
            if (!StringUtils.isEmpty(value)) {
                try {
                    String fldReplace = HiRegExpHelp.desensitization(value);
                    etfRoot.setChildValue(field, fldReplace);
                } catch (MalformedPatternException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    throw new HiException(e);
                }
            }
        }

        return HiATCConstants.SUCC;
    }

    /**
     * 对转义后的XML节点恢复
     *
     * @param argsMap
     * @param ctx
     * @return
     * @throws HiException
     */
    public int unescapeXml(HiATLParam argsMap, HiMessageContext ctx)
            throws HiException {

        HiMessage mess = ctx.getCurrentMsg();
        Logger log = HiLog.getLogger(mess);
        if (mess == null || ctx == null) {
            throw new HiException(HiMessageCode.ERR_SYSTEM, "前置系统错");
        }

        HiETF etfRoot = (HiETF) mess.getBody();
        if (etfRoot == null || etfRoot.isNullNode()) {
            throw new HiException(HiMessageCode.ERR_ETF_GET, "ETF is NULL");
        }

        String source = argsMap.get("source");
        if (StringUtils.isEmpty(source)) {
            throw new HiException(HiMessageCode.ERR_PARAM, "source");
        }

        StringEscapeUtils.unescapeXml(source);

        return HiATCConstants.SUCC;
    }

    /**
     * 对XML节点转义
     *
     * @param argsMap
     * @param ctx
     * @return
     * @throws HiException
     */
    public int escapeXml(HiATLParam argsMap, HiMessageContext ctx)
            throws HiException {

        HiMessage mess = ctx.getCurrentMsg();
        Logger log = HiLog.getLogger(mess);
        if (mess == null || ctx == null) {
            throw new HiException(HiMessageCode.ERR_SYSTEM, "前置系统错");
        }

        HiETF etfRoot = (HiETF) mess.getBody();
        if (etfRoot == null || etfRoot.isNullNode()) {
            throw new HiException(HiMessageCode.ERR_ETF_GET, "ETF is NULL");
        }

        String source = argsMap.get("source");
        if (StringUtils.isEmpty(source)) {
            throw new HiException(HiMessageCode.ERR_PARAM, "source");
        }

        StringEscapeUtils.escapeXml(source);

        return HiATCConstants.SUCC;
    }

    /**
     * 根据正则表达式用ETF树上的的值替换字符串中相应子串
     *
     * @param argsMap
     * @param ctx
     * @return
     * @throws HiException
     */
    public int RegReplace(HiATLParam argsMap, HiMessageContext ctx)
            throws HiException {

        HiMessage mess = ctx.getCurrentMsg();
        Logger log = HiLog.getLogger(mess);
        if (mess == null || ctx == null) {
            throw new HiException(HiMessageCode.ERR_SYSTEM, "前置系统错");
        }

        HiETF etfRoot = (HiETF) mess.getBody();
        if (etfRoot == null || etfRoot.isNullNode()) {
            throw new HiException(HiMessageCode.ERR_ETF_GET, "ETF is NULL");
        }

        String source = argsMap.get("source");
        if (StringUtils.isEmpty(source)) {
            throw new HiException(HiMessageCode.ERR_PARAM, "source");
        }

        String findRegexp = argsMap.get("findRegexp");
        if (StringUtils.isEmpty(source)) {
            throw new HiException(HiMessageCode.ERR_PARAM, "findRegexp");
        }

        String replaceRegexp = argsMap.get("replaceRegexp");
        if (StringUtils.isEmpty(source)) {
            throw new HiException(HiMessageCode.ERR_PARAM, "replaceRegexp");
        }

        ArrayList subString = null;
        try {
            subString = (ArrayList) HiRegExpHelp.findNext(source, findRegexp, 1);
        } catch (MalformedPatternException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            throw new HiException(e1);
        }

        if (subString.size() == 0)
            throw new HiException(HiMessageCode.ERR_PARAM, "没有找到匹配的值");

        Object[] matchedStrings = subString.toArray();
        String result = null;
        for (Object matchedString : matchedStrings) {
            System.out.println(matchedString);
            if (log.isDebugEnabled())
                log.debug(matchedString);
            String setReplaceRegexp = replaceRegexp;

            setReplaceRegexp = setReplaceRegexp.replaceFirst("XXXXXX",
                    (String) matchedString);
            String value = etfRoot.getChildValue(((String) matchedString)
                    .toUpperCase());
            if (value != null) {
                String replace = "$1\\EXXXXXX$2";
                replace = replace.replaceFirst("XXXXXX", value);
                try {
                    result = HiRegExpHelp.replace(source, setReplaceRegexp,
                            replace, 1);
                } catch (MalformedPatternException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    throw new HiException(e);
                }

                source = result;
            }
        }
        etfRoot.setChildValue("result", result);
        System.out.println(result);
        return HiATCConstants.SUCC;
    }

    public int split(HiATLParam argsMap, HiMessageContext ctx)
            throws HiException {

        HiMessage mess = ctx.getCurrentMsg();
        Logger log = HiLog.getLogger(mess);
        if (mess == null || ctx == null) {
            throw new HiException(HiMessageCode.ERR_SYSTEM, "前置系统错");
        }
        HiETF etfRoot = (HiETF) mess.getBody();
        if (etfRoot == null || etfRoot.isNullNode()) {
            throw new HiException(HiMessageCode.ERR_ETF_GET, "ETF is NULL");
        }

        String inputStr = argsMap.get("Value");
        if (StringUtils.isEmpty(inputStr)) {
            throw new HiException(HiMessageCode.ERR_PARAM, "Value");
        }

        String sepChare = argsMap.get("SepChar");
        if (StringUtils.isEmpty(sepChare)) {
            throw new HiException(HiMessageCode.ERR_PARAM, "SepChar");
        }

        String grpName = argsMap.get("GrpName");
        if (StringUtils.isEmpty(grpName)) {
            grpName = "GRP";
        }
        grpName += "_";

        String[] strs = inputStr.split(sepChare);

        for (int i = 1, loop = strs.length; i <= loop; i++) {
            etfRoot.removeChildNode(grpName + i);
            etfRoot.addNode(grpName + i).setChildValue("VAL", strs[i - 1]);
        }

        etfRoot.setChildValue("GRP_NUM", strs.length + "");

        return HiATCConstants.SUCC;

    }

    /**
     * 生成文件的MD5
     *
     * @param argsMap
     * @param ctx
     * @return
     * @throws HiException
     */
    public int GenFileMD5(HiATLParam argsMap, HiMessageContext ctx)
            throws HiException {
        HiMessage mess = ctx.getCurrentMsg();
        Logger log = HiLog.getLogger(mess);
        if (mess == null || ctx == null) {
            throw new HiException(HiMessageCode.ERR_SYSTEM, "前置系统错");
        }
        HiETF etfRoot = (HiETF) mess.getBody();
        if (etfRoot == null || etfRoot.isNullNode()) {
            throw new HiException(HiMessageCode.ERR_ETF_GET, "ETF is NULL");
        }

        String fileName = argsMap.get("FIL_NM");
        if (StringUtils.isEmpty(fileName)) {
            throw new HiException(HiMessageCode.ERR_PARAM, "FIL_NM");
        }
        String etfName = argsMap.get("ETF_NM");
        if (StringUtils.isEmpty(fileName)) {
            throw new HiException(HiMessageCode.ERR_PARAM, "ETF_NM");
        }

        HiByteBuffer fBuffer = new HiByteBuffer(1024);
        try {
            fBuffer = HiFileInputStream.read(fileName, fBuffer);
        } catch (IOException e) {
            throw HiException.makeException(e);
        }
        String md5Val = HiMd5Utils.md5(fBuffer.getBytes());
        etfRoot.addNode(etfName).setValue(md5Val);
        return HiATCConstants.SUCC;
    }

    /**
     * 功能：按对传入的变量按字典秦顺序排序后拼接成name=value&name=value格式。
     *
     * @param argsMap
     * @param ctx
     * @return
     * @throws HiException
     */
    public int sortByDict(HiATLParam argsMap, HiMessageContext ctx)
            throws HiException {
        HiMessage mess = ctx.getCurrentMsg();
        Logger log = HiLog.getLogger(mess);
        if (mess == null || ctx == null) {
            throw new HiException(HiMessageCode.ERR_SYSTEM, "前置系统错");
        }
        HiETF etfRoot = (HiETF) mess.getBody();
        if (etfRoot == null || etfRoot.isNullNode()) {
            throw new HiException(HiMessageCode.ERR_ETF_GET, "ETF is NULL");
        }

        LinkedHashMap map = argsMap.toMap();

        List<Map.Entry<String, String>> infoIds = new ArrayList<Map.Entry<String, String>>(
                map.entrySet());

        // 对HashMap中的key 进行排序
        Collections.sort(infoIds, new Comparator<Map.Entry<String, String>>() {
            public int compare(Map.Entry<String, String> o1,
                               Map.Entry<String, String> o2) {
                // System.out.println(o1.getKey()+"   ===  "+o2.getKey());
                return (o1.getKey()).toString().compareTo(
                        o2.getKey().toString());
            }
        });
        // 对HashMap中的key 进行排序后 显示排序结果
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < infoIds.size(); i++) {
            Entry<String, String> id = infoIds.get(i);
            if (!StringUtils.isEmpty(id.getValue())) {
                sb.append(StringUtils.lowerCase(id.getKey()));
                sb.append("=");
                sb.append(id.getValue());
                if (i != infoIds.size() - 1)
                    sb.append("&");
            }
        }
        if (sb.lastIndexOf("&") == sb.length() - 1) {
            sb.deleteCharAt(sb.length() - 1);
        }
        etfRoot.addNode("sort_result").setValue(sb.toString());
        return HiATCConstants.SUCC;
    }

    /**
     * 生成一个MD5
     *
     * @param argsMap
     * @param ctx
     * @return
     * @throws HiException
     */
    public int GenMD5(HiATLParam argsMap, HiMessageContext ctx)
            throws HiException {
        HiMessage mess = ctx.getCurrentMsg();
        Logger log = HiLog.getLogger(mess);
        if (mess == null || ctx == null) {
            throw new HiException(HiMessageCode.ERR_SYSTEM, "前置系统错");
        }
        HiETF etfRoot = (HiETF) mess.getBody();
        if (etfRoot == null || etfRoot.isNullNode()) {
            throw new HiException(HiMessageCode.ERR_ETF_GET, "ETF is NULL");
        }

        String plainText = argsMap.get("plainText");
        if (StringUtils.isEmpty(plainText)) {
            throw new HiException(HiMessageCode.ERR_PARAM, "plainText");
        }
        String key = argsMap.get("key");
        if (!StringUtils.isEmpty(key)) {
            plainText += key;
        }

        log.info(plainText);
        String md5 = HiMd5Utils.md5(plainText);

        String md5Node = argsMap.get("MD5");
        if (StringUtils.isEmpty(md5Node)) {
            // throw new HiException(HiMessageCode.ERR_PARAM, "md5Node");
            md5Node = "MD5";
        }
        System.out.println(md5);
        etfRoot.addNode(md5Node).setValue(md5);

        return HiATCConstants.SUCC;
    }

    public int splitHexCN(HiATLParam argsMap, HiMessageContext ctx)
            throws HiException {
        HiMessage mess = ctx.getCurrentMsg();
        Logger log = HiLog.getLogger(mess);
        if (mess == null || ctx == null) {
            throw new HiException(HiMessageCode.ERR_SYSTEM, "前置系统错");
        }
        HiETF etfRoot = (HiETF) mess.getBody();
        if (etfRoot == null || etfRoot.isNullNode()) {
            throw new HiException(HiMessageCode.ERR_ETF_GET, "ETF is NULL");
        }

        String hex = argsMap.get("hex");
        if (StringUtils.isEmpty(hex)) {
            throw new HiException(HiMessageCode.ERR_PARAM, "hex");
        }
        String part = argsMap.get("part");
        if (StringUtils.isEmpty(part)) {
            throw new HiException(HiMessageCode.ERR_PARAM, "part");
        }

        return HiATCConstants.SUCC;
    }

    /**
     * 功能：把ETF樹的XML節點轉換成xml字符串。
     *
     * @param argsMap
     * @param ctx
     * @return
     * @throws HiException
     */
    public int XmlNode2StrNode(HiATLParam argsMap, HiMessageContext ctx)
            throws HiException {
        HiMessage mess = ctx.getCurrentMsg();
        Logger log = HiLog.getLogger(mess);
        if (mess == null || ctx == null) {
            log.error(HiMessageCode.ERR_SYSTEM);
            throw new HiException(HiMessageCode.ERR_SYSTEM, "前置系统错");
        }

        String etfNodeName = argsMap.get("ETFNodeName");
        if (StringUtils.isEmpty(etfNodeName)) {
            throw new HiException(HiMessageCode.ERR_PARAM, "ETFNodeName");
        }

        String xmlNode = argsMap.get("XMLNodeName");
        if (StringUtils.isEmpty(xmlNode)) {
            throw new HiException(HiMessageCode.ERR_PARAM, "XMLNodeName");
        }

        HiETF etfRoot = (HiETF) mess.getBody();
        if (etfRoot == null || etfRoot.isNullNode()) {
            throw new HiException(HiMessageCode.ERR_ETF_GET, "ETF is NULL");
        }

        if (!etfRoot.isExist(etfNodeName)) {
            etfRoot.addNode(etfNodeName);
        }
        HiETF etfNode = etfRoot.getChildNodeBase(etfNodeName);
        String strNodeValue = etfNode.toString();

        if (etfRoot.isExist(xmlNode))
            etfRoot.removeChildNode(xmlNode);
        etfRoot.addNode(xmlNode).setValue(strNodeValue);

        return HiATCConstants.SUCC;
    }

    /**
     * XML字符串转成ETF节点。
     *
     * @param argsMap
     * @param ctx
     * @return
     * @throws HiException
     */
    public int StrNode2XmlNode(HiATLParam argsMap, HiMessageContext ctx)
            throws HiException {
        HiMessage mess = ctx.getCurrentMsg();
        Logger log = HiLog.getLogger(mess);
        if (mess == null || ctx == null) {
            log.error(HiMessageCode.ERR_SYSTEM);
            throw new HiException(HiMessageCode.ERR_SYSTEM, "前置系统错");
        }

        String etfNodeName = argsMap.get("ETFNodeName");
        if (StringUtils.isEmpty(etfNodeName)) {
            throw new HiException(HiMessageCode.ERR_PARAM, "ETFNodeName");
        }

        String xmlNodeName = argsMap.get("XMLNodeName");
        if (StringUtils.isEmpty(xmlNodeName)) {
            throw new HiException(HiMessageCode.ERR_PARAM, "XMLNodeName");
        }

        HiETF etfRoot = (HiETF) mess.getBody();
        if (etfRoot == null || etfRoot.isNullNode()) {
            throw new HiException(HiMessageCode.ERR_ETF_GET, "ETF is NULL");
        }

        if (etfRoot.isExist(etfNodeName))
            etfRoot.removeChildNode(etfNodeName);

        String nodeValue = etfRoot.getChildValue(xmlNodeName);
        if (StringUtils.isEmpty(nodeValue))
            nodeValue = "<" + etfNodeName + "/>";
        HiETF etfNode = HiETFFactory.createETF(nodeValue);
        // etfRoot.appendNode(etfNode);
        HiETF newNode = etfRoot.addNode(etfNodeName);
        List lst = etfNode.getChildNodes();
        Iterator iter = lst.iterator();
        while (iter.hasNext()) {
            HiETF cldETF = (HiETF) iter.next();
            newNode.appendNode(cldETF);
        }

        return HiATCConstants.SUCC;
    }

    public int Xml2ETF(HiATLParam argsMap, HiMessageContext ctx)
            throws HiException {
        HiMessage mess = ctx.getCurrentMsg();
        Logger log = HiLog.getLogger(mess);
        if (mess == null || ctx == null) {
            log.error(HiMessageCode.ERR_SYSTEM);
            throw new HiException(HiMessageCode.ERR_SYSTEM, "前置系统错");
        }

        String xmlString = argsMap.get("NodeName");

        if (StringUtils.isEmpty(xmlString)) {
            throw new HiException(HiMessageCode.ERR_PARAM, "NodeName");
        }

        try {
            Document doc = DocumentHelper.parseText(StringEscapeUtils
                    .unescapeXml(xmlString));
            HiETF newETF = HiETFUtils.convertToXmlETF(doc);
            HiETF etfRoot = mess.getETFBody();
            etfRoot.combine(newETF, true);
        } catch (DocumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new HiException(e);
        }

        return HiATCConstants.SUCC;
    }

    public int XmlFile2ETF(HiATLParam argsMap, HiMessageContext ctx)
            throws HiException {
        HiMessage mess = ctx.getCurrentMsg();
        Logger log = HiLog.getLogger(mess);
        if (mess == null || ctx == null) {
            log.error(HiMessageCode.ERR_SYSTEM);
            throw new HiException(HiMessageCode.ERR_SYSTEM, "前置系统错");
        }

        String fileName = argsMap.get("FileName");

        if (StringUtils.isEmpty(fileName)) {
            throw new HiException(HiMessageCode.ERR_PARAM, "FileName");
        }

        try {
            SAXReader reader = new SAXReader();
            Document doc = reader.read(new FileInputStream(fileName));
            HiETF newETF = HiETFUtilsExt.convertToXmlETF(doc);
            HiETF etfRoot = mess.getETFBody();
            etfRoot.combine(newETF, true);
        } catch (DocumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new HiException(e);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new HiException(e);
        }
        return HiATCConstants.SUCC;
    }

    public int HaveValue(HiATLParam argsMap, HiMessageContext ctx)
            throws HiException {
        HiMessage mess = ctx.getCurrentMsg();
        Logger log = HiLog.getLogger(mess);
        if (mess == null || ctx == null) {
            log.error(HiMessageCode.ERR_SYSTEM);
            throw new HiException(HiMessageCode.ERR_SYSTEM, "前置系统错");
        }

        String nodeName = argsMap.get("valName");
        if (StringUtils.isEmpty(nodeName)) {
            throw new HiException(HiMessageCode.ERR_PARAM, "valName");
        }
        HiETF etfRoot = (HiETF) mess.getBody();
        if (etfRoot == null || etfRoot.isNullNode()) {
            throw new HiException(HiMessageCode.ERR_ETF_GET, "ETF is NULL");
        }
        String nodeValue = etfRoot.getGrandChildValue(nodeName);

        // 如果有值
        System.out.println(nodeValue);
        if (!StringUtils.isEmpty(nodeValue))
            return HiATCConstants.SUCC;
        else
            return HiATCConstants.NEXIST;
    }

    public static void main(String[] args) {
        System.out.println("0.08".split(",").length);
        System.out.println("VERSION XML".replace("XML", "xml"));
        System.out
                .println("\u539f\u5b50\u51fd\u6578:[{0}]\u5b9a\u7fa9\u932f\u8aa4\u6216\u539f\u5b50\u51fd\u6578\u6240\u5728\u7684\u985e\u672a\u88dd\u8f09");
    }
}
