package com.thermostat.client;

import org.json.JSONException;
import org.json.JSONObject;

import com.thermostat.client.data.Configure;
import com.thermostat.client.utils.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.app.Activity;

public class RegisterActivity extends Activity {
	private final String TAG = "AndroidThermostat";
	private EditText etUsername;
	private EditText etPassword;
	private EditText etConfirmPwd;
	private Button btnRegister;
	private String confirmPwd;
	private String url = Configure.getURL();
	private String deviceID;
	private final String DEVICE_TYPE = "phone";

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);
		btnRegister = (Button) findViewById(R.id.btnRegister2);
		etUsername = (EditText) findViewById(R.id.etRegisterEmail);
		etPassword = (EditText) findViewById(R.id.etRegisterPwd);
		etConfirmPwd = (EditText) findViewById(R.id.etRegisterPwd2);
		deviceID = Utils.getDeviceID(RegisterActivity.this);

		btnRegister.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				String username = etUsername.getText().toString();
				String password = etPassword.getText().toString();
				String confirmPwd = etConfirmPwd.getText().toString();					
				if (!(username.trim().equals("") || password.trim().equals("")
						|| confirmPwd.trim().equals(""))) 
				{
					if(Utils.checkEmailFormat(username))
					{
						 if (password.equals(confirmPwd))
						 {
							 String registerURL = url + "register/";
								new HttpAsyncTaskReg().execute(registerURL, username,
										password);
						 }
						 else
						 {
							 Toast.makeText(getApplicationContext(),
										"Please confirm your password.",
										Toast.LENGTH_SHORT).show();
						 }
					}
					else
					{
						 Toast.makeText(getApplicationContext(),
									"Invalid email account",
									Toast.LENGTH_SHORT).show();
					}										
				}
				else
				{
					Toast.makeText(getApplicationContext(),
					"Please enter valid account and password.",
					Toast.LENGTH_SHORT).show();			
			    }
			}
		});
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			Intent intent = new Intent();
			intent.setClass(RegisterActivity.this, WelcomeActivity.class);
			startActivity(intent);
			RegisterActivity.this.finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private String buildJsonRegister(String type, String id, String username,
			String password) {
		JSONObject jsonRegister;
		jsonRegister = new JSONObject();
		try {
			jsonRegister.put("type", type);
			jsonRegister.put("id", id);
			jsonRegister.put("username", username);
			jsonRegister.put("password", password);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonRegister.toString();
	}

	private class HttpAsyncTaskReg extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {
			String json = buildJsonRegister(DEVICE_TYPE, deviceID, urls[1],
					urls[2]);
			Log.d(TAG, "register request with json:" + json);
			SharedPreferences register = getSharedPreferences("register",
					Activity.MODE_PRIVATE);
			SharedPreferences.Editor editor = register.edit();

			editor.putString("username", urls[1]);
			editor.putString("password", urls[2]);
			editor.commit();

			/*String debugResult = "{\"result\": \"success\"}";
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
					Toast.makeText(getApplicationContext(),
							"Success, please go to your email account to finish verification.",
							Toast.LENGTH_SHORT).show();
					/*SharedPreferences register = getSharedPreferences(
							"register", Activity.MODE_PRIVATE);
					String username = register.getString("username", "");
					String password = register.getString("password", "");
					Log.d(TAG, "Open an account successfully with account:"
							+ username + ",password:" + password);
					SharedPreferences sp = getSharedPreferences("share", 0);
					SharedPreferences.Editor editor = sp.edit();
					editor.putString("username", username);
					editor.putString("password", password);
					editor.commit();
					Toast.makeText(getApplicationContext(),
							"Account is opened successfully.",
							Toast.LENGTH_SHORT).show();*/
					Intent intent = new Intent();
					intent.setClass(RegisterActivity.this,
							SigninActivity.class);
					startActivity(intent);
					RegisterActivity.this.finish();
				} else {
					Toast.makeText(getApplicationContext(),
							"Fail to open account, please try again.", Toast.LENGTH_SHORT)
							.show();
				}
			}

		}
	}

}
