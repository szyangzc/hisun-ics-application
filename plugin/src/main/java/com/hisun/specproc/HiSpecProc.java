package com.hisun.specproc;

import com.hisun.app.handlers.HiSwCodeEBCDIIC2BIG5;
import com.hisun.app.utils.HiByteHelper;
import com.hisun.constants.HiConstants;
import com.hisun.constants.HiMessageCode;
import com.hisun.exception.HiException;
import com.hisun.hilog4j.HiLog;
import com.hisun.hilog4j.Logger;
import com.hisun.lang.HiByteBuffer;
import com.hisun.lang.HiByteUtil;
import com.hisun.lang.HiConvHelper;
import com.hisun.message.HiETF;
import com.hisun.message.HiMessage;
import com.hisun.message.HiMessageContext;
import org.apache.commons.lang.StringUtils;

import java.io.UnsupportedEncodingException;

public class HiSpecProc {
    public final static String PREFIX_ZERO = "0";

    public final static String SUFFIX_F = "F";

    public final static byte BYTE_F = (byte) 0xff;

    public static void main(String[] args) {

        try {
            HiByteBuffer buf = new HiByteBuffer();
            buf.append("一二三".getBytes("utf-8"));
            System.out.println(HiByteHelper.bytesToHexString(buf.getBytes()));
            String unicode = new String(buf.getBytes(), "utf-8");
            HiByteBuffer hbb = new HiByteBuffer(unicode.getBytes("gbk"));
            System.out.println(HiByteHelper.bytesToHexString(hbb.getBytes()));
        } catch (UnsupportedEncodingException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

    }

    /**
     * 字符集转换
     *
     * @param buf
     * @param ctx
     * @return
     */
    public HiByteBuffer UTF8TOGBK(HiByteBuffer buf, HiMessageContext ctx) {
        HiMessage msg = ctx.getCurrentMsg();
        Logger log = HiLog.getLogger(msg);
        try {
            String unicode = new String(buf.getBytes(), "utf-8");
            HiByteBuffer hbb = new HiByteBuffer(unicode.getBytes("gbk"));
            if (log.isInfoEnabled())
                log.info("UTF8TOGBK", hbb);
            return hbb;
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return buf;
        }
    }

    /**
     * 字符集转换
     *
     * @param buf
     * @param ctx
     * @return
     */
    public HiByteBuffer GBK2UTF8(HiByteBuffer buf, HiMessageContext ctx) {
        try {
            String gbkString = new String(buf.getBytes(), "gbk");
            HiMessage msg = ctx.getCurrentMsg();
            Logger log = HiLog.getLogger(msg);
            log.info(HiByteUtil.byteArrayToHex(buf.getBytes()));
            if (log.isDebugEnabled()) {
                log.debug("before conver", gbkString);
                log.debug("after conver", new HiByteBuffer(gbkString.getBytes("utf-8")));
            }
            return new HiByteBuffer(gbkString.getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return buf;
        }
    }

    public HiByteBuffer EBCDII2BIG5(HiByteBuffer buf, HiMessageContext ctx) {
        byte[] temp = new byte[buf.length() * 2];
        int len = HiSwCodeEBCDIIC2BIG5.HostToClient(buf.getBytes(), buf.length(), temp);
        if (temp[(len - 1)] == 2) {
            len--;
        }
        buf.clear();
        buf.append(temp, 0, len);
        return buf;
    }

    public HiByteBuffer BCD2ASCII(HiByteBuffer buf, HiMessageContext ctx) throws Exception {
        HiMessage mess = ctx.getCurrentMsg();

        if (mess == null || ctx == null) {
            throw new HiException(HiMessageCode.ERR_SYSTEM, "前置系统错");
        }

        return new HiByteBuffer(HiConvHelper.bcd2AscStr(buf.getBytes()).getBytes());
    }

    public HiByteBuffer ASCII2BCD(HiByteBuffer buf, HiMessageContext ctx) throws Exception {
        HiMessage mess = ctx.getCurrentMsg();

        if (mess == null || ctx == null) {
            throw new HiException(HiMessageCode.ERR_SYSTEM, "前置系统错");
        }

        return new HiByteBuffer(HiConvHelper.ascStr2Bcd(new String(buf.getBytes())));
    }

    /**
     * 对于经过PackIem节点的包进行原样输出，以免转成String时有些字符不能转。如80-9F之间。并且增加前置长度
     *
     * @param buf 源串
     * @return 加了前置长度的字符串
     * @throws Exception
     */
    public HiByteBuffer packItemAndAddPreLen(HiByteBuffer buf, HiMessageContext ctx) throws Exception {

        HiMessage mess = ctx.getCurrentMsg();

        if (mess == null || ctx == null) {
            throw new HiException(HiMessageCode.ERR_SYSTEM, "前置系统错");
        }
        HiETF etfRoot = (HiETF) mess.getBody();
        if (etfRoot == null || etfRoot.isNullNode()) {
            throw new HiException(HiMessageCode.ERR_ETF_GET, "ETF is NULL");
        }

        Logger log = HiLog.getLogger(mess);

        byte[] blen = new byte[2];

        log.debug("ETF=" + etfRoot);

        // String tmp = etfRoot.getChildValue("PRELEN");

        String tmp = etfRoot.getChildNode("REC_1").getChildValue("PRELEN");

        if (StringUtils.isEmpty(tmp)) {
            throw new HiException(HiMessageCode.ERR_ETF_GET, "前置长度为空![PRELEN]");
        }
        log.debug("PRELEN=" + tmp);

        int preLen = Integer.valueOf(tmp);

        tmp = etfRoot.getChildNode("REC_1").getChildValue("START_POINT");
        log.debug("START_POINT=" + tmp);

        if (StringUtils.isEmpty(tmp)) {
            throw new HiException(HiMessageCode.ERR_ETF_GET, "包起始长度为空![START_POINT]");
        }

        int startPoint = Integer.valueOf(tmp);

        tmp = etfRoot.getChildNode("REC_1").getChildValue("END_POINT");

        if (StringUtils.isEmpty(tmp)) {
            throw new HiException(HiMessageCode.ERR_ETF_GET, "包结束长度为空![END_POINT]");
        }
        log.debug("END_POINT=" + tmp);

        int endPoint = Integer.valueOf(tmp);

        // 计算长度
        int packItemLength = endPoint - startPoint;

        HiByteBuffer plainBuf = (HiByteBuffer) mess.getObjectHeadItem(HiConstants.PLAIN_TEXT);

        HiByteBuffer hbb = new HiByteBuffer(1024);

        switch (preLen) {
            case 1:
                blen[0] = (byte) packItemLength;
                hbb.append(blen[0]);
                break;
            case 2:
                blen = HiByteUtil.shortToByteArray(packItemLength);
                hbb.append(blen);
                break;
            case 4:
                blen = HiByteUtil.intToByteArray(packItemLength);
                hbb.append(blen);
                break;
        }

        log.debug("packItemAndAddPreLen  plainBuf before=" + HiByteUtil.toHex(hbb.getBytes()));

        hbb.append(plainBuf.subbyte(startPoint, packItemLength));

        log.debug("packItemAndAddPreLen plainBuf after=" + HiByteUtil.toHex(hbb.getBytes()));

        return hbb;

    }

    /**
     * 取得打包的长度的值
     *
     * @param buf 字段的信息
     * @param ctx context
     * @return 补足的长度
     * @throws Exception
     */
    public HiByteBuffer packItemAndAddPreLen2(HiByteBuffer buf, HiMessageContext ctx) throws Exception {

        HiMessage msg = ctx.getCurrentMsg();
        Logger log = HiLog.getLogger(msg);
        if (ctx == null) {
            log.error("mess is null!");
            return null;
        }

        HiETF etfRoot = (HiETF) msg.getBody();
        // 取得起点的值
        int startPoint = Integer.valueOf(etfRoot.getChildValue("START_POINT"));
        int endPoint = Integer.valueOf(etfRoot.getChildValue("END_POINT"));
        // 计算长度
        int length = endPoint - startPoint;

        HiByteBuffer plainBuf = (HiByteBuffer) msg.getObjectHeadItem(HiConstants.PLAIN_TEXT);
        HiByteBuffer hbb = new HiByteBuffer(1024);
        hbb.append(plainBuf.subbyte(startPoint, length));
        return packItemAndAddPreLen(hbb, ctx);

    }

    /**
     * 位数不满的场合右边补充F
     *
     * @param buf 传入的Item的值
     * @param ctx context
     * @return
     */
    public HiByteBuffer filRightFWithBCD(HiByteBuffer buf, HiMessageContext ctx) {

        HiMessage msg = ctx.getCurrentMsg();
        Logger log = HiLog.getLogger(msg);
        log.info("HiExprHuiExt.filRightFWithBCD.buf" + buf.toString() + " --> ctx = " + ctx);

        // 判断是否输入数据
        if (StringUtils.isBlank(buf.toString())) {
            log.error("HiExprHuiExt.filRightFWithBCD buf is null");
            return buf;
        }

        // 判断是否满位
        if (buf.length() % 2 != 0) {
            buf.append(SUFFIX_F);
        }

        int len = buf.length() / 2;

        String itemValue = buf.toString();
        buf.clear();
        buf.append(HiConvHelper.ascStr2Bcd(itemValue.trim()));

        int trimLen = buf.length();

        for (int i = 0; i < len - trimLen; i++) {

            buf.append(BYTE_F);
        }

        log.info("return buf = " + buf.toString());
        return buf;
    }

    /**
     * 位数不满的场合左边补充0
     *
     * @param buf 传入的Item的值
     * @param ctx context
     * @return
     */
    public HiByteBuffer filLeft0WithBCD(HiByteBuffer buf, HiMessageContext ctx) {

        HiMessage msg = ctx.getCurrentMsg();
        Logger log = HiLog.getLogger(msg);

        log.info("HiExprHuiExt.filLeft0WithBCD.buf" + buf.toString() + " --> ctx = " + ctx);

        // 判断是否输入数据
        if (StringUtils.isBlank(buf.toString())) {
            log.error("HiExprHuiExt.filLeft0WithBCD buf is null");
            return buf;
        }

        HiByteBuffer tmpBuf = new HiByteBuffer(buf.length());
        // 判断是否满位
        if (buf.length() % 2 != 0) {
            tmpBuf.append(PREFIX_ZERO);
        }

        tmpBuf.append(buf.toString());
        String itemValue = tmpBuf.toString();
        tmpBuf.clear();
        tmpBuf.append(HiConvHelper.ascStr2Bcd(itemValue));
        log.info("HiExprHuiExt.filLeft0WithBCD.buf" + tmpBuf.toString());
        return tmpBuf;
    }

    /**
     * 对BCD编码的报文解包
     *
     * @param buf 当前Item长度的报文
     * @param ctx
     * @return
     */
    public HiByteBuffer unpackBcd1(HiByteBuffer buf, HiMessageContext ctx) {

        HiMessage msg = ctx.getCurrentMsg();
        Logger log = HiLog.getLogger(msg);
        log.info("HiExprHuiExt.unpackBcd1=" + buf.toString() + " --> ctx = " + ctx);

        // 判断是否输入数据
        if (StringUtils.isBlank(buf.toString())) {
            log.error("HiExprHuiExt.unpackBcd1 buf is null");
            return buf;
        }

        byte[] bytes = buf.getBytes();

        HiByteBuffer tmpBuf = new HiByteBuffer(1024);

        for (int i = 0; i < bytes.length; i++) {

            byte high = (byte) ((bytes[i] & 0xf0) >>> 4);

            if (high == 15)
                break;

            tmpBuf.append(high);

            byte low = (byte) (bytes[i] & 0x0f);

            if (low == 15)
                break;

            tmpBuf.append(low);

        }
        log.info("HiExprHuiExt.unpackBcd --> resTmp = " + tmpBuf.toString());

        return tmpBuf;

    }

    public HiByteBuffer unpackBcd(HiByteBuffer buf, HiMessageContext ctx) {
        HiMessage msg = ctx.getCurrentMsg();
        Logger log = HiLog.getLogger(msg);
        log.info("HiExprHuiExt.unpackBcd=" + buf.toString() + " --> ctx = " + ctx);

        // 判断是否输入数据
        if (StringUtils.isBlank(buf.toString())) {
            log.error("HiExprHuiExt.unpackBcd buf is null");
            return buf;
        }
        byte[] bytes = buf.getBytes();

        StringBuffer sb = new StringBuffer(bytes.length * 2);

        for (int i = 0; i < bytes.length; i++) {

            byte high = (byte) ((bytes[i] & 0xf0) >>> 4);

            if (high == 15)
                break;

            sb.append(high);

            byte low = (byte) (bytes[i] & 0x0f);

            if (low == 15)
                break;

            sb.append(low);

        }
        log.info("HiExprHuiExt.unpackBcd --> resTmp = " + sb.toString());

        if (sb.length() == 0)
            sb.append(" ");

        return new HiByteBuffer(sb.toString().getBytes());

    }

    /**
     * BIN码的报文解包
     *
     * @param buf Item的数据
     * @param ctx context
     * @return Item的HiByeBuffer的数据
     */
    public HiByteBuffer unpackBin(HiByteBuffer buf, HiMessageContext ctx) {
        HiMessage msg = ctx.getCurrentMsg();
        Logger log = HiLog.getLogger(msg);
        if (StringUtils.isBlank(buf.toString())) {
            log.error("HiExprHuiExt.unpackBin buf is null");
            return buf;
        }

        String tmp = HiConvHelper.binToAscStr(buf.getBytes());
        log.info("HiExprHuiExt.unpackBin --> resTmp = " + tmp);
        return new HiByteBuffer(tmp.getBytes());
    }

    /**
     * 一个字节的数的转换
     *
     * @param buf 需要转换的Item的值
     * @param ctx context
     * @return 转化成BIN码的Item数据
     * @throws HiException
     */
    public HiByteBuffer number2Byte(HiByteBuffer buf, HiMessageContext ctx) throws HiException {
        HiMessage msg = ctx.getCurrentMsg();
        Logger log = HiLog.getLogger(msg);
        if (StringUtils.isBlank(buf.toString())) {
            log.error("HiExprHuiExt.number2Byte buf is null");
            return buf;
        }

        int i = Integer.valueOf(buf.toString());

        byte b = (byte) i;

        HiByteBuffer hbb = new HiByteBuffer(1024);

        hbb.append(b);

        return hbb;

    }

    /**
     * 一个字节的数的转换
     *
     * @param buf 需要转换的Item的值
     * @param ctx context
     * @return 转化成BIN码的Item数据
     * @throws HiException
     */
    public HiByteBuffer byte2Number(HiByteBuffer buf, HiMessageContext ctx) throws HiException {
        HiMessage msg = ctx.getCurrentMsg();
        Logger log = HiLog.getLogger(msg);
        if (buf.getBytes() == null) {
            log.error("HiExprHuiExt.byte2Number buf is null");
            return buf;
        }

        int i = (byte) buf.getBytes()[0];

        String s = String.valueOf(i);

        HiByteBuffer hbb = new HiByteBuffer(1024);

        hbb.append(s);

        return hbb;

    }

    /**
     * 整型转字节数组的组包处理 字节长度为4
     *
     * @param buf 需要转换的Item的值
     * @param ctx context
     * @return 转化成BIN码的Item数据
     * @throws HiException
     */

    public HiByteBuffer int2ByteArray(HiByteBuffer buf, HiMessageContext ctx) throws HiException {
        HiMessage msg = ctx.getCurrentMsg();
        Logger log = HiLog.getLogger(msg);
        if (StringUtils.isBlank(buf.toString())) {
            log.error("HiExprHuiExt.int2ByteArray buf is null");
            return buf;
        }

        int i = Integer.valueOf(buf.toString());

        return new HiByteBuffer(HiByteUtil.intToByteArray(i));

    }

    /**
     * 整型转字节数组的解包处理 字节长度为4.
     *
     * @param buf 需要转换的Item的值
     * @param ctx context
     * @return 转化成BIN码的Item数据
     * @throws HiException
     */

    public HiByteBuffer byteArray2Int(HiByteBuffer buf, HiMessageContext ctx) throws HiException {
        HiMessage msg = ctx.getCurrentMsg();
        Logger log = HiLog.getLogger(msg);
        if (StringUtils.isBlank(buf.toString())) {
            log.error("HiExprHuiExt.byteArray2Int buf is null");
            buf.append("0");
        }

        String s = String.valueOf(HiByteUtil.byteArrayToInt(buf.getBytes()));

        return new HiByteBuffer(s.getBytes());

    }

    /**
     * 整型转字节数组的组包处理 字节长度为4
     *
     * @param buf 需要转换的Item的值
     * @param ctx context
     * @return 转化成BIN码的Item数据
     * @throws HiException
     */

    public HiByteBuffer long2ByteArray(HiByteBuffer buf, HiMessageContext ctx) throws HiException {
        HiMessage msg = ctx.getCurrentMsg();
        Logger log = HiLog.getLogger(msg);
        if (StringUtils.isBlank(buf.toString())) {
            log.error("HiExprHuiExt.int2ByteArray buf is null");
            return buf;
        }

        long i = Long.valueOf(buf.toString());

        System.out.println(HiByteUtil.longToByteArray(i));

        return new HiByteBuffer(HiByteUtil.longToByteArray(i));

    }

    /**
     * 短整型转字节数组的组包处理 字节长度为2.
     *
     * @param buf 需要转换的Item的值
     * @param ctx context
     * @return 转化成BIN码的Item数据
     * @throws HiException
     */

    public HiByteBuffer short2ByteArray(HiByteBuffer buf, HiMessageContext ctx) throws HiException {
        HiMessage msg = ctx.getCurrentMsg();
        Logger log = HiLog.getLogger(msg);
        if (StringUtils.isBlank(buf.toString())) {
            log.error("HiExprHuiExt.short2ByteArray buf is null");
            return buf;
        }

        short i = Short.valueOf(buf.toString());

        return new HiByteBuffer(HiByteUtil.shortToByteArray(i));

    }

    /**
     * 字节数组转短整形 解包使用。字节长度为2.
     *
     * @param buf 需要转换的Item的值
     * @param ctx context
     * @return 转化成BIN码的Item数据
     * @throws HiException
     */

    public HiByteBuffer byteArray2Short(HiByteBuffer buf, HiMessageContext ctx) throws HiException {
        HiMessage msg = ctx.getCurrentMsg();
        Logger log = HiLog.getLogger(msg);
        if (StringUtils.isBlank(buf.toString())) {
            log.error("HiExprHuiExt.byteArray2Short buf is null");
            return buf;
        } else {
            String s = String.valueOf(HiByteUtil.byteArrayToShort(buf.getBytes()));

            return new HiByteBuffer(s.getBytes());
        }

    }

    /**
     * 16进制字符串转字节数组
     *
     * @param buf 需要转换的Item的值
     * @param ctx context
     * @return 转化成BIN码的Item数据
     * @throws HiException
     */

    public HiByteBuffer hexStr2ByteArray(HiByteBuffer buf, HiMessageContext ctx) throws HiException {
        HiMessage msg = ctx.getCurrentMsg();
        Logger log = HiLog.getLogger(msg);
        if (StringUtils.isBlank(buf.toString())) {
            log.error("HiExprHuiExt.hexStr2ByteArray buf is null");
            return buf;
        }

        log.info("HiExprHuiExt.hexStr2ByteArray --> In buf = " + buf.toString());

        String hexStr = buf.toString();

        byte[] result = null;
        if (hexStr.substring(0, 1).equalsIgnoreCase("0x"))
            try {
                result = HiByteUtil.hexToByteArray(hexStr.substring(2));
            } catch (Exception e) {
                throw HiException.makeException(e);
            }
        else
            try {
                result = HiByteUtil.hexToByteArray(hexStr);
            } catch (Exception e) {
                throw HiException.makeException(e);
            }
        log.info("HiExprHuiExt.hexStr2ByteArray --> Out Buf = " + HiByteUtil.toHex(result));

        return new HiByteBuffer(result);

    }

    public HiByteBuffer hexStr2ByteArrayCN(HiByteBuffer buf, HiMessageContext ctx) throws HiException {
        HiMessage msg = ctx.getCurrentMsg();
        Logger log = HiLog.getLogger(msg);
        if (StringUtils.isBlank(buf.toString())) {
            log.error("HiExprHuiExt.hexStr2ByteArray buf is null");
            return buf;
        }

        log.info("HiExprHuiExt.hexStr2ByteArray --> In buf = " + buf.toString());

        String hexStr = buf.toString();

        byte[] result = null;
        if (hexStr.startsWith("0x"))
            try {
                result = HiByteUtil.hexToByteArray(hexStr.substring(2));
            } catch (Exception e) {
                throw HiException.makeException(e);
            }
        else
            result = buf.getBytes();
        log.info("HiExprHuiExt.hexStr2ByteArray --> Out Buf = " + HiByteUtil.toHex(result));

        return new HiByteBuffer(result);

    }

    /**
     * 字节数组 转16进制字符串
     *
     * @param buf 需要转换的Item的值
     * @param ctx context
     * @return 转化成BIN码的Item数据
     * @throws HiException
     */

    public HiByteBuffer byteArray2HexStr(HiByteBuffer buf, HiMessageContext ctx) throws HiException {
        HiMessage msg = ctx.getCurrentMsg();
        Logger log = HiLog.getLogger(msg);
        if (StringUtils.isBlank(buf.toString())) {
            log.error("HiExprHuiExt.byteArray2HexStr buf is null");
            return buf;
        }

        return new HiByteBuffer(HiByteUtil.byteArrayToHex(buf.getBytes()).getBytes());

    }

    /**
     * 将127.0.0.1形式的IP地址转换成十进制整数
     *
     * @param buf 需要转换的Item的值
     * @param ctx context
     * @return 转化成BIN码的Item数据
     * @throws HiException
     */
    public HiByteBuffer ipToLong(HiByteBuffer buf, HiMessageContext ctx) {
        HiMessage msg = ctx.getCurrentMsg();
        Logger log = HiLog.getLogger(msg);
        if (StringUtils.isBlank(buf.toString())) {
            log.error("HiExprHuiExt.ipToLong buf is null");
            return buf;
        }

        String strIp = buf.toString();

        long[] ip = new long[4];
        // 先找到IP地址字符串中.的位置
        int position1 = strIp.indexOf(".");
        int position2 = strIp.indexOf(".", position1 + 1);
        int position3 = strIp.indexOf(".", position2 + 1);
        // 将每个.之间的字符串转换成整型
        ip[0] = Long.parseLong(strIp.substring(0, position1));
        ip[1] = Long.parseLong(strIp.substring(position1 + 1, position2));
        ip[2] = Long.parseLong(strIp.substring(position2 + 1, position3));
        ip[3] = Long.parseLong(strIp.substring(position3 + 1));
        long lIp = (ip[0] << 24) + (ip[1] << 16) + (ip[2] << 8) + ip[3];

        HiByteBuffer bb = new HiByteBuffer(1024);
        bb.writeLong(lIp);

        return bb;
    }

    /**
     * 将十进制整数形式转换成127.0.0.1形式的ip地址
     *
     * @param buf 需要转换的Item的值
     * @param ctx context
     * @return 转化成BIN码的Item数据
     * @throws HiException
     */
    public HiByteBuffer longToIP(HiByteBuffer buf, HiMessageContext ctx) {
        HiMessage msg = ctx.getCurrentMsg();
        Logger log = HiLog.getLogger(msg);
        if (StringUtils.isBlank(buf.toString())) {
            log.error("HiExprHuiExt.longToIP buf is null");
            return buf;
        }

        long longIp = Long.valueOf(buf.toString());

        StringBuffer sb = new StringBuffer("");
        // 直接右移24位
        sb.append(String.valueOf((longIp >>> 24)));
        sb.append(".");
        // 将高8位置0，然后右移16位
        sb.append(String.valueOf((longIp & 0x00FFFFFF) >>> 16));
        sb.append(".");
        // 将高16位置0，然后右移8位
        sb.append(String.valueOf((longIp & 0x0000FFFF) >>> 8));
        sb.append(".");
        // 将高24位置0
        sb.append(String.valueOf((longIp & 0x000000FF)));

        return new HiByteBuffer(sb.toString().getBytes());
    }

    /**
     * 将十进制整数形式转换成127.0.0.1形式的ip地址 转成int型。
     *
     * @param buf 需要转换的Item的值
     * @param ctx context
     * @return 转化成BIN码的Item数据
     * @throws HiException
     */

    public HiByteBuffer htons(HiByteBuffer buf, HiMessageContext ctx) {
        HiMessage msg = ctx.getCurrentMsg();
        Logger log = HiLog.getLogger(msg);
        if (StringUtils.isBlank(buf.toString())) {
            log.error("HiExprHuiExt.longToIP buf is null");
            return buf;
        }

        String strIp = buf.toString();

        int[] ip = new int[4];

        byte[] ipByte = new byte[4];

        // 先找到IP地址字符串中.的位置
        int position1 = strIp.indexOf(".");
        int position2 = strIp.indexOf(".", position1 + 1);
        int position3 = strIp.indexOf(".", position2 + 1);

        // 将每个.之间的字符串转换成整型
        ip[0] = Integer.parseInt(strIp.substring(0, position1));
        ip[1] = Integer.parseInt(strIp.substring(position1 + 1, position2));
        ip[2] = Integer.parseInt(strIp.substring(position2 + 1, position3));
        ip[3] = Integer.parseInt(strIp.substring(position3 + 1));

        ipByte[0] = (byte) (ip[0]);
        ipByte[1] = (byte) (ip[1]);
        ipByte[2] = (byte) (ip[2]);
        ipByte[3] = (byte) (ip[3]);

        return new HiByteBuffer(ipByte);
    }

    /**
     * TLV码的解包方式
     *
     * @param buf Item的TLV码数据
     * @param ctx context
     * @return Item的HiByteBuffer数据
     */
    public HiByteBuffer unpackTlv(HiByteBuffer buf, HiMessageContext ctx) {
        HiMessage msg = ctx.getCurrentMsg();
        Logger log = HiLog.getLogger(msg);
        if (StringUtils.isBlank(buf.toString())) {
            log.error("HiExprHuiExt.unpackTlv buf is null");
            return buf;
        }

        return buf;
    }

    /**
     * TLV码的组包方式
     *
     * @param buf Item的HiByteBuffer数据
     * @param ctx context
     * @return Item的TLV数据
     */
    public HiByteBuffer packTlv(HiByteBuffer buf, HiMessageContext ctx) {
        HiMessage msg = ctx.getCurrentMsg();
        Logger log = HiLog.getLogger(msg);
        if (StringUtils.isBlank(buf.toString())) {
            log.error("HiExprHuiExt.packTlv buf is null");
            return buf;
        }

        return buf;
    }
}
