package com.thermostat.client;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.thermostat.client.utils.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class AddHouseActivity extends Activity {
	
	private final String TAG = "AddHouseActivity";
	
	private Button btnAddThermostat; 
	private Button btnSave; 
	private EditText etHouseName;
	private TextView tvThermostats;
	private EditText etAddress;
	private CheckBox cbCurAddress;
	private String houseName;
	private String thermostats;
	private String thermoName;
	private String thermoIDs;
	private String address;
	private String provider;
	private boolean isCurAddrChecked;
	private SharedPreferences sp;
	private SharedPreferences temp;
	private double lat;
	private double lng;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.addhouse);
		btnAddThermostat = (Button) findViewById(R.id.btnAddThermostat);
		btnSave = (Button) findViewById(R.id.btnSaveHouse);
		tvThermostats = (TextView)findViewById(R.id.tvThermostats);
		etHouseName = (EditText) findViewById(R.id.etHouseName);
		etAddress = (EditText) findViewById(R.id.etAddress);
		cbCurAddress = (CheckBox) findViewById(R.id.cbCurAddress);
				
		temp= getSharedPreferences("temp", Activity.MODE_PRIVATE);
		thermoIDs = temp.getString("thermoIDs", "");
        thermostats =temp.getString("thermostats","");
        houseName = temp.getString("houseName", "");
        address = temp.getString("address", "");
        isCurAddrChecked = temp.getBoolean("curAddr", false);        
        etHouseName.setText(houseName);
		tvThermostats.setText(thermostats);
		etAddress.setText(address);
		cbCurAddress.setChecked(isCurAddrChecked);
		provider = Utils.chooseProvider(this);

	     //locationManager.requestLocationUpdates(provider, 1000, 0, locationListener);
		
		
		cbCurAddress.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if(cbCurAddress.isChecked())
        		{
        			SharedPreferences actState = getSharedPreferences("temp",0);
        			SharedPreferences.Editor editor = actState.edit();
        			editor.putBoolean("curAddr", true);
        			String context = Context.LOCATION_SERVICE;        		    	  
        		    LocationManager  locationManager=(LocationManager)getSystemService(context);
        		    Location location = locationManager.getLastKnownLocation(provider);
        		    lat=location.getLatitude();
        	        lng=location.getLongitude();	
        			address = getAddress(lat, lng, false);
        			if(!"".equals(address.trim()))
        			{
        				etAddress.setText(address);
        				editor.putString("address", address);
        				editor.putString("latitude", Double.toString(lat));
        				editor.putString("longitude", Double.toString(lng));
        				editor.commit();
        			}
        			
        		}
				else{
					etAddress.setText("");
					SharedPreferences actState = getSharedPreferences("temp",0);
        			SharedPreferences.Editor editor = actState.edit();
        			editor.putBoolean("curAddr",false);
        			editor.putString("address", "");
        			editor.commit();
				}
					
			}
        });
		
		
		btnAddThermostat.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v){
				houseName = etHouseName.getText().toString();
				address = etAddress.getText().toString();
				Boolean isChecked = cbCurAddress.isChecked();
				temp = getSharedPreferences("temp", Activity.MODE_PRIVATE);
				SharedPreferences.Editor editor = temp.edit();
			    editor.putString("houseName", houseName);
			    editor.putString("address", address);
			    editor.putBoolean("curAddr", isChecked);
			    editor.commit();
				Intent intent = new Intent();
			    intent.setClass(AddHouseActivity.this, AddThermostatActivity.class);
			    startActivity(intent);
			    AddHouseActivity.this.finish();
			}
		});	
		
		btnSave.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v){
				if(!"".equals(address.trim()))
				{
					sp = getSharedPreferences("share", Activity.MODE_PRIVATE);
					SharedPreferences.Editor spEditor = sp.edit();
					spEditor.putString("houseName", houseName);
					spEditor.putString("thermostats", thermostats);
					spEditor.putString("thermoIDs", thermoIDs);
					spEditor.putString("address", address);
					spEditor.commit();
					temp = getSharedPreferences("temp", Activity.MODE_PRIVATE);
					SharedPreferences.Editor tempEditor = temp.edit();
					tempEditor.putString("houseName", "");
					tempEditor.putString("thermostats", "");
					tempEditor.putString("thermoIDs", "");
					tempEditor.putBoolean("curAddr", false);
					tempEditor.putString("address", "");
					tempEditor.commit();
					Intent intent = new Intent();
				    intent.setClass(AddHouseActivity.this, ThermostatActivity.class);
				    startActivity(intent);
				    AddHouseActivity.this.finish();
				}
				else 
			    {
					Log.i(TAG, "please input the address!");
				}									
			}
		});		
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		 
        if (keyCode == KeyEvent.KEYCODE_BACK
                 && event.getRepeatCount() == 0) 
        {
        	temp = getSharedPreferences("temp", Activity.MODE_PRIVATE);
			SharedPreferences.Editor tempEditor = temp.edit();
			tempEditor.putString("houseName", "");
			tempEditor.putString("thermostats", "");
			tempEditor.putString("thermoIDs", "");
			tempEditor.putBoolean("curAddr", false);
			tempEditor.putString("address","");
			tempEditor.commit();
			Intent intent = new Intent();
		    intent.setClass(AddHouseActivity.this, ThermostatActivity.class);
		    startActivity(intent);
		    AddHouseActivity.this.finish();
            return true;
         }
         return super.onKeyDown(keyCode, event);
     }
	
	private String getAddress(double latitude, double longitude, boolean flag) {
		String addressString = "";
		Geocoder gc = new Geocoder(this, Locale.getDefault());
		try {
			// get address information
			List<Address> addresses = gc
					.getFromLocation(latitude, longitude, 1);
			StringBuilder sb = new StringBuilder();
			if (addresses.size() > 0) {
				Address address = addresses.get(0);
				int i;
				for (i = 0; i < address.getMaxAddressLineIndex()-1; i++){
					sb.append(address.getAddressLine(i)).append(",");
				}
				sb.append(address.getAddressLine(i));
					
			    if(flag){
					sb.append(address.getCountryName()).append("\n");
					addressString = sb.toString();
			    }
			    else 
			    	addressString = sb.toString();			    	
			}
		} catch (IOException e) {
			Log.e("TAG", "IOException when getting address");
		}
		return addressString;
	}
    
/*		private final LocationListener locationListener=new LocationListener()
		{
			
		        public void onLocationChanged(Location location)
		        {
		        	if(location!=null)
				     {
				            //get the coordinates of latitude and longitude
				            lat=location.getLatitude();
				            lng=location.getLongitude();			            
				     }
		        }
		        
		        public void onProviderDisabled(String provider){}
		        public void onProviderEnabled(String provider){}
		        public void onStatusChanged(String provider,int status,Bundle extras){}
		 };*/
		 
}
