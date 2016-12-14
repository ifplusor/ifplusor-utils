package psn.ifplusor.core.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 日期工具类
 */
public class DateUtil {

    private static final Logger logger = LoggerFactory.getLogger(DateUtil.class);

    public static final long DAYMILLISEC = 24 * 60 * 60 * 1000;	// the number of millisecond for one day
    public static final long HOURMILLISEC = 60 * 60 * 1000;

    /*
        SimpleDateFormat函数语法：

         G - 年代标志符
         y - 年
         M - 月
         d - 日
         h - 时, 在上午或下午 (1~12)
         H - 时, 在一天中 (0~23)
         m - 分
         s - 秒
         S - 毫秒
         E - 星期
         D - 一年中的第几天
         F - 一月中第'几'个星期几
         w - 一年中第几个星期
         W - 一月中第几个星期
         a - 上午 / 下午 标记符
         k - 时 在一天中 (1~24)
         K - 时 在上午或下午 (0~11)
         z - 时区
     */

    /**
     * 为减小创建SimpleDateFormat的开销，设立缓存。 (Map的key为日期转换格式)
     */
    private static final ThreadLocal<Map<String, SimpleDateFormat>> cache = new ThreadLocal<Map<String, SimpleDateFormat>>();

    private static SimpleDateFormat getDateFormat(String format) {
        Map<String, SimpleDateFormat> map = cache.get();
        if (map == null) {
            map = new HashMap<String, SimpleDateFormat>();
            cache.set(map);
        }
        SimpleDateFormat dateFormat = map.get(format);
        if (dateFormat == null) {
            dateFormat = new SimpleDateFormat(format);
            map.put(format, dateFormat);
        }
        return dateFormat;
    }

    /**
     * @param date   日期字符串
     * @param format 日期解析格式
     * @return 转换后的日期类
     * @throws Exception 如果参数错误或解析错误，抛出此异常
     */
    public static Date parse(final String date, final String format)
            throws Exception {
        if (date == null) {
            throw new Exception("参数date不能为空！");
        }
        if (format == null) {
            throw new Exception("参数format不能为空！");
        }

        try {
            return getDateFormat(format).parse(date);
        } catch (final ParseException e) {
            throw new Exception("日期格式解析错误。日期字符串：" + date + "；日期解析格式：" + format);
        }
    }

    /**
     * @param format "example:yyyy/MM/dd"
     * @throws Exception 如果参数错误或解析错误，抛出此异常
     */
    public static String format(final Date date, final String format)
            throws Exception {
        if (date == null) {
            throw new Exception("参数date不能为空！");
        }
        if (format == null) {
            throw new Exception("参数format不能为空！");
        }

        return getDateFormat(format).format(date);
    }

    public static Date parseYYYYMMDD(final String strDate) throws ParseException {
        if (strDate == null || strDate.equals("")) {
            if (logger.isDebugEnabled()) {
                logger.debug("content is null or empty.");
            }
            throw new ParseException("content is null or empty.", 0);
        }

        return getDateFormat("yyyyMMdd").parse(strDate);
    }

    public static String formatYYYYMMDD(final Date date) {
        if (date == null) {
            return "";
        }

        return getDateFormat("yyyyMMdd").format(date);
    }
}
