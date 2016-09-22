import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;


public class MarkovAlgorithm {
	private static final double R=6367444.7; 
	private static final long traveling_threshold = 5 * 60 *1000; //if user leaves some node comes back with in this threshold, then doesn't count it as a travel
	private static final long prospective_node_threshold = 5*60*1000; //if user stay at some coordinate for this threshold long, then it becomes a prospective node
	private static final float MIN_TIME_SEG = 1;
	private static final float NON_SPLIT_THRESH = 0.5f;
	private static double probability_threshold;
	private static double accuracy_filter;
	private static  double distance_threshold;
	private static long time_threshold;
	private static double home_lat;
	private static double home_long;
	private static int away_temp;
	private static int athome_temp;
	private static String mode; 
	private static String filename;	
	private static boolean isFirstLine = true; //mark whether the current line is first line of the file or not     
	private static Date initialTime; // starting time of the file
	private static long timeElapsed; // time elapsed since the starting time
	private static Coordinate curCoor = new Coordinate();//the coordinate of current candidate node
	private static Date curCoorTime; // the starting time that user enters into current candidate node
	private static long stayTime; //stay time of current candidate node
	private static Coordinate coorSum = new Coordinate(); // sum of the candidate node coordinates
	private static int coorCount = 0; //Count of the visit of candidate node coordinates 
	private static boolean isAtNode; // whether the current location is at a known node 
	private static int nodeID; // if isAtNode, then nodeID represents the id of that node, if !isAtNode, then nodeId = -1;
	private static ArrayList<Node> nodeList; // node List
	//private static ArrayList<Node> nodeListDynamicPro; // node List with dynamic probability
	private static ArrayList<Node> nodeListWeekday1; // node List with edges starting before 2pm on weekdays
	private static ArrayList<Node> nodeListWeekday2; // node List with edges starting after 2pm on weekdays
	private static ArrayList<Node> nodeListWeekend1; // node List with edges starting before 2pm on weekends
	private static ArrayList<Node> nodeListWeekend2; // node List with edges starting after 2pm on weekends
	private static int previousNodeID = -1; // ID of the previous Node 
	private static Date enteringTime; //the time that the user enters current node 
	private static Date leavingTime; // the time that the user leaves current node
	private static Date travelStartingTime; // the start time of current travel
	private static Date travelEndingTime; // the ending time of the current travel 
	private static long oldAverageStayTime; //in case user leaves some node comes back in 5 minutes, use this to keep track of the old averageStayTime of that node
	private static Coordinate prospectiveNodeCoor = null; // the curCoor of the prospective Node, use it to deal with the noise in node discovery phase
	private static Date prospectiveLeavingTime; //the time the user leaves the prospective Node, use it to deal with the noise in node discovery phase
	private static Date prospectiveReturningTime;//the time the user returns the prospective Node, use it to deal with the noise in node discovery phase
	private static Date prospectiveCoorTime;// the curCoorTime of the prospective Node, use it to deal with the noise in node discovery phase
	private static boolean hasUpdated = false; //whether the node list has been updated
	private static Date previousNodeEnteringTime = null; //previous node enteringTime
	
	@SuppressWarnings("deprecation")
	public static void main(String[] args) {
		readConfig("config_markov.txt");
		//create a writer to write output
		Writer writer = null;
		//create an array list to store the nodes
		nodeList = new ArrayList<Node>();
		nodeListWeekday1 = new ArrayList<Node>();
		nodeListWeekday2 = new ArrayList<Node>();
		nodeListWeekend1 = new ArrayList<Node>();
		nodeListWeekend2 = new ArrayList<Node>();
		
		//create a node for home
		Node homeNode = new Node();
		homeNode.id = 0;
		homeNode.coor.latitude = home_lat;
		homeNode.coor.longitude = home_long;
		homeNode.nextVisitCount = 0;
		//homeNode.nextVisitCount = -1;
		homeNode.averageStayTime =0;
		homeNode.visitCount =0;
		
		Node homeNode2 = getNodeCopy(homeNode);
		Node homeNode3 = getNodeCopy(homeNode);
		Node homeNode4 = getNodeCopy(homeNode);
		Node homeNode5 = getNodeCopy(homeNode);
	    //add home node to nodeList
		
		nodeList.add(homeNode);
		nodeListWeekday1.add(homeNode2);
		nodeListWeekday2.add(homeNode3);
		nodeListWeekend1.add(homeNode4);
		nodeListWeekend2.add(homeNode5);
		
		try {
			SimpleDateFormat df = new SimpleDateFormat(
					"MM/dd/yyyy HH:mm:ss");
			
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream("output_markov.txt"), "utf-8"));
			// write mode in the first line
			writer.write("mode=" + mode +"\r\n");
			
