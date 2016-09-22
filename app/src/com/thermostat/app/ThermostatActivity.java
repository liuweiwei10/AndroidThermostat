package com.thermostat.app;

import java.text.SimpleDateFormat;

import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
/*import android.text.SpannableString;
 import android.text.style.UnderlineSpan;*/
import android.widget.TextView;
import android.provider.Settings.Secure;

import com.thermostat.app.data.Configure;
import com.thermostat.app.utils.Utils;

public class ThermostatActivity extends Activity {

	private final String TAG = "AndroidThermostat";
	private final String url = Configure.getURL();
	private final String DEVICE_TYPE = Configure.getDeviceType();
	private final CharSequence[] modeItems = { "Heat", "Cool", "Auto", "Off" };
	private final CharSequence[] fanItems = { "Auto", "On" };
	private final CharSequence[] schemeItems = { "Auto", "Manual" };
	private final double KTOC = 273.15;
	private final double CTOF1 = 1.8;
	private final double CTOF2 = 32.0;
	private final Boolean CELCIUS = false;
	private final Boolean FAHRENHEIT = true;
	private final String CELCIUS_SYMBOL = "°„C";
	private final String FAHRENHEIT_SYMBOL = "°„F";

	private static final int ID_MODE_HEAT = 1;
	private static final int ID_MODE_COOL = 2;
	private static final int ID_MODE_AUTO = 3;
	private static final int ID_MODE_OFF = 4;

	private static final int ID_FAN_AUTO = 1;
	private static final int ID_FAN_ON = 2;

	private static final int ID_SCHEME_AUTO = 1;
	private static final int ID_SCHEME_MANUAL = 2;

	private static final int msgKey1 = 1;

	private boolean isLocService;
	private boolean isAutoEnabled;
	private boolean isRule;
	private String username;
	private String address;
	private String thermoName;
	private String insideTempString;
	private String targetTempString;
	private String targetStr;
	private String schemeStr;
	private String modeStr;
	private String fanStr;
	private int insideTemp;
	private int targetTemp;
	private Button btnSettings;
	private Button btnIncTemp;
	private Button btnDecTemp;
	private Button btnMode;
	private Button btnFan;
	private Button btnScheme;
	private TextView tvWeather;
	private TextView tvTime;
	private TextView tvAddress;
	private TextView tvInsideTemp;
	private TextView tvTargetTemp;
	private String deviceID;
	private ImageView imgWeather;

	private String weatherStr;

	// private boolean firstFlag;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.thermostat);

		btnMode = (Button) this.findViewById(R.id.btn_mode);
		btnFan = (Button) this.findViewById(R.id.btn_fan);
		btnScheme = (Button) this.findViewById(R.id.btn_scheme);
		tvTime = (TextView) this.findViewById(R.id.tv_time);
		tvWeather = (TextView) this.findViewById(R.id.tv_weather);
		imgWeather = (ImageView) this.findViewById(R.id.img_weather);
		username = Utils.getUsername(this);

