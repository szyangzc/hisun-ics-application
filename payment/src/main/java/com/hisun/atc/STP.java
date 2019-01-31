package com.hisun.atc;

import com.hisun.atc.common.HiArgUtils;
import com.hisun.constants.HiMessageCode;
import com.hisun.database.HiDataBaseUtil;
import com.hisun.database.HiResultSet;
import com.hisun.exception.HiException;
import com.hisun.hilog4j.HiLog;
import com.hisun.hilog4j.Logger;
import com.hisun.message.*;
import com.hisun.util.HiICSProperty;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * @author devwm
 * @description the new component for SWITCH-REGION
 * @create date Jul 11, 2008
 */
public class STP {


    public static void main(String[] args) throws Exception {
        String s1 = "x|y|z";
        String s2 = "a||c";
        StringTokenizer token = new StringTokenizer(s1, "|");
        int i = 0;

        String[] as = s2.split("\\|");
        for (String ss : as) {
            System.out.println(ss);
        }
        while (token.hasMoreTokens()) {
            String s3 = token.nextToken();
            String s4 = as[i++];
            System.out.println(s3 + "=" + s4);
        }
    }

    /**
     * createGWA
     *
     * @param argsMap
     * @param ctx
     * @return @GWA.
     * @throws HiException
     */
    public int InitGWA(HiATLParam argsMap, HiMessageContext ctx)
            throws HiException {
        HiMessage mess = ctx.getCurrentMsg();
        HiETF etfRoot = (HiETF) mess.getBody();
        HiETF etfGWA = (HiETF) HiETFFactory.createETF();

        ctx.setBaseSource("GWA", etfGWA);
        SimpleDateFormat fDate = new SimpleDateFormat(
                "yyyyMMdd");
        SimpleDateFormat fTime = new SimpleDateFormat(
                "HHmmss");
        Calendar calendar = Calendar.getInstance();

        Map<String, String> temp_map = null;
        HiDataBaseUtil dbUtil = (HiDataBaseUtil) ctx.getDataBaseUtil();
        // >>>>>>>>>>>>>獲取會計日相關
        String sql = "select SYST_AC_DATE,SYST_LAST_AC_DATE,SYST_NEXT_AC_DATE,SYST_SYS_MODE from STPTSYST";
        List list = dbUtil.execQuery(sql);
        if ((list == null) || (list.size() == 0)) {
            // //dbUtil.close();
            return -1;
        }
        // //dbUtil.close();
        java.util.Iterator stptsyst_it = list.iterator();
        temp_map = (Map) stptsyst_it.next();
        String SYST_AC_DATE = temp_map.get("SYST_AC_DATE");
        String SYST_LAST_AC_DATE = temp_map.get("SYST_LAST_AC_DATE");
        String SYST_NEXT_AC_DATE = temp_map.get("SYST_NEXT_AC_DATE");
        String SYST_SYS_MODE = temp_map.get("SYST_SYS_MODE");

        // >>>>>>>>>>>>>>獲取交易屬性相關
        String TRAN_TX_CODE = etfRoot.getChildValue("TIA_TXN_CD");
        String TRAN_TX_TYPE = "0";
        String TRAN_TX_LVL = "0";
        String TRAN_AUTH_LVL = "00";
        String TRAN_AUTH_SELF = "1";
        String TRAN_IS_LOG = "Y";
        sql = "select TRAN_TX_TYPE,TRAN_TX_LVL,TRAN_AUTH_LVL,TRAN_AUTH_SELF,TRAN_IS_LOG from stpttran where tran_tx_code="
                + "'" + TRAN_TX_CODE + "'";
        list = dbUtil.execQuery(sql);
        if ((list == null) || (list.size() == 0)) {
            // //dbUtil.close();
        } else {
            // //dbUtil.close();
            java.util.Iterator stpttran_it = list.iterator();
            temp_map = (Map) stpttran_it.next();
            TRAN_TX_TYPE = temp_map.get("TRAN_TX_TYPE");
            TRAN_TX_LVL = temp_map.get("TRAN_TX_LVL");
            TRAN_AUTH_LVL = temp_map.get("TRAN_AUTH_LVL");
            TRAN_AUTH_SELF = temp_map.get("TRAN_AUTH_SELF");
            TRAN_IS_LOG = temp_map.get("TRAN_IS_LOG");
        }

        // >>>>>>>>>>>>>>沖正標誌
        String CANCEL_IND = "0";
        if (TRAN_TX_CODE.substring(6) == "9") {
            CANCEL_IND = "1";
        }

        String GWA_JRN_NO = etfRoot.getChildValue("TIA_JRN_NO"); // 日志号
        String GWA_AC_DATE = SYST_AC_DATE; // 会计日期
        String GWA_LAST_AC_DATE = SYST_LAST_AC_DATE; // 上一會計日
        String GWA_NEXT_AC_DATE = SYST_NEXT_AC_DATE; // 下一會計日
        String GWA_SYS_MODE = SYST_SYS_MODE; // 系統狀態
        String GWA_AP_CODE = etfRoot.getChildValue("TIA_TXN_CD")
                .substring(0, 3); // 应用码
        String GWA_AP_MMO = "XX";// 应用符
        String GWA_TR_CODE = etfRoot.getChildValue("TIA_TXN_CD").substring(3); // 交易码
        String GWA_CHNL = "TL"; // 交易渠道
        String GWA_CANCEL_IND = CANCEL_IND; // 冲正标志，规定9结尾的交易码
        String GWA_TERM_ID = etfRoot.getChildValue("GWA_TERM_ID"); // 终端号
        String GWA_TL_ID = etfRoot.getChildValue("TIA_USER_ID"); // 柜员号
        String GWA_VCH_NO = ""; // 传票号，应用更新
        String GWA_HQT_BANK = "1111"; // 总行行号 HARDCODE
        String GWA_TR_BANK = "1"; // 交易行号（从数据库中提取）
        String GWA_TR_BRANCH = "1"; // 交易分行号（从数据库中提取）
        String GWA_TR_DEP = etfRoot.getChildValue("TIA_DEPT_NO"); // 交易部门号（从数据库中提取）
        String GWA_TR_DATE = fDate.format(calendar.getTime()); // 当前交易日期
        String GWA_TR_TIME = fTime.format(calendar.getTime()); // 当前交易时间
        String GWA_SUP1_ID = etfRoot.getChildValue("TIA_SUP1_ID"); // 授权柜员1
        String GWA_SUP2_ID = etfRoot.getChildValue("TIA_SUP2_ID"); // 授权柜员2
        String GWA_AUTH_LVL = "00"; // 授权级别 应用可修改
        String GWA_AUTH_RESN_TBL = "";// 授权原因 应用可修改
        String GWA_VCH_CNT = "0"; // 传票数量 应用可修改
        String GWA_HIS_CNT = "0"; // 历史摘要数量 应用可修改
        String GWA_MSG_TYPE = "N"; // 消息类型 後繼更新
        String GWA_MSG_AP_MMO = "RM"; // 应用码 後繼更新
        String GWA_MSG_CODE = ""; // 消息码 後繼更新
        String GWA_TRTT_TYPE = TRAN_TX_TYPE; // 交易類型
        String GWA_TRTT_CLS = TRAN_TX_LVL; // 交易级别
        String GWA_TRTT_AUTH_LVL = TRAN_AUTH_LVL; // 授权级别
        String GWA_TRTT_SELF_AUTH = TRAN_AUTH_SELF; // 自授權標誌
        String GWA_TRTT_LOG_JRN = TRAN_IS_LOG; // 记录日志标记
        String GWA_TRTT_SUBS_TRN_CODE = "";// 後繼更新
        String GWA_TRTT_SUBS_AP_CODE = ""; // 後繼更新
        String GWA_HIGHLIGHT = ""; // 前端有错误的栏位名称组合
        String GWA_AML_FLG = "0"; // 反洗黑錢標誌
        String GWA_SWIFT_SEND_FLG = "N"; // 異步發送SWIFT報文
        String GWA_JRN_JRN_FILE = "";
        /* using for aibs */
        String GWA_AIBS_CANCEL_FLG = "";
        String GWA_AIBS_OK_FLG = "";
        String GWA_AIBS_TLR_NAME = "";
        String GWA_AIBS_PASSWORD = "";
        String GWA_AIBS_WKSTN = "";
        String GWA_AIBS_USING_TAIBS_FLG = "";
        String GWA_AIBS_IS_ALREADY_LOGON = "";
        String GWA_USING_AIBS_JRN_CSV1 = "";
        String GWA_USING_AIBS_JRN_CSV2 = "";
        String GWA_BANK_ID = "";
        String BANK_ID = HiICSProperty.getProperty("ics.plt.org_code_name");
        if (StringUtils.isNotEmpty(BANK_ID))
            GWA_BANK_ID = etfRoot.getChildValue(BANK_ID);
        etfGWA.setChildValue("GWA_BANK_ID", GWA_BANK_ID);
        etfGWA.setChildValue("GWA_JRN_NO", GWA_JRN_NO);
        etfGWA.setChildValue("GWA_AC_DATE", GWA_AC_DATE);
        etfGWA.setChildValue("GWA_LAST_AC_DATE", GWA_LAST_AC_DATE);
        etfGWA.setChildValue("GWA_NEXT_AC_DATE", GWA_NEXT_AC_DATE);
        etfGWA.setChildValue("GWA_SYS_MODE", GWA_SYS_MODE);
        etfGWA.setChildValue("GWA_AP_CODE", GWA_AP_CODE);
        etfGWA.setChildValue("GWA_AP_MMO", GWA_AP_MMO);
        etfGWA.setChildValue("GWA_TR_CODE", GWA_TR_CODE);
        etfGWA.setChildValue("GWA_CHNL", GWA_CHNL);
        etfGWA.setChildValue("GWA_CANCEL_IND", GWA_CANCEL_IND);
        etfGWA.setChildValue("GWA_TERM_ID", GWA_TERM_ID);
        etfGWA.setChildValue("GWA_TL_ID", GWA_TL_ID);
        etfGWA.setChildValue("GWA_VCH_NO", GWA_VCH_NO);
        etfGWA.setChildValue("GWA_HQT_BANK", GWA_HQT_BANK);
        etfGWA.setChildValue("GWA_TR_BANK", GWA_TR_BANK);
        etfGWA.setChildValue("GWA_TR_BRANCH", GWA_TR_BRANCH);
        etfGWA.setChildValue("GWA_TR_DEP", GWA_TR_DEP);
        etfGWA.setChildValue("GWA_TR_DATE", GWA_TR_DATE);
        etfGWA.setChildValue("GWA_TR_TIME", GWA_TR_TIME);
        etfGWA.setChildValue("GWA_SUP1_ID", GWA_SUP1_ID);
        etfGWA.setChildValue("GWA_SUP2_ID", GWA_SUP2_ID);
        etfGWA.setChildValue("GWA_AUTH_LVL", GWA_AUTH_LVL);
        etfGWA.setChildValue("GWA_AUTH_RESN_TBL", GWA_AUTH_RESN_TBL);
        etfGWA.setChildValue("GWA_VCH_CNT", GWA_VCH_CNT);
        etfGWA.setChildValue("GWA_HIS_CNT", GWA_HIS_CNT);
        etfGWA.setChildValue("GWA_MSG_TYPE", GWA_MSG_TYPE);
        etfGWA.setChildValue("GWA_MSG_AP_MMO", GWA_MSG_AP_MMO);
        etfGWA.setChildValue("GWA_MSG_CODE", GWA_MSG_CODE);
        etfGWA.setChildValue("GWA_TRTT_TYPE", GWA_TRTT_TYPE);
        etfGWA.setChildValue("GWA_TRTT_CLS", GWA_TRTT_CLS);
        etfGWA.setChildValue("GWA_TRTT_AUTH_LVL", GWA_TRTT_AUTH_LVL);
        etfGWA.setChildValue("GWA_TRTT_SELF_AUTH", GWA_TRTT_SELF_AUTH);
        etfGWA.setChildValue("GWA_TRTT_LOG_JRN", GWA_TRTT_LOG_JRN);
        etfGWA.setChildValue("GWA_TRTT_SUBS_TRN_CODE", GWA_TRTT_SUBS_TRN_CODE);
        etfGWA.setChildValue("GWA_TRTT_SUBS_AP_CODE", GWA_TRTT_SUBS_AP_CODE);
        etfGWA.setChildValue("GWA_HIGHLIGHT", GWA_HIGHLIGHT);
        etfGWA.setChildValue("GWA_AML_FLG", GWA_AML_FLG);
        etfGWA.setChildValue("GWA_SWIFT_SEND_FLG", GWA_SWIFT_SEND_FLG);
        etfGWA.setChildValue("GWA_JRN_JRN_FILE", GWA_JRN_JRN_FILE);
        etfGWA.setChildValue("GWA_AIBS_CANCEL_FLG", GWA_AIBS_CANCEL_FLG);
        etfGWA.setChildValue("GWA_AIBS_OK_FLG", GWA_AIBS_OK_FLG);
        etfGWA.setChildValue("GWA_AIBS_TLR_NAME", GWA_AIBS_TLR_NAME);
        etfGWA.setChildValue("GWA_AIBS_PASSWORD", GWA_AIBS_PASSWORD);
        etfGWA.setChildValue("GWA_AIBS_WKSTN", GWA_AIBS_WKSTN);
        etfGWA.setChildValue("GWA_AIBS_USING_TAIBS_FLG",
                GWA_AIBS_USING_TAIBS_FLG);
        etfGWA.setChildValue("GWA_AIBS_IS_ALREADY_LOGON",
                GWA_AIBS_IS_ALREADY_LOGON);
        etfGWA.setChildValue("GWA_USING_AIBS_JRN_CSV1", GWA_USING_AIBS_JRN_CSV1);
        etfGWA.setChildValue("GWA_USING_AIBS_JRN_CSV2", GWA_USING_AIBS_JRN_CSV2);

        return 0;
    }

