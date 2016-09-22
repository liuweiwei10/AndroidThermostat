package com.thermostat.client;

import org.json.JSONException;
import org.json.JSONObject;
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
import com.thermostat.client.data.Configure;

public class SigninActivity extends Activity{
	private String TAG="AndroidThermostat";
	private EditText etUsername;
	private EditText etPassword; 
	private Button btnSignin;
	private String username;
	private String password;
	private String deviceID;
	private final String DEVICE_TYPE = Configure.getDeviceType();
	private String url = Configure.getURL();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.signin);
		btnSignin = (Button) findViewById(R.id.btnSignin2);
		etUsername = (EditText)findViewById(R.id.etSigninEmail);
	    etPassword = (EditText)findViewById(R.id.etSigninPwd);
	    deviceID = Utils.getDeviceID(SigninActivity.this);
	    
		btnSignin.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				username = etUsername.getText().toString();
				password = etPassword.getText().toString();
				if (!(username.trim().equals("") || password.trim().equals(""))) {
					if (Utils.checkEmailFormat(username)) {
						new HttpAsyncTask().execute(url + "sign_in/");
					} else {
						Toast.makeText(getApplicationContext(),
								"Invalid email account", Toast.LENGTH_SHORT)
								.show();
					}
				} else {
					Toast.makeText(getApplicationContext(),
							"Please enter valid account and password.",
							Toast.LENGTH_SHORT).show();
				}
			}
	    });
		
   }
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		 
        if (keyCode == KeyEvent.KEYCODE_BACK
                 && event.getRepeatCount() == 0) 
        {
			Intent intent = new Intent();
		    intent.setClass(SigninActivity.this, WelcomeActivity.class);
		    startActivity(intent);
		    SigninActivity.this.finish();
            return true;
         }
         return super.onKeyDown(keyCode, event);
     }
	
	private String buildJsonSignIn(String type, String id, String username, String password)	
	{
		JSONObject jsonSignIn; 
        jsonSignIn = new JSONObject();
        try {
        	jsonSignIn.put("type", type);
        	jsonSignIn.put("id", id);
        	jsonSignIn.put("username", username);
        	jsonSignIn.put("password", password);
 
        } catch (JSONException e) {
        	e.printStackTrace();
        } 
        return jsonSignIn.toString();
	}
	
	private class HttpAsyncTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {
		     String json = buildJsonSignIn(DEVICE_TYPE, deviceID, username, password);
		     Log.d(TAG,"sign in request with json:" + json);
			 /*String debugResult = "{\"result\": \"success\"}";
		     return debugResult;*/
			 return Utils.sendJson(urls[0], json);
		}

		// onPostExecute displays the results of the AsyncTask.
		@Override
		protected void onPostExecute(String result) {
			 Log.d(TAG, "result:" + result);
			 if(result.trim().equals(""))
				 {
				 Toast.makeText(getApplicationContext(), "Fail to connect to the server.",
						  Toast.LENGTH_SHORT).show();
				 }
			 else
			 {
				 if(Utils.isResultSuccess(result))
				 {
			          Log.d(TAG,"signed in successfully with account:" + username + "password:" + password);
		    		  SharedPreferences sp = getSharedPreferences("share",0);
		    		  SharedPreferences.Editor editor = sp.edit();
		    		  editor.putBoolean("isSignin", true);
		    		  editor.putString("username",username);
		    		  editor.putString("password",password);
		    		  editor.commit();
					  Toast.makeText(getApplicationContext(), "Redirecting...", 
					  Toast.LENGTH_SHORT).show();					  					  
					  Intent intent = new Intent();
				      intent.setClass(SigninActivity.this, ThermostatActivity.class);
					  startActivity(intent);
					  SigninActivity.this.finish();
				 }	
			     else{
					  Toast.makeText(getApplicationContext(), "Wrong account or password",
					  Toast.LENGTH_SHORT).show();
				}
			 }

		}
	}
	
	
}
