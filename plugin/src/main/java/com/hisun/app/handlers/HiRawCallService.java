package com.hisun.app.handlers;

import com.hisun.constants.HiConstants;
import com.hisun.database.HiDataBaseUtil;
import com.hisun.dispatcher.process.HiRouterOut;
import com.hisun.exception.HiException;
import com.hisun.framework.event.IServerStartListener;
import com.hisun.framework.event.IServerStopListener;
import com.hisun.framework.event.ServerEvent;
import com.hisun.hilog4j.Logger;
import com.hisun.lang.HiByteBuffer;
import com.hisun.message.*;
import com.hisun.pubinterface.IHandler;
import com.hisun.register.HiRegisterService;
import com.hisun.register.HiServiceObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author yzc
 * @since 2013.08.16 at Olympic Building 25th floor.
 */
public class HiRawCallService implements IHandler, IServerStartListener,
		IServerStopListener {
	private static Logger log = (Logger) HiContext.getCurrentContext()
			.getProperty(HiConstants.SERVERLOG);

	private String _service = "ESBLOG01";
	private String _msgType = "PLTIN0";
	private String _charset = null;
	private String _xmlTxn = "/IXPMsg/IXPHead/IXPComHead/IXPTxnCod";
	private static HiDataBaseUtil dbUtil = new HiDataBaseUtil();

	private void writeByteArrayToDb(byte[] cblob) throws HiException {
		Connection conn = dbUtil.getConnection();
		PreparedStatement stmt = null;
		String strSql = "insert into esbttomsg(txn_rec) values(?)";
		try {
			stmt = conn.prepareStatement(strSql);
			stmt.setBytes(1, cblob);
			int nRow = stmt.executeUpdate();
			if (log.isDebugEnabled())
				log.debug("execUpdate is end. ret_code ", nRow);
			dbUtil.commit();
		} catch (SQLException e) {
			throw new HiException(e);
		} finally {
			dbUtil.close(stmt);
		}

	}

	public void process(HiMessageContext ctx) throws HiException {
		HiMessage msg = ctx.getCurrentMsg();
		// Logger log = HiLog.getLogger(msg);
		HiByteBuffer buff = (HiByteBuffer) msg.getBody();
		String xmlDoc = null;
		try {
			if (_charset == null)
				_charset = Charset.defaultCharset().name();
			xmlDoc = new String(buff.getBytes(), _charset);
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if (log.isDebugEnabled()) {
			log.debug("xmlDoc: [" + xmlDoc + "]");
		}
		// xmlDoc = xmlDoc.toUpperCase();
		// xmlDoc = xmlDoc.replace("XML", "xml").replace("VERSION", "version");

		xmlDoc = xmlNodeNameToUpercase(xmlDoc);
		xmlDoc = modifyXmlProlog(xmlDoc);

		HiETF etfBody = HiETFFactory.createETF(xmlDoc);
		msg.setBody(etfBody);
		// Logger log = HiLog.getLogger(mess);
		if (log.isDebugEnabled()) {
			log.debug("service: [" + _service + "]");
		}

		String serverName = null;
		try {
			HiServiceObject serviceObject = HiRegisterService
					.getService(_service);
			serverName = serviceObject.getServerName();
		} catch (HiException e) {
			throw new HiException(e);
		}
		if (log.isDebugEnabled()) {
			log.debug("serverName: [" + serverName + "]");
		}

		// HiMessage msg = new HiMessage(serverName, _msgType);
		// msg.setBody(new HiByteBuffer(buffer));
		if (!serverName.equalsIgnoreCase(_service)) {
			msg.setHeadItem(HiMessage.STC, _service);
			msg.setHeadItem(HiMessage.SDT, serverName);
		} else {
			msg.setHeadItem(HiMessage.SDT, _service);
		}
		msg.setHeadItem(HiMessage.TEXT_TYPE, HiMessage.TEXT_TYPE_ETF);
		msg.setHeadItem(HiMessage.REQUEST_RESPONSE, HiMessage.TYPE_REQUEST);
		msg.setHeadItem(HiMessage.STM, new Long(System.currentTimeMillis()));

		if (log.isDebugEnabled()) {
			log.debug("msg: [" + msg + "]");
		}

		msg = HiRouterOut.syncProcess(msg);

		ctx.setCurrentMsg(msg);
	}

	public void anotherProcess(HiMessageContext ctx) throws HiException {
		HiMessage msg = ctx.getCurrentMsg();
		// Logger log = HiLog.getLogger(msg);
		HiByteBuffer buff = (HiByteBuffer) msg.getBody();
		writeByteArrayToDb(buff.getBytes());
	}

	public String getXmlTxn() {
		return _xmlTxn;
	}

	public void setXmlTxn(String xmlTxn) {
		this._xmlTxn = xmlTxn;
	}

	public String getCharset() {
		return _charset;
	}

	public void setCharset(String charset) {
		this._charset = charset;
	}

	public String getService() {
		return _service;
	}

	public void setService(String _service) {
		this._service = _service;
	}

	public String getMsgType() {
		return _msgType;
	}

	public void setMsgType(String _msgType) {
		this._msgType = _msgType;
	}

	/**
	 * 把xml节点转成大写
	 * 
	 * @param xmlString
	 * @return
	 */
	private static String xmlNodeNameToUpercase(String xmlString) {
		Pattern pattern = Pattern.compile("<.+?>");
		StringBuilder res = new StringBuilder();

		int lastIdx = 0;
		Matcher matchr = pattern.matcher(xmlString);

		while (matchr.find()) {
			String str = matchr.group();
			res.append(xmlString.substring(lastIdx, matchr.start()));
			res.append(str.toUpperCase());
			lastIdx = matchr.end();
		}

		res.append(xmlString.substring(lastIdx));

		System.out.println(res.toString());

		return res.toString();

	}

	private String modifyXmlProlog(String xmlString) {
		Pattern pattern = Pattern.compile("<\\?.+?\\?>");
		StringBuilder res = new StringBuilder();

		int lastIdx = 0;
		Matcher matchr = pattern.matcher(xmlString);

		String s = "<?xml version='1.0' encoding=" + "'" + _charset + "'?>";
		while (matchr.find()) {
			String str = matchr.group();
			res.append(xmlString.substring(lastIdx, matchr.start()));

			res.append(s);
			lastIdx = matchr.end();
		}

		res.append(xmlString.substring(lastIdx));
		if (lastIdx == 0)
			res.insert(0, s);
		System.out.println(res.toString());

		return res.toString();

	}

	public static void main(String[] args) {
		String xmlString = "<Root><a>aaaa&gt;</a><b/></Root>";
		HiRawCallService service = new HiRawCallService();
		System.out.println(service.modifyXmlProlog(xmlString));
	}

	public void serverStop(ServerEvent arg0) throws HiException {
		// TODO Auto-generated method stub
		dbUtil.close();
	}

	public void serverStart(ServerEvent arg0) throws HiException {
		// TODO Auto-generated method stub

	}
}
