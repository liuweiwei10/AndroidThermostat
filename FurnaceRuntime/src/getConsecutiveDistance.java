import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

public class getConsecutiveDistance {

	private static final double R = 6367444.7;
	private static String filename = "beichuan.txt";
	
	private static double lat1;
	private static double lon1;
	private static double lat2;
	private static double lon2;
	private static boolean isFirstLine = true;
	private static int count =0;
	private static Date date1;
	private static Date date2;
	
	public static void main(String[] args) {

		// create a writer to write output
		Writer writer = null;

		try {
			SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream("distance.txt"), "utf-8"));

			// handle raw data file line by line
			try (BufferedReader br = new BufferedReader(
					new FileReader(filename))) {
				String line;
				RawDataEntry entry = null;
				String dateFormat = null;				
 
				while ((line = br.readLine()) != null) {
					if (!line.startsWith("time") && !line.isEmpty()) { // ignore
																		// the
																		// first
																		// line
																		// of
																		// the
																		// raw
																		// data
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

						// get date from dateStr
						Date date = getDate(dateStr);

						// convert substrings to proper types

						double latitude = Double.parseDouble(latStr);
						double longitude = Double.parseDouble(longStr);
						double accuracy = Double.parseDouble(accuracyStr);

						// System.out.println("current entry time:" +
						// df.format(entry.getDate()));
						if (accuracy < 100) {
							if(isFirstLine) {
								lat1 = latitude;
								lon1 = longitude;
								isFirstLine = false;
								date1 = date;
							} else {
								lat2 = latitude;
								lon2 = longitude;
								date2= date;
								double distance = calDistance( lat1, lon1,lat2,lon2);
								double timeDiff = (date2.getTime() - date1.getTime())/1000.0;
								lat1 = lat2;
								lon1 = lon2;
								date1 = date2;
							
								if(distance >= 0.0) {
									
									count++;
								   if(timeDiff > 0){ 
								   double ratio =  (double)distance/ (double)timeDiff;
								   writer.write(ratio +"\r\n");
								   }
								}
							}
							

						}
					}
				}
			} catch (Exception e) {
				System.err.println(e.getStackTrace());
				System.err.println(e.getMessage()); // handle exception
				System.out.println("error catched at position 1");

			}
		} catch (IOException ex) {
			System.err.println(ex.getMessage());
			System.out.println("error catched at position 2");

		} finally {
			try {
				writer.close();
				System.out.println("Done calculation");
			} catch (Exception ex) {
				System.err.println(ex.getMessage());
				System.out.println("error catched at position 3");
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
	 * Calculate the geographical distance between two locations
	 * 
	 * @param lastLat
	 * @param lastLng
	 * @param lat
	 * @param lng
	 * @return
	 */
	public static double calDistance(double lastLat, double lastLng,
			double lat, double lng) {
		double distance = Math.acos(Math.cos(Math.toRadians(lastLat))
				* Math.cos(Math.toRadians(lat))
				* Math.cos(Math.toRadians(lastLng - lng))
				+ Math.sin(Math.toRadians(lastLat))
				* Math.sin(Math.toRadians(lat)))
				* R;
		return distance;
	}

	public static String covertArraytoString(ArrayList<Probability> array) {
		String arrStr = new String();
		if (array.isEmpty()) {
			arrStr = "null";
		} else {
			Iterator<Probability> iter = array.iterator();
			while (iter.hasNext()) {
				Probability pro = (Probability) iter.next();
				BigDecimal bPro = new BigDecimal(pro.probability);
				double probability = bPro.setScale(3, BigDecimal.ROUND_HALF_UP)
						.doubleValue();
				arrStr += pro.id + ", " + probability + " --> ";
			}
		}
		return arrStr;
	}

}
