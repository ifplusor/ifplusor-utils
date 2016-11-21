package psn.ifplusor.core.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author james
 * @version 11/18/16
 */
public class ThreadUtil {

    private static final Logger logger = LoggerFactory.getLogger(ThreadUtil.class);

    public interface Monitor {
        void doMonitor();
    }

    public static void wait(List<Thread> lstThreads, long interval, String name, Monitor monitor) {

        if (lstThreads.size() == 0) {
            return;
        }

        while (true) {
            boolean b运行中 = false;
            int n运行中 = 0;
            for (Thread t : lstThreads) {
                if (t.isAlive()) {
                    b运行中 = true;
                    n运行中++;
                }
            }

            if (!b运行中) {
                break;
            }
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("waiting, <" + name + "> 线程运行中:" + n运行中);
                }

                // 执行状态监控
                if (monitor != null) {
                    monitor.doMonitor();
                }

                Thread.sleep(interval);
            } catch (Exception e) {
                // TODO: handle exception
                logger.error(e.getMessage());
            }

        }
    }
}
