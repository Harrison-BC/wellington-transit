import java.awt.*;
import java.io.File;
import java.util.*;

/**
 * This represents the data structure storing all the roads, nodes, and
 * segments, as well as some information on which nodes and segments should be
 * highlighted.
 * 
 * @author Harrison Blackburn Churcher & tony
 */
public class Graph {
	// map node IDs to Nodes.
	Map<Integer, Node> nodes;
	Map<String, Route> routes;
	Map<Integer, Boundary> boundaries;
	// map road IDs to Roads.
	Map<Integer, Road> roads;

	Node highlightedNode;
	Collection<Road> highlightedRoads = new HashSet<>();
	static Collection<Node> highlightedNodes = new HashSet<>();

	public Graph(File nodes, File roads, File routes, File boundaries) {
		this.nodes = Parser.parseNodes(nodes, this);
		this.roads = Parser.parseRoads(roads, this);
		this.routes = Parser.parseRoutes(routes, this);
		this.boundaries = Parser.parseBoundaries(boundaries, this);
	}


	public void draw(Graphics g, Dimension screen, Location origin, double scale) {


		// a compatibility wart on swing is that it has to give out Graphics
		// objects, but Graphics2D objects are nicer to work with. Luckily
		// they're a subclass, and swing always gives them out anyway, so we can
		// just do this.
		Graphics2D g2 = (Graphics2D) g;

		// draw all the segments.
		g2.setColor(Mapper.SEGMENT_COLOUR);
//		for (Segment s : segments)
//			s.draw(g2, origin, scale);

		// draw the segments of all highlighted roads.
		g2.setColor(Mapper.HIGHLIGHT_COLOUR);
		g2.setStroke(new BasicStroke(3));
		for (Road road : highlightedRoads) {
//			for (Segment seg : road.components) {
//				seg.draw(g2, origin, scale);
//			}
		}

		// draw all highlighted segments of.
		g2.setColor(Mapper.HIGHLIGHT_COLOUR);
		g2.setStroke(new BasicStroke(3));

		for (Boundary b : boundaries.values()) {
			b.draw(g2, screen, origin, scale);
		}

		for (Road road : roads.values()) {
			road.draw(g2, screen, origin, scale);
		}

		for (Route s : routes.values()){
			s.draw(g2, screen, origin, scale);
		}

		g2.setColor(Mapper.NODE_COLOUR);
		for (Node n : nodes.values())
			n.draw(g2, screen, origin, scale);

		// draw the highlighted node, if it exists.
		if (highlightedNode != null) {
			g2.setColor(Mapper.HIGHLIGHT_COLOUR);
			highlightedNode.draw(g2, screen, origin, scale);
		}
	}

	public Map<Integer, Node> getNodes() {
		return nodes;
	}

	public void setHighlight(Node node) {
		this.highlightedNode = node;
	}

	public void setHighlight(Collection<Road> roads) {
		this.highlightedRoads = roads;
	}
}