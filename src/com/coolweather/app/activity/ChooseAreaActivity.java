package com.coolweather.app.activity;

import java.util.ArrayList;
import java.util.List;

import com.coolweather.app.R;
import com.coolweather.app.model.City;
import com.coolweather.app.model.County;
import com.coolweather.app.model.Province;
import com.coolweather.app.service.CoolWeatherDB;
import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseAreaActivity extends Activity {

	public static final int LEVEL_PROVINCE = 0;
	public static final int LEVEL_CITY = 1;
	public static final int LEVEL_COUNTY = 2;

	private ListView listview;
	private TextView textview;
	private ArrayAdapter<String> adapter;
	private ProgressDialog progressdialog;
	private CoolWeatherDB coolweatherdb;
	private List<String> datalist = new ArrayList<String>();
	private List<Province> provincelist = new ArrayList<Province>();
	private List<City> citylist = new ArrayList<City>();
	private List<County> countylist = new ArrayList<County>();
	private Province provinceSelected;
	private City citySelected;
	private County countySelected;
	private int currentLevel;
	/**
	 * 是否从WeatherActivity跳转
	 */
	private boolean isFromWeatherActivity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		isFromWeatherActivity=getIntent().getBooleanExtra("from_weather_activity",false);
		SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
		//已经选择了城市且不是从WeatherAcitivity跳转,才会直接转到WeatherActivity
		if(prefs.getBoolean("city_selected", false)&&!isFromWeatherActivity){
			Intent intent=new Intent(this,WeatherActivity.class);
			startActivity(intent);
			finish();
			return;
		}
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		listview = (ListView) findViewById(R.id.list_view);
		textview = (TextView) findViewById(R.id.title_text);
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, datalist);
		listview.setAdapter(adapter);
		coolweatherdb = CoolWeatherDB.getInstance(this);
		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (currentLevel == LEVEL_PROVINCE) {
					provinceSelected = provincelist.get(position);
					queryCities();
				} else if (currentLevel == LEVEL_CITY) {
					citySelected = citylist.get(position);
					queryCounties();
				}else if(currentLevel==LEVEL_COUNTY){
					String countyCode=countylist.get(position).getCountyCode();
					Intent intent=new Intent(ChooseAreaActivity.this, WeatherActivity.class);
					intent.putExtra("county_code", countyCode);
					startActivity(intent);
					finish();
				}

			}

		});
		queryProvinces();//加载省级数据
	}

	/**
	 * 查询所有省,优先从数据库查,若无则去服务器查
	 */
	private void queryProvinces() {
		provincelist = coolweatherdb.loadProvinces();
		if (provincelist.size() > 0) {
			datalist.clear();
			for (Province province : provincelist) {
				datalist.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listview.setSelection(0);
			textview.setText("中国");
			currentLevel = LEVEL_PROVINCE;
		} else {
			queryFromServer(null, "province");
		}

	}

	/**
	 * 查询所有市,优先从数据库查,若无则去服务器查
	 */
	private void queryCities() {
		citylist = coolweatherdb.loadCities(provinceSelected.getId());
		if (citylist.size() > 0) {
			datalist.clear();
			for (City city : citylist) {
				datalist.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			listview.setSelection(0);
			textview.setText(provinceSelected.getProvinceName());
			currentLevel = LEVEL_CITY;
		} else{
			queryFromServer(provinceSelected.getProvinceCode(), "city");
		}

	}

	/**
	 * 查询所有县,优先从数据库查,若无则去服务器查
	 */
	private void queryCounties() {
		countylist = coolweatherdb.loadCounties(citySelected.getId());
		if (countylist.size() > 0) {
			datalist.clear();
			for (County county : countylist) {
				datalist.add(county.getCountyName());
			}
			adapter.notifyDataSetChanged();
			listview.setSelection(0);
			textview.setText(citySelected.getCityName());
			currentLevel = LEVEL_COUNTY;
		}else{
			queryFromServer(citySelected.getCityCode(), "county");
		}

	}

	/**
	 * 根据传入的代号和类型从服务器上查询省市县数据
	 */
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
				if ("province".equals(type)) {
					result = Utility.handleProvincesResponse(coolweatherdb,
							response);
				} else if ("city".equals(type)) {
					result = Utility.handleCitiesResponse(coolweatherdb,
							response, provinceSelected.getId());
				} else if ("county".equals(type)) {
					result = Utility.handleCountiesResponse(coolweatherdb,
							response, citySelected.getId());
				}

				if (result) {
					// 通过runOnUiThread()方法回到主线程处理逻辑
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							closeProgressDialog();
							if ("province".equals(type)) {
								queryProvinces();
							} else if ("city".equals(type)) {
								queryCities();
							} else if ("county".equals(type)) {
								queryCounties();
							}
						}
					});
				}
			}

			@Override
			public void onError(Exception e) {
				// 通过runOnUiThread()方法回到主线程处理逻辑
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "数据加载失败", 1)
								.show();
					}
				});

			}
		});
	}

	/**
	 * 显示进度对话框
	 */
	private void showProgressDialog() {
		if (progressdialog == null) {
			progressdialog = new ProgressDialog(this);
			progressdialog.setMessage("正在加载中...");
			progressdialog.setCanceledOnTouchOutside(false);
		}
		progressdialog.show();
	}

	/**
	 * 关闭进度对话框
	 */
	private void closeProgressDialog() {
		if (progressdialog != null) {
			progressdialog.dismiss();
		}
	}

	/**
	 * 获取返回键,根据当前级别判断应返回的列表或退出
	 */
	@Override
	public void onBackPressed() {
		if (currentLevel == LEVEL_COUNTY) {
			queryCities();
		} else if (currentLevel == LEVEL_CITY) {
			queryProvinces();
		} else {
			if(isFromWeatherActivity){
				Intent intent=new Intent(this,WeatherActivity.class);
				startActivity(intent);
			}
			finish();
		}
	}
}
