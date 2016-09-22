package com.thermostat.client;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import com.thermostat.client.data.Configure;
import com.thermostat.client.utils.MonitorResponse;
import com.thermostat.client.utils.SettingsResponse;
import com.thermostat.client.utils.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

public class SettingsActivity extends Activity {

	private final String TAG = "AndroidThermostat";
	private final String URL = Configure.getURL();
	private final String DEVICE_TYPE = Configure.getDeviceType();
	private String deviceID;
	private EditText etAwayHeat;
	private EditText etAwayCool;
	private EditText etAtHomeHeat;
	private EditText etAtHomeCool;
	private EditText etName;
	private EditText etAddress;
	private CheckBox cbCurrentLoc;
	private double lng;
	private double lat;
	private String provider;
	private String username;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		etAwayHeat = (EditText) this.findViewById(R.id.etAwayHeat);
		etAwayCool = (EditText) this.findViewById(R.id.etAwayCool);
		etAtHomeHeat = (EditText) this.findViewById(R.id.etAtHomeHeat);
		etAtHomeCool = (EditText) this.findViewById(R.id.etAtHomeCool);
		etName = (EditText) this.findViewById(R.id.etName);
		etAddress = (EditText) this.findViewById(R.id.etAddress);
		cbCurrentLoc = (CheckBox) this.findViewById(R.id.cbCurrentLoc);
		deviceID = Utils.getDeviceID(SettingsActivity.this);
		provider = Utils.chooseProvider(this);
	    username = Utils.getUsername(this);
        
		SharedPreferences settings = getSharedPreferences("settings",
				Activity.MODE_PRIVATE);
		String name = settings.getString("name", "");
		String address = settings.getString("address", "");
		int awayHeat = settings.getInt("awayHeat", 0);
		int awayCool = settings.getInt("awayCool", 0);
		int atHomeHeat = settings.getInt("atHomeHeat", 0);
		int atHomeCool = settings.getInt("atHomeCool", 0);
		etAwayHeat.setText(String.valueOf(awayHeat));
		etAwayCool.setText(String.valueOf(awayCool));
		etAtHomeHeat.setText(String.valueOf(atHomeHeat));
		etAtHomeCool.setText(String.valueOf(atHomeCool));
		etName.setText(name);
		etAddress.setText(address);
		new HttpAsyncTaskGetSettings().execute(URL + "get_settings/");