    public int UpdateGWA(HiATLParam argsMap, HiMessageContext ctx)
            throws HiException {

        HiETF etfGWA = (HiETF) ctx.getBaseSource("GWA");

        if (argsMap == null || argsMap.size() == 0) {
            throw new HiException(HiMessageCode.ERR_EXP_ARGS_SIZE);
        }
        java.util.Iterator iter = argsMap.values().iterator();
        String _id = (String) iter.next();
        String _value = (String) iter.next();
        StringTokenizer _id_token = new StringTokenizer(_id, "|");
        // StringTokenizer _v_token = new StringTokenizer(_value, "|");
        String[] _v_token = _value.split("\\|");

        int i = 0;
        while (_id_token.hasMoreTokens()) {
            etfGWA.setChildValue(_id_token.nextToken(), _v_token[i++]);
        }
        return 0;

    }

    /* 讀取文件列，并將各列放在ETF樹中 */
    public int ReadFileLineAndColum(HiATLParam argsMap, HiMessageContext ctx)
            throws HiException {
        HiMessage mess = ctx.getCurrentMsg();
        HiETF etfRoot = (HiETF) mess.getBody();
        try {
            if (argsMap == null || argsMap.size() == 0) {
                throw new HiException(HiMessageCode.ERR_EXP_ARGS_SIZE);
            }
            Logger log = HiLog.getLogger(ctx.getCurrentMsg());
            java.io.FileReader fr = null;
            java.io.BufferedReader bf = null;
            String line = null;
            java.util.Iterator i = argsMap.values().iterator();
            String _fileName = (String) i.next();
            String _filePath = (String) i.next();

            fr = new java.io.FileReader(_fileName);
            bf = new java.io.BufferedReader(fr);
            log.info("_fileName:" + _fileName + "\t _filePath" + _filePath);
            int cout = 1;
            line = bf.readLine();
            line = bf.readLine();
            while (line != null) {
                log.info(line);
                String[] column = line.split("\t");
                String _file = _filePath + "/" + column[0] + ".txt";
                if (Integer.parseInt(column[1].trim()) > 0) {
                    java.io.FileReader _fr_tmp = new java.io.FileReader(_file);
                    java.io.BufferedReader _bf_tmp = new java.io.BufferedReader(
                            _fr_tmp);
                    int _cout_tmp = 0;
                    String _line_tmp = _bf_tmp.readLine();
                    if (_line_tmp == null) {
                        etfRoot.setChildValue("MSG_TYPE", "E");
                        etfRoot.setChildValue("MSG_INFO",
                                "NO LINE FOUND IN FILE:" + _file);
                        return -1;
                    }
                    while (_line_tmp != null) {
                        _cout_tmp += 1;
                        _line_tmp = _bf_tmp.readLine();
                    }
                    log.info("_file:" + _file + "\t rows:" + _cout_tmp);
                    if (_cout_tmp + 1 - Integer.parseInt(column[1].trim()) < 0) {
                        etfRoot.setChildValue("MSG_TYPE", "E");
                        etfRoot.setChildValue("MSG_INFO", "THE DATA IN FILE:"
                                + _file + "NOT MATCH " + _fileName);
                        return -1;
                    }
                }
                line = bf.readLine();
            }
            etfRoot.setChildValue("MSG_TYPE", "N");
            etfRoot.setChildValue("MSG_INFO", "DATA IN FILES ALL MATCH!");
        } catch (Exception e) {
            etfRoot.setChildValue("MSG_TYPE", "E");
            etfRoot.setChildValue("MSG_INFO", "Exception:" + e.toString());
        }
        return 0;
    }

