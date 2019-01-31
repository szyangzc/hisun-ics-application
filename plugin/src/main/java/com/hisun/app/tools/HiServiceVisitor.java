package com.hisun.app.tools;

import com.hisun.constants.HiConstants;
import com.hisun.constants.HiMessageCode;
import com.hisun.exception.HiException;
import com.hisun.hilog4j.HiLog;
import com.hisun.hilog4j.Logger;
import com.hisun.message.HiContext;
import com.hisun.message.HiMessage;
import com.hisun.message.HiMessageContext;
import com.hisun.register.HiBind;
import com.hisun.register.HiRegisterService;
import com.hisun.register.HiServiceObject;
import com.hisun.sm.HiStringManager;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class HiServiceVisitor {
    static HiStringManager sm = HiStringManager.getManager();

    /**
     * 得到某个对象的属性
     *
     * @param owner
     * @param fieldName
     * @return
     * @throws Exception
     */
    public Object getProperty(Object owner, String fieldName) throws Exception {
        Class ownerClassType = owner.getClass();

        Field field = ownerClassType.getField(fieldName);

        Object property = field.get(owner);

        return property;
    }

    /**
     * 得到某个类的静态属性
     *
     * @param className
     * @param fieldName
     * @return
     * @throws Exception
     */
    public Object getStaticProperty(String className, String fieldName)
            throws Exception {
        Class ownerClassType = Class.forName(className);

        Field field = ownerClassType.getField(fieldName);

        Object property = field.get(ownerClassType);

        return property;
    }

    /**
     * 执行某对象的方法
     *
     * @param owner
     * @param methodName
     * @param args
     * @return
     * @throws Exception
     */
    public Object invokeMethod(Object owner, String methodName, Object[] args)
            throws Exception {

        Class ownerClassType = owner.getClass();

        Class[] argsClass = new Class[args.length];

        for (int i = 0, j = args.length; i < j; i++) {
            argsClass[i] = args[i].getClass();
        }

        Method method = ownerClassType.getMethod(methodName, argsClass);

        return method.invoke(owner, args);
    }

    /**
     * 执行某个类的静态方法
     *
     * @param className
     * @param methodName
     * @param args
     * @return
     * @throws Exception
     */
    public Object invokeStaticMethod(String className, String methodName,
                                     Object[] args) throws Exception {
        Class ownerClass = Class.forName(className);

        Class[] argsClass = new Class[args.length];

        for (int i = 0, j = args.length; i < j; i++) {
            argsClass[i] = args[i].getClass();
        }

        Method method = ownerClass.getMethod(methodName, argsClass);

        return method.invoke(null, args);
    }

    /**
     * 新建实例
     *
     * @param className
     * @param args
     * @return
     * @throws Exception
     */
    public Object newInstance(String className, Object[] args) throws Exception {
        Class newoneClass = Class.forName(className);

        Class[] argsClass = new Class[args.length];

        for (int i = 0, j = args.length; i < j; i++) {
            argsClass[i] = args[i].getClass();
        }

        Constructor cons = newoneClass.getConstructor(argsClass);

        return cons.newInstance(args);

    }

    // 判断是否为某个类的实例
    public boolean isInstance(Object obj, Class cls) {
        return cls.isInstance(obj);
    }

    // 得到数组中的某个元素
    public Object getByArray(Object array, int index) {
        return Array.get(array, index);
    }

    /**
     * 取Service
     *
     * @param message
     * @return
     * @throws HiException
     */
    public static String getService(HiMessage message) throws HiException {
        Logger log = HiLog.getLogger(message);
        if (log.isDebugEnabled()) {
            log.debug("HiRouterOut.getService() - start");
        }

        String service = null;

        service = message.getHeadItem(HiMessage.SRN);
        String rqType = message.getHeadItem(HiMessage.REQUEST_RESPONSE);
        if (StringUtils.equals(rqType, HiMessage.TYPE_REQUEST)) {
            /**
             * 如果SRN不为空且不等于本机Region@name则直接将报文发送到S.CONSVR服务
             */
            HiContext ctx = HiContext.getCurrentContext();
            String regionName = null;
            if (ctx != null) {
                regionName = ctx.getStrProp(HiConstants.SVR_PARA_NAME_SPACE,
                        "_REGION_NAME");
            }

            service = message.getHeadItem(HiMessage.SRN);
            if (StringUtils.isNotBlank(service)
                    && StringUtils.equals(service, regionName)) {
                service = "S.CONSVR";
                return service;
            }

            service = message.getHeadItem(HiMessage.SDT);
            if (StringUtils.isNotBlank(service)) {
                return service;
            }

            service = message.getHeadItem(HiMessage.STC);
            if (StringUtils.isNotBlank(service)) {
                return service;
            }
        } else {
            service = message.getHeadItem(HiMessage.SRT);
            if (StringUtils.isNotBlank(service)) {
                return service;
            }
        }
        throw new HiException(HiMessageCode.ERR_MESSAGE_NO_ROUTER_INFO);
    }

    /**
     * 同步调用处理
     *
     * @param ctx
     * @throws HiException
     */
    public static HiMessage doSyncProcess(HiMessage message) throws HiException {
        Logger log = HiLog.getLogger(message);
        if (log.isDebugEnabled()) {
            log.debug("HiRouterOut.syncProcess() - start");
        }

        String name = getService(message);
        HiServiceObject serviceObject = HiRegisterService.getService(name);
        if (!serviceObject.isRunning()) {
            throw new HiException(HiMessageCode.ERR_SERVICE_PAUSE, name);
        }

        if (log.isDebugEnabled())
            log.debug(sm.getString("HiRouterOut.syncProcess", serviceObject));
        HiBind bind = serviceObject.getBind();
        HiMessage msg1 = null;
        HiMessageContext ctx = HiMessageContext.getCurrentMessageContext();

        String rqType = message.getHeadItem(HiMessage.REQUEST_RESPONSE);
        if (StringUtils.equals(rqType, HiMessage.TYPE_REQUEST)) {
            HiContext parent = ctx.getServerContext();
            if (parent != null) {
                if (parent.containsProperty(HiConstants.SERVERNAME)) {
                    message.addHeadItem(HiMessage.SRT,
                            parent.getStrProp(HiConstants.SERVERNAME));
                }
            }
        }

        try {
            msg1 = bind.process(message);
        } finally {
            if (msg1 != null) {
                rqType = msg1.getHeadItem(HiMessage.REQUEST_RESPONSE);
                if (StringUtils.equals(rqType, HiMessage.TYPE_RESPONSE)) {
                    msg1.delHeadItemVal(HiMessage.SRT);
                }
            }
            HiMessageContext.setCurrentMessageContext(ctx);
        }

        if (log.isDebugEnabled()) {
            log.debug("HiRouterOut.syncProcess() - end");
        }
        return msg1;
    }

    public static void main(String[] args) {
        String serverName = args[0];
        // HiStartup startup = HiStartup.getInstance(serverName);

        try {
            Class classType = Class.forName(serverName);
            Method methods[] = classType.getDeclaredMethods();
            for (int i = 0; i < methods.length; i++) {
                System.out.println(methods.toString());
            }
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
