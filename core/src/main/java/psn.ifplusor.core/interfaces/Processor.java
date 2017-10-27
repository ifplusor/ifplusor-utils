package psn.ifplusor.core.interfaces;

import java.nio.channels.FileChannel;

/**
 * @author James Yin
 * @version 8/18/17
 */
public interface Processor {
    void doProcess(FileChannel channel);
}