    public int IsAllLetter(HiATLParam argsMap, HiMessageContext ctx)
            throws HiException {
        java.util.Iterator it = argsMap.values().iterator();
        while (it.hasNext()) {
            if (!it.next().toString().matches("[a-zA-Z]*"))
                return 1;
        }
        return 0;
    }

    public int QueryCode(HiATLParam argsMap, HiMessageContext ctx)
            throws HiException {
        Logger log = HiLog.getLogger(ctx.getCurrentMsg());
        String CODE_CODE = "";
        int CODE_LENGTH = 0;
        int QUERYCODE_CODE_TOTLEN = 0;
        Map<String, String> mapTMP = null;
        HiMessage mess = ctx.getCurrentMsg();
        HiETF etfRoot = (HiETF) mess.getBody();
        String QUERYCODE_CODE = "";
        java.util.Iterator it = argsMap.values().iterator();
        String TYPE = it.next().toString();
        String CODE = "";
        if (it.hasNext()) {
            CODE = it.next().toString();
        }
        HiDataBaseUtil dbUtil = (HiDataBaseUtil) ctx.getDataBaseUtil();
        String sql = "";
        if (CODE.length() == 0 || CODE == null)
            sql = "select CODE_CD_DESC_ENG,CODE_LENGTH from STPTCODE where CODE_CD_TYPE='"
                    + TYPE + "' ORDER BY CODE_CODE,CODE_SEQ";
        else
            sql = "select CODE_CD_DESC_ENG,CODE_LENGTH from STPTCODE where CODE_CD_TYPE='"
                    + TYPE
                    + "' and CODE_CODE='"
                    + CODE
                    + "' ORDER BY CODE_CODE,CODE_SEQ";
        List list = dbUtil.execQuery(sql);
        if (list == null) {
            // //dbUtil.close();
            return -1;
        } else if (list.size() == 0) {
            // //dbUtil.close();
            return 2;
        }

        // //dbUtil.close();

        java.util.Iterator codeIt = list.iterator();
        while (codeIt.hasNext()) {
            mapTMP = (Map) codeIt.next();
            CODE_CODE = mapTMP.get("CODE_CD_DESC_ENG");
            CODE_LENGTH = Integer.parseInt(mapTMP.get("CODE_LENGTH"));
            // log.info("-------> Befor Fill Blank");
            // log.info("CODE_CODE:["+CODE_CODE+"] LENGTH:"+CODE_CODE.length());
            // log.info("CODE_LENGTH"+CODE_LENGTH);
            // log.info("QUERYCODE_CODE_TOTLEN"+QUERYCODE_CODE_TOTLEN);
            int temp = CODE_LENGTH - CODE_CODE.length();
            if (CODE_CODE.length() < CODE_LENGTH) {
                for (int j = 0; j < temp; j++) {
                    CODE_CODE = CODE_CODE + " ";
                }
            } else if (CODE_CODE.length() > CODE_LENGTH) {
                CODE_CODE = CODE_CODE.substring(0, CODE_LENGTH);
            }
            // log.info("-------> After Fill Blank");
            // log.info("CODE_CODE:["+CODE_CODE+"] LENGTH:"+CODE_CODE.length());
            // log.info("CODE_LENGTH"+CODE_LENGTH);
            // log.info("QUERYCODE_CODE_TOTLEN"+QUERYCODE_CODE_TOTLEN);
            QUERYCODE_CODE += CODE_CODE;
            QUERYCODE_CODE_TOTLEN = QUERYCODE_CODE_TOTLEN + CODE_LENGTH;
        }

        etfRoot.setChildValue("QUERYCODE_CODE", QUERYCODE_CODE);
        etfRoot.setChildValue("QUERYCODE_CODE_TOTLEN", QUERYCODE_CODE_TOTLEN
                + "");
        return 0;
    }

