package psn.ifplusor.core.interfaces;

/**
 * @author James Yin
 * @version 8/18/17
 */
public interface Monitor {

    /**
     * 处理监听的事件
     */
    void doMonitor();

    /**
     * 是否处理异常
     */
    boolean useException();

    /**
     * 处理异常捕获
     *
     * @param e 捕获的异常
     */
    void doExcept(Exception e);
}
