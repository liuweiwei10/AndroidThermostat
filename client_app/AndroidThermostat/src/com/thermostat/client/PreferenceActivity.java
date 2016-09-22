package com.thermostat.client;

import org.json.JSONException;
import org.json.JSONObject;

import com.thermostat.client.data.Configure;
import com.thermostat.client.utils.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

public class PreferenceActivity extends Activity {
	private final String TAG = "AndroidThermostat";
	private final String DEVICE_TYPE = "phone";

	private EditText etAwayHeatDef;
	private EditText etAwayCoolDef;
	private EditText etAtHomeHeatDef;
	private EditText etAtHomeCoolDef;
	private CheckBox cbCurrentLoc;
	private EditText etAddressDef;
	private Button btnOK;
	private String deviceID;
	private String provider;
	private String username;
	private String url = Configure.getURL();


	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preference);

		etAwayHeatDef = (EditText) findViewById(R.id.etAwayHeatDef);
		etAwayCoolDef = (EditText) findViewById(R.id.etAwayCoolDef);
		etAtHomeHeatDef = (EditText) findViewById(R.id.etAtHomeHeatDef);
		etAtHomeCoolDef = (EditText) findViewById(R.id.etAtHomeCoolDef);
		cbCurrentLoc = (CheckBox) findViewById(R.id.cbCurrentLocDef);
		etAddressDef = (EditText) findViewById(R.id.etAddressDef);
		btnOK = (Button) findViewById(R.id.btnOKPreference);
		deviceID = Utils.getDeviceID(PreferenceActivity.this);
		provider = Utils.chooseProvider(this);
		username = Utils.getUsername(this);

		btnOK.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				String awayHeat = etAwayHeatDef.getText().toString();
				String awayCool = etAwayCoolDef.getText().toString();
				String atHomeHeat = etAtHomeHeatDef.getText().toString();
				String atHomeCool = etAtHomeCoolDef.getText().toString();
				String address = etAddressDef.getText().toString();
				if (awayHeat.trim().equals("") || awayCool.trim().equals("")
						|| atHomeHeat.trim().equals("")
						|| atHomeCool.trim().equals("")
						|| address.trim().equals("")) {
					Toast.makeText(getApplicationContext(),
							"Please enter valid values", Toast.LENGTH_SHORT)
							.show();
				} else {
					String registerURL = url + "update_settings/";
					new HttpAsyncTaskPre().execute(registerURL, awayHeat,
							awayCool, atHomeHeat, atHomeCool, address);
				}
			}
		});

		cbCurrentLoc.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton arg0,
							boolean arg1) {
						if (cbCurrentLoc.isChecked()) {
							SharedPreferences actState = getSharedPreferences(
									"temp", 0);
							SharedPreferences.Editor editor = actState.edit();
							editor.putBoolean("curAddr", true);
							String context = Context.LOCATION_SERVICE;
							LocationManager locationManager = (LocationManager) getSystemService(context);
							Location location = locationManager
									.getLastKnownLocation(provider);
							double lat = location.getLatitude();
							double lng = location.getLongitude();
							String address = Utils.getAddress(
									PreferenceActivity.this, lat, lng, false);
							etAddressDef.setText(address);

						}
					}
				});
	}

	/*private String buildJsonPre(String type, String id, int awayHeat,
			int awayCool, int atHomeHeat, int atHomeCool, String address) {
		JSONObject jsonPreference;
		jsonPreference = new JSONObject();
		try {
			jsonPreference.put("type", type);
			jsonPreference.put("id", id);
			jsonPreference.put("awayHeat", awayHeat);
			jsonPreference.put("awayCool", awayCool);
			jsonPreference.put("atHomeHeat", atHomeHeat);
			jsonPreference.put("atHomeCool", atHomeCool);
			jsonPreference.put("address", address);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonPreference.toString();
	}*/

	private class HttpAsyncTaskPre extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {
			int awayHeat = Integer.parseInt(urls[1]);
			int awayCool = Integer.parseInt(urls[2]);
			int atHomeHeat = Integer.parseInt(urls[3]);
			int atHomeCool = Integer.parseInt(urls[4]);
			String address = urls[5];
			String json = Utils.buildJsonUpdateSettings(DEVICE_TYPE, deviceID, username, awayHeat,
					awayCool, atHomeHeat, atHomeCool, "debug", address);
			Log.d(TAG, "update settings request with json:" + json);
			SharedPreferences preference = getSharedPreferences("preference",
					Activity.MODE_PRIVATE);
			SharedPreferences.Editor editor = preference.edit();

			editor.putInt("awayHeat", awayHeat);
			editor.putInt("awayCool", awayCool);
			editor.putInt("atHomeHeat", atHomeHeat);
			editor.putInt("atHomeCool", atHomeCool);
			editor.putString("address", address);
			editor.commit();

		    //String debugResult = "{\"result\": \"success\"}";
			//return debugResult;
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
					SharedPreferences preference = getSharedPreferences(
							"preference", Activity.MODE_PRIVATE);
					int awayHeat = preference.getInt("awayHeat", 0);
					int awayCool = preference.getInt("awayCool", 0);
					int atHomeHeat = preference.getInt("atHomeHeat", 0);
					int atHomeCool = preference.getInt("atHomeCool", 0);
					String address = preference.getString("address", "");
					Log.d(TAG, "Default values set: awayHeat:" + awayHeat
							+ ",awayCool:" + awayCool + ",atHomeHeat:"
							+ atHomeHeat + ",atHomeCool:" + atHomeCool
							+ ",address:" + address);
					
					SharedPreferences settings = getSharedPreferences("settings",
							Activity.MODE_PRIVATE);
					SharedPreferences.Editor editorSettings = settings.edit();

					editorSettings.putInt("awayHeat", awayHeat);
					editorSettings.putInt("awayCool", awayCool);
					editorSettings.putInt("atHomeHeat", atHomeHeat);
					editorSettings.putInt("atHomeCool", atHomeCool);
					editorSettings.putString("address", address);
					editorSettings.commit();
					
					SharedPreferences share = getSharedPreferences("share", 0);
					SharedPreferences.Editor editorShare = share.edit();
					editorShare.putBoolean("isSignin", true);
					editorShare.commit();
					Toast.makeText(getApplicationContext(),
							"Success! Signing in....", Toast.LENGTH_SHORT)
							.show();
					Intent intent = new Intent();
					intent.setClass(PreferenceActivity.this,
							ThermostatActivity.class);
					startActivity(intent);
					PreferenceActivity.this.finish();
				} else {
					Toast.makeText(getApplicationContext(),
							"Fail to set preferences. Please try again",
							Toast.LENGTH_SHORT).show();
				}
			}

		}
	}
}
