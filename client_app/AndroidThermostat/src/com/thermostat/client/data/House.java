package com.thermostat.client.data;

public class House {
	
	private String name;
	private String address;
	private int thermoCount=0;
	private String[] thermostats;
	
	public String getName(){
		return name;
	}
	
	public String getAddress(){
		return address;		
	}
	
	public int getThermoCount(){
		return thermoCount;
	}
	
	public String[] getThermostats(){
		return thermostats;
	}
	
	public void setName(String string){
		name = string;
	}
	
	public void setAddress(String string){
		address=string;
	}
	
	public void addThermostat(String string){
		thermostats[thermoCount]=string;
		thermoCount = thermoCount+1;		
	}		
}
