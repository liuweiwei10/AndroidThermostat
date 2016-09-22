import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;



public class Test {
	
	
	public static void main(String[] args) {
		Node node = new Node();		
		MarkovAlgorithm.updateTimeSegList(node, true, 17.2f );
		MarkovAlgorithm.updateTimeSegList(node, false, 11.95f);
	    MarkovAlgorithm.updateTimeSegList(node, true, 16.9f);
	    
	    MarkovAlgorithm.updateTimeSegList(node, false, 10);
	    MarkovAlgorithm.updateTimeSegList(node, true, 13);
	    MarkovAlgorithm.updateTimeSegList(node,false,14.5f);
		printNodeTimeSegList(node);
	}
	
	
	public static void printNodeTimeSegList(Node node) {
		
		//for (TimeSegment seg :  node.timeSegList) {
		for(int i =0 ; i < node.timeSegList.size(); i++ ) {
			TimeSegment seg = node.timeSegList.get(i);
			System.out.println("*************************************************************");
			System.out.println("    seg.time:" +seg.time );
			System.out.println("    seg.prob:" +seg.prob);
			System.out.println(getSegRefTimes(seg));
			System.out.println("*************************************************************");
		}
	}
    public static String getSegRefTimes(TimeSegment seg) {
    	String refTimeTrueStr = "refTimeTrue :";
    	for (float time : seg.refTimeTrue) {
    		refTimeTrueStr += time + "/" ;
    	}
    	String refTimeFalseStr = "refTimeFalse:";
    	for (float time : seg.refTimeFalse) {
    		refTimeFalseStr += time + "/" ;
    	}
    	return refTimeTrueStr + "\n" + refTimeFalseStr  ;
    }
}
