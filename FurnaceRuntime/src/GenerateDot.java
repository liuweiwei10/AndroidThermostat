import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;


public class GenerateDot {
	//private static String filename = "nodeList.txt";	
	//private static String filename = "nodeListWeekday1.txt";
	//private static String filename = "nodeListWeekday2.txt";
	//private static String filename = "nodeListWeekend1.txt";
	private static String filename = "nodeListWeekend2.txt";
	private static final boolean HOME_FLAG =  true; //set to true if you want to draw the outdegree of home node, otherwise false;
	public static void main(String[] args) {
		Writer writer = null;
	
		try {				
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(filename +".dot"), "utf-8"));
			
		    writer.write("digraph G{"  +"\r\n");
		   // writer.write("    0 [shape=box]\r\n");
			
			// handle raw data file line by line
			try (BufferedReader br = new BufferedReader(new FileReader(
					filename))) {					
				String line;
				while ((line = br.readLine()) != null) {					
					//if (!line.startsWith("0") && !line.isEmpty()) { //ignore the first line of the raw data
					if (!line.isEmpty()) { 
						if(line.charAt(1)== '(') {
							if(line.startsWith("0")) {
								writer.write("    " + line.trim() + "[shape=box]\r\n");
							}else {
								writer.write("    " + line.trim() +"\r\n");
							}
						} else {
					    int idPos = line.indexOf(":");
						String nodeID = line.substring(0, idPos).trim();
						if(nodeID.trim().equalsIgnoreCase("0")) {
							if(!HOME_FLAG) {
								continue;
							}
						}
						String proStr = line.substring(idPos + 1).trim();
						if(!proStr.trim().equalsIgnoreCase("null")) {
						String[] proArr = proStr.split("-->");
						for(int i=0; i < proArr.length; i++) {
							if (proArr[i]!= "") {
								int braketPos = proArr[i].indexOf("(");
								String nodeID2 = proArr[i].substring(0, braketPos-1).trim();
								String probability = proArr[i].substring(braketPos).trim();
								if(nodeID.equalsIgnoreCase("0")) {
									writer.write("    " +nodeID+" -> "+nodeID2+" [label=" + "\""+ probability +"\""+ " fontcolor=green, color=green];" + "\r\n");
								}else if(nodeID2.equalsIgnoreCase("0")) {
									writer.write("    " +nodeID+" -> "+nodeID2+" [label=" + "\""+ probability +"\""+ " fontcolor=red, color=red];" + "\r\n");
								} else {
								writer.write("    " +nodeID+" -> "+nodeID2+" [label=" + "\""+ probability +"\"" +"];" + "\r\n");
								}
							}
						}
						}
					}
					}	
				}	
				 writer.write("}"  +"\r\n");
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
			} catch (Exception ex) {
				System.err.println(ex.getMessage());
				 System.out.println("error catched at position 3");
			}
		}
	}
}
