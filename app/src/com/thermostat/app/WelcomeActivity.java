package com.thermostat.app;

import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import com.thermostat.app.MyAdapter;


public class WelcomeActivity extends Activity {
	
	private final String TAG = "Thermostat";
	private static final String[] languages = {"ENGLISH", "ESPANOL", "FRANCAIS"};
    private int cur_pos = 0;
    private ListView listview;
    private ImageButton btnNext;
    private Button btnBottom1;
    private Button btnBottom2;
    private Button btnBottom3;
    private Button btnBottom4;
    private Button btnBottom5;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome);
		
		SharedPreferences sp = getSharedPreferences("setup",0);
		Boolean isRight = sp.getBoolean("language_anim", true);
		
		SharedPreferences.Editor editor = sp.edit();
		editor.putBoolean("bottom1", true);
		editor.commit();
		
		if (!isRight) {
			//set animation: activity enter from right
			Animation anim = AnimationUtils.loadAnimation(this,
					R.anim.in_from_left);
			findViewById(R.id.toplayout).startAnimation(anim);
		}	
		
	    listview = (ListView) findViewById(R.id.list_language); 
	    btnNext = (ImageButton) findViewById(R.id.img_btn_next);
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
   
	    cur_pos = sp.getInt("language_pos", 0);
            
        final MyAdapter adapter = new MyAdapter(this, cur_pos, languages);  
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
        
    	btnNext.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				
				SharedPreferences sp = getSharedPreferences("setup",0);
	    		SharedPreferences.Editor editor = sp.edit();
	    		String language =listview.getItemAtPosition(cur_pos).toString();
	    		Log.d(TAG, "language:" + language);
	    		editor.putString("language", language);
	    		editor.putInt("language_pos",cur_pos);
	    		editor.putBoolean("internet_anim", true);
	    		editor.commit();
	    		
		        Animation anim = AnimationUtils.loadAnimation(WelcomeActivity.this,R.anim.out_to_left);
		        findViewById(R.id.toplayout).startAnimation(anim);
	    			        
	    		Intent intent = new Intent();
			    intent.setClass(WelcomeActivity.this, InternetActivity.class);
				startActivity(intent);
				//overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);  
			    WelcomeActivity.this.finish();
			}
    	});	 
    	
    	btnBottom2.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				
				SharedPreferences sp = getSharedPreferences("setup", 0);
				SharedPreferences.Editor editor = sp.edit();
				editor.putBoolean("internet_anim", true);
				editor.commit();
			
	    		Intent intent = new Intent();
			    intent.setClass(WelcomeActivity.this, InternetActivity.class);
				startActivity(intent);
			    WelcomeActivity.this.finish();
			}
    	});	
    	
    	btnBottom3.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				
				SharedPreferences sp = getSharedPreferences("setup", 0);
				SharedPreferences.Editor editor = sp.edit();
				editor.putBoolean("heating_anim", true);
				editor.commit();
				
	    		Intent intent = new Intent();
			    intent.setClass(WelcomeActivity.this, HeatingActivity.class);
				startActivity(intent);
			    WelcomeActivity.this.finish();		    
			}
    	});
    	
    	btnBottom4.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				
				SharedPreferences sp = getSharedPreferences("setup", 0);
				SharedPreferences.Editor editor = sp.edit();
				editor.putBoolean("location_anim", true);
				editor.commit();
				
	    		Intent intent = new Intent();
			    intent.setClass(WelcomeActivity.this, LocationActivity.class);
				startActivity(intent);
			    WelcomeActivity.this.finish();
			}
    	});
    	
    	btnBottom5.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				
				SharedPreferences sp = getSharedPreferences("setup", 0);
				SharedPreferences.Editor editor = sp.edit();
				editor.putBoolean("temperature_anim", true);
				editor.commit();
				
	    		Intent intent = new Intent();
			    intent.setClass(WelcomeActivity.this, TemperatureActivity.class);
				startActivity(intent);
			    WelcomeActivity.this.finish();
			}
    	});
	}

}
