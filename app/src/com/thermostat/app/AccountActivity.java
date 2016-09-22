package com.thermostat.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class AccountActivity extends Activity {

	private final String TAG = "Thermostat";
	private ImageButton btnBack;
	private Button btnSkip;
	private Button btnSignUp;
	private Button btnSignIn;
	private Button btnBottom1;
	private Button btnBottom2;
	private Button btnBottom3;
	private Button btnBottom4;
	private Button btnBottom5;
	private TextView tvSignIn;
	private TextView tvSignUp;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.account);

		SharedPreferences sp = getSharedPreferences("setup", 0);
		Boolean isRight = sp.getBoolean("account_anim", true);

		if (isRight) {
			// set animation: activity enter from right
			Animation anim = AnimationUtils.loadAnimation(this,
					R.anim.in_from_right);
			findViewById(R.id.toplayout).startAnimation(anim);
		} else {
			// set animation: activity enter from left
			Animation anim = AnimationUtils.loadAnimation(this,
					R.anim.in_from_left);
			findViewById(R.id.toplayout).startAnimation(anim);
		}

		btnBack = (ImageButton) findViewById(R.id.img_btn_back);
		btnSkip = (Button) findViewById(R.id.btn_skip);
		btnSignUp = (Button) findViewById(R.id.btn_sign_up);
		btnSignIn = (Button) findViewById(R.id.btn_sign_in);
		tvSignIn = (TextView) findViewById(R.id.tv_sign_in);
		tvSignUp = (TextView) findViewById(R.id.tv_sign_up);

		Boolean isSignedUp = sp.getBoolean("sign_up", false);
		Boolean isSignedIn = sp.getBoolean("sign_in", false);
		if (isSignedUp) {
			btnSignUp.setEnabled(false);
			btnSignUp.setTextColor(R.color.deep_gray);
		}
		if (isSignedIn) {
			Log.d(TAG,"AccountActivity:signed in");
			btnSignIn.setEnabled(false);
			btnSignIn.setTextColor(R.color.deep_gray);
			SharedPreferences share = getSharedPreferences("share", 0);
			String username = share.getString("username", "");
			if (!username.trim().equals("")) {
				tvSignUp.setText("You've signed up with account:" + username);
				tvSignUp.setTextColor(getResources().getColor(R.color.blue));
				tvSignIn.setText("");
			}
			btnSignUp.setEnabled(false);
			btnSignUp.setTextColor(R.color.deep_gray);
		}

		btnBottom1 = (Button) findViewById(R.id.btn_bottom1);
		btnBottom2 = (Button) findViewById(R.id.btn_bottom2);
		btnBottom3 = (Button) findViewById(R.id.btn_bottom3);
		btnBottom4 = (Button) findViewById(R.id.btn_bottom4);
		btnBottom5 = (Button) findViewById(R.id.btn_bottom5);

		btnBottom1.setEnabled(sp.getBoolean("bottom1", false));
		btnBottom2.setEnabled(sp.getBoolean("bottom2", false));
		btnBottom3.setEnabled(sp.getBoolean("bottom3", false));
		btnBottom4.setEnabled(sp.getBoolean("bottom4", false));
		btnBottom5.setEnabled(sp.getBoolean("bottom5", false));

		btnBack.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {

				SharedPreferences sp = getSharedPreferences("setup", 0);
				SharedPreferences.Editor editor = sp.edit();
				editor.putBoolean("internet_anim", false);
				editor.commit();

				Animation anim = AnimationUtils.loadAnimation(
						AccountActivity.this, R.anim.out_to_right);
				findViewById(R.id.toplayout).startAnimation(anim);

				Intent intent = new Intent();
				intent.setClass(AccountActivity.this, InternetActivity.class);
				startActivity(intent);
				// overridePendingTransition(R.anim.in_from_left,
				// R.anim.out_to_right);
				AccountActivity.this.finish();
			}
		});

		btnSkip.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {

				SharedPreferences sp = getSharedPreferences("setup", 0);
				SharedPreferences.Editor editor = sp.edit();
				editor.putBoolean("heating_anim", true);
				editor.commit();

				Animation anim = AnimationUtils.loadAnimation(
						AccountActivity.this, R.anim.out_to_left);
				findViewById(R.id.toplayout).startAnimation(anim);

				Intent intent = new Intent();
				intent.setClass(AccountActivity.this, HeatingActivity.class);
				startActivity(intent);
				// overridePendingTransition(R.anim.in_from_right,
				// R.anim.out_to_left);
				AccountActivity.this.finish();
			}
		});

		btnSignUp.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {

				SharedPreferences sp = getSharedPreferences("setup", 0);
				SharedPreferences.Editor editor = sp.edit();
				editor.putBoolean("signup_anim", true);
				editor.commit();

				Animation anim = AnimationUtils.loadAnimation(
						AccountActivity.this, R.anim.out_to_left);
				findViewById(R.id.toplayout).startAnimation(anim);

				Intent intent = new Intent();
				intent.setClass(AccountActivity.this, SignUpActivity.class);
				startActivity(intent);
				AccountActivity.this.finish();
			}
		});

		btnSignIn.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {

				SharedPreferences sp = getSharedPreferences("setup", 0);
				SharedPreferences.Editor editor = sp.edit();
				editor.putBoolean("signin_anim", true);
				editor.commit();

				Animation anim = AnimationUtils.loadAnimation(
						AccountActivity.this, R.anim.out_to_left);
				findViewById(R.id.toplayout).startAnimation(anim);

				Intent intent = new Intent();
				intent.setClass(AccountActivity.this, SignInActivity.class);
				startActivity(intent);
				AccountActivity.this.finish();
			}
		});

		btnBottom1.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {

				SharedPreferences sp = getSharedPreferences("setup", 0);
				SharedPreferences.Editor editor = sp.edit();
				editor.putBoolean("language_anim", false);
				editor.commit();

				Intent intent = new Intent();
				intent.setClass(AccountActivity.this, WelcomeActivity.class);
				startActivity(intent);
				AccountActivity.this.finish();
			}
		});

		btnBottom3.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {

				SharedPreferences sp = getSharedPreferences("setup", 0);
				SharedPreferences.Editor editor = sp.edit();
				editor.putBoolean("heating_anim", true);
				editor.commit();

				Intent intent = new Intent();
				intent.setClass(AccountActivity.this, HeatingActivity.class);
				startActivity(intent);
				AccountActivity.this.finish();
			}
		});

		btnBottom4.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				SharedPreferences sp = getSharedPreferences("setup", 0);
				SharedPreferences.Editor editor = sp.edit();
				editor.putBoolean("location_anim", true);
				editor.commit();

				Intent intent = new Intent();
				intent.setClass(AccountActivity.this, LocationActivity.class);
				startActivity(intent);
				AccountActivity.this.finish();
			}
		});

		btnBottom5.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				SharedPreferences sp = getSharedPreferences("setup", 0);
				SharedPreferences.Editor editor = sp.edit();
				editor.putBoolean("temperature_anim", true);
				editor.commit();

				Intent intent = new Intent();
				intent.setClass(AccountActivity.this, TemperatureActivity.class);
				startActivity(intent);
				AccountActivity.this.finish();
			}
		});

	}

}