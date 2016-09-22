package com.thermostat.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
/*import android.text.SpannableString;
 import android.text.style.UnderlineSpan;*/
import android.widget.TextView;
import android.provider.Settings.Secure;

import com.thermostat.client.data.Configure;
import com.thermostat.client.utils.RulesResponse;
import com.thermostat.client.utils.Utils;
import com.thermostat.client.utils.MonitorResponse;

public class ThermostatActivity extends Activity {

	private final String TAG = "AndroidThermostat";
	private final String url = Configure.getURL();
	private final String DEVICE_TYPE = Configure.getDeviceType();
	private final CharSequence[] modeItems = { "Cool", "Heat", "Off" };
	private final CharSequence[] fanItems = { "Auto", "Off" };
	private final CharSequence[] schemeItems = { "Auto", "Manual" };
	private final String LEARNING_SERVICE = "com.thermostat.client.datalearning.LearningService";
	private final String LOCATION_SERVICE = "com.thermostat.client.datalearning.LocationService";

	private boolean isLocService;
	private boolean isAutoEnabled;
	private boolean isRule;
	private String username;
	private String address;
	private String thermoName;
	private String insideTempString;
	private String targetTempString;
	private String targetStr;
	private String schemeStr;
	private String modeStr;
	private String fanStr;
	private int insideTemp;
	private int targetTemp;
	private Button btnSettings;
	private Button btnIncTemp;
	private Button btnDecTemp;
	private Button btnMode;
	private Button btnFan;
	private Button btnScheme;
	private TextView tvThermoName;
	private TextView tvAddress;
	private TextView tvInsideTemp;
	private TextView tvTargetTemp;
	private String deviceID;
	
	
	private boolean firstFlag;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		tvThermoName = (TextView) this.findViewById(R.id.tvThermoName);
		tvAddress = (TextView) this.findViewById(R.id.tvAddress);
		tvInsideTemp = (TextView) this.findViewById(R.id.tvInsideTemp);
		tvTargetTemp = (TextView) this.findViewById(R.id.tvTargetTemp);
		btnSettings = (Button) this.findViewById(R.id.btnSettings);
		btnMode = (Button) this.findViewById(R.id.btnMode);
		btnFan = (Button) this.findViewById(R.id.btnFan);
		btnScheme = (Button) this.findViewById(R.id.btnScheme);
		btnIncTemp = (Button) this.findViewById(R.id.btnIncTemp);
		btnDecTemp = (Button) this.findViewById(R.id.btnDecTemp);
		username = Utils.getUsername(this);
        //debug
/*		SharedPreferences share = getSharedPreferences("share",
				Activity.MODE_PRIVATE);
		firstFlag = share.getBoolean("firstFlag", true);
		if (firstFlag) {
			Log.d(TAG,"firstFlag=true");
			modeStr = btnMode.getText().toString();
			targetStr = tvTargetTemp.getText().toString();
			fanStr = btnFan.getText().toString();
			schemeStr = btnScheme.getText().toString();
			SharedPreferences.Editor editor = share.edit();
			editor.putString("mode", modeStr);
			editor.putString("targetTemp", targetStr);
			editor.putString("scheme", schemeStr);
			editor.putString("fan", fanStr);
			editor.putBoolean("firstFlag", false);
			editor.commit();
		}*/
		
		
		getStatus();
		tvThermoName.setText(thermoName);
		tvAddress.setText(address);
		startLocService();
		deviceID = Utils.getDeviceID(ThermostatActivity.this);
		if(!isRule)
		{
			new HttpAsyncTaskGetRules().execute(url + "get_rules/");
		}
		
		
		// request monitor info
		// String jsonMnt = buildJsonMnt(DEVICE_TYPE,deviceID);
		new HttpAsyncTaskMonitor().execute(url + "monitor/");

		// tvTargetTemp.setText(targetStr);
		// btnMode.setText(modeStr);
		// btnFan.setText(fanStr);
		// btnScheme.setText(schemeStr);


		registerReceiver(targetUpdate, new IntentFilter("TARGET_UPDATE"));
		registerReceiver(addressUpdate, new IntentFilter("ADDRESS_UPDATE"));
		