    /**
     * <p>
     * SENDEMAIL，返回: 0-成功 -1-失敗
     * </p>
     */
    public static int SendEmail(HiATLParam argsMap, HiMessageContext ctx)
            throws HiException {
        HiMessage msg = ctx.getCurrentMsg();
        Logger log = HiLog.getLogger(msg);
        if (log.isDebugEnabled())
            log.debug("Enter SendEmail");
        String ServerHost = HiArgUtils.getStringNotNull(argsMap, "ServerHost");
        String ServerPort = HiArgUtils.getStringNotNull(argsMap, "ServerPort");
        String Validate = HiArgUtils.getStringNotNull(argsMap, "Validate");
        String UserName = HiArgUtils.getStringNotNull(argsMap, "UserName");
        String Password = HiArgUtils.getStringNotNull(argsMap, "Password");
        String FromAddress = HiArgUtils
                .getStringNotNull(argsMap, "FromAddress");
        String ToAddress = HiArgUtils.getStringNotNull(argsMap, "ToAddress");
        String Subject = HiArgUtils.getStringNotNull(argsMap, "Subject");
        String Content = HiArgUtils.getStringNotNull(argsMap, "Content");
        MailSenderInfo mailInfo = new MailSenderInfo();
        mailInfo.setMailServerHost(ServerHost);
        mailInfo.setMailServerPort(ServerPort);
        mailInfo.setValidate(Boolean.parseBoolean(Validate));
        mailInfo.setUserName(UserName);
        mailInfo.setPassword(Password);
        mailInfo.setFromAddress(FromAddress);
        mailInfo.setToAddress(ToAddress);
        mailInfo.setSubject(Subject);
        mailInfo.setContent(Content);
        SimpleMailSender sms = new SimpleMailSender();
        boolean rtn = sms.sendTextMail(mailInfo);
        if (rtn) {
            return 0;
        }
        return -1;
    }

