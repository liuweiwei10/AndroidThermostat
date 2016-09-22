import java.util.Date;

public class RawDataEntry {
	public Date date;
	private String provider;
	private double latitude;
	private double longitude;
	private double elevation;
	private double accuracy;
	private double bearing;
	private double speed;

	public RawDataEntry(Date date, String provider, double latitude, double longitude, double elevation, double accuracy, double bearing, double speed){
		this.date = date;
		this.provider = provider;
		this.latitude = latitude;
		this.longitude = longitude;
	    this.elevation = elevation;
	    this.accuracy = accuracy;
	    this.bearing = bearing;
	    this.speed = speed;
	}
	
	public Date getDate(){
		return date;
	}
	
	public String getProvider(){
		return provider;
	}
	
	public double getLatitude(){
		return latitude;
	}
	
	public double getElevation(){
		return elevation;
	}	
	
	public double getAccuracy(){
		return accuracy;
	}	
	
	public double getBearing(){
		return bearing;
	}
	
	
	public double getSpeed(){
		return speed;
	}
	
	
	public double getLongitude(){
		return longitude;
	}
	
	
	public void setDate(Date date){
		this.date = date;
	}
	
	public void getProvider(String provider){
		this.provider = provider;
	}
	
	public void setLatitude(double latitude){
		this.latitude = latitude;
	}
	
	public void setLongitude(double longitude ){
		this.longitude = longitude;
	}
	
	public void setElevation(double elevation){
		this.elevation = elevation;
	}	
	
	
	public void setBearing(double bearing){
		this.bearing = bearing ;
	}
	
	public void setSpeed(double speed){
		this.speed = speed;
	}
	
	

}
