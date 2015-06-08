package com.coolweather.app.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpUtil {
	
	public static void sendHttpRequest(final String address,final HttpCallbackListener listener){
		new Thread(){
			public void run() {
				HttpURLConnection conn=null;
				try {
					URL url=new URL(address);
					conn=(HttpURLConnection) url.openConnection();
					conn.setRequestMethod("GET");
					conn.setConnectTimeout(8000);
					conn.setReadTimeout(8000);
					InputStream is=conn.getInputStream();
					BufferedReader reader=new BufferedReader(new InputStreamReader(is));
					StringBuilder response=new StringBuilder();
					String line;
					while((line=reader.readLine())!=null){
						response.append(line);
					}
					if(listener==null){
						listener.onFinish(response.toString());
					}
					if(listener!=null){
						listener.onFinish(response.toString());
					}
				} catch (Exception e) {
					listener.onError(e);
				} finally{
					if(conn!=null){
						conn.disconnect();
					}
				}
			};
		}.start();
	}

}
