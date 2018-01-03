package com.ambergarden.samples.neo4j.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * 工具类
 * Created by yangchen on 16/10/27.
 */
public class RuleUtil {

	private static final Log log = LogFactory.getLog(RuleUtil.class);

	private static final List<String> MUNICIPALITY = Arrays.asList("北京", "上海", "天津", "重庆");

	/**
	 * 根据身份证号获取城市评分
	 *
	 * @param idCardNo
	 * @return
	 */
	public static Integer getCityPoint(String idCardNo) {
		if (idCardNo.startsWith("11")// 北京
				|| idCardNo.startsWith("4401")// 广州
				|| idCardNo.startsWith("31")// 上海
				|| idCardNo.startsWith("4403")) {// 深圳
			return 12;
		} else if (idCardNo.startsWith("12")// 天津
				|| idCardNo.startsWith("3301")// 杭州
				|| idCardNo.startsWith("3201")// 南京
				|| idCardNo.startsWith("3701")// 济南
				|| idCardNo.startsWith("5102")// 重庆
				|| idCardNo.startsWith("3702")// 青岛
				|| idCardNo.startsWith("2102")// 大连
				|| idCardNo.startsWith("3302")// 宁波
				|| idCardNo.startsWith("3502")// 厦门
				|| idCardNo.startsWith("5101")// 成都
				|| idCardNo.startsWith("4201")// 武汉
				|| idCardNo.startsWith("2301")// 哈尔滨
				|| idCardNo.startsWith("2101")// 沈阳
				|| idCardNo.startsWith("6101")// 西安
				|| idCardNo.startsWith("2201")// 长春
				|| idCardNo.startsWith("4301")// 长沙
				|| idCardNo.startsWith("4101")// 郑州
				|| idCardNo.startsWith("1301")// 石家庄
				|| idCardNo.startsWith("3205")// 苏州
				|| idCardNo.startsWith("4406")// 佛山
				|| idCardNo.startsWith("4419")// 东莞
				|| idCardNo.startsWith("3202")// 无锡
				|| idCardNo.startsWith("3706")// 烟台
				|| idCardNo.startsWith("1401")// 太原
				) {
			return 6;
		} else if (idCardNo.startsWith("3401")// 合肥
				|| idCardNo.startsWith("3601")// 南昌
				|| idCardNo.startsWith("4501")// 南宁
				|| idCardNo.startsWith("5301")// 昆明
				|| idCardNo.startsWith("3303")// 温州
				|| idCardNo.startsWith("3703")// 淄博
				|| idCardNo.startsWith("1302")// 唐山
				) {
			return 3;
		} else {
			return -1;
		}
	}

	/**
	 * 根据身份证号,获取年龄
	 *
	 * @param idCardNo
	 * @return
	 */
/*	public static Integer getAge(String idCardNo) {
		String birth = idCardNo.substring(6, 14);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

		try {
			Date date = sdf.parse(birth);
			return DateUtils.getAge(date);
		} catch (Exception e) {
		//	logger.error(RuleConstant.LoggerModuleCode, e);
			logger.error("异常", e);
			return -1;
		}
	}*/

	/**
	 * 与紧急联系人的第一次联系时间的评分规则
	 *
	 * @param firstCallTime
	 * @return
	 */
	public static int getFirstUrgentCallScore(Date firstCallTime) {
		if (firstCallTime == null) {
			return 0;
		}
		long n = new Date().getTime();

		long month = (n - firstCallTime.getTime())
				/ (1000L * 60 * 60 * 24 * 30);
		if (month >= 5) {
			return 10;
		} else if (month >= 3) {
			return 6;
		} else if (month >= 1) {
			return 2;
		} else {
			return 0;
		}
	}

	/**
	 * 判断某个时间是否在三个月内
	 *
	 * @param date
	 * @return
	 */
	public static boolean isIn3Month(Date date,Date baseDate) {
		long n = baseDate.getTime();
		long month = (n - date.getTime())
				/ (1000L * 60 * 60 * 24 * 30);
		if (month >= 3) {
			return false;
		} else {
			return true;
		}
	}




	/**
	 * 判断某个时间是多少天内
	 *
	 * @param date  时间点
	 * @param baseDate  相对时间点
	 * @param range 多少天内
	 * @return
	 */
	public static boolean isInDays(Date date,Date baseDate, Integer range) {
		long n = baseDate.getTime();

		long before = n - 1000L * 60 * 60 * 24 * range;

		long d = date.getTime();

		if (d < n && d >= before) {
			return true;
		} else {
			return false;
		}
	}