		btnSettings.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(ThermostatActivity.this, SettingsActivity.class);
				startActivity(intent);
				ThermostatActivity.this.finish();
			}
		});

		btnIncTemp.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				targetStr = tvTargetTemp.getText().toString();
				String[] targetArr = targetStr.split("бу");
				int oldTemp = Integer.parseInt(targetArr[0]);
				if (oldTemp < 89) {
					int newTemp = oldTemp + 1;
					String target = Integer.toString(newTemp);
					String controlURL = url + "control/";
					new HttpAsyncTaskCtrl().execute(controlURL, null, null,
							null, target);
				}
			}
		});

		btnDecTemp.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				String targetStr = tvTargetTemp.getText().toString();
				String[] targetArr = targetStr.split("бу");
				int oldTemp = Integer.parseInt(targetArr[0]);
				if (oldTemp > 50) {
					int newTemp = oldTemp - 1;
					String target = Integer.toString(newTemp);
					String controlURL = url + "control/";
					new HttpAsyncTaskCtrl().execute(controlURL, null, null,
							null, target);
				}
			}
		});

		btnMode.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						ThermostatActivity.this);
				builder.setTitle("Change the mode");
				builder.setItems(modeItems,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int item) {
								String modeStr = btnMode.getText().toString();
								String[] modeArr = modeStr.split(":\n");
								String mode = modeArr[1];
								String newMode = modeItems[item].toString();
								if (!newMode.equals(mode)) {
									String controlURL = url + "control/";
									String target = Integer.toString(0);
									new HttpAsyncTaskCtrl().execute(controlURL, null, newMode,
											null, target);
									/*modeStr = modeArr[0] + ":\n" + newMode;
									btnMode.setText(modeStr);
									SharedPreferences share = getSharedPreferences(
											"share", Activity.MODE_PRIVATE);
									SharedPreferences.Editor editor = share
											.edit();
									editor.putString("mode", modeStr);
									editor.commit();*/
								}
							}
						});
				AlertDialog modeDialog = builder.create();
				modeDialog.show();
			}
		});

		btnFan.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						ThermostatActivity.this);
				builder.setTitle("Change the fan setting");
				builder.setItems(fanItems,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int item) {
								fanStr = btnFan.getText().toString();
								String[] fanArr = fanStr.split(":\n");
								String fan = fanArr[1];
								String newFan = fanItems[item].toString();
								if (!newFan.equals(fan)) {
									String controlURL = url + "control/";
									String target = Integer.toString(0);
									new HttpAsyncTaskCtrl().execute(controlURL, null, null,
											newFan, target);
									/*fanStr = fanArr[0] + ":\n" + newFan;
									btnFan.setText(fanStr);
									SharedPreferences share = getSharedPreferences(
											"share", Activity.MODE_PRIVATE);
									SharedPreferences.Editor editor = share
											.edit();
									editor.putString("fan", fanStr);
									editor.commit();
								*/
								}
									
							}
						});
				AlertDialog modeDialog = builder.create();
				modeDialog.show();
			}
		});

		btnScheme.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						ThermostatActivity.this);
				builder.setTitle("Change the scheme");
				builder.setItems(schemeItems,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int item) {
								String schemeStr = btnScheme.getText()
										.toString();
								String[] schemeArr = schemeStr.split(":\n");
								String scheme = schemeArr[1];
								String newScheme = schemeItems[item].toString();
								if (!newScheme.equals(scheme)) {
									String controlURL = url + "control/";
									String target = Integer.toString(0);
									new HttpAsyncTaskCtrl().execute(controlURL,newScheme, null,
											null, target);
									/*schemeStr = schemeArr[0] + ":\n"
											+ newScheme;
									btnScheme.setText(schemeStr);
									SharedPreferences share = getSharedPreferences(
											"share", Activity.MODE_PRIVATE);
									SharedPreferences.Editor editor = share
											.edit();
									editor.putString("scheme", schemeStr);
									if (newScheme.trim() == "Auto") {
										btnFan.setEnabled(false);
										btnMode.setEnabled(false);
										btnIncTemp.setEnabled(false);
										btnDecTemp.setEnabled(false);

										editor.putBoolean("isAutoEnabled", true);
										editor.commit();
									} else {
										btnFan.setEnabled(true);
										btnMode.setEnabled(true);
										btnIncTemp.setEnabled(true);
										btnDecTemp.setEnabled(true);
										editor.putBoolean("isAutoEnabled",
												false);
										;
										editor.commit();
									}*/
								}

							}
						});
				AlertDialog modeDialog = builder.create();
				modeDialog.show();
			}
		});

	}
	
	private BroadcastReceiver addressUpdate= new BroadcastReceiver() {

	    @Override
	    public void onReceive(Context context, Intent intent) {

	    	tvAddress.setText(intent.getExtras().getString("address"));

	    }
	};

	
	private BroadcastReceiver targetUpdate= new BroadcastReceiver() {

	    @Override
	    public void onReceive(Context context, Intent intent) {

	    	tvTargetTemp.setText(intent.getExtras().getString("target"));

	    }
	};

	
	
	/*
	 * start the location service
	 */
	private void startLocService() {
		boolean isRunning = Utils.isServiceRunning(ThermostatActivity.this, LOCATION_SERVICE);		
		Log.d(TAG, "isRunning=" + isRunning);
		if (!isRunning) {
			SharedPreferences sp = getSharedPreferences("share",
					Activity.MODE_PRIVATE);
			startService(new Intent("com.thermostat.client.Location"));
			Log.d(TAG,"Location service has been started");
			SharedPreferences.Editor spEditor = sp.edit();
			spEditor.putBoolean("isLocService", true);
			spEditor.commit();
		}
	}

	/*
	 * get status in shared preference "share"
	 */
	private void getStatus() {
		SharedPreferences share = getSharedPreferences("share",
				Activity.MODE_PRIVATE);
		// get the status of location service
		isLocService = share.getBoolean("isLocService", false);
		isRule = share.getBoolean("isRule", false);
		targetStr = share.getString("targetTemp", "0буF");
		schemeStr = share.getString("scheme", "Scheme:\nAuto");
		modeStr = share.getString("mode", "Mode:\nCool");
		fanStr = share.getString("fan", "Fan:\nAuto");
		

		SharedPreferences settings = getSharedPreferences("settings",
				Activity.MODE_PRIVATE);
		thermoName = settings.getString("name", "");
		address = settings.getString("address", "");
	}

	private String buildJsonCtr(String type, String id, String username,String scheme,
			String mode, String fan, int target) {
		if (scheme == null) {
			if (btnScheme.getText().toString().indexOf("Auto") >= 0)
				scheme = "auto";
			else if (btnScheme.getText().toString().indexOf("Manual") >= 0)
				scheme = "manual";
		}
		if (mode == null) {
			if (btnMode.getText().toString().indexOf("Heat") >= 0)
				mode = "heat";
			else if (btnMode.getText().toString().indexOf("Cool") >= 0)
				mode = "cool";
			else if (btnMode.getText().toString().indexOf("Off") >= 0)
				mode = "off";
		}
		if (fan == null) {
			if (btnFan.getText().toString().indexOf("Auto") >= 0)
				fan = "auto";
			else if (btnFan.getText().toString().indexOf("Off") >= 0)
				fan = "off";
		}

		if (target == 0) {
			targetStr = tvTargetTemp.getText().toString();
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

	public String toCapital(String name) {
		name = name.substring(0, 1).toUpperCase() + name.substring(1);
		return name;

	}

	private String getModeStr(String mode) {
		String modeStr = btnMode.getText().toString();
		String[] modeArr = modeStr.split(":\n");
		modeStr = modeArr[0] + ":\n" + toCapital(mode);
		return modeStr;
	}

	private String getSchemeStr(String scheme) {
		String schemeStr = btnScheme.getText().toString();
		String[] schemeArr = schemeStr.split(":\n");
		schemeStr = schemeArr[0] + ":\n" + toCapital(scheme);
		return schemeStr;
	}

	private String getFanStr(String fan) {
		String fanStr = btnFan.getText().toString();
		String[] fanArr = fanStr.split(":\n");
		fanStr = fanArr[0] + ":\n" + toCapital(fan);
		return fanStr;
	}

	private String getInsideTempStr(int temp) {

		String insideTempStr = tvInsideTemp.getText().toString();
		String[] insideTempArr = targetStr.split("бу");
		insideTempStr = String.valueOf(temp) + "бу" + insideTempArr[1];
		return insideTempStr;
	}

	private String getTargetTempStr(int target) {
		String targetTempStr = tvTargetTemp.getText().toString();
		String[] targetTempArr = targetTempStr.split("бу");
		targetTempStr = String.valueOf(target) + "бу" + targetTempArr[1];
		return targetTempStr;
	}

	private class HttpAsyncTaskMonitor extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {
			String json = Utils.buildJsonCommon(DEVICE_TYPE, deviceID, username);
			Log.d(TAG, "monitor request with json:" + json);

/*		    String debugResult = "{\"result\": \"success\",\"scheme\": \"manual\", " +
		     		"\"mode\": \"cool\", \"fan\": \"auto\", \"inside_temperature\": 78, \"target_temperature\": 72}";
		    return debugResult;*/
			return Utils.sendJson(urls[0], json);
		}

		// onPostExecute displays the results of the AsyncTask.
		@Override
		protected void onPostExecute(String result) {
			Log.d(TAG, "result:" + result);
			boolean isSuccess = Utils.isResultSuccess(result);
			if (isSuccess) {
				MonitorResponse monitorResponse = new MonitorResponse(result);
				String mode = monitorResponse.getMode();
				Log.d(TAG, "mode:" + mode);
				String fan = monitorResponse.getFan();
				Log.d(TAG, "fan:" + fan);
				insideTemp = monitorResponse.getTemperature();
				Log.d(TAG, "insideTemp:" + insideTemp);
				targetTemp = monitorResponse.getTargetTemperature();
				Log.d(TAG, "targetTemp:" + targetTemp);
				String scheme = monitorResponse.getScheme();
				Log.d(TAG, "scheme:" + scheme);

				String insideTempStr = getInsideTempStr(insideTemp);
				String targetTempStr = getTargetTempStr(targetTemp);
				modeStr = getModeStr(mode);
				fanStr = getFanStr(fan);
				schemeStr = getSchemeStr(scheme);
				tvInsideTemp.setText(insideTempStr);
				tvTargetTemp.setText(targetTempStr);
				btnMode.setText(modeStr);
				btnFan.setText(fanStr);
				btnScheme.setText(schemeStr);

				SharedPreferences share = getSharedPreferences("share",
						Activity.MODE_PRIVATE);
				SharedPreferences.Editor editor = share.edit();
				editor.putString("mode", modeStr);
				editor.putString("fan", fanStr);
				editor.putString("scheme", schemeStr);
				editor.putString("insideTemp", insideTempStr);
				editor.putString("targetTemp", targetTempStr);
				editor.commit();

				String schemeStr = btnScheme.getText().toString();
				boolean isRunning = Utils.isServiceRunning(
						ThermostatActivity.this, LEARNING_SERVICE);
				if (schemeStr.indexOf("Auto") >= 0) {
					btnFan.setEnabled(false);
					btnMode.setEnabled(false);
					btnIncTemp.setEnabled(false);
					btnDecTemp.setEnabled(false);
					/*
					 * boolean isLearning =
					 * share.getBoolean("isLearning",false); Log.d(TAG,
					 * "is Learning =" + isLearning);
					 */
					// if(!isRunning)
					if (true) {
						stopService(new Intent(
								"com.thermostat.client.Learning"));
						startService(new Intent(
								"com.thermostat.client.Learning"));
						Log.d(TAG, "Learning service has been started");
						/*
						 * SharedPreferences.Editor shareEditor = share.edit();
						 * shareEditor.putBoolean("isLearning", true);
						 * shareEditor.commit();
						 */
					}
				} else {
					/*
					 * boolean isLearning =
					 * share.getBoolean("isLearning",false);
					 */
					if (isRunning) {
						stopService(new Intent("com.thermostat.client.learning"));
						/*
						 * SharedPreferences.Editor shareEditor = share.edit();
						 * shareEditor.putBoolean("isLearning", false);
						 * shareEditor.commit();
						 */
					}
				}
			}
			else{
				Log.d(TAG, "Fail to request monitor info");
				Toast.makeText(getApplicationContext(),
						"Fail to connect the server", Toast.LENGTH_SHORT)
						.show();			
				SharedPreferences share = getSharedPreferences("share",
						Activity.MODE_PRIVATE);
				targetStr = share.getString("targetTemp", "0буF");
				schemeStr = share.getString("scheme", "Scheme:\nManual");
				modeStr = share.getString("mode", "Mode:\nCool");
				fanStr = share.getString("fan", "Fan:\nAuto");
				btnMode.setText(modeStr);
				btnFan.setText(fanStr);
				btnScheme.setText(schemeStr);
				tvTargetTemp.setText(targetStr);	
				if (schemeStr.indexOf("Auto") >= 0) {
					btnFan.setEnabled(false);
					btnMode.setEnabled(false);
					btnIncTemp.setEnabled(false);
					btnDecTemp.setEnabled(false);					
				}
				
			}
				

		}
	}

	public class HttpAsyncTaskCtrl extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {

			int target = Integer.parseInt(urls[4]);
			String json = buildJsonCtr(DEVICE_TYPE, deviceID, username, urls[1], urls[2],
					urls[3], target);
			Log.d(TAG, "control request with json:" + json);
			SharedPreferences control = getSharedPreferences("control",
					Activity.MODE_PRIVATE);
			SharedPreferences.Editor editor = control.edit();
			if (urls[1] != null)
				editor.putString("scheme", urls[1]);
			if (urls[2] != null)
				editor.putString("mode", urls[2]);
			if (urls[3] != null)
				editor.putString("fan", urls[3]);
			if (target != 0)
				editor.putInt("targetTemp", target);
			editor.commit();
		 /*   String debugResult = "{\"result\": \"success\"}";
			return debugResult;*/
			return Utils.sendJson(urls[0], json);
		}

		// onPostExecute displays the results of the AsyncTask.
		@Override
		protected void onPostExecute(String result) {
			boolean resultCtr = Utils.isResultSuccess(result);
			Log.d(TAG, "result of control response:" + resultCtr);
			if (resultCtr)// check the http response here
			{

				SharedPreferences control = getSharedPreferences("control",
						Activity.MODE_PRIVATE);

				String scheme = control.getString("scheme", "");
				String mode = control.getString("mode", "");
				String fan = control.getString("fan", "");
				int target = control.getInt("targetTemp", 0);
				Log.d(TAG, "get info from sharePreference control, scheme="
						+ scheme + ",mode=" + mode + ",fan=" + fan
						+ ",targetTemp=" + target);

				SharedPreferences share = getSharedPreferences("share",
						Activity.MODE_PRIVATE);
				SharedPreferences.Editor editor = share.edit();
				if (!scheme.equals("")) {
					schemeStr = getSchemeStr(scheme);
					btnScheme.setText(schemeStr);
					editor.putString("scheme", schemeStr);
				}
				if (!mode.equals("")) {
					modeStr = getModeStr(mode);
					btnMode.setText(modeStr);
					editor.putString("mode", modeStr);
				}
				if (!fan.equals("")) {

					fanStr = getFanStr(fan);
					btnFan.setText(fanStr);
					editor.putString("fan", fanStr);
				}

				if (target != 0) {
					String targetTempStr = getTargetTempStr(target);
					tvTargetTemp.setText(targetTempStr);
					editor.putString("targetTemp", targetStr);
				}

				editor.commit();
				
				SharedPreferences.Editor controlEditor = control.edit();
				controlEditor.putString("scheme", "");
				controlEditor.putString("mode", "");
				controlEditor.putString("fan", "");
				controlEditor.putInt("targetTemp", 0);

				String schemeStr = btnScheme.getText().toString();
				boolean isRunning = Utils.isServiceRunning(ThermostatActivity.this, LEARNING_SERVICE);
				if (schemeStr.indexOf("Auto") >= 0) {
					btnFan.setEnabled(false);
					btnMode.setEnabled(false);
					btnIncTemp.setEnabled(false);
					btnDecTemp.setEnabled(false);
					editor.putBoolean("isAutoEnabled",
							true);	
					//if(!isRunning)
					//{			
					    stopService(new Intent(
					   "com.thermostat.client.Learning"));
						startService(new Intent("com.thermostat.client.Learning"));
					    Log.d(TAG, "Learning service has been started");
					//}	
					
				}
				else{
					    
						btnFan.setEnabled(true);
						btnMode.setEnabled(true);
						btnIncTemp.setEnabled(true);
						btnDecTemp.setEnabled(true);
						editor.putBoolean("isAutoEnabled",
								false);						
						editor.commit();
						if(isRunning)
						{
						    Log.d(TAG,"Switched back to manual mode, try to stop learning service");
							stopService(new Intent("com.thermostat.client.Learning"));
						}	
				}
			}

		}
	}
	
	private class HttpAsyncTaskGetRules extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {
			String json = Utils.buildJsonCommon(DEVICE_TYPE, deviceID, username);
			Log.d(TAG, "rules request with json:" + json);
		   /* String debugResult = "{\"result\": \"success\",\"rules\": {\"away_home_distance\": 3, \"at_home_distance\": 1}}";
	    	return debugResult;*/
			
			return Utils.sendJson(urls[0], json);
		}

		// onPostExecute displays the results of the AsyncTask.
		@Override
		protected void onPostExecute(String result) {
			Log.d(TAG, "result:" + result);
			if (Utils.isResultSuccess(result)) {
				RulesResponse ruleResponse = new RulesResponse(result);
				float atHomeDistance = ruleResponse.getAtHomeDistance();
				float awayHomeDistance = ruleResponse.getAwayHomeDistance();
				SharedPreferences rules = getSharedPreferences("rules",
						Activity.MODE_PRIVATE);
				SharedPreferences.Editor rulesEditor = rules.edit();
				rulesEditor.putFloat("awayHomeDistance", awayHomeDistance);
				rulesEditor.putFloat("atHomeDistance", atHomeDistance);
				rulesEditor.commit();
				SharedPreferences share = getSharedPreferences("share",
						Activity.MODE_PRIVATE);
				SharedPreferences.Editor shareEditor= share.edit();
				shareEditor.putBoolean("isRule",true);
			}
		}
		
	}
	

   @Override
	protected void onDestroy(){
	    super.onDestroy();
		unregisterReceiver(targetUpdate);
	}

}
