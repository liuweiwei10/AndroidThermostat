package com.thermostat.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

public class SuccessActivity extends Activity {
	
	private final String TAG = "Thermostat";
	private Button btnBottom1;
	private Button btnBottom2;
	private Button btnBottom3;
	private Button btnBottom4;
	private Button btnBottom5;
	private Button btnSignIn;
	private Button btnSignInLater;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.success);
		
		SharedPreferences sp = getSharedPreferences("setup",0);
		SharedPreferences.Editor editor = sp.edit();
		editor.putBoolean("sign_up", true);
		editor.commit();
		
		btnSignIn = (Button) findViewById(R.id.btn_sign_in);
		btnSignInLater = (Button) findViewById(R.id.btn_sing_in_later);
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
		
		btnSignInLater.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				
				SharedPreferences sp = getSharedPreferences("setup",0);
	    		SharedPreferences.Editor editor = sp.edit();
	    		editor.putBoolean("heating_anim", true);
	    		editor.commit();
	    		
		        Animation anim = AnimationUtils.loadAnimation(SuccessActivity.this,R.anim.out_to_left);
		        findViewById(R.id.toplayout).startAnimation(anim);
				
				Intent intent = new Intent();
				intent.setClass(SuccessActivity.this, HeatingActivity.class);
				startActivity(intent);
				SuccessActivity.this.finish();
			}

		});
		
		btnSignIn.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {

				SharedPreferences sp = getSharedPreferences("setup", 0);
				SharedPreferences.Editor editor = sp.edit();
				editor.putBoolean("signin_anim", true);
				editor.commit();

				Animation anim = AnimationUtils.loadAnimation(
						SuccessActivity.this, R.anim.out_to_left);
				findViewById(R.id.toplayout).startAnimation(anim);

				Intent intent = new Intent();
				intent.setClass(SuccessActivity.this, SignInActivity.class);
				startActivity(intent);
				SuccessActivity.this.finish();
			}
		});
	}

}
