package com.thermostat.client;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class WelcomeActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SharedPreferences sp = getSharedPreferences("share",0);
    	Boolean isSignin =sp.getBoolean("isSignin",false);
    	if(isSignin)
    	{
			  Intent intent = new Intent();
		      intent.setClass(WelcomeActivity.this, ThermostatActivity.class);
			  startActivity(intent);
			  WelcomeActivity.this.finish();
    	}
    	else{
    		setContentView(R.layout.welcome);
    		Button btnSignin = (Button) findViewById(R.id.btnSignin);
    		Button btnRegister = (Button) findViewById(R.id.btnRegister);
    		btnSignin.setOnClickListener(new Button.OnClickListener() {
    			public void onClick(View v)
    			{
    				Intent intent = new Intent();
    				intent.setClass(WelcomeActivity.this, SigninActivity.class);
    				startActivity(intent);
    				WelcomeActivity.this.finish();
    			}	    
    	    });
    		btnRegister.setOnClickListener(new Button.OnClickListener() {
    			public void onClick(View v)
    			{
    				Intent intent = new Intent();
    				intent.setClass(WelcomeActivity.this, RegisterActivity.class);
    				startActivity(intent);
    				WelcomeActivity.this.finish();
    			}	    
    	    }); 	
    	}
   }
}
