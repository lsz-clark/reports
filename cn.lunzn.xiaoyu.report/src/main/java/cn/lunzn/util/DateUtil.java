package cn.lunzn.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 管理台日期处理工具类
 * <功能详细描述>
 * 
 * @author  lunzn
 * @version  [版本号, 2017年4月5日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public abstract class DateUtil
{
    /**
     * 到天的时间格式
     */
    public static final String DATE_FORMAT_DAY_BAR = "yyyy-MM-dd";
    
    /**
     * 到秒的时间格式
     */
    public static final String DATE_FORMAT_SECOND_BAR = "yyyy-MM-dd HH:mm:ss";
    
    /**
     * 到毫秒的时间格式
     */
    public static final String DATE_FORMAT_MILLI_SECOND_BAR = "yyyy-MM-dd HH:mm:ss:SSS";
    
    /** 
     * 格式化时间(Date to String)
     * @param format 格式
     * @param value 日期
     * @return 日期字符串
     * @see [类、类#方法、类#成员]
     */
    public static String formatDateToString(String format, Date value)
    {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(value);
    }
    
    /** 
     * 格式化时间(Object to Date)
     * 
     * @param format 格式
     * @param value 日期
     * @return 日期
     * @see [类、类#方法、类#成员]
     */
    public static Date formatStringToDate(String format, String value)
    {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        
        Date date = null;
        try
        {
            date = sdf.parse(value);
        }
        catch (ParseException pe)
        {
        }
        
        return date;
    }
    
    /** 
     * 当日最早时间
     * @param date 待格式化日期
     * @return 当日最早时间
     * @see [类、类#方法、类#成员]
     */
    public static String startDate(String date)
    {
        return date + " 00:00:00";
    }
    
    /** 
     * 当日最晚时间
     * @param date 待格式化日期
     * @return 当日最晚时间
     * @see [类、类#方法、类#成员]
     */
    public static String endDate(String date)
    {
        return date + " 23:59:59";
    }
    
    /** 
     *  求几天前/几天后的日期
     * @param sDate 字符串日期
     * @param days 多少天之前
     * @return String 字符串日期
     * @see [类、类#方法、类#成员]
     */
    public static String strDiffDate(String sDate, int days)
    {
        Date date = DateUtil.formatStringToDate(DateUtil.DATE_FORMAT_DAY_BAR, sDate);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, days);
        return DateUtil.formatDateToString(DateUtil.DATE_FORMAT_DAY_BAR, calendar.getTime());
    }
}
