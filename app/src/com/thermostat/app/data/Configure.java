package com.thermostat.app.data;

public class Configure {
	private final static String url = "http://216.224.167.153:8000/";
	private final static String DEVICE_TYPE = "phone";
	public static String getURL()
	{
		return url;
	}
	public static String getDeviceType()
	{
		return DEVICE_TYPE;
	}
	
}