    /**
     * <p>
     * UploadExcel，返回: 0-上传失败 -1-解析EXCEL失敗 n - EXCEL_UPLOAD數組長度，記錄數
     * </p>
     */
    public static int UploadExcel(HiATLParam argsMap, HiMessageContext ctx)
            throws HiException {
        HiMessage msg = ctx.getCurrentMsg();
        Logger log = HiLog.getLogger(msg);
        HiETF etfRoot = (HiETF) msg.getBody();
        HiETF childTmp = null;
        if (log.isDebugEnabled())
            log.debug("Enter UploadExcel");
        String fileName = HiArgUtils.getStringNotNull(argsMap, "fileName");
        String titleLine = HiArgUtils.getStringNotNull(argsMap, "titleLine");
        File excelFile = new File(fileName);
        String[] heads = ExcelParse.parseExcel_Head(excelFile,
                Integer.parseInt(titleLine));
        String[][] conts = ExcelParse.parseExcel_Cont(excelFile,
                Integer.parseInt(titleLine));
        if (heads == null || conts == null) {
            log.info("Parse Excel Error:[heads] or [conts] is null!");
            return -1;
        }
        int rtn_group_length = 0;
        for (int i = 0; i < conts.length; i++) {
            // log.info("EXCEL_UPLOAD["+(i+1)+"]");
            etfRoot.setChildValue("EXCEL_UPLOAD_" + (i + 1), "");
            childTmp = etfRoot.getChildNode("EXCEL_UPLOAD_" + (i + 1));
            for (int j = 0; j < conts[i].length; j++) {
                // log.info("conts["+i+"]["+j+"]:"+conts[i][j]);
                if (!conts[i][j].equalsIgnoreCase("_null")) {
                    // log.info("heads["+j+"]:"+heads[j]);
                    if (!heads[j].equalsIgnoreCase("_null")) {
                        childTmp.setChildValue(heads[j].trim(), conts[i][j]);
                        rtn_group_length = i + 1;
                    }
                }
            }
        }
        log.info("[EXCEL_UPLOAD]rtn_group_length:" + rtn_group_length);
        // etfRoot.setChildValue("GrpLen",rtn_group_length);
        return rtn_group_length;
    }

