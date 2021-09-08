import java.awt.*;
import java.util.ArrayList;

public class Route {
    String id;

    public ArrayList<Double> lat = new ArrayList<>();
    public ArrayList<Double> lon = new ArrayList<>();

    public Route(String id){
        this.id = id;
    }

    public void draw(Graphics2D g2, Dimension area, Location origin, double scale) {
        g2.setStroke(new BasicStroke(2));
        g2.setColor(Mapper.SEGMENT_COLOUR);

        int[] xPoints = new int[lat.size()];;
        int[] yPoints = new int[lat.size()];
        for(int i = 0; i < lat.size(); i++){
            Location l = Location.newFromLatLon(lat.get(i), lon.get(i));
            Point p = l.asPoint(origin, scale);
            xPoints[i] = p.x;
            yPoints[i] = p.y;
        }

        if((lat.size() & 1) == 0){
            for(int i = 1; i < lat.size(); i++){
                g2.drawLine(xPoints[i], yPoints[i], xPoints[i-1], yPoints[i-1]);
            }
        } else {
            for(int i = 0; i < lat.size() - 1;i++){
                g2.drawLine(xPoints[i], yPoints[i], xPoints[i+1], yPoints[i+1]);
            }
        }
    }
}
