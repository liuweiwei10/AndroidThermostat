package com.thermostat.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
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
import android.widget.SimpleAdapter;

public class InternetActivity extends Activity {

	private final String TAG = "Thermostat";

	private ListView listview;
	private ImageButton btnBack;
	private ImageButton btnNext;
	private Button btnSkip;
	private String ssid;
	private int sigLevel;
	private int cur_pos = 0;
	private Button btnScan;
	private Button btnBottom1;
	private Button btnBottom2;
	private Button btnBottom3;
	private Button btnBottom4;
	private Button btnBottom5;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.internet);

		SharedPreferences sp = getSharedPreferences("setup", 0);
		Boolean isRight = sp.getBoolean("internet_anim", true);
		
		SharedPreferences.Editor editor = sp.edit();
		editor.putBoolean("bottom2", true);
		editor.commit();

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

		listview = (ListView) findViewById(R.id.list_wifi);
		btnBack = (ImageButton) findViewById(R.id.img_btn_back);
		btnNext = (ImageButton) findViewById(R.id.img_btn_next);
		btnSkip = (Button) findViewById(R.id.btn_skip);
		btnScan = (Button) findViewById(R.id.btn_scan);
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

		SimpleAdapter adapter = new SimpleAdapter(this, getWifiData(),
				R.layout.list_item2, new String[] { "title", "info", "img" },
				new int[] { R.id.title, R.id.info, R.id.img });
		listview.setAdapter(adapter);

		btnBack.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {

				// save wifi info here

				SharedPreferences sp = getSharedPreferences("setup", 0);
				SharedPreferences.Editor editor = sp.edit();
				editor.putBoolean("language_anim", false);
				editor.commit();

				Animation anim = AnimationUtils.loadAnimation(
						InternetActivity.this, R.anim.out_to_right);
				findViewById(R.id.toplayout).startAnimation(anim);

				Intent intent = new Intent();
				intent.setClass(InternetActivity.this, WelcomeActivity.class);
				startActivity(intent);
				// overridePendingTransition(R.anim.in_from_left,
				// R.anim.out_to_right);
				InternetActivity.this.finish();
			}
		});

		btnNext.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				SharedPreferences sp = getSharedPreferences("setup", 0);
				SharedPreferences.Editor editor = sp.edit();
				editor.putBoolean("account_anim", true);
				editor.commit();

				Animation anim = AnimationUtils.loadAnimation(
						InternetActivity.this, R.anim.out_to_left);
				findViewById(R.id.toplayout).startAnimation(anim);

				Intent intent = new Intent();
				intent.setClass(InternetActivity.this, AccountActivity.class);
				startActivity(intent);
				// overridePendingTransition(R.anim.in_from_right,
				// R.anim.out_to_left);
				InternetActivity.this.finish();
			}
		});

		btnSkip.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				SharedPreferences sp = getSharedPreferences("setup", 0);
				SharedPreferences.Editor editor = sp.edit();
				editor.putBoolean("account_anim", true);
				editor.commit();

				Animation anim = AnimationUtils.loadAnimation(
						InternetActivity.this, R.anim.out_to_left);
				findViewById(R.id.toplayout).startAnimation(anim);

				Intent intent = new Intent();
				intent.setClass(InternetActivity.this, AccountActivity.class);
				startActivity(intent);
				// overridePendingTransition(R.anim.in_from_right,
				// R.anim.out_to_left);
				InternetActivity.this.finish();
			}
		});
		
		btnScan.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				SimpleAdapter adapter = new SimpleAdapter(InternetActivity.this, getWifiData(),
						R.layout.list_item2, new String[] { "title", "info", "img" },
						new int[] { R.id.title, R.id.info, R.id.img });
				listview.setAdapter(adapter);
			}
		});
		
		btnBottom1.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				
				SharedPreferences sp = getSharedPreferences("setup", 0);
				SharedPreferences.Editor editor = sp.edit();
				editor.putBoolean("language_anim", false);
				editor.commit();
			
	    		Intent intent = new Intent();
			    intent.setClass(InternetActivity.this, WelcomeActivity.class);		    
				startActivity(intent);
				InternetActivity.this.finish();
			}
    	});	
    	
    	btnBottom3.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				
				SharedPreferences sp = getSharedPreferences("setup", 0);
				SharedPreferences.Editor editor = sp.edit();
				editor.putBoolean("heating_anim", true);
				editor.commit();
			
	    		Intent intent = new Intent();
			    intent.setClass(InternetActivity.this, HeatingActivity.class);
				startActivity(intent);
				InternetActivity.this.finish();
			}
    	});
    	
    	btnBottom4.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				SharedPreferences sp = getSharedPreferences("setup", 0);
				SharedPreferences.Editor editor = sp.edit();
				editor.putBoolean("location_anim", true);
				editor.commit();
				
	    		Intent intent = new Intent();
			    intent.setClass(InternetActivity.this, LocationActivity.class);
				startActivity(intent);
				InternetActivity.this.finish();
			}
    	});
    	
    	btnBottom5.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				SharedPreferences sp = getSharedPreferences("setup", 0);
				SharedPreferences.Editor editor = sp.edit();
				editor.putBoolean("temperature_anim", true);
				editor.commit();
				
	    		Intent intent = new Intent();
			    intent.setClass(InternetActivity.this, TemperatureActivity.class);
				startActivity(intent);
				InternetActivity.this.finish();
			}
    	});
	}

	private List<HashMap<String, Object>> getWifiData() {
		List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
		WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		List<ScanResult> wifiList = wifiManager.getScanResults();

		if (wifiList != null) {
			
			//sort the wifi list by signal strength
			Comparator<ScanResult> comparator = new Comparator<ScanResult>() {
		        @Override
		        public int compare(ScanResult result2, ScanResult result1) {
		            return (result2.level <result1.level ? 1 : (result2.level==result1.level ? 0 : -1));
		        }
		     };
		     
		     Collections.sort(wifiList, comparator);
		     
			int listSize = wifiList.size();
			for (ScanResult scanResult : wifiList) {
				sigLevel = WifiManager
						.calculateSignalLevel(scanResult.level, 4);
				ssid = scanResult.SSID;
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("title", ssid);
				boolean secured = false;
				if (scanResult.capabilities.contains("WEP")) {
					secured = true;
					map.put("info", "Secured with WEP");
				} else if (scanResult.capabilities.contains("WPA")) {
					secured = true;
					map.put("info", "Secured with WPA");
				} else if (scanResult.capabilities.contains("EAP")) {
					secured = true;
					map.put("info", "Secured with EAP");
				} else {
					map.put("info", "");
				}
				if (secured) {
					switch (sigLevel) {
					case 0:
						map.put("img", R.drawable.wifi_sig_0s);
						break;
					case 1:
						map.put("img", R.drawable.wifi_sig_1s);
						break;
					case 2:
						map.put("img", R.drawable.wifi_sig_2s);
						break;
					case 3:
						map.put("img", R.drawable.wifi_sig_3s);
						break;
					}
				} else {
					switch (sigLevel) {
					case 0:
						map.put("img", R.drawable.wifi_sig_0n);
						break;
					case 1:
						map.put("img", R.drawable.wifi_sig_1n);
						break;
					case 2:
						map.put("img", R.drawable.wifi_sig_2n);
						break;
					case 3:
						map.put("img", R.drawable.wifi_sig_3n);
						break;
					}
				}

				list.add(map);

			}
		} else {
			// handle the situation of no wifi signal found
		}

		return list;

	}
}