    /**
     * <p>
     * ExportExcel，返回: 0-成功 2-無記錄 -1 - 失敗
     * </p>
     */
    public static int ExportExcel(HiATLParam argsMap, HiMessageContext ctx)
            throws HiException {
        HiMessage msg = ctx.getCurrentMsg();
        Logger log = HiLog.getLogger(msg);
        HiETF etfRoot = (HiETF) msg.getBody();
        HiETF childTmp = null;
        if (log.isDebugEnabled())
            log.debug("Enter ExportExcel");
        String sqlCmd = HiArgUtils.getStringNotNull(argsMap, "sqlCmd");
        String fileName = HiArgUtils.getStringNotNull(argsMap, "fileName");
        String masterTitle = (String) argsMap.get("masterTitle");
        String useTitle = (String) argsMap.get("useTitle");
        if (masterTitle.length() == 0 || masterTitle == null
                || masterTitle.equalsIgnoreCase("null")) {
            masterTitle = "noTitle";
        }
        if (useTitle.length() == 0 || useTitle == null) {
            useTitle = "true";
        }
        log.info("[sqlCmd]:" + sqlCmd + "\n" + "[fileName]:" + fileName + "\n"
                + "[masterTitle]:" + masterTitle + "\n" + "[useTitle]:"
                + useTitle);
        /* 執行數據庫腳本 */
        HiDataBaseUtil dbUtil = (HiDataBaseUtil) ctx.getDataBaseUtil();
        List list = dbUtil.execQuery(sqlCmd);
        if (list == null) {
            log.info("Query return [null]!");
            return -1;
        } else if (list.size() == 0) {
            log.info("Query return 0 records!");
            return 2;
        }
        java.util.Iterator _outputIt = list.iterator();
        java.util.Iterator outputIt_tmp = _outputIt;
        Map _outputMap = (Map) outputIt_tmp.next();
        java.util.Set _keySet = _outputMap.keySet();
        Object[] _keyArray = _keySet.toArray();
		/* test */
        log.info("map length:" + _outputMap.size());
        log.info("set length:" + _keySet.size());
        for (int i = 0; i < _keyArray.length; i++) {
            log.info(_keyArray[i].toString());
        }
		/* 輸出文件 */
        File file = new File(fileName);
        WritableWorkbook ww = null;
        WritableSheet ws = null;
        Label _cellLabel = null;
        WritableCellFormat _cellFormat = new WritableCellFormat();
        int column = 0;
        int row = 0;
        int _tmp = 0;
        try {
            _cellFormat.setAlignment(Alignment.CENTRE);
            ww = Workbook.createWorkbook(file);
            ws = ww.createSheet("sheet0", 0);
            if (!masterTitle.equalsIgnoreCase("noTitle")) {

                _cellLabel = new Label(column, row++, masterTitle, _cellFormat);
                ws.addCell(_cellLabel);
                ws.mergeCells(0, 0, _keyArray.length - 1, 0);
            }
            if (useTitle.equalsIgnoreCase("true")) {
                column = 0;
                for (int i = 0; i < _keyArray.length; i++) {
                    _cellLabel = new Label(column++, row,
                            _keyArray[i].toString());
                    ws.addCell(_cellLabel);
                }
                row++;
            }
            _outputIt = list.iterator();
            while (_outputIt.hasNext()) {
                _outputMap = (Map) _outputIt.next();
                _tmp = _outputMap.size();
                column = 0;
                for (int i = 0; i < _tmp; i++) {
                    log.info(_outputMap.get(_keyArray[i].toString()));
                    if (_outputMap.get(_keyArray[i].toString()) == null
                            || _outputMap.get(_keyArray[i].toString())
                            .toString().length() <= 0) {
                        _cellLabel = new Label(column++, row, " ");
                    } else {
                        _cellLabel = new Label(column++, row, _outputMap.get(
                                _keyArray[i]).toString());
                    }
                    ws.addCell(_cellLabel);
                }
                ++row;
            }
            ww.write();
            ww.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            log.info("Exception occured:" + e.toString());
            return -1;
        }
        return 0;
    }

