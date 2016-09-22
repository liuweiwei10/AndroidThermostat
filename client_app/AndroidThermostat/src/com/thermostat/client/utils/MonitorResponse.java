package com.thermostat.client.utils;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class MonitorResponse {
	private final String TAG = "AndroidThermostat";
	private String mode;
	private String fan;
	private int inside_temperature;
	private int target_temperature;
	private String scheme;

	public MonitorResponse(String json)
	{
		try {
			JSONObject jsonObj = new JSONObject(json);					
			if(jsonObj.getString("result").equals("success"))
			{
                Log.d(TAG,"request monitor info: result = success");
				this.mode = jsonObj.getString("mode");
				Log.d(TAG, "mode:" + mode);
				this.fan = jsonObj.getString("fan");
				Log.d(TAG, "fan:" + fan);
				this.inside_temperature = jsonObj.getInt("inside_temperature");
				Log.d(TAG, "inside:" + inside_temperature);
				this.target_temperature = jsonObj.getInt("target_temperature");
				Log.d(TAG, "target:" + target_temperature);
				this.scheme = jsonObj.getString("scheme");
			}
		} catch (JSONException e) {
			Log.d(TAG, "Json parse error in Monitor Response");
            e.printStackTrace();
		}		
	}
	
	public String getMode()
	{
		return mode;		
	}
	
	public String getFan()
	{
		return fan;
	}
	
	public int getTemperature()
	{
		return inside_temperature;
	}
	
	public int getTargetTemperature()
	{
		return target_temperature;
	}
	
	public String getScheme()
	{
		return scheme;
	}
}
