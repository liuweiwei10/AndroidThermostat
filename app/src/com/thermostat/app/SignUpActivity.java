package com.thermostat.app;

import org.json.JSONException;
import org.json.JSONObject;

import com.thermostat.app.data.Configure;
import com.thermostat.app.utils.Utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.app.Activity;

public class SignUpActivity extends Activity {
	private final String TAG = "Thermostat";
	private EditText etEmail;
	private EditText etPwd;
	private EditText etPwdCfm;
	private Button btnSignup;
	private Button btnBack;
	private Button btnBottom1;
	private Button btnBottom2;
	private Button btnBottom3;
	private Button btnBottom4;
	private Button btnBottom5;
	private String confirmPwd;
	private String url = Configure.getURL();
	private String deviceID;
	private final String DEVICE_TYPE = "thermostat";
	private ProgressDialog  pdialog;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sign_up);
		
		SharedPreferences sp = getSharedPreferences("setup", 0);
		Boolean isRight = sp.getBoolean("signup_anim", true);

		if (isRight) {
			//set animation: activity enter from right
			Animation anim = AnimationUtils.loadAnimation(this,
					R.anim.in_from_right);
			findViewById(R.id.toplayout).startAnimation(anim);
		} else {
			//set animation: activity enter from left
			Animation anim = AnimationUtils.loadAnimation(this,
					R.anim.in_from_left);
			findViewById(R.id.toplayout).startAnimation(anim);
		}
		
		btnSignup = (Button) findViewById(R.id.btn_sign_up);
		etEmail= (EditText) findViewById(R.id.et_signup_email);
		etPwd = (EditText) findViewById(R.id.et_signup_pwd);
		etPwdCfm = (EditText) findViewById(R.id.et_signup_pwd_cfm);
		btnBack= (Button) findViewById(R.id.btn_back);	
		btnBottom1 = (Button) findViewById(R.id.btn_bottom1);
		btnBottom2 = (Button) findViewById(R.id.btn_bottom2);
		btnBottom3 = (Button) findViewById(R.id.btn_bottom3);
		btnBottom4 = (Button) findViewById(R.id.btn_bottom4);
		btnBottom5 = (Button) findViewById(R.id.btn_bottom5);

		btnBottom1.setEnabled(false);
		btnBottom2.setEnabled(false);
		btnBottom3.setEnabled(false);
		btnBottom4.setEnabled(false);
		btnBottom5.setEnabled(false);

		deviceID = Utils.getDeviceID(SignUpActivity.this);
		

		btnSignup.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				String username = etEmail.getText().toString();
				String password = etPwd.getText().toString();
				String confirmPwd = etPwdCfm.getText().toString();					
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
		
		btnBack.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v)
			{
				SharedPreferences sp = getSharedPreferences("setup",0);
	    		SharedPreferences.Editor editor = sp.edit();
	    		editor.putBoolean("account_anim", false);
	    		editor.commit();
	    		
		        Animation anim = AnimationUtils.loadAnimation(SignUpActivity.this,R.anim.out_to_right);
		        findViewById(R.id.toplayout).startAnimation(anim);
				Intent intent = new Intent();
				intent.setClass(SignUpActivity.this, AccountActivity.class);
				startActivity(intent);
			    SignUpActivity.this.finish();
			}	    
	    }); 	
	}
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			Intent intent = new Intent();
			intent.setClass(SignUpActivity.this, WelcomeActivity.class);
			startActivity(intent);
			SignUpActivity.this.finish();
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
			publishProgress();
			String debugResult = "{\"result\": \"success\"}";
			return debugResult;
			//return Utils.sendJson(urls[0], json);
		}
		protected void onProgressUpdate(Void... progress) {
			  pdialog = ProgressDialog.show(SignUpActivity.this, null, "Please wait...", true, false);  
			
		}

		// onPostExecute displays the results of the AsyncTask.
		@Override
		protected void onPostExecute(String result) {
			Log.d(TAG, "result:" + result);
			if (result.trim().equals("")) {
				pdialog.dismiss();
				Toast.makeText(getApplicationContext(),
						"Fail to connect to the server.", Toast.LENGTH_SHORT)
						.show();
			} else {
				if (Utils.isResultSuccess(result)) {
					SharedPreferences register = getSharedPreferences(
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
					/*Toast.makeText(getApplicationContext(),
							"Account is opened successfully.",
							Toast.LENGTH_SHORT).show();*/
					Intent intent = new Intent();
					intent.setClass(SignUpActivity.this,
							SuccessActivity.class);
					startActivity(intent);
					SignUpActivity.this.finish();
				} else {
					Toast.makeText(getApplicationContext(),
							"Fail to open account, please try again.", Toast.LENGTH_SHORT)
							.show();
				}
			}

		}
	}

}
