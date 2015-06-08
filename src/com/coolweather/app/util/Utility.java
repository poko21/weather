package com.coolweather.app.util;

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
}
