package com.thermostat.client.datalearning;

	import java.io.File;
	import java.io.FileNotFoundException;
	import java.io.FileOutputStream;
	import java.io.IOException;
	import java.text.SimpleDateFormat;
	import java.util.Date;
	import java.util.List;
	import java.util.Locale;
	import java.lang.Math;

	import android.app.Activity;
	import android.app.Service;
	import android.content.Context;
	import android.content.Intent;
	import android.location.Address;
	import android.location.Geocoder;
	import android.location.Location;
	import android.location.LocationListener;
	import android.location.LocationManager;
	import android.os.Bundle;
	import android.os.Environment;
	import android.os.IBinder;
	import android.util.Log;
	import com.thermostat.client.utils.Utils;;


	public class LocationService extends Service {
		
		private static final String TAG="LocationService";
		private static final int TRESHOLD_GPS=30;
		private static final int TRESHOLD_NETWORK=50; 
		private static final double R=6367444.7; 
		private double lastLat;
		private double lastLng;
		private double lat;
		private double lng;
		private String coordinateString;
		private String lastCoorStr;
		private Date curDate;	 
		private Date lastDate;
		private LocationManager locationManager;
	    private String provider;
	    private String timeString;	 
	    private static final long MAX_INTERVAL= 3600*1000;
	    private int interval = 5*60*1000;
	    
		
		public IBinder onBind(Intent arg0)
		{
			return null;
		}	
		
		public int onStartCommand(Intent intent, int flags, int startId)
		{
			super.onStartCommand(intent, flags, startId);
	        String context = Context.LOCATION_SERVICE;
	        provider = Utils.chooseProvider(this);	  
	        locationManager=(LocationManager)getSystemService(context);
	        //Register for location updates minimum time interval of 
	        locationManager.requestLocationUpdates(provider, interval, 0, locationListener);
	        Log.i(TAG, "the interval is " + interval);
			Log.i(TAG, "Recording service has started");
			return START_STICKY;
		}

		private void updateWithNewLocation(Location location) 
		{	 
			//Log.i(TAG,"entered updateWithNewLocation");
		     //get the current time	    
			SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");       
		     curDate=new Date(System.currentTimeMillis()); 
		     timeString=formatter.format(curDate);    
		     
		     if(location!=null)
		     {
		            //get the coordinates of latitude and longitude
		            lat=location.getLatitude();
		            lng=location.getLongitude();
		            Log.i(TAG, "Current coordinate:" + lat +"," + lng);
		            
		            //if last coordinate exists, compare with last coordinate
		            // to determine whether to save it as a new record,
		            //otherwise save it as a new record directly.	            
		            if(lastCoorStr!=null)
		            {
		            	double distance=calDistance(lastLat, lastLng, lat, lng);
		            	if(provider=="gps")
		            	{
		            		if(distance>TRESHOLD_GPS)
		            		{
		            			saveRecord(lat,lng); 
		            			Log.i(TAG, "distance= " + distance + ", provider=" + provider+". The record is saved");
		            			lastDate=curDate;		
		            		     
		            		}
		            		else
		            		{
		            			//Log.i(TAG, "curDate:"+curDate.getTime()+", lastDate:" +lastDate.getTime());
		            			Log.i(TAG, "time difference:" + (curDate.getTime()-lastDate.getTime())/1000 +" s."); 
		            			if((curDate.getTime()-lastDate.getTime())>= MAX_INTERVAL)
		            			 {
		            				 saveRecord(lat,lng);
		            				 Log.i(TAG, "The time difference is more than one hour. distance=" + distance + " provider=" + provider + ". The record is saved");
		            				 lastDate=curDate;		
		            			 }
		            			 else
		            			 {
			            			 Log.i(TAG, "distance= " + distance + ", provider=" + provider+". Consider it as no location change");	            				 
		            			 }
		            		}
		            	}
		            	if(provider=="network")
		            	{
		            		if(distance>TRESHOLD_NETWORK)
		            		{
		            			 saveRecord(lat,lng);
		            		     Log.i(TAG, "distance= " + distance + ", provider=" + provider+". The record is saved");
		            			 lastDate=curDate;		
		            		}
		            		else
		            		{
		            			 //Log.i(TAG, "curDate:"+curDate.getTime()+", lastDate:" +lastDate.getTime());
		            			 Log.i(TAG, "time difference:" + (curDate.getTime()-lastDate.getTime())/1000 +" s."); 
		            			 if((curDate.getTime()-lastDate.getTime())>= MAX_INTERVAL)
		            			 {
		            				 saveRecord(lat,lng);
		            				 Log.i(TAG, "The time difference is more than one hour. distance=" + distance + " provider=" + provider + "The record is saved");
		            				 lastDate=curDate;		
		            			 }
		            			 else
		            			 {
			            			 Log.i(TAG, "distance= " + distance + ", provider=" + provider+". Consider it as no location change");	            				 
		            			 }
		            		}
		            	}	           	
		            }	       
		            else 
		            {
		            	saveRecord(lat,lng);
		            	Log.i(TAG,"This is the first record, no need to compare. The record is saved.");
		            	lastDate=curDate;		
		            }
		     }
		     else
		     {
	             Log.i(TAG, "No coordinates obtained.");
		    	 //if last record has coordinates, save this record. 
		         if (lastCoorStr=="Could not find the coordinates.\n")
		         {	        	
		        	 if((curDate.getTime()-lastDate.getTime())>= MAX_INTERVAL)
	    			 {
	    				 saveRecord(lat,lng);
	    				 Log.i(TAG, "The time difference is more than one hour. Although it doesn't include a coordinate, the record is saved.");
	    			 }
	    			 else
	    			 {
	    				 Log.i(TAG,"Both this and last record don't include coordinate, discard this record.");	            				 
	    			 }	        		        	 
		         }
		         else 
		         {
		        	 saveRecordWithNoCoor();
		        	 Log.i(TAG,"The record is saved although it doesn't include a coordinate.");
		         }
		     }
		}
		
		private double calDistance(double lastLat, double lastLng, double lat, double lng)
		{
			double distance=Math.acos(Math.cos(Math.toRadians(lastLat))*Math.cos(Math.toRadians(lat))*Math.cos(Math.toRadians(lastLng-lng))+Math.sin(Math.toRadians(lastLat))*Math.sin(Math.toRadians(lat)))*R;
		    Log.i(TAG,"The distance between (" + lastLat + "," + lastLng + ") and (" + lat + "," + lng + ") is " + distance);
			return distance;
		}
		
		private void saveRecordWithNoCoor()
		{          
			 String addressString="Could not find the address\n\n";
	         coordinateString="Could not find the coordinates.\n";
	         String locString=timeString + " " + "From" + " " + provider + "\n"+coordinateString+"\n"+addressString;
	         saveToTxt(locString);
	         lastCoorStr=coordinateString;
		}
		
		private void saveRecord(double latitude, double longitude)
		{
		     String locString=getRecord(latitude,longitude);
			 saveToTxt(locString);
			 lastLat = latitude;
			 lastLng = longitude;
			 lastCoorStr = coordinateString; 
	    }
		
		
		private String getRecord(double latitude, double longitude)
		{   
		     String addressString="Could not find the address\n\n";       
	         coordinateString="latitude:"+latitude+"\nlongitude:"+longitude;
	         Geocoder gc=new Geocoder(this,Locale.getDefault());
	         try
	         {
	        	 //get address information
	             List<Address> addresses=gc.getFromLocation(latitude, longitude,1);
	             StringBuilder sb=new StringBuilder();
	             if(addresses.size()>0)
	             {
	            	 Address address=addresses.get(0);
	            	 for(int i=0;i<address.getMaxAddressLineIndex();i++)
	                 sb.append(address.getAddressLine(i)).append("\n");	                        
	            	 sb.append(address.getCountryName()).append("\n").append("\n");
	            	 addressString=sb.toString();
	             }
	         }catch(IOException e)
	         {
	        	 Log.e("TAG", "IOException when getting address");
	         }
	         String locString=timeString + " " + "From" + " " + provider + "\n"+coordinateString+"\n"+addressString;
	         return locString;
		}


		private final LocationListener locationListener=new LocationListener()
		{
			
		        public void onLocationChanged(Location location)
		        {
		        	updateWithNewLocation(location);
		        }
		        
		        public void onProviderDisabled(String provider)
		        {
		        	updateWithNewLocation(null);
		        }

		        public void onProviderEnabled(String provider){}
		        public void onStatusChanged(String provider,int status,Bundle extras){}
		 };
		 
		//save string to /sdcard/location.txt 
		boolean saveToTxt(String string)
		{
	        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){ 
	            try {
	            	File sdCardDir = Environment.getExternalStorageDirectory();          
	                File resultFile = new File(sdCardDir, "locationService.txt");
	                FileOutputStream outStream = new FileOutputStream(resultFile, true);
	                outStream.write(string.getBytes());
	                outStream.close();
			    }
	            catch (FileNotFoundException e) {
	                return false;
	            }
	            catch (IOException e){
	                return false;
	            }
	        }
	        else{
	        	return false;
	        }
			return true;
		}	
		    
		protected boolean isRouteDisplayed()
		{
				return false;
		}

		public void onDestroy()
		{
			super.onDestroy();
			locationManager.removeUpdates(locationListener);
		}
	}