		cbCurrentLoc
				.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton arg0,
							boolean arg1) {
						if (cbCurrentLoc.isChecked()) {
							String context = Context.LOCATION_SERVICE;
							LocationManager locationManager = (LocationManager) getSystemService(context);
							Location location = locationManager
									.getLastKnownLocation(provider);
							lat = location.getLatitude();
							lng = location.getLongitude();
							String address = getAddress(lat, lng, false);
							if (!"".equals(address.trim())) {
								etAddress.setText(address);
							}

						} else {
							etAddress.setText("");
						}

					}
				});
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

			String name = etName.getText().toString();
			String address = etAddress.getText().toString();

			String awayHeatStr = etAwayHeat.getText().toString();
			String awayCoolStr = etAwayCool.getText().toString();
			String atHomeHeatStr = etAtHomeHeat.getText().toString();
			String atHomeCoolStr = etAtHomeCool.getText().toString();
			int awayHeat = Integer.parseInt(awayHeatStr);
			int awayCool = Integer.parseInt(awayCoolStr);
			int atHomeHeat = Integer.parseInt(atHomeHeatStr);
			int atHomeCool = Integer.parseInt(atHomeCoolStr);
			if (!(name.equals(getSettingsName())
					&& address.equals(getSettingsAddress())
					&& awayHeat == getSettingsAwayHeat()
					&& awayCool == getSettingsAwayCool()
					&& atHomeHeat == getSettingsAtHomeHeat() && atHomeCool == getSettingsAtHomeCool())) {
				String settingsURL = URL + "update_settings/";
				new HttpAsyncTaskUpdateSettings().execute(settingsURL, awayHeatStr,
						awayCoolStr, atHomeHeatStr, atHomeCoolStr, name,
						address);

			}

			Intent intent = new Intent();
			intent.setClass(SettingsActivity.this, ThermostatActivity.class);
			startActivity(intent);
			SettingsActivity.this.finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	/*
	 * get status in shared preference "share"
	 */
	private String getSettingsName() {
		SharedPreferences settings = getSharedPreferences("settings",
				Activity.MODE_PRIVATE);
		String name = settings.getString("name", "");
		return name;
	}

	private String getSettingsAddress() {
		SharedPreferences settings = getSharedPreferences("settings",
				Activity.MODE_PRIVATE);
		String address = settings.getString("address", "");
		return address;
	}

	private int getSettingsAwayHeat() {
		SharedPreferences settings = getSharedPreferences("settings",
				Activity.MODE_PRIVATE);
		int awayHeat = settings.getInt("awayHeat", 0);
		return awayHeat;
	}

	private int getSettingsAwayCool() {
		SharedPreferences settings = getSharedPreferences("settings",
				Activity.MODE_PRIVATE);
		int awayCool = settings.getInt("awayCool", 0);
		return awayCool;
	}

	private int getSettingsAtHomeHeat() {
		SharedPreferences settings = getSharedPreferences("settings",
				Activity.MODE_PRIVATE);
		int atHomeHeat = settings.getInt("atHomeHeat", 0);
		return atHomeHeat;
	}

	private int getSettingsAtHomeCool() {
		SharedPreferences settings = getSharedPreferences("settings",
				Activity.MODE_PRIVATE);
		int atHomeCool = settings.getInt("atHomeCool", 0);
		return atHomeCool;
	}

	/*
	 * get address from latitude and longitude
	 */
	private String getAddress(double latitude, double longitude, boolean flag) {
		String addressString = "";
		Geocoder gc = new Geocoder(this, Locale.getDefault());
		try {
			// get address information
			List<Address> addresses = gc
					.getFromLocation(latitude, longitude, 1);
			StringBuilder sb = new StringBuilder();
			if (addresses.size() > 0) {
				Address address = addresses.get(0);
				int i;
				for (i = 0; i < address.getMaxAddressLineIndex() - 1; i++) {
					sb.append(address.getAddressLine(i)).append(",");
				}
				sb.append(address.getAddressLine(i));

				if (flag) {
					sb.append(address.getCountryName()).append("\n");
					addressString = sb.toString();
				} else
					addressString = sb.toString();
			}
		} catch (IOException e) {
			Log.e("TAG", "IOException when getting address");
		}
		return addressString;
	}

