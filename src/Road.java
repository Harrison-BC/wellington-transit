import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * Road represents ... a road ... in our graph, which is some metadata and a
 * collection of Segments. We have lots of information about Roads, but don't
 * use much of it.
 * 
 * @author tony
 */
public class Road {
	public final int roadID;
	public final String name;
	Double[] lat;
	Double[] lon;
	ArrayList<Road> subRoads;

	public Road(int roadID, String name) {
		this.roadID = roadID;
		this.name = name;
		this.subRoads = new ArrayList<>();
	}

	public void draw(Graphics2D g2, Dimension area, Location origin, double scale) {
		g2.setColor(Color.GREEN.darker());
		g2.setStroke(new BasicStroke(1));
		if(!subRoads.isEmpty()){
			subRoads.forEach(b -> b.draw(g2, area, origin, scale));
		} else {

		int[] xPoints = new int[lat.length];
		int[] yPoints = new int[lon.length];

		for(int i = 0; i < lat.length; i++){
			Location l = Location.newFromLatLon(lat[i], lon[i]);
			Point p = l.asPoint(origin, scale);

			if (xPoints[i] < 0 || xPoints[i] > area.width || yPoints[i] < 0 || yPoints[i] > area.height)
				continue;

			xPoints[i] = p.x;
			yPoints[i] = p.y;

		}

		for(int i = 0; i < lat.length - 1; i++){
            // don't draw points that are off the screen
            if (xPoints[i] < 0 || xPoints[i] > area.width || yPoints[i] < 0 || yPoints[i] > area.height)
                continue;
            g2.drawLine(xPoints[i], yPoints[i], xPoints[i+1], yPoints[i+1]);
        }
	}
	}
}

// code for COMP261 assignments