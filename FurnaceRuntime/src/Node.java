import java.util.ArrayList;


public class Node {
	int id;
	Coordinate coor = new Coordinate();	
	ArrayList<Probability> proList = new ArrayList<Probability>();
	ArrayList<TimeSegment> timeSegList = new ArrayList<TimeSegment>(); 
	ArrayList<TimeSegment> timeSegListWeekends = new ArrayList<TimeSegment>();
	int nextVisitCount;
	int visitCount;
	long averageStayTime;
}
