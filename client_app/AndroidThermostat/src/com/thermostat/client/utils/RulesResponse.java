package com.thermostat.client.utils;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class RulesResponse {	
	private final String TAG = "AndroidThermostat";
	private float awayHomeDistance;
	private float atHomeDistance;
	public RulesResponse(String json)
	{
		try {
			JSONObject jsonObj = new JSONObject(json);					
			if(jsonObj.getString("result").equals("success"))
			{
                Log.d(TAG,"get settings result = success");
                JSONObject jsonRules = new JSONObject();
                jsonRules = jsonObj.getJSONObject("rules");
				this.awayHomeDistance =(float)jsonRules.getDouble("away_home_distance");
				Log.d(TAG, "awayHomeDistance:" + awayHomeDistance);
				this.atHomeDistance = (float)jsonRules.getDouble("at_home_distance");
				Log.d(TAG, "awayCool:" + atHomeDistance);
			}
		} catch (JSONException e) {
			Log.d(TAG, "Json parse error in RulesResponse");
            e.printStackTrace();
		}		
	}
	
	public float getAwayHomeDistance(){
		return awayHomeDistance;	
	}
	
	public float getAtHomeDistance(){
		return atHomeDistance;
	}
	
}
