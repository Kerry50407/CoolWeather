package com.tw.coolweather.app.util;

import android.text.TextUtils;

import com.tw.coolweather.app.db.CoolWeatherDB;
import com.tw.coolweather.app.model.City;
import com.tw.coolweather.app.model.County;
import com.tw.coolweather.app.model.Province;

public class Utility {

	/**
	 * 解析和處理服務器返回的省級數據
	 */
	public synchronized static boolean handleProvincesResponse(
			CoolWeatherDB coolWeatherDB, String response) {
		if (!TextUtils.isEmpty(response)) {
			String[] allProvinces = response.split(",");
			if (allProvinces != null && allProvinces.length > 0) {
				for (String p : allProvinces) {
					String[] array = p.split("\\|");
					Province province = new Province();
					province.setProvinceCode(array[0]);
					province.setProvinceName(array[1]);
					// 將解析出來的數據儲存到Province表
					coolWeatherDB.saveProvince(province);
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * 解析和處理服務器返回的市級數據
	 */
	public static boolean handleCitiesResponse(CoolWeatherDB coolWeatherDB,
			String response, int provinceId) {
		if (!TextUtils.isEmpty(response)) {
			String[] allCities = response.split(",");
			if (allCities != null && allCities.length > 0) {
				for (String c : allCities) {
					String[] array = c.split("\\|");
					City city = new City();
					city.setCityCode(array[0]);
					city.setCityName(array[1]);
					city.setProvinceId(provinceId);
					coolWeatherDB.saveCity(city);
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * 解析和處理服務器返回的縣級數據
	 */
	public static boolean handleCountiesResponse(CoolWeatherDB coolWeatherDB,
			String response, int cityId) {
		if (!TextUtils.isEmpty(response)) {
			String[] allCounties = response.split(",");
			if (allCounties != null && allCounties.length > 0) {
				for (String c : allCounties) {
					String[] array = c.split("\\|");
					County county = new County();
					county.setCountyCode(array[0]);
					county.setCountyName(array[1]);
					county.setCityId(cityId);
					// 將解析出來的數據儲存到County表
					coolWeatherDB.saveCounty(county);
				}
			}
		}
		return false;
	}
}
