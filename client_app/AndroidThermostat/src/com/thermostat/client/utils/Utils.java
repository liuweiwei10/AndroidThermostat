package com.thermostat.client.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;
import android.os.Bundle;

import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class Utils {

	private final static String TAG = "AndroidThermostat";

	private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
	            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

	private static LocationManager locationManager;
	private static int timeoutConnection = 3000;
	private static int timeoutSocket = 5000;

	public static String chooseProvider(Context context) {

		locationManager=(LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
		boolean isGPSenabled = locationManager
				.isProviderEnabled(LocationManager.GPS_PROVIDER);
		String provider;
		if (isGPSenabled) {
			provider = LocationManager.GPS_PROVIDER;
			Log.i(TAG, "GPS is enabled, use GPS as location provider");
		} else {
			provider = LocationManager.NETWORK_PROVIDER;
			Log.i(TAG, "GPS is disabled, use Network as location provider");
		}
		return provider;
	}
	
	public static String sendJson(String url, String json) 
	{

		Log.d(TAG, "enter senJson() with url:" + url + ", json:" +json);
		InputStream inputStream = null;
		String result = "";
		try {
			HttpParams httpParameters = new BasicHttpParams();
	        HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
			HttpClient httpclient = new DefaultHttpClient(httpParameters);
			Log.d(TAG, url);
			HttpPost httpPost = new HttpPost(url);
			StringEntity se = new StringEntity(json);
			se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
			se.setContentType("application/json");
			httpPost.setEntity(se);
			//httpPost.setHeader("Accept", "application/json");
			httpPost.setHeader("Content-type", "application/json");
			HttpResponse httpResponse = httpclient.execute(httpPost);
			if(httpResponse.getStatusLine().getStatusCode()==HttpStatus.SC_OK)
				Log.v(TAG, "connect successfully");
			else
				Log.v(TAG, "status code" + httpResponse.getStatusLine().getStatusCode());
			inputStream = httpResponse.getEntity().getContent();
			if (inputStream != null) {
				result = convertInputStreamToString(inputStream);
				Log.d(TAG, "httpResponse:" + result);
			} 
			else{
				Log.d(TAG, "httpResponse is null");
			}
			inputStream.close();
		} catch (UnsupportedEncodingException e) {
	        e.printStackTrace();
	    } catch (ClientProtocolException e) {
	        e.printStackTrace();
	    } catch (ConnectTimeoutException e) {
	        Log.e("CONN TIMEOUT", e.toString());
	    } catch (SocketTimeoutException e) {
	        Log.e("SOCK TIMEOUT", e.toString());
	    } catch (IOException e) {
	        e.printStackTrace();
	    } catch (Exception e) {
	        Log.e("OTHER EXCEPTIONS", e.toString());
	    }
		
		Log.d("TAG", "httpResponse is null");
		return result;
	}
	
	public static String convertInputStreamToString(InputStream inputStream)
			throws IOException {
		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(inputStream));
		String line = "";
		String result = "";
		while ((line = bufferedReader.readLine()) != null)
			result += line;
		inputStream.close();
		return result;

	}
	
	public static boolean isResultSuccess(String json)
	{
		boolean result = false;
		try {
			JSONObject jsonObj = new JSONObject(json);
			String jresult = jsonObj.getString("result");
			if(jresult.equals("success"))
				result =true;

		} catch (JSONException e) {
			Log.d(TAG, "Json parse error");
            e.printStackTrace();
		}
		return result;
	}
	
	public static String getDeviceID(Context context) {
		TelephonyManager tMgr =(TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
	    String deviceID = tMgr.getLine1Number();
	    if(deviceID.equals(null)||deviceID.equals(""))
		{
	    	TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		    deviceID = tm.getDeviceId();
		}
		Log.d(TAG, "Device ID:" + deviceID);
		return deviceID;
	}
	
	public static String getAddress(Context context, double latitude, double longitude, boolean flag) {
		String addressString = "";
		Geocoder gc = new Geocoder(context, Locale.getDefault());
		try {
			// get address information
			List<Address> addresses = gc
					.getFromLocation(latitude, longitude, 1);
			StringBuilder sb = new StringBuilder();
			if (addresses.size() > 0) {
				Address address = addresses.get(0);
				int i;
				for (i = 0; i < address.getMaxAddressLineIndex()-1; i++){
					sb.append(address.getAddressLine(i)).append(",");
				}
				sb.append(address.getAddressLine(i));
					
			    if(flag){
					sb.append(address.getCountryName()).append("\n");
					addressString = sb.toString();
			    }
			    else 
			    	addressString = sb.toString();			    	
			}
		} catch (IOException e) {
			Log.e("TAG", "IOException when getting address");
		}
		return addressString;
	}
	
	public static String buildJsonCommon(String type, String id, String username) {
		JSONObject json;
		json = new JSONObject();
		try {
			json.put("type", type);
			json.put("id", id);
			json.put("username", username);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString();
	}
	
	public static String buildJsonUpdateSettings(String type, String id, String username,
			int awayHeat, int awayCool, int atHomeHeat, int atHomeCool,
			String name, String address) {
		JSONObject json;
		json = new JSONObject();
		JSONObject jsonPreference = new JSONObject();
		try {
			jsonPreference.put("away_home_heat", awayHeat);
			jsonPreference.put("away_home_cool", awayCool);
			jsonPreference.put("at_home_heat", atHomeHeat);
			jsonPreference.put("at_home_cool", atHomeCool);
			json.put("type", type);
			json.put("id", id);
			json.put("username", username);
			json.put("preference", jsonPreference);
			json.put("thermostat_name", name);
			json.put("home_address", address);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString();
	}
	
	public static boolean isServiceRunning(Context context, String className)
	{
		 boolean isRunning = false;
         ActivityManager activityManager = (ActivityManager)
         context.getSystemService(Context.ACTIVITY_SERVICE); 
         List<ActivityManager.RunningServiceInfo> serviceList 
                    = activityManager.getRunningServices(100);

         if (!(serviceList.size()>0)) {
             return false;         
         }
         for (int i=0; i<serviceList.size(); i++) {
             if (serviceList.get(i).service.getClassName().equals(className) == true) {
                 isRunning = true;        
                 break;
             }
         }
         return isRunning;
	}
	
	public static boolean checkEmailFormat(String username)
	{
		 Pattern pattern;
		 Matcher matcher;
		 pattern = Pattern.compile(EMAIL_PATTERN);
		 matcher = pattern.matcher(username);
         return matcher.matches();
	}
	
	public static String getUsername(Context context)
	{
		SharedPreferences share = context.getSharedPreferences("share", 0);
		String username = share.getString("usernanme", "");
        return username;
	}
				
	
/*	public static Location getCurLoc(Context context){
		

		locationManager=(LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(chooseProvider(context), 1000, 0, locationListener);  
		
		      
}
private	static final LocationListener locationListener  = new LocationListener() {   
	    public void onLocationChanged(Location location) { 
	        if (location != null) {
	        	
	        }   
	    }  
	    public void onProviderEnabled(String provider) {    
        
	    }   
	    public void onProviderDisabled(String provider) {   
	    	
	    }   
	    public void onStatusChanged(String provider, int status, Bundle extras) {   
	    	
	    }
	};*/
	
	
	
}
