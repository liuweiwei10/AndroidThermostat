package com.thermostat.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class Question2Activity extends Activity{
	
	private final String TAG = "Thermostat";
	private static final String[] heating_type = {"FORCED AIR", "IN-FLOOR RADIANT", "RADIATORS"};
	private ImageButton btnBack;
	private ImageButton btnNext;
    private ListView listview;
    private int cur_pos = 0;
	private Button btnBottom1;
	private Button btnBottom2;
	private Button btnBottom3;
	private Button btnBottom4;
	private Button btnBottom5;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.question2);
		
		SharedPreferences sp = getSharedPreferences("setup", 0);
		Boolean isRight = sp.getBoolean("question2_anim", true);

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
	    listview = (ListView) findViewById(R.id.list_answer);
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

        cur_pos = sp.getInt("question2_pos", 0);
		
		btnBack.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {	
				
				SharedPreferences sp = getSharedPreferences("setup",0);
	    		SharedPreferences.Editor editor = sp.edit();
	    		String heating =listview.getItemAtPosition(cur_pos).toString();
	    		Log.d(TAG, "heating type:" + heating);
	    		editor.putInt("question2_pos",cur_pos);
	    		editor.putBoolean("question1_anim", false);
	    		editor.commit();
	    		
		        Animation anim = AnimationUtils.loadAnimation(Question2Activity.this,R.anim.out_to_right);
		        findViewById(R.id.toplayout).startAnimation(anim);
	    		
	    		Intent intent = new Intent();
			    intent.setClass(Question2Activity.this, Question1Activity.class);
				startActivity(intent);
				//overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
				Question2Activity.this.finish();
			}
    	});	
		
		btnNext.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				SharedPreferences sp = getSharedPreferences("setup",0);
	    		SharedPreferences.Editor editor = sp.edit();
	    		String heating =listview.getItemAtPosition(cur_pos).toString();
	    		Log.d(TAG, "heating type:" + heating);
	    		editor.putInt("question2_pos",cur_pos);
	    		editor.putBoolean("location_anim", true);
	    		editor.commit();
	    		
		        Animation anim = AnimationUtils.loadAnimation(Question2Activity.this,R.anim.out_to_left);
		        findViewById(R.id.toplayout).startAnimation(anim);
	    		Intent intent = new Intent();
			    intent.setClass(Question2Activity.this, LocationActivity.class);
				startActivity(intent);
				//overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);  
				Question2Activity.this.finish();
			}
    	});	 
		
		btnBottom1.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				
				SharedPreferences sp = getSharedPreferences("setup", 0);
				SharedPreferences.Editor editor = sp.edit();
				editor.putBoolean("language_anim", false);
				editor.commit();
			
	    		Intent intent = new Intent();
			    intent.setClass(Question2Activity.this, WelcomeActivity.class);		    
				startActivity(intent);
				Question2Activity.this.finish();
			}
    	});	
    	
    	btnBottom2.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				
				SharedPreferences sp = getSharedPreferences("setup", 0);
				SharedPreferences.Editor editor = sp.edit();
				editor.putBoolean("internet_anim", false);
				editor.commit();
			
	    		Intent intent = new Intent();
			    intent.setClass(Question2Activity.this, InternetActivity.class);
				startActivity(intent);
				Question2Activity.this.finish();
			}
    	});
    	
    	btnBottom4.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				SharedPreferences sp = getSharedPreferences("setup", 0);
				SharedPreferences.Editor editor = sp.edit();
				editor.putBoolean("location_anim", true);
				editor.commit();
				
	    		Intent intent = new Intent();
			    intent.setClass(Question2Activity.this, LocationActivity.class);
				startActivity(intent);
				Question2Activity.this.finish();
			}
    	});
    	
    	btnBottom5.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				SharedPreferences sp = getSharedPreferences("setup", 0);
				SharedPreferences.Editor editor = sp.edit();
				editor.putBoolean("temperature_anim", true);
				editor.commit();
				
	    		Intent intent = new Intent();
			    intent.setClass(Question2Activity.this, TemperatureActivity.class);
				startActivity(intent);
				Question2Activity.this.finish();
			}
    	});
		
		final MyAdapter adapter = new MyAdapter(this, cur_pos, heating_type );  
        //final MyAdapter adapter = new MyAdapter(this);  
        listview.setAdapter(adapter);  
        listview.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listview.setOnItemClickListener(new OnItemClickListener() {  
            @Override  
            public void onItemClick(AdapterView<?> arg0, View arg1,  
                    int position, long id) { 
            	cur_pos = position;
                adapter.updateCurPos(cur_pos);
                adapter.notifyDataSetInvalidated();
            }  
        });
	
		
	}
}
