package com.tw.coolweather.app.db;

import android.database.sqlite.SQLiteDatabase;

public class CoolWeatherDB {
	/**
	 * 資料庫名
	 */
	public static final String DB_NAME = "cool_weather";
	/**
	 * 資料庫版本
	 */
	public static final int VERSION = 1;
	
	private static CoolWeatherDB coolWeatherDB;
	
	private SQLiteDatabase db;
}
