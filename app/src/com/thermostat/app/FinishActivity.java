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

public class FinishActivity extends Activity {
	private final String TAG = "Thermostat";
	private Button btnFinish;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.finish);
		btnFinish = (Button) findViewById(R.id.btn_finish);
		btnFinish.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				SharedPreferences sp_setup = getSharedPreferences("setup", 0);
				SharedPreferences sp_settings = getSharedPreferences("settings", 0);
				
				SharedPreferences.Editor ed_settings = sp_settings.edit();
				ed_settings.putString("thermo_name", sp_setup.getString("thermo_name", ""));
			    ed_settings.putString("address", sp_setup.getString("address", ""));
			    ed_settings.commit();
			    
				SharedPreferences.Editor ed_setup = sp_setup.edit();				
				ed_setup.putBoolean("first_launch", true);
				ed_setup.commit();
				
				Intent intent = new Intent();
				intent.setClass(FinishActivity.this, ThermostatActivity.class);
				startActivity(intent);
				// overridePendingTransition(R.anim.in_from_right,
				// R.anim.out_to_left);
				FinishActivity.this.finish();
			}
		});
	}
}
