package com.thermostat.client;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddThermostatActivity extends Activity{
	
	private final String TAG = "AddThermostatActivity";
	
	private Button btnSave;
	private EditText etThermoName;
	private EditText etThermoID;
	private String thermoName;
	private String thermoID;
	private String thermostats;
	private String thermoIDs;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.addthermostat);
		btnSave = (Button) findViewById(R.id.btnSaveThermostat);
		etThermoName = (EditText)findViewById(R.id.etThermostatName);
	    etThermoID = (EditText)findViewById(R.id.etThermostatID);
		btnSave.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v)
			{
				thermoName=etThermoName.getText().toString();
				thermoID=etThermoID.getText().toString();	
				if("".equals(thermoName.trim())  || "".equals(thermoID.trim()))
				{
					Log.i(TAG, "either name or ID is not input!");
				}
				else
				{
		    	SharedPreferences temp = getSharedPreferences("temp",0);
		    	thermostats =temp.getString("thermostats","");
		    	thermoIDs = temp.getString("thermoIDs", "");
		    	thermostats = thermostats + thermoName.trim() +"\n";
		    	thermoIDs = thermoIDs + thermoID.trim() + "\n";
		        SharedPreferences.Editor editor = temp.edit();
		        editor.putString("thermostats", thermostats);
		        editor.putString("thermoID",thermoID);
		        editor.commit();
			    Intent intent = new Intent();
				intent.setClass(AddThermostatActivity.this, AddHouseActivity.class);
			    startActivity(intent);
			    AddThermostatActivity.this.finish();
			    }
			}
	    });
		
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		 
        if (keyCode == KeyEvent.KEYCODE_BACK
                 && event.getRepeatCount() == 0) 
        {
        	SharedPreferences temp = getSharedPreferences("temp", Activity.MODE_PRIVATE);
			SharedPreferences.Editor tempEditor = temp.edit();
			tempEditor.putString("thermostats", "");
			tempEditor.putString("thermoIDs", "");
			tempEditor.commit();
			Intent intent = new Intent();
		    intent.setClass(AddThermostatActivity.this, AddHouseActivity.class);
		    startActivity(intent);
		    AddThermostatActivity.this.finish();
            return true;
         }
         return super.onKeyDown(keyCode, event);
     }
	
}
