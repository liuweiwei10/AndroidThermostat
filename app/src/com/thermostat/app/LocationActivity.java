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
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class LocationActivity extends Activity{

	private final String TAG = "Thermostat";
	private ImageButton btnBack;
	private ImageButton btnNext;
	private AutoCompleteTextView actvAddress;;
	private Button btnBottom1;
	private Button btnBottom2;
	private Button btnBottom3;
	private Button btnBottom4;
	private Button btnBottom5;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.location);
		
		SharedPreferences sp = getSharedPreferences("setup", 0);
		Boolean isRight = sp.getBoolean("location_anim", true);
		
		SharedPreferences.Editor editor = sp.edit();
		editor.putBoolean("bottom4", true);
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

		btnBack = (ImageButton) findViewById(R.id.img_btn_back);
		btnNext= (ImageButton) findViewById(R.id.img_btn_next);
		actvAddress = (AutoCompleteTextView) findViewById(R.id.actv_address);
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
		
        String address = sp.getString("address", "");
        actvAddress.setText(address);
        
        actvAddress.setAdapter(new AutoPlaceAdapter(this, R.layout.list_item_autocomplete));
        
		btnBack.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {	
				
				SharedPreferences sp = getSharedPreferences("setup",0);
	    		SharedPreferences.Editor editor = sp.edit();
	    		String address = actvAddress.getText().toString();
	    		editor.putString("address",address);
	    		editor.putBoolean("question2_anim", false);
	    		editor.commit();
	    		
		        Animation anim = AnimationUtils.loadAnimation(LocationActivity.this,R.anim.out_to_right);
		        findViewById(R.id.toplayout).startAnimation(anim);
		        
	    		Intent intent = new Intent();
			    intent.setClass(LocationActivity.this, Question2Activity.class);
				startActivity(intent);
				//overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
				LocationActivity.this.finish();
			}
    	});	
		btnNext.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {	
				if (actvAddress.getText().toString().trim().equals("")) {
					  Toast.makeText(getApplicationContext(), "Please enter an address.", 
					  Toast.LENGTH_SHORT).show();	
				} else {
				SharedPreferences sp = getSharedPreferences("setup",0);
	    		SharedPreferences.Editor editor = sp.edit();
	    		String address = actvAddress.getText().toString();
	    		editor.putString("address",address);
	    		editor.putBoolean("location2_anim", true);
	    		editor.commit();
	    		
		        Animation anim = AnimationUtils.loadAnimation(LocationActivity.this,R.anim.out_to_left);
		        findViewById(R.id.toplayout).startAnimation(anim);

	    		Intent intent = new Intent();
			    intent.setClass(LocationActivity.this, Location2Activity.class);
				startActivity(intent);
				//overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
				LocationActivity.this.finish();
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
			    intent.setClass(LocationActivity.this, WelcomeActivity.class);		    
				startActivity(intent);
				LocationActivity.this.finish();
			}
    	});	
    	
    	btnBottom2.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				
				SharedPreferences sp = getSharedPreferences("setup", 0);
				SharedPreferences.Editor editor = sp.edit();
				editor.putBoolean("internet_anim", false);
				editor.commit();
			
	    		Intent intent = new Intent();
			    intent.setClass(LocationActivity.this, InternetActivity.class);
				startActivity(intent);
				LocationActivity.this.finish();
			}
    	});
    	
    	btnBottom3.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				SharedPreferences sp = getSharedPreferences("setup", 0);
				SharedPreferences.Editor editor = sp.edit();
				editor.putBoolean("heating_anim", false);
				editor.commit();
				
	    		Intent intent = new Intent();
			    intent.setClass(LocationActivity.this, HeatingActivity.class);
				startActivity(intent);
				LocationActivity.this.finish();
			}
    	});
    	
    	btnBottom5.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				SharedPreferences sp = getSharedPreferences("setup", 0);
				SharedPreferences.Editor editor = sp.edit();
				editor.putBoolean("temperature_anim", true);
				editor.commit();
				
	    		Intent intent = new Intent();
			    intent.setClass(LocationActivity.this, TemperatureActivity.class);
				startActivity(intent);
				LocationActivity.this.finish();
			}
    	});

	}
}
