import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Runtime {
	private static String mode = "";

	private static int t_out = 40;
	private static int t_base = 72;
	private static String scale = "F";

	private static int deadband = 2;
	private static float p1 = 0.05f;
	private static float p2 = 10.00f;

	private static float area = 100.00f;
	private static float height = 3.00f;
	private static float air_density = 1.2745f;
	private static float air_heat_capacity = 0.718f;
	
	private static float power = 1000;
	private static String model = "linear";
	
	private static int pre_temp;// temperature of the previous entry
	private static int cur_temp; // temperature of the current entry
	private static int nxt_temp; // temperature of the next entry
	private static float sum_runtime = 0; //total furnace runtime for the input schedule
	private static float runtime_schedule = 0; // furnace runtime per day with schedule
	private static float runtime_baseline = 0; //furnace runtime per day with baseline 
	private static float total_time = 0; //total time of the schedule
	private static float cur_duration; //time duration for the current entry
	private static float nxt_duration; //time duration for the next entry
	private static int count=0; // keeping track of the number of entries
	private static float remaining_heating_time = 0;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String filename = "";
        
		mode = getModeFromInput();
		
        // get config filename
		if(mode.equalsIgnoreCase("")){
			System.out.println("can't extract mode from input file.");
			return;
		} else if (mode.equalsIgnoreCase("heat")) {
			filename = "config_heating.txt";
		} else if (mode.equalsIgnoreCase("cooling")) {
			filename = "config_cooling.txt"; 
		}
			
		// read parameters from config file
		if (!filename.trim().equalsIgnoreCase(""))
			readConfig(filename);
       
		handleInput();
		//System.out.println("area:" + area);
		//System.out.println("air density:" + air_density);
		
		//calculate baseline runtime 
		runtime_baseline = computeMaintainRuntime(t_base, 24);
		System.out.println("baseline runtime:" + runtime_baseline );
		System.out.println("ratio:" + (runtime_baseline - runtime_schedule)/runtime_baseline);
	}
	
	/**
	 * calculate the furnace runtime of maintaining @param temperature for @param duration hours.
	 * @param temperature
	 * @param duration
	 * @return
	 */
	public static float computeMaintainRuntime(float temperature, float duration) {
		float runtime;
		float offtime;
		if(mode.equalsIgnoreCase("heat")){
		 offtime = computeOffTime(temperature + deadband/2, -deadband);
		} else {
		 offtime = computeOffTime(temperature + deadband/2, deadband);
		}
		float ontime = computeHeatCoolRuntime(deadband);
		float ratio = ontime/(offtime + ontime);
		runtime = ratio * duration;
		return runtime;
	}
	
	/**
	 * calculate the furnace runtime for heat up/cool down the house by @param tempDiff degree
	 * @param tempDiff
	 * @return
	 */
	public static float computeHeatCoolRuntime(float tempDiff) {
		float runtime;
		//System.out.println("model:" + model);
		if(model.trim().equalsIgnoreCase("linear")) {
			runtime = p1 * tempDiff;
		} else {
			float tempDiffInC = tempDiff * 5/9;
			float q = area * height * air_density * air_heat_capacity * tempDiffInC;
			runtime = (q/(power/1000))/3600;
		}		
		return runtime;
	}
	
	
	/**
	 * calculate the time it takes for the @param temperature of the house changing by @param x degrees, in the case that hvac is off
	 * 
	 * @param temperature
	 * @param x
	 * @return sum
	 */
	public static float computeOffTime(float temperature, float x) {
		//System.out.println("droptime: temperature= " + temperature + ", x = " + x);
		double offtime = 0;
		offtime = -p2 * Math.log(Math.abs(temperature + x - t_out)/Math.abs(temperature - t_out));
		
/*		if (x<=0) 
			return -1; 
		else {
			for(int i = 0; i<x; i++) {
				float change = temperature - t_out -i;
				offtime += p2/change;
			}
		}*/
		
			return (float)offtime;
		}
	
	/**
	 * calculate the temperature change in a given period of time when the hvac is off
	 * need to handle cooling case...................
	 * @param initTemp
	 * @param time
	 * @return
	 */
	public static float computeTempChangeInGivenTime(float initTemp, float time) {
		double tempChange;
		System.out.println("initTemp:" + initTemp + "; T_out:" + t_out + "; time :" +time);
		tempChange = (initTemp - t_out)* (1-Math.pow(Math.E, (-time/p2))); //verify the correctness
		return (float) tempChange;
		
	}

	/**
	 * read parameters from config file 
	 * @param filename 
	 */
	public static void readConfig(String filename) {
		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
			String line;
			while ((line = br.readLine()) != null) {
				// ignore comment lines and empty lines
				if (line.startsWith("/*") || line.trim().equalsIgnoreCase("")) {
					continue;
				} else {
					// extract the key from the line
					int offset = line.indexOf("=");
					char[] key = new char[offset];
					line.getChars(0, offset, key, 0);
					String keyname = new String(key);

					// extract the value from the line
					String value;
					int comment_index = line.indexOf("//");
					if (comment_index == -1) {
						value = line.substring(offset + 1);
					} else {
						value = line.substring(offset + 1, comment_index);
					}

					// map to parameter
					switch (keyname.trim().toLowerCase()) {
					case "t_out":
						if (!value.trim().equalsIgnoreCase("")) {
							t_out = Integer.parseInt(value.trim());
							System.out.println("**************t_out:" + t_out + "****************");
						}
						break;
					case "t_base":
						if (!value.trim().equalsIgnoreCase("")) {
							t_base = Integer.parseInt(value.trim());
							System.out.println("**************t_base:" + t_base + "****************");
						}
						break;
					case "scale":
						if (!value.trim().equalsIgnoreCase("")) {
							scale = value.trim();
							System.out.println("**************scale:" + scale + "****************");
						}
						break;
					case "deadband":
						if (!value.trim().equalsIgnoreCase("")) {
							deadband = Integer.parseInt(value.trim());
							System.out.println("**************deadband:" + deadband + "****************");

						}
						break;
					case "p1":
						if (!value.trim().equalsIgnoreCase("")) {
							p1 = Float.parseFloat(value.trim());
							System.out.println("**************p1:" + p1 + "****************");

						}
						break;
					case "p2":
						if (!value.trim().equalsIgnoreCase("")) {
							p2 = Float.parseFloat(value.trim());
							System.out.println("**************p2:" + p2 + "****************");

						}
						break;
					case "area":
						if (!value.trim().equalsIgnoreCase("")) {
							area = Float.parseFloat(value.trim());
							System.out.println("**************area:" + area + "****************");

						}
						break;
					case "height":
						if (!value.trim().equalsIgnoreCase("")) {
							height = Float.parseFloat(value.trim());
							System.out.println("**************height:" + height + "****************");

						}
						break;
					case "air_density":
						if (!value.trim().equalsIgnoreCase("")) {
							air_density = Float.parseFloat(value.trim());
							System.out.println("**************air_heat_capacity:" + air_heat_capacity + "****************");
						}
						break;
					case "air_heat_capacity":
						if (!value.trim().equalsIgnoreCase("")) {
							air_heat_capacity = Float.parseFloat(value.trim());
							System.out.println("**************air_heat_capacity:" + air_heat_capacity + "****************");

						}
						break;
					case "power":
						if (!value.trim().equalsIgnoreCase("")) {
							power = Integer.parseInt(value.trim());
							System.out.println("**************power:" + power + "****************");
						}
						break;
					case "model":
						if (!value.trim().equalsIgnoreCase("")) {
							model = value.trim();
							System.out.println("**************model:" + model + "****************");
						}
						break;
					default:
						break;
					}

				}
			}
		} catch (Exception e) {
			System.err.println(e.getMessage()); // handle exception
		}
	}
	
	/*
	 * extract mode(either heating or cooling) from the input
	 */
	public static String getModeFromInput() {
		String mode="";
		try (BufferedReader br = new BufferedReader(new FileReader("input.txt"))) {
			String line;
			while ((line = br.readLine()) != null) {
				if(line.startsWith("MODE")) {
					//extract the value of mode
                	int offset = line.indexOf("=");
                	if (offset != -1) {
                		if(line.indexOf("//")== -1)
                			mode = line.substring(offset + 1);
                		else mode = line.substring(offset + 1, line.indexOf("//"));
                	}
                    break;
				}
			}
		}catch (Exception e) {
			System.err.println(e.getMessage()); // handle exception
		}
		return mode;
	}
	
	/**
	 * handle input file line by line, calculate the runtime for each line, output the runtime per day of the whole schedule 
	 */
	public static void handleInput() {
		try (BufferedReader br = new BufferedReader(new FileReader("input.txt"))) {
			String line;
			while ((line = br.readLine()) != null) {
				// ignore comment lines and empty lines
				if (line.startsWith("/*") || line.trim().equalsIgnoreCase("")) {
					continue;
				} else {				
                    	//extract the schedule entry
                    	if( line.contains("=") && line.contains(":") && line.contains("-")) {
                    		count ++;
                    		int pos1 = line.indexOf("-");
                    		int pos2 = line.indexOf("=");
                    		String timeStr1 = line.substring(0, pos1);
                    		String timeStr2 = line.substring(pos1 + 1, pos2);;
                    		String tempStr= line.substring(pos2 + 1).trim();                    		
                    		//System.out.println("time1:"+ timeStr1 + ";time2:" + timeStr2 + ";temperature:" + tempStr);                    		
                    		if(count ==1) {
                    			cur_temp = t_base;
                    			nxt_temp = Integer.parseInt(tempStr);
                    			nxt_duration = getTimeDiffInHours(timeStr1,timeStr2);
                    			total_time +=  nxt_duration;
                    		}else {
                    			//compute the runtime of (count-1)th entry
                    			pre_temp = cur_temp;
                    			cur_temp = nxt_temp;
                    			cur_duration = nxt_duration;
                    			nxt_temp = Integer.parseInt(tempStr);
                    			nxt_duration = getTimeDiffInHours(timeStr1,timeStr2);
                    			float runtimeForEntry =  computeRuntimeForEntry(pre_temp, cur_temp, nxt_temp, cur_duration);
                    			sum_runtime += runtimeForEntry;
                    			int lineNo = count-1;
                    			System.out.println("line:" + lineNo + "****" + "pre:" + pre_temp + "****" + "cur" + cur_temp + "****" + "next" + nxt_temp + "****" + "dur" + cur_duration +"****" + "runtime:" + runtimeForEntry);
                    			total_time +=  nxt_duration;                    	
                    		}   		
                    	}
                    }                	
				}
	
			//process the last entry;
			pre_temp = cur_temp;
			cur_temp = nxt_temp;
			cur_duration = nxt_duration;
			nxt_temp = -1;
			float runtimeForLastEntry =  computeRuntimeForEntry(pre_temp, cur_temp, nxt_temp, cur_duration);
			sum_runtime += runtimeForLastEntry;
			System.out.println("line:" + count + "****" + "pre:" + pre_temp + "****" + "cur" + cur_temp + "****" + "next" + nxt_temp + "****" + "dur" + cur_duration +"****" +"runtime:" + + runtimeForLastEntry);
			runtime_schedule = sum_runtime/total_time * 24;
			System.out.println("runtime with schedule:" +runtime_schedule);
			
		} catch (Exception e) {
			System.err.println(e.getMessage()); // handle exception
		}
	}
	
	/**
	 * calculate the furnace runtime for entry
	 * need to handle cooling case.......................
	 * @param pre, temperature of previous entry
	 * @param cur, temperature of next entry
	 * @param nxt, temperature of current entry
	 * @param dur, time duration of current entry
	 * @return
	 */
	public static float computeRuntimeForEntry(float pre, float cur, float nxt, float dur){  
        System.out.println("remaining_heating_time = " + remaining_heating_time);
		float maintainTime = dur-remaining_heating_time;
		float preheatRuntime =remaining_heating_time;
		remaining_heating_time =  0;
		float current = cur;
		if(mode.equalsIgnoreCase("heat")) {
			if (pre > current) {
				float offtime = computeOffTime(pre, current-pre);	
				System.out.println("offtime" + offtime);
				if (offtime >= maintainTime) {
					float tempChange = computeTempChangeInGivenTime(current, maintainTime);
					System.out.println("temp change:" +tempChange );
					maintainTime = 0;
					current = pre - tempChange;
					if (nxt > current) {
						preheatRuntime = computeHeatCoolRuntime(nxt-current);
					}
					
				} else {
					maintainTime -= offtime; 
					if(nxt > current) {
						// pre-heating needed
					    preheatRuntime = computeHeatCoolRuntime(nxt-current);
						if(preheatRuntime >= maintainTime) {							
							maintainTime = 0;
							// need to calculate the preheat time point to achieve accuracy	
						} else {
							maintainTime -= preheatRuntime;
						}
					}
				}
			} else {
				if(nxt > current) {
					//pre-heating needed
					preheatRuntime = computeHeatCoolRuntime(nxt-current);
					// not able to preheat in current duration
					if(preheatRuntime >= maintainTime) {
						 maintainTime = 0;
						 remaining_heating_time = preheatRuntime - maintainTime;
						 preheatRuntime = maintainTime;
						 
					 } else {
						 maintainTime -= preheatRuntime;
					 }
						 
				}
			}
		} else  {
			//handle the case of cooling
		}
		float maintainRunTime =  computeMaintainRuntime(cur, maintainTime);
		System.out.println("maintainTime:" + maintainTime +";maintainRuntime:" + maintainRunTime +";preheatRuntime:" + preheatRuntime);
		return maintainRunTime+ preheatRuntime;
		
	}
	
	/**
	 * calculate time difference in hours between two time string 
	 * @param timeStr1
	 * @param timeStr2
	 * @return diffHours
	 */
	public static float getTimeDiffInHours(String timeStr1,String timeStr2) {
		
		float diffHours = -1;
 
		//HH converts hour in 24 hours format (0-23), day calculation
		SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm");
 
		Date d1 = null;
		Date d2 = null;
 
		try {
			d1 = format.parse(timeStr1);
			d2 = format.parse(timeStr2);
 
			//in milliseconds
			long diff = d2.getTime() - d1.getTime();
			diffHours = diff / (60 * 60 * 1000) % 24;
			//System.out.print(diffHours + " hours, ");

		} catch (Exception e) {
			e.printStackTrace();
		}
		return diffHours;
		
 
	}

}
