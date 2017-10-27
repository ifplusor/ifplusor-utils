package psn.ifplusor.core.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author James Yin
 * @version 8/18/17
 */
public class ErrorUtil {

    private ErrorUtil() {}

    public static String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter(1000);
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        pw.close();
        return sw.toString();
    }
}
