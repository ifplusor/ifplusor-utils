package psn.ifplusor.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import psn.ifplusor.core.interfaces.Monitor;

import java.util.List;

/**
 * @author james
 * @version 11/18/16
 */
public class ThreadUtil {

    private static final Logger logger = LoggerFactory.getLogger(ThreadUtil.class);

    private ThreadUtil() {}

    public static void wait(List<Thread> lstThreads, long interval, String name, Monitor monitor) {

        if (lstThreads.size() == 0) {
            return;
        }

        while (true) {
            boolean bRunning = false;
            int nRunning = 0;

            for (Thread t : lstThreads) {
                if (t.isAlive()) {
                    bRunning = true;
                    nRunning++;
                }
            }

            if (!bRunning) {
                break;
            }

            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("waiting, <" + name + "> 线程运行中:" + nRunning);
                }

                // 执行状态监控
                if (monitor != null) {
                    monitor.doMonitor();
                }

                Thread.sleep(interval);
            } catch (Exception e) {
                if (monitor != null && monitor.useException()) {
                    monitor.doExcept(e);
                } else {
                    logger.error(ErrorUtil.getStackTrace(e));
                }
            }

        }
    }
}
