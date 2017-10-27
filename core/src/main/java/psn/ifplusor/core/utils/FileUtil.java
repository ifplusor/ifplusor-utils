package psn.ifplusor.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import psn.ifplusor.core.interfaces.Processor;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.Charset;

/**
 * @author james
 * @version 12/15/16
 */
public class FileUtil {

    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

    private FileUtil() {}

    public static String removeEndSeparator(String path) {
        if (path.endsWith(File.separator)) {
            return path.substring(0, path.length() - File.separator.length());
        }
        return path;
    }

    public static long copy(String inFilePath, String outFilePath)
            throws FileNotFoundException, IOException {
        File inFile = new File(inFilePath);
        File outFile = new File(outFilePath);
        return copy(inFile, outFile);
    }

    /**
     * @throws FileNotFoundException 找不到文件
     */
    public static long copy(File inFile, File outFile) throws IOException {
        FileChannel inChannel = null;
        FileChannel outChannel = null;
        try {
            inChannel = new FileInputStream(inFile).getChannel();
            outChannel = new FileOutputStream(outFile).getChannel();

            return copy(inChannel, outChannel);
        } finally {
            if (inChannel != null) {
                try {
                    inChannel.close();
                } catch (IOException e) {
                    logger.error(ErrorUtil.getStackTrace(e));
                }
            }

            if (outChannel != null) {
                try {
                    outChannel.close();
                } catch (IOException e) {
                    logger.error(ErrorUtil.getStackTrace(e));
                }
            }
        }
    }

    public static long copy(FileChannel inChannel, FileChannel outChannel) throws IOException {
        return inChannel.transferTo(0, inChannel.size(), outChannel);
    }

    /**
     * @throws FileNotFoundException 找不到文件
     */
    public static long copyWithLock(String inFilePath, String outFilePath) throws IOException {
        File inFile = new File(inFilePath);
        File outFile = new File(outFilePath);
        return copyWithLock(inFile, outFile);
    }

    /**
     * @throws FileNotFoundException 找不到文件
     */
    public static long copyWithLock(File inFile, File outFile) throws IOException {
        FileChannel inChannel = null;
        FileChannel outChannel = null;
        try {
            inChannel = new FileInputStream(inFile).getChannel();
            outChannel = new FileOutputStream(outFile).getChannel();

            return copyWithLock(inChannel, outChannel);
        } finally {
            if (inChannel != null) {
                try {
                    inChannel.close();
                } catch (IOException e) {
                    logger.error(ErrorUtil.getStackTrace(e));
                }
            }

            if (outChannel != null) {
                try {
                    outChannel.close();
                } catch (IOException e) {
                    logger.error(ErrorUtil.getStackTrace(e));
                }
            }
        }
    }

    public static long copyWithLock(FileChannel inChannel, FileChannel outChannel) throws IOException {
        FileLock lock = null;
        try {
            // 测试锁
            lock = outChannel.lock();
            return copy(inChannel, outChannel);
        } finally {
            if (lock != null) {
                try {
                    lock.release();
                } catch (IOException e) {
                    logger.error(ErrorUtil.getStackTrace(e));
                }
            }
        }
    }

    public static boolean tryLock(FileChannel channel, Processor processor) throws IOException {
        return tryLock(channel, 0L, 9223372036854775807L, false, processor);
    }

    public static boolean tryLock(FileChannel channel, long position, long size, boolean shared,
                                  Processor processor) throws IOException {
        FileLock lock = null;
        try {
            // 测试锁
            lock = channel.tryLock(position, size, shared);
            if (lock != null) {
                if (processor != null) {
                    processor.doProcess(channel);
                }
                return true;
            } else {
                return false;
            }
        } finally {
            if (lock != null) {
                try {
                    lock.release();
                } catch (IOException e) {
                    logger.error(ErrorUtil.getStackTrace(e));
                }
            }
        }
    }

    public static boolean lock(FileChannel channel, Processor processor) throws IOException {
        return lock(channel, 0L, 9223372036854775807L, false, processor);
    }

    public static boolean lock(FileChannel channel, long position, long size, boolean shared,
                               Processor processor) throws IOException {
        FileLock lock = null;
        try {
            // 测试锁
            lock = channel.lock(position, size, shared);
            if (lock != null) {
                if (processor != null) {
                    processor.doProcess(channel);
                }
                return true;
            } else {
                return false;
            }
        } finally {
            if (lock != null) {
                try {
                    lock.release();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String read(InputStream is, Charset charset) {
        try {
            InputStreamReader isr = new InputStreamReader(is, charset);
            StringBuilder sbread = new StringBuilder();
            while (isr.ready()) {
                sbread.append((char) isr.read());
            }
            isr.close();
            return sbread.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