/*	private String buildJsonUpdateSettings(String type, String id, int awayHeat,
			int awayCool, int atHomeHeat, int atHomeCool, String name,
			String address) {
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
			json.put("preference", jsonPreference);		
			json.put("thermostat_name", name);
			json.put("home_address", address);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString();
	}*/

	private class HttpAsyncTaskGetSettings extends
			AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {
			String json = Utils.buildJsonCommon(DEVICE_TYPE, deviceID, username);
			Log.d(TAG, "get settings request with json:" + json);

/*			String debugResult = "{\"result\": \"success\", \"preference\": {\"away_home_heat\":69, "
					+ "\"away_home_cool\": 79, \"at_home_heat\": 74, \"at_home_cool\": 74}, \"thermostat_name\": \"Weiwei\", \"home_address\": \"726 E 9th St, Tucson, AZ, 85719\"}";
			return debugResult;*/
		   return Utils.sendJson(urls[0], json);
		}

		// onPostExecute displays the results of the AsyncTask.
		@Override
		protected void onPostExecute(String result) {
			Log.d(TAG, "result:" + result);
		/*	if (result.trim().equals("") || result.equals("failure")) {

				SharedPreferences settings = getSharedPreferences("settings",
						Activity.MODE_PRIVATE);
				String name = settings.getString("name", "");
				String address = settings.getString("address", "");
				int awayHeat = settings.getInt("awayHeat", 0);
				int awayCool = settings.getInt("awayCool", 0);
				int atHomeHeat = settings.getInt("atHomeHeat", 0);
				int atHomeCool = settings.getInt("atHomeCool", 0);
				etAwayHeat.setText(String.valueOf(awayHeat));
				etAwayCool.setText(String.valueOf(awayCool));
				etAtHomeHeat.setText(String.valueOf(atHomeHeat));
				etAtHomeCool.setText(String.valueOf(atHomeCool));
				etName.setText(name);
				etAddress.setText(address);
			}*/

			if (Utils.isResultSuccess(result)) {
				Log.d(TAG, "request settings successfully.");
				SettingsResponse getSettingsResponse = new SettingsResponse(
						result);
				int awayHeat = getSettingsResponse.getAwayHeat();
				int awayCool = getSettingsResponse.getAwayCool();
				int atHomeHeat = getSettingsResponse.getAtHomeHeat();
				int atHomeCool = getSettingsResponse.getAtHomeCool();
				String address = getSettingsResponse.getAddress();
				String name = getSettingsResponse.getName();

				etAwayHeat.setText(String.valueOf(awayHeat));
				etAwayCool.setText(String.valueOf(awayCool));
				etAtHomeHeat.setText(String.valueOf(atHomeHeat));
				etAtHomeCool.setText(String.valueOf(atHomeCool));
				etAddress.setText(address);
				etName.setText(name);

				SharedPreferences settings = getSharedPreferences("settings",
						Activity.MODE_PRIVATE);
				SharedPreferences.Editor editor = settings.edit();
				editor.putInt("awayHeat", awayHeat);
				editor.putInt("awayCool", awayCool);
				editor.putInt("atHomeHeat", atHomeHeat);
				editor.putInt("atHomeCool", atHomeCool);
				editor.putString("name", name);
				editor.putString("address", address);
				editor.commit();

			}
		}
	}

	private class HttpAsyncTaskUpdateSettings extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {
			int awayHeat = Integer.parseInt(urls[1]);
			int awayCool = Integer.parseInt(urls[2]);
			int atHomeHeat = Integer.parseInt(urls[3]);
			int atHomeCool = Integer.parseInt(urls[4]);
			String name = urls[5];
			String address  = urls[6];			
			String json = Utils.buildJsonUpdateSettings(DEVICE_TYPE, deviceID, username, awayHeat, awayCool, atHomeHeat, atHomeCool, name, address);
			Log.d(TAG, "update settings request with json:" + json);
			SharedPreferences temp = getSharedPreferences("tempSettings",
					Activity.MODE_PRIVATE);
			SharedPreferences.Editor editor = temp.edit();

			editor.putInt("awayHeat", awayHeat);
			editor.putInt("awayCool", awayCool);
			editor.putInt("atHomeHeat", atHomeHeat);
			editor.putInt("atHomeCool", atHomeCool);
			editor.putString("address", address);
			editor.putString("name", name);
			editor.commit();

/*
			 String debugResult = "{\"result\": \"success\"}";
			 return debugResult;*/
			 return Utils.sendJson(urls[0], json);
		}

		// onPostExecute displays the results of the AsyncTask.
		@Override
		protected void onPostExecute(String result) {
			Log.d(TAG, "result:" + result);
			if (result.trim().equals("")) {
				Toast.makeText(getApplicationContext(),
						"Fail to connect to the server.", Toast.LENGTH_SHORT)
						.show();
			} else {
				if (Utils.isResultSuccess(result)) {
					SharedPreferences temp = getSharedPreferences(
							"tempSettings", Activity.MODE_PRIVATE);
					int awayHeat = temp.getInt("awayHeat", 0);
					int awayCool = temp.getInt("awayCool", 0);
					int atHomeHeat = temp.getInt("atHomeHeat", 0);
					int atHomeCool = temp.getInt("atHomeCool", 0);
					String address = temp.getString("address", "");
					String name = temp.getString("name", "");
					Log.d(TAG, "settings changed: awayHeat:" + awayHeat
							+ ",awayCool:" + awayCool + ",atHomeHeat:"
							+ atHomeHeat + ",atHomeCool:" + atHomeCool
							+ ",address:" + address + ",name:" + name);

					SharedPreferences settings = getSharedPreferences(
							"settings", Activity.MODE_PRIVATE);
					SharedPreferences.Editor editorSettings = settings.edit();

					editorSettings.putInt("awayHeat", awayHeat);
					editorSettings.putInt("awayCool", awayCool);
					editorSettings.putInt("atHomeHeat", atHomeHeat);
					editorSettings.putInt("atHomeCool", atHomeCool);
					editorSettings.putString("address", address);
					editorSettings.putString("name", name);
					editorSettings.commit();
					Intent i = new Intent("ADDRESS_UPDATE");
					i.putExtra("address",address);
					sendBroadcast(i);

				} else {
					Toast.makeText(getApplicationContext(),
							"Fail to change the settings", Toast.LENGTH_SHORT)
							.show();
				}

			}
		}
	}
}