	/**
	 * 手机号开通时间到现在的天数
	 *
	 * @param openDate
	 * @return
	 */
	public static long getBetweenDays(Date openDate) {
		long o = openDate.getTime();
		long n = new Date().getTime();
		return (n - o) / (1000 * 60 * 60 * 24);
	}

	/**
	 * 根据18位身份证号返回15位身份证号
	 * @param str_18
	 * @return
	 */
	public static String get15IDNoBy18(String str_18){
		if(str_18.length() != 18) return str_18;
		String str_15 = "";
		str_15 += str_18.substring(0,6);
		str_15 += str_18.substring(8,17);
		return str_15;
	}


	/**
	 * 手机号开通时间到现在的天数
	 *
	 * @param openDate
	 * @return
	 */
	public static long getBetweenDays(Date openDate,Date crawTime) {
		long o = openDate.getTime();
		long n = crawTime.getTime();
		return (n - o) / (1000 * 60 * 60 * 24);
	}

/*

	public static String getRiskValueByOgnlRules(Map map, Map<String, Integer> config, ObjectHolder objectHolder) {
		for (String section : config.keySet()) {
			try {
				if (Ognl.getValue(section, map).equals(true)) {

					return config.get(section).toString();
				}
			} catch (OgnlException e) {
				throw new RuntimeException(e);
			}
		}
		return "0";
	}*/

	public static boolean compareHome(String home1, String home2){
		String regex = "[省|市|区|县|旗|盟|,]";
		String[] home1s = home1.split(regex);
		String[] home2s = home2.split(regex);
		int min = Math.min(home1s.length, home2s.length);
		if(home1s.length==home2s.length){
			if (home1.length() > home2.length()) {
				if (home1sInHom2s(home2s, home1s)) return true;
			}else if (home2.length() > home1.length()) {
				if (home1sInHom2s(home1s, home2s)) return true;
			}else{
				return home1.equals(home2);
			}
		}
		if (home1s.length == min) {
			if (home1sInHom2s(home1s, home2s)) return true;
		}
		if (home2s.length == min) {
			if (home1sInHom2s(home2s, home1s)) return true;
		}
		return false;
	}

	private static boolean home1sInHom2s(String[] home1s, String[] home2s) {
		for (String s : home1s) {
			boolean hashMatch=false;
			if(s.length()==0) continue;
			for (String home21 : home2s) {
				if(home21.length()==0) continue;
				if(s.equals(home21) || home21.contains(s)){
					hashMatch=true;
					continue;
				}
			}
			if (!hashMatch) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 判断是否为直辖市
	 * @param key
	 * @return
	 *//*
	public static boolean isMunicipality(String key) {
		return MUNICIPALITY.contains(key);
	}
	*//**
	 * 查询用户在某一时间时的年龄
	 * @param string
	 * @param createTime
	 *//*
	public static Integer getAge(String idCardNo, Date createTime) {
		String birth = idCardNo.substring(6, 14);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

		try {
			Date date = sdf.parse(birth);
			return getOneDayAge(date, createTime);
		} catch (Exception e) {
			logger.error("异常", e);
			return -1;
		}
	}
*/
	/**
	 * 获取法定年龄
	 * @param birthDay 生日
	 * @return
	 */
	public static final int getOneDayAge(Date birthDay,Date oneDay) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(birthDay);
		Calendar now = Calendar.getInstance();
		now.setTime(oneDay);
		int age = now.get(Calendar.YEAR) - calendar.get(Calendar.YEAR);
		calendar.set(Calendar.YEAR,now.get(Calendar.YEAR));
		if(calendar.compareTo(now)==1){
			return age-1;
		}
		return age;
	}

	public static final int getAgeByYear(String idCardNo,Date oneDay) {
		String birth = idCardNo.substring(6, 14);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		Date date = null;
		try {
			date = sdf.parse(birth);
		} catch (Exception e) {

			return -1;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		Calendar now = Calendar.getInstance();
		now.setTime(oneDay);
		int age = now.get(Calendar.YEAR) - calendar.get(Calendar.YEAR);
		calendar.set(Calendar.YEAR,now.get(Calendar.YEAR));
		return age;
	}

//	public static void main(String[] args) {
//		int age = RuleUtil.getAgeByYear("421182201511252657", new Date());
//		System.out.println(age);
//	}
}