			// handle raw data file line by line
			try (BufferedReader br = new BufferedReader(new FileReader(
					filename))) {				
				String line;
				RawDataEntry entry = null;
				String dateFormat = null; 	
				
				while ((line = br.readLine()) != null) {					
					if (!line.startsWith("time") && !line.isEmpty()) { //ignore the first line of the raw data
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
						//System.out.println("current entry time:" + df.format(entry.getDate()));
						Date debugDate = df.parse("03/15/2015 09:58:24");
						if(entry.getDate().equals(debugDate)) {
						   System.out.println("debug");
						}
						if (!isIgnored(entry.getAccuracy())) {							
							if (isFirstLine) {
							
								initialTime = entry.getDate();
								timeElapsed = 0;
								
								if(isAtHome(entry.getLatitude(), entry.getLongitude())) {
									isAtNode = true;									
									nodeID = 0;
									enteringTime = entry.getDate();
									Node node = getNodeFromList(0,nodeID);
									
								} else {
									
								stayTime = 0;
								curCoor.latitude = entry.getLatitude();
								curCoor.longitude = entry.getLongitude();
								curCoorTime = entry.getDate();
								coorSum.latitude = curCoor.latitude;
								coorSum.longitude = curCoor.longitude;
								coorCount=0;
								coorCount++;
								isAtNode = false;
								nodeID = -1;
							    //System.out.println("0~~~~~~~~~~~~~~~~~~~~~~~current candidate:"+ df.format(entry.getDate())+":"+ curCoor.latitude + ", " + curCoor.longitude + "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");	
								}
								
								isFirstLine = false;

							} else {
								timeElapsed = entry.getDate().getTime()
										- initialTime.getTime();
								
								if (isAtNode) {                                     
									// handle the case that it's already at some
									// node
								    if(nodeID ==0) {
								    	//the user was at home
										double distance = calDistance(home_lat,
												home_long, entry.getLatitude(),entry.getLongitude());
										Node node = getNodeFromList(0,nodeID);
										if(distance >= distance_threshold) {
											//the user leaves home
											
											leavingTime = entry.getDate();
											
											long duration = leavingTime.getTime() - enteringTime.getTime();
																						
											if(duration < time_threshold) {
												//short stay time or just pass the node, don't consider this as a travel
											    System.out.println("short stay at node " + node.id + ",duration:" + duration/60000 + "m");
											} else {											travelStartingTime = entry.getDate();
											node.averageStayTime =(node.averageStayTime * (node.visitCount-1) + duration)/node.visitCount;
											oldAverageStayTime = node.averageStayTime;
											
											previousNodeID = node.id;
											
											//System.out.println("distance_threshold = " + distance_threshold);
										    System.out.println("distance=" + distance);										    
											System.out.println(df.format(entry.getDate()) + "  user leaves home..." );
											System.out.println("node:" + node.id + "; visit Count:" + node.visitCount + "; averageStayTime:" + node.averageStayTime/(1000*60) + " minutes");
											
											
											
											//enter place dicovery phase
											stayTime = 0;
											curCoor.latitude = entry.getLatitude();
											curCoor.longitude = entry.getLongitude();
											curCoorTime = entry.getDate();
											coorSum.latitude = curCoor.latitude;
											coorSum.longitude = curCoor.longitude;
											coorCount = 0;
											coorCount++;
											isAtNode = false;
											nodeID = -1;		
											
									//	    System.out.println("1~~~~~~~~~~~~~~~~~~~~~~~current candidate:"+ df.format(entry.getDate())+":"+ curCoor.latitude + ", " + curCoor.longitude + "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");	
											
										   //enteringTime = leavingTime;
									
											
											//handle output to modify the setPoint
											//
											//
											//
											}
										} else {
											//the user is still at home
											//System.out.println(df.format(entry.getDate()) + "  user is at home");
                                            if((entry.getDate().getTime() - enteringTime.getTime())>= time_threshold && !hasUpdated) {
                                            	//when user is at home for time_threshold long, add the corresponding edge
                                            	node.visitCount++;
                                            	hasUpdated = true;
												if (previousNodeID != -1) {
													// update the probability list
													// for previous node
													Node previousNode = getNodeFromList(0,previousNodeID);
													if (previousNode != null) {
														updateProList(previousNode,
																node.id);
														float startingTime = getExactHourOfDay(travelStartingTime);
													    if(node.id == 0) {
													    	updateTimeSegList(previousNode, true, startingTime);
													    } else {
													    	updateTimeSegList(previousNode, false, startingTime);

													    }
														if(travelStartingTime.getDay() != 0 && travelStartingTime.getDay() != 6){
															// weekday
															if(getExactHourOfDay(travelStartingTime) < 14) {
																// update nodeListWeekday1
																Node node1 = getNodeFromList(1, previousNodeID);
																updateProList(node1, node.id);
															} else {
																// update nodeListWeekday1
																Node node1 = getNodeFromList(2, previousNodeID);
																updateProList(node1, node.id);
															}
														} else {
															//weekend
															if(getExactHourOfDay(travelStartingTime) < 14) {
																// update nodeListWeekend1
																Node node1 = getNodeFromList(3, previousNodeID);
																updateProList(node1, node.id);
															} else {
																Node node1 = getNodeFromList(4, previousNodeID);
																updateProList(node1, node.id);
															}
														}
														
														Probability pro = getProByID(
																previousNode.proList,
																node.id);
														if (pro != null) {
															System.out.println(df.format(entry.getDate()) + " edge: node " + previousNode.id
																			+ " to "+ "node "+ node.id
																			+ ": average starting time = "
																			+ pro.averageStartingTime
																			+ "; visit count = "
																			+ pro.visitCount
																			+ "; standard deviation = "
																			+ pro.standardDeviationTime
																			+ "; maximum = "
																			+ pro.maxStartingTime
																			+ "; minmum = "
																			+ pro.minStartingTime);
														}
													} else {
														System.out
																.println("can't find node with previousNodeID ");
													}
												}

							    		    }
										}
										
								    } else {
								    	//user is at some other node(not home node)
								        Node node = getNodeFromList(0, nodeID);
								    	if(!(node == null)) {
								    		double distance = calDistance(node.coor.latitude,
								    				node.coor.longitude, entry.getLatitude(),entry.getLongitude());
								    		
								    		//System.out.println(df.format(entry.getDate()) + "user is at node:" + node.id + " with distance: " + distance);
								    		writer.write(df.format(entry.getDate()) + "user is at node:" + node.id + " with distance: " + distance +"\r\n");
								    		
								    		
								    		if(distance >= distance_threshold) {
								    		    //user leaves node
								    	
								    			leavingTime = entry.getDate();	
								    			
								    			
												long duration = leavingTime.getTime() - enteringTime.getTime();
												System.out.println("node " + node.id + "; leavingtime " + df.format(leavingTime) + "; entering Time " + df.format(enteringTime));
												if(duration < time_threshold || !hasUpdated) {
													isAtNode = false;
													nodeID = -1;
													//short stay time or just pass the node, don't consider this as a travel
												    System.out.println("short stay at node " + node.id + ",duration:" + duration/60000 + "m");
												} else {
												
												
												travelStartingTime = entry.getDate();
												
												//Update the averageStayTime of the node
												oldAverageStayTime = node.averageStayTime;
												
												node.averageStayTime =(node.averageStayTime * (node.visitCount-1) + duration)/node.visitCount;
												
												
								    			System.out.println(df.format(entry.getDate()));
												System.out.println("leaves node :" + node.id + " with distance: " + distance);
												System.out.println("node:" + node.id + "; visit Count:" + node.visitCount + "; averageStayTime:" + node.averageStayTime/(1000*60) + " minutes");

								    			previousNodeID = node.id;
								
								    			//enter place dicovery phase
												stayTime = 0;
												curCoor.latitude = entry.getLatitude();
												curCoor.longitude = entry.getLongitude();
												curCoorTime = entry.getDate();
												coorSum.latitude = curCoor.latitude;
												coorSum.longitude = curCoor.longitude;
												coorCount = 0;
												coorCount++;
												isAtNode = false;
												nodeID = -1;	
												
										//	    System.out.println("2~~~~~~~~~~~~~~~~~~~~~~~current candidate:"+ df.format(entry.getDate())+":"+ curCoor.latitude + ", " + curCoor.longitude + "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");	
								    			
												//enteringTime = leavingTime;
												
												//
												//
												//make prediction here
												//
												//
												
												}
								    		} else {
								    			//still in the range of node,ignore the entry						
                                                if((entry.getDate().getTime() - enteringTime.getTime())>= time_threshold && !hasUpdated) {
                                                	node.visitCount++;
                                                	hasUpdated = true;
													if (previousNodeID != -1) {
														// update the probability list
														// for previous node
														Node previousNode = getNodeFromList(0, previousNodeID);
														if (previousNode != null) {
															updateProList(previousNode,
																	node.id);
															float startingTime = getExactHourOfDay(travelStartingTime);
														    if(node.id == 0) {
														    	updateTimeSegList(previousNode, true, startingTime);
														    } else {
														    	updateTimeSegList(previousNode, false, startingTime);

														    }
															if(travelStartingTime.getDay() != 0 && travelStartingTime.getDay() != 6){
																// weekday
																if(getExactHourOfDay(travelStartingTime) < 14) {
																	// update nodeListWeekday1
																	Node node1 = getNodeFromList(1, previousNodeID);
																	updateProList(node1, node.id);
																} else {
																	// update nodeListWeekday1
																	Node node1 = getNodeFromList(2, previousNodeID);
																	updateProList(node1, node.id);
																}
															} else {
																//weekend
																if(getExactHourOfDay(travelStartingTime) < 14) {
																	// update nodeListWeekend1
																	Node node1 = getNodeFromList(3, previousNodeID);
																	updateProList(node1, node.id);
																} else {
																	Node node1 = getNodeFromList(4, previousNodeID);
																	updateProList(node1, node.id);
																}
															}
															Probability pro = getProByID(
																	previousNode.proList,
																	node.id);
															if (pro != null) {
																System.out.println(df.format(entry.getDate()) + " edge: node " + previousNode.id
																				+ " to "+ "node "+ node.id
																				+ ": average starting time = "
																				+ pro.averageStartingTime
																				+ "; visit count = "
																				+ pro.visitCount
																				+ "; standard deviation = "
																				+ pro.standardDeviationTime
																				+ "; maximum = "
																				+ pro.maxStartingTime
																				+ "; minmum = "
																				+ pro.minStartingTime);
															}
														} else {
															System.out
																	.println("can't find node with previousNodeID ");
														}
													}
	
								    		    }
								    		}
								    		
								    	} else {
								    	   System.out.println("exception: node with nodeID doesn't exist");
								    	}							    	
								    }	
								} else {
									// scan nodeList to see if it enters any
									// node
									Node node =getNodeFromList(entry.getLatitude(),
											entry.getLongitude());
									if(node != null){                                         
										// handle the case it enters some node
										    
										    travelEndingTime = entry.getDate();
										    
											System.out.println(df.format(entry.getDate()));
											System.out.println("enters node :" + node.id);
											
										    if((previousNodeID == node.id)&& (travelEndingTime.getTime() - travelStartingTime.getTime() < traveling_threshold)) {
										    	//ignore the noise(leaves the node and come back in short time)
										    	
										    	System.out.println("short leave, ignore!");
										    	//node.visitCount--;
										    	if(previousNodeEnteringTime != null)
										    		enteringTime = previousNodeEnteringTime;
										    	isAtNode = true;
										    	nodeID = previousNodeID;
										    	previousNodeID = -1;
												node.averageStayTime = oldAverageStayTime;
										    } else {
											// long travelTime =
											// travelEndingTime.getTime() -
											// travelStartingTime.getTime();
											hasUpdated = false;
										    enteringTime = entry.getDate();
                                         //   System.out.println("enteringTime:" + entry.getDate());
											// travelEndingTime =
											// travelStartingTime;

											if (node.id == 0) {
												
												// user arrives home
												// update the setpoint if needed
											}

											//previousNodeID = -1;
											isAtNode = true;
											nodeID = node.id;
									}
										
									} else {
										// compare with curCoor
										double distance = calDistance(
												curCoor.latitude,
												curCoor.longitude,
												entry.getLatitude(),
												entry.getLongitude());
										
										if (distance > distance_threshold) {
											// the user is moving
                                            if (prospectiveNodeCoor != null){
											if ((curCoor.longitude == prospectiveNodeCoor.longitude) && (curCoor.latitude == prospectiveNodeCoor.latitude)) {
												prospectiveLeavingTime = entry
														.getDate();
											}
                                            }
											
											boolean enterDiscoveryPhase = true;
											double d = -1;
											 
											if (prospectiveNodeCoor != null) {
											
												d = calDistance(
														prospectiveNodeCoor.latitude,
														prospectiveNodeCoor.longitude,
														entry.getLatitude(),
														entry.getLongitude());
												
												//System.out.println("prospetiveNodeCoor exist!: " + prospectiveNodeCoor.latitude+ "," +prospectiveNodeCoor.longitude + "; distance =" + d);
											}

											if (d >= 0 && d <= distance_threshold) {
												   
												   System.out.println("return to prospective Node."); 
													prospectiveReturningTime = entry.getDate();
													System.out.println("prospectiveReturningtime = " + df.format(prospectiveReturningTime) +";prospectiveLeavingTime= " +df.format(prospectiveLeavingTime)); 
													if ((prospectiveReturningTime.getTime()
															- prospectiveLeavingTime.getTime())< traveling_threshold) {
														// short leave of
														// prospective node,
														// doesn't count

														// restore the curCoor
														// and curCoorTime
														System.out.println("short leave of prospective coor:" + prospectiveNodeCoor.latitude +", "
														+  prospectiveNodeCoor.longitude + "; leave at:" +df.format(prospectiveLeavingTime) 
														+",return at:" + df.format(prospectiveReturningTime));
														curCoor.latitude = prospectiveNodeCoor.latitude;
														curCoor.longitude = prospectiveNodeCoor.longitude;
														curCoorTime = prospectiveCoorTime;
														enteringTime = prospectiveCoorTime;
														enterDiscoveryPhase = false;
													}										
											}  
                                            if(enterDiscoveryPhase) {
                                            	//System.out.println("enter discovery phase, update entering time");
                                            	previousNodeEnteringTime = enteringTime;
												enteringTime = entry.getDate();
	                                           // System.out.println("enteringTime:" + entry.getDate());

												// restart place dicovery phase
												// using current coordinates
												/*System.out
														.println("step out current candidate: "
																+ curCoor.latitude
																+ ", "
																+ curCoor.longitude
																+ " with distance "
																+ distance);*/
												stayTime = 0;
												curCoor.latitude = entry
														.getLatitude();
												curCoor.longitude = entry
														.getLongitude();
												curCoorTime = entry.getDate();
												coorSum.latitude = curCoor.latitude;
												coorSum.longitude = curCoor.longitude;
												coorCount = 0;
												coorCount++;
												isAtNode = false;
												nodeID = -1;

											/*	System.out
														.println("3~~~~~~~~~~~~~~~~~~~~~~~current candidate:"
																+ df.format(entry
																		.getDate())
																+ ":"
																+ curCoor.latitude
																+ ", "
																+ curCoor.longitude
																+ "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");*/
										}
										} else { // still in the range of the
													// current candidate node
											stayTime = entry.getDate()
													.getTime()
													- curCoorTime.getTime();
									
											
											if (stayTime >= time_threshold) {
												// enough stay time of current candidate node												
												System.out.println("new node discoverd!!!");												
												
												travelEndingTime = curCoorTime;
												enteringTime = curCoorTime;
												// update the sum of coordinates for candidate node 
												coorSum.latitude +=  entry.getLatitude();
												coorSum.longitude += entry.getLongitude();
												coorCount++;
												
												
												Coordinate coor = new Coordinate();
												coor.latitude = coorSum.latitude / coorCount ;
												coor.longitude = coorSum.longitude / coorCount;
												
											    //create new node for this candidate
												Node newNode = new Node();
												Node lastNode = nodeList.get(nodeList.size() -1);
												newNode.id = lastNode.id +1;				
												newNode.coor = coor;
												newNode.nextVisitCount = 0;
												newNode.visitCount = 0;
												newNode.averageStayTime = 0;
												
												Node newNode2 = getNodeCopy(newNode);
												Node newNode3 = getNodeCopy(newNode);
												Node newNode4 = getNodeCopy(newNode);
												Node newNode5 = getNodeCopy(newNode);
												
												nodeList.add(newNode);	
												nodeListWeekday1.add(newNode2);
												nodeListWeekday2.add(newNode3);
												nodeListWeekend1.add(newNode4);
												nodeListWeekend2.add(newNode5);
												
												
												isAtNode = true;
												nodeID = newNode.id;
	                                            newNode.visitCount ++;
	                                            hasUpdated = true;

							        			if(previousNodeID != -1) {	
													//update the probability list for previous node
														Node previousNode = getNodeFromList(0, previousNodeID);
														if (previousNode != null ) {
															//System.out.println("update node " + previousNode.id + ", with next node " + newNode.id);
															updateProList(previousNode, newNode.id);
															float startingTime = getExactHourOfDay(travelStartingTime);
														    if(newNode.id == 0) {
														    	updateTimeSegList(previousNode, true, startingTime);
														    } else {
														    	updateTimeSegList(previousNode, false, startingTime);

														    }
															if(travelStartingTime.getDay() != 0 && travelStartingTime.getDay() != 6){
																// weekday
																if(getExactHourOfDay(travelStartingTime) < 14) {
																	// update nodeListWeekday1
																	Node node1 = getNodeFromList(1, previousNodeID);
																	updateProList(node1, newNode.id);
																} else {
																	// update nodeListWeekday1
																	Node node1 = getNodeFromList(2, previousNodeID);
																	updateProList(node1, newNode.id);
																}
															} else {
																//weekend
																if(getExactHourOfDay(travelStartingTime) < 14) {
																	// update nodeListWeekend1
																	Node node1 = getNodeFromList(3, previousNodeID);
																	updateProList(node1, newNode.id);
																} else {
																	Node node1 = getNodeFromList(4, previousNodeID);
																	updateProList(node1, newNode.id);
																}
															}
															Probability pro = getProByID(
																	previousNode.proList,
																	newNode.id);
															if (pro != null) {
																System.out.println(df.format(entry.getDate()) + " edge: node " + previousNode.id
																				+ " to "+ "node "+ newNode.id
																				+ ": average starting time = "
																				+ pro.averageStartingTime
																				+ "; visit count = "
																				+ pro.visitCount
																				+ "; standard deviation = "
																				+ pro.standardDeviationTime
																				+ "; maximum = "
																				+ pro.maxStartingTime
																				+ "; minmum = "
																				+ pro.minStartingTime);
															}
														} else {
															System.out.println("can't find node with previousNodeID "); 
														}
												}
												
												previousNodeID = -1;
												System.out.println("******************************************");
												System.out.println("node ID" + newNode.id);
												System.out.println("node coordinate: ("
																+ newNode.coor.latitude
																+ ", "
																+ newNode.coor.longitude
																+ ");");
												System.out.println("enter time: "
																+ df.format(curCoorTime));
												System.out.println("current_time:"
																+ df.format(entry
																		.getDate()));
												System.out.println("******************************************");

											} else {												
												// update the sum of coordinates for candidate node 
												coorSum.latitude +=  entry.getLatitude();
												coorSum.longitude += entry.getLongitude();
												coorCount ++;
                              	
												//if the stayTime achieves the prospective_node_threshold, then curCoor becomes prospectiveNodeCoor
    											if (stayTime >= prospective_node_threshold ) {
    												//System.out.println("stay time >= prespective threshold : stayTime = " + stayTime/60000 + "min, threshold = " +  prospective_node_threshold/60000 );
    												if(prospectiveNodeCoor == null)
    													prospectiveNodeCoor = new Coordinate();

    												prospectiveNodeCoor.latitude = curCoor.latitude;
    												prospectiveNodeCoor.longitude = curCoor.longitude;
    												prospectiveCoorTime = curCoorTime;
    												
    												//System.out.println("prospective updated to " +prospectiveNodeCoor.latitude + ", " + prospectiveNodeCoor.longitude);
    											}
											}
										}

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
			} catch (Exception ex) {
				System.err.println(ex.getMessage());
				 System.out.println("error catched at position 3");
			}
		}
	//print the node List	
	printNodeList(0);
	printNodeList(1);
	printNodeList(2);
	printNodeList(3);
	printNodeList(4);
	printNodeList(5);
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
					case "probability_threshold":
						if (!value.trim().equalsIgnoreCase("")) {
							probability_threshold = Double.parseDouble(value.trim());
							System.out.println("**************probability_threshold:"
									+ probability_threshold + "****************");
						}
						break;
					case "distance_threshold":
						if (!value.trim().equalsIgnoreCase("")) {
							distance_threshold = Double.parseDouble(value.trim());
							System.out.println("**************distance_threshold:"
									+ distance_threshold + "****************");
						}
						break;
						
					case "time_threshold" :
						if (!value.trim().equalsIgnoreCase("")) {
							time_threshold = Long.parseLong(value.trim());
							System.out.println("**************time_threshold:"
									+ time_threshold + "****************");
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
	 * get node from nodeList with coordinates (lat,lon)
	 * @param lat
	 * @param lon
	 * @return node ID, if no node found, return -1;
	 */
	public static Node getNodeFromList(double lat, double lon) {
		Node nodeFound = null;
		Iterator<Node> iter = nodeList.iterator();
		while (iter.hasNext()) {
			Node node = (Node) iter.next();
			if(calDistance(node.coor.latitude, node.coor.longitude,lat,lon)< distance_threshold) {
				nodeFound = node;
				break;
			}
		}
		return nodeFound;
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
	 * determine whether the location identified by a pair of coordinates is home
	 * @param lat
	 * @param lon
	 * @return
	 */
	public static boolean isAtHome(double lat, double lon) {
		boolean isAtHome = false;
		double distance = calDistance(home_lat, home_long, lat, lon);
        if(distance < distance_threshold) isAtHome = true;
        return isAtHome;
	}
	
	/**
	 * get node from specific by nodeID
	 * @param i specifies which nodeList to search 0 for nodeList, 1 for nodeListWeekday1, 2 for nodeListWeekday2, 3 for nodeListWeekend1, 4 for nodeListWeekend2
	 * @param nodeID
	 * @return
	 */
	public static Node getNodeFromList(int i, int nodeID) {
		Node targetNode = null;
		Iterator<Node> it1;
		switch (i) {
		case 0:
			it1 = nodeList.iterator();
			break;
		case 1:
			it1 = nodeListWeekday1.iterator();
			break;
		case 2:
			it1 = nodeListWeekday2.iterator();
			break;
		case 3:
			it1 = nodeListWeekend1.iterator();
			break;
		case 4:
			it1 = nodeListWeekend2.iterator();
			break;
		default:
			return targetNode;			
		}
		
		while (it1.hasNext()) {
			Node node = (Node) it1.next();
			if (node.id == nodeID) {
				targetNode = node;
				break;
			}
		}
		return targetNode;
	}
	
	/**
	 * 
	 * @param node
	 * @param isToHome
	 * @param startingTime
	 */
	public static void updateTimeSegList(Node node, boolean isToHome, float startingTime) {
	    System.out.println("updateTimeSegList called..node id:" + node.id + ";startingTime:" +startingTime+";isToHome:" +isToHome);
		//float startingTime = getExactHourOfDay(travelStartingTime);
		if(node.timeSegList.isEmpty()) {
			// timeSegList is empty, create the first time Segment
			TimeSegment timeSeg = new TimeSegment();
			timeSeg.time = 24;
			if(isToHome){
				timeSeg.refTimeTrue.add(startingTime);
				timeSeg.prob = 1;
			} else {
				timeSeg.refTimeFalse.add(startingTime);
				timeSeg.prob = 0;
			}
			node.timeSegList.add(timeSeg);
		} else {
			// there are time segments in the timeSegList
			
			//scan timeSegList to find the segment that this data falls in
		    Iterator iter =  node.timeSegList.iterator();
		    TimeSegment previousSeg = null;
		    TimeSegment targetSeg = null;
		    
		    while (iter.hasNext()) {
		    	previousSeg = targetSeg;
		    	targetSeg = (TimeSegment) iter.next();
		    	if(startingTime < targetSeg.time) {
		    		break;
		    	} 
		    } 
		    
		    if(targetSeg != null) {
		    	System.out.println("targetSeg found.. time=" + targetSeg.time );
		    	if (targetSeg.prob == 1 && isToHome) {
		    		//no need to split segment
		    		targetSeg.refTimeTrue.add(startingTime);
		    		Collections.sort(targetSeg.refTimeTrue);
		    	} else if (targetSeg.prob == 0  && !isToHome){
		    		//no need to split segment
		    		targetSeg.refTimeFalse.add(startingTime);
		    		Collections.sort(targetSeg.refTimeFalse);
		    		
		    	} else if (targetSeg.prob==1 && !isToHome) { 
		    		// may need to split segment
		    		boolean isTargetTrue;
		    		if (targetSeg.prob ==1) {
		    			isTargetTrue = true;
		    		} else {
		    			isTargetTrue = false;
		    		}
		    		
		    		
		    		//scan the refTimeTrue list to get the refTime
		    		float refTime1 = -1;
		    		float refTime2 = -1;
		    		for(float time: targetSeg.refTimeTrue ) {
		    			if(startingTime > time) {
		    				refTime1 = time;
		    			} else {
	                           refTime2 = time;
	                           break;
		    			}
		    		}
		    		float newTime = -1;
		    		float newTime2 = -1;
		    		int refTimeFlag = -1;
		    		if((startingTime - refTime1) < NON_SPLIT_THRESH || (refTime2 -startingTime) < NON_SPLIT_THRESH ) {
		    			// simple strategy: if with in non split thresh then don't split, may improve later
		    			updateSeg(targetSeg, startingTime, isToHome);
		    			System.out.println("updateTimeSegList(): with in NON_SPLIT_THRESH, don't split..."+ 
		    			"current data:" + startingTime + "; refTime1:" + refTime1 + ";refTime2:" + refTime2);
		    		} else {		    			
		    			if(refTime1 == -1) {
		    				refTimeFlag = 2;
			    			newTime = (refTime2 + startingTime)/2;		    			
			    		} else if (refTime2 == -1){
			    			newTime = (refTime1 + startingTime)/2;
			    			refTimeFlag = 1;
			    		} else {
			    			//between two ref time
			    			refTimeFlag = 0;// refTimeFlag = 0 means there are two refTime
			    			newTime = (refTime1 + startingTime)/2;
			    			newTime2 = (refTime2 + startingTime)/2;  		
			    		}

			    		if(newTime2 == -1) {
			    			if(Math.abs(newTime - targetSeg.time) > MIN_TIME_SEG && 
				    				(previousSeg == null || Math.abs(newTime - previousSeg.time) > MIN_TIME_SEG)) {
			    				// consecutive segments should have time interval greater than MIN_TIME_SEG 
			    				TimeSegment newSeg = new TimeSegment();
					    		newSeg.time = newTime;
					    		if(refTimeFlag == 2) {
					    			newSeg.refTimeFalse.add(startingTime);
					    		    newSeg.prob = 0;	
					    		} else if(refTimeFlag == 1) {
					    			for(float time: targetSeg.refTimeTrue) {
					    				newSeg.refTimeTrue.add(time);
					    			}
					    				newSeg.prob = 1;
					    				targetSeg.refTimeTrue.clear();
					    				targetSeg.refTimeFalse.add(startingTime);
					    				targetSeg.prob = 0;					    			
					    		}
					    		
					    		node.timeSegList.add(newSeg);
					    		sortNodeTimeSegmentList(node);
					    		mergeNodeTimeSegmentList(node);
			    			
			    			} else {
			    				updateSeg(targetSeg, startingTime, isToHome);
				    			System.out.println("updateTimeSegList():  with in MIN_TIME_SEG, don't split..."+ 
			    				"newTime:" + newTime + ";targetSeg.time:" +targetSeg.time + ";previousSeg.time:" + previousSeg.time);
			    			}
			    		} else {
			    			//handle the case that there are two ref times
			    			if(Math.abs(newTime2 - targetSeg.time) > MIN_TIME_SEG && 
			    					Math.abs(newTime2 - newTime) > MIN_TIME_SEG && 
				    				(previousSeg == null || Math.abs(newTime - previousSeg.time) > MIN_TIME_SEG)) {
			    				// consecutive segments should have time interval greater than MIN_TIME_SEG 
			    				TimeSegment newSeg = new TimeSegment();
					    		newSeg.time = newTime;
					    		
					    		// set values for newSeg2
					    		TimeSegment newSeg2 = new TimeSegment();
					    		newSeg2.time = newTime2;
			    				newSeg2.refTimeFalse.add(startingTime);
			    				newSeg2.prob =0;
			    				
			    				//copy ref times from targetSeg to newSeg
			    				for(float time : targetSeg.refTimeTrue) {
			    				      	if (time <= refTime1)  {
			    				      		newSeg.refTimeTrue.add(time);			    				      		
			    				      	}
			    				}
			    				
			    				//remove ref times from targetSeg
			    				for(float time : newSeg.refTimeTrue) {
			    					targetSeg.refTimeTrue.remove(time);
			    				}
			    				
			    				newSeg.prob = 1;
			    				node.timeSegList.add(newSeg);
			    				node.timeSegList.add(newSeg2);
					    		sortNodeTimeSegmentList(node);
					    		mergeNodeTimeSegmentList(node);
			    			
			    				
			    			} else {
			    				//segments have time interval <= MIN_TIME_SEG
			    				updateSeg(targetSeg, startingTime, isToHome);
				    			System.out.println("updateTimeSegList():  with in MIN_TIME_SEG, don't split..."+ 
			    				"newTime:" + newTime + ";newTime2:"+ newTime2 + ";targetSeg.time:" +targetSeg.time + ";previousSeg.time:" + previousSeg.time);
			    			}		    			
			    		}
		    		}		    		    		
		    	} else if (targetSeg.prob==0 && isToHome) {

		    		// may need to split segment
		    		
		    		//scan the refTimeTrue list to get the refTime
		    		float refTime1 = -1;
		    		float refTime2 = -1;
		    		for(float time: targetSeg.refTimeFalse) {
		    			if(startingTime > time) {
		    				refTime1 = time;
		    			} else {
	                           refTime2 = time;
	                           break;
		    			}
		    		}
		    		float newTime = -1;
		    		float newTime2 = -1;
		    		int refTimeFlag = -1;
		    		if(Math.abs(startingTime - refTime1) < NON_SPLIT_THRESH || Math.abs(refTime2 -startingTime) < NON_SPLIT_THRESH ) {
		    			// simple strategy: if with in non split thresh then don't split, may improve later
		    			updateSeg(targetSeg, startingTime, isToHome);
		    			System.out.println("updateTimeSegList(): with in NON_SPLIT_THRESH, don't split..."+ 
		    			"current data:" + startingTime + "; refTime1:" + refTime1 + ";refTime2:" + refTime2);
		    		} else {		    			
		    			if(refTime1 == -1) {
		    				refTimeFlag = 2;
			    			newTime = (refTime2 + startingTime)/2;		    			
			    		} else if (refTime2 == -1){
			    			newTime = (refTime1 + startingTime)/2;
			    			refTimeFlag = 1;
			    		} else {
			    			//between two ref time
			    			refTimeFlag = 0;// refTimeFlag = 0 means there are two refTime
			    			newTime = (refTime1 + startingTime)/2;
			    			newTime2 = (refTime2 + startingTime)/2;  		
			    		}

			    		if(newTime2 == -1) {
			    			if(Math.abs(newTime - targetSeg.time) > MIN_TIME_SEG && 
				    				(previousSeg == null || Math.abs(newTime - previousSeg.time) > MIN_TIME_SEG)) {
			    				// consecutive segments should have time interval greater than MIN_TIME_SEG 
			    				TimeSegment newSeg = new TimeSegment();
					    		newSeg.time = newTime;
					    		if(refTimeFlag == 2) {
					    			newSeg.refTimeTrue.add(startingTime);
					    			newSeg.prob = 1;					    			
					    		} else if(refTimeFlag == 1) {
					    			for(float time: targetSeg.refTimeFalse) {
					    				newSeg.refTimeFalse.add(time);
					    			}
					    				newSeg.prob = 0;
					    				targetSeg.refTimeFalse.clear();
					    				targetSeg.refTimeTrue.add(startingTime);
					    				targetSeg.prob = 1;					    			
					    		}
					    		
					    		node.timeSegList.add(newSeg);
					    		sortNodeTimeSegmentList(node);
					    		mergeNodeTimeSegmentList(node);
			    			
			    			} else {
			    				updateSeg(targetSeg, startingTime, isToHome);
				    			System.out.println("updateTimeSegList():  with in MIN_TIME_SEG, don't split..."+ 
			    				"newTime:" + newTime + ";targetSeg.time:" +targetSeg.time + ";previousSeg.time:" + previousSeg.time);
			    			}
			    		} else {
			    			//handle the case that there are two ref times
			    			if(Math.abs(newTime2 - targetSeg.time) > MIN_TIME_SEG && 
			    					Math.abs(newTime2 - newTime) > MIN_TIME_SEG && 
				    				(previousSeg == null || Math.abs(newTime - previousSeg.time) > MIN_TIME_SEG)) {
			    				// consecutive segments should have time interval greater than MIN_TIME_SEG 
			    				TimeSegment newSeg = new TimeSegment();
					    		newSeg.time = newTime;
					    		
					    		// set values for newSeg2
					    		TimeSegment newSeg2 = new TimeSegment();
					    		newSeg2.time = newTime2;
			    				newSeg2.refTimeTrue.add(startingTime);
			    				newSeg2.prob =1;
			    				
			    				//copy ref times from targetSeg to newSeg
			    				for(float time : targetSeg.refTimeFalse) {
			    				      	if (time <= refTime1)  {
			    				      		newSeg.refTimeFalse.add(time);			    				      		
			    				      	}
			    				}
			    				
			    				//remove ref times from targetSeg
			    				for(float time : newSeg.refTimeFalse) {
			    					targetSeg.refTimeFalse.remove(time);
			    				}
			    				
			    				newSeg.prob = 0;
			    				node.timeSegList.add(newSeg);
			    				node.timeSegList.add(newSeg2);
					    		sortNodeTimeSegmentList(node);
					    		mergeNodeTimeSegmentList(node);
			    			
			    				
			    			} else {
			    				//segments have time interval <= MIN_TIME_SEG
			    				updateSeg(targetSeg, startingTime, isToHome);
				    			System.out.println("updateTimeSegList():  with in MIN_TIME_SEG, don't split..."+ 
			    				"newTime:" + newTime + ";newTime2:"+ newTime2 + ";targetSeg.time:" +targetSeg.time + ";previousSeg.time:" + previousSeg.time);
			    			}		    			
			    		}
		    		}		    		    		

		    		
		    	} else {
		    		System.out.println("updateTimeSegList():  prob of targetSeg is not 1 or 0, don't split.." + 
		    				"targetSeg.prob:" + targetSeg.prob + ";targetSeg.time:" +targetSeg.time + ";refTimeTrue.size:"
		    				+ targetSeg.refTimeTrue.size()+ ";refTimeFalse.size:" + targetSeg.refTimeFalse.size());

		    		// the segment has 0 < prob < 1, no need to split
		    		updateSeg(targetSeg, startingTime, isToHome);
		    	}
		    }    
		}		    
	}
	
	public static void mergeNodeTimeSegmentList(Node node) {
	    boolean needMerge = true;
	    
	    //System.out.println("timeSegList.size:" + node.timeSegList.size());
	    while(needMerge) {
	    	
	    	boolean isFound = false;
		    int startIndex = -1;
		    int endIndex = -1;
		    float currentSegProb =-1;
		    
	    	for(int i=0; i < node.timeSegList.size(); i++) {	
	    		TimeSegment seg = node.timeSegList.get(i);       
		        if(isFound) {
		        	if(currentSegProb == seg.prob) {
		        		endIndex =i;
		        	} else {
		        		break;
		        	}
		        } else {
		        	if(currentSegProb == seg.prob ) {
	            	isFound = true;
	            	endIndex = i;
	                } else {
	            	currentSegProb = seg.prob;
	            	startIndex = i;
	            
	                }
	    	    }
		    }
	    	
	    	if(isFound) {
	    		//Merge TimeSegment from startIndex to endIndex
	    		TimeSegment newSeg = new TimeSegment(); 
	    		newSeg.time = node.timeSegList.get(endIndex).time;
                newSeg.prob = node.timeSegList.get(startIndex).prob;
	    		
                for(int i=startIndex ; i <= endIndex ; i++) {
	    			TimeSegment seg = node.timeSegList.get(i);
	    			for(float time : seg.refTimeFalse) {
	    				newSeg.refTimeFalse.add(time);
	    			}
	    			for(float time : seg.refTimeTrue) {
	    				newSeg.refTimeTrue.add(time);
	    			}
	    		}
                int removedNo = 0;
                for(int i= 1 ; i <= (endIndex- startIndex + 1) ; i++) {
                    node.timeSegList.remove(startIndex);
                    removedNo++;
                }
                node.timeSegList.add(newSeg);
                sortNodeTimeSegmentList(node);
                	    		
	    	} else {
	    		needMerge = false;
	    	}
	    	
	    }
	}
	
	public static void sortNodeTimeSegmentList(Node node) {
	    Collections.sort(node.timeSegList, new Comparator<TimeSegment>() {
            public int compare(TimeSegment o1, TimeSegment o2) {
                return Float.compare(o1.time, o2.time);
            }
        });
	}
	
	public static void updateSeg(TimeSegment seg, float startingTime, boolean isToHome) {
		if(isToHome) {
			seg.refTimeTrue.add(startingTime);
			Collections.sort(seg.refTimeTrue);
			
		} else {
			seg.refTimeFalse.add(startingTime);
			Collections.sort(seg.refTimeFalse);
		}
		  //recalculate probability		    			
		recalculateProbForSeg(seg);
	}
	
	
	public static void recalculateProbForSeg(TimeSegment seg) {
		seg.prob = (float)seg.refTimeTrue.size()/(float)(seg.refTimeTrue.size()+ seg.refTimeFalse.size());
	}
	
	/**
	 * update the Probability List of node with a next node ID
	 * @param node the node need to be updated
	 * @param nextID id of the next visited node
	 */
	public static void updateProList(Node node, int nextID){
		
		float startingTime = getExactHourOfDay(travelStartingTime);
		
		if(node.nextVisitCount == 0) {
			//probability list is empty
			Probability pro = new Probability();
			pro.id = nextID;
			pro.probability = 1;
			pro.visitCount = 1;
			//System.out.println("debug.........................." + travelEndingTime.getTime() - travelStartingTime.getTime());
			pro.averageStartingTime = startingTime;
			pro.averageTravelTime = travelEndingTime.getTime() - travelStartingTime.getTime();
			pro.maxStartingTime = startingTime;
			pro.minStartingTime = startingTime;
			pro.standardDeviationTime = 0;
			pro.startingTimeArr.add(startingTime) ;
			node.proList.add(pro);
			node.nextVisitCount ++;
		} else {
		    node.nextVisitCount ++;
			Probability pro = getProByID(node.proList, nextID);

			if( pro != null) {
				//node with nextID is already in the probability list 
				
				pro.visitCount ++;
				pro.averageStartingTime = (pro.averageStartingTime * (pro.visitCount - 1) + startingTime)/pro.visitCount;
		        pro.averageTravelTime = (pro.averageTravelTime * (pro.visitCount-1) + (travelEndingTime.getTime() - travelStartingTime.getTime()))/pro.visitCount;
		        pro.maxStartingTime = pro.maxStartingTime >= startingTime ? pro.maxStartingTime : startingTime;
		        pro.minStartingTime = pro.minStartingTime <= startingTime ? pro.minStartingTime : startingTime;
		        pro.startingTimeArr.add(startingTime);
		        pro.standardDeviationTime = calculateStandardDeviation(pro.startingTimeArr, pro.averageStartingTime, pro.visitCount);
		        
			    //update the probability of each node
			    Iterator<Probability> iter = node.proList.iterator();
				while (iter.hasNext()) {
					Probability curPro =  (Probability) iter.next();
					curPro.probability =  curPro.visitCount/(double)node.nextVisitCount;
				}
				
			} else {
				//node with nextID is not in the probability list 
				
				//create a Probability object
				Probability newPro = new Probability();
				newPro.id = nextID;

				newPro.averageStartingTime = startingTime;
				newPro.averageTravelTime = travelEndingTime.getTime() - travelStartingTime.getTime();
				newPro.maxStartingTime = startingTime;
				newPro.minStartingTime = startingTime;
				newPro.standardDeviationTime = 0;
				newPro.startingTimeArr.add(startingTime) ;
				
				newPro.visitCount = 1;
				node.proList.add(newPro);
				
				 //update the probability of each node
			    Iterator<Probability> iter = node.proList.iterator();
				while (iter.hasNext()) {
					Probability curPro =  (Probability) iter.next();
					curPro.probability =curPro.visitCount/(double)node.nextVisitCount;
				}
				
			}
	
		}
	}
	

	public static Probability getProByID(ArrayList<Probability> proList,int id) {
		Probability targetPro = null;
		Iterator<Probability> iter = proList.iterator();
		while (iter.hasNext()) {
			Probability pro = (Probability) iter.next();
			if (pro.id == id) {
				targetPro = pro;
				break;
			}
		}
		return targetPro;
	}
	
   public static void printNodeTimeSegList(Node node) {
		
		//for (TimeSegment seg :  node.timeSegList) {
		for(int i =0 ; i < node.timeSegList.size(); i++ ) {
			TimeSegment seg = node.timeSegList.get(i);
			System.out.println("**********************************");
			System.out.println("         seg.time:" +seg.time );
			System.out.println("         seg.prob:" +seg.prob);
			System.out.println("         " +getSegRefTimes(seg));
			System.out.println("**********************************");
		}
	}
   
    public static String getSegRefTimes(TimeSegment seg) {
    	String refTimeTrueStr = "true samples :";
    	for (float time : seg.refTimeTrue) {
    		refTimeTrueStr += time + "/" ;
    	}
    	String refTimeFalseStr = "false samples:";
    	for (float time : seg.refTimeFalse) {
    		refTimeFalseStr += time + "/" ;
    	}
    	return refTimeTrueStr + "\n" + "         "  + refTimeFalseStr  ;
    }
	
	/*  print node list
	 *  @param i specifies which nodeList to search 0 for nodeList, 1 for nodeListWeekday1, 2 for nodeListWeekday2, 3 for nodeListWeekend1, 4 for nodeListWeekend2
	 */
	public static void printNodeList(int i) {
		Writer writer = null;
		DecimalFormat df = new DecimalFormat("#");
		String fileName;
		Iterator<Node> iter;	
		switch (i) {
		case 0:
			fileName = "nodeList.txt";
			iter = nodeList.iterator();	
			break;
		case 1:
			fileName = "nodeListWeekday1.txt";
			iter = nodeListWeekday1.iterator();	
			break;
		case 2:
			fileName = "nodeListWeekday2.txt";
			iter = nodeListWeekday2.iterator();	
			break;
		case 3:
			fileName = "nodeListWeekend1.txt";
			iter = nodeListWeekend1.iterator();	
			break;
		case 4:
			fileName = "nodeListWeekend2.txt";
			iter = nodeListWeekend2.iterator();	
			break;
		default:
			return;			
		}
		try {
			 writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(fileName), "utf-8"));
	
		
		System.out.println("**********************total time of the raw data: " + (double) timeElapsed/(60*60*1000) + "*************************");
		System.out.println("**********************************************************************************");
		System.out.println("************************Node List " + fileName + " begin**********************************");
		System.out.println("**********************************************************************************");
		System.out.println("\n" + "node ID" + "    " + "node latitude" +  "    " + "node longitude" + "     " +"visit Count " + "    " +"average stay"  + "    "+ "next visit count" +  "    " + "    " + "probability list(id,(pro,travel,starting,deviation))");
	/*	Iterator<Node> iter2 = nodeList.iterator();	
		while (iter2.hasNext()) {
			Node node = (Node) iter2.next();
		
			writer.write(node.id + "("+ df.format((double)node.averageStayTime/(double)60000) +")" +"\r\n");
		}*/
	
		while (iter.hasNext()) {
			Node node = (Node) iter.next();
		
			BigDecimal bLat  = new BigDecimal(node.coor.latitude); 
		    double lat = bLat.setScale(6,   BigDecimal.ROUND_HALF_UP).doubleValue();  
			
		    BigDecimal bLon  = new BigDecimal(node.coor.longitude); 
		    double lon = bLon.setScale(6,   BigDecimal.ROUND_HALF_UP).doubleValue();  
			
			writer.write(node.id + ":" + covertArraytoString(node.proList) +"\r\n");
			System.out.println(node.id + "          "  + lat + "           "  + lon + "         " + node.visitCount +"               "+ node.averageStayTime/(1000*60) +"              "+ node.nextVisitCount + "                 " + covertArraytoString(node.proList));
		}
		
		System.out.println("**********************************************************************************");
		System.out.println("*******************************Node List " + fileName + "end*********************************");
		System.out.println("**********************************************************************************");
	    
		if(i==0) {
			for(Node node : nodeList) {
			  System.out.println("############################ Time segments of node id :" +node.id + " begin #############################");
			  printNodeTimeSegList(node);
			  System.out.println("############################ Time segments of node id :" +node.id + " end #############################\n\n");			  
			}
		}
		
		} catch (IOException ex) {
			System.err.println(ex.getMessage());
			 System.out.println("error catched at position 4");

		} finally {
			try {
				writer.close();
			} catch (Exception ex) {
				System.err.println(ex.getMessage());
				 System.out.println("error catched at position 5");
			}
		}
	}
	
	
	public static String covertArraytoString(ArrayList<Probability> array){
		DecimalFormat df = new DecimalFormat("#.##");
		String arrStr = new String();
		if (array.isEmpty()) {
			arrStr = "null";
		} else {
			Iterator<Probability> iter = array.iterator();
			while (iter.hasNext()) {
				Probability pro = (Probability) iter.next();
				BigDecimal bPro  = new BigDecimal(pro.probability); 
			    double probability = bPro.setScale(3,   BigDecimal.ROUND_HALF_UP).doubleValue();  			    
				arrStr += pro.id + ",(" + probability + "/"  + df.format((double)pro.averageTravelTime/(double)60000) + "/" + df.format(pro.averageStartingTime) + "/" + df.format(pro.standardDeviationTime) + "/" +df.format(pro.visitCount)+ ") --> " ;
				}
		}
		return arrStr;
	}
	
	public static float getExactHourOfDay(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE);
		float exactHour = (float) (hour + (float)minute /60.0);
		return exactHour;
	}
	
	public static float calculateStandardDeviation(ArrayList<Float> arr, float aver, int count) {
		double sum = 0;
		Iterator<Float> iter = arr.iterator();
		while(iter.hasNext()) {
			float x = (float) iter.next();
			sum += Math.pow((x - aver), 2);			
		}
		float deviation =(float) Math.sqrt(sum/count);
		return deviation;
	}
	
	/**
	 * make a copy of the node
	 * @param node
	 * @return
	 */
	public static Node getNodeCopy(Node node) {
		Node newNode = new Node();
		newNode.id = node.id;
		newNode.coor.latitude = node.coor.latitude;
		newNode.coor.longitude = node.coor.longitude;
		newNode.nextVisitCount = node.nextVisitCount;
		newNode.averageStayTime = node.averageStayTime;
		newNode.visitCount = node.visitCount;
		return newNode;
	}
	
	
/*	public static Probability getProbabilityByID(ArrayList<Probability> proList, int id) {
		Probability targetPro = null;
		Iterator<Probability> iter = proList.iterator();
		while (iter.hasNext()) {
			Probability pro = (Probability) iter.next();
			if (pro.id == id)
				targetPro = pro;
		}
		retrun targetPro;
	}*/
}
