package com.hisun.app.handlers;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import com.hisun.constants.HiConstants;
import com.hisun.constants.HiMessageCode;
import com.hisun.exception.HiException;
import com.hisun.framework.event.IServerDestroyListener;
import com.hisun.framework.event.IServerInitListener;
import com.hisun.framework.event.IServerStartListener;
import com.hisun.framework.event.IServerStopListener;
import com.hisun.framework.event.ServerEvent;
import com.hisun.hilog4j.Logger;
import com.hisun.message.HiContext;
import com.hisun.message.HiMessage;
import com.hisun.message.HiMessageContext;
import com.hisun.pubinterface.IHandler;


/**
 * 
 * @author yzc
 * @since 2013.5.30
 */
public class HiFlowCtrl implements IHandler, IServerInitListener,
		IServerStartListener, IServerStopListener, IServerDestroyListener {
	public static Logger log = (Logger) HiContext.getCurrentContext()
			.getProperty(HiConstants.SERVERLOG);

	private int semNum; // 信号量数目

	private long tmOut = -1;

	private static Semaphore semaphore = null; // 信号灯

	public void serverDestroy(ServerEvent arg0) throws HiException {
		// TODO Auto-generated method stub
		// semaphore.release(semNum);
	}

	public void serverStop(ServerEvent arg0) throws HiException {
		// TODO Auto-generated method stub
		semaphore.release(semNum);
		log.info("HiFlowCtrl Server released semaphore.");
	}

	public void serverStart(ServerEvent arg0) throws HiException {
		// TODO Auto-generated method stub
		log.info("HiFlowCtrl Server start.");
	}

	public void serverInit(ServerEvent arg0) throws HiException {
		// TODO Auto-generated method stub
		semaphore = new Semaphore(semNum);
		log.info("HiFlowCtrl Server init semaphore number", semNum);
	}

	public void process(HiMessageContext msg) throws HiException {
		// TODO Auto-generated method stub

	}

	public void release(HiMessageContext msg) throws HiException {
		// TODO Auto-generated method stub

		semaphore.release();
		HiMessage currentMsg = msg.getCurrentMsg();
		String reqID = currentMsg.getRequestId();
		if (log.isInfoEnabled()) {
			log.info("[" + reqID + "]", "released a semaphore.");
		}
	}

	public void acquire(HiMessageContext msg) throws HiException {
		// TODO Auto-generated method stub
		HiMessage currentMsg = msg.getCurrentMsg();
		String reqID = currentMsg.getRequestId();
		if (semaphore.availablePermits() > 0) {
			if (log.isInfoEnabled()) {
				log.info("available permists:" + semaphore.availablePermits());
				log.info("[" + reqID + "]", "has available permist.");
				log.info(currentMsg.getHead());
			}
		} else {
			if (log.isInfoEnabled()) {
				log.info("[" + reqID + "]",
						" waiting for other thread release semaphore.");
			}

		}
		try {
			if (tmOut == -1)
				semaphore.acquire();
			else {
				if (!semaphore.tryAcquire(tmOut, TimeUnit.SECONDS)) {
					log.error("[" + reqID + "]","acquire semaphore timout.");
					throw new HiException(HiMessageCode.ERR_TIME_OUT,
							"acquire semaphore.");
				}
			}
			semaphore.getQueueLength();
			if (log.isInfoEnabled()) {
				log.info("[", reqID, "]", "acquired a semphore.");
				log.info("current threads counts:", semaphore.getQueueLength());
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public int getSemNum() {
		return semNum;
	}

	public void setSemNum(int semNum) {
		this.semNum = semNum;
	}

	public long getTmOut() {
		return tmOut;
	}

	public void setTmOut(long tmOut) {
		this.tmOut = tmOut;
	}

}