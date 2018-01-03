package com.ambergarden.samples.neo4j.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * @author BYX
 */
public class DateUtil {
	
	
	/**
	 * 将时间转成"yyyyMMdd"格式的字符串
	 * 
	 * @param date 时间
	 */
	public static String formatToYYYYMMDDStr(Date date) {
		if (date != null) {
			return DateFormat.getInstance("yyyyMMdd").format(date);
		} else {
			return "null";
		}
	}

	/**
	 * 将时间转成"yyyy-MM-dd"格式的字符串
	 * @param date时间
	 * @return
	 */
	public static String formatToYYYYMMDD(Date date) {
		if (date != null) {
			return DateFormat.getInstance("yyyy-MM-dd").format(date);
		} else {
			return "null";
		}
	}

	/**
	 * 将时间转成"yyyy-MM-dd"格式的字符串
	 * @param date时间
	 * @return
	 */
	public static String formatToYYYYMMDDMMHH(Date date) {
		if (date != null) {
			return DateFormat.getInstance("yyyy-MM-dd HH:mm").format(date);
		} else {
			return "null";
		}
	}



	public static Date formatToDayByYYYYMMDDA(String str)
			throws ParseException {
		DateFormat format = DateFormat.getInstance("yyyyMMdd");
		return format.parse(str);
	}

	public static Date formatToDayByYYYYMMDDMMHH(String str)
			throws ParseException {
		DateFormat format = DateFormat.getInstance("yyyy-MM-dd HH:mm");
		return format.parse(str);
	}
	
	public static Date formatToDayByYYYYMMDDMMHHSS(String str)
			throws ParseException {
		DateFormat format = DateFormat.getInstance("yyyy-MM-dd HH:mm:ss");
		return format.parse(str);
	}
	
	public static Date formatToDayByYYYYMMDD(String str)
			throws ParseException {
		DateFormat format = DateFormat.getInstance("yyyy-MM-dd");
		return format.parse(str);
	}

	public static String formatToYYYYMMDDMMHHSS(Date date) {
		DateFormat format = DateFormat.getInstance("yyyy-MM-dd HH:mm:ss");
		return format.format(date);
	}

	
	public static String formatToyyyyMMddHHmmss(Date date) {
		DateFormat format = DateFormat.getInstance("yyyyMMddHHmmss");
		return format.format(date);
	}
	
	public static Date formatToyyyyMMddHHmmss(String str) throws ParseException{
		DateFormat format = DateFormat.getInstance("yyyyMMddHHmmss");
		return format.parse(str);
	}

	/**
	 * 两日期相差天数
	 * @param smdate
	 * @param bdate
	 * @return
	 * @throws ParseException
	 */
	public static int daysBetween(Date smdate,Date bdate) throws ParseException     
    {    
		SimpleDateFormat sdf  =new SimpleDateFormat("yyyy-MM-dd");
        smdate=sdf.parse(sdf.format(smdate));  
        bdate=sdf.parse(sdf.format(bdate));  
        Calendar cal = Calendar.getInstance();    
        cal.setTime(smdate);    
        long time1 = cal.getTimeInMillis();                 
        cal.setTime(bdate);    
        long time2 = cal.getTimeInMillis(); 
        long between_days=(time2-time1)/(1000*3600*24);  
       return Integer.parseInt(String.valueOf(between_days));           
    }
	/**
	 * 两日期相差月数 
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public static int getMonth(Date startDate, Date endDate) {
        long monthday;
        try {
        	SimpleDateFormat sdf  =new SimpleDateFormat("yyyy-MM-dd");
        	startDate=sdf.parse(sdf.format(startDate));  
        	endDate=sdf.parse(sdf.format(endDate));

            Calendar starCal = Calendar.getInstance();
            starCal.setTime(startDate);

            int sYear = starCal.get(Calendar.YEAR);
            int sMonth = starCal.get(Calendar.MONTH);
            int sDay = starCal.get(Calendar.DATE);

            Calendar endCal = Calendar.getInstance();
            endCal.setTime(endDate);
            int eYear = endCal.get(Calendar.YEAR);
            int eMonth = endCal.get(Calendar.MONTH);
            int eDay = endCal.get(Calendar.DATE);

            monthday = ((eYear - sYear) * 12 + (eMonth - sMonth));

            if (sDay < eDay) {
                monthday = monthday + 1;
            }
            return Integer.parseInt(String.valueOf(monthday));
        } catch (ParseException e) {
            monthday = 0;
        }
        return Integer.parseInt(String.valueOf(monthday));
    }
	
	/***
	 * 
	 * @param startDate 开始日期
	 * @param phaseNumber 期数
	 * @param  type  2月，5天  12表示分钟
	 * @return
	 * @throws ParseException 
	 */
    public static Date getMonthlyRepayDate(Date startDate, Integer phaseNumber,Integer type) throws ParseException
    {
   /* 	SimpleDateFormat sdf  =new SimpleDateFormat("yyyy-MM-dd");
    	startDate=sdf.parse(sdf.format(startDate));  */
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.add(type, phaseNumber);
        return calendar.getTime();
    }
    
