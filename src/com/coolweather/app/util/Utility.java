package com.coolweather.app.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.coolweather.app.model.City;
import com.coolweather.app.model.County;
import com.coolweather.app.model.Province;
import com.coolweather.app.service.CoolWeatherDB;

public class Utility {
	/**
	 * 解析和处理服务器返回的省级数据
	 */
	public synchronized static boolean handleProvincesResponse(CoolWeatherDB coolweatherdb,String response){
		System.out.println("test");
		if(!TextUtils.isEmpty(response)){
			
			String[] allprovinces=response.split(",");
			if(allprovinces!=null&&allprovinces.length>0){
				for(String each:allprovinces){
					String[] province=each.split("\\|");
					Province p=new Province();
					p.setProvinceName(province[1]);
					p.setProvinceCode(province[0]);
					coolweatherdb.saveProvince(p);
				}
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 解析和处理服务器返回的市级数据
	 */
	public synchronized static boolean handleCitiesResponse(CoolWeatherDB coolweatherdb,String response,int provinceID){
		if(!TextUtils.isEmpty(response)){
			String[] cities=response.split(",");
			if(cities!=null&&cities.length>0){
				for(String each:cities){
					String[] city=each.split("\\|");
					City c=new City();
					c.setCityName(city[1]);
					c.setCityCode(city[0]);
					c.setProvinceId(provinceID);
					coolweatherdb.saveCity(c);
				}
				System.out.println("test");
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 解析和处理服务器返回的县级数据
	 */
	public synchronized static boolean handleCountiesResponse(CoolWeatherDB coolweatherdb,String response,int cityID){
		if(!TextUtils.isEmpty(response)){
			String[] counties=response.split(",");
			if(counties!=null&&counties.length>0){
				for(String each:counties){
					String[] county=each.split("\\|");
					County c=new County();
					c.setCountyName(county[1]);
					c.setCountyCode(county[0]);
					c.setCityId(cityID);
					coolweatherdb.saveCounty(c);
				}
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 解析服务器返回的JSON数据并存储到本地
	 */
	public static void handleWeatherResponse(Context context,String response){
		try {
			JSONObject jsonObject=new JSONObject(response);
			JSONObject weatherInfo=jsonObject.getJSONObject("weatherinfo");
			String cityName=weatherInfo.getString("city");
			String weatherCode=weatherInfo.getString("cityid");
			String temp1=weatherInfo.getString("temp");
//			String temp2=weatherInfo.getString("temp2");
			String weatherDesp=weatherInfo.getString("WD");
			String publishTime=weatherInfo.getString("time");
			saveWeatherInfo(context,cityName,weatherCode,temp1,weatherDesp,publishTime);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void saveWeatherInfo(Context context, String cityName,
			String weatherCode, String temp1, String weatherDesp,
			String publishTime) {
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy年M月d日",Locale.CHINA);
		SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(context).edit();
		editor.putBoolean("city_selected", true);
		editor.putString("city_name", cityName);
		editor.putString("weather_code", weatherCode);
		editor.putString("temp1", temp1);
//		editor.putString("temp2", temp2);
		editor.putString("weather_desp", weatherDesp);
		editor.putString("publish_time", publishTime);
		editor.putString("current_date", sdf.format(new Date()));
		editor.commit();
		
	}
}
