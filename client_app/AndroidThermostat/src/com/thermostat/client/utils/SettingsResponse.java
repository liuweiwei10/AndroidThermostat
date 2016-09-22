package com.thermostat.client.utils;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class SettingsResponse {
	private final String TAG = "AndroidThermostat";
	private int awayHeat;
	private int awayCool;
	private int atHomeHeat;
	private int atHomeCool;
	private String address;
	private String name;

	public SettingsResponse(String json)
	{
		try {
			JSONObject jsonObj = new JSONObject(json);					
			if(jsonObj.getString("result").equals("success"))
			{
                Log.d(TAG,"get settings result = success");
                JSONObject jsonPreference = new JSONObject();
                jsonPreference = jsonObj.getJSONObject("preference");
				this.awayHeat = jsonPreference.getInt("away_home_heat");
				Log.d(TAG, "awayHeat:" + awayHeat);
				this.awayCool = jsonPreference.getInt("away_home_cool");
				Log.d(TAG, "awayCool:" + awayCool);
				this.atHomeHeat = jsonPreference.getInt("at_home_heat");
				Log.d(TAG, "atHomeHeat:" + atHomeHeat);
				this.atHomeCool = jsonPreference.getInt("at_home_cool");
				Log.d(TAG, "atHomeCool:" + atHomeCool);
				this.address = jsonObj.getString("home_address");
				Log.d(TAG, "address:" + address);
				this.name = jsonObj.getString("thermostat_name");
				Log.d(TAG, "name:" + name);
			}
		} catch (JSONException e) {
			Log.d(TAG, "Json parse error in SettingsResponse");
            e.printStackTrace();
		}		
	}
	
	public int getAwayHeat()
	{
		return awayHeat;		
	}
	
	public int getAwayCool()
	{
		return awayCool;
	}
	
	
	public int getAtHomeHeat()
	{
		return atHomeHeat;		
	}
	
	public int getAtHomeCool()
	{
		return atHomeCool;
	}
	
	public String getAddress()
	{
		return address;
	}
	
	public String getName()
	{
		return name;
	}

}