		SharedPreferences sp_setup = getSharedPreferences("setup",
				Activity.MODE_PRIVATE);
		Boolean isFirst = sp_setup.getBoolean("first_launch", false);
		if (isFirst) {
			AlertDialog.Builder builder = new AlertDialog.Builder(
					ThermostatActivity.this,
					AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
			builder.setTitle("Do you want to begin by heating or cooling?");
			final String[] type = { "Heating", "Cooling" };
			builder.setSingleChoiceItems(type, 0,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Toast.makeText(ThermostatActivity.this,
									"start with£∫" + type[which],
									Toast.LENGTH_SHORT).show();
						}
					});
			builder.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							SharedPreferences sp_setup = getSharedPreferences(
									"setup", Activity.MODE_PRIVATE);
							SharedPreferences.Editor ed_setup = sp_setup.edit();
							ed_setup.putBoolean("first_launch", false);
							ed_setup.commit();
						}
					});

			builder.show();
		}

		// show time on screen;
		new TimeThread().start();

		ActionItem modeHeat = new ActionItem(ID_MODE_HEAT, "Heat",
				getResources().getDrawable(R.drawable.ic_mode_heat));
		ActionItem modeCool = new ActionItem(ID_MODE_COOL, "Cool",
				getResources().getDrawable(R.drawable.ic_mode_cool));
		ActionItem modeAuto = new ActionItem(ID_MODE_AUTO, "Auto",
				getResources().getDrawable(R.drawable.ic_mode_auto));
		ActionItem modeOff = new ActionItem(ID_MODE_OFF, "Off", getResources()
				.getDrawable(R.drawable.ic_mode_off));

		ActionItem fanAuto = new ActionItem(ID_FAN_AUTO, "Auto", getResources()
				.getDrawable(R.drawable.ic_fan_auto));
		ActionItem fanOn = new ActionItem(ID_FAN_ON, "On", getResources()
				.getDrawable(R.drawable.ic_fan_on));

		ActionItem schemeAuto = new ActionItem(ID_SCHEME_AUTO, "Auto",
				getResources().getDrawable(R.drawable.ic_scheme_auto));
		ActionItem schemeManual = new ActionItem(ID_SCHEME_MANUAL, "Manual",
				getResources().getDrawable(R.drawable.ic_scheme_manual));

		final QuickAction qaMode = new QuickAction(this);
		final QuickAction qaFan = new QuickAction(this);
		final QuickAction qaScheme = new QuickAction(this);

		qaMode.addActionItem(modeHeat);
		qaMode.addActionItem(modeCool);
		qaMode.addActionItem(modeAuto);
		qaMode.addActionItem(modeOff);

		qaFan.addActionItem(fanAuto);
		qaFan.addActionItem(fanOn);

		qaScheme.addActionItem(schemeAuto);
		qaScheme.addActionItem(schemeManual);

		qaMode.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
			@Override
			public void onItemClick(QuickAction quickAction, int pos,
					int actionId) {
				ActionItem actionItem = quickAction.getActionItem(pos);

				if (actionId == ID_MODE_HEAT) {
					Drawable drawable = getResources().getDrawable(
							R.drawable.ic_mode_heat);
					drawable.setBounds(0, 0, drawable.getMinimumWidth(),
							drawable.getMinimumHeight());
					btnMode.setCompoundDrawables(drawable, null, null, null);
					btnMode.setText(modeItems[0]);
				} else if (actionId == ID_MODE_COOL) {
					Drawable drawable = getResources().getDrawable(
							R.drawable.ic_mode_cool);
					drawable.setBounds(0, 0, drawable.getMinimumWidth(),
							drawable.getMinimumHeight());
					btnMode.setCompoundDrawables(drawable, null, null, null);
					btnMode.setText(modeItems[1]);
				} else if (actionId == ID_MODE_AUTO) {
					Drawable drawable = getResources().getDrawable(
							R.drawable.ic_mode_auto);
					drawable.setBounds(0, 0, drawable.getMinimumWidth(),
							drawable.getMinimumHeight());
					btnMode.setCompoundDrawables(drawable, null, null, null);
					btnMode.setText(modeItems[2]);
				} else if (actionId == ID_MODE_OFF) {
					Drawable drawable = getResources().getDrawable(
							R.drawable.ic_mode_off);
					drawable.setBounds(0, 0, drawable.getMinimumWidth(),
							drawable.getMinimumHeight());
					btnMode.setCompoundDrawables(drawable, null, null, null);
					btnMode.setText(modeItems[3]);
				}
			}
		});

		qaFan.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
			@Override
			public void onItemClick(QuickAction quickAction, int pos,
					int actionId) {
				ActionItem actionItem = quickAction.getActionItem(pos);

				if (actionId == ID_FAN_AUTO) {
					Drawable drawable = getResources().getDrawable(
							R.drawable.ic_fan_auto);
					drawable.setBounds(0, 0, drawable.getMinimumWidth(),
							drawable.getMinimumHeight());
					btnFan.setCompoundDrawables(drawable, null, null, null);
					btnFan.setText(fanItems[0]);
				} else if (actionId == ID_FAN_ON) {
					Drawable drawable = getResources().getDrawable(
							R.drawable.ic_fan_on);
					drawable.setBounds(0, 0, drawable.getMinimumWidth(),
							drawable.getMinimumHeight());
					btnFan.setCompoundDrawables(drawable, null, null, null);
					btnFan.setText(fanItems[1]);
				}
			}
		});

		qaScheme.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
			@Override
			public void onItemClick(QuickAction quickAction, int pos,
					int actionId) {
				ActionItem actionItem = quickAction.getActionItem(pos);

				if (actionId == ID_SCHEME_AUTO) {
					Drawable drawable = getResources().getDrawable(
							R.drawable.ic_scheme_auto);
					drawable.setBounds(0, 0, drawable.getMinimumWidth(),
							drawable.getMinimumHeight());
					btnScheme.setCompoundDrawables(drawable, null, null, null);
					btnScheme.setText(schemeItems[0]);
				} else if (actionId == ID_SCHEME_MANUAL) {
					Drawable drawable = getResources().getDrawable(
							R.drawable.ic_scheme_manual);
					drawable.setBounds(0, 0, drawable.getMinimumWidth(),
							drawable.getMinimumHeight());
					btnScheme.setCompoundDrawables(drawable, null, null, null);
					btnScheme.setText(schemeItems[1]);
				}
			}
		});

		qaMode.setOnDismissListener(new QuickAction.OnDismissListener() {
			@Override
			public void onDismiss() {
			}
		});

		btnMode.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				qaMode.show(v);
			}
		});

		btnFan.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				qaFan.show(v);
			}
		});

		btnScheme.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				qaScheme.show(v);
			}
		});

		/*
		 * getStatus(); tvThermoName.setText(thermoName);
		 * tvAddress.setText(address);
		 * 
		 * deviceID = Utils.getDeviceID(ThermostatActivity.this);
		 */
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			Log.d("QuickAction", "onWindowFocusChanged");
			Log.d("QuickAction", "btnMode getTop=" + btnMode.getTop());
			Log.d("QuickAction", "btnMode getRight=" + btnMode.getRight());
			Log.d("QuickAction", "btnMode getLeft=" + btnMode.getLeft());
		}
	}

	/*
	 * get status in shared preference "share"
	 */
	private void getStatus() {
		SharedPreferences share = getSharedPreferences("share",
				Activity.MODE_PRIVATE);
		// get the status of location service
		isLocService = share.getBoolean("isLocService", false);
		isRule = share.getBoolean("isRule", false);
		targetStr = share.getString("targetTemp", "0°„F");
		schemeStr = share.getString("scheme", "Scheme:\nAuto");
		modeStr = share.getString("mode", "Mode:\nCool");
		fanStr = share.getString("fan", "Fan:\nAuto");

		SharedPreferences settings = getSharedPreferences("settings",
				Activity.MODE_PRIVATE);
		thermoName = settings.getString("thermo_name", "");
		address = settings.getString("address", "");
	}

	public class TimeThread extends Thread {
		@Override
		public void run() {
			do {
				try {
					WeatherTask task = new WeatherTask();
					task.execute("Tucson,US");
					int i;
					for(i =0; i < 60; i++) {
						Thread.sleep(1000);
						Message msg = new Message();
						msg.what = msgKey1;
						mHandler.sendMessage(msg);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} while (true);
		}
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case msgKey1:
				long sysTime = System.currentTimeMillis();
				CharSequence sysTimeStr = DateFormat.format(
						"MM/dd/yyyy \n HH:mm", sysTime);
				tvTime.setText(sysTimeStr);
				break;

			default:
				break;
			}
		}
	};

	private class WeatherTask extends AsyncTask<String, Void, Weather> {
		@Override
		protected Weather doInBackground(String... urls) {
			Weather weather = new Weather();
		    weather.weatherData =new WeatherHttpClient().getWeatherJson(urls[0]);
		    WeatherHttpClient weatherClient = new WeatherHttpClient();
			try {
				  weatherClient.parseWetherJson(weather.weatherData);
				  String imageId = weatherClient.getImageId();
				  weather.weatherImage =  weatherClient.getImage(imageId);;
				
			} catch (JSONException e) {				
				Log.e(TAG, "Error occurrs when parsing weather Json...");
				e.printStackTrace();
			}
		    return weather;
		}

		protected void onPostExecute(Weather weather) {
			WeatherHttpClient weatherClient = new WeatherHttpClient();
			Log.d("weather", "weatherData:" + weather.weatherData);
			Log.d("weather", "weatherImg:" + weather.weatherImage);
			try {
				weatherClient.parseWetherJson(weather.weatherData);
			} catch (JSONException e) {
				Log.e(TAG, "Error occurrs when parsing weather Json...");
				e.printStackTrace();
			}
			String outTempStr;
			String locStr;
			float outTemp = weatherClient.getTemp();
			locStr = weatherClient.getLocation();
			
			if(weather.weatherImage != null) {
				imgWeather.setImageBitmap(weather.weatherImage);
			}
			
			if (outTemp != 0) {
				SharedPreferences share = getSharedPreferences("share",
						Activity.MODE_PRIVATE);
				Boolean tempScale = share.getBoolean("temp_metric", true);
				int outTempInt;
				if (tempScale == CELCIUS) {
					outTempInt = (int) Math.round(outTemp - KTOC);
					outTempStr = Integer.toString(outTempInt) + CELCIUS_SYMBOL;
				} else {
					outTempInt = (int) Math.round((outTemp - KTOC) * CTOF1
							+ CTOF2);
					outTempStr = Integer.toString(outTempInt)
							+ FAHRENHEIT_SYMBOL;
				}
				if (locStr != null) {			
					weatherStr = outTempStr + ", " + locStr;
					tvWeather.setText(weatherStr);
				}
			}
		}
	}
	
	private class Weather {
	       private String weatherData;
	       private Bitmap weatherImage;
 	}

}
