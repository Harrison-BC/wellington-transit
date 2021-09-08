import java.awt.*;
import java.util.*;

/**
 * Node represents an intersection in the road graph. It stores its ID and its
 * location, as well as all the segments that it connects to. It knows how to
 * draw itself, and has an informative toString method.
 * 
 * @author tony
 */
public class Node{

	public final int nodeID;
	public final String name;
	public final Location location;

	public Node(int nodeID, String name, double lat, double lon) {
		this.nodeID = nodeID;
		this.name = name;
		this.location = Location.newFromLatLon(lat, lon);
	}


	public void draw(Graphics2D g2, Dimension area, Location origin, double scale) {
		g2.setStroke(new BasicStroke(3));
		// draw all the nodes.
		Point p = location.asPoint(origin, scale);

		// for efficiency, don't render nodes that are off-screen.
		if (p.x < 0 || p.x > area.width || p.y < 0 || p.y > area.height)
			return;

		int size = (int) (Mapper.NODE_GRADIENT * Math.log(scale) + Mapper.NODE_INTERCEPT);
		//g.fillRect(p.x - size / 2, p.y - size / 2, size, size);
		g2.fillOval(p.x - size / 2, p.y - size / 2, size, size);
	}

	public String toString() {
		return "Stop name: " + name + "  ID: " + nodeID + "  loc: " + location;
	}

	public boolean equals(Object s1) {
		if (this == s1) return true;
		if (!(s1 instanceof Node other)) return false;

		return this.nodeID == other.nodeID;
	}

	public int hashCode(){
		return (7919 * nodeID)% 72201;
	}
}