    public int GetHeadItem(HiATLParam args, HiMessageContext ctx)
            throws HiException {

        HiMessage mess = ctx.getCurrentMsg();
        Logger log = HiLog.getLogger(mess);

        if (log.isDebugEnabled()) {
            log.debug("GetHeadItem process start");
        }
        String headParamName = args.get("headParamName");
        String saveParamName = args.get("saveParamName");

        HiETF etfRoot = (HiETF) mess.getBody();

        String headParamValue = mess.getHeadItem(headParamName);

        if (headParamName.equals("FID") && StringUtils.isEmpty(headParamValue)) {
            headParamValue = mess.getRequestId();
        }

        etfRoot.setChildValue(saveParamName, headParamValue);

        if (log.isDebugEnabled()) {
            log.debug("GetHeadItem process end");
        }

        return 0;

    }

    // 入参 :开始日期 args[0]（20120202）;最终日期args[1](20120203);货币种类args[2](CNY)
    // 出参 :natureDay(两个日期之间的日然日);workDay(两个日期之间的工作日);relaxDay(两个日期之间的休假日);
    public static int GetDayInfo(HiATLParam args, HiMessageContext ctx)
            throws Exception {
        HiMessage mess = ctx.getCurrentMsg();
        Logger log = HiLog.getLogger(mess);

        if (log.isDebugEnabled()) {
            log.debug("GetHeadItem process start");
        }
        String startDay = args.get("startDay");
        String endDay = args.get("endDay");
        String moneyType = args.get("moneyType");
        String dayString[] = getDayString(startDay, endDay, moneyType, ctx);
        int natureDay = dayString[0].length();
        int workDay = dayString[0].length()
                - dayString[0].replace("H", "").length();
        int relaxDay = natureDay - workDay;
        HiETF etfRoot = (HiETF) mess.getBody();
        etfRoot.setChildValue("NATUREDAY", String.valueOf(natureDay));
        etfRoot.setChildValue("WORKDAY", String.valueOf(workDay));
        etfRoot.setChildValue("RELAXDAY", String.valueOf(relaxDay));
        etfRoot.setChildValue("STARTDAYFLAG", dayString[1]);
        etfRoot.setChildValue("ENDDAYFLAG", dayString[2]);
        etfRoot.setChildValue("STARTDAYWEEK", dayString[3]);
        etfRoot.setChildValue("ENDDAYWEEK", dayString[4]);
        if (log.isDebugEnabled()) {
            log.debug("GetHeadItem process end");
        }
        return 0;

    }

