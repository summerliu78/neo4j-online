package com.ambergarden.samples.neo4j.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DateFormat {
	public static final String DEFAULT_FORMAT = "yyyy-MM-dd HH.mm.ss";
	private SimpleDateFormat dateFormat;
	private Locale locale;
	private static ThreadLocal<Map<Key, DateFormat>> formatMapThreadLocal = new ThreadLocal();

	private DateFormat(SimpleDateFormat dateFormat, Locale locale) {
		this.dateFormat = dateFormat;
		this.locale = locale;
	}

	public static DateFormat getInstance() {
		return getInstance("yyyy-MM-dd HH.mm.ss");
	}

	public static DateFormat getInstance(String pattern, TimeZone timeZone, Locale locale) {
		Map<Key, DateFormat> formatMap = (Map)formatMapThreadLocal.get();
		if(formatMap == null) {
			formatMap = new HashMap();
			formatMapThreadLocal.set(formatMap);
		}

		DateFormat.Key key = new DateFormat.Key(pattern, locale);
		DateFormat format = (DateFormat)((Map)formatMap).get(key);
		if(format == null) {
			format = new DateFormat(new SimpleDateFormat(pattern, locale), locale);
			((Map)formatMap).put(key, format);
		}

		format.setTimeZone(timeZone);
		return format;
	}

	public static DateFormat getInstance(String pattern) {
		return getInstance(pattern, TimeZone.getDefault(), Locale.getDefault());
	}

	public String format(Date date) {
		return this.dateFormat.format(date);
	}

	public String format(long date) {
		return this.dateFormat.format(new Date(date));
	}

	public Date parse(String source) throws ParseException {
		return this.dateFormat.parse(source);
	}

	public long parseToLong(String source) throws ParseException {
		return this.parse(source).getTime();
	}

	public String getPattern() {
		return this.dateFormat.toPattern();
	}

	public TimeZone getTimeZone() {
		return this.dateFormat.getTimeZone();
	}

	public void setTimeZone(TimeZone zone) {
		this.dateFormat.setTimeZone(zone);
	}

	public Locale getLocale() {
		return this.locale;
	}

	private static class Key {
		String pattern;
		Locale locale;

		Key(String pattern, Locale locale) {
			this.pattern = pattern;
			this.locale = locale;
		}

/*		public int hashCode() {
			int prime = true;
			int result = 1;
			int result = 31 * result + (this.locale == null?0:this.locale.hashCode());
			result = 31 * result + (this.pattern == null?0:this.pattern.hashCode());
			return result;
		}*/

		public boolean equals(Object obj) {
			if(this == obj) {
				return true;
			} else if(obj == null) {
				return false;
			} else if(this.getClass() != obj.getClass()) {
				return false;
			} else {
				DateFormat.Key other = (DateFormat.Key)obj;
				if(this.locale == null) {
					if(other.locale != null) {
						return false;
					}
				} else if(!this.locale.equals(other.locale)) {
					return false;
				}

				if(this.pattern == null) {
					if(other.pattern != null) {
						return false;
					}
				} else if(!this.pattern.equals(other.pattern)) {
					return false;
				}

				return true;
			}
		}
	}
}
