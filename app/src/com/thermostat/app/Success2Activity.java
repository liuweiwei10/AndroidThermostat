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
import android.widget.TextView;

public class Success2Activity extends Activity {
	
	private final String TAG = "Thermostat";
	private Button btnBottom1;
	private Button btnBottom2;
	private Button btnBottom3;
	private Button btnBottom4;
	private Button btnBottom5;
	private Button btnContinue;
	private TextView tvSuccessInfo;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.success2);
		
		SharedPreferences sp = getSharedPreferences("setup",0);
		SharedPreferences.Editor editor = sp.edit();
		editor.putBoolean("sign_in", true);
		editor.commit();
			
		
		btnContinue = (Button) findViewById(R.id.btn_continue);
		tvSuccessInfo =(TextView) findViewById(R.id.tv_success_info);
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
		
		SharedPreferences share = getSharedPreferences("share",0);
		String username = share.getString("username", "");
		String info =this.getString(R.string.signin_info_1) + ": " + username + this.getString(R.string.signin_info_2);
		tvSuccessInfo.setText(info);
	
		btnContinue.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				
				SharedPreferences sp = getSharedPreferences("setup",0);
	    		SharedPreferences.Editor editor = sp.edit();
	    		editor.putBoolean("heating_anim", true);
	    		editor.commit();
	    		
		        Animation anim = AnimationUtils.loadAnimation(Success2Activity.this,R.anim.out_to_left);
		        findViewById(R.id.toplayout).startAnimation(anim);
				
				Intent intent = new Intent();
				intent.setClass(Success2Activity.this, HeatingActivity.class);
				startActivity(intent);
				Success2Activity.this.finish();
			}

		});
	}

}