    // 计算两个日期之间的自然日，工作日，休假日
    public static String[] getDayString(String startDate, String endDate,
                                        String moneyType, HiMessageContext ctx) throws Exception {
        HiDataBaseUtil dbUtil = (HiDataBaseUtil) ctx.getDataBaseUtil();
        String[] dayInfo = new String[5];
        StringBuffer buff = new StringBuffer("");
        int sYear = Integer.parseInt(startDate.substring(0, 4));
        int sMonth = Integer.parseInt(startDate.substring(4, 6));
        int sDay = Integer.parseInt(startDate.substring(6, 8));
        int eYear = Integer.parseInt(endDate.substring(0, 4));
        int eMonth = Integer.parseInt(endDate.substring(4, 6));
        int eDay = Integer.parseInt(endDate.substring(6, 8));
        if (eYear > sYear) {
            if (eYear > sYear + 1) {
                for (int i = sYear + 1; i < eYear; i++) {
                    String yaerSql = "select PCAL_MONTH_1,PCAL_MONTH_2,PCAL_MONTH_3,PCAL_MONTH_4,PCAL_MONTH_5,PCAL_MONTH_6,PCAL_MONTH_7,PCAL_MONTH_8,PCAL_MONTH_9,PCAL_MONTH_10,PCAL_MONTH_11,PCAL_MONTH_12  from STPTPCAL where PCAL_YEAR="
                            + i + " and PCAL_CCY='" + moneyType + "'";
                    // HiResultSet rs = dbUtil.execQuerySQL(yaerSql);
                    HiResultSet rs = dbUtil.execQuerySQL(yaerSql);
                    for (int j = 0; j < rs.getRecord(0).size(); j++) {
                        buff.append(rs.getRecord(0).getValue(j).trim());
                    }
                }
            }
            String sMonthCount = "";
            for (int i = sMonth; i <= 12; i++) {
                if (i == 12) {
                    sMonthCount = sMonthCount + "PCAL_MONTH_" + i;
                } else {
                    sMonthCount = sMonthCount + "PCAL_MONTH_" + i + ",";
                }
            }
            String sMonthSql = "select " + sMonthCount
                    + " from STPTPCAL where PCAL_YEAR=" + sYear
                    + " and PCAL_CCY='" + moneyType + "'";
            HiResultSet sRs = dbUtil.execQuerySQL(sMonthSql);
            for (int j = 0; j < sRs.getRecord(0).size(); j++) {
                if (j == 0) {
                    String temp = sRs.getRecord(0).getValue(j).trim();
                    buff.append(temp.substring(sDay, temp.length()));
                    dayInfo[1] = temp.substring(sDay - 1, sDay);
                } else {
                    buff.append(sRs.getRecord(0).getValue(j).trim());
                }
            }
            String eMonthCount = "";
            for (int i = 1; i <= eMonth; i++) {
                if (i == eMonth) {
                    eMonthCount = eMonthCount + "PCAL_MONTH_" + i;
                } else {
                    eMonthCount = eMonthCount + "PCAL_MONTH_" + i + ",";
                }
            }
            String eMonthSql = "select " + eMonthCount
                    + " from STPTPCAL where PCAL_YEAR=" + sYear
                    + " and PCAL_CCY='" + moneyType + "'";
            HiResultSet eRs = dbUtil.execQuerySQL(eMonthSql);
            for (int j = 0; j < eRs.getRecord(0).size(); j++) {
                if (j == eRs.getRecord(0).size() - 1) {
                    String temp = eRs.getRecord(0).getValue(j).trim();
                    buff.append(temp.substring(0, eDay - 1));
                    dayInfo[2] = temp.substring(eDay - 1, eDay);
                } else {

                    buff.append(eRs.getRecord(0).getValue(j).trim());
                }
            }
        }
        if (sYear == eYear) {
            String daySql = "";
            for (int i = sMonth; i <= eMonth; i++) {
                if (i == eMonth) {
                    daySql = daySql + "PCAL_MONTH_" + i;
                } else {
                    daySql = daySql + "PCAL_MONTH_" + i + ",";
                }
            }
            daySql = "select " + daySql + " from STPTPCAL where PCAL_YEAR="
                    + sYear + " and PCAL_CCY='" + moneyType + "'";
            HiResultSet dRs = dbUtil.execQuerySQL(daySql);
            if (eMonth > sMonth) {
                for (int j = 0; j < dRs.getRecord(0).size(); j++) {
                    if (j == dRs.getRecord(0).size() - 1) {
                        String temp = dRs.getRecord(0).getValue(j).trim();
                        buff.append(temp.substring(0, eDay - 1));
                        dayInfo[2] = temp.substring(eDay - 1, eDay);
                        continue;
                    }
                    if (j == 0) {
                        String temp = dRs.getRecord(0).getValue(j).trim();
                        buff.append(temp.substring(sDay - 1, temp.length()));
                        dayInfo[1] = temp.substring(sDay - 1, sDay);
                        continue;
                    }
                    buff.append(dRs.getRecord(0).getValue(j).trim());
                }
            }
            if (sMonth == eMonth) {
                // String daySqlMonth = "select PCAL_MONTH_" + sMonth
                // + " from STPTPCAL where PCAL_YEAR=" + sYear
                // + " and PCAL_CCY='" + moneyType + "'";
                HiResultSet dRsMonth = dbUtil.execQuerySQL(daySql);
                String temp = dRsMonth.getRecord(0).getValue(0).trim();
                buff.append(temp.substring(sDay - 1, eDay - 1));
                dayInfo[1] = temp.substring(sDay - 1, sDay);
                dayInfo[2] = temp.substring(eDay - 1, eDay);
            }
        }
        dayInfo[0] = buff.toString();
        dayInfo[3] = String.valueOf(dayForWeek(startDate));
        dayInfo[4] = String.valueOf(dayForWeek(endDate));
        for (int i = 0; i < dayInfo.length; i++) {
            System.out.println(dayInfo[i]);
        }
        return dayInfo;
    }

    // 计算星期几
    public static int dayForWeek(String pTime) throws Exception {

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");

        Calendar c = Calendar.getInstance();

        c.setTime(format.parse(pTime));

        int dayForWeek = 0;

        if (c.get(Calendar.DAY_OF_WEEK) == 1) {

            dayForWeek = 7;

        } else {

            dayForWeek = c.get(Calendar.DAY_OF_WEEK) - 1;

        }
        return dayForWeek;
    }
}