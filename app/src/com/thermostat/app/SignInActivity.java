package com.thermostat.app;

import org.json.JSONException;
import org.json.JSONObject;
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
import com.thermostat.app.data.Configure;

public class SignInActivity extends Activity{
	private String TAG="Thermostat";
	private EditText etUsername;
	private EditText etPassword; 
	private Button btnBack;
	private Button btnSignin;
	private Button btnBottom1;
	private Button btnBottom2;
	private Button btnBottom3;
	private Button btnBottom4;
	private Button btnBottom5;
	private String username;
	private String password;
	private String deviceID;
	private final String DEVICE_TYPE = Configure.getDeviceType();
	private String url = Configure.getURL();
	private ProgressDialog  pdialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sign_in);
		
		
		SharedPreferences sp = getSharedPreferences("setup", 0);
		Boolean isRight = sp.getBoolean("signin_anim", true);

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
		
		btnSignin = (Button) findViewById(R.id.btn_sign_in);
		etUsername = (EditText)findViewById(R.id.et_email);
	    etPassword = (EditText)findViewById(R.id.et_password);
	    deviceID = Utils.getDeviceID(SignInActivity.this);   
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
		
		btnBack.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v)
			{
				SharedPreferences sp = getSharedPreferences("setup",0);
	    		SharedPreferences.Editor editor = sp.edit();
	    		editor.putBoolean("account_anim", false);
	    		editor.commit();
	    		
		        Animation anim = AnimationUtils.loadAnimation(SignInActivity.this,R.anim.out_to_right);
		        findViewById(R.id.toplayout).startAnimation(anim);
				Intent intent = new Intent();
				intent.setClass(SignInActivity.this, AccountActivity.class);
				startActivity(intent);
				SignInActivity.this.finish();
			}	    
	    }); 	
		
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
			 String debugResult = "{\"result\": \"success\"}";
		     return debugResult;
			 //return Utils.sendJson(urls[0], json);
		}

		protected void onProgressUpdate(Void... progress) {
			  pdialog = ProgressDialog.show(SignInActivity.this, null, "Please wait...", true, false);  
			
		}
		
		// onPostExecute displays the results of the AsyncTask.
		@Override
		protected void onPostExecute(String result) {
			 Log.d(TAG, "result:" + result);
			 if(result.trim().equals(""))
				 {
				 pdialog.dismiss();
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
				      intent.setClass(SignInActivity.this, Success2Activity.class);
					  startActivity(intent);
					  SignInActivity.this.finish();
				 }	
			     else{
					  Toast.makeText(getApplicationContext(), "Wrong account or password",
					  Toast.LENGTH_SHORT).show();
				}
			 }

		}
	}
	
	
}
