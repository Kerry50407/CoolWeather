package com.tw.coolweather.app.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.coolweather.R;
import com.tw.coolweather.app.db.CoolWeatherDB;
import com.tw.coolweather.app.model.City;
import com.tw.coolweather.app.model.County;
import com.tw.coolweather.app.model.Province;
import com.tw.coolweather.app.util.HttpCallbackListener;
import com.tw.coolweather.app.util.HttpUtil;
import com.tw.coolweather.app.util.Utility;

public class ChooseAreaActivity extends Activity {
	public static final int LEVEL_PROVINCE = 0;
	public static final int LEVEL_CITY = 1;
	public static final int LEVEL_COUNTY = 2;

	private ProgressDialog progressDialog;
	private TextView titleText;
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private CoolWeatherDB coolWeatherDB;
	private List<String> dataList = new ArrayList<String>();

	/**
	 * 省列表
	 */
	private List<Province> provinceList;
	/**
	 * 市列表
	 */
	private List<City> cityList;
	/**
	 * 縣列表
	 */
	private List<County> countyList;
	/**
	 * 選中的省份
	 */
	private Province selectedProvince;
	/**
	 * 選中的城市
	 */
	private City selectedCity;
	/**
	 * 當前選中的級別
	 */
	private int currentLevel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);

		listView = (ListView) findViewById(R.id.list_view);
		titleText = (TextView) findViewById(R.id.title_text);

		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, dataList);
		listView.setAdapter(adapter);

		coolWeatherDB = CoolWeatherDB.getInstance(this);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (currentLevel == LEVEL_PROVINCE) {
					selectedProvince = provinceList.get(position);
					queryCities();
				} else if (currentLevel == LEVEL_CITY) {
					selectedCity = cityList.get(position);
					queryCounties();
				}
			}
		});
		queryProvinces();
	}

	/**
	 * 查詢全國所有的省,優先從數據庫查詢,如果沒有查詢到再去服務器上查詢
	 */
	private void queryProvinces() {
		provinceList = coolWeatherDB.loadProvinces();
		if (provinceList.size() > 0) {
			dataList.clear();
			for (Province province : provinceList) {
				dataList.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			currentLevel = LEVEL_PROVINCE;
		} else {
			queryFromServer(null, "province");
		}

	}

	/**
	 * 查詢選中省內所有的市,優先從資料庫查詢,如果沒有查詢到再去服務器上查詢
	 */
	private void queryCities() {
		cityList = coolWeatherDB.loadCities(selectedProvince.getId());
		if (cityList.size() > 0) {
			dataList.clear();
			for (City city : cityList) {
				dataList.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel = LEVEL_CITY;
		} else {
			queryFromServer(selectedProvince.getProvinceCode(), "city");
		}
	}

	/**
	 * 查詢選中市內所有的縣,優先從資料庫查詢,如果沒有查詢到再去服務器上查詢
	 */
	private void queryCounties() {
		countyList = coolWeatherDB.loadCounties(selectedCity.getId());
		if (countyList.size() > 0) {
			dataList.clear();
			for (County county : countyList) {
				dataList.add(county.getCountyName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedCity.getCityName());
			currentLevel = LEVEL_COUNTY;
		} else {
			Log.d("ChooseAreaActivity", selectedCity.getCityCode());
			queryFromServer(selectedCity.getCityCode(), "county");
		}
	}

	private void queryFromServer(final String code, final String type) {
		String address;
		if (!TextUtils.isEmpty(code)) {
			address = "http://www.weather.com.cn/data/list3/city" + code
					+ ".xml";
		} else {
			address = "http://www.weather.com.cn/data/list3/city.xml";
		}
		showProgressDialog();
		
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				boolean result = false;
				if("province".equals(type)) {
					result = Utility.handleProvincesResponse(coolWeatherDB, response);
				} else if("city".equals(type)) {
					result = Utility.handleCitiesResponse(coolWeatherDB, response, selectedProvince.getId());
				} else if("county".equals(type)) {
					result = Utility.handleCountiesResponse(coolWeatherDB, response, selectedCity.getId());
				}
				if(result) {
					//通過runOnUIThread()方法回到主線程處理邏輯
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							closeProgressDialog();
							if("province".equals(type)) {
								queryProvinces();
							} else if("city".equals(type)) {
								queryCities();
							} else if("county".equals(type)) {
								queryCounties();
							}
						}
					});
				}
				
			}
			
			@Override
			public void onError(Exception e) {
				//通過runOnUIThread()方法回到主線程處理邏輯
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "讀取失敗", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}

	/**
	 * 顯示進度對話框
	 */
	private void showProgressDialog() {
		if(progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("正在讀取中..");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	
	/**
	 * 關閉進度對話框
	 */
	private void closeProgressDialog() {
		if(progressDialog != null) {
			progressDialog.dismiss();
		}
	}
	
	@Override
	public void onBackPressed() {
		if(currentLevel == LEVEL_COUNTY) {
			queryCities();
		} else  if(currentLevel == LEVEL_CITY){
			queryProvinces();
		} else {
			finish();
		}
	}
}
