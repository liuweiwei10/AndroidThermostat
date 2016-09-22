package com.thermostat.app;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.util.Log;

public class WeatherHttpClient {
	private String city;
	private String country;
	private float curTemp;
	private float maxTemp;
	private float minTemp;
	private String description;
	private String main;
	private String imageId;
	private byte[] imageData;
	

	private final String TAG = "Thermostat";
	private static String BASE_URL = "http://api.openweathermap.org/data/2.5/weather?q=";
	private static String IMG_URL = "http://openweathermap.org/img/w/";
	
	public String getWeatherJson(String location) {
		 HttpURLConnection conn = null;
		 StringBuilder weatherJson = new StringBuilder();
		 try{
		       URL url = new URL(BASE_URL + location);
		       conn = (HttpURLConnection) url.openConnection();
		       conn.setRequestMethod("GET");
		       conn.connect();
		       
		       InputStreamReader in = new InputStreamReader(conn.getInputStream());
	            int read;
	            char[] buff = new char[1024];
	            while ((read = in.read(buff)) != -1) {
	            	weatherJson.append(buff, 0, read);
	            }
		 
		 } catch (MalformedURLException e) {
	            Log.e(TAG, "Error processing weather API URL", e);
	            return weatherJson.toString();
	        } catch (IOException e) {
	            Log.e(TAG, "Error connecting to weather API", e);
	            return weatherJson.toString();
	        } finally {
	            if (conn != null) {
	                conn.disconnect();
	            }
	        }
			return weatherJson.toString();

	}
	
	public Bitmap getImage(String imgId) {
		HttpURLConnection conn = null;
        InputStream in = null;
		try {
		   URL url = new URL(IMG_URL + imgId +".png");	
		   conn = (HttpURLConnection) url.openConnection();
		   in = conn.getInputStream();
		   return BitmapFactory.decodeStream(in);         
	 
	 } catch (MalformedURLException e) {
           Log.e(TAG, "Error processing weather API URL", e);
       } catch (IOException e) {
           Log.e(TAG, "Error connecting to weather API", e);
       } finally {
           if (conn != null) {
               conn.disconnect();
           }
       }
		return null;
		
	}
	
	public void parseWetherJson(String weatherData) throws JSONException {
		JSONObject dataObj = new JSONObject(weatherData);
		
		JSONObject sysObj = dataObj.getJSONObject("sys");
		country = sysObj.getString("country");
		city = dataObj.getString("name");
		
		JSONArray weatherArr = dataObj.getJSONArray("weather");
		JSONObject weatherObj = weatherArr.getJSONObject(0);
		main = weatherObj.getString("main");
		description = weatherObj.getString("description");
		imageId =  weatherObj.getString("icon");
		
		JSONObject mainObj = dataObj.getJSONObject("main");
		curTemp = (float) mainObj.getDouble("temp");
		maxTemp = (float) mainObj.getDouble("temp_max");
		minTemp = (float) mainObj.getDouble("temp_min");
	}
	
	public float getTemp() {
		return curTemp;
	}
	public String getLocation() {
		return city;
	}
	public String getImageId() {
		return imageId;
	}
	
}

