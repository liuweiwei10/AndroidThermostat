package com.thermostat.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class HeatingActivity extends Activity{

	private final String TAG = "Thermostat";
	private ImageView imgEquip;
	private TextView tvInstalled;
	private ImageButton btnBack;
	private ImageButton btnNext;
	private Button btnBottom1;
	private Button btnBottom2;
	private Button btnBottom3;
	private Button btnBottom4;
	private Button btnBottom5;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.heating);
		
		SharedPreferences sp = getSharedPreferences("setup", 0);
		Boolean isRight = sp.getBoolean("heating_anim", true);
		
		SharedPreferences.Editor editor = sp.edit();
		editor.putBoolean("bottom3", true);
		editor.commit();
		
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
		
		imgEquip = (ImageView) findViewById(R.id.img_equipment);
		tvInstalled = (TextView) findViewById(R.id.tv_installed);
		btnBack = (ImageButton) findViewById(R.id.img_btn_back);
		btnNext= (ImageButton) findViewById(R.id.img_btn_next);
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


		imgEquip.setImageResource(getEquipSrc());
		tvInstalled.setText("Installed: " + getEquipInstalled());	 
		
		btnBack.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {		
				
				SharedPreferences sp = getSharedPreferences("setup",0);
	    		SharedPreferences.Editor editor = sp.edit();
	    		editor.putBoolean("account_anim", false);
	    		editor.commit();
	    		
		        Animation anim = AnimationUtils.loadAnimation(HeatingActivity.this,R.anim.out_to_right);
		        findViewById(R.id.toplayout).startAnimation(anim);

	    		Intent intent = new Intent();
			    intent.setClass(HeatingActivity.this, AccountActivity.class);
				startActivity(intent);
				//overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
				HeatingActivity.this.finish();
			}
    	});	
		
		btnNext.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {	
				
				SharedPreferences sp = getSharedPreferences("setup",0);
	    		SharedPreferences.Editor editor = sp.edit();
	    		editor.putBoolean("question1_anim", true);
	    		editor.commit();
	    		
		        Animation anim = AnimationUtils.loadAnimation(HeatingActivity.this,R.anim.out_to_left);
		        findViewById(R.id.toplayout).startAnimation(anim);

	    		Intent intent = new Intent();
			    intent.setClass(HeatingActivity.this, Question1Activity.class);
				startActivity(intent);
				//overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
				HeatingActivity.this.finish();
			}
    	});	
		
		btnBottom1.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				
				SharedPreferences sp = getSharedPreferences("setup", 0);
				SharedPreferences.Editor editor = sp.edit();
				editor.putBoolean("language_anim", false);
				editor.commit();
			
	    		Intent intent = new Intent();
			    intent.setClass(HeatingActivity.this, WelcomeActivity.class);		    
				startActivity(intent);
				HeatingActivity.this.finish();
			}
    	});	
    	
    	btnBottom2.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				
				SharedPreferences sp = getSharedPreferences("setup", 0);
				SharedPreferences.Editor editor = sp.edit();
				editor.putBoolean("internet_anim", false);
				editor.commit();
			
	    		Intent intent = new Intent();
			    intent.setClass(HeatingActivity.this, InternetActivity.class);
				startActivity(intent);
				HeatingActivity.this.finish();
			}
    	});
    	
    	btnBottom4.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				SharedPreferences sp = getSharedPreferences("setup", 0);
				SharedPreferences.Editor editor = sp.edit();
				editor.putBoolean("location_anim", true);
				editor.commit();
				
	    		Intent intent = new Intent();
			    intent.setClass(HeatingActivity.this, LocationActivity.class);
				startActivity(intent);
				HeatingActivity.this.finish();
			}
    	});
    	
    	btnBottom5.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				SharedPreferences sp = getSharedPreferences("setup", 0);
				SharedPreferences.Editor editor = sp.edit();
				editor.putBoolean("temperature_anim", true);
				editor.commit();
				
	    		Intent intent = new Intent();
			    intent.setClass(HeatingActivity.this, TemperatureActivity.class);
				startActivity(intent);
				HeatingActivity.this.finish();
			}
    	});
	}
    	
	private int getEquipSrc() {
		return R.drawable.equipments;
	}
	
	private String getEquipInstalled(){
		String equipInstalled = "Heating/Cooling/Fan";
		return equipInstalled;
	}
	
}
