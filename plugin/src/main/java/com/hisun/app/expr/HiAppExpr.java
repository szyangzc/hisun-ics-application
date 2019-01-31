package com.hisun.app.expr;

import com.hisun.app.tools.HiDateUtilExt;
import com.hisun.atc.HiATCConstants;
import com.hisun.charset.impl.HiCharsetFactory;
import com.hisun.charset.impl.HiCharsetProcess;
import com.hisun.constants.HiMessageCode;
import com.hisun.crypt.Decryptor;
import com.hisun.crypt.Encryptor;
import com.hisun.crypt.des.DESCryptorFactory;
import com.hisun.engine.invoke.impl.HiItemHelper;
import com.hisun.exception.HiException;
import com.hisun.hilog4j.HiLog;
import com.hisun.hilog4j.Logger;
import com.hisun.lang.HiByteUtil;
import com.hisun.lang.HiConvHelper;
import com.hisun.message.HiMessage;
import com.hisun.message.HiMessageContext;
import com.hisun.util.date.HiDateUtils;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HiAppExpr {
    private final static String FALSE = "0";
    private final static String TRUE = "1";

    /**
     * 取日期包括毫秒，格式如2016-02-54 19:02:18,104
     *
     * @param ctx
     * @return
     * @throws HiException
     */
    public static String GETDATEMILLS(Object ctx) throws HiException {
        Calendar calendar = Calendar.getInstance();
        return DateFormatUtils.format(calendar.getTime(), "yyyyMMddHHmmssSSS");
    }

    /**
     * 截取包念中文的字符串时避免有半个字出现。
     *
     * @param ctx
     * @param args
     * @return
     */
    public static String SUBMULTBYTE(Object ctx, String args[]) throws HiException {
        if (args.length != 2)
            throw new HiException(HiMessageCode.ERR_EXP_ARGS_SIZE, "SUBMULTBYTE");
        String sMultbyte = args[0];
        int len = Integer.parseInt(args[1]);
        return HiConvHelper.substringMultByte(sMultbyte, len);
    }

    /**
     * 功能：字符集转换表达式
     *
     * @param ctx
     * @param args
     * @return
     * @throws HiException
     */
    public static String CONV_CHARSET(Object ctx, String[] args) throws HiException {
        if (args.length != 3)
            throw new HiException(HiMessageCode.ERR_EXP_ARGS_SIZE, "CONV_CHARSET");
        String src = args[0].trim();
        String fromCharset = args[1].trim();
        String toCharset = args[2].trim();
        try {
            return new String(src.getBytes(fromCharset), toCharset);
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new HiException(e);
        }
    }

    /**
     * 判断指定的日期是不是月末
     *
     * @param ctx ymd
     * @return
     * @throws HiException
     */
    public static String IS_MONTH_ENDDAY(Object ctx, String[] args) throws HiException {
        if (args.length != 1)
            throw new HiException(HiMessageCode.ERR_EXP_ARGS_SIZE,
                    "IS_MONTH_ENDDAY YYYYMMDD");
        String dateStr = args[0];
        // 年
        int year = Integer.valueOf(dateStr.substring(0, 4));
        // 月
        int month = Integer.valueOf(dateStr.substring(4, 6));

        Date date = null;
        try {
            date = HiDateUtils.parseDate(dateStr);
        } catch (ParseException e) {
            throw HiException.makeException(e);
        }
        if (HiDateUtilExt.getLastDayOfMonth(year, month) == date) {
            return TRUE;
        } else {
            return FALSE;
        }
    }

    /**
     * 功能：取下一个日期
     *
     * @param ctx
     * @return
     * @throws HiException
     */
    public static String NEXT_DAY(Object ctx) throws HiException {
        Date today = new Date();
        Date nextDate = HiDateUtilExt.getAnotherDate(today, 1);
        String sNextDate = HiDateUtils.format(nextDate, "yyyyMMdd");
        return sNextDate;
    }

    /**
     * 取上一个日期
     *
     * @param ctx
     * @return
     * @throws HiException
     */
    public static String LAST_DAY(Object ctx) throws HiException {
        Date today = new Date();
        Date nextDate = HiDateUtilExt.getAnotherDate(today, -1);
        String sNextDate = HiDateUtils.format(nextDate, "yyyyMMdd");
        return sNextDate;
    }

    /**
     * 功能：取文件的最后修改日期
     *
     * @param ctx
     * @param args
     * @return
     * @throws HiException
     */
    public static String GET_FILE_DATE(Object ctx, String[] args)
            throws HiException {
        if (args.length != 1)
            throw new HiException(HiMessageCode.ERR_EXP_ARGS_SIZE,
                    "GET_FILE_DATE");
        String fileName = args[0].trim();

        File file = new File(fileName);
        if (file.exists()) {
            long lTime = file.lastModified();
            Date date = new Date(lTime);
            DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            return dateFormat.format(date);
        } else {
            return "";
        }
    }

    /**
     * 从文件路径中取文件名称
     *
     * @param ctx
     * @param args
     * @return
     * @throws HiException
     */
    public static String GET_FILNM_FROM_PATH(Object ctx, String[] args)
            throws HiException {
        if (args.length != 1)
            throw new HiException(HiMessageCode.ERR_EXP_ARGS_SIZE,
                    "GET_FILNM_FROM_PATH");
        String filePath = args[0].trim();

        int idx = filePath.lastIndexOf(File.separator) + 1;
        return filePath.substring(idx);

    }

    /**
     * 日期格式任意转换
     *
     * @param ctx
     * @param args
     * @return
     * @throws HiException
     */
    public static String FMTDATEANOTHER(Object ctx, String[] args)
            throws HiException {
        if (args.length != 3)
            throw new HiException(HiMessageCode.ERR_EXP_ARGS_SIZE, "FMTDATE");
        String str = args[0].trim();
        String pattern1 = args[1].trim();
        String pattern2 = args[2].trim();

        if (StringUtils.isEmpty(pattern1) || StringUtils.isEmpty(pattern2)) {
            throw new HiException(HiMessageCode.ERR_EXP_ARGS, "FMTDATE",
                    "FORMATIN FORMATOUT");
        }

        String[] pattern = {pattern1};
        Date date;
        try {
            date = DateUtils.parseDate(str, pattern);
            return DateFormatUtils.format(date, pattern2);
        } catch (ParseException e) {
            throw new HiException(HiMessageCode.ERR_EXP_INVOKE, "FMTDATE", e);
        }
    }

    /**
     * 去金额格式化中的各种符号：如122,112,222.21为1221122222.21
     *
     * @param ctx
     * @param args
     * @return
     * @throws HiException
     */
    public static String UNFMTAMT(Object ctx, String[] args) throws HiException {
        if (args.length != 1)
            throw new HiException(HiMessageCode.ERR_EXP_ARGS_SIZE, "UNFMTAMT");
        String fmtAmount = args[0].trim();

        // 只允数字
        String regEx = "[^0-9|\\.]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(fmtAmount);
        fmtAmount = m.replaceAll("").trim();

        return fmtAmount;
    }

    public static String FMTAMT(Object ctx, String[] args) throws HiException {
        if (args.length != 1)
            throw new HiException(HiMessageCode.ERR_EXP_ARGS_SIZE, "fmtAmt");
        HiMessage mess = ((HiMessageContext) ctx).getCurrentMsg();
        Logger log = HiLog.getLogger(mess);

        if (mess == null || ctx == null) {
            throw new HiException(HiMessageCode.ERR_SYSTEM, "前置系统错");
        }

        String amt = args[0];
        NumberFormat nf = NumberFormat.getCurrencyInstance();
        String formatAmt = nf.format(Double.parseDouble(amt));

        // System.out.println(formatAmt);

        return formatAmt.substring(1);

    }

    /**
     * 对转义后的XML节点恢复
     *
     * @param argsMap
     * @param ctx
     * @return
     * @throws HiException
     */
    public static String UNESCAPEXML(Object ctx, String[] args)
            throws HiException {
        if (args.length != 1)
            throw new HiException(HiMessageCode.ERR_EXP_ARGS_SIZE,
                    "unescapeXml");
        HiMessage mess = ((HiMessageContext) ctx).getCurrentMsg();
        Logger log = HiLog.getLogger(mess);
        if (mess == null || ctx == null) {
            throw new HiException(HiMessageCode.ERR_SYSTEM, "前置系统错");
        }

        String source = args[0];

        return StringEscapeUtils.unescapeXml(source);

    }

    /**
     * 对XML节点转义
     *
     * @param argsMap
     * @param ctx
     * @return
     * @throws HiException
     */
    public String ESCAPEXML(Object ctx, String[] args) throws HiException {
        if (args.length != 1)
            throw new HiException(HiMessageCode.ERR_EXP_ARGS_SIZE, "escapeXml");
        HiMessage mess = ((HiMessageContext) ctx).getCurrentMsg();
        Logger log = HiLog.getLogger(mess);
        if (mess == null || ctx == null) {
            throw new HiException(HiMessageCode.ERR_SYSTEM, "前置系统错");
        }

        String source = args[0];

        return StringEscapeUtils.escapeXml(source);

    }

    /**
     * @param ctx
     * @param args
     * @return
     * @throws Exception
     */
    public static String DES(Object ctx, String[] args) throws Exception {
        if (args.length < 2)
            throw new HiException(HiMessageCode.ERR_EXP_ARGS_SIZE, "DES");

        String data = args[0];
        String mode = args[1];

        DESCryptorFactory factory = new DESCryptorFactory();
        Decryptor decryptor = factory.getDecryptor();
        decryptor.setKey(factory.getDefaultDecryptKey());
        Encryptor encryptor = factory.getEncryptor();
        encryptor.setKey(factory.getDefaultEncryptKey());
        // String data =
        // "4321748374jfkdsflkadsjf.,.,!&**&&@*&(@#(@#*(@*#(@*#(@*#fjdkajfksajfkkk))((((*@@#@#@#@#@#@#@dfdjfkja,,<<<<<jfdkajffdjkasfj*(*(-21";

        byte[] bs = null;

        // 如果是加密模式
        if (StringUtils.equalsIgnoreCase(mode, "E")) {

            bs = encryptor.encrypt(data.getBytes());

            return HiByteUtil.toHex(bs).substring(2);

        } else if (StringUtils.equalsIgnoreCase(mode, "D")) { // 如果是解密模式

            return new String(decryptor.decrypt(Hex.decodeHex(data
                    .toCharArray())));
        }
        return null;
    }

    /**
     * <p>
     * <p/>
     * </p>
     */
    public static String URLENCODER(Object ctx, String[] args)
            throws HiException {
        if (args.length != 2)
            throw new HiException(HiMessageCode.ERR_EXP_ARGS_SIZE, "URLEncoder");
        try {
            String str = args[0];
            String encoding = args[1];
            return java.net.URLEncoder.encode(str, encoding);
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new HiException(e);
        }

    }

    /**
     * @param ctx
     * @param args
     * @return
     * @throws HiException
     */
    public static String URLDECODER(Object ctx, String[] args)
            throws HiException {
        if (args.length != 2)
            throw new HiException(HiMessageCode.ERR_EXP_ARGS_SIZE, "URLEncoder");
        try {
            String str = args[0];
            String encoding = args[1];
            return java.net.URLDecoder.decode(str, encoding);
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new HiException(e);
        }

    }

    /**
     * @param ctx
     * @return
     * @throws HiException
     */
    public static String HOSTNAME(Object ctx) throws HiException {
        // HiMessageContext msgCtx = ctx;
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            throw new HiException(e);
        }
    }

    /**
     * @param ctx
     * @param args
     * @return
     * @throws HiException
     */
    public static String IXPMAC(Object ctx, String[] args) throws HiException {
        // HiMessageContext msgCtx = ctx;
        if (StringUtils.isEmpty(args[0]))
            return String.valueOf(HiATCConstants.SUCC);
        String src = args[0];
        MessageDigest md5 = null;
        String mac = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            mac = HiByteUtil.byteArrayToHex(md5.digest(src.getBytes()));
            System.err.println(mac);
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new HiException(e);
        }
        return mac;
    }

    /**
     * @param ctx
     * @param args
     * @return
     * @throws HiException
     */
    public static String MAC(Object ctx, String[] args) throws HiException {
        // HiMessageContext msgCtx = ctx;
        if (StringUtils.isEmpty(args[0]))
            return String.valueOf(HiATCConstants.SUCC);
        String src = args[0];
        String s = null;
        char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'a', 'b', 'c', 'd', 'e', 'f'};

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(src.getBytes());
            byte[] tmp = md.digest();

            char[] str = new char[32];

            int k = 0;
            for (int i = 0; i < 16; ++i) {
                byte byte0 = tmp[i];
                str[(k++)] = hexDigits[(byte0 >>> 4 & 0xF)];

                str[(k++)] = hexDigits[(byte0 & 0xF)];
            }
            s = new String(str);
            System.out.println(s);
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return s;
    }

    /**
     * @param ctx
     * @param args
     * @return
     * @throws HiException
     */
    public static String EXECCONDITION(Object ctx, String[] args)
            throws HiException {
        // HiMessageContext msgCtx = ctx;
        if (StringUtils.isEmpty(args[0]))
            return String.valueOf(HiATCConstants.SUCC);
        HiMessage msg = ((HiMessageContext) ctx).getCurrentMsg();
        String val = HiItemHelper.execExpression(msg, args[0]);

        return val;
    }

    /**
     * 字符集转换，
     *
     * @param ctx
     * @param args ：参数1：要转的源，参数2：源的字符集，参数3：目标字符集
     * @return
     * @throws HiException
     */
    public static String CHARSETCONVERT(Object ctx, String[] args)
            throws HiException {

        if (args.length < 4) {
            throw new HiException(HiMessageCode.ERR_AGR_NOT_FOUND,
                    "param is invalid");
        }
        HiMessage mess = ((HiMessageContext) ctx).getCurrentMsg();
        Logger log = HiLog.getLogger(mess);

        String src = args[0];
        String srcCharset = args[1];
        String dstCharset = args[2];

        if (log.isDebugEnabled()) {
            log.debug(srcCharset, src);
            try {
                log.debug("src hex string",
                        HiByteUtil.toHex(src.getBytes(srcCharset)));
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        HiCharsetProcess charsetConvert = HiCharsetFactory.createConverter(
                srcCharset, dstCharset);

        String dst = charsetConvert.convertLocal(src);
        if (log.isDebugEnabled()) {
            log.debug(dstCharset, dst);
            try {
                log.debug("dest hex string",
                        HiByteUtil.toHex(dst.getBytes(dstCharset)));
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return dst;
    }

    /**
     * @param ctx
     * @param args
     * @return
     * @throws HiException
     */
    public static String HEX2BASE64(Object ctx, String[] args)
            throws HiException {
        HiMessage mess = ((HiMessageContext) ctx).getCurrentMsg();
        Logger log = HiLog.getLogger(mess);

        if (args.length < 1) {
            log.error("You should input a pack name!!!");
        }

        String hexStr = args[0];

        byte[] pkisign = new byte[0];
        try {
            pkisign = HiByteUtil.hexToByteArray(hexStr);
        } catch (Exception e) {
            throw HiException.makeException(e);
        }

        return new sun.misc.BASE64Encoder().encode(pkisign);
    }

    /**
     * @param ctx
     * @param args
     * @return
     * @throws HiException
     */
    public static String EBASE64(Object ctx, String[] args)
            throws HiException {
        HiMessage mess = ((HiMessageContext) ctx).getCurrentMsg();
        Logger log = HiLog.getLogger(mess);
        if (args.length < 1) {
            log.error("You should input a pack name!!!");
        }

        return new sun.misc.BASE64Encoder().encode(args[0].getBytes());
    }

    public static String DBASE64(Object ctx, String[] args)
            throws HiException {
        HiMessage mess = ((HiMessageContext) ctx).getCurrentMsg();
        Logger log = HiLog.getLogger(mess);
        if (args.length < 1) {
            log.error("You should input a pack name!!!");
        }

        try {
            return new String(
                    new sun.misc.BASE64Decoder().decodeBuffer(args[0]), args[1]);
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new HiException(e);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new HiException(e);
        }
    }

    /**
     * @param ctx
     * @param args
     * @return
     * @throws HiException
     * @throws DecoderException
     */
    public static String BCD2NORMAL(Object ctx, String[] args) throws HiException,
            DecoderException {
        if (args.length != 1)
            throw new HiException(HiMessageCode.ERR_EXP_ARGS_SIZE, "BCD2NORMAL");
        HiMessage mess = ((HiMessageContext) ctx).getCurrentMsg();
        Logger log = HiLog.getLogger(mess);
        log.debug("args[0]=", HiByteUtil.toHex(args[0].getBytes()));

        byte[] bytes = args[0].getBytes();

        StringBuffer temp = new StringBuffer(bytes.length * 2);

        for (int i = 0; i < bytes.length; i++) {

            temp.append((byte) ((bytes[i] & 0xf0) >>> 4));

            temp.append((byte) (bytes[i] & 0x0f));
        }
        return temp.toString().substring(0, 1).equalsIgnoreCase("0") ? temp
                .toString().substring(1) : temp.toString();
    }

    /** */
    /**
     * @throws UnsupportedEncodingException
     * @函数功能: 10进制串转为BCD码
     * @输入参数: 10进制串
     * @输出结果: BCD码
     */

    public static String NORMAL2BCD(Object ctx, String[] args) throws HiException,
            UnsupportedEncodingException {
        if (args.length != 1)
            throw new HiException(HiMessageCode.ERR_EXP_ARGS_SIZE, "NORMAL2BCD");

        String asc = args[0];
        int len = asc.length();
        int mod = len % 2;

        if (mod != 0) {
            asc = "0" + asc;
            len = asc.length();
        }

        byte abt[] = new byte[len];
        if (len >= 2) {
            len = len / 2;
        }

        byte bbt[] = new byte[len];
        abt = asc.getBytes();
        int j, k;

        for (int p = 0; p < asc.length() / 2; p++) {
            if ((abt[2 * p] >= '0') && (abt[2 * p] <= '9')) {
                j = abt[2 * p] - '0';
            } else if ((abt[2 * p] >= 'a') && (abt[2 * p] <= 'z')) {
                j = abt[2 * p] - 'a' + 0x0a;
            } else {
                j = abt[2 * p] - 'A' + 0x0a;
            }

            if ((abt[2 * p + 1] >= '0') && (abt[2 * p + 1] <= '9')) {
                k = abt[2 * p + 1] - '0';
            } else if ((abt[2 * p + 1] >= 'a') && (abt[2 * p + 1] <= 'z')) {
                k = abt[2 * p + 1] - 'a' + 0x0a;
            } else {
                k = abt[2 * p + 1] - 'A' + 0x0a;
            }

            int a = (j << 4) + k;
            byte b = (byte) a;
            bbt[p] = b;
        }

        // System.out.println("before covert:");
        // printHexString(bbt);

        // System.out.println("\nconvert after:");

        return HiByteUtil.toBin(bbt);

    }

    /**
     * UCS2解码
     *
     * @param src UCS2 源串
     * @return 解码后的UTF-16BE字符串
     * @throws Exception
     */
    public static String DUCS2(String src) throws Exception {
        byte[] bytes = new byte[src.length() / 2];
        for (int i = 0; i < src.length(); i += 2) {
            bytes[i / 2] = (byte) (Integer
                    .parseInt(src.substring(i, i + 2), 16));
        }
        String reValue;
        try {
            reValue = new String(bytes, "UTF-16BE");
        } catch (UnsupportedEncodingException e) {
            throw new Exception(e);
        }
        return reValue;

    }

    /**
     * UCS2编码
     *
     * @param src UTF-16BE编码的源串
     * @return 编码后的UCS2串
     * @throws Exception
     */
    public static String EUCS2(String src) throws Exception {

        byte[] bytes;
        try {
            bytes = src.getBytes("UTF-16BE");
        } catch (UnsupportedEncodingException e) {
            throw new Exception(e);
        }

        StringBuffer reValue = new StringBuffer();
        StringBuffer tem = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            tem.delete(0, tem.length());
            tem.append(Integer.toHexString(bytes[i] & 0xFF));
            if (tem.length() == 1) {
                tem.insert(0, '0');
            }
            reValue.append(tem);
        }
        return reValue.toString().toUpperCase();
    }
}
