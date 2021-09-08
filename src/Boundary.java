import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class Boundary {
    int id;
    ArrayList<Double> lat;
    ArrayList<Double> lon;
    ArrayList<Boundary> smallerAreas;

    public Boundary(int id){
        this.id = id;
        lat = new ArrayList<>();
        lon = new ArrayList<>();
        smallerAreas = new ArrayList<>();
    }

    public void draw(Graphics2D g, Dimension area, Location origin, double scale) {
        if(!smallerAreas.isEmpty()) {
            // bounding box for each smaller area, if the boundingBox is not within screen, skip drawing it
            smallerAreas.forEach(b -> b.draw(g, area, origin, scale));
        }

        int[] xPoints = new int[lat.size()];
        int[] yPoints = new int[lon.size()];
//        int j = 0;
        for(int i = 0; i < lat.size(); i++){
            Location l = Location.newFromLatLon(lat.get(i), lon.get(i));
            Point p = l.asPoint(origin, scale);

            // TODO: prohibit the drawing of objects off screen to make it faster
//            if (p.x < 0 || p.x > area.width || p.y < 0 || p.y > area.height) {
//                continue;
//            }

            xPoints[i] = p.x;
            yPoints[i] = p.y;
//            j++;
        }

//        int[] xPointsSmaller = new int[j];
//        int[] yPointsSmaller = new int[j];

//        for(int i = 0; i < j; i++){
//            xPointsSmaller[i] = xPoints[i];
//            yPointsSmaller[i] = yPoints[i];
//        }

        g.setColor(new Color(173,223,173));
        Polygon p = new Polygon(xPoints, yPoints, xPoints.length);
        g.fillPolygon(p);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Boundary boundary = (Boundary) o;

        if (id != boundary.id) return false;
        if (!Objects.equals(lat, boundary.lat)) return false;
        if (!Objects.equals(lon, boundary.lon)) return false;
        return Objects.equals(smallerAreas, boundary.smallerAreas);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (lat != null ? lat.hashCode() : 0);
        result = 31 * result + (lon != null ? lon.hashCode() : 0);
        result = 31 * result + (smallerAreas != null ? smallerAreas.hashCode() : 0);
        return result;
    }
}
