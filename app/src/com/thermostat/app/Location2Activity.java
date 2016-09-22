package com.thermostat.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class Location2Activity extends Activity {

	private final String TAG = "Thermostat";
	private ImageButton btnBack;
	private ImageButton btnNext;
	private EditText etThermoName;
	private Button btnBottom1;
	private Button btnBottom2;
	private Button btnBottom3;
	private Button btnBottom4;
	private Button btnBottom5;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.location2);

		SharedPreferences sp = getSharedPreferences("setup", 0);
		Boolean isRight = sp.getBoolean("location2_anim", true);

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
		btnNext = (ImageButton) findViewById(R.id.img_btn_next);
		etThermoName = (EditText) findViewById(R.id.et_thermo_name);
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

		String thermoName = sp.getString("thermo_name", "");
		etThermoName.setText(thermoName);

		btnBack.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {

				SharedPreferences sp = getSharedPreferences("setup", 0);
				SharedPreferences.Editor editor = sp.edit();
				String thermoName = etThermoName.getText().toString();
				editor.putString("thermo_name", thermoName);
				editor.putBoolean("location_anim", false);
				editor.commit();

				Animation anim = AnimationUtils.loadAnimation(
						Location2Activity.this, R.anim.out_to_right);
				findViewById(R.id.toplayout).startAnimation(anim);

				Intent intent = new Intent();
				intent.setClass(Location2Activity.this, LocationActivity.class);
				startActivity(intent);
				// overridePendingTransition(R.anim.in_from_left,
				// R.anim.out_to_right);
				Location2Activity.this.finish();
			}
		});
		btnNext.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				if (etThermoName.getText().toString().trim().equals("")) {
					  Toast.makeText(getApplicationContext(), "Please enter a name.", 
					  Toast.LENGTH_SHORT).show();	
				} else {

					SharedPreferences sp = getSharedPreferences("setup", 0);
					SharedPreferences.Editor editor = sp.edit();
					String thermoName = etThermoName.getText().toString();
					editor.putString("thermo_name", thermoName);
					editor.putBoolean("temperature_anim", true);
					editor.commit();

					Animation anim = AnimationUtils.loadAnimation(
							Location2Activity.this, R.anim.out_to_left);
					findViewById(R.id.toplayout).startAnimation(anim);

					Intent intent = new Intent();
					intent.setClass(Location2Activity.this,
							TemperatureActivity.class);
					startActivity(intent);
					// overridePendingTransition(R.anim.in_from_right,
					// R.anim.out_to_left);
					Location2Activity.this.finish();

				}
			}
		});

		btnBottom1.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {

				SharedPreferences sp = getSharedPreferences("setup", 0);
				SharedPreferences.Editor editor = sp.edit();
				editor.putBoolean("language_anim", false);
				editor.commit();

				Intent intent = new Intent();
				intent.setClass(Location2Activity.this, WelcomeActivity.class);
				startActivity(intent);
				Location2Activity.this.finish();
			}
		});

		btnBottom2.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {

				SharedPreferences sp = getSharedPreferences("setup", 0);
				SharedPreferences.Editor editor = sp.edit();
				editor.putBoolean("internet_anim", false);
				editor.commit();

				Intent intent = new Intent();
				intent.setClass(Location2Activity.this, InternetActivity.class);
				startActivity(intent);
				Location2Activity.this.finish();
			}
		});

		btnBottom3.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				SharedPreferences sp = getSharedPreferences("setup", 0);
				SharedPreferences.Editor editor = sp.edit();
				editor.putBoolean("heating_anim", false);
				editor.commit();

				Intent intent = new Intent();
				intent.setClass(Location2Activity.this, HeatingActivity.class);
				startActivity(intent);
				Location2Activity.this.finish();
			}
		});

		btnBottom5.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				SharedPreferences sp = getSharedPreferences("setup", 0);
				SharedPreferences.Editor editor = sp.edit();
				editor.putBoolean("temperature_anim", true);
				editor.commit();

				Intent intent = new Intent();
				intent.setClass(Location2Activity.this,
						TemperatureActivity.class);
				startActivity(intent);
				Location2Activity.this.finish();
			}
		});

	}
}