    /**
     * 获取当天的起始日期
     *
     * @return
     * @author 董小满
     * @since 2014-05-19
     */
    public static Long getStartTime() {

        Calendar todayStart = Calendar.getInstance();
        todayStart.set(Calendar.HOUR, 0);
        todayStart.set(Calendar.MINUTE, 0);
        todayStart.set(Calendar.SECOND, 0);
        todayStart.set(Calendar.MILLISECOND, 0);
        return todayStart.getTime().getTime();
    }

    /**
     * 获取当天的结束日期
     *
     * @return
     * @author 董小满
     * @since 2014-05-19
     */
    public static Long getEndTime() {
        Calendar todayEnd = Calendar.getInstance();
        todayEnd.set(Calendar.HOUR, 23);
        todayEnd.set(Calendar.MINUTE, 59);
        todayEnd.set(Calendar.SECOND, 59);
        todayEnd.set(Calendar.MILLISECOND, 999);
        return todayEnd.getTime().getTime();
    }
    

    /**
     * 获取某月前X月的第一天
     *@param dateStr 初始时间
     *@param monthNum 与dateStr相隔的月数
     * @return
     */
    public static Date getFirstDayOfMonth(String dateStr,int monthNum) 
    { 
    	Date date=null;
    	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    	try {
			date = format.parse(dateStr);
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
	    try {
	    	 Calendar calendar = Calendar.getInstance();  
	    	 calendar.setTime(date);
	    	 int month = calendar.get(Calendar.MONTH);
	    	 calendar.set(Calendar.MONTH, month+monthNum);
	    	 calendar.set(Calendar.DAY_OF_MONTH,calendar.getMinimum(Calendar.DAY_OF_MONTH));  
	    	 Date strDateTo = calendar.getTime();  
	    	 return strDateTo;
		} catch (Exception e) {
			e.printStackTrace();
		}
	    return date;
    }
    
    /**
     * 获取某月前X月的最后一天
     *@param dateStr 初始时间
     *@param monthNum 与dateStr相隔的月数
     * @return
     */
    public static Date getLastDayOfMonth(String dateStr,int monthNum) 
    { 
    	Date date=null;
    	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    	try {
			date = format.parse(dateStr);
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
	    try {
	    	 Calendar calendar = Calendar.getInstance();
	    	 calendar.setTime(date);
	    	 int month = calendar.get(Calendar.MONTH);
	    	 calendar.set(Calendar.MONTH, month+monthNum);
	    	 calendar.set(Calendar.DAY_OF_MONTH,calendar.getActualMaximum(Calendar.DAY_OF_MONTH));  
	    	 Date strDateTo = calendar.getTime();  
	    	 return strDateTo;
		} catch (Exception e) {
			e.printStackTrace();
		}
	    return date;
    }
    
    /**
     * 用于天数比较（常用）
     *
     * @param smdate
     * @param bdate
     * @return
     * @throws ParseException
     */
    public static int daysBetweenInt(Date smdate, Date bdate) throws ParseException {
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        smdate = sdf.parse(sdf.format(smdate));
        bdate = sdf.parse(sdf.format(bdate));
        Calendar cal = Calendar.getInstance();
        cal.setTime(smdate);
        long time1 = cal.getTimeInMillis();
        cal.setTime(bdate);
        long time2 = cal.getTimeInMillis();
        long between_days = (time2 - time1) / (1000 * 3600 * 24);
        return Integer.parseInt(String.valueOf(between_days));
    }
    
    /**
     * 
     *计算smdate time天之后的日期
     * @param smdate
     * @param time
     * @return
     * @throws ParseException
     */
    /**
     * 计算smdate之后的日期
     * @param smdate
     * @param year 年
     * @param month 月
     * @param day 日
     * @return
     * @throws ParseException
     */
    public static Date getSmdateDaysLater(Date smdate,Integer year,Integer month, Integer day) throws ParseException {
    	Calendar cal = new GregorianCalendar();  
        cal.setTime(smdate);  
        int oldYear = cal.get(Calendar.YEAR);  
        int oldMonth = cal.get(Calendar.MONTH);  
        int oldDay = cal.get(Calendar.DAY_OF_MONTH);  
        int newDay = oldDay + day;  
        int newMonth = oldMonth + month;  
        int newYear = oldYear + year;  
        cal.set(Calendar.YEAR, newYear);  
        cal.set(Calendar.MONTH, newMonth);  
        cal.set(Calendar.DAY_OF_MONTH, newDay);  
        Date effectiveDate = new Date(cal.getTimeInMillis());
        return effectiveDate;
    }

	public static Date getEffectiveDate(Date currentDate,int timeout){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(currentDate);
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.add(Calendar.DATE,timeout);
		return calendar.getTime();
	}
  /*  public static void main(String[] args) {
   	 try {
   		java.text.DateFormat format2 = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			//展示日期不能大于上线日期
		 Date notifyTime_=format2.parse("2016-03-24 13:00:00");
		 System.out.println(format2.format(DateUtil.getMonthlyRepayDate(notifyTime_, 1, 12)));
	} catch (ParseException e) {
		e.printStackTrace();
	}
	}*/
}
