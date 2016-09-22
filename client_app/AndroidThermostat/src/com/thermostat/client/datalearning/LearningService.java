package com.thermostat.client.datalearning;

import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import com.thermostat.client.ThermostatActivity;
import com.thermostat.client.data.Configure;
import com.thermostat.client.utils.Utils;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;


public class LearningService  extends Service{
	
	private static final String TAG="AndroidThermostat";
	private final String DEVICE_TYPE = Configure.getDeviceType();
	private static final double R=6367444.7; 
	private final String url = Configure.getURL();
	private String deviceID;
	private String address;
	private double latitude;
	private double longitude;
	private int delay = 2*1000;
	private int period = 5*60*1000; 
	private float awayHomeDistance;
	private float atHomeDistance;
	private String provider;
	private boolean isAway;
	private String username;
	LocationManager locationManager;
	
	
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		super.onStartCommand(intent, flags, startId);	
		Log.d(TAG, "Learning Service has been started!");
		deviceID = Utils.getDeviceID(LearningService.this);
		provider = Utils.chooseProvider(this);
	    username = Utils.getUsername(this);

		String context = Context.LOCATION_SERVICE;
		locationManager = (LocationManager) getSystemService(context);		
		SharedPreferences settings = getSharedPreferences("settings",
				Activity.MODE_PRIVATE);
		address = settings.getString("address", "");
		Log.d(TAG, "address:" +address);
		SharedPreferences rules = getSharedPreferences("rules",
				Activity.MODE_PRIVATE);
		awayHomeDistance = rules.getFloat("awayHomeDistance", 0 );
		atHomeDistance = rules.getFloat("atHomeDistance", 0 );
		Log.d(TAG, "rules, awayHomeDistance:" + awayHomeDistance+ ", atHomeDistance=" + atHomeDistance );
		if(!(address.equals("")|| awayHomeDistance == 0 || atHomeDistance == 0))
		{
			double[] coordinate =getGeoCoorFromGivenAddress(address);
			latitude = coordinate[0];
			longitude = coordinate[1];
			Timer timer = new Timer();
			timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				Location location = locationManager
						.getLastKnownLocation(provider);
				double lat = location.getLatitude();
				double lng = location.getLongitude();
				double distance = calDistance(latitude,longitude, lat, lng);
				SharedPreferences share = getSharedPreferences("share",
						Activity.MODE_PRIVATE);		
				//isAway = share.getBoolean("isAway", false);				
				if(distance/1609.344 > awayHomeDistance)
				//if(distance > 50)//debug
				{
					/*if(!isAway)
					{*/
						SharedPreferences.Editor editor = share.edit();
						editor.putBoolean("isAway", true);
					/*}*/
									
					Log.d(TAG,"you are away from home.");
						
						String modeStr = share.getString("mode", "");					
						String targetStr = share.getString("targetTemp","");
						String[] targetArr = targetStr.split("бу");						
						int oldTarget = Integer.parseInt(targetArr[0]);
						int newTarget = 0;
						if (modeStr.indexOf("Cool") >= 0){		
							SharedPreferences settings = getSharedPreferences("settings",
									Activity.MODE_PRIVATE);
							newTarget = settings.getInt("awayCool", 0);
							Log.d(TAG,"current mode is cool,oldTarget=" + oldTarget + "new target, awayCool=" +newTarget);
						}
						if (modeStr.indexOf("Heat") >= 0){
							SharedPreferences settings = getSharedPreferences("settings",
									Activity.MODE_PRIVATE);
							newTarget = settings.getInt("awayHeat", 0);
							Log.d(TAG,"current mode is heat, new target, awayHeat=" +newTarget);
						}
						if(!(newTarget==0) && !(newTarget == oldTarget))
						{
							
							String target = Integer.toString(newTarget);
							String controlURL = url + "control/";
						    Log.d(TAG, "URL"+ controlURL);
							new HttpAsyncTaskAutoCtrl().execute(controlURL,target);
						}					
				}
				else if(distance/1609.344 < atHomeDistance)
				{
					/*if(isAway)
					{*/
						Log.d(TAG,"you are at home.");
						SharedPreferences.Editor editor = share.edit();
						editor.putBoolean("isAway", false);
						
						String modeStr = share.getString("mode", "");					
						String targetStr = share.getString("targetTemp","");
						String[] targetArr = targetStr.split("бу");						
						int oldTarget = Integer.parseInt(targetArr[0]);
						int newTarget = 0;
						if (modeStr.indexOf("Cool") >= 0){		
							SharedPreferences settings = getSharedPreferences("settings",
									Activity.MODE_PRIVATE);
							newTarget = settings.getInt("atHomeCool", 0);
							Log.d(TAG,"current mode is cool,oldTarget=" + oldTarget + "new target, atHomeCool=" +newTarget);
						}
						if (modeStr.indexOf("Heat") >= 0){
							SharedPreferences settings = getSharedPreferences("settings",
									Activity.MODE_PRIVATE);
							newTarget = settings.getInt("atHomeHeat", 0);
							Log.d(TAG,"current mode is heat, new target, atHomeHeat=" +newTarget);
						}
						if(!(newTarget==0) && !(newTarget == oldTarget))
						{
							
							String target = Integer.toString(newTarget);
							String controlURL = url + "control/";
						    Log.d(TAG, "URL"+ controlURL);
							new HttpAsyncTaskAutoCtrl().execute(controlURL,target);
						}
					//}
				}
				
			}

			}, delay, period);
			
		}
		return START_STICKY;
	}
	
	private double calDistance(double lat1, double lng1, double lat2, double lng2)
	{
		double distance=Math.acos(Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2))*Math.cos(Math.toRadians(lng1-lng2))+Math.sin(Math.toRadians(lat1))*Math.sin(Math.toRadians(lat2)))*R;
	    Log.i(TAG,"The distance between (" + lat1 + "," + lng1 + ") and (" + lat2 + "," + lng2 + ") is " + distance);
		return distance;
	}
	
	private double[] getGeoCoorFromGivenAddress(String address)
    {
          double lat= 0.0, lng= 0.0;
          double[] coordinate= {lat,lng};

        Geocoder geoCoder = new Geocoder(this, Locale.getDefault());    
        try 
        {
            List<Address> addresses = geoCoder.getFromLocationName(address , 1);
            if (addresses.size() > 0) 
            {            
            	Address location = addresses.get(0);
                lat= location.getLatitude();
                lng = location.getLongitude();
                coordinate[0] = lat;
                coordinate[1] = lng;
            Log.d(TAG, "The home address is: " + coordinate[0] + ", " + coordinate[1]);
            }
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
		return coordinate;
    }

	
	private String buildJsonCtr(String type, String id, String username, String scheme,
			String mode, String fan, int target) {
		SharedPreferences share = getSharedPreferences("share",
				Activity.MODE_PRIVATE);
		if (scheme == null) {
			if(share.getString("scheme","").indexOf("Auto")>=0)
				scheme = "auto";
			else if (share.getString("scheme","").indexOf("Manual")>=0)
				scheme = "manual";
		}
		if (mode == null) {
			if (share.getString("mode","").indexOf("Heat")>=0)
				mode = "heat";
			else if (share.getString("mode","").indexOf("Cool")>=0)
				mode = "cool";
			else if (share.getString("mode","").indexOf("Off")>=0)
				mode = "off";
		}
		if (fan == null) {
			if (share.getString("fan","").indexOf("Auto")>=0)
				fan = "auto";
			else if (share.getString("fan","").indexOf("Off")>= 0)
				fan = "off";
		}

		if (target == 0) {
			String targetStr = share.getString("targetTemp","");
			String[] targetArr = targetStr.split("бу");
			target = Integer.parseInt(targetArr[0]);
		}

		JSONObject jsonCtr;
		jsonCtr = new JSONObject();
		try {
			jsonCtr.put("type", type);
			jsonCtr.put("id", id);
			jsonCtr.put("username", username);
			jsonCtr.put("scheme", scheme);
			jsonCtr.put("mode", mode);
			jsonCtr.put("fan", fan);
			jsonCtr.put("target_temperature", target);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonCtr.toString();
	}
	
	
	public class HttpAsyncTaskAutoCtrl extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {

			int target = Integer.parseInt(urls[1]);
			Log.d(TAG,"doInBackground, target=" + target);
			String json = buildJsonCtr(DEVICE_TYPE, deviceID, username, null, null,
					null, target);
			Log.d(TAG, "control request with json:" + json);
			SharedPreferences autoControl = getSharedPreferences("autoControl",
					Activity.MODE_PRIVATE);
			SharedPreferences.Editor editor = autoControl.edit();
		     editor.putInt("targetTemp", target);
			editor.commit();
		    //String debugResult = "{\"result\": \"success\"}";
			//return debugResult;
			return Utils.sendJson(urls[0], json);
		}

		// onPostExecute displays the results of the AsyncTask.
		@Override
		protected void onPostExecute(String result) {
			boolean resultCtr = Utils.isResultSuccess(result);
			Log.d(TAG, "result of control response:" + resultCtr);
			if (resultCtr)// check the http response here
			{

				SharedPreferences autoControl = getSharedPreferences("autoControl",
						Activity.MODE_PRIVATE);
				int target = autoControl.getInt("targetTemp", 0);
				Log.d(TAG, "get info from sharePreference control,targetTemp=" + target);

				SharedPreferences share = getSharedPreferences("share",
						Activity.MODE_PRIVATE);
				SharedPreferences.Editor editor = share.edit();
				
				if (target != 0) {
					String targetStr = target + "буF";
					editor.putString("targetTemp", targetStr);
					Intent i = new Intent("TARGET_UPDATE");
					i.putExtra("target",targetStr);
					sendBroadcast(i);
				}
				editor.commit();
			
			}

		}
	}
	
	public void onDestroy(){
		/*Handler handler = new Handler();
		handler.removeCallbacksAndMessages(null);*/
		super.onDestroy();	
		Log.d(TAG,"Learning service has been stopped");
	}

}
