package com.qwe.anna.widget;

public class ParseTimeUtil {
	public static String format(long s) {
		int mi =  60;
		int hh = mi * 60;
		int dd = hh * 24;
		long day = s / dd;
		long hour = (s - day * dd) / hh;
		long minute = (s - day * dd - hour * hh) / mi;
		long second = (s - day * dd - hour * hh - minute * mi);
		String strMinute = minute < 10 ? "0" + minute : "" + minute;
		String strSecond = second < 10 ? "0" + second : "" + second;
		return strMinute + ":" + strSecond;
		}
}
