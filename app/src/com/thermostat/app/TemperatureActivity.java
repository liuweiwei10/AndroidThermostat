package com.thermostat.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class TemperatureActivity extends Activity {

	private final String TAG = "Thermostat";
	private final int MAX_TEMP = 90;
	private final int MIN_TEMP = 55;
	private ImageButton btnBack;
	private ImageButton btnNext;
	private EditText etMaxTemp;
	private EditText etMinTemp;
	private Button btnBottom1;
	private Button btnBottom2;
	private Button btnBottom3;
	private Button btnBottom4;
	private Button btnBottom5;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.temperature);

		SharedPreferences sp = getSharedPreferences("setup", 0);
		Boolean isRight = sp.getBoolean("temperature_anim", true);

		SharedPreferences.Editor editor = sp.edit();
		editor.putBoolean("bottom5", true);
		editor.commit();

		if (isRight) {
			// set animation: activity enter from right
			Animation anim = AnimationUtils.loadAnimation(this,
					R.anim.in_from_right);
			findViewById(R.id.toplayout).startAnimation(anim);
		}

		btnBack = (ImageButton) findViewById(R.id.img_btn_back);
		btnNext = (ImageButton) findViewById(R.id.img_btn_next);
		etMaxTemp = (EditText) findViewById(R.id.et_max_temp);
		etMinTemp = (EditText) findViewById(R.id.et_min_temp);
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

		String maxTemp = sp.getString("max_temp", "");
		String minTemp = sp.getString("min_temp", "");
		etMaxTemp.setText(maxTemp);
		etMinTemp.setText(minTemp);

		btnBack.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {

				// check the if the input is a valid value of temperature

				SharedPreferences sp = getSharedPreferences("setup", 0);
				SharedPreferences.Editor editor = sp.edit();
				String maxTemp = etMaxTemp.getText().toString();
				String minTemp = etMinTemp.getText().toString();
				editor.putString("max_temp", maxTemp);
				editor.putString("min_temp", minTemp);
				editor.putBoolean("location2_anim", false);
				editor.commit();

				Animation anim = AnimationUtils.loadAnimation(
						TemperatureActivity.this, R.anim.out_to_right);
				findViewById(R.id.toplayout).startAnimation(anim);

				Intent intent = new Intent();
				intent.setClass(TemperatureActivity.this,
						Location2Activity.class);
				startActivity(intent);
				// overridePendingTransition(R.anim.in_from_left,
				// R.anim.out_to_right);
				TemperatureActivity.this.finish();
			}
		});
		btnNext.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				String strMin = etMinTemp.getText().toString();
				String strMax = etMaxTemp.getText().toString();
				if (strMin.trim().equals("")
						|| strMax.trim().equals("")) {
					Toast.makeText(getApplicationContext(),
							"Please enter the temperatures.",
							Toast.LENGTH_SHORT).show();
				} else {
					int minTemp = Integer.parseInt(strMin);
					int maxTemp = Integer.parseInt(strMax);

					if (minTemp > MAX_TEMP || minTemp < MIN_TEMP
							|| maxTemp > MAX_TEMP || maxTemp < MIN_TEMP) {
						Toast.makeText(
								getApplicationContext(),
								"A valid value of temperature should lie between 55 ¡ãF and 90 ¡ãF.",
								Toast.LENGTH_SHORT).show();
					} else if (minTemp > maxTemp) {
						Toast.makeText(
								getApplicationContext(),
								"The maximum temperature should be larger than the minimum temperture.",
								Toast.LENGTH_SHORT).show();
					} else {

						SharedPreferences sp = getSharedPreferences("setup", 0);
						SharedPreferences.Editor editor = sp.edit();
						editor.putString("max_temp", strMax);
						editor.putString("min_temp", strMin);
						editor.commit();

						Intent intent = new Intent();
						intent.setClass(TemperatureActivity.this,
								FinishActivity.class);
						startActivity(intent);
						overridePendingTransition(R.anim.in_from_right,
								R.anim.out_to_left);
						TemperatureActivity.this.finish();
					}
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
				intent.setClass(TemperatureActivity.this, WelcomeActivity.class);
				startActivity(intent);
				TemperatureActivity.this.finish();
			}
		});

		btnBottom2.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {

				SharedPreferences sp = getSharedPreferences("setup", 0);
				SharedPreferences.Editor editor = sp.edit();
				editor.putBoolean("internet_anim", false);
				editor.commit();

				Intent intent = new Intent();
				intent.setClass(TemperatureActivity.this,
						InternetActivity.class);
				startActivity(intent);
				TemperatureActivity.this.finish();
			}
		});

		btnBottom3.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				SharedPreferences sp = getSharedPreferences("setup", 0);
				SharedPreferences.Editor editor = sp.edit();
				editor.putBoolean("heating_anim", false);
				editor.commit();

				Intent intent = new Intent();
				intent.setClass(TemperatureActivity.this, HeatingActivity.class);
				startActivity(intent);
				TemperatureActivity.this.finish();
			}
		});

		btnBottom4.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				SharedPreferences sp = getSharedPreferences("setup", 0);
				SharedPreferences.Editor editor = sp.edit();
				editor.putBoolean("location_anim", false);
				editor.commit();

				Intent intent = new Intent();
				intent.setClass(TemperatureActivity.this,
						LocationActivity.class);
				startActivity(intent);
				TemperatureActivity.this.finish();
			}
		});

	}

}
