import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RaidusAlgorithm {
	private static final double R=6367444.7; 
	
	private static double radius;
	private static double accuracy_filter;
	private static double home_lat;
	private static double home_long;
	private static boolean isFirstEntry = true;
	private static int away_temp;
	private static int athome_temp;
	private static String mode;
	private static boolean isAtHome;
	private static boolean isIgnored = false;
	private static String filename;

	public static void main(String[] args) {
		// read from config file
		readConfig("config_radius.txt");
		Writer writer = null;
		try {
			SimpleDateFormat df = new SimpleDateFormat(
					"MM/dd/yyyy HH:mm");
			
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream("output.txt"), "utf-8"));
			// write mode in the first line
			writer.write("mode=" + mode +"\r\n");
			
			// handle raw data file line by line
			try (BufferedReader br = new BufferedReader(new FileReader(
					filename))) {
				
				String line;
				RawDataEntry entry = null;
				String dateFormat = null; 
				
				while ((line = br.readLine()) != null) {
					
					if (!line.startsWith("time")) { //ignore the first line of the raw data
						
						if(!isIgnored && (dateFormat!=null) && !isIgnored(entry.getAccuracy())) { 
						     writer.write(dateFormat + "-" ); //write the starting time to the output file
						}
						
						// extract the positions of each substring
						int datePos = line.indexOf(",");
						int providerPos = line.indexOf(",", datePos + 1);
						int latPos = line.indexOf(",", providerPos + 1);
						int longPos = line.indexOf(",", latPos + 1);
						int elevationPos = line.indexOf(",", longPos + 1);
						int accuracyPos = line.indexOf(",", elevationPos + 1);
						int bearingPos = line.indexOf(",", accuracyPos + 1);

						// get substrings from the line
						String dateStr = line.substring(0, datePos);
						String providerStr = line.substring(datePos + 1,
								providerPos);
						String latStr = line.substring(providerPos + 1, latPos);
						String longStr = line.substring(latPos + 1, longPos);
						String elevationStr = line.substring(longPos + 1,
								elevationPos);
						String accuracyStr = line.substring(elevationPos + 1,
								accuracyPos);
						String bearingStr = line.substring(accuracyPos + 1,
								bearingPos);
						String speedStr = line.substring(bearingPos + 1);

						// System.out.println("dateStr:" + dateStr +
						// ";\nproviderStr:" +providerStr + ";\nlatStr :" +
						// latStr
						// + ";\nlongStr :" + longStr + ";\naccuracyStr :" +
						// accuracyStr +"\n\n");

						// get date from dateStr
						Date date = getDate(dateStr);
						// System.out.println("date" + date);
						
						//convert substrings to proper types
						String provider = providerStr;
						double latitude = Double.parseDouble(latStr);
						double longitude = Double.parseDouble(longStr);
						double elevation = Double.parseDouble(elevationStr);
						double accuracy = Double.parseDouble(accuracyStr);
						double bearing = Double.parseDouble(bearingStr);
						double speed = Double.parseDouble(speedStr);

						// create the entry for the line
						entry = new RawDataEntry(date, provider,
								latitude, longitude, elevation, accuracy,
								bearing, speed);
						
					    //calculate distance from home
					    double distance = calDistance(home_lat, home_long, entry.getLatitude(), entry.getLongitude());
					    
					    //get Date string with output format
					    dateFormat = df.format(entry.getDate());
			    
					    //filter the inaccurate entry  
						if (!isIgnored(entry.getAccuracy())) {
						    System.out.println(dateFormat + " " + distance );
						    
							if (isFirstEntry) {
							    //deal with the case that the entry is the first entry
								isFirstEntry = false;
								
								if(distance < radius) {
									isAtHome = true;
								} else {
									isAtHome = false;
								}									
							} else {
							    //deal with the case that the entry is not the first entry
								if(isAtHome && distance >= radius) { //the user has stepped out the radius 
									isAtHome = false;							
									writer.write(dateFormat + "=" + athome_temp + "\r\n" );
									isIgnored = false;									
								} else if (!isAtHome && distance < radius) {//the user has entered into the radius
									isAtHome = true;
									writer.write(dateFormat + "=" + away_temp + "\r\n");
									isIgnored = false;
								} else {
									isIgnored = true; // user location status has not been changed, ignore this entry
								}
								
							}
						}
					}
				}
				//handle the last entry, if it's ignored, 
				if(isIgnored) {
					 dateFormat = df.format(entry.getDate());
					if(isAtHome){
					    writer.write(dateFormat + "=" + athome_temp +"\r\n");
					} else {
						writer.write(dateFormat + "=" + away_temp +"\r\n");
					}
				} else {
					
				}
			} catch (Exception e) {
				System.err.println(e.getMessage()); // handle exception
			}			
		} catch (IOException ex) {
			// report
		} finally {
			try {
				writer.close();
			} catch (Exception ex) {
			}
		}
	}

	/**
	 * Convert UTC 00:00 to UTC -7:00, and return date from dateStr
	 * 
	 * @param dateStr
	 * @return
	 */
	public static Date getDate(String dateStr) {
		String str;
		Date date = new Date();
		try {
			str = dateStr.replace("T", " ");
			str = str.replace("Z", " ");
			// System.out.println("str:" + str);
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date rawDate = df.parse(str);
			date = new Date(rawDate.getTime() - 7 * 60 * 60 * 1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return date;
	}

	/**
	 * read parameters from config file
	 * 
	 * @param filename
	 */
	public static void readConfig(String file) {

		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
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
					case "radius":
						if (!value.trim().equalsIgnoreCase("")) {
							radius = Double.parseDouble(value.trim());
							System.out.println("**************radius:" + radius
									+ "****************");
						}
						break;
					case "accuracy_filter":
						if (!value.trim().equalsIgnoreCase("")) {
							accuracy_filter = Double.parseDouble(value.trim());
							System.out.println("**************accuracy_filter:"
									+ accuracy_filter + "****************");
						}
						break;
					case "home_lat":
						if (!value.trim().equalsIgnoreCase("")) {
							home_lat = Double.parseDouble(value.trim());
							System.out.println("**************home_lat:"
									+ home_lat + "****************");
						}
						break;
					case "home_long":
						if (!value.trim().equalsIgnoreCase("")) {
							home_long = Double.parseDouble(value.trim());
							System.out.println("**************home_long:"
									+ home_long + "****************");

						}
						break;
					case "away_temp":
						if (!value.trim().equalsIgnoreCase("")) {
							away_temp = Integer.parseInt(value.trim());
							System.out.println("**************away_temp:"
									+ away_temp + "****************");

						}
						break;
					case "athome_temp":
						if (!value.trim().equalsIgnoreCase("")) {
							athome_temp = Integer.parseInt(value.trim());
							System.out.println("**************athome_temp:"
									+ athome_temp + "****************");

						}
						break;
					case "mode":
						if (!value.trim().equalsIgnoreCase("")) {
							mode = value.trim();
							System.out.println("**************mode:" + mode
									+ "****************");

						}
						break;
					case "filename":
						if (!value.trim().equalsIgnoreCase("")) {
							filename = value.trim();
							System.out.println("**************filename:" + filename
									+ "****************");
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

	/**
	 * accaracy filter 
	 * @param accuracy
	 * @return
	 */
	public static boolean isIgnored(double accuracy) {
		if (accuracy < accuracy_filter)
			return false;
		else
			return true;
	}
	
	/**
	 * Calculate the geographical distance between two locations 
	 * @param lastLat
	 * @param lastLng
	 * @param lat
	 * @param lng
	 * @return
	 */
	public static double calDistance(double lastLat, double lastLng, double lat, double lng)
	{
		double distance=Math.acos(Math.cos(Math.toRadians(lastLat))*Math.cos(Math.toRadians(lat))*Math.cos(Math.toRadians(lastLng-lng))+Math.sin(Math.toRadians(lastLat))*Math.sin(Math.toRadians(lat)))*R;
		return distance;
	}
}
